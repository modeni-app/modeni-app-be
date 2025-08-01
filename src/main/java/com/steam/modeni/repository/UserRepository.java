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
    
    // 가족 구성원 수 조회 (미션 완료 조건 확인용)
    long countByFamilyCode(Long familyCode);
}
