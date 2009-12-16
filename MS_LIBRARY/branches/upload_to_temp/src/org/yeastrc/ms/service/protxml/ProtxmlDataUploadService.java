/**
 * ProtxmlDataUploadService.java
 * @author Vagisha Sharma
 * Jul 28, 2009
 * @version 1.0
 */
package org.yeastrc.ms.service.protxml;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.yeastrc.ms.dao.DAOFactory;
import org.yeastrc.ms.dao.ProteinferDAOFactory;
import org.yeastrc.ms.dao.analysis.MsRunSearchAnalysisDAO;
import org.yeastrc.ms.dao.nrseq.NrSeqLookupUtil;
import org.yeastrc.ms.dao.protinfer.ibatis.ProteinferInputDAO;
import org.yeastrc.ms.dao.protinfer.ibatis.ProteinferIonDAO;
import org.yeastrc.ms.dao.protinfer.ibatis.ProteinferPeptideDAO;
import org.yeastrc.ms.dao.protinfer.ibatis.ProteinferProteinDAO;
import org.yeastrc.ms.dao.protinfer.ibatis.ProteinferSpectrumMatchDAO;
import org.yeastrc.ms.dao.protinfer.proteinProphet.ProteinProphetParamDAO;
import org.yeastrc.ms.dao.protinfer.proteinProphet.ProteinProphetProteinDAO;
import org.yeastrc.ms.dao.protinfer.proteinProphet.ProteinProphetProteinGroupDAO;
import org.yeastrc.ms.dao.protinfer.proteinProphet.ProteinProphetProteinIonDAO;
import org.yeastrc.ms.dao.protinfer.proteinProphet.ProteinProphetRocDAO;
import org.yeastrc.ms.dao.protinfer.proteinProphet.ProteinProphetRunDAO;
import org.yeastrc.ms.dao.protinfer.proteinProphet.ProteinProphetSubsumedProteinDAO;
import org.yeastrc.ms.dao.search.MsSearchDAO;
import org.yeastrc.ms.dao.search.MsSearchResultDAO;
import org.yeastrc.ms.domain.analysis.peptideProphet.PeptideProphetAnalysis;
import org.yeastrc.ms.domain.analysis.peptideProphet.PeptideProphetResult;
import org.yeastrc.ms.domain.nrseq.NrDbProtein;
import org.yeastrc.ms.domain.protinfer.ProteinInferenceProgram;
import org.yeastrc.ms.domain.protinfer.ProteinferInput;
import org.yeastrc.ms.domain.protinfer.ProteinferProtein;
import org.yeastrc.ms.domain.protinfer.ProteinferSpectrumMatch;
import org.yeastrc.ms.domain.protinfer.proteinProphet.Modification;
import org.yeastrc.ms.domain.protinfer.proteinProphet.ProteinProphetGroup;
import org.yeastrc.ms.domain.protinfer.proteinProphet.ProteinProphetParam;
import org.yeastrc.ms.domain.protinfer.proteinProphet.ProteinProphetProtein;
import org.yeastrc.ms.domain.protinfer.proteinProphet.ProteinProphetProteinPeptide;
import org.yeastrc.ms.domain.protinfer.proteinProphet.ProteinProphetProteinPeptideIon;
import org.yeastrc.ms.domain.protinfer.proteinProphet.ProteinProphetROC;
import org.yeastrc.ms.domain.protinfer.proteinProphet.ProteinProphetRun;
import org.yeastrc.ms.domain.search.MsResidueModification;
import org.yeastrc.ms.domain.search.MsResultResidueMod;
import org.yeastrc.ms.domain.search.MsResultTerminalMod;
import org.yeastrc.ms.domain.search.MsSearch;
import org.yeastrc.ms.domain.search.MsSearchDatabase;
import org.yeastrc.ms.domain.search.MsTerminalModification;
import org.yeastrc.ms.domain.search.SearchFileFormat;
import org.yeastrc.ms.domain.search.impl.ResultResidueModBean;
import org.yeastrc.ms.domain.search.impl.ResultTerminalModBean;
import org.yeastrc.ms.parser.DataProviderException;
import org.yeastrc.ms.parser.protxml.InteractProtXmlParser;
import org.yeastrc.ms.service.DynamicModLookupUtil;
import org.yeastrc.ms.service.ModifiedSequenceBuilder;
import org.yeastrc.ms.service.ModifiedSequenceBuilderException;
import org.yeastrc.ms.service.ProtinferUploadService;
import org.yeastrc.ms.service.UploadException;
import org.yeastrc.ms.service.UploadException.ERROR_CODE;
import org.yeastrc.ms.upload.dao.UploadDAOFactory;
import org.yeastrc.ms.upload.dao.analysis.peptideProphet.PeptideProphetAnalysisUploadDAO;
import org.yeastrc.ms.upload.dao.analysis.peptideProphet.PeptideProphetResultUploadDAO;
import org.yeastrc.ms.util.TimeUtils;

