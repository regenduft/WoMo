package womo;

import java.io.*;
import java.util.*;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.*;
import org.w3c.dom.*;
import org.w3c.dom.traversal.*;
import org.xml.sax.*;

import womo.spec.*;

/**
 * This class has methods to instrument a bpel-process and the deployment descriptor.
 * @author Florian Jostock
 *
 */
public class ProcessInstrumenter {
	
	private Document doc;
	private DOMParser parser;
	private String name;
	protected HashSet<String> noActivity = new HashSet<String>();

	/**
	 * creates a new Processinstrumenter
	 *
	 */
	public ProcessInstrumenter(){
		parser = new DOMParser();
		noActivity.add("variables");
		noActivity.add("correlationSets");
		noActivity.add("partnerLinks");
		noActivity.add("eventHandlers");
		noActivity.add("faultHandlers");
		noActivity.add("compensationHandlers");
	}
	
	/**
	 * Reads a xml-bpel-prozess and instruments it by the given spec
	 * @param os Outputstream to write the instrumented Prozess to
	 * @param reader InputStream to read the xml-bpel-process from
	 * @param spec Specification with the requirements for this process
	 * @param name Name of this process within the workflow-monitor
	 * @throws IOException
	 * @throws SAXException
	 */
	public void writeInstrumented(Writer out, Reader in, Specification spec, String name) throws IOException, SAXException{
		linkNr = 1;
		this.name = name;
		parser.parse(new InputSource(in));
		doc = parser.getDocument();
		
		MultiMap<String,Requirement> specMap = new MultiMap<String,Requirement>();
		for (Requirement r: spec.getRequirements()) {
				specMap.put(r.getActivityName(), r);
		}
		Map<String,Binding> addyMap = new HashMap<String,Binding>();
		for (Binding b: spec.getBindings()) {
				addyMap.put(b.getPlink(), b);
		}

		Element root = doc.getDocumentElement();
		root.setAttribute("xmlns:nswomo", "http://womo");
		root.setAttribute("xmlns:nswomoxsd", "http://womo/xsd");
		root.setAttribute("xmlns:abx", "http://www.activebpel.org/bpel/extension");
		root.setAttribute("xmlns:wsa", "http://schemas.xmlsoap.org/ws/2003/03/addressing");
		
		instrumentSubtree(root, specMap, addyMap);
			
		XMLSerializer xmlout = new XMLSerializer(out, new OutputFormat(doc));
		xmlout.serialize(doc);
	}

	/**
	 * This method instruments a Process Deployment Descriptor (Pdd). It adds the references
	 * to the wsdl-files and the address for the partnerlink 
	 * @param in InputStream with the orignial pdd-xml-data
	 * @param out Outputstream to write the instrumented pdd to
	 * @throws IOException
	 * @throws SAXException
	 */
	public void instrumentPdd(Reader in, Writer out) throws IOException, SAXException{
		parser.parse(new InputSource(in));
		Document pdd = parser.getDocument();

		Element addr = pdd.createElement("wsa:Address");
		addr.appendChild(pdd.createTextNode("http://bandit.informatik.uni-kl.de:8080/womo/services/evtrcv"));
		Element svc = pdd.createElement("wsa:ServiceName");
		svc.setAttribute("PortName", "evtrcvSOAP11port_http");
		svc.appendChild(pdd.createTextNode("s:evtrcv"));

		Element endp = pdd.createElement("wsa:EndpointReference");
		endp.setAttribute("xmlns:s", "http://womo");
		endp.setAttribute("xmlns:wsa", "http://schemas.xmlsoap.org/ws/2003/03/addressing");
		endp.appendChild(addr);
		endp.appendChild(svc);
		
		Element prole = pdd.createElement("partnerRole");
		prole.setAttribute("endpointReference", "static");
		prole.appendChild(endp);

		Element plnk = pdd.createElement("partnerLink");
		plnk.setAttribute("name", "womolnk");
		plnk.appendChild(prole);

		pdd.getElementsByTagName("partnerLinks").item(0).appendChild(plnk);
		
		Element wsdl1 = pdd.createElement("wsdl");
		wsdl1.setAttribute("location", "http://bandit.informatik.uni-kl.de:8080/womo/womo-web/womo.wsdl");
		wsdl1.setAttribute("namespace", "http://womo");

		Element wsdl2 = pdd.createElement("wsdl");
		wsdl2.setAttribute("location", "http://bandit.informatik.uni-kl.de:8080/womo/womo-web/evtrcv.wsdl");
		wsdl2.setAttribute("namespace", "http://womo");
		
		pdd.getElementsByTagName("references").item(0).appendChild(wsdl1);
		pdd.getElementsByTagName("references").item(0).appendChild(wsdl2);
		
		XMLSerializer xmlout = new XMLSerializer(out, new OutputFormat(pdd));
		xmlout.serialize(pdd);
	}
	
