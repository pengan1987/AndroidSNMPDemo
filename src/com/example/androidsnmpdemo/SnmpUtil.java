package com.example.androidsnmpdemo;

import java.io.IOException;
import java.util.Vector;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SnmpUtil {

	private Snmp snmp = null;
	
	DefaultUdpTransportMapping transport = null;
	private Address targetAddress = null;
	PDU pdu = new PDU();

	public void initComm(String targetAddressString) throws IOException {

		
		targetAddress = GenericAddress.parse("udp:" + targetAddressString
				+ "/161");
		transport = new DefaultUdpTransportMapping();
		snmp = new Snmp(transport);
		transport.listen();
	}

	public ResponseEvent sendPDU(PDU pdu) throws IOException {
	
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("public"));
		target.setAddress(targetAddress);
		// ͨ�Ų��ɹ�ʱ�����Դ���
		target.setRetries(2);
		// ��ʱʱ��
		target.setTimeout(1500);
		target.setVersion(SnmpConstants.version1);
		// ��Agent����PDU��������Response
		return snmp.send(pdu, target);
	}

	public void setOctetStringPDU(String sOID, String value) throws IOException {
		// set PDU

		pdu.add(new VariableBinding(new OID(sOID), new OctetString(value)));
		pdu.setType(PDU.SET);
		sendPDU(pdu);
		pdu.clear();
	}

	public void setIntegerPDU(String sOID, Integer value) throws IOException {
		// set PDU

		pdu.add(new VariableBinding(new OID(sOID), new Integer32(value)));
		pdu.setType(PDU.SET);
		sendPDU(pdu);
		pdu.clear();
	}

	public String getPDU(String sOID) throws IOException {
		// get PDU
		pdu.clear();
		pdu.add(new VariableBinding(new OID(sOID)));
		pdu.setType(PDU.GET);
		return readResponse(sendPDU(pdu));
	}

	public String readResponse(ResponseEvent respEvnt) {
		// ����Response
		String result = "";
		if (respEvnt != null && respEvnt.getResponse() != null) {
			@SuppressWarnings("unchecked")
			Vector<VariableBinding> recVBs = (Vector<VariableBinding>) respEvnt
					.getResponse().getVariableBindings();
			for (int i = 0; i < recVBs.size(); i++) {
				VariableBinding recVB = recVBs.elementAt(i);
				// System.out
				// .println(recVB.getOid() + " : " + recVB.getVariable());
				result = recVB.getOid() + " : " + recVB.getVariable();
			}
		}
		return result;
	}

	
}