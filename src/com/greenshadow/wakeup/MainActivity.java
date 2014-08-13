/**
 * 交互界面
 * */
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
import android.widget.CheckBox;
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
	private CheckBox checkBox;

	private Intent serviceIntent;

	/**
	 * 存储SharedPreferences设置数据，文件位置 /data/data/包名/shared_prefs/*.xml
	 * 
	 * 设置数据有三个：BOOT_START(boolean)判断开机是否自启；number(int)记录接近次数；sensitivityValue(
	 * long)记录灵敏度阀值
	 * 
	 * SharedPreferences中存数的数据为屏幕显示的数据，不是控件返回的值
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
		checkBox = (CheckBox) findViewById(R.id.check_box);

		settings = getSharedPreferences("settings", Activity.MODE_PRIVATE);

		if (SensorsService.isRunning())
			switcher.setChecked(true);
		switcher.setOnCheckedChangeListener(new OnCheckedChangeListener() {// 开关变化监听器
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
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
											startService(serviceIntent);// 打开服务
											FlushSharedPreferences(false, true);
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
					FlushSharedPreferences(false, false);
				}
			}
		});
		checkBox.setChecked(settings.getBoolean("notification", true));
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					if (SensorsService.isRunning())
						startService(serviceIntent);
					FlushSharedPreferences(false, SensorsService.isRunning());
				} else {
					if (SensorsService.isRunning())
						startService(serviceIntent);
					FlushSharedPreferences(false, SensorsService.isRunning());
				}
			}
		});
		seekBar.setProgress((int) settings.getLong("sensitivityValue", 500));
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {// 拖动条改变监听器
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) { // 数据变化时
				sensitivity.setText("" + (progress));
			}
		});
		sensitivity.setText("" + (seekBar.getProgress())); // 启动时设置SeekBar右边TextBox的值
		spinner.setSelection(settings.getInt("number", 4) - 2);// 同上
	}

	@Override
	protected void onDestroy() { // 结束activity
		if (!SensorsService.isRunning())
			System.exit(0);
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
			if (SensorsService.isRunning()) // 如果服务正在运行则重新调用服务的onStartCommand方法
				startService(serviceIntent);
			FlushSharedPreferences(false, SensorsService.isRunning());
		}
	}

	public void restore_ButtonClick(View v) {// 重置设置按钮
		if (v.getId() == R.id.button_restore) {
			seekBar.setProgress(500);
			spinner.setSelection(2);
			FlushSharedPreferences(true, SensorsService.isRunning());
		}
	}

	/**
	 * 刷新SharedPreferences中的数据
	 * 
	 * @param clear
	 *            是否为清除模式
	 * @param boot
	 *            开机自启模式
	 * */
	private void FlushSharedPreferences(boolean clear, boolean boot) {
		Editor editor = settings.edit();
		if (clear) {
			editor.clear(); // 清空
			editor.putBoolean("BOOT_START", SensorsService.isRunning()); // 将自启状态改为目前服务运行状态
		} else {
			editor.putInt("number", spinner.getSelectedItemPosition() + 2); // 接近次数
			editor.putLong("sensitivityValue", seekBar.getProgress()); // 灵敏度阀值
			editor.putBoolean("BOOT_START", boot); // 开机自启状态
			editor.putBoolean("notification", checkBox.isChecked());
		}
		editor.commit(); // 提交
	}
}
