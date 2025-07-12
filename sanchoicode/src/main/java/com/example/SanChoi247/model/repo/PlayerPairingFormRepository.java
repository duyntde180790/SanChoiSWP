package com.example.SanChoi247.model.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SanChoi247.model.entity.PlayerPairingForm;

@Repository
public interface PlayerPairingFormRepository extends JpaRepository<PlayerPairingForm, Integer> {
    List<PlayerPairingForm> findByStatus(String status);
    List<PlayerPairingForm> findByStatusAndIdNot(String status, Integer id);
    List<PlayerPairingForm> findByStatusIn(List<String> statuses);
    Optional<PlayerPairingForm> findByPhone(String phone);
    Optional<PlayerPairingForm> findByEmail(String email);
}