/**
 * 
 */
public class ProtxmlDataUploadService implements ProtinferUploadService {

    private final DAOFactory daoFactory;
    private final MsSearchResultDAO resDao;
    private final PeptideProphetResultUploadDAO ppResDao;
    
    
    private final ProteinferDAOFactory piDaoFactory;
    private ProteinferPeptideDAO peptDao;
    private ProteinferProteinDAO protDao;
    private ProteinferIonDAO ionDao;
    private ProteinferSpectrumMatchDAO psmDao;
    private ProteinProphetRunDAO ppRunDao;
    private ProteinProphetProteinGroupDAO grpDao;
    private ProteinProphetProteinDAO ppProtDao;
    private ProteinProphetProteinIonDAO ppProteinIonDao; 
    private ProteinProphetSubsumedProteinDAO ppSusumedDao;
    
    private Map<String, Integer> peptideMap; // peptide sequence and its ID in the database
    private Map<String, Integer> ionMap;     // ion sequence (chg_modseq) and its ID in the database
    private Map<String, Integer> modifiedStateMap;  // map of modified sequence and modificationStateID
    private Map<Integer, Integer> peptModStateCountMap; // map to keep track of # of mod. states for a peptide
    
    private DynamicModLookupUtil modLookup;
    private int searchId;
//    private int analysisId;
    private String protxmlDirectory;
    private int nrseqDatabaseId;
    
//    private int uploadedPinferId;
    private int indistinguishableProteinGroupId = 1;
    
//    private int numProteinGroups;
    
    private StringBuilder uploadMsg;
    private StringBuilder preUploadCheckMsg;
    private boolean preUploadCheckDone = false;
    
    private static final Pattern fileNamePattern = Pattern.compile("interact\\S*.prot.xml");
    private List<String> protXmlFiles = new ArrayList<String>();
    private List<Integer> runSearchAnalysisIds;  
    
    private InteractProtXmlParser parser = null;
    private Pattern equivalentAAPattern = null;
    private List<Pattern> equivalentAAPatterns = null;
    private Map<Pattern, char[]> equivalentAminoAcids = null;
    
    private Map<String, Integer> proteinIdMap;
    
    private static final Logger log = Logger.getLogger(ProtxmlDataUploadService.class.getName());
    
    public ProtxmlDataUploadService() {
        
        peptideMap = new HashMap<String, Integer>();
        ionMap = new HashMap<String, Integer>();
        modifiedStateMap = new HashMap<String, Integer>();
        peptModStateCountMap = new HashMap<Integer, Integer>();
        runSearchAnalysisIds = new ArrayList<Integer>();
        
        
        piDaoFactory = ProteinferDAOFactory.instance();
        daoFactory = DAOFactory.instance();
        
        resDao = daoFactory.getMsSearchResultDAO();
        ppResDao = UploadDAOFactory.getInstance().getPeptideProphetResultDAO();
        
        peptDao = piDaoFactory.getProteinferPeptideDao();
        protDao = piDaoFactory.getProteinferProteinDao();
        ionDao = piDaoFactory.getProteinferIonDao();
        psmDao = piDaoFactory.getProteinferSpectrumMatchDao();
        grpDao = piDaoFactory.getProteinProphetProteinGroupDao();
        ppRunDao = piDaoFactory.getProteinProphetRunDao();
        ppProtDao = piDaoFactory.getProteinProphetProteinDao();
        ppProteinIonDao = piDaoFactory.getProteinProphetProteinIonDao();
        ppSusumedDao = piDaoFactory.getProteinProphetSubsumedProteinDao();
        
    }
    
    private void reset() {
        
        indistinguishableProteinGroupId = 1;
        
        peptideMap.clear();
        ionMap.clear();
        modifiedStateMap.clear();
        peptModStateCountMap.clear();
        runSearchAnalysisIds.clear();
        
        proteinIdMap = new HashMap<String, Integer>();
    }
    
