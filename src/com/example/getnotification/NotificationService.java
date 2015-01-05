package com.example.getnotification;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationService extends NotificationListenerService {

	public static boolean sCleanNotification = false;

	@Override
	public void onStart(Intent intent, int startId) {
		if (sCleanNotification) {
			// 清空通知栏
			try {
				NotificationService.this.cancelAllNotifications();
			} catch (Exception e) {
				e.printStackTrace();
				// 开启通知设置
				// startActivity(new
				// Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
			}
		} else {
			mHandler.sendEmptyMessageDelayed(0, 2000);
		}
	}

	@Override
	public void onNotificationPosted(StatusBarNotification arg0) {
		if (sCleanNotification) {
			// 清空通知栏
			try {
				NotificationService.this.cancelAllNotifications();
			} catch (Exception e) {
				e.printStackTrace();
				// 开启通知设置
				// startActivity(new
				// Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
			}
		} else {
			stopSelf();
		}
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification arg0) {

	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// 关闭
			stopSelf();
		}

	};

}
