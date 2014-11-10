package womo;

import java.io.File;
import java.util.HashMap;

import org.apache.axiom.om.*;
import org.apache.axis2.AxisFault;

/**
 * This POJO-class contains the operations on the {@link Instrumenter} which are
 * published as Webservice.
 * @author f_jostoc@informatik.uni-kl.de
 *
 */
public class InstrumenterService {
	
	BprServiceUser bprSvc;
	
	public InstrumenterService(){
		bprSvc = new BprServiceUser("http://localhost:8080/active-bpel/services/DeployBPRService");
	}

	public String deploy(String filename, byte[] data) throws Exception  {
		Utils.bytearray2file(data, filename);
		return "Datei " + filename + " gesichert";
	}

	public String deployBpr(String filename, byte[] data) throws Exception  {
		System.out.println("deployBpr");
		System.out.println(filename);
		Utils.bytearray2file(data, filename);
		return "Datei " + filename + " gesichert";
	}
	
	/**
	 * sends the process saved with this filename to activebpel deploy webservice
	 * @param filename name of the file with the process
	 * @return activebpels answer
	 * @throws Exception
	 */	
	public String deployTo(String filename) throws Exception {
		return bprSvc.sendBpr(
			new File(filename), 
			"deployBpr"
		);
	}
	
	public String deployProc(String processName) throws Exception {
		File bpr = new File("/tmp/"+processName+".bpr");
		Backend.getInstance().getProcess(processName).saveBpr(bpr, true);
		return bprSvc.sendBpr(
			bpr,
			"deployBpr"
		);
	}	
	
	public String helo(String n) {
		return "helo ";
	}
	
	public String hello() throws AxisFault {
		HashMap<String, OMNode> args = new HashMap<String, OMNode>();
		OMFactory omFactory = OMAbstractFactory.getOMFactory();
		OMElement el = omFactory.createOMElement("laber", null);
		el.addChild(omFactory.createOMText("bla"));
		args.put(
			"el", 
			el
		);
		args.put(
			"laber", 
			omFactory.createOMText("hui")
		);
		ServiceUser su = new ServiceUser("http://localhost:8080/womo/services/evtrcv");
		su.callSvc(
			"http://womo/xsd",
			"receive",
			args
		);
		
		return "hello ";
	}
	
}
