/**
 * ProteinProphetProteinDAO.java
 * @author Vagisha Sharma
 * Jul 29, 2009
 * @version 1.0
 */
package org.yeastrc.ms.dao.protinfer.proteinProphet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.yeastrc.ms.dao.ibatis.BaseSqlMapDAO;
import org.yeastrc.ms.dao.protinfer.GenericProteinferProteinDAO;
import org.yeastrc.ms.dao.protinfer.ibatis.ProteinAndGroupId;
import org.yeastrc.ms.dao.protinfer.ibatis.ProteinferProteinDAO;
import org.yeastrc.ms.domain.protinfer.GenericProteinferProtein;
import org.yeastrc.ms.domain.protinfer.PeptideDefinition;
import org.yeastrc.ms.domain.protinfer.ProteinUserValidation;
import org.yeastrc.ms.domain.protinfer.ProteinferProtein;
import org.yeastrc.ms.domain.protinfer.SORT_BY;
import org.yeastrc.ms.domain.protinfer.proteinProphet.ProteinProphetFilterCriteria;
import org.yeastrc.ms.domain.protinfer.proteinProphet.ProteinProphetProtein;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 * 
 */
public class ProteinProphetProteinDAO extends BaseSqlMapDAO
    implements GenericProteinferProteinDAO<ProteinProphetProtein> {

    
    private static final String sqlMapNameSpace = "ProteinProphetProtein";
    
    private final ProteinferProteinDAO protDao;
    
    public ProteinProphetProteinDAO(SqlMapClient sqlMap, ProteinferProteinDAO protDao) {
        super(sqlMap);
        this.protDao = protDao;
    }

    @Override
    public int save(GenericProteinferProtein<?> protein) {
        return protDao.save(protein);
    }
    
    public int saveProteinProphetProtein(ProteinProphetProtein protein) {
        int proteinId = save(protein);
        protein.setId(proteinId);
        save(sqlMapNameSpace+".insert", protein); // save entry in the ProteinProphetProtein table
        return proteinId;
    }

    @Override
    public int update(GenericProteinferProtein<?> protein) {
        return protDao.update(protein);
    }
    
    @Override
    public void saveProteinferProteinPeptideMatch(int pinferProteinId,
            int pinferPeptideId) {
        protDao.saveProteinferProteinPeptideMatch(pinferProteinId, pinferPeptideId);
    }

    @Override
    public void updateUserAnnotation(int pinferProteinId, String annotation) {
        protDao.updateUserAnnotation(pinferProteinId, annotation);
    }

    @Override
    public void updateUserValidation(int pinferProteinId,
            ProteinUserValidation validation) {
        protDao.updateUserValidation(pinferProteinId, validation);
    }
    
    @Override
    public void delete(int pinferProteinId) {
        protDao.delete(pinferProteinId);
    }

    @Override
    public List<Integer> getNrseqIdsForRun(int proteinferId) {
        return protDao.getNrseqIdsForRun(proteinferId);
    }

    public  List<Integer> getNrseqProteinIds(int pinferId, boolean isParsimonious) {
        if(isParsimonious)
            return getNrseqProteinIds(pinferId, isParsimonious, false); // return only parsimonious
        else
            return getNrseqIdsForRun(pinferId); // return parsimonious and non-parsimonious
    }
    
    public List<Integer> getNrseqProteinIds(int pinferId, boolean parsimonious, boolean nonParsimonious) {
        Map<String, Number> map = new HashMap<String, Number>(4);
        map.put("pinferId", pinferId);
        if(parsimonious && !nonParsimonious)            map.put("isSubsumed", 0);
        else if(!parsimonious && nonParsimonious)       map.put("isSubsumed", 1);
        return queryForList(sqlMapNameSpace+".proteinProphetNrseqProteinIds", map);
    }
    
    @Override
    public int getPeptideCountForProtein(int nrseqId, List<Integer> pinferIds) {
        // TODO may need to override -- 
        // 1. check if any of these is a ProteinProphet run
        // 2. only return peptides that have at least one ion as "contributing_evidence"
        return protDao.getPeptideCountForProtein(nrseqId, pinferIds);
    }

    @Override
    public List<String> getPeptidesForProtein(int nrseqId,
            List<Integer> pinferIds) {
        // TODO may need to override -- 
        // 1. check if any of these is a ProteinProphet run
        // 2. only return peptides that have at least one ion as "contributing_evidence"
        return protDao.getPeptidesForProtein(nrseqId, pinferIds);
    }

    @Override
    public List<Integer> getProteinsForPeptide(int pinferId, String peptide, boolean exactMatch) {
        return protDao.getProteinsForPeptide(pinferId, peptide, exactMatch); 
    }
    
    @Override
    public int getProteinCount(int proteinferId) {
        return protDao.getProteinCount(proteinferId);
    }

    @Override
    public List<Integer> getProteinIdsForNrseqIds(int proteinferId,
            ArrayList<Integer> nrseqIds) {
        return protDao.getProteinIdsForNrseqIds(proteinferId, nrseqIds);
    }

    public boolean isNrseqProteinGrouped(int pinferId, int nrseqId) {
        ProteinferProtein protein = this.loadProtein(pinferId, nrseqId);
        return isProteinGrouped(protein.getId());
    }
    
    public boolean isProteinGrouped(int pinferProteinId) {
        ProteinProphetProtein ppProt = this.loadProtein(pinferProteinId);
        int groupId = ppProt.getGroupId();
        return (this.getProteinProphetIndistinguishableGroupProteinIds(ppProt.getProteinferId(), groupId).size() > 1);
    }
        
        
    @Override
    public List<Integer> getProteinferProteinIds(int proteinferId) {
        return protDao.getProteinferProteinIds(proteinferId);
    }

    public List<Integer> getProteinferProteinIds(int pinferId, boolean isParsimonious) {
        
        Map<String, Number> map = new HashMap<String, Number>(4);
        map.put("pinferId", pinferId);
        if(isParsimonious)          map.put("isSubsumed", 0);
        return queryForList(sqlMapNameSpace+".proteinProphetProteinIds", map);
    }
    
    public  int getIndistinguishableGroupCount(int pinferId, boolean parsimonious) {
        
        Map<String, Integer> map = new HashMap<String, Integer>(2);
        map.put("pinferId", pinferId);
        if(parsimonious) {
            map.put("isSubsumed", 0);
        }
        return (Integer)queryForObject(sqlMapNameSpace+".selectGroupCount", map);
    }
    
    @Override
    public ProteinProphetProtein loadProtein(int pinferProteinId) {
        return (ProteinProphetProtein) super.queryForObject(sqlMapNameSpace+".select", pinferProteinId);
    }
    
    @Override
    public ProteinferProtein loadProtein(int proteinferId, int nrseqProteinId) {
        return protDao.loadProtein(proteinferId, nrseqProteinId);
    }

    @Override
    public List<ProteinProphetProtein> loadProteins(int proteinferId) {
        return super.queryForList(sqlMapNameSpace+".selectProteinsForProteinferRun", proteinferId);
    }
    
    
    public List<Integer> getFilteredSortedProteinIds(int pinferId, ProteinProphetFilterCriteria filterCriteria) {
        
        // Get a list of protein ids filtered by sequence coverage
        boolean sort = filterCriteria.getSortBy() == SORT_BY.COVERAGE;
        List<Integer> ids_cov = proteinIdsByCoverage(pinferId, 
                filterCriteria.getCoverage(), filterCriteria.getMaxCoverage(),
                sort, filterCriteria.isGroupProteins());
        
        // Get a list of protein ids filtered by spectrum count
        sort = filterCriteria.getSortBy() == SORT_BY.NUM_SPECTRA;
        List<Integer> ids_spec_count = proteinIdsBySpectrumCount(pinferId, 
                filterCriteria.getNumSpectra(), filterCriteria.getNumMaxSpectra(),
                sort, filterCriteria.isGroupProteins());
        
        // Get a list of protein ids filtered by peptide count
        sort = filterCriteria.getSortBy() == SORT_BY.NUM_PEPT;
        List<Integer> ids_pept = proteinIdsByAllPeptideCount(pinferId, 
                                            filterCriteria.getNumPeptides(), filterCriteria.getNumMaxPeptides(),
                                            filterCriteria.getPeptideDefinition(),
                                            sort, 
                                            filterCriteria.isGroupProteins(), 
                                            filterCriteria.parsimoniousOnly()
                                            );
        
        // Get a list of protein ids filtered by UNIQUE peptide count
        List<Integer> ids_uniq_pept = null;
        if(filterCriteria.getNumUniquePeptides() == 0  &&
           filterCriteria.getNumMaxUniquePeptides() == Integer.MAX_VALUE
           && filterCriteria.getSortBy() != SORT_BY.NUM_UNIQ_PEPT) {
            ids_uniq_pept = ids_pept;
        }
        else {
            sort = filterCriteria.getSortBy() == SORT_BY.NUM_UNIQ_PEPT;
            ids_uniq_pept = proteinIdsByUniquePeptideCount(pinferId, 
                                               filterCriteria.getNumUniquePeptides(),
                                               filterCriteria.getNumMaxUniquePeptides(),
                                               filterCriteria.getPeptideDefinition(),
                                               sort,
                                               filterCriteria.isGroupProteins(),
                                               filterCriteria.parsimoniousOnly());
        }
        
        
        // If the user is filtering on validation status 
        List<Integer> ids_validation_status = null;
        sort = filterCriteria.getSortBy() == SORT_BY.VALIDATION_STATUS;
        if(filterCriteria.getValidationStatus().size() > 0 || sort) {
            ids_validation_status = proteinIdsByValidationStatus(pinferId, filterCriteria.getValidationStatus(),
                                                                 sort);
        }
        
        // If the user is filtering on protein group probability
        List<Integer> ids_probability_grp = null;
        sort = filterCriteria.getSortBy() == SORT_BY.PROBABILITY_GRP;
        ids_probability_grp = proteinIdsByProbability(pinferId, 
                filterCriteria.getMinGroupProbability(), filterCriteria.getMaxGroupProbability(),
                sort, true); // filter proteins by ProteinProphet group probability; 
                             // If sort is true proteins will be grouped
        
        // If the user is filtering on protein  probability
        List<Integer> ids_probability_prot = null;
        sort = filterCriteria.getSortBy() == SORT_BY.PROBABILITY_PROT;
        ids_probability_prot = proteinIdsByProbability(pinferId, 
                filterCriteria.getMinProteinProbability(), filterCriteria.getMaxProteinProbability(),
                sort, false);   // filter proteins by ProteinProphet protein probability
                                // If sort is true no grouping by ProteinProphet groupID,
                                // group only by indistinct group ID
        
        
        // If the user if filtering on peptide charge states
        List<Integer> ids_chargeStates = null;
        if(filterCriteria.getChargeStates().size() > 0 || filterCriteria.getChargeGreaterThan() != -1) {
            ids_chargeStates = proteinIdsByPeptideChargeState(pinferId, filterCriteria.getChargeStates(),
                    filterCriteria.getChargeGreaterThan());
        }
        
        // If the user wants to exclude indistinguishable protein groups get the protein IDs that are not
        // in a indistinguishable protein group.
        List<Integer> notInGroup = null;
        if(filterCriteria.isExcludeIndistinGroups()) {
            notInGroup = proteinsNotInGroup(pinferId);
        }
        
        // If the user is only interested in proteins with a certain peptide
        List<Integer> peptideMatches = null;
        if(filterCriteria.getPeptide() != null) {
            peptideMatches = this.getProteinsForPeptide(pinferId, filterCriteria.getPeptide(), filterCriteria.getExactPeptideMatch());
        }
        
        // get the set of common ids; keep the order of ids returned from the query
        // that returned sorted results
        if(filterCriteria.getSortBy() == SORT_BY.COVERAGE) {
            Set<Integer> others = combineLists(ids_spec_count, ids_pept, ids_uniq_pept, ids_validation_status, 
                    ids_probability_grp, ids_probability_prot, ids_chargeStates, notInGroup, peptideMatches);
            return getCommonIds(ids_cov, others);
        }
        else if (filterCriteria.getSortBy() == SORT_BY.NUM_SPECTRA) {
            Set<Integer> others = combineLists(ids_cov, ids_pept, ids_uniq_pept, ids_validation_status, 
                    ids_probability_grp, ids_probability_prot, ids_chargeStates, notInGroup, peptideMatches);
            return getCommonIds(ids_spec_count, others);
        }
        else if (filterCriteria.getSortBy() == SORT_BY.NUM_PEPT) {
            Set<Integer> others = combineLists(ids_spec_count, ids_cov, ids_uniq_pept, ids_validation_status, 
                    ids_probability_grp, ids_probability_prot, ids_chargeStates, notInGroup, peptideMatches);
            return getCommonIds(ids_pept, others);
        }
        else if (filterCriteria.getSortBy() == SORT_BY.NUM_UNIQ_PEPT) {
            Set<Integer> others = combineLists(ids_spec_count, ids_pept, ids_cov, ids_validation_status, 
                    ids_probability_grp, ids_probability_prot, ids_chargeStates, notInGroup, peptideMatches);
            return getCommonIds(ids_uniq_pept, others);
        }
        else if (filterCriteria.getSortBy() == SORT_BY.PROTEIN_PROPHET_GROUP) {
            Set<Integer> others = combineLists(ids_spec_count, ids_pept, ids_cov, ids_uniq_pept, ids_validation_status, 
                    ids_probability_grp, ids_probability_prot, ids_chargeStates, notInGroup, peptideMatches);
            List<Integer> idsbyGroup = sortProteinIdsByProteinProphetGroup(pinferId);
            return getCommonIds(idsbyGroup, others);
        }
        else if(filterCriteria.getSortBy() == SORT_BY.VALIDATION_STATUS) {
            Set<Integer> others = combineLists(ids_spec_count, ids_pept, ids_cov, ids_uniq_pept, 
                    ids_probability_grp, ids_probability_prot, ids_chargeStates, notInGroup, peptideMatches);
            return getCommonIds(ids_validation_status, others);
        }
        else if(filterCriteria.getSortBy() == SORT_BY.PROBABILITY_GRP) {
            Set<Integer> others = combineLists(ids_spec_count, ids_pept, ids_cov, ids_uniq_pept, ids_validation_status,
                    ids_probability_prot, ids_chargeStates, notInGroup, peptideMatches);
            return getCommonIds(ids_probability_grp, others);
        }
        else if(filterCriteria.getSortBy() == SORT_BY.PROBABILITY_PROT) {
            Set<Integer> others = combineLists(ids_spec_count, ids_pept, ids_cov, ids_uniq_pept, ids_validation_status,
                    ids_probability_grp, ids_chargeStates, notInGroup, peptideMatches);
            return getCommonIds(ids_probability_prot, others);
        }
        else {
            Set<Integer> combineLists = combineLists(ids_cov, ids_spec_count, ids_pept, ids_uniq_pept, ids_validation_status, 
                    ids_probability_grp, ids_probability_prot, ids_chargeStates, notInGroup, peptideMatches);
            return new ArrayList<Integer>(combineLists);
        }
    }
    
    public List<Integer> proteinsNotInGroup(int pinferId) {
        return queryForList(sqlMapNameSpace+".proteinsNotInGroup", pinferId);
    }
    
    // -----------------------------------------------------------------------------------
    // FILTER NRSEQ PROTEIN IDS 
    // -----------------------------------------------------------------------------------
    public List<Integer> getFilteredNrseqIds(int pinferId, ProteinProphetFilterCriteria filterCriteria) {
        
        // Get a list of protein ids filtered by sequence coverage
        List<Integer> ids_cov = nrseqIdsByCoverage(pinferId, 
                filterCriteria.getCoverage(), filterCriteria.getMaxCoverage());
        
        // Get a list of protein ids filtered by spectrum count
        List<Integer> ids_spec_count = nrseqIdsBySpectrumCount(pinferId, 
                filterCriteria.getNumSpectra(), filterCriteria.getNumMaxSpectra());
        
        // Get a list of protein ids filtered by peptide count
        List<Integer> ids_pept = nrseqIdsByAllPeptideCount(pinferId, 
                                            filterCriteria.getNumPeptides(), filterCriteria.getNumMaxPeptides(),
                                            filterCriteria.getPeptideDefinition(),
                                            filterCriteria.parsimoniousOnly()
                                            );
        
        // Get a list of protein ids filtered by UNIQUE peptide count
        List<Integer> ids_uniq_pept = null;
        if(filterCriteria.getNumUniquePeptides() == 0  &&
           filterCriteria.getNumMaxUniquePeptides() == Integer.MAX_VALUE) {
            ids_uniq_pept = ids_pept;
        }
        else {
            ids_uniq_pept = nrseqIdsByAllPeptideCount(pinferId, 
                                               filterCriteria.getNumUniquePeptides(),
                                               filterCriteria.getNumMaxUniquePeptides(),
                                               filterCriteria.getPeptideDefinition(),
                                               filterCriteria.parsimoniousOnly());
        }
        
        
        // If the user is filtering on validation status 
        List<Integer> ids_validation_status = null;
        if(filterCriteria.getValidationStatus().size() > 0) {
            ids_validation_status = nrseqIdsByValidationStatus(pinferId, filterCriteria.getValidationStatus());
        }
        
        // If the user is filtering on protein group probability
        List<Integer> ids_probability_grp = null;
        ids_probability_grp = nrseqIdsByProbability(pinferId, 
                filterCriteria.getMinGroupProbability(), filterCriteria.getMaxGroupProbability(), 
                true); // this will filter by ProteinProphet group probability
        
        
        // If the user is filtering on protein probability
        List<Integer> ids_probability_prot = null;
        ids_probability_prot = nrseqIdsByProbability(pinferId, 
                filterCriteria.getMinProteinProbability(), filterCriteria.getMaxProteinProbability(), 
                false); // this will filter by ProteinProphet group probability
        
        // get the set of common ids; 
        Set<Integer> combineLists = combineLists(ids_cov, ids_spec_count, 
                ids_pept, ids_uniq_pept, ids_validation_status, 
                ids_probability_grp, ids_probability_prot);
        
        return new ArrayList<Integer>(combineLists);
    }
    
    
    /**
     * List is sorted by the proteinProphetGrouID and groupID (indistinguishable group ID). 
     * @param pinferId
     * @return
     */
    public List<Integer> sortProteinIdsByProteinProphetGroup(int pinferId) {
        return queryForList(sqlMapNameSpace+".proteinIdsByProteinProphetGroupId", pinferId);
    }
    
    // -----------------------------------------------------------------------------------------------
    // PROBABILITY
    // -----------------------------------------------------------------------------------------------
    public List<Integer> sortProteinIdsByProbability(int pinferId, boolean groupProteins) {
        return proteinIdsByProbability(pinferId, 0.0, 1.0, true, groupProteins);
    }
    
    private List<Integer> proteinIdsByProbability(int pinferId, 
            double minProbability, double maxProbability,
            boolean sort, boolean useGrpProbability) {
        
        // If we are NOT filtering anything AND NOT sorting on probability just return all the protein Ids
        // for this protein inference run
        if(minProbability == 0.0 && maxProbability == 1.0 && !sort) {
            return getProteinferProteinIds(pinferId, false);
        }
        
        Map<String, Number> map = new HashMap<String, Number>(10);
        map.put("pinferId", pinferId);
        map.put("minProbability", minProbability);
        map.put("maxProbability", maxProbability);
        if(sort)    map.put("sort", 1);
        
        // if proteins are being grouped by protein prophet group filter on group probability
        if(useGrpProbability)
            return queryForList(sqlMapNameSpace+".filterByProteinGroupProbability", map);
        // otherwise filter on individual protein probability
        // proteins in an indistinguishable protein group will have the same probability
        else
            return queryForList(sqlMapNameSpace+".filterByProteinProbability", map);
       
    }
    
    private List<Integer> nrseqIdsByProbability(int pinferId, 
            double minProbability, double maxProbability, boolean useGroupProbability) {
        
        // If we are NOT filtering anything AND NOT sorting on spectrum count just return all the protein Ids
        // for this protein inference run
        if(minProbability == 0.0 && maxProbability == 1.0) {
            return getNrseqIdsForRun(pinferId);
        }
        
        Map<String, Number> map = new HashMap<String, Number>(10);
        map.put("pinferId", pinferId);
        map.put("minProbability", minProbability);
        map.put("maxProbability", maxProbability);
        
        // if proteins are being grouped by protein prophet group filter on group probability
        if(useGroupProbability)
            return queryForList(sqlMapNameSpace+".filterNrseqIdsByProteinGroupProbability", map);
        // otherwise filter on individual protein probability
        // proteins in an indistinguishable protein group will have the same probability
        else
            return queryForList(sqlMapNameSpace+".filterNrseqIdsByProteinProbability", map);
       
    }
    
    // -----------------------------------------------------------------------------------------------
    // COVERAGE
    // -----------------------------------------------------------------------------------------------
    public List<Integer> sortProteinIdsByCoverage(int pinferId, boolean groupProteins) {
        return proteinIdsByCoverage(pinferId, 0.0, 100.0, true, groupProteins);
    }
    
    private List<Integer> proteinIdsByCoverage(int pinferId, 
            double minCoverage, double maxCoverage,
            boolean sort, boolean groupProteins) {
        
        Map<String, Number> map = new HashMap<String, Number>(10);
        map.put("pinferId", pinferId);
        map.put("minCoverage", minCoverage);
        map.put("maxCoverage", maxCoverage);
        if(sort)    map.put("sort", 1);
        
        // If we are not sorting on coverage do a simple query and return a list of protien ids that 
        // satisfy the coverage criteria.
        if(!sort) {
            return queryForList(sqlMapNameSpace+".filterByCoverage", map);
        }
        // If we are sorting on coverage we have two cases:
        // 1. We are grouping on protein prophet group and then indistinguishable group
        // 2. We are grouping only on indistinguishable group
        else { 
            // group proteins and sort
            // List of protein IDs along with their proteinProphetGroupID, groupID and coverage (sorted by 
            // protein prophet group, then coverage).
            List<ProteinGroupCoverage> prGrC = queryForList(sqlMapNameSpace+".filterProteinGroupCoverage", map);
            
            // Case 1: grouping by protein prophet group and then indistinguishable group.
            if(groupProteins)
                return getProteinIdsGroupedByPhrophetGroupAndIGroup(prGrC);
            // Case 2: grouping by indistinguishable group only
            else
                return getProteinIdsGroupedByIGroup(prGrC);
        }
    }
    
    private List<Integer> getProteinIdsGroupedByPhrophetGroupAndIGroup(List<ProteinGroupCoverage> prGrC) {
        int lastProphetGrp = -1;
        double lastMaxCoverage = 0.0;
        // Set the coverage for each protein in a group to be the max coverage in the group.
        for(ProteinGroupCoverage pgc: prGrC) {
            if(pgc.proteinProphetGroupId != lastProphetGrp) {
                pgc.maxGrpCoverage = pgc.coverage;
                lastProphetGrp = pgc.proteinProphetGroupId;
                lastMaxCoverage = pgc.coverage; // first protein (for a group) has the max coverage.
            }
            else {
                pgc.maxGrpCoverage = lastMaxCoverage;
            }
        }
        // Sort on coverage then protein prophet group, then indistinguishable group.
        Collections.sort(prGrC, new Comparator<ProteinGroupCoverage>() {
            @Override
            public int compare(ProteinGroupCoverage o1, ProteinGroupCoverage o2) {
                int val = Double.valueOf(o1.maxGrpCoverage).compareTo(o2.maxGrpCoverage);
                if(val != 0)    return val;
                val = Integer.valueOf(o1.proteinProphetGroupId).compareTo(o2.proteinProphetGroupId);
                if(val != 0)    return val;
                val = Integer.valueOf(o1.proteinGroupId).compareTo(o2.proteinGroupId);
                if(val != 0)    return val;
                return Double.valueOf(o1.coverage).compareTo(o2.coverage);
            }});
        List<Integer> proteinIds = new ArrayList<Integer>(prGrC.size());
        
        // return the list of protein IDs in the sorted order above.
        for(ProteinGroupCoverage pgc: prGrC)
            proteinIds.add(pgc.proteinId);
        return proteinIds;
    }
    
    private List<Integer> getProteinIdsGroupedByIGroup(List<ProteinGroupCoverage> prGrC) {
        int lastIGrp = -1;
        double lastMaxCoverage = 0.0;
        // Set the coverage for each protein in a group to be the max coverage in the indistinguishable group.
        for(ProteinGroupCoverage pgc: prGrC) {
            if(pgc.proteinGroupId != lastIGrp) {
                pgc.maxGrpCoverage = pgc.coverage;
                lastIGrp = pgc.proteinProphetGroupId;
                lastMaxCoverage = pgc.coverage; // first protein (for a group) has the max coverage.
            }
            else {
                pgc.maxGrpCoverage = lastMaxCoverage;
            }
        }
        // Sort on coverage, then indistinguishable group.
        Collections.sort(prGrC, new Comparator<ProteinGroupCoverage>() {
            @Override
            public int compare(ProteinGroupCoverage o1, ProteinGroupCoverage o2) {
                int val = Double.valueOf(o1.maxGrpCoverage).compareTo(o2.maxGrpCoverage);
                if(val != 0)    return val;
                val = Integer.valueOf(o1.proteinGroupId).compareTo(o2.proteinGroupId);
                if(val != 0)    return val;
                return Double.valueOf(o1.coverage).compareTo(o2.coverage);
            }});
        List<Integer> proteinIds = new ArrayList<Integer>(prGrC.size());
        
        // return the list of protein IDs in the sorted order above.
        for(ProteinGroupCoverage pgc: prGrC)
            proteinIds.add(pgc.proteinId);
        return proteinIds;
    }
    
    public static class ProteinGroupCoverage {
        private int proteinId;
        private int proteinGroupId;
        private int proteinProphetGroupId;
        private double coverage;
        private double maxGrpCoverage;
        public void setProteinId(int proteinId) {
            this.proteinId = proteinId;
        }
        public void setProteinGroupId(int proteinGroupId) {
            this.proteinGroupId = proteinGroupId;
        }
        public void setProteinProphetGroupId(int proteinProphetGroupId) {
            this.proteinProphetGroupId = proteinProphetGroupId;
        }
        public void setCoverage(double coverage) {
            this.coverage = coverage;
        }
    }
    
    private List<Integer> nrseqIdsByCoverage(int pinferId, 
            double minCoverage, double maxCoverage) {
        
        if(minCoverage == 0 && maxCoverage == 100.0) {
            return getNrseqIdsForRun(pinferId);
        }
        Map<String, Number> map = new HashMap<String, Number>(10);
        map.put("pinferId", pinferId);
        map.put("minCoverage", minCoverage);
        map.put("maxCoverage", maxCoverage);
        
        return queryForList(sqlMapNameSpace+".filterNrseqIdsByCoverage", map);
    }
    
    
    // -----------------------------------------------------------------------------------------------
    // SPECTRUM COUNT
    // -----------------------------------------------------------------------------------------------
    public List<Integer> sortProteinIdsBySpectrumCount(int pinferId, boolean groupProteins) {
        return proteinIdsBySpectrumCount(pinferId, 1, Integer.MAX_VALUE, true, groupProteins);
    }
    
    private List<Integer> nrseqIdsBySpectrumCount(int pinferId, int minSpecCount, int maxSpecCount) {
        
        // If we are NOT filtering anything just return all the protein Ids
        // for this protein inference run
        if(minSpecCount <= 1 && maxSpecCount == Integer.MAX_VALUE) {
            return getNrseqIdsForRun(pinferId); // get both parsimonious and non-parsimonious
        }
        
        Map<String, Number> map = new HashMap<String, Number>(10);
        map.put("pinferId", pinferId);
        map.put("minSpectra", minSpecCount);
        map.put("maxSpectra", maxSpecCount);
        return queryForList(sqlMapNameSpace+".filterNrseqIdsBySpecCount", map);
    }

    private List<Integer> proteinIdsBySpectrumCount(int pinferId, int minSpecCount, int maxSpecCount,
            boolean sort, boolean groupProteins) {
        
        // If we are NOT filtering anything AND NOT sorting on spectrum count just return all the protein Ids
        // for this protein inference run
        if(minSpecCount <= 1 && maxSpecCount == Integer.MAX_VALUE && !sort) {
            return getProteinferProteinIds(pinferId, false);
        }
        
        Map<String, Number> map = new HashMap<String, Number>(10);
        map.put("pinferId", pinferId);
        map.put("minSpectra", minSpecCount);
        map.put("maxSpectra", maxSpecCount);
        if(sort)                    map.put("sort", 1);
        if(sort && groupProteins)   map.put("groupProteins", 1);
        return queryForList(sqlMapNameSpace+".filterBySpecCount", map);
    }
    
    // -----------------------------------------------------------------------------------
    // PEPTIDE CHARGE STATE
    // -----------------------------------------------------------------------------------
    private List<Integer> proteinIdsByPeptideChargeState(int pinferId,
            List<Integer> chargeStates, int chargeGreaterThan) {
        Map<String, Object> map = new HashMap<String, Object>(6);
        map.put("pinferId", pinferId);
        if(chargeStates != null && chargeStates.size() > 0) {
            String cs = "";
            for(Integer c: chargeStates)
                cs += ","+c;
            if(cs.length() > 0) cs = cs.substring(1); // remove first comma
            cs = "("+cs+")";
            map.put("chargeStates", cs);
        }
        if(chargeGreaterThan != -1) {
            map.put("chargeGreaterThan", chargeGreaterThan);
        }
        return queryForList(sqlMapNameSpace+".filterByChargeStates", map);
    }
    
    // -----------------------------------------------------------------------------------------------
    // PEPTIDE COUNT
    // -----------------------------------------------------------------------------------------------
    public List<Integer> sortProteinIdsByPeptideCount(int pinferId, PeptideDefinition peptideDef, boolean groupProteins) {
        return proteinIdsByPeptideCount(pinferId, 1, Integer.MAX_VALUE, peptideDef, true, groupProteins, false, false);
    }
    
    public List<Integer> sortProteinIdsByUniquePeptideCount(int pinferId, PeptideDefinition peptideDef, boolean groupProteins) {
        return proteinIdsByPeptideCount(pinferId, 0, Integer.MAX_VALUE, peptideDef, true, groupProteins, false, true);
    }
    
    private List<Integer> proteinIdsByUniquePeptideCount(int pinferId, 
            int minUniqPeptideCount, int maxUniqPeptideCount, 
            PeptideDefinition peptDef,
            boolean sort, boolean groupProteins, boolean isParsimonious) {
        return proteinIdsByPeptideCount(pinferId, 
                minUniqPeptideCount, maxUniqPeptideCount, 
                peptDef, sort, groupProteins, isParsimonious, true);
    }
    
    private List<Integer> proteinIdsByAllPeptideCount(int pinferId, 
            int minUniqPeptideCount, int maxUniqPeptideCount,
            PeptideDefinition peptDef,
            boolean sort, boolean groupProteins, boolean isParsimonious) {
        return proteinIdsByPeptideCount(pinferId, 
                minUniqPeptideCount, maxUniqPeptideCount,
                peptDef, sort, groupProteins, isParsimonious, false);
    }
    
    private List<Integer> nrseqIdsByUniquePeptideCount(int pinferId, 
            int minUniqPeptideCount, int maxUniqPeptideCount, 
            PeptideDefinition peptDef,boolean isParsimonious) {
        return nrseqIdsByPeptideCount(pinferId, 
                minUniqPeptideCount, maxUniqPeptideCount, 
                peptDef, isParsimonious, true);
    }
    
    private List<Integer> nrseqIdsByAllPeptideCount(int pinferId, 
            int minUniqPeptideCount, int maxUniqPeptideCount,
            PeptideDefinition peptDef, boolean isParsimonious) {
        return nrseqIdsByPeptideCount(pinferId, 
                minUniqPeptideCount, maxUniqPeptideCount,
                peptDef, isParsimonious, false);
    }
    
    private List<Integer> proteinIdsByPeptideCount(int pinferId, int minPeptideCount, int maxPeptideCount,
            PeptideDefinition peptDef,
            boolean sort, boolean groupProteins, boolean isParsimonious, boolean uniqueToProtein) {
        
        // If we are NOT filtering anything AND NOT sorting on peptide count just return all the protein Ids
        // for this protein inference run
        if(!uniqueToProtein) {
            if(minPeptideCount <= 1 && maxPeptideCount == Integer.MAX_VALUE && !sort) {
                return getProteinferProteinIds(pinferId, isParsimonious);
            }
        }
        if(uniqueToProtein) {
            if(minPeptideCount <= 0 && maxPeptideCount == Integer.MAX_VALUE && !sort) {
                return getProteinferProteinIds(pinferId, isParsimonious);
            }
        }
        
        Map<String, Number> map = new HashMap<String, Number>(12);
        map.put("pinferId", pinferId);
        map.put("minPeptides", minPeptideCount);
        map.put("maxPeptides", maxPeptideCount);
        if(uniqueToProtein) map.put("uniqueToProtein", 1);
        if(isParsimonious)          map.put("isSubsumed", 0);
        if(sort)                    map.put("sort", 1);
        if(sort && groupProteins)   map.put("groupProteins", 1);
        
        List<Integer> peptideIds = null;
        // peptide uniquely defined by sequence, mods and charge (this is the only option
        // used by ProteinProphet)
        if(peptDef.isUseCharge() && peptDef.isUseMods()) {
            peptideIds = queryForList(sqlMapNameSpace+".filterByPeptideCount_SMC", map);
        }
        
        return peptideIds;
    }
    
    private List<Integer> nrseqIdsByPeptideCount(int pinferId, int minPeptideCount, int maxPeptideCount,
            PeptideDefinition peptDef, boolean isParsimonious, boolean uniqueToProtein) {
        
        // If we are NOT filtering anything just return all the protein Ids
        // for this protein inference run
        if(!uniqueToProtein) {
            if(minPeptideCount <= 1 && maxPeptideCount == Integer.MAX_VALUE) {
                return getNrseqProteinIds(pinferId, isParsimonious);
            }
        }
        else {
            if(minPeptideCount <= 0 && maxPeptideCount == Integer.MAX_VALUE) {
                return getNrseqProteinIds(pinferId, isParsimonious);
            }
        }
        
        Map<String, Number> map = new HashMap<String, Number>(12);
        map.put("pinferId", pinferId);
        map.put("minPeptides", minPeptideCount);
        map.put("maxPeptides", maxPeptideCount);
        if(uniqueToProtein) map.put("uniqueToProtein", 1);
        if(isParsimonious)          map.put("isSubsumed", 0);
        
        
        List<Integer> proteinIds = null;
        // peptide uniquely defined by sequence, mods and charge (this is the only option
        // used by ProteinProphet)
        if(peptDef.isUseCharge() && peptDef.isUseMods()) {
            proteinIds = queryForList(sqlMapNameSpace+".filterNrseqIdsByPeptideCount_SMC", map);
        }
        
        return proteinIds;
    }
    
    
    // -----------------------------------------------------------------------------------------------
    // VALIDATION STATUS
    // -----------------------------------------------------------------------------------------------
    public List<Integer> sortProteinIdsByValidationStatus(int pinferId) {
        return proteinIdsByValidationStatus(pinferId, new ArrayList<ProteinUserValidation>(0), true);
    }
    
    private List<Integer> proteinIdsByValidationStatus(int pinferId,
            List<ProteinUserValidation> validationStatus, boolean sort) {
        Map<String, Object> map = new HashMap<String, Object>(6);
        map.put("pinferId", pinferId);
        if(validationStatus != null && validationStatus.size() > 0) {
            String vs = "";
            for(ProteinUserValidation v: validationStatus)
                vs += ",\'"+v.getStatusChar()+"\'";
            if(vs.length() > 0) vs = vs.substring(1); // remove first comma
            vs = "("+vs+")";
            map.put("validationStatus", vs);
        }
        if(sort)    map.put("sort", 1);
        return queryForList(sqlMapNameSpace+".filterByValidationStatus", map);
    }
    
    private List<Integer> nrseqIdsByValidationStatus(int pinferId,
            List<ProteinUserValidation> validationStatus) {
        Map<String, Object> map = new HashMap<String, Object>(6);
        map.put("pinferId", pinferId);
        if(validationStatus != null && validationStatus.size() > 0) {
            String vs = "";
            for(ProteinUserValidation v: validationStatus)
                vs += ",\'"+v.getStatusChar()+"\'";
            if(vs.length() > 0) vs = vs.substring(1); // remove first comma
            vs = "("+vs+")";
            map.put("validationStatus", vs);
        }
        return queryForList(sqlMapNameSpace+".filterNrseqIdsByValidationStatus", map);
    }
    
    
    
    
    private final Set<Integer> combineLists(List<Integer>...lists) {
        
        int numValidLists = 0;
        int count = 0;
        for(List<Integer> list: lists) {
            if(list == null)    continue;
            numValidLists++;
            count = Math.min(count, list.size());
        }
        Map<Integer, Integer> idCount = new HashMap<Integer, Integer>((int) (count*1.5));
        for(List<Integer> list: lists) {
            if(list == null)    continue;
            for(int id: list) {
                Integer c = idCount.get(id);
                if(c == null)   idCount.put(id, 1);
                else            idCount.put(id, ++c);
            }
        }
        Set<Integer> set = new HashSet<Integer>((int) (count*1.5));
        for(int id: idCount.keySet()) {
            if(idCount.get(id) == numValidLists)    set.add(id);
        }
        return set;
    }
    
    private final List<Integer> getCommonIds(List<Integer> ordered, Set<Integer> others) {
        Iterator<Integer> iter = ordered.iterator();
        while(iter.hasNext()) {
            Integer id = iter.next();
            if(!others.contains(id))
                iter.remove();
        }
        return ordered;
    }
    
    /**
     * Returns the protein ids for an indistinguishable protein group
     * @param pinferId
     * @param groupId
     * @return
     */
    public List<Integer> getProteinProphetIndistinguishableGroupProteinIds(int pinferId, int groupId) {
        Map<String, Integer> map = new HashMap<String, Integer>(2);
        map.put("pinferId", pinferId);
        map.put("groupId", groupId);
        return queryForList(sqlMapNameSpace+".selectProteinIdsForGroup", map);
    }

    /**
     * Returns the proteins for an indistinguishable protein group
     * @param pinferId
     * @param groupId
     * @return
     */
    public List<ProteinProphetProtein> loadProteinProphetIndistinguishableGroupProteins(
            int pinferId, int groupId) {
        
        Map<String, Integer> map = new HashMap<String, Integer>(2);
        map.put("pinferId", pinferId);
        map.put("groupId", groupId);
        return queryForList(sqlMapNameSpace+".selectProteinsForGroup", map);
    }
    
    /**
     * Returns the protein ids for an indistinguishable protein group
     * @param pinferId
     * @param proteinProphetGroupId
     * @return
     */
    public List<Integer> getProteinProphetGroupProteinIds(int pinferId, int proteinProphetGroupId) {
        Map<String, Integer> map = new HashMap<String, Integer>(2);
        map.put("pinferId", pinferId);
        map.put("proteinProphetGroupId", proteinProphetGroupId);
        return queryForList(sqlMapNameSpace+".selectProteinIdsForProteinProphetGroup", map);
    }

    /**
     * Returns the proteins for an indistinguishable protein group
     * @param pinferId
     * @param proteinProphetGroupId
     * @return
     */
    public List<ProteinProphetProtein> loadProteinProphetGroupProteins(
            int pinferId, int proteinProphetGroupId) {
        
        Map<String, Integer> map = new HashMap<String, Integer>(2);
        map.put("pinferId", pinferId);
        map.put("proteinProphetGroupId", proteinProphetGroupId);
        return queryForList(sqlMapNameSpace+".selectProteinsForProteinProphetGroup", map);
    }
    
    
    /**
     * Returns a map of proteinIds and proteinGroupIds.
     * Keys in the map are proteinIds and Values are proteinGroupIds
     */
    public Map<Integer, Integer> getProteinGroupIds(int pinferId, boolean parsimonious) {
        Map<String, Number> map = new HashMap<String, Number>(8);
        map.put("pinferId", pinferId);
        if(parsimonious)          map.put("isSubsumed", 0);
        List<ProteinAndGroupId> protGrps = queryForList(sqlMapNameSpace+".selectProteinAndGroupIds", map);
        
        Map<Integer, Integer> protGrpmap = new HashMap<Integer, Integer>((int) (protGrps.size() * 1.5));
        for(ProteinAndGroupId pg: protGrps) {
            protGrpmap.put(pg.getProteinId(), pg.getGroupId());
        }
        return protGrpmap;
    }

}
