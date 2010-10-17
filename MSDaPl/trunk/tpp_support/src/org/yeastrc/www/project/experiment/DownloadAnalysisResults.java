/**
 * 
 */
package org.yeastrc.www.project.experiment;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.yeastrc.experiment.PercolatorResultPlus;
import org.yeastrc.ms.dao.DAOFactory;
import org.yeastrc.ms.dao.analysis.MsRunSearchAnalysisDAO;
import org.yeastrc.ms.dao.analysis.percolator.PercolatorResultDAO;
import org.yeastrc.ms.dao.run.MsScanDAO;
import org.yeastrc.ms.dao.run.ms2file.MS2RunDAO;
import org.yeastrc.ms.dao.run.ms2file.MS2ScanDAO;
import org.yeastrc.ms.dao.search.MsRunSearchDAO;
import org.yeastrc.ms.dao.search.sequest.SequestSearchResultDAO;
import org.yeastrc.ms.domain.analysis.MsSearchAnalysis;
import org.yeastrc.ms.domain.analysis.percolator.PercolatorResult;
import org.yeastrc.ms.domain.analysis.percolator.PercolatorResultFilterCriteria;
import org.yeastrc.ms.domain.run.MsScan;
import org.yeastrc.ms.domain.run.ms2file.MS2Scan;
import org.yeastrc.ms.domain.search.MsSearchResultProtein;
import org.yeastrc.ms.domain.search.Program;
import org.yeastrc.ms.service.ModifiedSequenceBuilderException;
import org.yeastrc.ms.util.TimeUtils;
import org.yeastrc.www.util.RoundingUtils;

/**
 * DownloadAnalysisResults.java
 * @author Vagisha Sharma
 * Jun 10, 2010
 * 
 */
public class DownloadAnalysisResults extends Action {

	private static final Logger log = Logger.getLogger(ViewAnalysisResults.class.getName());

    public ActionForward execute( ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response )
    throws Exception {


    	Integer analysisId = (Integer) request.getAttribute("analysisId");
    	List<Integer> resultIds = (List<Integer>) request.getAttribute("analysisResultIds");
    	Program program = (Program) request.getAttribute("analysisProgram");
    	AnalysisFilterResultsForm myForm = (AnalysisFilterResultsForm) request.getAttribute("filterForm");
    	
    	long s = System.currentTimeMillis();
        response.setContentType("text/plain");
        response.setHeader("Content-Disposition","attachment; filename=\"Analysis_"+analysisId+".txt\"");
        response.setHeader("cache-control", "no-cache");
        PrintWriter writer = response.getWriter();
        writer.write("Date: "+new Date()+"\n\n");
        writeResults(writer, analysisId, resultIds, program, myForm);
        writer.close();
        long e = System.currentTimeMillis();
        log.info("DownloadAnalysisResults results in: "+TimeUtils.timeElapsedMinutes(s,e)+" minutes");
        return null;
    }
    	
    private void writeResults(PrintWriter writer, int analysisId, List<Integer> resultIds, Program analysisProgram, AnalysisFilterResultsForm myForm) {	
    	
    	if(analysisProgram == Program.PERCOLATOR) {
            writePercolatorResults(writer, analysisId, analysisProgram, resultIds, ((PercolatorFilterResultsForm)myForm));
        }
    	else {
    		log.error("Unrecognized analysis program: "+analysisProgram);
    		writer.write("Unrecognized analysis program: "+analysisProgram);
    	}
    
    }

