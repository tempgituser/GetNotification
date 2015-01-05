package com.example.getnotification;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
	
}
