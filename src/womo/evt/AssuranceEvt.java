//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.07.22 at 11:30:57 PM CEST 
//


package womo.evt;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AssuranceEvt complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AssuranceEvt">
 *   &lt;complexContent>
 *     &lt;extension base="{}Event">
 *       &lt;sequence>
 *         &lt;element name="leftValue" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="rightValue" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AssuranceEvt", propOrder = {
    "leftValue",
    "rightValue"
})
public class AssuranceEvt
    extends Event
{

    @XmlElement(required = true)
    protected String leftValue;
    @XmlElement(required = true)
    protected String rightValue;

    /**
     * Gets the value of the leftValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLeftValue() {
        return leftValue;
    }

    /**
     * Sets the value of the leftValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLeftValue(String value) {
        this.leftValue = value;
    }

    /**
     * Gets the value of the rightValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRightValue() {
        return rightValue;
    }

    /**
     * Sets the value of the rightValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRightValue(String value) {
        this.rightValue = value;
    }

}
