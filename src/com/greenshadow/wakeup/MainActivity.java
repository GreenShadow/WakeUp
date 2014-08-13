/**
 * ��������
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

	public static MainActivity mainActivity;// ����MainActivity��Context

	private Switch switcher;
	private SeekBar seekBar;
	private TextView sensitivity;
	private Spinner spinner;
	private CheckBox checkBox;

	private Intent serviceIntent;

	/**
	 * �洢SharedPreferences�������ݣ��ļ�λ�� /data/data/����/shared_prefs/*.xml
	 * 
	 * ����������������BOOT_START(boolean)�жϿ����Ƿ�������number(int)��¼�ӽ�������sensitivityValue(
	 * long)��¼�����ȷ�ֵ
	 * 
	 * SharedPreferences�д���������Ϊ��Ļ��ʾ�����ݣ����ǿؼ����ص�ֵ
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
		switcher.setOnCheckedChangeListener(new OnCheckedChangeListener() {// ���ر仯������
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					new AlertDialog.Builder(mainActivity)
							// չ��һ���Ի���
							.setView(
									getLayoutInflater().inflate(
											R.layout.alter_dialog_view_tips,
											null))
							.setPositiveButton("ȷ��", new OnClickListener() { // ȷ���ĵ������
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											startService(serviceIntent);// �򿪷���
											FlushSharedPreferences(false, true);
										}
									})
							.setNegativeButton("ȡ��", new OnClickListener() { // ȡ���ĵ������
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
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {// �϶����ı������
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) { // ���ݱ仯ʱ
				sensitivity.setText("" + (progress));
			}
		});
		sensitivity.setText("" + (seekBar.getProgress())); // ����ʱ����SeekBar�ұ�TextBox��ֵ
		spinner.setSelection(settings.getInt("number", 4) - 2);// ͬ��
	}

	@Override
	protected void onDestroy() { // ����activity
		if (!SensorsService.isRunning())
			System.exit(0);
		Log.v("123456789", "onDestroy");
		super.onDestroy();
	}

	public void sensitivityHelp_ButtonClick(View v) { // �����Ȱ�����ť
		if (v.getId() == R.id.button_sensitivity_help) {
			new AlertDialog.Builder(this)
					.setView(
							getLayoutInflater()
									.inflate(
											R.layout.alter_dialog_view_sensitivity_help,
											null))
					.setPositiveButton("ȷ��", null).show();
		}
	}

	public void up_ButtonClick(View v) {// ������+1��ť
		if (v.getId() == R.id.button_up) {
			seekBar.setProgress(seekBar.getProgress() + 1);
		}
	}

	public void down_ButtonClick(View v) {// ������-1��ť
		if (v.getId() == R.id.button_down) {
			seekBar.setProgress(seekBar.getProgress() - 1);
		}
	}

	public void numberHelp_ButtonClick(View v) {// �ӽ�����������ť
		if (v.getId() == R.id.button_number_help) {
			new AlertDialog.Builder(this)
					.setView(
							getLayoutInflater().inflate(
									R.layout.alter_dialog_view_number_help,
									null)).setPositiveButton("ȷ��", null).show();
		}
	}

	public void save_ButtonClick(View v) {// �������ð�ť
		if (v.getId() == R.id.button_save) {
			if (SensorsService.isRunning()) // ��������������������µ��÷����onStartCommand����
				startService(serviceIntent);
			FlushSharedPreferences(false, SensorsService.isRunning());
		}
	}

	public void restore_ButtonClick(View v) {// �������ð�ť
		if (v.getId() == R.id.button_restore) {
			seekBar.setProgress(500);
			spinner.setSelection(2);
			FlushSharedPreferences(true, SensorsService.isRunning());
		}
	}

	/**
	 * ˢ��SharedPreferences�е�����
	 * 
	 * @param clear
	 *            �Ƿ�Ϊ���ģʽ
	 * @param boot
	 *            ��������ģʽ
	 * */
	private void FlushSharedPreferences(boolean clear, boolean boot) {
		Editor editor = settings.edit();
		if (clear) {
			editor.clear(); // ���
			editor.putBoolean("BOOT_START", SensorsService.isRunning()); // ������״̬��ΪĿǰ��������״̬
		} else {
			editor.putInt("number", spinner.getSelectedItemPosition() + 2); // �ӽ�����
			editor.putLong("sensitivityValue", seekBar.getProgress()); // �����ȷ�ֵ
			editor.putBoolean("BOOT_START", boot); // ��������״̬
			editor.putBoolean("notification", checkBox.isChecked());
		}
		editor.commit(); // �ύ
	}
}
