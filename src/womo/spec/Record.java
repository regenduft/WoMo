//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.08.17 at 09:54:26 AM CEST 
//


package womo.spec;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Record complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Record">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="formular" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="type" type="{}RecordType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Record", propOrder = {
    "formular",
    "type"
})
public class Record {

    @XmlElement(required = true)
    protected String formular;
    @XmlElement(required = true)
    protected RecordType type;

    /**
     * Gets the value of the formular property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormular() {
        return formular;
    }

    /**
     * Sets the value of the formular property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormular(String value) {
        this.formular = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link RecordType }
     *     
     */
    public RecordType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link RecordType }
     *     
     */
    public void setType(RecordType value) {
        this.type = value;
    }

}