package com.example.netmonitor.repository;

import com.example.netmonitor.model.NetworkDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NetworkDeviceRepository extends JpaRepository<NetworkDevice, Long> {
	


}

