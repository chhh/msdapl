//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.08.01 at 02:54:01 PM PDT 
//


package org.yeastrc.ms.writer.mzidentml.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * All identifications made from searching one spectrum. For PMF data, all peptide identifications will be listed underneath as SpectrumIdentificationItems. For MS/MS data, there will be ranked SpectrumIdentificationItems corresponding to possible different peptide IDs.
 * 
 * <p>Java class for SpectrumIdentificationResultType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SpectrumIdentificationResultType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://psidev.info/psi/pi/mzIdentML/1.1}IdentifiableType">
 *       &lt;sequence>
 *         &lt;element name="SpectrumIdentificationItem" type="{http://psidev.info/psi/pi/mzIdentML/1.1}SpectrumIdentificationItemType" maxOccurs="unbounded"/>
 *         &lt;group ref="{http://psidev.info/psi/pi/mzIdentML/1.1}ParamGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="spectrumID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="spectraData_ref" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpectrumIdentificationResultType", propOrder = {
    "spectrumIdentificationItem",
    "paramGroup"
})
public class SpectrumIdentificationResultType
    extends IdentifiableType
{

    @XmlElement(name = "SpectrumIdentificationItem", required = true)
    protected List<SpectrumIdentificationItemType> spectrumIdentificationItem;
    @XmlElements({
        @XmlElement(name = "userParam", type = UserParamType.class),
        @XmlElement(name = "cvParam", type = CVParamType.class)
    })
    protected List<AbstractParamType> paramGroup;
    @XmlAttribute(required = true)
    protected String spectrumID;
    @XmlAttribute(name = "spectraData_ref", required = true)
    protected String spectraDataRef;

    /**
     * Gets the value of the spectrumIdentificationItem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the spectrumIdentificationItem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSpectrumIdentificationItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SpectrumIdentificationItemType }
     * 
     * 
     */
    public List<SpectrumIdentificationItemType> getSpectrumIdentificationItem() {
        if (spectrumIdentificationItem == null) {
            spectrumIdentificationItem = new ArrayList<SpectrumIdentificationItemType>();
        }
        return this.spectrumIdentificationItem;
    }

    /**
     *  Scores or parameters associated with the SpectrumIdentificationResult (i.e the set of SpectrumIdentificationItems derived from one spectrum) e.g. the number of peptide sequences within the parent tolerance for this spectrum. Gets the value of the paramGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the paramGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParamGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UserParamType }
     * {@link CVParamType }
     * 
     * 
     */
    public List<AbstractParamType> getParamGroup() {
        if (paramGroup == null) {
            paramGroup = new ArrayList<AbstractParamType>();
        }
        return this.paramGroup;
    }

    /**
     * Gets the value of the spectrumID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpectrumID() {
        return spectrumID;
    }

    /**
     * Sets the value of the spectrumID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpectrumID(String value) {
        this.spectrumID = value;
    }

    /**
     * Gets the value of the spectraDataRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpectraDataRef() {
        return spectraDataRef;
    }

    /**
     * Sets the value of the spectraDataRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpectraDataRef(String value) {
        this.spectraDataRef = value;
    }

}
