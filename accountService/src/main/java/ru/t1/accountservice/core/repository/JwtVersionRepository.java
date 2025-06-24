package ru.t1.accountservice.core.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.t1.accountservice.core.entity.jwtversion.JwtVersion;

@Repository
public interface JwtVersionRepository extends JpaRepository<JwtVersion, Long> {

    Optional<JwtVersion> findByUserId(Long userId);

    boolean existsByUserId(Long id);

    void deleteByUserId(Long userId);
}