/**
 * 主服务
 * */
package com.greenshadow.wakeup;

import java.util.Timer;
import java.util.TimerTask;

import com.greenshadow.wakeup.ScreenOnOffListener.ScreenStateListener;

import android.app.Activity;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class SensorsService extends Service implements SensorEventListener {

	private static boolean state = false; // 服务状态

	private int number;
	private long sensitivityValue;
	private long later = 0;// 上次的时间（传感器变化时用）
	private int counter;// 计数器
	private boolean inLockAcquireTime = false; // 判断是否在wl获取权限的时间内

	public SensorManager sm;// 传感器管理器
	private PowerManager.WakeLock wl;// 唤醒锁
	private ScreenOnOffListener screenListener;// 屏幕监听器

	private Notification notification;
	private Timer timer;

	@Override
	public void onCreate() {
		sm = (SensorManager) getSystemService(SENSOR_SERVICE); // 传感器管理器
		screenListener = new ScreenOnOffListener(this); // 实例化屏幕监听对象

		// 用Notification.Builder创建一个通知
		Builder builder = new Builder(this);
		Intent appIntent = new Intent(Intent.ACTION_MAIN)
				// 用于启动Activity的Intent
				.addCategory(Intent.CATEGORY_LAUNCHER)
				.setComponent(
						new ComponentName(this.getPackageName(), this
								.getPackageName() + ".MainActivity"))
				.setFlags(
						Intent.FLAG_ACTIVITY_NEW_TASK
								| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		builder.setContentIntent(
				PendingIntent.getActivity(this, 0, appIntent,
						PendingIntent.FLAG_UPDATE_CURRENT))
				.setSmallIcon(R.drawable.icon_48)
				.setLargeIcon(
						BitmapFactory.decodeResource(getResources(),
								R.drawable.icon))
				.setTicker(this.getString(R.string.app_name) + "服务已启动")
				.setContentTitle(this.getString(R.string.app_name))
				.setContentText("服务正在运行 点击展开设置");
		notification = builder.getNotification();

		state = true;
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		SharedPreferences settings = getSharedPreferences("settings",
				Activity.MODE_PRIVATE);
		number = settings.getInt("number", 4);// 从设置文件中读取数据
		sensitivityValue = settings.getLong("sensitivityValue", 500);
		flags = Service.START_REDELIVER_INTENT; // 服务被杀死后重启时保留intent对象。这里似乎只用粘性标记就可以了？
		int ID;
		if (settings.getBoolean("notification", true))
			ID = 1;
		else
			ID = 0;

		screenListener.requestScreenStateUpdate(new ScreenStateListener() {// 注册屏幕监听
					@Override
					public void onScreenOn() { // 开屏时注销传感器的监听
						sm.unregisterListener(SensorsService.this,
								sm.getDefaultSensor(Sensor.TYPE_PROXIMITY));
					}

					@Override
					public void onScreenOff() { // 关屏时重新注册传感器监听
						sm.registerListener(SensorsService.this,
								sm.getDefaultSensor(Sensor.TYPE_PROXIMITY),
								SensorManager.SENSOR_DELAY_FASTEST);

						// 如果在这个时间内则表明是由本服务将设备唤醒的，且在自动释放wl锁之前，
						// 所以要在关屏时主动释放wl锁并注销timer
						if (inLockAcquireTime) {
							wl.release(); // 释放
							inLockAcquireTime = false;
							Log.v("123456789", "关屏时释放 when screen off release");
							timer.cancel(); // 注销timer
							Log.v("123456789", "timer.cancel");
						}
					}
				});
		startForeground(ID, notification); // 服务常驻后台
		Log.v("123456789", "开始服务 onStartCommand");
		state = true;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {// 注销服务
		stopForeground(true); // 取消服务常驻内存
		screenListener.stopScreenStateUpdate(); // 注销屏幕监听
		Log.v("123456789", "停止服务 onDestroy");
		super.onDestroy();
		state = false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onSensorChanged(SensorEvent event) { // 传感器数值变化
		float value = event.values[0];// values[0]就是距离的值
		long now;// 现在的系统时间

		if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
			if (value > 0) {
				Log.v("123456789", "远离 far away");
			} else {
				Log.v("123456789", "接近 close");
				now = System.currentTimeMillis();// 获取系统时间
				if ((now - later) < sensitivityValue) {// 如果时间差小于灵敏度阀值
					counter++;// 计数器自增
					if (counter >= number) {// 如果计数器大于等于接近次数
						Light();// 亮屏方法
						counter = 0;// 重置计数器
					}
				} else {// 如果时间差大于灵敏度阀值
					counter = 0;// 重置计数器
				}
				later = now;
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	private void Light() {
		if (!inLockAcquireTime) {
			PowerManager pm;
			pm = (PowerManager) getSystemService(POWER_SERVICE);// 电源管理器
			wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
					| PowerManager.FULL_WAKE_LOCK, "bright"); // wakelock锁
			wl.setReferenceCounted(false);
			wl.acquire();// 点亮，这里是永久点亮，所以必须注销
			inLockAcquireTime = true;
			Log.v("123456789", "inLockAcquireTime 为 true");
			timer = new Timer();
			timer.schedule(new TimerTask() { // 利用timer延迟15秒执行，15秒之后注销wl锁
						@Override
						public void run() {
							wl.release();
							inLockAcquireTime = false;
							Log.v("123456789", "inLockAcquireTime 为 false");
						}
					}, 15000);
			Log.v("123456789", "点亮方法 light");
		}
	}

	public static boolean isRunning() { // 返回服务状态
		return state;
	}
}
