/**
 * 
 */
package org.yeastrc.www.proteinfer.job;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

/**
 * RerunProteinInferenceForm.java
 * @author Vagisha Sharma
 * Jun 7, 2010
 * 
 */
public class RerunProteinInferenceForm extends ActionForm {

	private boolean deleteOriginal = false;
    private FormFile inputFile;
    
    
    /**
     * Validate the properties that have been sent from the HTTP request,
     * and return an ActionErrors object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * an empty ActionErrors object.
     */
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        
        return errors;
    }

	public boolean isDeleteOriginal() {
		return deleteOriginal;
	}

	public void setDeleteOriginal(boolean deleteOriginal) {
		this.deleteOriginal = deleteOriginal;
	}

	public FormFile getInputFile() {
		return inputFile;
	}

	public void setInputFile(FormFile inputFile) {
		this.inputFile = inputFile;
	}
}
