/**
 * 
 */
package org.yeastrc.www.yates;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.yeastrc.db.DBConnectionManager;

/**
 * @author Mike
 *
 */
public class MSUploadJobSaver {

	
	public void savetoDatabase() throws Exception {
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			
			conn = DBConnectionManager.getConnection("yrc");
			String sql = "SELECT * FROM YRC_JOB_QUEUE.tblJobs WHERE id = 0";
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			
			rs = stmt.executeQuery( sql );
			rs.moveToInsertRow();
			
			rs.updateInt( "submitter", this.submitter );
			rs.updateInt( "type", 0 );
			rs.updateDate( "submitDate", new java.sql.Date( (new java.util.Date()).getTime() ) );
			rs.updateInt( "status", 0 );
			rs.updateInt( "attempts", 0 );
			
			rs.insertRow();
			rs.last();
			
			int id = rs.getInt( "id" );
			
			rs.close(); rs = null;
			stmt.close(); stmt = null;
			
			sql = "SELECT * FROM YRC_JOB_QUEUE.tblMSJobs WHERE jobID = 0";
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			
			rs = stmt.executeQuery( sql );
			rs.moveToInsertRow();
			
			rs.updateInt( "jobID", id );
			rs.updateInt( "projectID", this.projectID );
			rs.updateString( "serverDirectory", this.serverDirectory );
			rs.updateDate( "runDate", new java.sql.Date( this.runDate.getTime() ) );
			rs.updateInt( "baitProtein", this.baitProtein );
			rs.updateString( "baitDescription", this.baitDescription );
			rs.updateInt( "targetSpecies", this.targetSpecies );
			rs.updateString( "comments", this.comments );
			rs.updateInt( "groupID", this.groupID );
			
			rs.insertRow();
			
			rs.close(); rs = null;
			stmt.close(); stmt = null;
			conn.close(); conn = null;
			
		} finally {
			
			if (rs != null) {
				try { rs.close(); rs = null; } catch (Exception e) { ; }
			}

			if (stmt != null) {
				try { stmt.close(); stmt = null; } catch (Exception e) { ; }
			}
			
			if (conn != null) {
				try { conn.close(); conn = null; } catch (Exception e) { ; }
			}			
		}
		
	}
	
	private int submitter;
	private int projectID;
	private String serverDirectory;
	private java.util.Date runDate;
	private int baitProtein;
	private String baitDescription;
	private int targetSpecies;
	private String comments;
	private int groupID;
	
	
	/**
	 * @return the baitDescription
	 */
	public String getBaitDescription() {
		return baitDescription;
	}
	/**
	 * @param baitDescription the baitDescription to set
	 */
	public void setBaitDescription(String baitDescription) {
		this.baitDescription = baitDescription;
	}
	/**
	 * @return the baitProtein
	 */
	public int getBaitProtein() {
		return baitProtein;
	}
	/**
	 * @param baitProtein the baitProtein to set
	 */
	public void setBaitProtein(int baitProtein) {
		this.baitProtein = baitProtein;
	}
	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}
	/**
	 * @param comments the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}
	/**
	 * @return the groupID
	 */
	public int getGroupID() {
		return groupID;
	}
	/**
	 * @param groupID the groupID to set
	 */
	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}
	/**
	 * @return the projectID
	 */
	public int getProjectID() {
		return projectID;
	}
	/**
	 * @param projectID the projectID to set
	 */
	public void setProjectID(int projectID) {
		this.projectID = projectID;
	}
	/**
	 * @return the runDate
	 */
	public java.util.Date getRunDate() {
		return runDate;
	}
	/**
	 * @param runDate the runDate to set
	 */
	public void setRunDate(java.util.Date runDate) {
		this.runDate = runDate;
	}
	/**
	 * @return the serverDirectory
	 */
	public String getServerDirectory() {
		return serverDirectory;
	}
	/**
	 * @param serverDirectory the serverDirectory to set
	 */
	public void setServerDirectory(String serverDirectory) {
		this.serverDirectory = serverDirectory;
	}
	/**
	 * @return the submitter
	 */
	public int getSubmitter() {
		return submitter;
	}
	/**
	 * @param submitter the submitter to set
	 */
	public void setSubmitter(int submitter) {
		this.submitter = submitter;
	}
	/**
	 * @return the targetSpecies
	 */
	public int getTargetSpecies() {
		return targetSpecies;
	}
	/**
	 * @param targetSpecies the targetSpecies to set
	 */
	public void setTargetSpecies(int targetSpecies) {
		this.targetSpecies = targetSpecies;
	}
	
	
	
	
}
