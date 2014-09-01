package org.onaips.vnc;

import android.content.Intent;

public class RemoteControlConstants {

	/**
	 * Broadcast Receiver action for situation when some application REQUESTS
	 * something from VNC server application
	 */
	public static final String ACTION_REQUEST = "com.onaips.vnc.REQUEST";
	/**
	 * Broadcast Receiver action for situation when VNC server application
	 * RESPONDS to requesting application
	 */
	public static final String ACTION_RESPONSE = "com.onaips.vnc.RESPONSE";

	/**
	 * Broadcast Receiver category for requesting or responding current VNC
	 * server status
	 */
	public static final String CATEGORY_SERVER_STATUS = "category.vnc.SERVER_STATUS";

	/**
	 * Broadcast Receiver category for requesting or responding current vnVNCc
	 * server IP address
	 */
	public static final String CATEGORY_IP_ADDRESS = "category.vnc.IP_ADDRESS";

	/**
	 * Broadcast Receiver category for requesting or responding for starting
	 * server
	 */
	public static final String CATEGORY_START_SERVER = "category.vnc.START_SERVER";

	/**
	 * Broadcast Receiver category for requesting or responding for stopping
	 * server
	 */
	public static final String CATEGORY_STOP_SERVER = "category.vnc.STOP_SERVER";

	/**
	 * Default intent flags for Broadcast
	 */
	public static final int INTENT_FLAGS = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
			| Intent.FLAG_ACTIVITY_NEW_TASK;

	/**
	 * Permission for VNC server remote control
	 */
	public static final String PERMISSION_REMOTE_CONTROL = "com.permission.onaips.vnc.REMOTE_CONTROL";

	/**
	 * Type of response to put in Intent's extras
	 */
	public static final String EXTRA_RESPONSE_KEY = "response";
	public static final int EXTRA_RESPONSE_UNDEFINED = -1;
	public static final int EXTRA_RESPONSE_SERVICE_STARTED = 0;
	public static final int EXTRA_RESPONSE_SERVICE_NOT_STARTED = 1;
	public static final int EXTRA_RESPONSE_SERVICE_STOPPED = 2;
	public static final int EXTRA_RESPONSE_SERVICE_NOT_STOPPED = 3;
	public static final int EXTRA_RESPONSE_SERVICE_STATUS = 4;
	public static final int EXTRA_RESPONSE_IP_ADDRESS = 5;

	/**
	 * Additional data to put in Intent's extras
	 */
	public static final String EXTRA_DATA_KEY = "data";

	/**
	 * VNC server status to put in Intent's extras
	 */
	public static final String EXTRA_SERVICE_STATUS_KEY = "service_status";
	public static final int EXTRA_SERVICE_STATUS_UNDEFINED = -1;
	public static final int EXTRA_SERVICE_STATUS_NOT_RUNNING = 0;
	public static final int EXTRA_SERVICE_STATUS_RUNNING = 1;

}
