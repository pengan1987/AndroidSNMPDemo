package com.example.androidsnmpdemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.view.Menu;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			((TextView) findViewById(R.id.textView1)).setText("thread done");
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button button1 = (Button) findViewById(R.id.button1);
		final TextView textView1 = (TextView) findViewById(R.id.textView1);
		final TextView textView2 = (TextView) findViewById(R.id.textView2);

		button1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				searchSnmpDevices();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void searchSnmpDevices() {
		new Thread() {
			@Override
			public void run() {
				SnmpService snmp = new SnmpService();
				snmp.findSnmpDevicesList("192.168.1.22");
				handler.sendEmptyMessage(0);
			}
		}.start();

	}
}