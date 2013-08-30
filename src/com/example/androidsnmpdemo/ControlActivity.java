package com.example.androidsnmpdemo;

import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.app.Activity;
import android.view.Menu;
import android.view.View;

import android.widget.TextView;
import android.widget.ToggleButton;

public class ControlActivity extends Activity {

	private static String TargetIP;
	private static Boolean luminosityMonitorActive;

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
				TextView textViewTargetIp = (TextView) theActivity
						.findViewById(R.id.textViewLuminosityValue);
				textViewTargetIp.setText(lux.toString());
				break;

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

		Bundle bundle = this.getIntent().getExtras();
		TargetIP = (String) bundle.get("TargetIP");

		final ToggleButton toggleButtonPower = (ToggleButton) findViewById(R.id.toggleButtonPower);
		TextView textViewTargetIp = (TextView) findViewById(R.id.textViewTargetIp);
		textViewTargetIp.setText("Target IP: " + TargetIP);
		luminosityMonitorActive = true;
		startLuminosityMonitor();
		toggleButtonPower.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				changeSwich(toggleButtonPower.isChecked());
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		luminosityMonitorActive = false;
		finish();

	}

	private void startLuminosityMonitor() {
		new Thread() {
			@Override
			public void run() {
				while (luminosityMonitorActive) {
					SnmpService snmp = new SnmpService();
					Integer result = snmp.getSnmpInteger(TargetIP,
							"1.3.6.1.4.1.36582.2.0");
					Message msg = new Message();
					msg.what = 2;
					msg.obj = result;
					handler.sendMessage(msg);
					try {
						sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();

	}

	private void changeSwich(final boolean checked) {

		new Thread() {
			@Override
			public void run() {
				SnmpService snmp = new SnmpService();
				snmp.setSnmpSwitch(TargetIP, "1.3.6.1.4.1.36582.1.0", checked);
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
