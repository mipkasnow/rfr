package io.student.rangiffler.data.repository;

import io.student.rangiffler.data.entity.CountryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CountryRepository extends JpaRepository<CountryEntity, UUID> {
  Page<CountryEntity> findAllByNameContainsIgnoreCase(String name, Pageable pageable);

  Optional<CountryEntity> findByName(String name);

  Optional<CountryEntity> findByCode(String code);
}
