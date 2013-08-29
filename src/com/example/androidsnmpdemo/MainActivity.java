package com.example.androidsnmpdemo;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.R.string;
import android.app.Activity;

import android.view.Menu;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Handler handler = new Handler() {

		private Spinner spinner;
		private ArrayAdapter adapter;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				// search device
				@SuppressWarnings("unchecked")
				ArrayList<String> foundList = (ArrayList<String>) msg.obj;
				((TextView) findViewById(R.id.textView2))
						.setText("found hosts:" + foundList.size());
				spinner = (Spinner) findViewById(R.id.spinnerDevices);
				// 绑定要显示的texts
				 adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, foundList);
				 spinner.setAdapter(adapter);
				break;

			default:
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String localIP = IpUtil.getIPAddress(true);
		// IpUtil.getMACAddress("eth0");

		setContentView(R.layout.activity_main);
		final EditText editTextLocalIp = (EditText) findViewById(R.id.editTextLocalIP);
		editTextLocalIp.setText(localIP);
		Button button1 = (Button) findViewById(R.id.searchDevicesButton);

		final TextView textView2 = (TextView) findViewById(R.id.textView2);

		button1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String LocIP = editTextLocalIp.getText().toString();
				searchSnmpDevices(LocIP);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void searchSnmpDevices(final String LocalIP) {
		new Thread() {
			@Override
			public void run() {
				SnmpService snmp = new SnmpService();
				ArrayList<String> foundedDevices = snmp.findSnmpDevicesList(LocalIP);
				Message msg = new Message();
				msg.what = 0;
				msg.obj = foundedDevices;
				handler.sendMessage(msg);
			}
		}.start();

	}
}