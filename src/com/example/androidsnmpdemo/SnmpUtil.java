package com.example.androidsnmpdemo;

import java.io.IOException;
import java.util.Vector;
 
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import android.R.string;
 
public class SnmpUtil {
 
       private Snmp snmp = null;
 
       private Address targetAddress = null;
 
       public void initComm(String targetAddressString) throws IOException {
             
              // 设置Agent方的IP和端口
              targetAddress = GenericAddress.parse("udp:"+targetAddressString+"/161");
              TransportMapping transport = new DefaultUdpTransportMapping();
              snmp = new Snmp(transport);
              transport.listen();
       }
 
       public ResponseEvent sendPDU(PDU pdu) throws IOException {
              // 设置 target
              CommunityTarget target = new CommunityTarget();
              target.setCommunity(new OctetString("public"));
              target.setAddress(targetAddress);
              // 通信不成功时的重试次数
              target.setRetries(2);
              // 超时时间
              target.setTimeout(1500);
              target.setVersion(SnmpConstants.version1);
              // 向Agent发送PDU，并返回Response
              return snmp.send(pdu, target);
       }
      
       public void setPDU() throws IOException {
              // set PDU
              PDU pdu = new PDU();
              pdu.add(new VariableBinding(new OID(new int[] { 1, 3, 6, 1, 2, 1, 1, 5, 0 }), new OctetString("SNMPTEST")));
              pdu.setType(PDU.SET);
              sendPDU(pdu);
       }
      
       public String getPDU(String sOID) throws IOException {
              // get PDU
              PDU pdu = new PDU();
              pdu.add(new VariableBinding(new OID(sOID)));
              pdu.setType(PDU.GET);
              return readResponse(sendPDU(pdu));
       }
      
       public String readResponse(ResponseEvent respEvnt) {
              // 解析Response
    	   String result = "";
              if (respEvnt != null && respEvnt.getResponse() != null) {
                     Vector<VariableBinding> recVBs = (Vector<VariableBinding>) respEvnt.getResponse().getVariableBindings();
                     for (int i = 0; i < recVBs.size(); i++) {
                            VariableBinding recVB = recVBs.elementAt(i);
                            System.out.println(recVB.getOid() + " : " + recVB.getVariable());
                            result = recVB.getOid() + " : " + recVB.getVariable();
                     }
              }
              return result;
       }
      
      /* public static void getProp(String targetAddressString,String OID) {
              try {
                     SnmpUtil util = new SnmpUtil();
                     util.initComm(targetAddressString);
                  //   util.setPDU();
                     util.getPDU();
              } catch (IOException e) {
                     e.printStackTrace();
              }
       }*/
}