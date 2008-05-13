/**
 * 
 */
package org.yeastrc.jobqueue;

/**
 * @author Mike
 *
 */
public class JobUtils {

	// job types:
	public static final int TYPE_MASS_SPEC_DOWNLOAD = 0;
	
	// job statuses:
	public static final int STATUS_QUEUED = 0;
	public static final int STATUS_OUT_FOR_WORK = 1;
	public static final int STATUS_SOFT_ERROR = 2;
	public static final int STATUS_HARD_ERROR = 3;
	public static final int STATUS_COMPLETE = 4;
	
}
