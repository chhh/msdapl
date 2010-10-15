package org.yeastrc.ms.parser.percolator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.yeastrc.ms.domain.analysis.percolator.PercolatorParam;
import org.yeastrc.ms.domain.analysis.percolator.PercolatorPeptideResultIn;
import org.yeastrc.ms.domain.analysis.percolator.PercolatorResultIn;
import org.yeastrc.ms.domain.analysis.percolator.impl.PercolatorParamBean;
import org.yeastrc.ms.domain.search.MsResidueModificationIn;
import org.yeastrc.ms.domain.search.MsSearchResultPeptide;
import org.yeastrc.ms.domain.search.Program;
import org.yeastrc.ms.parser.DataProviderException;
import org.yeastrc.ms.parser.PercolatorXmlDataProvider;
import org.yeastrc.ms.parser.sqtFile.SQTParseException;
import org.yeastrc.ms.parser.sqtFile.sequest.SequestResultPeptideBuilder;

public class PercolatorXmlFileReader implements PercolatorXmlDataProvider{

	private InputStream inputStr = null;
    private XMLStreamReader reader = null;
    private String percolatorVersion = null;
    private List<PercolatorParam> params = null;
    private String filePath;
    
    private List<? extends MsResidueModificationIn> searchDynamicResidueMods;
    private Program searchProgram;
    
    private static final String namespace = "http://github.com/percolator/percolator/raw/master/src/xml/percolator_out";
    
    public PercolatorXmlFileReader() {
    	searchDynamicResidueMods = new ArrayList<MsResidueModificationIn>();
    }
    
    public void setDynamicResidueMods(List<? extends MsResidueModificationIn> dynaResidueMods) {
        if (dynaResidueMods != null)
            this.searchDynamicResidueMods = dynaResidueMods;
    }
    
    public void setSearchProgram(Program program) {
    	this.searchProgram = program;
    }
    
	public void open(String filePath) throws DataProviderException {
        
		this.filePath = filePath;
		
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        try {
            inputStr = new FileInputStream(filePath);
            reader = inputFactory.createXMLStreamReader(inputStr);
            
            initialize();
        }
        catch (FileNotFoundException e) {
            throw new DataProviderException("File not found: "+filePath, e);
        }
        catch (XMLStreamException e) {
            throw new DataProviderException("Error reading file: "+filePath, e);
        }
    }

	public static boolean isSupportedPercolatorXml(String filePath) throws DataProviderException {
		
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLStreamReader reader = null;
		boolean percXml = false;
        try {
        	InputStream inputStr = new FileInputStream(filePath);
        	reader = inputFactory.createXMLStreamReader(inputStr);
        	while(reader.hasNext()) {
    			
    			int evtType = reader.next();
    			if(evtType == XMLStreamReader.START_ELEMENT) {
    				if(reader.getLocalName().equalsIgnoreCase("percolator_output")) {
    					percXml = true;
    					break;
    				}
    				else
    					percXml = false;
    			}
        	}
        }
        catch (FileNotFoundException e) {
            throw new DataProviderException("File not found: "+filePath, e);
        }
        catch (XMLStreamException e) {
            throw new DataProviderException("Error reading file: "+filePath, e);
        }
        finally {
        	if(reader != null) try {reader.close();} catch (XMLStreamException e) {}
        }
        
        if(percXml) {
        	PercolatorXmlFileReader percReader = new PercolatorXmlFileReader();
        	percReader.open(filePath);
        	String percVersion = percReader.getPercolatorVersion();
        	percReader.close();
        	if(percVersion.equals("UNOFFICIAL") || Double.parseDouble(percVersion) > 1.14)
        		return true;
        	else
        		return false;
        }
        else
        	return false;
	}
	
	private void initialize() throws XMLStreamException {
		
		params = new ArrayList<PercolatorParam>();
		
		while(reader.hasNext()) {
			
			int evtType = reader.next();
			
            if(evtType == XMLStreamReader.START_ELEMENT) {
            	if(reader.getLocalName().equalsIgnoreCase("percolator_output")) {
            		readPercolatorVersion();
            	}
            	else if(reader.getLocalName().equalsIgnoreCase("process_info")) {
            		readPercolatorParams();
            	}
            	else if(reader.getLocalName().equalsIgnoreCase("psms") ||
            			reader.getLocalName().equalsIgnoreCase("psm")) { // older XML's don't have a <psms> element.
            		break;
            	}
            }
		}
	}
	
