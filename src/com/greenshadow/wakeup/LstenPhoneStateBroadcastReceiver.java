/**
 * �绰״̬�����㲥
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
				.getSystemService(Service.TELEPHONY_SERVICE);// �绰���������
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Service.NOTIFICATION_SERVICE);// ֪ͨ������
		Log.v("123456789", intent.getAction());

		if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
			Log.v("123456789", "CallState=" + tm.getCallState());
			if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
				if (!SensorsService.isRunning()) { // �������������״̬
					nm.cancel(2); // ȡ��idΪ2��֪ͨ
					context.startService(new Intent(context,
							SensorsService.class)); // ��������
				}
			} else {
				Builder builder = new Builder(context); // ����builder����һ��֪ͨ
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
								context.getString(R.string.app_name) + "��������ͣ")
						.setContentTitle(context.getString(R.string.app_name))
						.setContentText("����ͨ����������ͣ");
				Notification notification = builder.getNotification();
				context.stopService(new Intent(context, SensorsService.class)); // ֹͣ����
				nm.notify(2, notification); // ����֪ͨ
			}
		}
	}
}
