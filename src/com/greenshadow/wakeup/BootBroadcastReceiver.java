/**
 * 开机自启广播
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
				Activity.MODE_PRIVATE);// 从SharedPreferences读取开机自启状态
		boolean bootStart = settings.getBoolean("BOOT_START", false);
		if (bootStart
				&& intent.getAction().equals(
						"android.intent.action.BOOT_COMPLETED")) // 如果开机自启且系统返回BOOT_COMPLETED
			context.startService(new Intent(context, SensorsService.class));
	}
}