	/**
	 * This method instruments the subtree of an xml-bpel-document. 
	 * It calls herself recursivly for each while-loop. The document,
	 * an each subtree of a while, may be put into a flow. So we can
	 * put parallel invokes to send events into this flow, too.   
	 * @param root Root node of the subtree, wich should be instrumented
	 * @param specMap Map of the names of activities to instrument to the requirements
	 * @param addyMap Map of the names of partnerlinks to bindings for this partnerlinks.
	 */
	protected void instrumentSubtree(Node root, MultiMap<String,Requirement> specMap, Map<String,Binding> addyMap){
		// This method calls herself recursivly for each while, and examines the complete
		// subtree of any <while> as it was an complete document.
		// So within these subtrees it creates new <flow> whith <links> and <invoke>s. The
		// instrumented aktivitias have links to these invokes. The cause it calls herself
		// recursivly for whiles and creates new <flow> etc within them if necessary, is 
		// that links may not leave a <while>.
		
		DocumentTraversal traversable = (DocumentTraversal) doc;
		TreeWalker walker = traversable.createTreeWalker(
				root, NodeFilter.SHOW_ELEMENT, new WhileChildFilter(root), true);
		Element node;
		
		// The following loop examines the complete bpel-document
		
		while ((node = (Element)walker.nextNode()) != null) {
			
			// if we find a while, call this method recursivly with the whil
			// as "root"-Node
			if ("while".equals(node.getLocalName())){
				instrumentSubtree(node, specMap, addyMap);
			}
			
			// add our variables to the global definition
			if ("variables".equals(node.getLocalName())){
				node.appendChild(myVar());
				node.appendChild(myLoopVar());
				for (String plink: addyMap.keySet()) {
					node.appendChild(arrayVar(plink));
					node.appendChild(counterVar(plink));
				}
			}
			
			// add our partnerLink to the global definition
			if ("partnerLinks".equals(node.getLocalName())){
				node.appendChild(myPlnk());
			}
			
			// check if we need to instrument this node 
			List<Requirement> specs = specMap.getList(node.getAttribute("name"));
			for (Requirement r : specs) {
				Node parent = node.getParentNode();
				
				// if we only react on failures, and 
				// report an event and/or take mesures
				if (r instanceof OnFailure) {
					OnFailure onf = (OnFailure) r;
					
					// To react on failures, we need a new scope
					// around the activity, we want to instrument and
					// a fault handler for this scope. We put a sequence
					// in the scope, to which we will add other activities
					// later
					Node seq = el("bpws:sequence");
					Node scope = 
						el("bpws:scope", 
								el("bpws:faultHandlers",
										el("bpws:catchAll",
												seq
										)
								)
						);
					
					// Now lets look for the "REDO"-action, in that case we need a loop
					HashMap<ActionType, Action> actions = new HashMap<ActionType, Action>(); 
					for ( Action act : onf.getActions() ) actions.put(act.getType(),act);
					
					if ( actions.containsKey(ActionType.REDO) ) {

						// If we have a REDO-action, we create a sequence, where
						// we initialize the loop-var and then enter the loop 
						Node outerSeq = el("bpws:sequence");
						outerSeq.appendChild(
							el("bpws:assign", el("bpws:copy", new Node[]{
								el("bpws:from", text("false")), 
								el("bpws:to", attr("variable", WOMOLOOPVAR))
							}))
						);
						
						// oh - if we have alternative bindings defined for this
						// partnerlink, we need to put them into an array with all
						// the bindings (before the loop), and iterate trough this 
						// array later (within the loop)
						if (actions.containsKey(ActionType.TOGGLE_PLINK))
							insertBindingUpdateActivities(node.getAttribute("partnerLink"), addyMap, outerSeq, seq);
						
					    // now, after initialzing, add the "while"-loop
						Element loop = el("bpws:while");
						outerSeq.appendChild(loop);
						loop.setAttribute("condition", "bpws:getVariableData('" + WOMOLOOPVAR + "')");
						
						// We now put the loop into the scope, and
						// replace the original node with the scope,
						// and put the original node into a sequence in the loop.
						loop.appendChild(scope);
						parent.insertBefore(outerSeq, node);
						parent.removeChild(node);
						scope.appendChild(
								el("bpws:sequence", new Node []{
										node,
										el("bpws:assign", el("bpws:copy", new Node[]{
												el("bpws:from", text("true")),
												el("bpws:to", new Node[] {
														attr("variable", WOMOLOOPVAR),
												})
										}))
								})
						);
					} else {
						
						// if we have no loop, we only put the scope around
						// the orininal node
						parent.insertBefore(scope, node);
						parent.removeChild(node);
						scope.appendChild(node);
					}
					
					// now lets add our failure-handling activities to the sequence
					// within the failure-handler of the scope
					Element assignNode = assignRecordToMyVar(onf.getRecord());
					seq.appendChild(assignNode);
					seq.appendChild(invokeEvt());
					if ( actions.containsKey(ActionType.ACTIVITY) ) {
						seq.appendChild(parseString(actions.get(ActionType.ACTIVITY).getActivity()));
					}
				}
				
				// if we want to report an event, but don't 
				// evaluate an assurance or take mesures
				if (r instanceof Log){
					Node seq = el("bpws:sequence");
					if (r.getPosition().equals(Position.ON_FAILURE)) {

						// if we react on failure, put the failure-handling
						// sequence into a failure-handler of a new scope...
						Node scope = 
							el("bpws:scope", 
								el("bpws:faultHandlers",
									el("bpws:catchAll",
										seq
									)
								)
							);
						
						// and put this scope around the original node
						parent.insertBefore(scope, node);
						parent.removeChild(node);
						scope.appendChild(node);
						
						// and add the failure-handling-activities into the
						// sequence
						Log l = (Log) r;
						Element assignNode = assignRecordToMyVar(l.getRecord());
						seq.appendChild(assignNode);
						seq.appendChild(invokeEvt());
					} else {
						
						// if the event occures every time, put a sequence around
						// the original node..
						parent.insertBefore(seq, node);
						parent.removeChild(node);
						Log l = (Log) r;
						Element assignNode = assignRecordToMyVar(l.getRecord());
						
						// "createParallelInvoke" puts the complete process
						// into a flow, and puts a new invoke to the event
						// receiver into this flow and returns a link to this
						// invoke
						assignNode.appendChild(createParallelInvoke(root));
						moveChildsWithName("target", node, seq);
						
						// ... now put the original node and the assign wich
						// collects the necessary data for this event into
						// the sequence
						if (r.getPosition().equals(Position.PRE)){
							seq.appendChild(assignNode);
							seq.appendChild(node);
						}
						if (r.getPosition().equals(Position.POST)) {
							seq.appendChild(node);
							seq.appendChild(assignNode);
						}
					}
				}
				
				// if we evaluate an assurance and take mesures
				if (r instanceof Assurance) {
					Node seq = el("bpws:sequence");
					Assurance ass = (Assurance) r;
					
					// First lets look for the "REDO"-action, in that case we need a loop
					HashMap<ActionType, Action> actions = new HashMap<ActionType, Action>(); 
					for ( Action act : ass.getActions() ) actions.put(act.getType(),act);
					if ( actions.containsKey(ActionType.REDO) ) {

						// If we have a REDO-action, we create a sequence, where
						// we initialize the loop-var and then enter the loop
						// we replace the original node with this sequence
						Node outerSeq = el("bpws:sequence");
						parent.insertBefore(outerSeq, node);
						parent.removeChild(node);
						seq.appendChild(node);
						outerSeq.appendChild(initAssuranceToMyVar());

						// oh - if we have alternative bindings defined for this
						// partnerlink, we need to put them into an array with all
						// the bindings (before the loop), and iterate trough this 
						// array later (within the loop).
						if (actions.containsKey(ActionType.TOGGLE_PLINK))
							insertBindingUpdateActivities(node.getAttribute("partnerLink"), addyMap, outerSeq, seq);
						
						// now append the loop to the sequence, and put the 
						// inner sequence (wich contains the original node) into
						Element loop = el("bpws:while");
						outerSeq.appendChild(loop);
						loop.setAttribute("condition", getMyVarPath("nswomoxsd:value"));
						loop.appendChild(seq);
					} else {
						
						// if there is no REDO, we only surround the original node
						// with a sequence
						parent.insertBefore(seq, node);
						parent.removeChild(node);
						seq.appendChild(node);
					}
					
					// finally add our mesures and Assurance-checking to the
					// (inner) sequence
					seq.appendChild(evalAssurance(ass));
					seq.appendChild(invokeEvt());
					if ( actions.containsKey(ActionType.ACTIVITY) ) {
						seq.appendChild(parseString(actions.get(ActionType.ACTIVITY).getActivity()));
					}
				}
			}	
		}
	}
	