	private void readPercolatorVersion() throws XMLStreamException {
		
		String version = reader.getAttributeValue(namespace,"percolator_version");
		if(version == null)
			version = reader.getAttributeValue(null,"percolator_version");
        if(version != null) {
        	version = version.trim();
        	// Example: Percolator version 1.14
            Pattern pattern = Pattern.compile("^Percolator\\sv.*\\s(\\d+\\.\\d+).*$"); 
            Matcher matcher = pattern.matcher(version);
            if(matcher.matches()) {
               this.percolatorVersion = matcher.group(1);
               return;
            }
            else {
            	if(version.equals("Percolator version UNOFFICIAL")) {
            		this.percolatorVersion = "UNOFFICIAL";
            		return;
            	}
            }
        }
	}
	
	private void readPercolatorParams() throws XMLStreamException {
		
		while(reader.hasNext()) {

			int evtType = reader.next();

			if(evtType == XMLStreamReader.START_ELEMENT) {

				if(reader.getLocalName().equalsIgnoreCase("other_command_line")) {
					String value = reader.getElementText();
					PercolatorParamBean param = new PercolatorParamBean();
					param.setParamName("command_line");
					param.setParamValue(value);
					params.add(param);
				}
				
				else if(reader.getLocalName().equalsIgnoreCase("pi_0")) {
					String value = reader.getElementText();
					PercolatorParamBean param = new PercolatorParamBean();
					param.setParamName("pi_0");
					param.setParamValue(value);
					params.add(param);
				}
			}
			if(evtType == XMLStreamReader.END_ELEMENT && 
					reader.getLocalName().equalsIgnoreCase("process_info"))
				return;
		}
	}
	
	public String getPercolatorVersion() {
		return percolatorVersion;
	}
	
	/**
	 * Returns the contents of the <process_info> element. Each sub-element is parsed as a 
	 * PercolatorParam object (element name = paramName; paramValue = element content)
	 * @return
	 */
	public List<PercolatorParam> getPercolatorParams() {
		if(params == null)
			return new ArrayList<PercolatorParam>(0);
		else
			return params;
	}
	
	@Override
	public boolean hasNextPsm() throws DataProviderException {
		
		if (reader == null)
            return false;
		
		try {
			while(reader.hasNext()) {
                
				int evtType = reader.next();
                
                if (evtType == XMLStreamReader.END_ELEMENT) {
                    // this is the end of one msms_run_summary
                    if (reader.getLocalName().equals("psms"))  {
                        return false;
                    }
                }
                
                if (evtType == XMLStreamReader.START_ELEMENT) {
                    // this is the end of one msms_run_summary
                    if (reader.getLocalName().equals("psm"))  {
                        return true;
                    }
                }
			}
		}
		catch (XMLStreamException e) {
            throw new DataProviderException("Error reading file: "+filePath, e);
        }
		return false;
	}
	
