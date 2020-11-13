package com.coria.v3.repository;

import com.coria.v3.dbmodel.ASLocationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * Created by David Fradin, 2020
 */
@Repository
public interface ASLocationRepository extends JpaRepository<ASLocationEntity, String> {
    Page<ASLocationEntity> findAllByIdIn(Collection<String> id, Pageable pageable);

    Page<ASLocationEntity> findAllByIdContaining(String id, Pageable pageable);

    long countAllByIdIn(Collection<String> id);

    long countAllByIdContaining(String id);

}
