/* UploadY2HFormAction.java
 * Created on May 13, 2004
 */
package org.yeastrc.www.y2h;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import org.yeastrc.project.ProjectPIComparator;
import org.yeastrc.project.Projects;
import org.yeastrc.project.ProjectsSearcher;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

/**
 * Add one sentence class summary here.
 * Add class description here
 * 
 * @author Michael Riffle <mriffle@alumni.washington.edu>
 * @version 1.0, May 13, 2004
 *
 */
public class UploadY2HFormAction extends Action {

	public ActionForward execute( ActionMapping mapping,
								  ActionForm form,
								  HttpServletRequest request,
								  HttpServletResponse response )
	throws Exception {

		// User making this request
		User user = UserUtils.getUser(request);
		if (user == null) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.login.notloggedin"));
			saveErrors( request, errors );
			return mapping.findForward("authenticate");
		}

		// Restrict access to administrators
		Groups groupMan = Groups.getInstance();
		if (!groupMan.isMember(user.getResearcher().getID(), Projects.TWOHYBRID) &&
		  !groupMan.isMember(user.getResearcher().getID(), "administrators")) {
			ActionErrors errors = new ActionErrors();
			errors.add("access", new ActionMessage("error.access.invalidgroup"));
			saveErrors( request, errors );
			return mapping.findForward("adminHome");
		}

		// Create a list of collaborations and technology dev projects for two hybrid
		ProjectsSearcher ps = new ProjectsSearcher();
		ps.addGroup(Projects.TWOHYBRID);
		ps.addType(Projects.COLLABORATION);
		ps.addType(Projects.TECHNOLOGY);
		
		List projects = ps.search();
		Collections.sort(projects, new ProjectPIComparator());
		
		HttpSession session = request.getSession();
		session.setAttribute("Y2HProjects", projects);
		
		// Kick it to the view page
		return mapping.findForward("Success");

	}

}