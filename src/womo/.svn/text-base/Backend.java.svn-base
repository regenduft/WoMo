package womo;

import java.io.*;
import java.util.*;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.*;

import org.xml.sax.*;

public class Backend {
	
	protected Map<String, Process> processes = new HashMap<String, Process>();
	protected SAXParser parser;
	
	private static Backend myInstance;

	private Backend() {
		super();		
		SAXParserFactory fac = SAXParserFactory.newInstance();
		try {
			parser = fac.newSAXParser();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
	
	public static Backend getInstance() {
		if ( myInstance == null ) { myInstance = new Backend(); }
		return myInstance;
	}
	
	public void addProcess(Process proc) throws Exception {
		String name = proc.getName();
		if (processes.get(name) != null) {
			throw new Exception("Process with this name exists. Cannot add: " + name);
		}
		processes.put(name, proc);
	}
	
	public void addProcess(String name, String inputFileName) throws Exception {
		if (processes.get(name) != null) {
			throw new Exception("Process with this name exists. Cannot add: " + name);
		}
		processes.put(name, new Process(new File(inputFileName), name));
	}
	
	public void process2Html(String name, Writer out, String prefix4links, String prefix4plinks) throws IOException, SAXException {
		Process p = processes.get(name);
		Map<String, Integer> rmap = new HashMap<String, Integer>();
		for (womo.spec.Requirement r : p.getSpec().getRequirements()){
			if (rmap.containsKey(r.getActivityName())){
				Integer i = rmap.get(r.getActivityName());
				i++;
			}
			rmap.put(r.getActivityName(), 1);
		}
		parser.parse(new InputSource(p.getReader()), new BpelHandler(out, prefix4links, prefix4plinks, rmap));
	}

	public void instance2Html(String name, Integer id, Writer out) throws IOException, SAXException, JAXBException {
		StringWriter sw = new StringWriter();
		processes.get(name).writeInstance(sw, id);
		parser.parse(new InputSource(new StringReader(sw.toString())), new BpelHandler(out, "", "", null));
	}
	
	public void printInstances(String name, Writer out) throws IOException{
		for (Integer i : processes.get(name).getInstanceIds()){
			out.write("<a href=\"instance.jsp?name="+name+"&id="+i.toString()+"\">");
			out.write(i.toString());
			out.write("</a> ");
		}
	}
	
//	public void instrumentedProcess2Html(String name, Writer out, String prefix4links, String prefix4plinks) throws SAXException, IOException{
//		parser.parse(new InputSource(processes.get(name).getInstrumentedReader()), new BpelHandler(out, prefix4links, prefix4plinks));
//	}
	
	public Set<String> getProcessNames(){
		return processes.keySet();
	}
	
	public Process getProcess(String name){
		return processes.get(name);
	}

}
