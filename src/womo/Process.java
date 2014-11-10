package womo;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.xml.sax.SAXException;
import javax.xml.bind.*;

import womo.spec.*;
import womo.evt.*;

/**
 * This class represents a bpel-process-definition. It can be read from a bpel-file or a 
 * bpr-archive. It is possible to add a specification, and the bpel-process and the deployment
 * descriptor both can be instrumented. Object of this class also holds information about the 
 * instances of a process, and can save events that were received from this process-instance.
 * @author Florian Jostock
 *
 */
public class Process {

	private ZipFile bpr = null;
	private ZipEntry ze = null;
	private File bpel = null;
	private String name;
	private Specification spec = new Specification();
	private Map<Integer, ProcessInstance> instances = new HashMap<Integer, ProcessInstance>();
	
	protected ProcessInstrumenter instrumenter;
	
	public Process(File bpr_or_bpel, String name) throws IOException, JAXBException {
		this.name = name;
		instrumenter = new ProcessInstrumenter();
		try {
			bpr = new ZipFile(bpr_or_bpel);
		} catch (ZipException e) {
			bpr = null;
			bpel = bpr_or_bpel;
			return;
		}
		readBpr();
	}
	
	protected void readBpr() throws JAXBException, IOException{
		//look wich contents exist in the file and take the appropriate action
		for (Enumeration<? extends ZipEntry> ze = bpr.entries(); ze.hasMoreElements() ;) {
			ZipEntry el = ze.nextElement();
			if (el.getName().endsWith("bpel")) {
				this.ze = el;
			}
			if ("specification.xml".equals(el.getName())){
				this.readSpec(new InputStreamReader(bpr.getInputStream(el)));
			}
		}
		if (ze == null) {
			throw new IOException("could not find a .bpel-file in this zip-archive");
		}
	}
	
	public Specification getSpec(){ return spec;	}
	public void setSpec(Specification myspec){ this.spec = myspec;}
	public String getName() {	return name;	}
	public void setName(String name) { this.name = name; }
	
	public ProcessInstance getInstance(Integer id){
		ProcessInstance instance = instances.get(id);
		if (instance == null){ 
			instance = new ProcessInstance();
			instance.setId(id);
			instances.put(id, instance);
		}
		return instance;
	}
	public Set<Integer> getInstanceIds(){
		return instances.keySet();
	}
	
	public void readSpec(Reader in) throws JAXBException{
			/* XMLDecoder in = new XMLDecoder(new BufferedInputStream(new FileInputStream(specFile)));
			   spec = (Specification) in.readObject(); in.close();	*/
		JAXBContext jaxbContext = JAXBContext.newInstance("womo.spec");
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		spec = (Specification) unmarshaller.unmarshal(in);
	}

	public void writeSpec(Writer out) throws JAXBException{
		JAXBContext jaxbContext = JAXBContext.newInstance("womo.spec");
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal( spec, out );
	}
	
	public void readInstance(Reader in) throws JAXBException{
		JAXBContext jaxbContext = JAXBContext.newInstance("womo.evt");
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		ProcessInstance i = (ProcessInstance) unmarshaller.unmarshal(in);
		instances.put(i.getId(), i);
	}

	public void writeInstance(Writer out, Integer id) throws JAXBException{
		JAXBContext jaxbContext = JAXBContext.newInstance("womo.evt");
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal( this.getInstance(id), out );
	}
	
	public Reader getReader() throws IOException {
		if (this.bpel == null) {
			return new InputStreamReader(this.bpr.getInputStream(ze));
		} 
		return new InputStreamReader(new FileInputStream(bpel));
	}	
	
	public void writeInstrumented(Writer out) throws IOException, SAXException {
		instrumenter.writeInstrumented(out, getReader(), getSpec(), getName());
	}
	
	public Reader getInstrumentedReader() throws IOException, SAXException{
		File instrumented = new File("/tmp/tmp.bpel");//File.createTempFile("instrumented_process", ".bpel");
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(instrumented));
		instrumenter.writeInstrumented(writer, getReader(), getSpec(), getName());
		writer.close();
		return new FileReader(instrumented);
	}
	
	public void saveBpr(boolean instrumented) throws IOException, SAXException, JAXBException {
		File tmp = File.createTempFile("womoTmpBprArchive", "bpr");
		saveBpr(tmp, instrumented);
		bpr.close();
		File orig = new File(bpr.getName());
		File backup = new File(orig.getParent() + "backup" + orig.getName());
		orig.renameTo(backup);
		tmp.renameTo(new File(bpr.getName()));
		bpr = new ZipFile(bpr.getName());
		readBpr();
	}
	
	public void saveBpr(File bprFile, boolean instrumented) throws IOException, SAXException, JAXBException{
		ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(bprFile));
		for (Enumeration<? extends ZipEntry> ze = bpr.entries(); ze.hasMoreElements() ;) {
			ZipEntry el = ze.nextElement();
			if (instrumented && el.getName().endsWith(".bpel")) {
				zipout.putNextEntry(new ZipEntry(el.getName()));
				instrumenter.writeInstrumented(new OutputStreamWriter(zipout), getReader(), getSpec(), getName());
			} else if (instrumented && el.getName().endsWith(".pdd")) {
				zipout.putNextEntry(new ZipEntry(el.getName()));
				instrumenter.instrumentPdd(new InputStreamReader(bpr.getInputStream(el)), new OutputStreamWriter(zipout));
			} else if ("specification.xml".equals(el.getName())){
				//nix tun, wird ganz zum Schluss geschrieben.
			} else {
				zipout.putNextEntry(el);
				byte[] buffer = new byte[1024];
				InputStream inputstream = bpr.getInputStream(el);
				int len;
				while ((len = inputstream.read(buffer, 0, 1024)) > 0) {
					zipout.write(buffer, 0, len);
				}
			}
		}
		zipout.putNextEntry(new ZipEntry("specification.xml"));
		this.writeSpec(new OutputStreamWriter(zipout));
		zipout.close();
		
	}	
	
}
	