/**
 * OnDatabaseUpgradeCompeteListener.java
 * @date Jul 2, 2011
 * @author Twenty Codes, LLC
 * @author ricky barrette
 */
package com.TwentyCodes.android.LocationRinger.db;

/**
 * This interface will be used to listen to see when the database events are
 * complete
 * 
 * @author ricky barrette
 */
public interface DatabaseListener {
	
	public void onDatabaseCreate();

	public void onDatabaseUpgrade();

	public void onDatabaseUpgradeComplete();

	public void onRestoreComplete();

	public void onRingerDeletionComplete();
	
}