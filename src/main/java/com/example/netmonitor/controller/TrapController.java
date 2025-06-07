package com.example.netmonitor.controller;



import com.example.netmonitor.snmp.SnmpTrapReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TrapController {

    @Autowired
    private SnmpTrapReceiver trapReceiver;

    @GetMapping("/snmp-traps")
    public List<String> getSnmpTraps() {
        return trapReceiver.getReceivedTraps();
    }
}

