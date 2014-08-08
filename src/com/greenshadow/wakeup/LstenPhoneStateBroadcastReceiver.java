/**
 * 电话状态监听广播
 * */
package com.greenshadow.wakeup;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.Notification.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.telephony.TelephonyManager;
import android.util.Log;

public class LstenPhoneStateBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context context, Intent intent) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Service.TELEPHONY_SERVICE);// 电话服务管理器
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Service.NOTIFICATION_SERVICE);// 通知管理器
		Log.v("123456789", intent.getAction());

		if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
			Log.v("123456789", "CallState=" + tm.getCallState());
			if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
				if (!SensorsService.isRunning()) { // 如果服务不在运行状态
					nm.cancel(2); // 取消id为2的通知
					context.startService(new Intent(context,
							SensorsService.class)); // 开启服务
				}
			} else {
				Builder builder = new Builder(context); // 利用builder建立一个通知
				builder.setContentIntent(
						PendingIntent.getActivity(context, 0, new Intent(
								context, SensorsService.class),
								PendingIntent.FLAG_NO_CREATE))
						.setSmallIcon(R.drawable.icon_48)
						.setLargeIcon(
								BitmapFactory.decodeResource(
										context.getResources(), R.drawable.icon))
						.setOngoing(true)
						.setTicker(
								context.getString(R.string.app_name) + "服务已暂停")
						.setContentTitle(context.getString(R.string.app_name))
						.setContentText("正在通话，服务暂停");
				Notification notification = builder.getNotification();
				context.stopService(new Intent(context, SensorsService.class)); // 停止服务
				nm.notify(2, notification); // 发出通知
			}
		}
	}
}