	@Override
	public PercolatorResultIn getNextPsm() throws DataProviderException {
		
		// We are at the <psm> element; First read the psm_id attribute
		PercolatorXmlResult result = new PercolatorXmlResult();
		String id = reader.getAttributeValue(namespace, "psm_id");
		if(id == null) 
			throw new DataProviderException("psm_id attribute is required for parsing result");
		
		try {
			result.setId(id);
		}
		catch(IllegalArgumentException e) {
			throw new DataProviderException("Could not parse psm_id attribute: "+id);
		}
		
		String seq = null;
		char nterm = '-';
		char cterm = '-';
		
		try {
			while (reader.hasNext()) {
				int evtType = reader.next();
				
				if (evtType == XMLStreamReader.END_ELEMENT && reader.getLocalName().equalsIgnoreCase("psm"))
					break;
				
				if (evtType == XMLStreamReader.START_ELEMENT) {
					
					if(reader.getLocalName().equalsIgnoreCase("svm_score")) {
						String score = reader.getElementText();
						result.setDiscriminantScore(Double.valueOf(score));
					}
					else if(reader.getLocalName().equalsIgnoreCase("q_value")) {
						String score = reader.getElementText();
						result.setQvalue(Double.parseDouble(score));
					}
					else if(reader.getLocalName().equalsIgnoreCase("pep")) {
						String score = reader.getElementText();
						result.setPosteriorErrorProbability(Double.parseDouble(score));
					}
					else if(reader.getLocalName().equalsIgnoreCase("p_value")) {
						String score = reader.getElementText();
						result.setPvalue(Double.parseDouble(score));
					}
					else if(reader.getLocalName().equalsIgnoreCase("protein_id")) {
						String name = reader.getElementText();
						result.addMatchingLocus(name, null);
					}
					else if(reader.getLocalName().equalsIgnoreCase("exp_mass")) {
						String exptMass = reader.getElementText();
						result.setObservedMass(new BigDecimal(exptMass));
					}
					else if(reader.getLocalName().equalsIgnoreCase("peptide") ||
							reader.getLocalName().equalsIgnoreCase("peptide_seq")) {
						
						seq = reader.getAttributeValue(null, "seq");
						if(seq == null || seq.length() == 0) 
							throw new DataProviderException("seq attribute is required for parsing <peptide> element");
						
						String nt = reader.getAttributeValue(null, "n");
						if(nt == null || nt.length() == 0) 
							throw new DataProviderException("n attribute is required for parsing <peptide> element");
						nterm = nt.charAt(0);
						
						String ct = reader.getAttributeValue(null, "c");
						if(ct == null || ct.length() == 0) 
							throw new DataProviderException("c attribute is required for parsing <peptide> element");
						cterm = ct.charAt(0);
					}
				}
			}
		}
		catch(XMLStreamException e) {
			throw new DataProviderException("Error reading result for psm: "+id, e);
		}
		
		
		// parse the peptide sequence
		// NOTE: We assume that the sequence represents the modified sequence (Sequest-style)
        try {

            MsSearchResultPeptide resultPeptide = null;
            
            if(searchProgram == Program.SEQUEST )//|| searchProgram == Program.EE_NORM_SEQUEST)
                resultPeptide = SequestResultPeptideBuilder.instance().build(
                        nterm+"."+seq+"."+cterm, this.searchDynamicResidueMods, null);
            
            else
                throw new DataProviderException("Cannot parse peptide string for search program: "+searchProgram);
            
            result.setResultPeptide(resultPeptide);
        }
        catch(SQTParseException e) {
            throw new DataProviderException("Error building peptide result: "+e.getMessage());
        }
        
		if(!result.isComplete()) {
			throw new DataProviderException("Incomplete Percolator psm result: "+result.toString());
		}
		return result;
	}
	
	@Override
	public boolean hasNextPeptide() throws DataProviderException {
		
		if (reader == null)
            return false;
		
		try {
			while(reader.hasNext()) {
                
				int evtType = reader.next();
                
                if (evtType == XMLStreamReader.END_ELEMENT) {
                    // this is the end of one msms_run_summary
                    if (reader.getLocalName().equals("peptides"))  {
                        return false;
                    }
                }
                
                if (evtType == XMLStreamReader.START_ELEMENT) {
                    // this is the end of one msms_run_summary
                    if (reader.getLocalName().equals("peptide"))  {
                        return true;
                    }
                }
			}
		}
		catch (XMLStreamException e) {
            throw new DataProviderException("Error reading file: "+filePath, e);
        }
		return false;
	}
	
