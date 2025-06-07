package com.example.netmonitor.service;

import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SnmpService {

	public String getAsString(String ipAddress, int port, String readCommunity, String oid, String snmpVersion) throws IOException {
        String addressStr = "udp:" + ipAddress + "/" + port;
        Address targetAddress = GenericAddress.parse(addressStr);
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

		CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(readCommunity));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(1500);

        // Set version
        switch (snmpVersion) {
            case "1":
                target.setVersion(SnmpConstants.version1);
                break;
            case "2c":
                target.setVersion(SnmpConstants.version2c);
                break;
            case "3":
                transport.close();
                throw new UnsupportedOperationException("SNMP v3 is not supported yet.");
            default:
                target.setVersion(SnmpConstants.version2c);
                break;
        }

        PDU pdu = new PDU();
        String cleanOid = oid.trim().replaceAll("[^0-9.]", "");
        pdu.add(new VariableBinding(new OID(cleanOid)));
        pdu.setType(PDU.GET);

        Snmp snmp = new Snmp(transport);
		ResponseEvent responseEvent = snmp.get(pdu, target);

        if (responseEvent != null && responseEvent.getResponse() != null) {
            VariableBinding vb = responseEvent.getResponse().get(0);
            Variable var = vb.getVariable();

            snmp.close();

            if (oid.equals("1.3.6.1.2.1.1.3.0") && var instanceof TimeTicks) {
                TimeTicks timeTicks = (TimeTicks) var;
                long seconds = timeTicks.toLong() / 100;
                return String.valueOf(seconds);
            }
            
            if (var instanceof OctetString) {
                OctetString octetString = (OctetString) var;
                byte[] valueBytes = octetString.getValue();

                // Convertim byteîn șir ASCII
                StringBuilder sb = new StringBuilder();
                for (byte b : valueBytes) {
                    char c = (char) (b & 0xFF);
                    if (c >= 32 && c <= 126) {
                        sb.append(c);
                    } else {
                        sb.append('.');
                    }
                }

                return sb.toString().trim();
            }
            
            return var.toString();
        } else {
            snmp.close();
            throw new IOException("No response from SNMP agent.");
        }
    }

    public String getNext(String ipAddress, int port, String readCommunity, String oid, String snmpVersion) throws IOException {
        return performSnmpOperation(ipAddress, port, readCommunity, null, oid, snmpVersion, PDU.GETNEXT);
    }

    public String walk(String ipAddress, int port, String readCommunity, String oid, String snmpVersion) throws IOException {
        StringBuilder sb = new StringBuilder();
        String addressStr = "udp:" + ipAddress + "/" + port;
        Address targetAddress = GenericAddress.parse(addressStr);
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(readCommunity));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(snmpVersion.equals("1") ? SnmpConstants.version1 : SnmpConstants.version2c);

        Snmp snmp = new Snmp(transport);
        OID currentOid = new OID(oid);

        while (true) {
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(currentOid));
            pdu.setType(PDU.GETNEXT);

            ResponseEvent responseEvent = snmp.getNext(pdu, target);
            if (responseEvent == null || responseEvent.getResponse() == null)
                break;

            VariableBinding vb = responseEvent.getResponse().get(0);
            if (vb.getOid() == null || !vb.getOid().startsWith(new OID(oid))) {
                break;
            }

            sb.append(vb.getOid().toString()).append(" = ").append(vb.getVariable().toString()).append("\n");
            currentOid = vb.getOid();
        }

        snmp.close();
        return sb.toString().trim();
    }

    public String set(String ipAddress, int port, String writeCommunity, String oid, String value, String snmpVersion) throws IOException {
        return performSnmpOperation(ipAddress, port, writeCommunity, value, oid, snmpVersion, PDU.SET);
    }

    private String performSnmpOperation(String ip, int port, String community, String value, String oid, String snmpVersion, int pduType) throws IOException {
        String addressStr = "udp:" + ip + "/" + port;
        Address targetAddress = GenericAddress.parse(addressStr);
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(snmpVersion.equals("1") ? SnmpConstants.version1 : SnmpConstants.version2c);

        PDU pdu = new PDU();
        OID targetOid = new OID(oid);

        if (pduType == PDU.SET) {
            Variable var = new OctetString(value);
            pdu.add(new VariableBinding(targetOid, var));
        } else {
            pdu.add(new VariableBinding(targetOid));
        }

        pdu.setType(pduType);
        Snmp snmp = new Snmp(transport);

        ResponseEvent responseEvent = (pduType == PDU.SET) ? snmp.set(pdu, target)
                : (pduType == PDU.GETNEXT) ? snmp.getNext(pdu, target) : snmp.get(pdu, target);

        snmp.close();

        if (responseEvent != null && responseEvent.getResponse() != null) {
            VariableBinding vb = responseEvent.getResponse().get(0);
            return vb.getVariable().toString();
        }

        throw new IOException("No response from SNMP agent.");
    }

    
}
