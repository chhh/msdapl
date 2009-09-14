/**
 * PeptideProphetAnalysisUploadDAOImpl.java
 * @author Vagisha Sharma
 * Sep 11, 2009
 * @version 1.0
 */
package org.yeastrc.ms.upload.dao.analysis.peptideProphet.ibatis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yeastrc.ms.dao.ibatis.BaseSqlMapDAO;
import org.yeastrc.ms.dao.search.MsRunSearchDAO;
import org.yeastrc.ms.domain.analysis.peptideProphet.PeptideProphetAnalysis;
import org.yeastrc.ms.upload.dao.analysis.MsSearchAnalysisUploadDAO;
import org.yeastrc.ms.upload.dao.analysis.peptideProphet.PeptideProphetAnalysisUploadDAO;
import org.yeastrc.ms.util.StringUtils;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * 
 */
public class PeptideProphetAnalysisUploadDAOImpl extends BaseSqlMapDAO implements
    PeptideProphetAnalysisUploadDAO{

    private static final String namespace = "PeptideProphetAnalysis";
    
    private final MsSearchAnalysisUploadDAO analysisDao;
    
    public PeptideProphetAnalysisUploadDAOImpl(SqlMapClient sqlMap, 
                MsSearchAnalysisUploadDAO analysisDao) {
        super(sqlMap);
        this.analysisDao = analysisDao;
    }

    @Override
    public PeptideProphetAnalysis load(int analysisId) {
        return (PeptideProphetAnalysis) queryForObject(namespace+".select", analysisId);
    }

    @Override
    public PeptideProphetAnalysis loadAnalysisForFileName(String fileName, int searchId) {
        // get all the runSearchIds for the search
        List<Integer> analysisIds = analysisDao.getAnalysisIdsForSearch(searchId);
        String idString = StringUtils.makeCommaSeparated(analysisIds);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("filename", fileName);
        map.put("analysisIds", idString);
        
        return (PeptideProphetAnalysis) queryForObject(namespace+".selectAnalysisForFileName", map);
    }

    @Override
    public int save(PeptideProphetAnalysis analysis) {
        int analysisId = analysisDao.save(analysis);
        analysis.setId(analysisId);
        this.save(namespace+".insert", analysis);
        return analysisId;
    }
}
