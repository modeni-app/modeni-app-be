package com.steam.modeni.repository;

import com.steam.modeni.domain.entity.Family;
import com.steam.modeni.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    long countByFamily(Family family);
}