    public void upload() throws UploadException {
        
        if(!preUploadCheckDone) {
            if(!preUploadCheckPassed()) {
                UploadException ex = new UploadException(ERROR_CODE.PREUPLOAD_CHECK_FALIED);
                ex.appendErrorMessage(this.getPreUploadCheckMsg());
                ex.appendErrorMessage("\n\t!!!PROTEIN INFERENCE WILL NOT BE UPLOADED\n");
                throw ex;
            }
        }
        
        uploadMsg = new StringBuilder();
        
        // Make sure we have a nrseq database ID
        MsSearchDAO searchDao = DAOFactory.instance().getMsSearchDAO();
        MsSearch search = searchDao.loadSearch(searchId);
        List<MsSearchDatabase> dbList = search.getSearchDatabases();
        if(dbList.size() != 1) {
            UploadException ex = new UploadException(ERROR_CODE.PREUPLOAD_CHECK_FALIED);
            ex.appendErrorMessage("No NRSEQ fasta database ID found for searchID: "+searchId);
            ex.appendErrorMessage("\n\t!!!PROTEIN INFERENCE WILL NOT BE UPLOADED\n");
            throw ex;
        }
        else {
            this.nrseqDatabaseId = dbList.get(0).getSequenceDatabaseId();
        }
        
        for(String protxmlFile: this.protXmlFiles) {
            uploadProtxmlFile(protxmlFile);
        }
    }


    private void uploadProtxmlFile(String protxmlFile) throws UploadException {
        
        log.info("Uploading protein inference results in file: "+protxmlFile);
        
        long s = System.currentTimeMillis();
        
        reset();
        
        int numProteinGroups = 0;
        int uploadedPinferId = 0;
        
        parser = new InteractProtXmlParser();
        try {
            parser.open(this.protxmlDirectory+File.separator+protxmlFile);
        }
        catch (DataProviderException e) {
            UploadException ex = new UploadException(ERROR_CODE.PROTXML_ERROR, e);
            ex.appendErrorMessage(e.getMessage());
            throw ex;
        }
        
        modLookup = new DynamicModLookupUtil(searchId);
        
        // create a new entry for this protein inference run
        try {uploadedPinferId = addProteinInferenceRun(parser);}
        catch(UploadException ex) {
            parser.close();
            ex.appendErrorMessage("DELETING PROTEIN INFERENCE..."+uploadedPinferId);
            ppRunDao.delete(uploadedPinferId);
            throw ex;
        }
        
        // save the protein and protein groups
        try {
            while(parser.hasNextProteinGroup()) {
                saveProteinProphetGroup(parser.getNextGroup(), uploadedPinferId);
                numProteinGroups++;
            }
        }
        catch (DataProviderException e) {
            UploadException ex = new UploadException(ERROR_CODE.PROTXML_ERROR, e);
            ex.appendErrorMessage(e.getErrorMessage());
            ex.appendErrorMessage("DELETING PROTEIN INFERENCE..."+uploadedPinferId);
            ppRunDao.delete(uploadedPinferId);
            throw ex;
        }
        catch(UploadException e) {
            e.appendErrorMessage("DELETING PROTEIN INFERENCE..."+uploadedPinferId);
            ppRunDao.delete(uploadedPinferId);
            throw e;
        }
        catch(RuntimeException e) {
            UploadException ex = new UploadException(ERROR_CODE.GENERAL, e);
            ex.appendErrorMessage("DELETING PROTEIN INFERENCE..."+uploadedPinferId);
            ppRunDao.delete(uploadedPinferId);
            throw ex;
        }
        finally {
            parser.close();
        }
        
        long e = System.currentTimeMillis();
        
        log.info("Uploaded file: "+protxmlFile+"; ID: "+uploadedPinferId+"; #protein groups: "+numProteinGroups+
                "; Time: "+TimeUtils.timeElapsedSeconds(s, e));
        
        uploadMsg.append("\n\tProtein inferenceID: "+uploadedPinferId);
        uploadMsg.append("; #Protein groups in file: "+protxmlFile+": "+numProteinGroups);
    }


    private void saveProteinProphetGroup(ProteinProphetGroup proteinGroup,
            int pinferId) throws UploadException {
        
        proteinGroup.setProteinferId(pinferId);
        
        int ppGrpId = grpDao.saveGroup(proteinGroup);
        
        Map<Integer, Set<String>> subsumedMap = new HashMap<Integer, Set<String>>();
//        Map<String, Integer> proteinIdMap = new HashMap<String, Integer>();
        
        for(ProteinProphetProtein protein: proteinGroup.getProteinList()) {
            
            protein.setProteinferId(pinferId);
            protein.setProteinProphetGroupId(ppGrpId);
            protein.setGroupId(this.indistinguishableProteinGroupId);
            int piProteinId = saveProtein(protein, subsumedMap);
            proteinIdMap.put(protein.getProteinName(), piProteinId);
            
            // Are there indistinguishable proteins?
            for(String name: protein.getIndistinguishableProteins()) {
                if(name.equals(protein.getProteinName()))
                    continue;
                ProteinProphetProtein iProt = protein.getIndistinguishableProtein(name);
                piProteinId = saveProtein(iProt, subsumedMap);
                proteinIdMap.put(name, piProteinId);
                // TODO what about protein coverage
            }
            
            this.indistinguishableProteinGroupId++;
        }
        
        saveSubsumedProteins(subsumedMap, proteinIdMap);
    }
    