	private void writePercolatorResults(PrintWriter writer,int analysisId, Program analysisProgram, List<Integer> resultIds, 
			PercolatorFilterResultsForm myForm) {
		
		PercolatorResultFilterCriteria filterCriteria = myForm.getFilterCriteria();
		writeFilters(writer, filterCriteria);
		writer.write("\n\n");
		
		// Get the names of the files
		Map<Integer, String> filenameMap = getFileNames(analysisId);
		
		// Do we have Bullseye results for the searched files
        boolean hasBullsEyeArea = false;
        List<Integer> searchIds = DAOFactory.instance().getMsSearchAnalysisDAO().getSearchIdsForAnalysis(analysisId);
        MsRunSearchDAO rsDao = DAOFactory.instance().getMsRunSearchDAO();
        MS2RunDAO runDao = DAOFactory.instance().getMS2FileRunDAO();
        List<Integer> runSearchIds = rsDao.loadRunSearchIdsForSearch(searchIds.get(0));
        for(int runSearchId: runSearchIds) {
            int runId = rsDao.loadRunSearch(runSearchId).getRunId();
            if(runDao.isGeneratedByBullseye(runId)) {
                hasBullsEyeArea = true;
                break;
            }
        }
        
        MsSearchAnalysis analysis = DAOFactory.instance().getMsSearchAnalysisDAO().load(analysisId);
        // Which version of Percolator are we using
        String version = analysis.getAnalysisProgramVersion();
        boolean hasPEP = true;
        try {
            float vf = Float.parseFloat(version.trim());
            if(vf < 1.06)   hasPEP = false;
        }
        catch(NumberFormatException e){
            log.error("Cannot determine if this version of Percolator prints PEP. Version: "+version);
        }
        
        // Summary
        writer.write("SUMMARY:  ");
        writer.write("# Unfiltered Results: "+getUnfilteredResultCount(analysisProgram, analysisId));
        writer.write("# Filtered Results: "+resultIds.size());
        writer.write("\n\n\n");
        
        // Results
        PercolatorResultDAO presDao = DAOFactory.instance().getPercolatorResultDAO();
        SequestSearchResultDAO seqResDao = DAOFactory.instance().getSequestResultDAO();
        MsScanDAO scanDao = DAOFactory.instance().getMsScanDAO();
        MS2ScanDAO ms2ScanDao = DAOFactory.instance().getMS2FileScanDAO();
        
        writeHeader(writer, hasBullsEyeArea, hasPEP);
        
		for(int percResultId: resultIds) {
			PercolatorResult result = presDao.loadForPercolatorResultId(percResultId);
            PercolatorResultPlus resPlus = null;
            
            if(hasBullsEyeArea) {
                MS2Scan scan = ms2ScanDao.loadScanLite(result.getScanId());
                resPlus = new PercolatorResultPlus(result, scan);
            }
            else {
                MsScan scan = scanDao.loadScanLite(result.getScanId());
                resPlus = new PercolatorResultPlus(result, scan);
            }
            
            resPlus.setFilename(filenameMap.get(result.getRunSearchAnalysisId()));
            resPlus.setSequestData(seqResDao.load(result.getId()).getSequestResultData());
            
            writeResult(resPlus, hasPEP, hasBullsEyeArea, writer);
		}
		
	}

	private void writeFilters(PrintWriter writer,
			PercolatorResultFilterCriteria filterCriteria) {
		writer.write("FILTERS:\n");
		if(filterCriteria.getMinScan() != null)
			writer.write("Min. Scan: "+filterCriteria.getMinScan()+"\n");
		if(filterCriteria.getMaxScan() != null)
			writer.write("Max. Scan: "+filterCriteria.getMaxScan()+"\n");
		if(filterCriteria.getMinCharge() != null)
			writer.write("Min. Charge: "+filterCriteria.getMinCharge()+"\n");
		if (filterCriteria.getMinRetentionTime() != null)
			writer.write("Min. RT: "+filterCriteria.getMinRetentionTime()+"\n");
		if(filterCriteria.getMaxRetentionTime() != null)
			writer.write("Min. RT: "+filterCriteria.getMaxRetentionTime()+"\n");
		if (filterCriteria.getMinObservedMass() != null)
			writer.write("Min. Obs. Mass: "+filterCriteria.getMinObservedMass()+"\n");
		if(filterCriteria.getMaxObservedMass() != null)
			writer.write("Max. Obs. Mass: "+filterCriteria.getMaxObservedMass()+"\n");
		if (filterCriteria.getMinQValue() != null)
			writer.write("Min. q-value: "+filterCriteria.getMinQValue()+"\n");
		if (filterCriteria.getMaxQValue() != null)
			writer.write("Max. q-value: "+filterCriteria.getMaxQValue()+"\n");
		if(filterCriteria.getMinPep() != null)
			writer.write("Min. PEP: "+filterCriteria.getMinPep()+"\n");
		if (filterCriteria.getMaxPep() != null)
			writer.write("Max. PEP: "+filterCriteria.getMaxPep()+"\n");
		if (filterCriteria.getPeptide() != null) {
			writer.write("Filter peptide: "+filterCriteria.getPeptide());
			if(filterCriteria.getPeptide() != null && filterCriteria.getPeptide().trim().length() > 0) {
				writer.write("; Get exact match: "+filterCriteria.isShowOnlyModified());
			}
			writer.write("\n");
		}
		
		writer.write("Modified peptides: "+(!filterCriteria.isShowOnlyUnmodified())+"\n");
		writer.write("Un-modified peptides: "+(!filterCriteria.isShowOnlyModified())+"\n");
	}

