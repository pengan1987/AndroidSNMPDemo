package com.example.androidsnmpdemo;

import java.io.IOException;
import java.util.ArrayList;


public class SnmpService {
	String tag = "SnmpService";
	SnmpUtil util;

	public ArrayList<String> findSnmpDevicesList(String currentIP) {
		ArrayList<String> result = new ArrayList<String>();
		SnmpUtil util = new SnmpUtil();
		String BroadCastIP = currentIP.substring(0,
				currentIP.lastIndexOf(".") + 1) + "255";

		try {

			util.initComm(BroadCastIP);
			String pduContent = util.getPDU("1.3.6.1.2.1.4.20.1.1");
			if (pduContent.contains(":")) {
				String preResult = pduContent
						.substring(pduContent.indexOf(":") + 2);
				if (preResult.length() >= 5)
					result.add(preResult);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public void setSnmpSwitch(String deviceIP,String OID, boolean switcher) {
		SnmpUtil util = new SnmpUtil();
		try {
			util.initComm(deviceIP);
			int power = 2;
			if (switcher)
				power = 1;

			util.setIntegerPDU(OID, power);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public Integer getSnmpInteger(String deviceIP,String OID) {
		Integer result = 0;
		SnmpUtil util = new SnmpUtil();
		
		try {

			util.initComm(deviceIP);
			String pduContent = util.getPDU(OID);
			if (pduContent.contains(":")) {
				String preResult = pduContent
						.substring(pduContent.indexOf(":") + 2);
				result = Integer.parseInt(preResult);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}
}
