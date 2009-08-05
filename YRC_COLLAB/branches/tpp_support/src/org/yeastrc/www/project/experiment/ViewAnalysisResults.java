/**
 * ViewAnalysisResults.java
 * @author Vagisha Sharma
 * Aug 4, 2009
 * @version 1.0
 */
package org.yeastrc.www.project.experiment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.yeastrc.experiment.PeptideProphetResultPlus;
import org.yeastrc.experiment.PercolatorResultPlus;
import org.yeastrc.experiment.ProjectExperimentDAO;
import org.yeastrc.experiment.TabularPeptideProphetResults;
import org.yeastrc.experiment.TabularPercolatorResults;
import org.yeastrc.ms.dao.DAOFactory;
import org.yeastrc.ms.dao.analysis.MsRunSearchAnalysisDAO;
import org.yeastrc.ms.dao.analysis.peptideProphet.PeptideProphetResultDAO;
import org.yeastrc.ms.dao.analysis.percolator.PercolatorResultDAO;
import org.yeastrc.ms.dao.run.MsScanDAO;
import org.yeastrc.ms.dao.search.MsSearchDAO;
import org.yeastrc.ms.dao.search.sequest.SequestSearchResultDAO;
import org.yeastrc.ms.domain.analysis.MsSearchAnalysis;
import org.yeastrc.ms.domain.analysis.peptideProphet.PeptideProphetResult;
import org.yeastrc.ms.domain.analysis.percolator.PercolatorResult;
import org.yeastrc.ms.domain.run.MsScan;
import org.yeastrc.ms.domain.search.MsSearch;
import org.yeastrc.ms.domain.search.Program;
import org.yeastrc.ms.domain.search.SORT_BY;
import org.yeastrc.ms.domain.search.SORT_ORDER;
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectFactory;
import org.yeastrc.www.misc.Pageable;
import org.yeastrc.www.misc.ResultsPager;
import org.yeastrc.www.misc.Tabular;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

/**
 * 
 */
public class ViewAnalysisResults extends Action {

    private static final Logger log = Logger.getLogger(ViewAnalysisResults.class.getName());

