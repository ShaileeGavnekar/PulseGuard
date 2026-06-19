package com.pulseguard.monitor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonitorRepository extends JpaRepository<Monitor, Long> {

    Page<Monitor> findByOwnerId(Long ownerId, Pageable pageable);

    Optional<Monitor> findByIdAndOwnerId(Long id, Long ownerId);

    List<Monitor> findByEnabledTrue();
}
