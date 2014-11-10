package womo;

import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.*;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axiom.om.*;
/**
 * This class containts Methods used to call simple Webservices.
 * @author f_jostoc
 *
 */
public class ServiceUser {

	protected OMFactory omFactory;
	protected String uri;

	/**
	 * 
	 * @param uri Adress of the webservice to call
	 */	
	public ServiceUser(String uri){
		omFactory = OMAbstractFactory.getOMFactory();
		this.uri = uri;
	}
	
	/**
	 * This method calls the given operation from the webservice at the given uri. 
	 * @param operation Name of the Operation to call
	 * @param data Map of parameter Names to contents of this parameter 
	 * @return the called webservices answer
	 * @throws AxisFault
	 */
	public OMElement callSvc(String operation, Map<String,OMNode> data) throws AxisFault {
		return callSvc(	(OMNamespace)null, operation, data );
	}
	
	public OMElement callSvc(String namespace, String operation, Map<String,OMNode> data) throws AxisFault {
		return callSvc(
				omFactory.createOMNamespace(namespace, "nswomo"),
				operation,
				data
		);
	}	

	public OMElement callSvc(OMNamespace ns, String operation, Map<String,OMNode> data) throws AxisFault {
		ServiceClient service = new ServiceClient() /*Anonymous Service*/;
		Options options = new Options();
		options.setTo(new EndpointReference(uri));
	//	options.setAction(operation);
		service.setOptions(options);

		OMElement request = omFactory.createOMElement(operation, ns);
		for (Map.Entry<String, OMNode> element: data.entrySet()) {
			omFactory.createOMElement(element.getKey(), ns, request).addChild(element.getValue());
		}
		
		return service.sendReceive(request);
	}	

	
}
