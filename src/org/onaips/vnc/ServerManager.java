package org.onaips.vnc;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.util.Log;
import android.widget.Toast;

public class ServerManager extends Service {
	
	public static final String EXTRA_FORCE_START_SERVER_KEY = "force_start_server";
	
	public static final String KEY_PUBLIC_IP = "public_IP";
	public static final String KEY_LOCAL_IP = "local_IP";
	public static final String KEY_PORT = "port";
	public static final String KEY_NOTE = "_Note:";
	// TODO: move to strings.xml
	public static final String FORWARD_INSTRUCTION = "You need to configure router to forward ";
	
	SharedPreferences preferences;
	private static PowerManager.WakeLock wakeLock = null;

	boolean serverOn = false;
	public static String SOCKET_ADDRESS = "org.onaips.vnc.gui";
	SocketListener serverConnection = null;

	private String rHost = null;
	private final IBinder mBinder = new MyBinder();
	private Handler handler;
	
	private VncBroadcastReceiver mVncBroadcastReceiver = new VncBroadcastReceiver();

	@Override
	public void onCreate() {
		super.onCreate();

		handler = new Handler(Looper.getMainLooper());
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		registerVncReceiver();
		
		if (serverConnection != null) {
			log("ServerConnection was already active!");
		} else {
			log("ServerConnection started");
			serverConnection = new SocketListener();
			serverConnection.start();
		}

	}

	private void registerVncReceiver() {
		IntentFilter intentFilter = new IntentFilter(
				RemoteControlConstants.ACTION_REQUEST);
		intentFilter.addCategory(RemoteControlConstants.CATEGORY_SERVER_STATUS);
		intentFilter.addCategory(RemoteControlConstants.CATEGORY_IP_ADDRESS);
		intentFilter.addCategory(RemoteControlConstants.CATEGORY_STOP_SERVER);

		this.registerReceiver(mVncBroadcastReceiver, intentFilter,
				RemoteControlConstants.PERMISSION_REMOTE_CONTROL, null);
	}

	private void unregisterVncReceiver() {
		this.unregisterReceiver(mVncBroadcastReceiver);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		boolean forceStartServer = false;
		
		if (intent != null) {
			forceStartServer = intent.getBooleanExtra(
					EXTRA_FORCE_START_SERVER_KEY, false);	
		}		
		 
		handleStart(forceStartServer);
		return START_NOT_STICKY;
	}

	private void handleStart(boolean forceStartServer) {
		log("ServerManager::handleStart");

		if (forceStartServer) {
			log("ServerManager: force start server");
			startServer();
		} else {
			Boolean startdaemon = preferences.getBoolean("startdaemononboot",
					false);
			log("Let me see if we need to start daemon..."
					+ (startdaemon ? "Yes" : "No"));
			if (startdaemon)
				startServer();
		}
	}
	
	public void startServer() {
		// Lets see if i need to boot daemon...
		try {
			Process sh;
			String files_dir = getFilesDir().getAbsolutePath();

			String password = preferences.getString("password", "");
			String password_check = "";
			if (!password.equals(""))
				password_check = "-p " + password;

			String rotation = preferences.getString("rotation", "0");
			if (!rotation.equals(""))
				rotation = "-r " + rotation;

			String scaling = preferences.getString("scale", "100");

			String scaling_string = "";
			if (!scaling.equals(""))
				scaling_string = "-s " + scaling;

			String port = preferences.getString("port", "5901");
			try {
				int port1 = Integer.parseInt(port);
				port = String.valueOf(port1);
			} catch (NumberFormatException e) {
				port = "5901";
			}
			String port_string = "";
			if (!port.equals(""))
				port_string = "-P " + port;

			String reverse_string = "";
			if (rHost != null && !rHost.equals(""))
				reverse_string = "-R " + rHost;


			String display_method = "";
			if (!preferences.getString("displaymode", "auto").equals("auto"))
				display_method = "-m " + preferences.getString("displaymode", "auto");

			String display_zte="";
			if (preferences.getBoolean("rotate_zte", false))
				display_zte = "-z";
			
			Runtime.getRuntime().exec(
					"chmod 777 " + getFilesDir().getAbsolutePath()
					+ "/androidvncserver");
 
			String permission_string="chmod 777 " + files_dir + "/androidvncserver";
			String server_string= getFilesDir().getAbsolutePath()+ "/androidvncserver " + password_check + " " + rotation+ " " + scaling_string + " " + port_string + " "
			+ reverse_string + " " + display_method + " " + display_zte;
 
			boolean root=preferences.getBoolean("asroot",true);
			root &= MainActivity.hasRootPermission();
 
			if (root)     
			{ 
				log("Running as root...");
				sh = Runtime.getRuntime().exec("su",null,new File(files_dir));
				OutputStream os = sh.getOutputStream();
				writeCommand(os, permission_string);
				writeCommand(os, server_string);
			}
			else
			{
				log("Not running as root...");
				Runtime.getRuntime().exec(permission_string);
				Runtime.getRuntime().exec(server_string,null,new File(files_dir));
			}
			// dont show password on logcat
			log("Starting " + getFilesDir().getAbsolutePath()
					+ "/androidvncserver " + " " + rotation + " "
					+ scaling_string + " " + port_string + " " + display_method);
			RemoteControlBroadcaster.sendServiceStartedBroadcast(this);
		} catch (IOException e) {
			String errorDescription = "startServer(): " + e.getMessage();
			log(errorDescription);
			RemoteControlBroadcaster.sendServiceNotStartedBroadcast(this,
					errorDescription);
		} catch (Exception e) {
			String errorDescription = "startServer(): " + e.getMessage();
			log(errorDescription);
			RemoteControlBroadcaster.sendServiceNotStartedBroadcast(this,
					errorDescription);
		}

	}