    private void saveSubsumedProteins(Map<Integer, Set<String>> subsumedMap, Map<String, Integer> proteinIdMap) {
        
        for(int subsumedId: subsumedMap.keySet()) {
            Set<String> subsuming = subsumedMap.get(subsumedId);
            for(String name: subsuming) {
                Integer subsumingId = proteinIdMap.get(name);
                if(subsumingId == null) {
                    log.warn("Subsuming protein not seen yet: "+name);
                    continue;
                }
                ppSusumedDao.saveSubsumedProtein(subsumedId, subsumingId);
            }
        }
    }

    private int saveProtein(ProteinProphetProtein protein, Map<Integer, Set<String>> subsumedMap) 
        throws UploadException {
        
        int nrseqId = getNrseqProteinId(protein.getProteinName(), nrseqDatabaseId);
        
        // if a protein with this nrseqId has already been saved do not save it again
        // ProteinProphet can list identical proteins (same sequence) as indistinguishable
        // proteins.  We will not save them twice. 
        ProteinferProtein oldProtein = ppProtDao.loadProtein(protein.getProteinferId(), nrseqId);
        if(oldProtein != null) {
            return oldProtein.getId();
        }
        
        if(nrseqId == 0) {
            UploadException ex = new UploadException(ERROR_CODE.PROTEIN_NOT_FOUND);
            ex.appendErrorMessage("No NRSEQ id foud for protein: "+protein.getProteinName()+"; databaseId: "+nrseqDatabaseId);
            throw ex;
        }
        
        protein.setNrseqProteinId(nrseqId);
        int piProteinId = ppProtDao.saveProteinProphetProtein(protein);
        // save peptides
        savePeptides(protein);
        
        // Is this a subsumed protein
        // NOTE: assuming all subsuming proteins for a protein will be in the same
        // protein group as the protein
        if(protein.getSubsumed()) {
            subsumedMap.put(piProteinId, protein.getSusumingProteins());
        }
        return piProteinId;
    }
    
    private void savePeptides(ProteinProphetProtein protein) throws UploadException {
        
        for(ProteinProphetProteinPeptide peptide: protein.getPeptides()) {
            // is this peptide saved already? 
            Integer pinferPeptideId = peptideMap.get(peptide.getSequence());
            if(pinferPeptideId == null) {
                // save the peptide
                peptide.setProteinferId(protein.getProteinferId());
                pinferPeptideId = peptDao.save(peptide);
                peptideMap.put(peptide.getSequence(), pinferPeptideId);
            }
            peptide.setId(pinferPeptideId);
            
            // link this peptide and protein
            protDao.saveProteinferProteinPeptideMatch(protein.getId(), peptide.getId());
            
            
            // look at each ion for the peptide
            for(ProteinProphetProteinPeptideIon ion: peptide.getIonList()) {
                
                ion.setPiProteinId(protein.getId());
                ion.setProteinferPeptideId(pinferPeptideId);
                
                Integer pinferIonId = savePeptideIon(peptide, ion);
                
                // create an entry in the ProteinProphetProteinIon table
                ion.setId(pinferIonId);
                ppProteinIonDao.save(ion);
            }
        }
    }

    private Integer savePeptideIon(ProteinProphetProteinPeptide peptide,
            ProteinProphetProteinPeptideIon ion) throws UploadException {
        
        // Update the modified sequence for the ion based on the modifications we have
        // in the database for this search
        List<MsResultResidueMod> dynaResModList = null;
        List<MsResultTerminalMod> dynaTermModList = null;
        if(ion.getModifications().size() > 0) {
            String strippedSeq = peptide.getSequence();
            
            dynaResModList = getMatchingResidueModifications(ion, strippedSeq);
            dynaTermModList = getMatchingTerminalModifications(ion, strippedSeq);
            updateModifiedSequence(ion, strippedSeq, dynaResModList, dynaTermModList);
        }
        
        Integer pinferIonId = ionMap.get(ion.getCharge()+"_"+ion.getModifiedSequence());
        
        if(pinferIonId == null) {
            
            Integer modStateId = modifiedStateMap.get(ion.getModifiedSequence());
            if(modStateId == null) {
                Integer modStateCnt = peptModStateCountMap.get(peptide.getId());
                if(modStateCnt == null) {
                    modStateCnt = 1;
                    peptModStateCountMap.put(peptide.getId(), modStateCnt);
                }
                else
                    peptModStateCountMap.put(peptide.getId(), ++modStateCnt);
                modStateId = modStateCnt;
                modifiedStateMap.put(ion.getModifiedSequence(), modStateId);
                
            }
            
            ion.setModificationStateId(modStateId);
            pinferIonId = ionDao.save(ion);
            ion.setId(pinferIonId);
            
            ionMap.put(ion.getCharge()+"_"+ion.getModifiedSequence(), pinferIonId);
            
            // save spectra for the ion
            saveIonSpectra(ion, dynaResModList);
        }
        return pinferIonId;
    }

