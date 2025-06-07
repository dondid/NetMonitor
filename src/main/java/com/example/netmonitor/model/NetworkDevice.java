package com.example.netmonitor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class NetworkDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ipAddress;
    private String community;
    private String description;
    private boolean online;

    private String sysDescr;
    private Long uptimeSeconds;

    // Constructori
    public NetworkDevice() {}

    public NetworkDevice(String ipAddress, String community, String description, boolean online, String sysDescr, Long uptimeSeconds) {
        this.ipAddress = ipAddress;
        this.community = community;
        this.description = description;
        this.online = online;
        this.sysDescr = sysDescr;
        this.uptimeSeconds = uptimeSeconds;
    }

    // Getteri și setteri

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getSysDescr() {
        return sysDescr;
    }

    public void setSysDescr(String sysDescr) {
        this.sysDescr = sysDescr;
    }

    public Long getUptimeSeconds() {
        return uptimeSeconds;
    }

    public void setUptimeSeconds(Long uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }


}