	/**
	 * Parse a String that contains xml-data and return the root-element of this xml-tree, wich
	 * contains the complete tree. 
	 * @param str String with xml-data that should be parsed
	 * @return Element containing the complete dom-tree
	 */
	protected Element parseString(String str){
		try {
			parser.parse(str);
			return parser.getDocument().getDocumentElement();
		} catch (SAXException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		return el("failure", text("The workflow instrumenter could not insert an activity here, because" +
			"the code for the activitiy, that was supplied in the requirement specification was not correct!"));
	}
	
	/**
	 * Creates a parallel invoke activity, by puting the given root-nodes childs into a flow
	 * (if there root isn't a flow yet) and adding the invoke to this flow, and defining
	 * the new invoke as target of a link. The source of this link will be returned and 
	 * should be added to a child of the root.
	 * @param root Root Node, create Flow as Child and put all Childs and the Invoke into 
	 * @return Link Source Node, can be used to link to the parallel Invoke 
	 */
	protected Element createParallelInvoke(Node root){
		Node flow;
		Node links = null;
		Node child = root.getFirstChild();
		while ( child != null && noActivity.contains(child.getLocalName())) 
			child = child.getNextSibling();
		if (  child == null  ||  ! "bpws:flow".equals(child.getNodeName())  ) {
			// If there isn't a <bpws:flow>-Element yet, create it and move all child-activitys
			// into the new flow-Element. At the same time we can search a <bpws:links>-Element
			flow = el("bpws:flow");
			while (child != null){
				Node next = child.getNextSibling();
				root.removeChild(child);
				flow.appendChild(child);
				if (links == null && "bpws:links".equals(child.getNodeName())) {
					links = child;
				}
				child = next;
				while ( child != null && noActivity.contains(child.getLocalName())) 
					child = child.getNextSibling();
			}
			root.appendChild(flow);
		} else {
			// child was a <bpws:flow>-Element yet - so search, if there is a <bpws:links>-Element, too
			flow = child;
			child = flow.getFirstChild();
			while (links == null && child != null){
				if ("bpws:links".equals(child.getNodeName()))
					links = child;
				child = child.getNextSibling();
			}
		}
		// if we didn't find a <bpws:links>-Element, we create it now
		if (links == null) {
			links = el("bpws:links");
			flow.insertBefore(links, flow.getFirstChild());
		}
		String linkName = newLinkName();
		Node link = el("bpws:link", attr("name", linkName));
		links.appendChild(link);
		Node target = el("bpws:target", attr("linkName", linkName));
		Node invoke = invokeEvt();
		invoke.appendChild(target);
		flow.appendChild(invoke);
		return el("bpws:source", attr("linkName", linkName));
	}

	/**
	 * creates an assign-activity wich initializes the "womoEvtVar" with the
	 * correct event for a record and then copys the result of the records
	 * expression to the value-field.
	 * @param r Record containg the expression and record type to use 
	 * @return Element containg the assign activity
	 */
	protected Element assignRecordToMyVar(Record r) {
		Element an = el("bpws:assign", new Node[]{
				initMyVar(new Node[]{
					el("nswomoxsd:type", text(r.getType().toString())),
					el("nswomoxsd:value", text("0"))
				}),
				setProcId()
		});
		if (r.getType() == RecordType.FORMULAR)
			an.appendChild(setExpr(r.getFormular(), "nswomoxsd:value"));
		return an;
	}

	/**
	 * creates an assign-activity wich copys the results of the 
	 * records and the assurance to the womoEvtVar
	 * @param ass Assurance containing the records and the operator
	 * @return Element containing the assign activity
	 */
	protected Element evalAssurance(Assurance ass){
		Record r1 = ass.getLeftRecord();
		Record r2 = ass.getRightRecord();
		Operator op = ass.getOperator();
		Element an = el("bpws:assign");
		if (r1.getType() == RecordType.FORMULAR)
			an.appendChild(setExpr(r1.getFormular(), "nswomoxsd:leftValue"));
		if (r2.getType() == RecordType.FORMULAR)
			an.appendChild(setExpr(r2.getFormular(), "nswomoxsd:rightValue"));
		an.appendChild(
			setExpr(
				getMyVarPath("/nswomoxsd:leftValue") + 
				op.value() +
				getMyVarPath("nswomoxsd:rightValue"),
				"nswomoxsd:value"
			)
		);
		return an;
	}
	
	/**
	 * Moves all childs with this name from the "from" node to the "to" node
	 * @param name Name of the childs to move
	 * @param from Node, to search for childs with the name
	 * @param to Node, where this childs should be moved
	 */
	protected void moveChildsWithName(String name, Node from, Node to){
		Node nodeIterator = from.getFirstChild();
		FINDTARGETS:
		while (nodeIterator != null){
			if (name.equals(nodeIterator.getLocalName())) {
				from.removeChild(nodeIterator);
				to.appendChild(nodeIterator);
				break FINDTARGETS;
			}
			nodeIterator = nodeIterator.getNextSibling();
		}
	}
	
	/**
	 * Create a bpel-expression to access a part of the womoEvtVar 
	 * @param path end of the xml-path we want to access
	 * @return String containig getVariableData(womoEvtVar, part1, /receive/evt/path)
	 */
	protected String getMyVarPath(String path){
		return "getVariableData('"+VARNAME+"', 'part1', '/nswomoxsd:receive/nswomoxsd:evt/"+path+"') ";
	}
	
	/**
	 * creates an assign-activity wich initializes the  womoEvtVar
	 * for an assurance event 	 
	 * @return assign-activity
	 */
	protected Element initAssuranceToMyVar() {
		Element an = el("bpws:assign", new Node[]{
				initMyVar(new Node[]{
					el("nswomoxsd:type", text("Assurance")),
					el("nswomoxsd:leftValue", text("0")),
					el("nswomoxsd:rightValue", text("0")),
					el("nswomoxsd:value", text("0"))
				}),
				setProcId()
		});
		return an;
	}
	
	/**
	 * Inserts the activities to initialize and update partnerlinks bindings
	 * by an array of possible bindings.
	 * @param plink Name of the partnerlink to update (his dependent links will be updated, too)
	 * @param addyMap Map with the Bindings assigned to partnerlink-names
	 * @param addInitHere Node to append the activities for initializing the arrays to
	 * @param addUpdateHere Node to append the activities for updating the partnerlinks to
	 */
	protected void insertBindingUpdateActivities(String plink, Map<String,Binding> addyMap, Node addInitHere, Node addUpdateHere){
		if (addyMap.containsKey(plink)){
			Binding bind = addyMap.get(plink);
			addInitHere.appendChild(initArrayVar(bind, bind));
			addUpdateHere.appendChild(getNextBinding(plink));
			for (String dependentPlink: bind.getDependentPlinks()){
				if (addyMap.containsKey(dependentPlink)){
					addInitHere.appendChild(initArrayVar(addyMap.get(dependentPlink),bind));
					addUpdateHere.appendChild(getNextBinding(dependentPlink));
				}
			}
		}
	}

	
	/**
	 * creates an assign-activity, wich gets the next binding from a binding-array-variable
	 * and copys it to the partnerlink
	 * @param plink wich should be updated by this assign
	 * @return Element with assign-activity
	 */
	protected Element getNextBinding(String plink){
		return el("bpws:assign", new Node[]{
				el("bpws:copy", new Node[]{
					el("bpws:from", new Node[]{
						attr("variable", ARRAY_VAR_PRFX + plink),
						attr("query", "/wsa:addresses/wsa:EndpointReference[ bpws:getVariableData('"+COUNTER_VAR_PRFX + plink+"') ]")
					}),
					el("bpws:to", attr("partnerLink", plink))
				}),
				el("bpws:copy", new Node[]{
					el("bpws:from", new Node[]{
						attr("expression", "bpws:getVariableData('"+COUNTER_VAR_PRFX + plink+"') + 1")
					}),
					el("bpws:to", attr("variable", COUNTER_VAR_PRFX + plink))
				})
			});
	}
	
	/**
	 * Creates an assign-activity wicht initializes the binding-array-variable for
	 * this binding / partnerlink
	 * @param b Binding, the name of the partnerlink and the default values.
	 * @param addys This parameter will be ignored, if b has an array with
	 * 		svcAddresses with size > 0. If not, addys array will be used. In 
	 * 		combination with null values for service-name, namespace and port (wich
	 * 		will then be read from the default values in b), this can be used 
	 * 		to assign the same urls to several partnerlinks
	 * @return Element with assign-activity
	 */
	protected Element initArrayVar(Binding b, Binding addys){
		String plink = b.getPlink();
		Element fromLiteral = el("wsa:addresses");
		if (b.getAddresses()!= null && b.getAddresses().size()> 0) {
			// Ignore addys if b has own addresses
			addys = b;
		}
		for (SvcAddress a: addys.getAddresses()){
			fromLiteral.appendChild(
				el("wsa:EndpointReference", new Node[]{
					attr("xmlns:s", (a.getNs() != null && ! "".equals(a.getNs())) ? a.getNs() : b.getDefaultNs() ),
					attr("xmlns:wsa", "http://schemas.xmlsoap.org/ws/2003/03/addressing"),
					el("wsa:Address", text(a.getUrl())),
					el("wsa:ServiceName", new Node[]{
						attr("PortName", (a.getPort() != null && ! "".equals(a.getPort())) ? a.getPort() : b.getDefaultPort()),
						text("s:"+ ((a.getName() != null && ! "".equals(a.getName())) ? a.getName(): b.getDefaultName()))
					})
				})
			);
		}
		return
			el("bpws:assign", new Node[]{
				el("bpws:copy", new Node[]{
						el("bpws:from", fromLiteral),
						el("bpws:to", attr("variable", ARRAY_VAR_PRFX + plink))
				}),
				el("bpws:copy", new Node[]{
						el("bpws:from", text("1")),
						el("bpws:to", attr("variable", COUNTER_VAR_PRFX + plink))
				})
			})
		;
		
	}
	
	protected Integer linkNr;
	
	/**
	 * Generates a new name for a bpel-link-element "WomoEvtLnk" + serial number 
	 * @return String with a new link name
	 */
	protected String newLinkName(){
		return "WomoEvtLnk_" + linkNr++;
	}
	
	/**
	 * creates a new Element node.
	 * @param tag name of the Element
	 * @return Element-Node with this name
	 */
	protected Element el(String tag){
		return doc.createElement(tag);
	}

	/**
	 * creates a new Element node with child node or attribute i
	 * @param tag name of the Element
	 * @return Element-Node with this name
	 */
	protected Element el(String tag, Node i){
		Element e = doc.createElement(tag);
		if (i instanceof Attr) { 
			e.setAttributeNode((Attr)i);
		} else {
			e.appendChild(i);
		}
		return e;
	}

	/**
	 * creates a new Element node with child nodes and / or attributes
	 * @param tag name of the Element
	 * @return Element-Node with this name
	 */
	protected Element el(String tag, Node[] childs){
		Element e = doc.createElement(tag);
		for (Node i: childs){
			if (i instanceof Attr) { 
				e.setAttributeNode((Attr)i);
			} else {
				e.appendChild(i);
			}
		}
		return e;
	}
	
	/**
	 * creates a new Attribute node
	 * @param name name of the Attribute
	 * @param value value of the Attribute
	 * @return Attribute-Node
	 */
	protected Attr attr(String name, String value){
		Attr a = doc.createAttribute(name);
		a.setValue(value);
		return a;
	}
	
	/**
	 * creates a new Text node
	 * @param content text, contained in this node
	 * @return Text-Node
	 */
	protected Text text(String content){
		return doc.createTextNode(content);
	}

	protected static final String VARNAME = "womoEvtVar"; 
	protected static final String WOMOLOOPVAR = "womoLoopVar"; 
	protected static final String ARRAY_VAR_PRFX = "womoAddys"; 
	protected static final String COUNTER_VAR_PRFX = "womoAddyCounter"; 
	protected static final String PLNKNAME = "womolnk"; 
	
    /**
     * Creates a partner-link-Type-definition for the partner-link to the event receiver
     * Name of the partnerlink is defined in constant PLNKNAME
     * @return Element containing partnerLink-definition
     */
	protected Element myPlnk(){
		return el("bpws:partnerLink", new Node[]{
				attr("partnerLinkType", "nswomo:evtrcvType"),
				attr("name", PLNKNAME),
				attr("partnerRole", "service")
		});
	}
	
	/**
	 * Creates a variable-deklaration for an integer variable with the name
	 * womoAddyCounter+suffix (prefix defined in COUNTER_VAR_PREFIX)
	 * @param suffix Suffix to append to the variable name
	 * @return Element containing variable deklaration
	 */
	protected Element counterVar(String suffix){
		return el("bpws:variable", new Node[]{
				attr("type", "xsd:integer"),
				attr("name", COUNTER_VAR_PRFX + suffix)
		});
	}

	/**
	 * Creates variable deklaration for variable of the element type 
	 * wsa:addresses (this type is not part of wsa, it has to be 
	 * deklared in some wsdl-file that ist imported into the process) with
	 * the name womoAddys+suffix (prefix defined in ARRAY_VAR_PREFIX)
	 * @param suffix Suffix to append to the variable name
	 * @return Element containing variable deklaration
	 */
	protected Element arrayVar(String suffix){
		return el("bpws:variable", new Node[]{
				attr("element", "wsa:addresses"),
				attr("name", ARRAY_VAR_PRFX + suffix)
		});
	}
	
	/**
	 * Creates variable deklaration for variable of message type
	 * nswomo:receiveMessage that can be send to the event receiver.
	 * Name of the variable womoEvtVar (defined in constant VARNAME)
	 * @return Element containing variable deklaration
	 */
	protected Element myVar(){
		return el("bpws:variable", new Node[]{
				attr("messageType", "nswomo:receiveMessage"),
				attr("name", VARNAME)
		});
	}
	
	/**
	 * Creates variable deklaration for boolean variable with name
	 * womoLoopVar (defined in constant WOMOLOOPVAR)
	 * @return Element containing variable deklaration
	 */
	protected Element myLoopVar(){
		return el("bpws:variable", new Node[]{
				attr("type", "xsd:boolean"),
				attr("name", WOMOLOOPVAR)
		});
	}
	
	/**
	 * Creates bpel-invoke-activity that sends contents of womoEvtName (VARNAME)
	 * to the event receiver (partnerlink PLNKNAME) 
	 * @return Element containing invoke-activity
	 */
	protected Element invokeEvt(){
		return el("bpws:invoke", new Node[]{
				attr("operation", "receive"),
				attr("inputVariable", VARNAME),
				attr("partnerLink", PLNKNAME),
				attr("portType", "nswomo:evtrcvPortType")
		});
	}
	
	/**
	 * Creates copy-part to insert into the assign operation, wich
	 * initializes the womoEvtVar (VARNAME) with the nodes
	 * processName and processId, and the node evt containing the
	 * nodes given with parameter "data"
	 * @param data Nodes to put as contents of the evt-node
	 * @return Element containing copy-part of assign-activity
	 */
	protected Element initMyVar(Node[] data){
		return 		
			el( "bpws:copy", new Node[] {
				el(	"bpws:from",
					el("nswomoxsd:receive", new Node[]{
						el("nswomoxsd:processName", text(this.name)),
						el("nswomoxsd:processId", text("0")),
						el("nswomoxsd:evt", data)
					})
				),
				el(	"bpws:to", new Node[]{
					attr("variable", VARNAME),
					attr("part", "part1")
				})
			});
	}
	
	/**
	 * creates copy-part of assign activity wich copys the process-id
	 * from an expression to the womoEvtVar (VARNAME). 
	 * @return Element containing copy-part of assign operation
	 */
	protected Element setProcId(){
		return el("bpws:copy", new Node[]{
				el("bpws:from", attr("expression", "abx:getProcessId()")),
				el(	"bpws:to", new Node[]{
						attr("variable", VARNAME),
						attr("part", "part1"),
						attr("query", "/nswomoxsd:receive/nswomoxsd:processId")
					})
		});
	}

	/**
	 * Creates a copy operation wich copys the expression to the given node
	 * that is part of the evt-node of the womoEvtVar (VARNAME)
	 * @param expr Expression to evaluate and copy
	 * @param to_node Node that is contained in the subtree of the evt-node of womoEvtVar
	 * @return Element containing copy-part of assign-operation
	 */
	protected Element setExpr(String expr, String to_node){
		return el("bpws:copy", new Node[]{
				el("bpws:from", attr("expression", "abx:getProcessId()")),
				el(	"bpws:to", new Node[]{
						attr("variable", VARNAME),
						attr("part", "part1"),
						attr("query", "/nswomoxsd:receive/nswomoxsd:evt/" + to_node)
					})
		});

	}
	
	/**
	 * This class can be used to filter all childs of a while from
	 * a DOM-Tree
	 * @author Florian Jostock
	 *
	 */
	class WhileChildFilter implements NodeFilter{

		private Node root;
		
		/**
		 * Creates a new WhileChildFilter.
		 * The childs of the root node will never be ignored, even if the root is a while.
		 * @param root node wich should not be ignored
		 */
		WhileChildFilter(Node root){
			this.root = root;
		}
		
		public short acceptNode(Node n) {
			if (n != null && n.getParentNode() != null && "while".equals(n.getParentNode().getLocalName()) && this.root != n.getParentNode()) {
				return FILTER_REJECT;
			}
			return FILTER_ACCEPT;
		}
		
	}
}
