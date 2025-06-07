package com.example.netmonitor.snmp;


import org.snmp4j.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityProtocols;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class SnmpTrapReceiver implements CommandResponder {

    private final List<String> receivedTraps = new ArrayList<>();

    @PostConstruct
    public void startTrapListener() throws IOException {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping(new UdpAddress("0.0.0.0/162"));
        Snmp snmp = new Snmp(transport);
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
        SecurityProtocols.getInstance().addDefaultProtocols();
        snmp.addCommandResponder(this);
        transport.listen();
        System.out.println("Trap Receiver started on port 162...");
    }

    @Override
    public void processPdu(CommandResponderEvent event) {
        if (event != null && event.getPDU() != null) {
            String trapInfo = "Trap received from " + event.getPeerAddress() + ":\n";
            for (VariableBinding vb : event.getPDU().getVariableBindings()) {
                trapInfo += vb.getOid() + " = " + vb.getVariable() + "\n";
            }
            System.out.println(trapInfo);
            synchronized (receivedTraps) {
                receivedTraps.add(trapInfo);
            }
        }
    }

    public List<String> getReceivedTraps() {
        synchronized (receivedTraps) {
            return new ArrayList<>(receivedTraps);
        }
    }
}
