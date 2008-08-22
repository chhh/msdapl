package org.yeastrc.ms.domain.search.sqtfile;

import java.math.BigDecimal;

public interface SQTSearchScan extends SQTSearchScanBase {

    public abstract int getScanNumber();

}

interface SQTSearchScanBase {
    /**
     * @return the charge
     */
    public abstract int getCharge();

    /**
     * @return the processTime
     */
    public abstract int getProcessTime();

    /**
     * @return the serverName
     */
    public abstract String getServerName();

    /**
     * @return the totalIntensity
     */
    public abstract BigDecimal getTotalIntensity();

    /**
     * @return the observedMass
     */
    public abstract BigDecimal getObservedMass();
    
    /**
     * @return the lowestSp
     */
    public abstract BigDecimal getLowestSp();
    
    /**
     * Returns the number of sequence matching the precursor ion.
     * @return
     */
    public abstract int getSequenceMatches();
}