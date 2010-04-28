/**
 * ComparisonProtein.java
 * @author Vagisha Sharma
 * Apr 11, 2009
 * @version 1.0
 */
package org.yeastrc.www.compare;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.yeastrc.ms.util.StringUtils;
import org.yeastrc.nrseq.ProteinListing;
import org.yeastrc.nrseq.ProteinReference;
import org.yeastrc.www.compare.dataset.Dataset;
import org.yeastrc.www.compare.dataset.DatasetProteinInformation;

/**
 * 
 */
public class ComparisonProtein {

    private final int nrseqId;
    private int groupId;
    private ProteinListing listing;
    private float molecularWeight = -1.0f;
    private float pi = -1.0f;
    private int maxPeptideCount;
    
    private List<DatasetProteinInformation> datasetInfo;
    
    public ComparisonProtein(int nrseqId) {
        this.nrseqId = nrseqId;
        datasetInfo = new ArrayList<DatasetProteinInformation>();
    }
    
    public void setProteinListing(ProteinListing listing) {
    	this.listing = listing;
    }
    
    public ProteinListing getProteinListing() {
    	return this.listing;
    }
    
    public int getNrseqId() {
        return nrseqId;
    }

    public List<DatasetProteinInformation> getDatasetInfo() {
        return datasetInfo;
    }
    
    public void setDatasetInformation(List<DatasetProteinInformation> infoList) {
        this.datasetInfo = infoList;
    }
    
    public void addDatasetInformation(DatasetProteinInformation info) {
        datasetInfo.add(info);
    }
    
    public DatasetProteinInformation getDatasetProteinInformation(Dataset dataset) {
        
        for(DatasetProteinInformation dsInfo: datasetInfo) {
            if(dataset.equals(dsInfo.getDataset())) {
                return dsInfo;
            }
        }
        return null;
    }
    
    public boolean isInDataset(Dataset dataset) {
        DatasetProteinInformation dpi = getDatasetProteinInformation(dataset);
        if(dpi != null)
            return dpi.isPresent();
        return false;
    }

    public int getMaxPeptideCount() {
        return maxPeptideCount;
    }

    public void setMaxPeptideCount(int maxPeptideCount) {
        this.maxPeptideCount = maxPeptideCount;
    }

    public boolean isParsimonious() {
        for(DatasetProteinInformation dpi: this.datasetInfo) {
            if(dpi.isParsimonious())
                return true;
        }
        return false;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
    
    public void setMolecularWeight(float weight) {
        this.molecularWeight = weight;
    }
    
    public float getMolecularWeight() {
        return this.molecularWeight;
    }
    
    public float getPi() {
        return pi;
    }
    
    public void setPi(float pi) {
        this.pi = pi;
    }
    
    public boolean molWtAndPiSet() {
        return molecularWeight != -1.0f && pi != -1.0;
    }
    
    public String getAccessionsCommaSeparated() throws SQLException {
    	List<String> accessions = listing.getFastaAccessions();
    	return StringUtils.makeCommaSeparated(accessions);
    }
    
    public ProteinReference getOneDescriptionReference() throws SQLException {
    	if(listing.getDescriptionReferences().size() > 0)
    		return listing.getDescriptionReferences().get(0);
    	return null;
    }
    
    public String getCommonNamesCommaSeparated() throws SQLException {
    	List<String> names = listing.getCommonNames();
    	return StringUtils.makeCommaSeparated(names);
    }
    
}