	@Override
	public PercolatorPeptideResultIn getNextPeptide() throws DataProviderException {
	
		/*
		 <peptide peptide_id="TVEEDHPIPEDVHENYENK">
      		<svm_score>1.38005</svm_score>
      		<q_value>0</q_value>
      		<pep>8.95165e-12</pep>
      		<protein_id>YGL008C</protein_id>
      		<protein_id>YPL036W</protein_id>
      		<p_value>0.000158428</p_value>
      		<psms>
        		<psm_id>target_005018_3_1</psm_id>
      		</psms>
    	</peptide>
		 */
		
		// We are at the <peptide> element; First read the peptide_id attribute
		PercolatorXmlPeptideResult result = new PercolatorXmlPeptideResult();
		String sequence = reader.getAttributeValue(namespace, "peptide_id");
		if(sequence == null) 
			throw new DataProviderException("peptide_id attribute is required for parsing <peptide> result");
		
		// parse the peptide sequence
		// NOTE: We assume that the sequence represents the modified sequence (Sequest-style)
        try {

            MsSearchResultPeptide resultPeptide = null;
            
            if(searchProgram == Program.SEQUEST )//|| searchProgram == Program.EE_NORM_SEQUEST)
                resultPeptide = SequestResultPeptideBuilder.instance().build(
                        "-."+sequence+".-",  // NOTE: we don't have n and c term residue information for peptides
                        this.searchDynamicResidueMods, null);
            
            else
                throw new DataProviderException("Cannot parse peptide string for search program: "+searchProgram);
            
            result.setResultPeptide(resultPeptide);
        }
        catch(SQTParseException e) {
            throw new DataProviderException("Error building peptide result: "+e.getMessage());
        }
		
		
		
		try {
			Set<String> uniqPsmIds = new HashSet<String>();
			
			while (reader.hasNext()) {
				int evtType = reader.next();
				
				if (evtType == XMLStreamReader.END_ELEMENT && reader.getLocalName().equalsIgnoreCase("peptide"))
					break;
				
				if (evtType == XMLStreamReader.START_ELEMENT) {
					
					if(reader.getLocalName().equalsIgnoreCase("svm_score")) {
						String score = reader.getElementText();
						result.setDiscriminantScore(Double.valueOf(score));
					}
					else if(reader.getLocalName().equalsIgnoreCase("q_value")) {
						String score = reader.getElementText();
						result.setQvalue(Double.parseDouble(score));
					}
					else if(reader.getLocalName().equalsIgnoreCase("pep")) {
						String score = reader.getElementText();
						result.setPosteriorErrorProbability(Double.parseDouble(score));
					}
					else if(reader.getLocalName().equalsIgnoreCase("p_value")) {
						String score = reader.getElementText();
						result.setPvalue(Double.parseDouble(score));
					}
					else if(reader.getLocalName().equalsIgnoreCase("exp_mass")) {
						String exptMass = reader.getElementText();
						result.setObservedMass(new BigDecimal(exptMass));
					}
					else if(reader.getLocalName().equalsIgnoreCase("protein_id")) {
						String name = reader.getElementText();
						result.addMatchingLocus(name, null);
					}
					
					else if(reader.getLocalName().equalsIgnoreCase("psm_ids")) {
						
						while(reader.hasNext()) {
							
							evtType = reader.next();
							
							if(evtType == XMLStreamReader.END_ELEMENT && reader.getLocalName().equalsIgnoreCase("psm_ids"))
								break;
							
							if(evtType == XMLStreamReader.START_ELEMENT && reader.getLocalName().equalsIgnoreCase("psm_id")) {
								String psmid = reader.getElementText();
								if(psmid == null || psmid.trim().length() == 0) {
									throw new DataProviderException("No id found for psm for peptide: "+sequence);
								}
								uniqPsmIds.add(psmid);
							}
						}
					}
				}
			}
			
			for(String psmid: uniqPsmIds) {
				try {
					result.addMatchingPsmId(psmid);
				}
				catch(IllegalArgumentException e) {
					throw new DataProviderException("Could not parse psm_id attribute: "+psmid+" for peptide: "+
							result.getResultPeptide().getPeptideSequence());
				}
			}
		}
		catch(XMLStreamException e) {
			throw new DataProviderException("Error reading result for peptide: "+sequence, e);
		}
		
		if(!result.isComplete()) {
			throw new DataProviderException("Incomplete Percolator peptide result: "+result.toString());
		}
		return result;
		
	}

	

	@Override
	public void close() {
		if (reader != null) {
            try {reader.close();}
            catch (XMLStreamException e) {}
        }
        
        if(inputStr != null) {
            try {inputStr.close();}
            catch(IOException e){}
        }
	}
}
