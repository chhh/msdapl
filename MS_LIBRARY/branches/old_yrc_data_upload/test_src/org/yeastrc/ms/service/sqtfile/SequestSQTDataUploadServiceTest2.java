package org.yeastrc.ms.service.sqtfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.yeastrc.ms.dao.BaseDAOTestCase;
import org.yeastrc.ms.dao.DAOFactory;
import org.yeastrc.ms.dao.search.sequest.SequestSearchDAO;
import org.yeastrc.ms.dao.search.sequest.SequestSearchResultDAO;
import org.yeastrc.ms.dao.search.sqtfile.SQTRunSearchDAO;
import org.yeastrc.ms.dao.search.sqtfile.SQTSearchScanDAO;
import org.yeastrc.ms.domain.general.MsEnzyme;
import org.yeastrc.ms.domain.general.MsEnzyme.Sense;
import org.yeastrc.ms.domain.search.MsResidueModification;
import org.yeastrc.ms.domain.search.MsResidueModificationIn;
import org.yeastrc.ms.domain.search.MsResultResidueMod;
import org.yeastrc.ms.domain.search.MsResultTerminalMod;
import org.yeastrc.ms.domain.search.MsSearchDatabase;
import org.yeastrc.ms.domain.search.MsSearchResultPeptide;
import org.yeastrc.ms.domain.search.MsSearchResultProtein;
import org.yeastrc.ms.domain.search.MsTerminalModification;
import org.yeastrc.ms.domain.search.MsTerminalModificationIn;
import org.yeastrc.ms.domain.search.Param;
import org.yeastrc.ms.domain.search.Program;
import org.yeastrc.ms.domain.search.SearchFileFormat;
import org.yeastrc.ms.domain.search.ValidationStatus;
import org.yeastrc.ms.domain.search.MsTerminalModification.Terminal;
import org.yeastrc.ms.domain.search.sequest.SequestResultData;
import org.yeastrc.ms.domain.search.sequest.SequestSearch;
import org.yeastrc.ms.domain.search.sequest.SequestSearchResult;
import org.yeastrc.ms.domain.search.sqtfile.SQTHeaderItem;
import org.yeastrc.ms.domain.search.sqtfile.SQTRunSearch;
import org.yeastrc.ms.domain.search.sqtfile.SQTSearchScan;
import org.yeastrc.ms.parser.DataProviderException;
import org.yeastrc.ms.service.MsDataUploader;

public class SequestSQTDataUploadServiceTest2 extends BaseDAOTestCase {

    private static final SequestSearchDAO ssearchDao = DAOFactory.instance().getSequestSearchDAO();
    private static final SQTRunSearchDAO sqtRunSearchDao = DAOFactory.instance().getSqtRunSearchDAO();
    private static final SQTSearchScanDAO sqtScanDao = DAOFactory.instance().getSqtSpectrumDAO();
    private static final SequestSearchResultDAO sresDao = DAOFactory.instance().getSequestResultDAO();

