package com.example.androidsnmpdemo;

import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
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
			default:
				break;
			}
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
		TextView textViewTargetIp = (TextView) findViewById(R.id.textViewTargetIp);
		textViewTargetIp.setText("Target IP: " + TargetIP);
		MonitorActive = true;
		startMonitors();

		toggleButtonPower.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
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

							sleep(500);
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				snmp=null;
			}
		}.start();

	}

	private void changeSwich(final boolean checked) {

		new Thread() {
			@Override
			public void run() {
				SnmpService snmp = new SnmpService(TargetIP);
				snmp.setSnmpSwitch("1.3.6.1.4.1.36582.1.0", checked);
				snmp=null;
			}
		}.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.control, menu);
		return true;
	}

}
