package com.example.getnotification;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	Button testButton;
	TextView textView;
	String TAG = "zzz";
	String STARTCODE = " ";
	boolean debug = true;

	String RUN = "🐴";
	String DOG = "🐶";
	String TIME = "🕗";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);


//		Intent notificationIntent = new Intent(getApplicationContext(), NotificationService.class);
//		startService(notificationIntent);
//		
//		NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//		nMgr.cancelAll();
//		clearNotification();

		testButton = (Button) findViewById(R.id.exec);
		final EditText edittext = (EditText) findViewById(R.id.edittext);
		Button sendButton = (Button) findViewById(R.id.send);
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				openDialog();
//				sendMessage("test");
				try {
//					byte[] origin = edittext.getText().toString().getBytes("GBK");
					byte[] origin = "test".getBytes("UTF-7");
					String converted = new String(origin, "GB2312");
					insertDBToSend(SQL.name4, converted);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
//				backToBefore(MainActivity.this);
//				insertDBToSend(SQL.name4, edittext.getText().toString());
			}
		});
		Button closeButton = (Button) findViewById(R.id.close);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
		
		textView = (TextView) findViewById(R.id.textView);
		testButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] commands = { "dumpsys notification" };
				String log = run(commands);
				String notiTitle = debug ? " 发信息给您" : "null 发信息给您";
				boolean checkNotfi = false;
