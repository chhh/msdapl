/**
 * SequestResultsGetter.java
 * @author Vagisha Sharma
 * Jan 2, 2009
 * @version 1.0
 */
package edu.uwpr.protinfer.idpicker;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.yeastrc.ms.dao.DAOFactory;
import org.yeastrc.ms.dao.search.MsSearchResultProteinDAO;
import org.yeastrc.ms.dao.search.sequest.SequestSearchResultDAO;
import org.yeastrc.ms.domain.search.MsSearchResultProtein;
import org.yeastrc.ms.domain.search.sequest.SequestResultData;
import org.yeastrc.ms.domain.search.sequest.SequestSearchResult;

import edu.uwpr.protinfer.infer.Peptide;
import edu.uwpr.protinfer.infer.PeptideHit;
import edu.uwpr.protinfer.infer.Protein;
import edu.uwpr.protinfer.infer.ProteinHit;
import edu.uwpr.protinfer.util.TimeUtils;

/**
 * 
 */
public class SequestResultsGetter implements ResultsGetter {

    private static final Logger log = Logger.getLogger(SequestResultsGetter.class);
    
    private static final SequestResultsGetter instance = new SequestResultsGetter();
    
    private SequestResultsGetter() {}
    
    public static SequestResultsGetter instance() {
        return instance;
    }

    @Override
    public List<PeptideSpectrumMatchNoFDR> getResultsNoFdr(int inputId, IDPickerParams params) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public List<PeptideSpectrumMatchIDP> getResults(int inputId, IDPickerParams params) {
        
        SequestSearchResultDAO resultDao = DAOFactory.instance().getSequestResultDAO();
        MsSearchResultProteinDAO protDao = DAOFactory.instance().getMsProteinMatchDAO();
        
        log.info("Loading top hits for runSearchID: "+inputId);
        
        long start = System.currentTimeMillis();
        long s = start;
        List<SequestSearchResult> resultList = resultDao.loadTopResultsForRunSearchN(inputId);
        log.info("\tTotal top hits for "+inputId+": "+resultList.size());
        long e = System.currentTimeMillis();
        
        log.info("\tTime: "+TimeUtils.timeElapsedSeconds(s,e)+" seconds.");
        
       
        // make a list of peptide spectrum matches and read the matching proteins from the database
        s = System.currentTimeMillis();
        
        String decoyPrefix = params.getDecoyPrefix();
        
        List<PeptideSpectrumMatchIDP> psmList = new ArrayList<PeptideSpectrumMatchIDP>(resultList.size());
        for (SequestSearchResult result: resultList) {
            
            SequestResultData scores = result.getSequestResultData();
            
            // get the peptide
            Peptide peptide = new Peptide(result.getResultPeptide().getPeptideSequence(), -1);
            PeptideHit peptHit = new PeptideHit(peptide);
            
            // read the matching proteins from the database now
            List<MsSearchResultProtein> msProteinList = protDao.loadResultProteins(result.getId());
           
            for (MsSearchResultProtein protein: msProteinList) {
                Protein prot = new Protein(protein.getAccession(), -1);
                if(decoyPrefix != null) {
                    if (prot.getAccession().startsWith(decoyPrefix))
                        prot.setDecoy();
                }
                peptHit.addProteinHit(new ProteinHit(prot, '\u0000', '\u0000'));
            }
            
            SpectrumMatchIDPImpl specMatch = new SpectrumMatchIDPImpl();
            specMatch.setHitId(result.getId());
            specMatch.setScanId(result.getScanId());
            specMatch.setCharge(result.getCharge());
            specMatch.setSourceId(inputId);
            specMatch.setRank(scores.getxCorrRank());
            
            PeptideSpectrumMatchIDPImpl psm = new PeptideSpectrumMatchIDPImpl();
            psm.setPeptide(peptHit);
            psm.setSpectrumMatch(specMatch);
            psm.setAbsoluteScore(scores.getxCorr().doubleValue());
            psm.setRelativeScore(scores.getDeltaCN().doubleValue());
            
            psmList.add(psm);
        }
        e = System.currentTimeMillis();
        log.info("\tTime to get matching proteins and create list of spectrum matches: "+TimeUtils.timeElapsedSeconds(s, e)+" seconds.");
        e = System.currentTimeMillis();
        log.info("Total time: "+TimeUtils.timeElapsedSeconds(start, e)+" seconds.");
        return psmList;
    }
    
}
