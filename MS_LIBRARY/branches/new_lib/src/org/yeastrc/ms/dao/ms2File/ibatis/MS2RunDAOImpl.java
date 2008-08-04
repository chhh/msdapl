package org.yeastrc.ms.dao.ms2File.ibatis;

import java.util.List;

import org.yeastrc.ms.dao.DAOFactory;
import org.yeastrc.ms.dao.MsRunDAO;
import org.yeastrc.ms.dao.MsScanDAO;
import org.yeastrc.ms.dao.ibatis.BaseSqlMapDAO;
import org.yeastrc.ms.dao.ms2File.MS2HeaderDAO;
import org.yeastrc.ms.domain.MsRun;
import org.yeastrc.ms.domain.MsRunDb;
import org.yeastrc.ms.domain.RunFileFormat;
import org.yeastrc.ms.domain.ms2File.MS2Field;
import org.yeastrc.ms.domain.ms2File.MS2Run;
import org.yeastrc.ms.domain.ms2File.MS2RunDb;
import org.yeastrc.ms.domain.ms2File.MS2Scan;
import org.yeastrc.ms.domain.ms2File.MS2ScanDb;

import com.ibatis.sqlmap.client.SqlMapClient;

public class MS2RunDAOImpl extends BaseSqlMapDAO implements MsRunDAO<MS2Run, MS2RunDb> {

    private MsRunDAO<MsRun, MsRunDb> msRunDao;
    private MS2HeaderDAO ms2HeaderDao;
    private MsScanDAO<MS2Scan, MS2ScanDb> ms2ScanDao;
    
    
    
    public MS2RunDAOImpl(SqlMapClient sqlMap, MsRunDAO<MsRun, MsRunDb> msRunDao,
            MS2HeaderDAO ms2HeaderDao, MsScanDAO<MS2Scan, MS2ScanDb> ms2ScanDao) {
        super(sqlMap);
        this.msRunDao = msRunDao;
        this.ms2HeaderDao = ms2HeaderDao;
        this.ms2ScanDao = ms2ScanDao;
    }

    public RunFileFormat getRunFileFormat(int runId) throws Exception {
        return msRunDao.getRunFileFormat(runId);
    }

    /**
     * Saves the run along with MS2 file specific information
     */
    public int saveRun(MS2Run run, int experimentId) {

        // save the run
        int runId = msRunDao.saveRun(run, experimentId);

        MS2HeaderDAO headerDao = DAOFactory.instance().getMS2FileRunHeadersDAO();
        for (MS2Field header: run.getHeaderList()) {
            headerDao.save(header, runId);
        }
        return runId;
    }


    public MS2RunDb loadRun(int runId) {
        // MsRun.select has a discriminator and will instantiate the
        // appropriate type of run object
        return (MS2RunDb) queryForObject("MsRun.select", runId);
    }

    public List<MS2RunDb> loadExperimentRuns(int msExperimentId) {
        return queryForList("MsRun.selectRunsForExperiment", msExperimentId);
    }

    @Override
    public int loadRunIdForExperimentAndFileName(int experimentId, String fileName) {
      return msRunDao.loadRunIdForExperimentAndFileName(experimentId, fileName);
    }
    
    public List<Integer> runIdsFor(String fileName, String sha1Sum) {

        return msRunDao.runIdsFor(fileName, sha1Sum);
    }

    public void delete(int runId) {
        msRunDao.delete(runId);
    }
}