    private void updateModifiedSequence(ProteinProphetProteinPeptideIon ion,
            String strippedSeq, List<MsResultResidueMod> modList,
            List<MsResultTerminalMod> termModList)
            throws UploadException {
        
        try {
            String modifiedSequence = ModifiedSequenceBuilder.build(strippedSeq, modList, termModList);
            ion.setModifiedSequence(modifiedSequence);
        }
        catch (ModifiedSequenceBuilderException e) {
            UploadException ex = new UploadException(ERROR_CODE.MOD_LOOKUP_FAILED, e);
            ex.appendErrorMessage(e.getMessage());
            throw ex;
        }
    }

    private List<MsResultResidueMod> getMatchingResidueModifications(
            ProteinProphetProteinPeptideIon ion, String strippedSeq)
            throws UploadException {
        
        List<MsResultResidueMod> modList = new ArrayList<MsResultResidueMod>(ion.getModifications().size());
        
        for(Modification mod: ion.getModifications()) {
            
            // if this is not a dynamic modification ignore it
            // NOTE: ProtXml modifications are 1-based.  
            if(modLookup.isStaticModification(strippedSeq.charAt(mod.getPosition() - 1), mod.getMass(), true))
                continue;
            
            MsResidueModification dbMod = modLookup.getDynamicResidueModification(
                        strippedSeq.charAt(mod.getPosition() - 1), // ProtXml modifications are 1-based
                        mod.getMass(),
                        true); // mass = mass of amino acid + modification mass
            
            if(dbMod == null) {
                UploadException ex = new UploadException(ERROR_CODE.MOD_LOOKUP_FAILED);
                ex.appendErrorMessage("searchId: "+searchId+
                        "; peptide: "+strippedSeq+
                        "; char: "+strippedSeq.charAt(mod.getPosition() - 1)+
                        "; pos: "+mod.getPosition()+
                        "; mass: "+mod.getMass());
                throw ex;
            }
            
            ResultResidueModBean resModBean = new ResultResidueModBean();
            resModBean.setModificationMass(dbMod.getModificationMass());
            resModBean.setModificationSymbol(dbMod.getModificationSymbol());
            resModBean.setModifiedPosition(mod.getPosition() - 1); // ProtXml modifications are 1-based
            resModBean.setModifiedResidue(dbMod.getModifiedResidue());
            
            modList.add(resModBean);
        }
        return modList;
    }
    
    private List<MsResultTerminalMod> getMatchingTerminalModifications(
            ProteinProphetProteinPeptideIon ion, String strippedSeq)
            throws UploadException {
        
        List<MsResultTerminalMod> modList = new ArrayList<MsResultTerminalMod>(ion.getModifications().size());
        
        for(Modification mod: ion.getModifications()) {
            
            // don't look any further if this is not a modification at the terminal residues
            if(!mod.isTerminalModification())
                continue;
            
            MsTerminalModification dbMod = modLookup.getTerminalModification(mod.getTerminus(), mod.getMass());
            if(dbMod == null) {
                UploadException ex = new UploadException(ERROR_CODE.MOD_LOOKUP_FAILED);
                ex.appendErrorMessage("searchId: "+searchId+
                        "; peptide: "+strippedSeq+
                        "; terminus: "+mod.getTerminus()+
                        "; mass: "+mod.getMass());
                throw ex;
            }
            
            ResultTerminalModBean resModBean = new ResultTerminalModBean();
            resModBean.setModificationMass(dbMod.getModificationMass());
            resModBean.setModificationSymbol(dbMod.getModificationSymbol());
            resModBean.setModifiedTerminal(dbMod.getModifiedTerminal());
            
            modList.add(resModBean);
        }
        return modList;
    }
    
