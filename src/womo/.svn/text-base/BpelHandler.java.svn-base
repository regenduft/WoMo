package womo;

import java.io.*;
import java.util.HashSet;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class is a Framework for parsing XML-data. 
 * Its output is HTML-Formated XML-Data to be displayed with a Webbrowser. 
 * One can specifiy how XML-Data should be reformated by overwriting the 
 * openTag, startAttributes, attribute, endAttributes, prepareChild and closeTag-methods.
 * @author f_jostoc
 */
public class BpelHandler extends DefaultHandler {

	/**
 	* Contructs a new BpelHandler - printig all reformated XML-data to "out" 
 	* @param out print XML-data here
 	*/	
	public BpelHandler(Writer out, String linkPrefix, String plinkPrefix, Map<String, Integer> requirements) {
		super();
		this.out = out;
		ignoreTags.add("bpws:variables");
		//ignoreTags.add("bpws:partnerLinks");
		ignoreTags.add("bpws:correlationSets");
		ignoreTags.add("bpws:correlations");
		//ignoreTags.add("bpws:assign");
		strongAttributes.add("name");
		
		// HTML-spefific
		this.linkPrefix = linkPrefix;
		this.plinkPrefix = plinkPrefix;
		this.requirements = requirements;
	}

	// HTML-specific
	/** Prefix to prepend before links, that get generated for all strong Attributes */
	protected String linkPrefix;
	protected String plinkPrefix;
	protected Map<String, Integer> requirements;

	/** Set to true by prepareChilds-method, so when closing the tag later we'll know we had a Child*/
	protected boolean hasChild = true;

	
	// not HTML-specific
	/** Attributes with this names will be printed before other attributes formatted by strongAttribute	 */
	protected HashSet<String> strongAttributes = new HashSet<String>();

	/** This is a list of tags we will not print (and their childs).  */
	protected HashSet<String> ignoreTags = new HashSet<String>();

	/** If greater than zero, we are ignoring (not printing) the actual tag and his childs */
	protected int ignoreLevel = 0;
	
	/** The Writer to print the Output to. */
	protected Writer out;

	
	/**
	 * Prints the string. Usally to the writer "out".
	 * @param str String to print.
	 */
	protected void print(String str){
		try { 
			out.write(str);
		} catch (IOException e) {e.printStackTrace();} 
	}
	
	
	// HTML SPECIFIC METHODS
	// ------------------------------------------
	/**
	 * prints an opening tag, called when tag-open is read
	 * @param name of the tag, wich was just openend
	 */
	protected void openTag(String name) {
		print("<li>&lt;<strong>" + name + "</strong> ");
		hasChild=false;
	}
	
	/**
	 * prints String to prepend the attributes, called after openTag and before attribute
	 */
	protected void startAttributes(){
		print("<font size=-1>");
	}
	
	/**
	 * prints formated string for this attribute, called before attribute.. 
	 * @param name name of the attribute to print out
	 * @param value value of the attribute to print out
	 */
	protected void strongAttribute(String name, String value, String tag){
		if ("bpws:partnerLink".equals(tag)) {
			print(name+"=\"<strong><a href=\"" + plinkPrefix + value + "\" >" + value + "</a></strong>\" ");
		} else {
			print(name+"=\"<strong><a href=\"" + linkPrefix + value + "\" >" + value + "</a>");
			if (requirements.containsKey(value)) {
				for (int i = 0; i < requirements.get(value); i++) {
					print(" <a href=\"" + linkPrefix + value + "&requirement=" + i + "\" >" + i + "</a> ");
				}
			}
			print("</strong>\" ");
		}
	}

	/**
	 * prints formated string for this attribute 
	 * @param name name of the attribute to print out
	 * @param value value of the attribute to print out
	 */
	protected void attribute(String name, String value){
		print(name + "=\"" + value + "\" ");
	}
	
	/** 
	 * finalze printing attributes, called after attribute and before closeTag/prepareChilds
	 */
	protected void endAttributes(){
		print("</font>");
	}
	
	/**
	 * prepare printing childs of this element, called before the openTag-call for the child
	 */
	protected void prepareChilds(){
		if (!hasChild) { print("&gt;\n<ul>\n"); hasChild = true;}
	}	
	
	/**
	 * finalize printing this element, called after all childs were printed (if any)
	 * @param name Elements name
	 */
	protected void closeTag(String name){
		if (hasChild) {print("\n</ul>&lt;<strong>/" + name + "</strong>&gt;</li>\n");}
		else {print("/&gt;</li>\n");}
		hasChild=true;
	}

	public void startDocument(){
		print ("\n<ul>");
	}
	
	/**
	 * Flushes the output-writer! Important, don't forget if this method gets overwriten!!
	 */
	public void endDocument(){
		print("</ul>\n");
		try {
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// GENERIC METHODS
	// ------------------------------------------
	
	public void startElement(String uri, String name, String qname, Attributes att){
		if (ignoreLevel > 0){ ignoreLevel++; return; }
		if (ignoreTags.contains(qname)){ ignoreLevel++; return; }
		//if (! "".equals(uri)) { qname = "{" + uri + "}" + name; } 

		prepareChilds();
		openTag(qname);
		startAttributes();
		for (String a: strongAttributes){
			String v = att.getValue(a);
			if (v!= null) 
				strongAttribute(a, v, qname);
		}
		for (int i = 0; i < att.getLength(); i++)
			if (! strongAttributes.contains(att.getQName(i)))
				attribute (att.getQName(i), att.getValue(i) );
		endAttributes();
	}

	public void endElement(String uri, String name, String qname){
		if (ignoreLevel > 0){ ignoreLevel--; return; }
		//if (! "".equals(uri)) { qname = "{" + uri + "}" + name; }
		closeTag(qname);
	}
	
	public void characters(char [] ch, int start, int length){
		if (ignoreLevel > 0){return;}
		if (length > 0 ) {
			prepareChilds();
			print(new String(ch, start, length));
		}
	}

}
