//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.07.22 at 11:30:57 PM CEST 
//


package womo.evt;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the womo.evt package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: womo.evt
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EvalAssuranceEvt }
     * 
     */
    public EvalAssuranceEvt createEvalAssuranceEvt() {
        return new EvalAssuranceEvt();
    }

    /**
     * Create an instance of {@link AssuranceEvt }
     * 
     */
    public AssuranceEvt createAssuranceEvt() {
        return new AssuranceEvt();
    }

    /**
     * Create an instance of {@link ProcessInstance }
     * 
     */
    public ProcessInstance createProcessInstance() {
        return new ProcessInstance();
    }

    /**
     * Create an instance of {@link Event }
     * 
     */
    public Event createEvent() {
        return new Event();
    }

}
