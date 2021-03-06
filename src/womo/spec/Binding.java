//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.08.17 at 09:54:26 AM CEST 
//


package womo.spec;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Binding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Binding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="plink" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dependentPlinks" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="addresses" type="{}SvcAddress" maxOccurs="unbounded"/>
 *         &lt;element name="defaultNs" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="defaultName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="defaultPort" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Binding", propOrder = {
    "plink",
    "dependentPlinks",
    "addresses",
    "defaultNs",
    "defaultName",
    "defaultPort"
})
public class Binding {

    @XmlElement(required = true)
    protected String plink;
    protected List<String> dependentPlinks;
    @XmlElement(required = true)
    protected List<SvcAddress> addresses;
    @XmlElement(required = true)
    protected String defaultNs;
    @XmlElement(required = true)
    protected String defaultName;
    @XmlElement(required = true)
    protected String defaultPort;

    /**
     * Gets the value of the plink property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlink() {
        return plink;
    }

    /**
     * Sets the value of the plink property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlink(String value) {
        this.plink = value;
    }

    /**
     * Gets the value of the dependentPlinks property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dependentPlinks property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDependentPlinks().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getDependentPlinks() {
        if (dependentPlinks == null) {
            dependentPlinks = new ArrayList<String>();
        }
        return this.dependentPlinks;
    }

    /**
     * Gets the value of the addresses property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the addresses property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAddresses().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SvcAddress }
     * 
     * 
     */
    public List<SvcAddress> getAddresses() {
        if (addresses == null) {
            addresses = new ArrayList<SvcAddress>();
        }
        return this.addresses;
    }

    /**
     * Gets the value of the defaultNs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultNs() {
        return defaultNs;
    }

    /**
     * Sets the value of the defaultNs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultNs(String value) {
        this.defaultNs = value;
    }

    /**
     * Gets the value of the defaultName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultName() {
        return defaultName;
    }

    /**
     * Sets the value of the defaultName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultName(String value) {
        this.defaultName = value;
    }

    /**
     * Gets the value of the defaultPort property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultPort() {
        return defaultPort;
    }

    /**
     * Sets the value of the defaultPort property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultPort(String value) {
        this.defaultPort = value;
    }

}
