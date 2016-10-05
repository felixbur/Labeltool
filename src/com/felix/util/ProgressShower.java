package com.felix.util;

/**
 * Progresshower is an interface for a class that is progress reported to.
 * 
 * @author felix
 * 
 */
public interface ProgressShower {
	/**
	 * Start the activity.
	 * 
	 * @param message
	 */
	public void init(String message);

	/**
	 * Report some progress.
	 * 
	 * @param message
	 */
	public void showProgress(String message);

	/**
	 * Report finishing the activity.
	 * 
	 * @param message
	 */
	public void done(String message);
}