	private void writeHeader(PrintWriter writer, boolean hasBullsEyeArea,
			boolean hasPEP) {
		writer.write("File\t");
        writer.write("Scan#\t");
        writer.write("Charge\t");
        writer.write("ObsMass\t");
        writer.write("RT\t");
        if(hasBullsEyeArea)
        	writer.write("Area\t");
        writer.write("q-value\t");
        if(hasPEP)
        	writer.write("PEP\t");
        else
        	writer.write("DiscriminantScore\t");
        writer.write("XCorrRank\t");
        writer.write("XCorr\t");
        writer.write("Peptide\t");
        writer.write("Protein(s)\n");
	}

	private void writeResult(PercolatorResultPlus result, boolean hasPEP, boolean hasBullseyeArea, PrintWriter writer) {
		
		writer.write(result.getFilename()+"\t");
		writer.write(result.getScanNumber()+"\t");
        writer.write(result.getCharge()+"\t");
        writer.write(String.valueOf(RoundingUtils.getInstance().roundFour(result.getObservedMass()))+"\t");
        
        // Retention time
        BigDecimal temp = result.getRetentionTime();
        if(temp == null) {
            writer.write("\t");
        }
        else
            writer.write(RoundingUtils.getInstance().roundFour(temp)+"\t");
        
        // Area of the precursor ion
        if(hasBullseyeArea) {
            writer.write(String.valueOf(RoundingUtils.getInstance().roundTwo(result.getArea()))+"\t");
        }
        
        writer.write(result.getQvalueRounded()+"\t");
        if(hasPEP)
            writer.write(result.getPosteriorErrorProbabilityRounded()+"\t");
        else
            writer.write(result.getDiscriminantScoreRounded()+"\t");
        
        // Sequest data
        writer.write(result.getSequestData().getxCorrRank()+"\t");
        writer.write(RoundingUtils.getInstance().roundTwo(result.getSequestData().getxCorr())+"\t");
        
        try {
            writer.write(result.getResultPeptide().getFullModifiedPeptide()+"\t");
        }
        catch (ModifiedSequenceBuilderException e) {
        	writer.write("ERROR\t");
        }
        
        String proteins = "";
        for(MsSearchResultProtein protein: result.getProteinMatchList())
        	proteins += ","+protein.getAccession();
        
        if(proteins.length() > 0)
        	proteins = proteins.substring(1); // remove first comma
        writer.write(proteins+"\n");
        
	}

	// ----------------------------------------------------------------------------------------
    // FILENAMES FOR THE ANALYSIS ID
    // ----------------------------------------------------------------------------------------
    private Map<Integer, String> getFileNames(int searchAnalysisId) {

        MsRunSearchAnalysisDAO saDao = DAOFactory.instance().getMsRunSearchAnalysisDAO();
        List<Integer> runSearchAnalysisIds = saDao.getRunSearchAnalysisIdsForAnalysis(searchAnalysisId);

        Map<Integer, String> filenameMap = new HashMap<Integer, String>(runSearchAnalysisIds.size()*2);
        for(int runSearchAnalysisId: runSearchAnalysisIds) {
            String filename = saDao.loadFilenameForRunSearchAnalysis(runSearchAnalysisId);
            filenameMap.put(runSearchAnalysisId, filename);
        }
        return filenameMap;

    }
    
    // ----------------------------------------------------------------------------------------
    // UN-FILTERED RESULT COUNT
    // ----------------------------------------------------------------------------------------
    private int getUnfilteredResultCount(Program program, int searchAnalysisId) {
        // Get ALL the filtered and resultIds
    	if(program == Program.PERCOLATOR) {
            PercolatorResultDAO presDao = DAOFactory.instance().getPercolatorResultDAO();
            return presDao.numAnalysisResults(searchAnalysisId);
        }
        else {
            log.error("Unrecognized analysis program: "+program.displayName());
            return 0;
        }
    }
}
