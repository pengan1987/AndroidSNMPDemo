package com.example.androidsnmpdemo;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.R.string;
import android.app.Activity;
import android.content.Intent;

import android.view.Menu;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Handler handler = new Handler() {

		private Spinner spinner;
		@SuppressWarnings("rawtypes")
		private ArrayAdapter adapter;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				// search device
				@SuppressWarnings("unchecked")
				ArrayList<String> foundList = (ArrayList<String>) msg.obj;
				((TextView) findViewById(R.id.textMessage))
						.setText("found hosts:" + foundList.size());
				spinner = (Spinner) findViewById(R.id.spinnerDevices);

				adapter = new ArrayAdapter<String>(MainActivity.this,
						android.R.layout.simple_spinner_item, foundList);
				if (adapter.getCount() > 0) {
					spinner.setEnabled(true);
					spinner.setAdapter(adapter);
				} else {
					spinner.setEnabled(false);
					spinner.setAdapter(null);
				}
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
		Spinner spinnerDevices = (Spinner) findViewById(R.id.spinnerDevices);
		spinnerDevices.setEnabled(false);

		final EditText editTextLocalIp = (EditText) findViewById(R.id.editTextLocalIP);
		editTextLocalIp.setText(localIP);
		Button ButtonSeachDevices = (Button) findViewById(R.id.buttonSearchDevices);
		Button ButtonStartControl = (Button) findViewById(R.id.buttonStartControl);

		final TextView textMessage = (TextView) findViewById(R.id.textMessage);

		ButtonSeachDevices.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String LocIP = editTextLocalIp.getText().toString();
				searchSnmpDevices(LocIP);
			}
		});
		ButtonStartControl.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intentForControl = new Intent();
				intentForControl.setClass(MainActivity.this,
						ControlActivity.class);

				Spinner spinnerDevices = (Spinner) findViewById(R.id.spinnerDevices);

				if (spinnerDevices.isEnabled()) {

					// This is for Debug Only
					// intentForControl.putExtra("TargetIP", "192.168.99.9");
					// this is for REAL device
					String TargetIP = spinnerDevices.getSelectedItem()
							.toString();
					intentForControl.putExtra("TargetIP", TargetIP);
					startActivity(intentForControl);
				} else {

					Toast.makeText(MainActivity.this,
							"No Target Device Selected", Toast.LENGTH_SHORT)
							.show();
					// This is for Debug Only
					intentForControl.putExtra("TargetIP", "192.168.99.9");
					startActivity(intentForControl);

				}
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
				ArrayList<String> foundedDevices = snmp
						.findSnmpDevicesList(LocalIP);
				Message msg = new Message();
				msg.what = 0;
				msg.obj = foundedDevices;
				handler.sendMessage(msg);
				snmp = null;
			}
		}.start();

	}
}