/**
 * ������
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

	private static boolean state = false; // ����״̬

	private int number;
	private long sensitivityValue;
	private long later = 0;// �ϴε�ʱ�䣨�������仯ʱ�ã�
	private int counter;// ������
	private boolean inLockAcquireTime = false; // �ж��Ƿ���wl��ȡȨ�޵�ʱ����

	public SensorManager sm;// ������������
	private PowerManager.WakeLock wl;// ������
	private ScreenOnOffListener screenListener;// ��Ļ������

	private Notification notification;
	private Timer timer;

	@Override
	public void onCreate() {
		sm = (SensorManager) getSystemService(SENSOR_SERVICE); // ������������
		screenListener = new ScreenOnOffListener(this); // ʵ������Ļ��������

		// ��Notification.Builder����һ��֪ͨ
		Builder builder = new Builder(this);
		Intent appIntent = new Intent(Intent.ACTION_MAIN)
				// ��������Activity��Intent
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
				.setTicker(this.getString(R.string.app_name) + "����������")
				.setContentTitle(this.getString(R.string.app_name))
				.setContentText("������������ ���չ������");
		notification = builder.getNotification();

		state = true;
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		SharedPreferences settings = getSharedPreferences("settings",
				Activity.MODE_PRIVATE);
		number = settings.getInt("number", 4);// �������ļ��ж�ȡ����
		sensitivityValue = settings.getLong("sensitivityValue", 500);
		flags = Service.START_REDELIVER_INTENT; // ����ɱ��������ʱ����intent���������ƺ�ֻ��ճ�Ա�ǾͿ����ˣ�
		int ID;
		if (settings.getBoolean("notification", true))
			ID = 1;
		else
			ID = 0;

		screenListener.requestScreenStateUpdate(new ScreenStateListener() {// ע����Ļ����
					@Override
					public void onScreenOn() { // ����ʱע���������ļ���
						sm.unregisterListener(SensorsService.this,
								sm.getDefaultSensor(Sensor.TYPE_PROXIMITY));
					}

					@Override
					public void onScreenOff() { // ����ʱ����ע�ᴫ��������
						sm.registerListener(SensorsService.this,
								sm.getDefaultSensor(Sensor.TYPE_PROXIMITY),
								SensorManager.SENSOR_DELAY_FASTEST);

						// ��������ʱ������������ɱ������豸���ѵģ������Զ��ͷ�wl��֮ǰ��
						// ����Ҫ�ڹ���ʱ�����ͷ�wl����ע��timer
						if (inLockAcquireTime) {
							wl.release(); // �ͷ�
							inLockAcquireTime = false;
							Log.v("123456789", "����ʱ�ͷ� when screen off release");
							timer.cancel(); // ע��timer
							Log.v("123456789", "timer.cancel");
						}
					}
				});
		startForeground(ID, notification); // ����פ��̨
		Log.v("123456789", "��ʼ���� onStartCommand");
		state = true;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {// ע������
		stopForeground(true); // ȡ������פ�ڴ�
		screenListener.stopScreenStateUpdate(); // ע����Ļ����
		Log.v("123456789", "ֹͣ���� onDestroy");
		super.onDestroy();
		state = false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onSensorChanged(SensorEvent event) { // ��������ֵ�仯
		float value = event.values[0];// values[0]���Ǿ����ֵ
		long now;// ���ڵ�ϵͳʱ��

		if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
			if (value > 0) {
				Log.v("123456789", "Զ�� far away");
			} else {
				Log.v("123456789", "�ӽ� close");
				now = System.currentTimeMillis();// ��ȡϵͳʱ��
				if ((now - later) < sensitivityValue) {// ���ʱ���С�������ȷ�ֵ
					counter++;// ����������
					if (counter >= number) {// ������������ڵ��ڽӽ�����
						Light();// ��������
						counter = 0;// ���ü�����
					}
				} else {// ���ʱ�����������ȷ�ֵ
					counter = 0;// ���ü�����
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
			pm = (PowerManager) getSystemService(POWER_SERVICE);// ��Դ������
			wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
					| PowerManager.FULL_WAKE_LOCK, "bright"); // wakelock��
			wl.setReferenceCounted(false);
			wl.acquire();// ���������������õ��������Ա���ע��
			inLockAcquireTime = true;
			Log.v("123456789", "inLockAcquireTime Ϊ true");
			timer = new Timer();
			timer.schedule(new TimerTask() { // ����timer�ӳ�15��ִ�У�15��֮��ע��wl��
						@Override
						public void run() {
							wl.release();
							inLockAcquireTime = false;
							Log.v("123456789", "inLockAcquireTime Ϊ false");
						}
					}, 15000);
			Log.v("123456789", "�������� light");
		}
	}

	public static boolean isRunning() { // ���ط���״̬
		return state;
	}
}
