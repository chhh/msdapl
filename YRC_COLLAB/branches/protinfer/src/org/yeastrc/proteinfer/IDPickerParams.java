package org.yeastrc.proteinfer;

public class IDPickerParams {

    private float maxAbsoluteFdr = 0.05f;
    private float maxRelativeFdr = 0.05f;
    private float decoyRatio = 1.0f;
    private int minDistinctPeptides = 2;
    private boolean doParsimonyAnalysis = true;
    
    public float getMaxAbsoluteFdr() {
        return maxAbsoluteFdr;
    }
    public void setMaxAbsoluteFdr(float maxAbsoluteFdr) {
        this.maxAbsoluteFdr = maxAbsoluteFdr;
    }
    public float getMaxRelativeFdr() {
        return maxRelativeFdr;
    }
    public void setMaxRelativeFdr(float maxRelativeFdr) {
        this.maxRelativeFdr = maxRelativeFdr;
    }
    public float getDecoyRatio() {
        return decoyRatio;
    }
    public void setDecoyRatio(float decoyRatio) {
        this.decoyRatio = decoyRatio;
    }
    public int getMinDistinctPeptides() {
        return minDistinctPeptides;
    }
    public void setMinDistinctPeptides(int minDistinctPeptides) {
        this.minDistinctPeptides = minDistinctPeptides;
    }
    public boolean getDoParsimonyAnalysis() {
        return doParsimonyAnalysis;
    }
    public void setDoParsimonyAnalysis(boolean doParsimonyAnalysis) {
        this.doParsimonyAnalysis = doParsimonyAnalysis;
    }
    
    
}
