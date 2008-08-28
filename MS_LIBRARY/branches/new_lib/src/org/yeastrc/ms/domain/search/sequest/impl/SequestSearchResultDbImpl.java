package org.yeastrc.ms.domain.search.sequest.impl;

import java.math.BigDecimal;

import org.yeastrc.ms.domain.search.impl.MsSearchResultDbImpl;
import org.yeastrc.ms.domain.search.sequest.SequestResultData;
import org.yeastrc.ms.domain.search.sequest.SequestSearchResultDb;

public class SequestSearchResultDbImpl extends MsSearchResultDbImpl implements SequestSearchResultDb, SequestResultData {

    private SequestResultDataImpl sequestData;
    
    public SequestSearchResultDbImpl() {
        sequestData = new SequestResultDataImpl();
    }
    
    /**
     * @return the xCorrRank
     */
    public int getxCorrRank() {
        return sequestData.getxCorrRank();
    }
    /**
     * @param xcorrRank the xCorrRank to set
     */
    public void setxCorrRank(int xcorrRank) {
        sequestData.setXcorrRank(xcorrRank);
    }
    /**
     * @return the spRank
     */
    public int getSpRank() {
        return sequestData.getSpRank();
    }
    /**
     * @param spRank the spRank to set
     */
    public void setSpRank(int spRank) {
        sequestData.setSpRank(spRank);
    }
    /**
     * @return the deltaCN
     */
    public BigDecimal getDeltaCN() {
        return sequestData.getDeltaCN();
    }
    /**
     * @param deltaCN the deltaCN to set
     */
    public void setDeltaCN(BigDecimal deltaCN) {
        sequestData.setDeltaCN(deltaCN);
    }
    /**
     * @return the xCorr
     */
    public BigDecimal getxCorr() {
        return sequestData.getxCorr();
    }
    /**
     * @param xcorr the xCorr to set
     */
    public void setxCorr(BigDecimal xcorr) {
        sequestData.setXcorr(xcorr);
    }
    /**
     * @return the sp
     */
    public BigDecimal getSp() {
        return sequestData.getSp();
    }
    /**
     * @param sp the sp to set
     */
    public void setSp(BigDecimal sp) {
        sequestData.setSp(sp);
    }
    
    public Double getEvalue() {
        return sequestData.getEvalue();
    }
    
    public void setEvalue(Double evalue) {
        sequestData.setEvalue(evalue);
    }

    @Override
    public BigDecimal getCalculatedMass() {
        return sequestData.getCalculatedMass();
    }

    public void setCalculatedMass(BigDecimal calculatedMass) {
        sequestData.setCalculatedMass(calculatedMass);
    }
    
    @Override
    public int getMatchingIons() {
        return sequestData.getMatchingIons();
    }

    public void setMatchingIons(int matchingIons) {
        sequestData.setMatchingIons(matchingIons);
    }
    
    @Override
    public int getPredictedIons() {
        return sequestData.getPredictedIons();
    }
    
    public void setPredictedIons(int predictedIons) {
        sequestData.setPredictedIons(predictedIons);
    }

    @Override
    public SequestResultData getSequestResultData() {
        return sequestData;
    }
}