	void startReverseConnection(String host) {
		try {
			rHost = host;

			if (isServerRunning()) {
				killServer(this);
				Thread.sleep(2000);
			}

			startServer();
			rHost = null;

		} catch (InterruptedException e) {
			log(e.getMessage());
		}
	}

	public static void killServer(Context context) {
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress addr = InetAddress.getLocalHost();
			String toSend = "~KILL|";
			byte[] buffer = toSend.getBytes();

			DatagramPacket question = new DatagramPacket(buffer, buffer.length,
					addr, 13132);
			clientSocket.send(question);
			RemoteControlBroadcaster.sendServiceStoppedBroadcast(context);
		} catch (Exception e) {
			RemoteControlBroadcaster.sendServiceNotStoppedBroadcast(context, e.getMessage());
		}
	}

	public static boolean isServerRunning() {
		try {
			byte[] receiveData = new byte[1024];
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress addr = InetAddress.getLocalHost();

			clientSocket.setSoTimeout(100);
			String toSend = "~PING|";
			byte[] buffer = toSend.getBytes();

			DatagramPacket question = new DatagramPacket(buffer, buffer.length,
					addr, 13132);
			clientSocket.send(question);

			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			clientSocket.receive(receivePacket);
			String receivedString = new String(receivePacket.getData());
			receivedString = receivedString.substring(0, receivePacket
					.getLength());

			return receivedString.equals("~PONG|");
		} catch (Exception e) {
			return false;
		}
	}

	class SocketListener extends Thread {
		DatagramSocket server = null;
		boolean finished = false;

		public void finishThread() {
			finished = true;
		}

		@Override
		public void run() {
			try {
				server = new DatagramSocket(13131);
				log("Listening...");

				while (!finished) {
					DatagramPacket answer = new DatagramPacket(new byte[1024],
							1024);
					server.receive(answer);

					String resp = new String(answer.getData());
					resp = resp.substring(0, answer.getLength());

					log("RECEIVED " + resp);  

					if (resp.length() > 5
							&& resp.substring(0, 6).equals("~CLIP|")) {
						resp = resp.substring(7, resp.length() - 1);
						ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

						clipboard.setText(resp.toString());
					} else if (resp.length() > 6
							&& resp.substring(0, 6).equals("~SHOW|")) {
						resp = resp.substring(6, resp.length() - 1);
						showTextOnScreen(resp);
					} else if (resp.length() > 15
							&& (resp.substring(0, 15).equals("~SERVERSTARTED|") || resp
									.substring(0, 15).equals("~SERVERSTOPPED|"))) {
						Intent intent = new Intent("org.onaips.vnc.ACTIVITY_UPDATE");
						sendBroadcast(intent);
					} 
					else if (preferences.getBoolean("notifyclient", true)) {
						if (resp.length() > 10
								&& resp.substring(0, 11).equals("~CONNECTED|")) {
							resp = resp.substring(11, resp.length() - 1);
							showClientConnected(resp);
						} else if (resp.length() > 13
								&& resp.substring(0, 14).equals(
								"~DISCONNECTED|")) {
							showClientDisconnected();
						}
					} else {
						log("Received: " + resp);
					}
				}
			} catch (IOException e) {
				log("ERROR em SOCKETLISTEN " + e.getMessage());
			}
		}
	}

	public void showClientConnected(String c) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

		int icon = R.drawable.icon;
		CharSequence tickerText = c + " connected to VNC server";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);

		Context context = getApplicationContext();
		CharSequence contentTitle = "Droid VNC Server";
		CharSequence contentText = "Client Connected from " + c;
		Intent notificationIntent = new Intent();
		PendingIntent contentIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		mNotificationManager.notify(MainActivity.APP_ID, notification);

		// lets see if we should keep screen on
		if (preferences.getBoolean("screenturnoff", false)) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "VNC");
			wakeLock.acquire();
		}
	}

	void showClientDisconnected() {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		mNotificationManager.cancel(MainActivity.APP_ID);

		if (wakeLock != null && wakeLock.isHeld())
			wakeLock.release();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//showTextOnScreen("Droid VNC server service killed...");
		unregisterVncReceiver();
	}

	static void writeCommand(OutputStream os, String command) throws Exception {
		os.write((command + "\n").getBytes("ASCII"));
	}

	public void showTextOnScreen(final String t) {
		handler.post(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), t, Toast.LENGTH_LONG)
				.show();
			}
		});
	}

	public void log(String s) {
		Log.v(MainActivity.VNC_LOG, s);
	}


	// We return the binder class upon a call of bindService
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public class MyBinder extends Binder {
		ServerManager getService() {
			return ServerManager.this;
		}
	}
	
	public static class VncBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent == null) {
				Log.d(VncBroadcastReceiver.class.getSimpleName(),
						"intent is null");
				return;
			}

			if (!RemoteControlConstants.ACTION_REQUEST.equals(intent
					.getAction())) {
				Log.d(VncBroadcastReceiver.class.getSimpleName(),
						"intent action is not: "
								+ RemoteControlConstants.ACTION_REQUEST);
				return;
			}

			Set<String> categories = intent.getCategories();
			if (categories == null) {
				Log.d(VncBroadcastReceiver.class.getSimpleName(),
						"intent categories set is null");
				return;
			}

			if (categories
					.contains(RemoteControlConstants.CATEGORY_SERVER_STATUS)) {
				sendServiceStatusBroadcast(context);
			} else if (categories
					.contains(RemoteControlConstants.CATEGORY_IP_ADDRESS)) {
				sendIpAddressBroadcast(context);
			} else if (categories.contains(RemoteControlConstants.CATEGORY_STOP_SERVER)) {
				killServer(context);
			} else {
				Log.d(VncBroadcastReceiver.class.getSimpleName(),
						"incompatible categories");
			}
		}

		private void sendServiceStatusBroadcast(Context context) {
			boolean isServerRunning = isServerRunning();
			RemoteControlBroadcaster
					.sendServiceStatusBroadcast(
							context,
							isServerRunning ? RemoteControlConstants.EXTRA_SERVICE_STATUS_RUNNING
									: RemoteControlConstants.EXTRA_SERVICE_STATUS_NOT_RUNNING);
		}

		private void sendIpAddressBroadcast(final Context context) {
			Thread thread = new Thread(new Runnable() {				
				public void run() {
					String ipExternalAddress = getExternalIpAddress();
					String ipLocalAddress = getLocalIpAddress();
					String port = getServerPort(context);
					
					JSONObject root = new JSONObject();
					try {
					root.put(KEY_PUBLIC_IP, ipExternalAddress);
					root.put(KEY_LOCAL_IP, ipLocalAddress);
					root.put(KEY_PORT, port);
					root.put(KEY_NOTE,
							FORWARD_INSTRUCTION
									+ ipExternalAddress + ":" + port + " to "
									+ ipLocalAddress + ":" + port);
					} catch (JSONException e) {
						Log.e(VncBroadcastReceiver.class.getSimpleName(), e.getMessage());
					}
					
					RemoteControlBroadcaster.sendServiceIpAddressBroadcast(context, root.toString());		
				}
			});
			thread.start();
		}
	}	
	
	public static String getLocalIpAddress() {
		try {
			String ipv4;
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						if (!inetAddress.isLoopbackAddress()
								&& InetAddressUtils
										.isIPv4Address(ipv4 = inetAddress
												.getHostAddress()))
							return ipv4;
					}
				}
			}
		} catch (SocketException ex) {
			// TODO: do sth
		}
		return "";
	}
	
	public static String getExternalIpAddress() {
		String ip;
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(
					"http://ip2country.sourceforge.net/ip2c.php?format=JSON");
			HttpResponse response;

			response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();
			entity.getContentLength();
			String str = EntityUtils.toString(entity);
			JSONObject json_data = new JSONObject(str);
			ip = json_data.getString("ip");
			return ip;
		} catch (Exception e) {
			return "";
		}
	}
	
	public static String getServerPort(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String port = preferences.getString("port", "5901");
		try {
			int port1 = Integer.parseInt(port);
			port = String.valueOf(port1);
		} catch (NumberFormatException e) {
			port = "5901";
		}
		return port;
	}
}