    private void saveIonSpectra(ProteinProphetProteinPeptideIon ion, List<MsResultResidueMod> modList) 
        throws UploadException {
        
        
        // get all spectra for the given searchID that have the given unmodified sequence
        List<Integer> resultIds = getSearchResultIdsMatchingPeptide(ion);
        
        List<PeptideProphetResult> matchingResults = new ArrayList<PeptideProphetResult>();
        for(int searchResultId: resultIds) {
            
            for(int runSearchAnalysisId: this.runSearchAnalysisIds) {
                PeptideProphetResult result = ppResDao.loadForRunSearchAnalysis(searchResultId, runSearchAnalysisId);
                if(result == null)
                    continue;

                // ignore all spectra with PeptideProphet probability < 0.05
                if(result.getProbability() < parser.getMinInitialProbability())
                    continue;

                matchingResults.add(result);
            }
        }
        
        // sort the results by probability
        Collections.sort(matchingResults, new Comparator<PeptideProphetResult>() {
            @Override
            public int compare(PeptideProphetResult o1, PeptideProphetResult o2) {
                return Double.valueOf(o2.getProbability()).compareTo(o1.getProbability());
            }});
        
        // store the ones that have the charge and modification state as this ion
        int rank = 0;
        int numFound = 0;
        for(PeptideProphetResult result: matchingResults) {
        
            rank++;
            // make sure they are the same charge
            if(result.getCharge() != ion.getCharge())
                continue;
            
            List<MsResultResidueMod> resMods = result.getResultPeptide().getResultDynamicResidueModifications();
            List<MsResultTerminalMod> termMods = result.getResultPeptide().getResultDynamicTerminalModifications();
            String modifiedSeq;
            
            try {
                modifiedSeq = ModifiedSequenceBuilder.build(ion.getUnmodifiedSequence(), resMods, termMods);
            }
            catch (ModifiedSequenceBuilderException e) {
                UploadException ex = new UploadException(ERROR_CODE.MOD_LOOKUP_FAILED);
                ex.appendErrorMessage("Error building modified sequence for result: "+result.getId()+
                        "; sequence: "+result.getResultPeptide().getPeptideSequence());
                ex.appendErrorMessage(e.getMessage());
                throw ex;
            }
            if(ion.getModifiedSequence().equals(modifiedSeq)) {
                numFound++;
                ProteinferSpectrumMatch psm = new ProteinferSpectrumMatch();
                psm.setResultId(result.getId());
                psm.setProteinferIonId(ion.getId());
                psm.setRank(rank); 
                psmDao.saveSpectrumMatch(psm);
            }
        }
        
        // make sure the number of results returned above match the spectrum count for this ion in the 
        // ProtXml file.
        if(numFound != ion.getSpectrumCount()) {
//            UploadException ex = new UploadException(ERROR_CODE.GENERAL);
//            ex.appendErrorMessage("Spectrum count ("+ion.getSpectrumCount()+") for ion ("+ion.getModifiedSequence()+
//                        ") does not match the number of results returned: "+numFound);
//            throw ex;
            log.warn("Spectrum count ("+ion.getSpectrumCount()+") for ion ("+ion.getModifiedSequence()+
                  ")  charge ("+ion.getCharge()+") does not match the number of results returned: "+numFound);
        }
    }

    private List<Integer> getSearchResultIdsMatchingPeptide(
            ProteinProphetProteinPeptideIon ion) {
        
        // first check if this peptide had any equivalent amino acids (e.g I, L)
        boolean equi = hasEquivalentAminoAcids(ion.getUnmodifiedSequence());
        if(!equi)
            return resDao.loadResultIdsForSearchPeptide(searchId, ion.getUnmodifiedSequence());
        else {
            // MySQL regexps appear to be slow so switching to searching on all permutations
//            String peptideRegex = makeRegex(ion.getUnmodifiedSequence());
//            return resDao.loadResultIdsForSearchPeptideRegex(searchId, peptideRegex);
            List<String> peptidePermutations = makePermutations(ion.getUnmodifiedSequence());
            return resDao.loadResultIdsForSearchPeptides(searchId, peptidePermutations);
        }
    }

    private List<String> makePermutations(String unmodifiedSequence) {
        
        if(equivalentAAPatterns == null) {
            equivalentAAPatterns = new ArrayList<Pattern>();
            equivalentAminoAcids = new HashMap<Pattern, char[]>();
            List<String> eqAA = parser.getEquivalentResidues();
            for(String a: eqAA) {
                Pattern p = Pattern.compile("["+a+"]");
                equivalentAAPatterns.add(p);
                equivalentAminoAcids.put(p, a.toCharArray());
            }
        }
        
        // get an index of all positions and the options available at the positions
        List<Integer> matchingPositions = new ArrayList<Integer>();
        Map<Integer, char[]> postionOptions = new HashMap<Integer, char[]>();
        
        for(Pattern p: equivalentAAPatterns) {
            Matcher m = p.matcher(unmodifiedSequence);
            while(m.find()) {
                int idx = m.start();
                matchingPositions.add(idx);
                postionOptions.put(idx, equivalentAminoAcids.get(p));
            }
        }
        Collections.sort(matchingPositions);
        
        return permute(0, unmodifiedSequence, matchingPositions, postionOptions);
    }
    