    protected void setUp() throws Exception {
        super.setUp();
      resetDatabase();

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testUploadValidSequestData2() throws DataProviderException {
        String dir = "test_resources/validSequestData_dir2";

        MsDataUploader uploader = new MsDataUploader();
        int experimentId = 0;
        java.util.Date searchDate = new java.util.Date();
        uploader.setSpectrumDataDirectory(dir);
        uploader.setSearchDirectory(dir);
        uploader.setSearchDate(searchDate);
        uploader.setRemoteServer("remoteServer");
        uploader.setRemoteSearchDataDirectory("remoteDirectory");
        uploader.uploadData();
        experimentId = uploader.getUploadedExperimentId();
        assertEquals(0, uploader.getUploadExceptionList().size());
        
        assertEquals(0, uploader.getUploadExceptionList().size());
        assertNotSame(0, experimentId);
        checkUploadedSearch(uploader.getUploadedSearchId(), searchDate, dir);
    }
    
    private void checkUploadedSearch(int searchId, java.util.Date searchDate, String dir) {

        // make sure all the data got uploaded
        int runId1 = getRunId("1");
        int runId2 = getRunId("2");
        assertNotSame(0, runId1);
        assertNotSame(0, runId2);

        checkSearch(searchId, searchDate, dir);
//      checkSearchForFile1(searchId, runId1); // 1.ms2
        int runSearchId = checkSearchForFile2(searchId, runId2); // 2.ms2
        checkSearchResults2(runSearchId, runId2);
        checkSearchScan2(runSearchId, runId2);
    }

    private void checkSearchScan2(int runSearchId, int runId) {
        // S       00023   00023   3       22      shamu048        866.46000       1892.2  56.4    4716510
        int scanId = scanDao.loadScanIdForScanNumRun(23, runId);
        SQTSearchScan scan = sqtScanDao.load(runSearchId, scanId, 3, new BigDecimal("866.46000"));
        assertEquals(scanId, scan.getScanId());
        assertEquals(runSearchId, scan.getRunSearchId());
        assertEquals(3, scan.getCharge());
        assertEquals(22, scan.getProcessTime());
        assertEquals("shamu048", scan.getServerName());
//        assertEquals(866.46, scan.getObservedMass().doubleValue());
        assertEquals(1892.2, scan.getTotalIntensity().doubleValue());
        assertEquals(56.4, scan.getLowestSp().doubleValue());
        assertEquals(4716510, scan.getSequenceMatches());
        // S       00020   00020   1       22      shamu049        807.67000       2681.7  95.3    5138490
        scanId = scanDao.loadScanIdForScanNumRun(20, runId);
        scan = sqtScanDao.load(runSearchId, scanId, 1, new BigDecimal("807.67000"));
        assertEquals(scanId, scan.getScanId());
        assertEquals(runSearchId, scan.getRunSearchId());
        assertEquals(1, scan.getCharge());
        assertEquals(22, scan.getProcessTime());
        assertEquals("shamu049", scan.getServerName());
//        assertEquals(807.67000, scan.getObservedMass().doubleValue());
        assertEquals(2681.7, scan.getTotalIntensity().doubleValue());
        assertEquals(95.3, scan.getLowestSp().doubleValue());
        assertEquals(5138490, scan.getSequenceMatches());
        // S       00010   00010   1       23      shamu050        717.62000       4000.6  111.6   5928764
        scanId = scanDao.loadScanIdForScanNumRun(10, runId);
        scan = sqtScanDao.load(runSearchId, scanId, 1, new BigDecimal("717.62000"));
        assertEquals(scanId, scan.getScanId());
        assertEquals(runSearchId, scan.getRunSearchId());
        assertEquals(1, scan.getCharge());
        assertEquals(23, scan.getProcessTime());
        assertEquals("shamu050", scan.getServerName());
//        assertEquals(717.62000, scan.getObservedMass().doubleValue());
        assertEquals(4000.6, scan.getTotalIntensity().doubleValue());
        assertEquals(111.6, scan.getLowestSp().doubleValue());
        assertEquals(5928764, scan.getSequenceMatches());
        // S       00026   00026   1       23      shamu048        817.33000       2044.4  69.6    5697304
        scanId = scanDao.loadScanIdForScanNumRun(26, runId);
        scan = sqtScanDao.load(runSearchId, scanId, 1, new BigDecimal("817.33000"));
        assertEquals(scanId, scan.getScanId());
        assertEquals(runSearchId, scan.getRunSearchId());
        assertEquals(1, scan.getCharge());
        assertEquals(23, scan.getProcessTime());
        assertEquals("shamu048", scan.getServerName());
//        assertEquals(817.33, scan.getObservedMass().doubleValue());
        assertEquals(2044.4, scan.getTotalIntensity().doubleValue());
        assertEquals(69.6, scan.getLowestSp().doubleValue());
        assertEquals(5697304, scan.getSequenceMatches());
    }

    private void checkSearchResults2(int runSearchId, int runId) {

        List<Integer> ids = sresDao.loadResultIdsForRunSearch(runSearchId);
        assertEquals(13, ids.size());
        Collections.sort(ids);
        // S       00023   00023   3       22      shamu048        866.46000       1892.2  56.4    4716510
        // M         1       4      866.96470123      0.00000  1.1529   3.137   9     14          L.S*DMSASRI.T   U
        SequestSearchResult res = sresDao.load(ids.get(0));
        assertEquals(getScanId(runId, 23), res.getScanId());
        assertEquals(3, res.getCharge());
        assertEquals(866.46, res.getObservedMass().doubleValue());
        assertEquals("SDMSASRI", res.getResultPeptide().getPeptideSequence());
        assertEquals('L', res.getResultPeptide().getPreResidue());
        assertEquals('T', res.getResultPeptide().getPostResidue());
        assertEquals(ValidationStatus.UNVALIDATED, res.getValidationStatus());
        // check the residue modifications for this result
        MsSearchResultPeptide peptide = res.getResultPeptide();
        List<MsResultResidueMod> resultMods = peptide.getResultDynamicResidueModifications();
        assertEquals(1, resultMods.size());
        assertEquals(0, resultMods.get(0).getModifiedPosition());
        assertEquals('*', resultMods.get(0).getModificationSymbol());
        assertEquals('S', resultMods.get(0).getModifiedResidue());
        assertEquals(79.9876, resultMods.get(0).getModificationMass().doubleValue());
        // check the terminal modifications for this result
        List<MsResultTerminalMod> termMods = peptide.getResultDynamicTerminalModifications();
        assertEquals(0, termMods.size());
        // check the Sequest specific results
        SequestResultData data = res.getSequestResultData();
        assertEquals(1, data.getxCorrRank());
        assertEquals(4, data.getSpRank());
        assertEquals(866.96470123, data.getCalculatedMass().doubleValue());
        assertEquals(0.0, data.getDeltaCN().doubleValue());
        assertEquals(1.1529, data.getxCorr().doubleValue());
        assertEquals(3.137, data.getSp().doubleValue());
        assertEquals(9, data.getMatchingIons());
        assertEquals(14, data.getPredictedIons());
        // check the protein matches for this result
//      28 L       Reverse_gi|927415|emb|CAA55359.1|
//      29 L       Reverse_gi|21618336|ref|NP_659006.1|
        List<MsSearchResultProtein> prList = res.getProteinMatchList();
        Collections.sort(prList, new MatchProteinComparator());
        assertEquals(2, prList.size());
        MsSearchResultProtein pr = prList.get(0);
        assertEquals(ids.get(0).intValue(), pr.getResultId());
        assertEquals("Reverse_gi|21618336|ref|NP_659006.1|", pr.getAccession());
        pr = prList.get(1);
        assertEquals(ids.get(0).intValue(), pr.getResultId());
        assertEquals("Reverse_gi|927415|emb|CAA55359.1|", pr.getAccession());


        //  M         2     200      865.91874      0.0311   1.117    2.953   8     16        T.S*GTS*SAS*LR.K    V
        res = sresDao.load(ids.get(1));
        assertEquals(getScanId(runId, 23), res.getScanId());
        assertEquals(3, res.getCharge());
        assertEquals("SGTSSASLR", res.getResultPeptide().getPeptideSequence());
        assertEquals('T', res.getResultPeptide().getPreResidue());
        assertEquals('K', res.getResultPeptide().getPostResidue());
        assertEquals(ValidationStatus.VALID, res.getValidationStatus());
        // check the residue modifications for this result
        peptide = res.getResultPeptide();
        resultMods = peptide.getResultDynamicResidueModifications();
        Collections.sort(resultMods, new ResultResidueModComparator<MsResultResidueMod>());
        assertEquals(3, resultMods.size());
        MsResultResidueMod rmod = resultMods.get(0);
        assertEquals(0, rmod.getModifiedPosition());
        assertEquals('*', rmod.getModificationSymbol());
        assertEquals('S', rmod.getModifiedResidue());
        assertEquals(79.9876, rmod.getModificationMass().doubleValue());
        rmod = resultMods.get(1);
        assertEquals(3, rmod.getModifiedPosition());
        assertEquals('*', rmod.getModificationSymbol());
        assertEquals('S', rmod.getModifiedResidue());
        assertEquals(79.9876, rmod.getModificationMass().doubleValue());
        rmod = resultMods.get(2);
        assertEquals(6, rmod.getModifiedPosition());
        assertEquals('*', rmod.getModificationSymbol());
        assertEquals('S', rmod.getModifiedResidue());
        assertEquals(79.9876, rmod.getModificationMass().doubleValue());
        // check the terminal modifications for this result
        termMods = peptide.getResultDynamicTerminalModifications();
        assertEquals(0, termMods.size());
        // check the Sequest specific results
        data = res.getSequestResultData();
        assertEquals(2, data.getxCorrRank());
        assertEquals(200, data.getSpRank());
        assertEquals(865.91874, data.getCalculatedMass().doubleValue());
        assertEquals(0.0311, data.getDeltaCN().doubleValue());
        assertEquals(1.117, data.getxCorr().doubleValue());
        assertEquals(2.953, data.getSp().doubleValue());
        assertEquals(8, data.getMatchingIons());
        assertEquals(16, data.getPredictedIons());


        // M         4      22      866.99266      0.0989   1.0388   2.551   8     14               A.SG#IY*ASRL.S   N
        res = sresDao.load(ids.get(3));
        assertEquals(getScanId(runId, 23), res.getScanId());
        assertEquals(3, res.getCharge());
        assertEquals("SGIYASRL", res.getResultPeptide().getPeptideSequence());
        assertEquals('A', res.getResultPeptide().getPreResidue());
        assertEquals('S', res.getResultPeptide().getPostResidue());
        assertEquals(ValidationStatus.NOT_VALID, res.getValidationStatus());
        // check the residue modifications for this result
        peptide = res.getResultPeptide();
        resultMods = peptide.getResultDynamicResidueModifications();
        Collections.sort(resultMods, new ResultResidueModComparator<MsResultResidueMod>());
        assertEquals(2, resultMods.size());
        rmod = resultMods.get(0);
        assertEquals(1, rmod.getModifiedPosition());
        assertEquals('#', resultMods.get(0).getModificationSymbol());
        assertEquals('G', resultMods.get(0).getModifiedResidue());
        assertEquals(-99.9, resultMods.get(0).getModificationMass().doubleValue());
        rmod = resultMods.get(1);
        assertEquals(3, rmod.getModifiedPosition());
        assertEquals('*', rmod.getModificationSymbol());
        assertEquals('Y', rmod.getModifiedResidue());
        assertEquals(79.9876, rmod.getModificationMass().doubleValue());
        // check the terminal modifications for this result
        termMods = peptide.getResultDynamicTerminalModifications();
        assertEquals(0, termMods.size());
        // check the ProLuCID specific results
        data = res.getSequestResultData();
        assertEquals(4, data.getxCorrRank());
        assertEquals(22, data.getSpRank());
        assertEquals(866.99266, data.getCalculatedMass().doubleValue());
        assertEquals(0.0989, data.getDeltaCN().doubleValue());
        assertEquals(1.0388, data.getxCorr().doubleValue());
        assertEquals(2.551, data.getSp().doubleValue());
        assertEquals(8, data.getMatchingIons());
        assertEquals(14, data.getPredictedIons());
        // check the protein matches for this result
        // 36 L       gi|113427084|ref|XP_001128380.1|
        prList = res.getProteinMatchList();
        assertEquals(1, prList.size());
        pr = prList.get(0);
        assertEquals(ids.get(3).intValue(), pr.getResultId());
        assertEquals("gi|113427084|ref|XP_001128380.1|", pr.getAccession());
        
        
        // 60 S       00026   00026   1       23      shamu048        817.33000       2044.4  69.6    5697304
        // 61 M         1      22      816.80570      0.00000  1.5492   3.795  11     24                  D.AGGGAGGGGAGAG#.Q  M
        // 62 L       gi|3090887|gb|AAC15421.1|
        res = sresDao.load(ids.get(10));
        assertNotSame(0, res.getScanId());
        assertEquals(getScanId(runId, 26), res.getScanId());
        assertEquals(1, res.getCharge());
        assertEquals("AGGGAGGGGAGAG", res.getResultPeptide().getPeptideSequence());
        assertEquals('D', res.getResultPeptide().getPreResidue());
        assertEquals('Q', res.getResultPeptide().getPostResidue());
        assertEquals(ValidationStatus.MAYBE, res.getValidationStatus());
        // check the residue modifications for this result
        peptide = res.getResultPeptide();
        resultMods = peptide.getResultDynamicResidueModifications();
        assertEquals(1, resultMods.size());
        rmod = resultMods.get(0);
        assertEquals(12, rmod.getModifiedPosition());
        assertEquals('#', resultMods.get(0).getModificationSymbol());
        assertEquals('G', resultMods.get(0).getModifiedResidue());
        assertEquals(-99.9, resultMods.get(0).getModificationMass().doubleValue());
        // check the terminal modifications for this result
        termMods = peptide.getResultDynamicTerminalModifications();
        assertEquals(0, termMods.size());
        // check the Sequest specific results
        data = res.getSequestResultData();
        assertEquals(1, data.getxCorrRank());
        assertEquals(22, data.getSpRank());
        assertEquals(816.80570, data.getCalculatedMass().doubleValue());
        assertEquals(0.0, data.getDeltaCN().doubleValue());
        assertEquals(1.5492, data.getxCorr().doubleValue());
        assertEquals(3.795, data.getSp().doubleValue());
        assertEquals(11, data.getMatchingIons());
        assertEquals(24, data.getPredictedIons());
        // check the protein matches for this result
        // 62 L       gi|3090887|gb|AAC15421.1|
        prList = res.getProteinMatchList();
        assertEquals(1, prList.size());
        pr = prList.get(0);
        assertEquals(ids.get(10).intValue(), pr.getResultId());
        assertEquals("gi|3090887|gb|AAC15421.1|", pr.getAccession());
    }

    private int getScanId(int runId, int scanNumber) {
        return scanDao.loadScanIdForScanNumRun(scanNumber, runId);
    }

    private int checkSearchForFile2(int searchId, int runId) {
        int runSearchId = sqtRunSearchDao.loadIdForRunAndSearch(runId, searchId);
        SQTRunSearch runSearch = sqtRunSearchDao.loadRunSearch(runSearchId);
        assertEquals(runId, runSearch.getRunId());
        assertEquals(searchId, runSearch.getSearchId());
        assertEquals(SearchFileFormat.SQT_SEQ, runSearch.getSearchFileFormat());
        assertEquals("2008-01-29", runSearch.getSearchDate().toString());
        assertEquals(167, runSearch.getSearchDuration());
        assertEquals(Program.SEQUEST, runSearch.getSearchProgram());

        // check headers
        checkRunSearchHeaders2(runSearch);

        return runSearchId;
    }

    private void checkRunSearchHeaders2(SQTRunSearch runSearch) {
        List<SQTHeaderItem> headers = runSearch.getHeaders();
        assertEquals(23, headers.size());
    }

    private void checkSearch(int searchId, java.util.Date experimentDate, String dir) {
        SequestSearch search = ssearchDao.loadSearch(searchId);
//      assertEquals(experimentDate, search.getSearchDate());
        assertEquals("remoteDirectory", search.getServerDirectory());
        assertEquals(Program.SEQUEST, search.getSearchProgram());
        assertEquals("3.0", search.getSearchProgramVersion());

        // check the database
        checkSearchDatabase(search);

        // check the enzyme
        checkEnzyme(search);

        // check static residue modifications
        checkStaticResidueMods(search);

        // check dynamic residue modifications
        checkDynamicResidueMods(search);

        // check static terminal modifications
        checkStaticTerminalMods(search);

        // check dynamic terminal modifications
        checkDynamicTerminalMods(search);
        
        // check the parameters
//        try {
//            this.checkUploadedParams(search, dir);
//        }
//        catch (IOException e) {
//            fail("Error checking uploaded params");
//        }

    }

    private void checkUploadedParams(SequestSearch search, String dir) throws IOException {
        List<Param> params = search.getSequestParams();
        
        StringBuilder fromDb = new StringBuilder();
        for (Param p: params) {
            fromDb.append("\n"+p.getParamName()+"\t"+p.getParamValue());
        }
        fromDb.deleteCharAt(0);
        
        // read the file from directory to compare against
        String origFile = dir+File.separator+"sequest.params.fortest";
        StringBuilder buf = new StringBuilder();
        BufferedReader r = null;
        r = new BufferedReader(new FileReader(origFile));
        String line = r.readLine();
        while (line != null) {
            buf.append("\n"+line.trim());
            line = r.readLine();
        }
        r.close();
        buf.deleteCharAt(0);
        
        assertEquals(fromDb.length(), buf.toString().length());
        String[] fromDbLines = fromDb.toString().split("\\n");
        String[] origLines = buf.toString().split("\\n");
        assertEquals(fromDbLines.length, origLines.length);
        for (int i = 0; i < fromDbLines.length; i++) {
            assertEquals(fromDbLines[i], origLines[i]);
        }
        assertEquals(fromDb.toString(), buf.toString());
    }
  
    private void checkSearchDatabase(SequestSearch search) {
        List<MsSearchDatabase> dbs = search.getSearchDatabases();
        assertEquals(1, dbs.size());
        MsSearchDatabase db = dbs.get(0);
        assertEquals("/net/maccoss/vol2/software/pipeline/dbase/mouse-contam.fasta", db.getServerPath());
        assertEquals("remoteServer", db.getServerAddress());
        assertEquals(3, db.getSequenceDatabaseId());
        assertEquals("mouse-contam.fasta", db.getDatabaseFileName());
    }

    private void checkEnzyme(SequestSearch search) {
        List<MsEnzyme> enzymes = search.getEnzymeList();
        assertEquals(1, enzymes.size());
        MsEnzyme en = enzymes.get(0);
        assertEquals("Trypsin", en.getName());
        assertEquals(Sense.CTERM, en.getSense());
        assertEquals("KR", en.getCut());
        assertEquals("P", en.getNocut());
        assertEquals(null, en.getDescription());
    }

    private void checkStaticResidueMods(SequestSearch search) {
        List<MsResidueModification> mods = search.getStaticResidueMods();
        assertEquals(1, mods.size());

        MsResidueModification mod = mods.get(0);
        assertEquals('C', mod.getModifiedResidue());
        assertEquals(0, mod.getModificationSymbol());
        assertEquals(57.02146, mod.getModificationMass().doubleValue());
    }

    private void checkDynamicResidueMods(SequestSearch search) {
        List<MsResidueModification> mods = search.getDynamicResidueMods();
        assertEquals(6, mods.size());
        Collections.sort(mods, new ResidueModComparator<MsResidueModification>());
        int i = 0;
        MsResidueModification mod = mods.get(i++);
        assertEquals('D', mod.getModifiedResidue());
        assertEquals('#', mod.getModificationSymbol());
        assertEquals(-99.9, mod.getModificationMass().doubleValue());
        mod = mods.get(i++);
        assertEquals('G', mod.getModifiedResidue());
        assertEquals('#', mod.getModificationSymbol());
        assertEquals(-99.9, mod.getModificationMass().doubleValue());
        mod = mods.get(i++);
        assertEquals('S', mod.getModifiedResidue());
        assertEquals('*', mod.getModificationSymbol());
        assertEquals(79.9876, mod.getModificationMass().doubleValue());
        mod = mods.get(i++);
        assertEquals('T', mod.getModifiedResidue());
        assertEquals('*', mod.getModificationSymbol());
        assertEquals(79.9876, mod.getModificationMass().doubleValue());
        mod = mods.get(i++);
        assertEquals('V', mod.getModifiedResidue());
        assertEquals('#', mod.getModificationSymbol());
        assertEquals(-99.9, mod.getModificationMass().doubleValue());
        mod = mods.get(i++);
        assertEquals('Y', mod.getModifiedResidue());
        assertEquals('*', mod.getModificationSymbol());
        assertEquals(79.9876, mod.getModificationMass().doubleValue());
    }

    private void checkStaticTerminalMods(SequestSearch search) {
        List<MsTerminalModification> mods = search.getStaticTerminalMods();
        assertEquals(2, mods.size());
        Collections.sort(mods, new TerminalModComparator<MsTerminalModification>());
        int i = 0; 
        MsTerminalModification mod = mods.get(i++);
        assertEquals(Terminal.CTERM, mod.getModifiedTerminal());
        assertEquals(0, mod.getModificationSymbol());
        assertEquals(-10.0, mod.getModificationMass().doubleValue());

        mod = mods.get(i++);
        assertEquals(Terminal.NTERM, mod.getModifiedTerminal());
        assertEquals(0, mod.getModificationSymbol());
        assertEquals(987.654, mod.getModificationMass().doubleValue());
    }

    private void checkDynamicTerminalMods(SequestSearch search) {
        List<MsTerminalModification> mods = search.getDynamicTerminalMods();
        assertEquals(0, mods.size());
    }

    private class ResidueModComparator <T extends MsResidueModificationIn> implements Comparator<T> {
        public int compare(T o1, T o2) {
            return Character.valueOf(o1.getModifiedResidue()).compareTo(Character.valueOf(o2.getModifiedResidue()));
        }
    }

    private class TerminalModComparator <T extends MsTerminalModificationIn> implements Comparator<T> {
        public int compare(T o1, T o2) {
            return o1.getModificationMass().compareTo(o2.getModificationMass());
        }
    }

    private class ResultResidueModComparator <T extends MsResultResidueMod> implements Comparator<T> {
        public int compare(T o1, T o2) {
            return Integer.valueOf(o1.getModifiedPosition()).compareTo(Integer.valueOf(o2.getModifiedPosition()));
        }
    }

    private class MatchProteinComparator implements Comparator<MsSearchResultProtein> {
        public int compare(MsSearchResultProtein o1, MsSearchResultProtein o2) {
            return o1.getAccession().compareTo(o2.getAccession());
        }
    }

    private int getRunId(String runFileName) {
        List<Integer> runIds = runDao.loadRunIdsForFileName(runFileName);
        assertEquals(1, runIds.size());
        int runId = runIds.get(0);
        assertNotSame(0, runId);
        return runId;
    }
}
