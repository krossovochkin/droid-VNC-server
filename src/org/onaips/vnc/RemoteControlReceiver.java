package org.onaips.vnc;

import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RemoteControlReceiver extends BroadcastReceiver {

	public static final String TAG = RemoteControlReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null) {
			Log.d(TAG, "intent is null");
			return;
		}

		if (!RemoteControlConstants.ACTION_REQUEST.equals(intent.getAction())) {
			Log.d(TAG, "intent action is not: "
					+ RemoteControlConstants.ACTION_REQUEST);
			return;
		}

		Set<String> categories = intent.getCategories();
		if (categories == null) {
			Log.d(TAG, "intent categories set is null");
			return;
		}

		if (categories.contains(RemoteControlConstants.CATEGORY_START_SERVER)) {
			Intent i = new Intent(context, ServerManager.class);
			i.putExtra(ServerManager.EXTRA_FORCE_START_SERVER_KEY, true);
			context.startService(i);
		} else {
			Log.d(TAG, "insupported intent category");
			return;
		}
	}

}
