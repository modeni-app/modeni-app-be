package com.steam.modeni.repository;

import com.steam.modeni.domain.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {
    Optional<Family> findByFamilyCode(String familyCode);
    boolean existsByFamilyCode(String familyCode);
} 