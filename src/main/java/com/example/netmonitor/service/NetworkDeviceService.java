package com.example.netmonitor.service;

import com.example.netmonitor.model.NetworkDevice;
import com.example.netmonitor.repository.NetworkDeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NetworkDeviceService {

    private final NetworkDeviceRepository repository;

    public NetworkDeviceService(NetworkDeviceRepository repository) {
        this.repository = repository;
    }

    public List<NetworkDevice> findAll() {
        return repository.findAll();
    }

    public NetworkDevice save(NetworkDevice device) {
        return repository.save(device);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    
    public void deleteAll() {
        repository.deleteAll();
    }

}