    private List<String> permute(int index, String sequence, 
            List<Integer> matchingPositions, Map<Integer, char[]> positionOptions) {
        
        if(matchingPositions == null || matchingPositions.size() == 0) {
            List<String> permutations = new ArrayList<String>(1);
            permutations.add(sequence);
            return permutations;
        }
        
        List<String> futureOptions = new ArrayList<String>(0);
        if(index < matchingPositions.size()-1) {
            futureOptions = permute(index+1, sequence, matchingPositions, positionOptions);
        }
        
        int start = index == 0 ? 0 : matchingPositions.get(index - 1) + 1;
        
        String mysequence = sequence.substring(start, matchingPositions.get(index)); // end indices are exclusive
        int myIndex = matchingPositions.get(index);
        List<String> allPermutations = new ArrayList<String>();
        
        for(char opt: positionOptions.get(myIndex)) {
            String seq = mysequence+opt;
            if(index == matchingPositions.size() - 1) {
                seq = seq + sequence.substring(matchingPositions.get(index) + 1);
            }
            if(futureOptions.size() > 0) {
                for(String fopt: futureOptions) {
                    allPermutations.add(seq+fopt);
                }
            }
            else {
                allPermutations.add(seq);
            }
        }
        return allPermutations;
    }

    private String makeRegex(String unmodifiedSequence) {
        
        if(equivalentAAPatterns == null) {
            equivalentAAPatterns = new ArrayList<Pattern>();
            List<String> eqAA = parser.getEquivalentResidues();
            for(String a: eqAA) {
                Pattern p = Pattern.compile("["+a+"]");
                equivalentAAPatterns.add(p);
            }
        }
        
        String peptideRegex = unmodifiedSequence;
        
        for(Pattern p: equivalentAAPatterns) {
            peptideRegex = p.matcher(peptideRegex).replaceAll(p.pattern());
        }
        return "^"+peptideRegex+"$";
    }

    private boolean hasEquivalentAminoAcids(String unmodifiedSequence) {
        
        if(parser.getEquivalentResidues() == null || parser.getEquivalentResidues().size() == 0)
            return false;
        
        if(this.equivalentAAPattern == null) {
            StringBuilder buf = new StringBuilder(".*[");
            List<String> eqAA = parser.getEquivalentResidues();
            for(String a: eqAA)
                buf.append(a);
            buf.append("]+.*");
            this.equivalentAAPattern = Pattern.compile(buf.toString());
        }
        
        return equivalentAAPattern.matcher(unmodifiedSequence).matches();
    }

    private int getNrseqProteinId(String accession, int nrseqDatabaseId) {
        NrDbProtein protein = NrSeqLookupUtil.getDbProtein(nrseqDatabaseId, accession);
        if(protein != null)
            return protein.getProteinId();
        else
            return 0;
    }

