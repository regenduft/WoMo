package womo;

import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.*;
import javax.xml.namespace.QName;
import org.apache.axiom.om.*;

import womo.evt.*;

public class EventReceiverService {

	public void receive(String processName, Integer processId, OMElement evt) {
		try{
		System.out.println(processName);
		System.out.println(processId);
		System.out.println(evt.toString());

		Backend backend = Backend.getInstance();
		Event e = new Event();
		e.setTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
    	e.setType(EvtType.fromValue((evt.getFirstChildWithName(new QName("type")).getText().toLowerCase())));
    	e.setValue(evt.getFirstChildWithName(new QName("value")).getText());
		Process proc = backend.getProcess(processName);
		if (proc == null) {
			try {
				backend.addProcess(processName, "/home/f_jostoc/"+processName+".bpr");
				proc = backend.getProcess(processName);
			} catch (Exception err) {
				err.printStackTrace();
			}
		}
		ProcessInstance i = proc.getInstance(processId);
		List<Event> evl = i.getEvents();
		evl.add(e);
		} catch (Exception err) {
			err.printStackTrace();
		}
	}
	
	public String receiveEgal(OMElement evt){
		System.out.println(evt.getText());
		System.out.println(evt.getChildrenWithName(new QName("evt")).next().toString());
		return "1";
	}
	
}
