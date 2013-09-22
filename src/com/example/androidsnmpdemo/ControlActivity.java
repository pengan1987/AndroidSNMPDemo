package com.example.androidsnmpdemo;

import java.lang.ref.WeakReference;

import afzkl.development.colorpickerview.dialog.ColorPickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import android.util.Log;
import android.view.Menu;
import android.view.View;


import android.widget.Button;

import android.widget.CompoundButton;


import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ControlActivity extends Activity {

	private static String TargetIP;
	private static Boolean MonitorActive;
	private static Integer valueRed = 25;
	private static Integer valueGreen = 25;
	private static Integer valueBlue = 25;
	private static Boolean luminosityMonEnabled = true;
	private static Boolean UltraSndMonEnabled = false;

	static class MyHandler extends Handler {

		WeakReference<ControlActivity> mActivity;

		MyHandler(ControlActivity activity) {
			mActivity = new WeakReference<ControlActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			ControlActivity theActivity = mActivity.get();
			switch (msg.what) {
			case 2:
				Integer lux = (Integer) msg.obj;
				TextView textViewLuminosity = (TextView) theActivity
						.findViewById(R.id.textViewLuminosityValue);
				textViewLuminosity.setText(lux.toString());
				break;
			case 3:
				Integer dist = (Integer) msg.obj;
				TextView textViewDistence = (TextView) theActivity
						.findViewById(R.id.textViewObjDistenceValue);
				textViewDistence.setText(dist.toString());


				if (dist > 400) {
					((ToggleButton) theActivity
							.findViewById(R.id.toggleButtonPower))
							.setChecked(true);
				} else if (dist < 200) {
					((ToggleButton) theActivity
							.findViewById(R.id.toggleButtonPower))
							.setChecked(false);

				}

			case 4:
				Integer pickedcolor = (Integer)msg.obj;
				int r = (pickedcolor >> 16) & 0xFF;
				int g = (pickedcolor >> 8) & 0xFF;
				int b = (pickedcolor >> 0) & 0xFF;
				
				theActivity.setColorBars(r, g, b);
				theActivity.changeDeviceColor();
			default:
				break;
			}
			theActivity = null;
		}

	};

	MyHandler handler = new MyHandler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control);

		TargetIP = this.getIntent().getStringExtra("TargetIP");

		final ToggleButton toggleButtonPower = (ToggleButton) findViewById(R.id.toggleButtonPower);
		final ToggleButton toggleButtonUltraSnd = (ToggleButton) findViewById(R.id.toggleButtonUltraSnd);
		final Button ButtonColor = (Button) findViewById(R.id.buttonColor);
		TextView textViewTargetIp = (TextView) findViewById(R.id.textViewTargetIp);
		textViewTargetIp.setText("Target IP: " + TargetIP);
		MonitorActive = true;
		startMonitors();

		toggleButtonPower
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						changeSwich(toggleButtonPower.isChecked());

					}
				});

		toggleButtonUltraSnd.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				UltraSndMonEnabled = toggleButtonUltraSnd.isChecked();
				Log.i("ultrasndchange", UltraSndMonEnabled.toString());
			}
		});

		ButtonColor.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onClickColorPickerDialog();
			}
		});
		
		SeekBar seekBarRed = (SeekBar) findViewById(R.id.seekBarRed);
		SeekBar seekBarGreen = (SeekBar) findViewById(R.id.seekBarGreen);
		SeekBar seekBarBlue = (SeekBar) findViewById(R.id.seekBarBlue);

		seekBarRed.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBarRed, int progress,
					boolean fromUser) {
				valueRed = progress;

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBarRed) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBarRed) {

				changeDeviceColor();

			}
		});

		seekBarGreen.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBarGreen, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				valueGreen = progress;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBarGreen) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBarGreen) {
				changeDeviceColor();
			}
		});

		seekBarBlue.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBarBlue, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				valueBlue = progress;
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBarBlue) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBarBlue) {
				changeDeviceColor();

			}
		});

	}

	protected void changeDeviceColor() {
		String colorString = "Red:" + valueRed.toString() + " Green:"
				+ valueGreen.toString() + " Blue:" + valueBlue.toString();
		TextView textViewColor = (TextView) findViewById(R.id.textViewColor);
		textViewColor.setText(colorString);
		new Thread() {
			@Override
			public void run() {
				SnmpService snmp = new SnmpService(TargetIP);
				try {
					snmp.setSnmpInteger("1.3.6.1.4.1.36582.1.3", valueRed);
					sleep(100);
					snmp.setSnmpInteger("1.3.6.1.4.1.36582.1.5", valueGreen);
					sleep(100);
					snmp.setSnmpInteger("1.3.6.1.4.1.36582.1.6", valueBlue);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				snmp = null;
			}
		}.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		MonitorActive = false;
		UltraSndMonEnabled = false;
		finish();

	}

	private void startMonitors() {
		new Thread() {
			@Override
			public void run() {
				SnmpService snmp = new SnmpService(TargetIP);
				while (MonitorActive) {

					try {
						if (luminosityMonEnabled) {
							Integer result = snmp
									.getSnmpInteger("1.3.6.1.4.1.36582.2.0");
							Message msg = new Message();
							msg.what = 2;
							msg.obj = result;
							handler.sendMessage(msg);

							sleep(500);
						}
						if (UltraSndMonEnabled) {
							Integer result = snmp
									.getSnmpInteger("1.3.6.1.4.1.36582.3.1");
							Message msg = new Message();
							msg.what = 3;
							msg.obj = result;
							handler.sendMessage(msg);

							sleep(300);
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				snmp = null;
			}
		}.start();

	}

	private void changeSwich(final boolean checked) {

		new Thread() {
			@Override
			public void run() {
				SnmpService snmp = new SnmpService(TargetIP);
				snmp.setSnmpSwitch("1.3.6.1.4.1.36582.1.0", checked);
				snmp = null;
			}
		}.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.control, menu);
		return true;
	}
	public void onClickColorPickerDialog() {
		
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int initialValue = prefs.getInt("color_2", 0xFF303030);
		
		Log.d("mColorPicker", "initial value:" + initialValue);
				
		final ColorPickerDialog colorDialog = new ColorPickerDialog(this, initialValue);
		
		colorDialog.setAlphaSliderVisible(false);
		colorDialog.setTitle("Pick a Color");
		
		colorDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {

				String colorString = colorToHexString(colorDialog.getColor());
				Toast.makeText(ControlActivity.this, "Selected Color: " + colorString, Toast.LENGTH_LONG).show();
							
				//Save the value in our preferences.
				SharedPreferences.Editor editor = prefs.edit();
				editor.putInt("color_2", colorDialog.getColor());
				editor.commit();
				
				Message msg = new Message();
				msg.what = 4;
				
				msg.obj = colorDialog.getColor();
				handler.sendMessage(msg);
			}
		});
		
		colorDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Nothing to do here.
			}
		});
		
		colorDialog.show();
	}
	
	private String colorToHexString(int color) {
		return String.format("#%06X", 0xFFFFFFFF & color);
	}
	private void setColorBars(int Red, int Green, int Blue){
		SeekBar seekBarRed = (SeekBar) findViewById(R.id.seekBarRed);
		SeekBar seekBarGreen = (SeekBar) findViewById(R.id.seekBarGreen);
		SeekBar seekBarBlue = (SeekBar) findViewById(R.id.seekBarBlue);
		seekBarRed.setProgress(Red);
		seekBarGreen.setProgress(Green);
		seekBarBlue.setProgress(Blue);
	}
}