    private int addProteinInferenceRun(InteractProtXmlParser parser) throws UploadException {
        
        int uploadedPinferId = 0;
        
        List<String> inputFiles = parser.getInputFiles();
        
        if(inputFiles.size() == 0) {
            UploadException e = new UploadException(ERROR_CODE.GENERAL);
            e.appendErrorMessage("No input(pepXML) files found for ProteinProphet run");
            throw e; 
        }
        
        boolean first = true;
        for(String inputPepXml: inputFiles) {
            PeptideProphetAnalysisUploadDAO pprophAnalysisDao = UploadDAOFactory.getInstance().getPeptideProphetAnalysisDAO();
            String fileName = new File(inputPepXml).getName();
            PeptideProphetAnalysis analysis = pprophAnalysisDao.loadAnalysisForFileName(fileName, this.searchId);
            if(analysis == null) {
                UploadException e = new UploadException(ERROR_CODE.GENERAL);
                e.appendErrorMessage("No matching PeptideProphet analysis found for input file: "+fileName+" and searchID: "+searchId);
                throw e;
            }
            
            if(first) {
                ProteinProphetRun run = new ProteinProphetRun();
                run.setInputGenerator(analysis.getAnalysisProgram());
                run.setProgram(ProteinInferenceProgram.PROTEIN_PROPHET);
                run.setProgramVersion(parser.getProgramVersion());
                if(parser.getDate() != null)
                    run.setDate(new java.sql.Date(parser.getDate().getTime()));
                run.setFilename(parser.getFileName());
                uploadedPinferId = ppRunDao.saveProteinProphetRun(run);
                first = false;
            }
            
            MsRunSearchAnalysisDAO rsaDao = daoFactory.getMsRunSearchAnalysisDAO();
            this.runSearchAnalysisIds = rsaDao.getRunSearchAnalysisIdsForAnalysis(analysis.getId());
            
            ProteinferInputDAO inputDao = ProteinferDAOFactory.instance().getProteinferInputDao();
            try {
                for(int rsaId: runSearchAnalysisIds) {
                    ProteinferInput input = new ProteinferInput();
                    input.setInputId(rsaId);
//                    input.setInputType(InputType.ANALYSIS);
                    input.setProteinferId(uploadedPinferId);
                    inputDao.saveProteinferInput(input);
                }
            }
            catch(RuntimeException ex) {
                UploadException e = new UploadException(ERROR_CODE.GENERAL, ex);
                e.appendErrorMessage("Error saving ProteinProphet input.");
                throw e;
            }
        }
        
        // save the parameters
        List<ProteinProphetParam> params = parser.getParams();
        ProteinProphetParamDAO paramDao = ProteinferDAOFactory.instance().getProteinProphetParamDao();
        try {
            for(ProteinProphetParam param: params) {
                param.setProteinferId(uploadedPinferId);
                paramDao.saveProteinProphetParam(param);
            }
        }
        catch(RuntimeException ex) {
            UploadException e = new UploadException(ERROR_CODE.GENERAL, ex);
            e.appendErrorMessage("Error saving ProteinProphet params.");
            throw e;
        }
        
        
        // save the ROC points
        ProteinProphetRocDAO rocDao = piDaoFactory.getProteinProphetRocDao();
        try {
            ProteinProphetROC roc = parser.getProteinProphetRoc();
            roc.setProteinferId(uploadedPinferId);
            rocDao.saveRoc(roc);
        }
        catch(RuntimeException e) {
            UploadException ex = new UploadException(ERROR_CODE.GENERAL, e);
            ex.appendErrorMessage("Error saving ProteinProphet ROC points.");
            throw ex;
        }
        return uploadedPinferId;
    }


    @Override
    public void setAnalysisId(int analysisId) {
        throw new UnsupportedOperationException("ProxmlDataUploadService determines the analysis ID based "+ 
                "on source_files attribute in pep.mxl files");
//        this.analysisId = analysisId;
    }

    @Override
    public void setSearchId(int searchId) {
        this.searchId = searchId;
    }

    @Override
    public String getPreUploadCheckMsg() {
        return preUploadCheckMsg.toString();
    }

    @Override
    public String getUploadSummary() {
        return "\tProtein inference file format: "+SearchFileFormat.PROTXML+
        "\n\t"+uploadMsg.toString();
    }

    @Override
    public boolean preUploadCheckPassed() {
        
        preUploadCheckMsg = new StringBuilder();
        
        // 1. valid data directory
        File dir = new File(protxmlDirectory);
        if(!dir.exists()) {
            appendToMsg("Data directory does not exist: "+protxmlDirectory);
            return false;
        }
        if(!dir.isDirectory()) {
            appendToMsg(protxmlDirectory+" is not a directory");
            return false;
        }
        
        // 2. Look for interact*.prot.xml file
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String name_lc = name.toLowerCase();
                return name_lc.endsWith(".prot.xml");
            }});
        
        boolean found = false;
        for (int i = 0; i < files.length; i++) {
            if (fileNamePattern.matcher(files[i].getName().toLowerCase()).matches()) {
                protXmlFiles.add(files[i].getName());
                found = true;
            }
        }
        if(!found) {
            appendToMsg("Could not find interact*.prot.xml file(s) in directory: "+protxmlDirectory);
            return false;
        }
        
        
        preUploadCheckDone = true;
        
        return true;
    }
    private void appendToMsg(String msg) {
        this.preUploadCheckMsg.append(msg+"\n");
    }

    @Override
    public void setDirectory(String directory) {
        this.protxmlDirectory = directory;
    }

    @Override
    public void setRemoteDirectory(String remoteDirectory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRemoteServer(String remoteServer) {
        throw new UnsupportedOperationException();
    }
    
    public static void main(String[] args) throws UploadException {
        ProtxmlDataUploadService p = new ProtxmlDataUploadService();
//        p.setDirectory("/Users/silmaril/Desktop/18mix_new");
        p.setDirectory("/Users/silmaril/WORK/UW/FLINT/Jimmy_Test");
//        p.setAnalysisId(30);
        p.setSearchId(37);
        p.upload();
        System.out.println(p.getUploadSummary());
    }
}