    public ActionForward execute( ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response )
    throws Exception {



        // User making this request
        User user = UserUtils.getUser(request);
        if (user == null) {
            ActionErrors errors = new ActionErrors();
            errors.add("username", new ActionMessage("error.login.notloggedin"));
            saveErrors( request, errors );
            return mapping.findForward("authenticate");
        }

        // Get the form
        AnalysisFilterResultsForm myForm = (AnalysisFilterResultsForm)form;

        int searchAnalysisId = myForm.getSearchAnalysisId();
        if(searchAnalysisId == 0) {
            try {
                String strID = request.getParameter("ID");
                if(strID != null)
                    searchAnalysisId = Integer.parseInt(strID);


            } catch (NumberFormatException nfe) {
                ActionErrors errors = new ActionErrors();
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionMessage("error.general.invalid.id", "PeptideProphet analysis"));
                saveErrors( request, errors );
                return mapping.findForward("Failure");
            }
        }
        // If we still don't have a valid id, return an error
        if(searchAnalysisId == 0) {
            ActionErrors errors = new ActionErrors();
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionMessage("error.general.invalid.id", "PeptideProphet analysis"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }

        // If this is a brand new form
        if(myForm.getSearchAnalysisId() == 0) {
            myForm.setSearchAnalysisId(searchAnalysisId);
            myForm.setShowModified(true);
            myForm.setShowUnmodified(true);
            myForm.setExactPeptideMatch(true);
            if(myForm instanceof PeptideProphetFilterResultsForm) {
                ((PeptideProphetFilterResultsForm)myForm).setMinProbability("0.05");
                ((PeptideProphetFilterResultsForm)myForm).setSortBy(SORT_BY.PEPTP_PROB);
                ((PeptideProphetFilterResultsForm)myForm).setSortOrder(SORT_ORDER.DESC);
            }
            else if(myForm instanceof PercolatorFilterResultsForm) {
                ((PercolatorFilterResultsForm)myForm).setMaxQValue("0.05");
                ((PercolatorFilterResultsForm)myForm).setSortBy(SORT_BY.QVAL);
                ((PercolatorFilterResultsForm)myForm).setSortOrder(SORT_ORDER.ASC);
            }
        }


        // GET THE SUMMARY 
        List<Integer> projectIds = new ArrayList<Integer>();
        List<Integer> experimentIds = new ArrayList<Integer>();
        String programString = "";
        int numResults = 0;
        int numResultsFiltered = 0;


        MsSearchAnalysis analysis = DAOFactory.instance().getMsSearchAnalysisDAO().load(searchAnalysisId);
        programString = analysis.getAnalysisProgram()+" "+analysis.getAnalysisProgramVersion();
        

        List<Integer> searchIds = DAOFactory.instance().getMsSearchAnalysisDAO().getSearchIdsForAnalysis(searchAnalysisId);
        MsSearchDAO searchDao = DAOFactory.instance().getMsSearchDAO();


        for(int searchId: searchIds) {
            MsSearch search = searchDao.loadSearch(searchId);
            experimentIds.add(search.getExperimentId());
        }
        if(experimentIds.size() > 0) {
            // Get the projects for these experiments
            ProjectExperimentDAO projExpDao = ProjectExperimentDAO.instance();
            projectIds = projExpDao.getProjectIdsForExperiments(experimentIds);
        }

        // Does the user have access to look at these results? 
        for(int projectId: projectIds) {
            Project project;
            try {
                project = ProjectFactory.getProject(projectId);
                if (!project.checkReadAccess(user.getResearcher())) {
                    
                    // This user doesn't have access to this project.
                    ActionErrors errors = new ActionErrors();
                    errors.add("username", new ActionMessage("error.project.noaccess"));
                    saveErrors( request, errors );
                    return mapping.findForward("Failure");
                }
            } catch (Exception e) {
                
                // Couldn't load the project.
                ActionErrors errors = new ActionErrors();
                errors.add("username", new ActionMessage("error.project.projectnotfound"));
                saveErrors( request, errors );
                return mapping.findForward("Failure");  
            }
        }

        // Get ALL the filtered and sorted resultIds
        numResults = getUnfilteredResultCount(analysis.getAnalysisProgram(), searchAnalysisId);
        List<Integer> resultIds = getFilteredResultIds(analysis.getAnalysisProgram(), searchAnalysisId, myForm);
        numResultsFiltered = resultIds.size();


        // Extract the ones we will display
        int numResultsPerPage = 50;
        int pageNum = myForm.getPageNum();
        if(pageNum <= 0) {
            pageNum = 1;
            myForm.setPageNum(pageNum);
        }
        ResultsPager pager = ResultsPager.instance();
        boolean desc = false;
        if(myForm.getSortOrder() != null)
            desc = myForm.getSortOrder() == SORT_ORDER.DESC ? true : false;
        // TODO if the pageNum is out of range .....
        List<Integer> forPage = pager.page(resultIds, pageNum, numResultsPerPage, desc);



        // Get details for the result we will display
        // Set up for tabular display
        Tabular tabResults = getTabularResults(analysis, forPage, numResultsPerPage, myForm);
        
        ((Pageable)tabResults).setCurrentPage(pageNum);
        int pageCount = pager.getPageCount(resultIds.size(), numResultsPerPage);
        ((Pageable)tabResults).setLastPage(pageCount);
        List<Integer> pageList = pager.getPageList(resultIds.size(), pageNum, numResultsPerPage);
        ((Pageable)tabResults).setDisplayPageNumbers(pageList);

        

        // required attributes in the request
        request.setAttribute("projectIds", projectIds);
        request.setAttribute("experimentIds", experimentIds);
        request.setAttribute("program", programString);
        request.setAttribute("numResults", numResults);
        request.setAttribute("numResultsFiltered", numResultsFiltered);

        request.setAttribute("filterForm", myForm);
        request.setAttribute("results", tabResults);
        request.setAttribute("searchAnalysisId", searchAnalysisId);


        // Forward them on to the happy success view page!
        return mapping.findForward("Success");
    }
    
    // ----------------------------------------------------------------------------------------
    // TABULAR RESULTS
    // ----------------------------------------------------------------------------------------
    private Tabular getTabularResults(MsSearchAnalysis analysis, 
            List<Integer> forPage, int numResultsPerPage, AnalysisFilterResultsForm myForm) {
        
        if(analysis.getAnalysisProgram() == Program.PERCOLATOR) {
            return getPercolatorResults(analysis, forPage, numResultsPerPage, myForm);
        }
        else if(analysis.getAnalysisProgram() == Program.PEPTIDE_PROPHET) {
            return getPeptideProphetResults(analysis, forPage, numResultsPerPage, myForm);
        }
        
        log.error("Unrecognized analysis program: "+analysis.getAnalysisProgram().displayName());
        return null;
    }

    // -------PERCOLATOR RESULTS
    private Tabular getPercolatorResults(MsSearchAnalysis analysis,
            List<Integer> forPage, int numResultsPerPage, AnalysisFilterResultsForm myForm) {
        
        // Get details for the result we will display
        Map<Integer, String> filenameMap = getFileNames(analysis.getId());
        
        MsScanDAO scanDao = DAOFactory.instance().getMsScanDAO();
        SequestSearchResultDAO seqResDao = DAOFactory.instance().getSequestResultDAO();
        
        PercolatorResultDAO presDao = DAOFactory.instance().getPercolatorResultDAO();
        List<PercolatorResultPlus> results = new ArrayList<PercolatorResultPlus>(numResultsPerPage);
        for(Integer resultId: forPage) {
            PercolatorResult result = presDao.load(resultId);
            MsScan scan = scanDao.loadScanLite(result.getScanId());
            PercolatorResultPlus resPlus = new PercolatorResultPlus(result, scan);
            resPlus.setFilename(filenameMap.get(result.getRunSearchAnalysisId()));
            resPlus.setSequestData(seqResDao.load(resultId).getSequestResultData());
            results.add(resPlus);
        }

        // Which version of Percolator are we using
        String version = analysis.getAnalysisProgramVersion();
        boolean hasPEP = true;
        try {
            float vf = Float.parseFloat(version.trim());
            if(vf < 1.06)   hasPEP = false;
        }
        catch(NumberFormatException e){
            log.error("Cannot detrmine if this version of Percolator prints PEP. Version: "+version);
        }

        TabularPercolatorResults tabResults = new TabularPercolatorResults(results, hasPEP);
        tabResults.setSortedColumn(myForm.getSortBy());
        tabResults.setSortOrder(myForm.getSortOrder());
        
        return tabResults;
    }
    
