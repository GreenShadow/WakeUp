package com.greenshadow.wakeup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static MainActivity mainActivity;// 传递MainActivity的Context

	private Switch switcher;
	private SeekBar seekBar;
	private TextView sensitivity;
	private Spinner spinner;

	private int number;// 接近次数
	private long sensitivityValue;// 灵敏度阀值（seekBar值）
	private Intent serviceIntent;

	/**
	 * 存储SharedPreferences设置数据，文件位置 /data/data/包名/shared_prefs/*.xml
	 * 
	 * 设置数据有三个：BOOT_START(boolean)判断开机是否自启 number(int)记录接近次数
	 * sensitivityValue(long)记录灵敏度阀值
	 * */
	private SharedPreferences settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mainActivity = MainActivity.this;
		serviceIntent = new Intent(this, SensorsService.class);

		switcher = (Switch) findViewById(R.id.switcher);
		seekBar = (SeekBar) findViewById(R.id.seek_bar);
		sensitivity = (TextView) findViewById(R.id.sensitivity_value);
		spinner = (Spinner) findViewById(R.id.spinner);

		settings = getSharedPreferences("settings", Activity.MODE_PRIVATE);// 建立设置数据文件，命名为settings.xml
		seekBar.setProgress((int) settings.getLong("sensitivityValue", 500));// 从设置文件中读取，若不存在默认500
		spinner.setSelection(settings.getInt("number", 4) - 2);// 同上
		if (SensorsService.isRunning())// 判断服务状态
			switcher.setChecked(true);
		switcher.setOnCheckedChangeListener(new OnCheckedChangeListener() {// 开关变化监听器
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					final boolean isChecked) {
				if (isChecked) {
					new AlertDialog.Builder(mainActivity)
							// 展开一个对话框
							.setView(
									getLayoutInflater().inflate(
											R.layout.alter_dialog_view_tips,
											null))
							.setPositiveButton("确定", new OnClickListener() { // 确定的点击监听
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											number = spinner
													.getSelectedItemPosition() + 2;
											sensitivityValue = seekBar
													.getProgress();

											Editor editor = settings.edit();// 以下几行为写入设置数据文件
											editor.putInt("number", number);
											editor.putLong("sensitivityValue",
													sensitivityValue);
											editor.putBoolean("BOOT_START",
													true);
											editor.commit();// 保存文件

											startService(serviceIntent);// 打开服务
										}
									})
							.setNegativeButton("取消", new OnClickListener() { // 取消的点击监听
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											switcher.setChecked(false);
										}
									})
							.setCancelable(false)
							.setOnKeyListener(
									new DialogInterface.OnKeyListener() {
										@Override
										public boolean onKey(
												DialogInterface dialog,
												int keyCode, KeyEvent event) {
											if (keyCode == KeyEvent.KEYCODE_BACK) {
												dialog.dismiss();
												switcher.setChecked(false);
											}
											return false;
										}
									}).show();
				} else {
					stopService(serviceIntent);

					Editor editor = settings.edit();
					editor.putBoolean("BOOT_START", false); // 将开机自启改为false
					editor.commit();
				}
			}
		});

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {// 拖动条改变监听器
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { // 停止拖动时 必须重写的方法
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { // 开始拖动时 必须重写的方法
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) { // 数据变化时
				sensitivity.setText("" + (progress));
			}
		});

		sensitivity.setText("" + (seekBar.getProgress())); // 启动时设置SeekBar右边TextBox的值
	}

	@Override
	protected void onDestroy() { // 结束activity
		Log.v("123456789", "onDestroy");
		super.onDestroy();
	}

	public void sensitivityHelp_ButtonClick(View v) { // 灵敏度帮助按钮
		if (v.getId() == R.id.button_sensitivity_help) {
			new AlertDialog.Builder(this)
					.setView(
							getLayoutInflater()
									.inflate(
											R.layout.alter_dialog_view_sensitivity_help,
											null))
					.setPositiveButton("确定", null).show();
		}
	}

	public void up_ButtonClick(View v) {// 灵敏度+1按钮
		if (v.getId() == R.id.button_up) {
			seekBar.setProgress(seekBar.getProgress() + 1);
		}
	}

	public void down_ButtonClick(View v) {// 灵敏度-1按钮
		if (v.getId() == R.id.button_down) {
			seekBar.setProgress(seekBar.getProgress() - 1);
		}
	}

	public void numberHelp_ButtonClick(View v) {// 接近次数帮助按钮
		if (v.getId() == R.id.button_number_help) {
			new AlertDialog.Builder(this)
					.setView(
							getLayoutInflater().inflate(
									R.layout.alter_dialog_view_number_help,
									null)).setPositiveButton("确定", null).show();
		}
	}

	public void save_ButtonClick(View v) {// 保存设置按钮
		if (v.getId() == R.id.button_save) {
			number = spinner.getSelectedItemPosition() + 2;
			sensitivityValue = seekBar.getProgress();
			if (SensorsService.isRunning()) // 如果服务正在运行则重新调用服务的onStartCommand方法
				startService(serviceIntent);

			Editor editor = settings.edit();
			editor.putInt("number", spinner.getSelectedItemPosition() + 2);
			editor.putLong("sensitivityValue", seekBar.getProgress());
			editor.putBoolean("BOOT_START", SensorsService.isRunning());
			editor.commit();
		}
	}

	public void restore_ButtonClick(View v) {// 重置设置按钮
		if (v.getId() == R.id.button_restore) {
			seekBar.setProgress(500);
			spinner.setSelection(2);
			Editor editor = settings.edit();
			editor.clear();
			editor.putBoolean("BOOT_START", SensorsService.isRunning()); // 将自启状态改为目前服务运行状态
			editor.commit();
		}
	}
}