//				checkNotfi = true;
				
				if (null == null || checkNotfi && log.toString().contains(notiTitle)){
				
					String message = run(SQL.sql);
					if(message == null || message.length() < 1){
						Toast.makeText(MainActivity.this, "enpty message", Toast.LENGTH_SHORT).show();
						return;
					}
					Log.wtf(TAG, message);
					Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
					String[] idAndData = message.split("\\|");
					String id = idAndData[0];
					String data = idAndData[1];

					if (!isNewMessage(id)) {
//						return;
					}
					textView.setText(data);
					
					if (data.startsWith(RUN)) {
						data = data.replace(RUN, "");
						String[] runCommands = { data };
						run(runCommands);

						deleteMessage(id);
						clearNotification();
					} else if (data.startsWith(DOG)) {

					} else if (data.startsWith(TIME)) {

					} else {

					}
					
				}
			}
		});
	}
	
	public String run(String[] commands){
		Process process = null;
		DataOutputStream dataOutputStream = null;
		final StringBuilder log = new StringBuilder();

		try {
			process = Runtime.getRuntime().exec("su");
			dataOutputStream = new DataOutputStream(process.getOutputStream());
			int length = commands.length;
			for (int i = 0; i < length; i++) {
				Log.e(TAG, "commands[" + i + "]:" + commands[i]);
				dataOutputStream.writeBytes(commands[i] + "\n");
			}
			dataOutputStream.writeBytes("exit\n");
			dataOutputStream.flush();

			process.waitFor();

			BufferedReader reader = null;
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = "";
			// List<String> lineList = new ArrayList<String>();
			String separator = System.getProperty("line.separator");
			Pattern pattern = Pattern.compile("pkg=[^\\s]+");
			while ((line = reader.readLine()) != null) {
				if (line != null && line.trim().startsWith("NotificationRecord")) {
					Matcher matcher = pattern.matcher(line);
					if (matcher.find()) {
						// lineList.add(matcher.group());
					} else {
						// Log.e(TAG, "what's this?!");
					}
				}

				log.append(line);
				log.append(separator);
			}
			Log.e(TAG, "log:" + log.toString());
		} catch (Exception e) {
			Log.e(TAG, "copy fail", e);
		} finally {
			try {
				if (dataOutputStream != null) {
					dataOutputStream.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
		Log.e(TAG, "finish");
		return log == null || log.length() < 1 ? "" : log.toString();
	}
	
	String SAVE_FILE = "preferences";
	public boolean isNewMessage(String idString){
		Long id = Long.valueOf(idString);
		SharedPreferences pref = MainActivity.this.getSharedPreferences(SAVE_FILE, android.content.Context.MODE_PRIVATE);
		Long lastSavedId = pref.getLong("lastId", 0L);
		if (id > lastSavedId) {
			Editor editor = pref.edit();
			editor.putLong("lastId", id);
			editor.commit();
			return true;
		} else {
			return false;
		}
	}
	
	public void deleteMessage(String id) {
		String delete[] = { "sqlite3 /data/data/com.whatsapp/databases/msgstore.db 'delete from messages where _id = " + id + ";'" };
		run(delete);
	}
	
	
	public void clearNotification() {
		String clear[] = { "service call notification 1" };
		run(clear);
	}
	
	public void openDialog(){
		Cursor c = getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[] { ContactsContract.Contacts.Data._ID },
				ContactsContract.Data.DATA1 + "=?", new String[] { SQL.name4 }, null);
		c.moveToFirst();
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("content://com.android.contacts/data/" + c.getString(0)));
		startActivity(i);
		c.close();
	}
	
	public void sendMessage(String text) {
		String inputText[] = { "input text \"" + text + "\"", "input keyevent 66", "input keyevent 4", "input keyevent 4", "input keyevent 4" };
		run(inputText);
	}

	public void pressBack() {
		String inputText[] = { "input keyevent 4" };
		run(inputText);
	}
	
	public void backToBefore(Context context){
		String backText[] = { "input keyevent 4" };
		while(getCurrentActivityName(context).equals("com.whatsapp")){
			run(backText);
		}
	}

	public String getCurrentActivityName(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
		ComponentName componentInfo = taskInfo.get(0).topActivity;
		return componentInfo.getClassName();
	}
	
	/**
	 * CREATE TABLE messages (_id INTEGER PRIMARY KEY AUTOINCREMENT,
	 * key_remote_jid TEX T NOT NULL, key_from_me INTEGER, key_id TEXT NOT NULL,
	 * status INTEGER, needs_pus h INTEGER, data TEXT, timestamp INTEGER,
	 * media_url TEXT, media_mime_type TEXT, m edia_wa_type TEXT, media_size
	 * INTEGER, media_name TEXT, media_caption TEXT, medi a_hash TEXT,
	 * media_duration INTEGER, origin INTEGER, latitude REAL, longitude RE AL,
	 * thumb_image TEXT, remote_resource TEXT, received_timestamp INTEGER,
	 * send_tim estamp INTEGER, receipt_server_timestamp INTEGER,
	 * receipt_device_timestamp INTEG ER, read_device_timestamp INTEGER,
	 * played_device_timestamp INTEGER, raw_data BLO B, recipient_count
	 * INTEGER);
	 */
	public void insertDBToSend(final String jid, final String text){
//		String insert[] = { "sqlite3 /data/data/com.whatsapp/databases/msgstore.db 'insert  into messages "
//				+ "(key_remote_jid,key_from_me,key_id,status,needs_push,data,timestamp,media_wa_type,media_size,media_duration,origin,latitude, longitude,received_timestamp,send_timestamp,receipt_server_timestamp,receipt_device_timestamp, read_device_timestamp, recipient_count ) values("
//				+ "  ? ,          \"0\",       \"0\",  0,    0,         ?,     0,        \"0\",        \"0\",      0,            0,    0,        0,               ?,               -1,                    -1,            -1,                         -1,                    0 );'" };
		new Thread(){
			@Override
			public void run(){
				String insert[] = { "sqlite3 /data/data/com.whatsapp/databases/msgstore.db 'insert into messages (key_remote_jid,key_from_me,key_id,status,needs_push,data,timestamp,media_wa_type,media_size,media_duration,origin,latitude,longitude,received_timestamp,send_timestamp,receipt_server_timestamp,receipt_device_timestamp, read_device_timestamp, recipient_count ) values(\"" + jid + "\",\"1\","+ String.valueOf(System.currentTimeMillis())  +",0,0,\"" + text + "\","+ String.valueOf(System.currentTimeMillis())  +",\"0\",\"0\",0,0,0,0,"+ String.valueOf(System.currentTimeMillis())  +",-1,-1,-1,-1,0 );'" };
				MainActivity.this.run(insert);
				restartApp();
			}
		}.start();
	}
	
	public void restartApp(){
		String restart[] = { "am force-stop " + SQL.packageName, "am start -n " + SQL.packageNameActivity };
//		String close[] = { "am force-stop " + packageName };
//		run(close);
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		String start[] = { "am start -n " + packageName };
//		run(start);
		run(restart);
		pressBack();
		pressBack();
	}
	
	
	
	
	
	
}
