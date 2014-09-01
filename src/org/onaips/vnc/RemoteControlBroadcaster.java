package org.onaips.vnc;

import android.content.Context;
import android.content.Intent;

public class RemoteControlBroadcaster {

	/**
	 * Sends broadcast with response that VNC service was started. You should
	 * request "is service running" to ensure that service is really started.
	 * 
	 * @param context
	 *            context to send broadcast
	 */
	public static final void sendServiceStartedBroadcast(Context context) {
		Intent intent = getBroadcastIntentTemplate(
				RemoteControlConstants.ACTION_RESPONSE,
				RemoteControlConstants.CATEGORY_START_SERVER,
				RemoteControlConstants.INTENT_FLAGS);
		intent.putExtra(RemoteControlConstants.EXTRA_RESPONSE_KEY,
				RemoteControlConstants.EXTRA_RESPONSE_SERVICE_STARTED);
		context.sendBroadcast(intent,
				RemoteControlConstants.PERMISSION_REMOTE_CONTROL);
	}

	/**
	 * Sends broadcast with response that VNC service was not started.
	 * 
	 * @param context
	 *            context to send broadcast
	 * @param errorDescription
	 *            description of why service was not started
	 */
	public static final void sendServiceNotStartedBroadcast(Context context,
			String errorDescription) {
		Intent intent = getBroadcastIntentTemplate(
				RemoteControlConstants.ACTION_RESPONSE,
				RemoteControlConstants.CATEGORY_START_SERVER,
				RemoteControlConstants.INTENT_FLAGS);
		intent.putExtra(RemoteControlConstants.EXTRA_RESPONSE_KEY,
				RemoteControlConstants.EXTRA_RESPONSE_SERVICE_NOT_STARTED);
		intent.putExtra(RemoteControlConstants.EXTRA_DATA_KEY, errorDescription);
		context.sendBroadcast(intent);
	}

	/**
	 * Sends broadcast with response that VNC service was stopped.
	 * 
	 * @param context
	 *            context to send broadcast
	 */
	public static final void sendServiceStoppedBroadcast(Context context) {
		Intent intent = getBroadcastIntentTemplate(
				RemoteControlConstants.ACTION_RESPONSE,
				RemoteControlConstants.CATEGORY_STOP_SERVER,
				RemoteControlConstants.INTENT_FLAGS);
		intent.putExtra(RemoteControlConstants.EXTRA_RESPONSE_KEY,
				RemoteControlConstants.EXTRA_RESPONSE_SERVICE_STOPPED);
		context.sendBroadcast(intent);
	}

	/**
	 * Sends broadcast with response that VNC service was not stopped.
	 * 
	 * @param context
	 *            context to send broadcast
	 * @param errorDescription
	 *            description of why service was not started
	 */
	public static final void sendServiceNotStoppedBroadcast(Context context,
			String errorDescription) {
		Intent intent = getBroadcastIntentTemplate(
				RemoteControlConstants.ACTION_RESPONSE,
				RemoteControlConstants.CATEGORY_STOP_SERVER,
				RemoteControlConstants.INTENT_FLAGS);
		intent.putExtra(RemoteControlConstants.EXTRA_RESPONSE_KEY,
				RemoteControlConstants.EXTRA_RESPONSE_SERVICE_NOT_STOPPED);
		intent.putExtra(RemoteControlConstants.EXTRA_DATA_KEY, errorDescription);
		context.sendBroadcast(intent);
	}

	/**
	 * Sends broadcast with current VNC service status.
	 * 
	 * @param context
	 *            context to send broadcast.
	 * @param serviceStatus
	 *            current VNC service status (running/not running)
	 */
	public static final void sendServiceStatusBroadcast(Context context,
			int serviceStatus) {
		Intent intent = getBroadcastIntentTemplate(
				RemoteControlConstants.ACTION_RESPONSE,
				RemoteControlConstants.CATEGORY_SERVER_STATUS,
				RemoteControlConstants.INTENT_FLAGS);
		intent.putExtra(RemoteControlConstants.EXTRA_RESPONSE_KEY,
				RemoteControlConstants.EXTRA_RESPONSE_SERVICE_STATUS);
		intent.putExtra(RemoteControlConstants.EXTRA_SERVICE_STATUS_KEY,
				serviceStatus);
		context.sendBroadcast(intent);
	}

	public static final void sendServiceIpAddressBroadcast(Context context,
			String ipAddress) {
		Intent intent = getBroadcastIntentTemplate(
				RemoteControlConstants.ACTION_RESPONSE,
				RemoteControlConstants.CATEGORY_IP_ADDRESS,
				RemoteControlConstants.INTENT_FLAGS);
		intent.putExtra(RemoteControlConstants.EXTRA_RESPONSE_KEY,
				RemoteControlConstants.EXTRA_RESPONSE_IP_ADDRESS);
		intent.putExtra(RemoteControlConstants.EXTRA_DATA_KEY, ipAddress);
		context.sendBroadcast(intent);
	}

	/**
	 * Gets default (template) intent to send via broadcast
	 * 
	 * @param action
	 *            intent action
	 * @param category
	 *            intent category
	 * @param flags
	 *            intent flags
	 * @return intent "template"
	 */
	private static final Intent getBroadcastIntentTemplate(String action,
			String category, int flags) {
		Intent intent = new Intent(action);
		intent.addFlags(flags);
		intent.addCategory(category);
		return intent;
	}

}
