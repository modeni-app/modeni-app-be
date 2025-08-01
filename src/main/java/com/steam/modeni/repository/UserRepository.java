package com.steam.modeni.repository;

import com.steam.modeni.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserId(String userId);
    boolean existsByUserId(String userId);
    List<User> findByFamilyCode(Long familyCode);
    
    // 가족 공유 기능을 위한 메서드
    List<User> findByFamilyCodeAndIdNot(Long familyCode, Long excludeUserId);
}
