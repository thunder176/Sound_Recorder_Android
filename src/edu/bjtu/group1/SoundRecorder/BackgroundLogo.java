package edu.bjtu.group1.SoundRecorder;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

public class BackgroundLogo {

	private Activity context;
	/**
	 * 
	 * @param context
	 */
	public BackgroundLogo(Activity context) {
		this.context = context;
	}

	/**
	 * 
	 * @param name
	 */
	public void showNotification(String name) {
		// 创建一个NotificationManager的引用
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		Notification  notification = new Notification(R.drawable.ic_launcher, name,
				System.currentTimeMillis());
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_NO_CLEAR;
		CharSequence contentTitle = "SoundRecorder"; 
		CharSequence contentText = name; 

		try {
			Intent notificationIntent = new Intent(context,
					Class.forName("edu.bjtu.group1.SoundRecorder.MainActivity"));
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					notificationIntent, 0);
			notification.setLatestEventInfo(context, contentTitle, contentText,
					pendingIntent);

			notificationManager.notify(0, notification);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void cancelNotification() {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
	}
}
