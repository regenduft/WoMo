package womo;

import java.io.File;
import java.util.HashMap;

import javax.activation.*;
import org.apache.axiom.om.*;
import org.apache.axis2.AxisFault;

public class BprServiceUser extends ServiceUser {
	
	/**
	 * 
	 * @param uri Adress of the webservice to call
	 */	
	public BprServiceUser(String uri){
		super(uri);
	}
	
	/**
	 * This method calls the given operation from a webservice at the given uri
	 * and sends the contents of the given file as a Message with Name "aBprFilename" 
	 * of type xsd:base64Binary and the filename as a text-Message "aBase64File"
	 * @param filename send this files' contents and this filename
	 * @param op call this operation
	 * @return the webservices answer, serialized
	 * @throws AxisFault
	 */
	public String sendBpr(File f, String op) throws AxisFault{
		HashMap<String, OMNode> args = new HashMap<String, OMNode>();
		args.put(
			"aBprFilename", 
			omFactory.createOMText(f.getName())
		);
		args.put(
			"aBase64File", 
			omFactory.createOMText(
				new DataHandler(new FileDataSource(f)), 
				false
			)
		);
		return super.callSvc(
			op,
			args
		).getText();
	}
}
