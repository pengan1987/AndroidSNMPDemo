package com.example.androidsnmpdemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
			if (pduContent.contains(":")){
			String preResult = pduContent
					.substring(pduContent.indexOf(":") + 2);
			if (preResult.length() >= 5)
				result.add(preResult);}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}
}
