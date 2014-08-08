/**
 * ���������㲥
 * */
package com.greenshadow.wakeup;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences settings = context.getSharedPreferences("settings",
				Activity.MODE_PRIVATE);// ��SharedPreferences��ȡ��������״̬
		boolean bootStart = settings.getBoolean("BOOT_START", false);
		if (bootStart
				&& intent.getAction().equals(
						"android.intent.action.BOOT_COMPLETED")) // �������������ϵͳ����BOOT_COMPLETED
			context.startService(new Intent(context, SensorsService.class));
	}
}
