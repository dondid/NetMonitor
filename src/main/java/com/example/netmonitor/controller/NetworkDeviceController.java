package com.example.netmonitor.controller;

import com.example.netmonitor.model.NetworkDevice;
import com.example.netmonitor.service.NetworkDeviceService;
import com.example.netmonitor.service.SnmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;


@Controller
public class NetworkDeviceController {

    private final NetworkDeviceService service;

    @Autowired
    private SnmpService snmpService;

    public NetworkDeviceController(NetworkDeviceService service) {
        this.service = service;
    }

    // Test de conectivitate la IP și port
	@GetMapping("/test-connection")
    @ResponseBody
    public String testConnection(@RequestParam String ip) {
        String command;
        // Detectare OS pentru comanda traceroute
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Windows folosește tracert
            command = "tracert " + ip;
        } else {
            // Linux/macOS folosește traceroute
            command = "traceroute " + ip;
        }

        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return "Traceroute successful:\n" + output.toString();
            } else {
                return "Traceroute failed with exit code " + exitCode + ":\n" + output.toString();
            }
        } catch (Exception e) {
            return "Error running traceroute: " + e.getMessage();
        }
    }

    
    // Afișare interfață MIB Browser
    @GetMapping({"/", "/devices", "/mib-browser"})
    public String showMibBrowser(Model model) {
        model.addAttribute("device", new NetworkDevice());
        model.addAttribute("devices", service.findAll());
        return "devices/mib-browser";
    }
    
    @PostMapping("/devices/search")
    public String performSnmpOperation(Model model,
                                       @RequestParam String ipAddress,
                                       @RequestParam(required = false, defaultValue = "161") int port,
                                       @RequestParam String protocol,
                                       @RequestParam String operationType,
                                       @RequestParam(required = false) String value,
                                       @RequestParam String readCommunity,
                                       @RequestParam String writeCommunity,
                                       @RequestParam String snmpVersion,
                                       @RequestParam String oid) {

        NetworkDevice device = new NetworkDevice();
        device.setIpAddress(ipAddress);
        device.setOnline(true);
        device.setCommunity(readCommunity);
        device.setDescription(oid);

        try {
            String result = switch (operationType) {
                case "Get" -> snmpService.getAsString(ipAddress, port, readCommunity, oid, snmpVersion);
                case "GetNext" -> snmpService.getNext(ipAddress, port, readCommunity, oid, snmpVersion);
                case "Set" -> snmpService.set(ipAddress, port, writeCommunity, oid, value, snmpVersion);
                case "Walk" -> snmpService.walk(ipAddress, port, readCommunity, oid, snmpVersion);
                default -> "Unsupported operation";
            };

            device.setSysDescr(result);

            String uptimeStr = snmpService.getAsString(ipAddress, port, readCommunity, "1.3.6.1.2.1.1.3.0", snmpVersion);
            if (uptimeStr != null && uptimeStr.matches("\\d+")) {
                long ticks = Long.parseLong(uptimeStr);
                device.setUptimeSeconds(ticks / 100); // Conversie TimeTicks
            }

        } catch (Exception e) {
            device.setOnline(false);
            device.setDescription("Error: " + e.getMessage());
        }

        service.save(device);

        model.addAttribute("devices", service.findAll());
        model.addAttribute("device", new NetworkDevice());

        return "devices/mib-browser";
    }
    
    @PostMapping("/devices/clear")
    public String clearResults(Model model) {
        service.deleteAll();
        model.addAttribute("device", new NetworkDevice());
        model.addAttribute("devices", service.findAll()); // va fi listă goală
        return "devices/mib-browser";
    }

}
    
