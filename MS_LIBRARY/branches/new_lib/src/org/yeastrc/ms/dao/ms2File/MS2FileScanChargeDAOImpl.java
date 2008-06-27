/**
 * MsScanChargeDAO.java
 * @author Vagisha Sharma
 * Jun 17, 2008
 * @version 1.0
 */
package org.yeastrc.ms.dao.ms2File;

import java.util.List;

import org.yeastrc.ms.dao.BaseSqlMapDAO;
import org.yeastrc.ms.dto.ms2File.Ms2FileScanCharge;

import com.ibatis.sqlmap.client.SqlMapClient;

public class MS2FileScanChargeDAOImpl extends BaseSqlMapDAO implements MS2FileScanChargeDAO {

    public MS2FileScanChargeDAOImpl(SqlMapClient sqlMap) {
        super(sqlMap);
    }

    public int save(Ms2FileScanCharge scanCharge) {
        return saveAndReturnId("scanCharge.insert", scanCharge);
    }
    
    public Ms2FileScanCharge load(int scanChargeId) {
        return (Ms2FileScanCharge) queryForObject("scanCharge.select", scanChargeId);
    }
    
    public List<Ms2FileScanCharge> loadChargesForScan(int scanId) {
        return queryForList("scanCharge.selectChargesForScan", scanId);
    }
}