    // -------PEPTIDE PROPHET RESULTS
    private Tabular getPeptideProphetResults(MsSearchAnalysis analysis,
            List<Integer> forPage, int numResultsPerPage, AnalysisFilterResultsForm myForm) {
        
        // Get details for the result we will display
        Map<Integer, String> filenameMap = getFileNames(analysis.getId());
        
        MsScanDAO scanDao = DAOFactory.instance().getMsScanDAO();
        SequestSearchResultDAO seqResDao = DAOFactory.instance().getSequestResultDAO();
        
        PeptideProphetResultDAO presDao = DAOFactory.instance().getPeptideProphetResultDAO();
        List<PeptideProphetResultPlus> results = new ArrayList<PeptideProphetResultPlus>(numResultsPerPage);
        for(Integer resultId: forPage) {
            PeptideProphetResult result = presDao.load(resultId);
            MsScan scan = scanDao.loadScanLite(result.getScanId());
            PeptideProphetResultPlus resPlus = new PeptideProphetResultPlus(result, scan);
            resPlus.setFilename(filenameMap.get(result.getRunSearchAnalysisId()));
            resPlus.setSequestData(seqResDao.load(resultId).getSequestResultData());
            results.add(resPlus);
        }

        TabularPeptideProphetResults tabResults = new TabularPeptideProphetResults(results);
        tabResults.setSortedColumn(myForm.getSortBy());
        tabResults.setSortOrder(myForm.getSortOrder());
        
        return tabResults;
    }
    
    // ----------------------------------------------------------------------------------------
    // UN-FILTERED RESULT COUNT
    // ----------------------------------------------------------------------------------------
    private int getUnfilteredResultCount(Program program, int searchAnalysisId) {
        // Get ALL the filtered and sorted resultIds
        if(program == Program.PEPTIDE_PROPHET) {
            PeptideProphetResultDAO ppRes = DAOFactory.instance().getPeptideProphetResultDAO();
            return ppRes.numAnalysisResults(searchAnalysisId);
        }
        else if(program == Program.PERCOLATOR) {
            PercolatorResultDAO presDao = DAOFactory.instance().getPercolatorResultDAO();
            return presDao.numAnalysisResults(searchAnalysisId);
        }
        else {
            log.error("Unrecognized analysis program: "+program.displayName());
            return 0;
        }
    }
    
    // ----------------------------------------------------------------------------------------
    // FILTERED RESULT IDS
    // ----------------------------------------------------------------------------------------
    private List<Integer> getFilteredResultIds(Program program, int searchAnalysisId, AnalysisFilterResultsForm myForm) {
        
        if(program == Program.PEPTIDE_PROPHET) {
            PeptideProphetResultDAO ppRes = DAOFactory.instance().getPeptideProphetResultDAO();
            if(myForm.isPeptidesView()) {
                return ppRes.loadResultIdsForSearchAnalysisUniqPeptide(searchAnalysisId, 
                        ((PeptideProphetFilterResultsForm)myForm).getFilterCriteria(), myForm.getSortCriteria());
            }
            else {
                return ppRes.loadResultIdsForSearchAnalysis(searchAnalysisId, 
                        ((PeptideProphetFilterResultsForm)myForm).getFilterCriteria(), myForm.getSortCriteria());
            }
            
        }
        else if(program == Program.PERCOLATOR) {
            PercolatorResultDAO presDao = DAOFactory.instance().getPercolatorResultDAO();
            if(myForm.isPeptidesView()) {
                return presDao.loadResultIdsForSearchAnalysisUniqPeptide(searchAnalysisId, 
                        ((PercolatorFilterResultsForm)myForm).getFilterCriteria(), myForm.getSortCriteria());
            }
            else {
                return presDao.loadResultIdsForSearchAnalysis(searchAnalysisId, 
                        ((PercolatorFilterResultsForm)myForm).getFilterCriteria(), myForm.getSortCriteria());
            }
        }
        else {
            log.error("Unrecognized analysis program: "+program.displayName());
            return new ArrayList<Integer>(0);
        }
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
}