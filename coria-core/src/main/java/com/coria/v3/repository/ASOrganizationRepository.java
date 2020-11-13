package com.coria.v3.repository;

import com.coria.v3.dbmodel.ASOrganizationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * Created by David Fradin, 2020
 */
@Repository
public interface ASOrganizationRepository extends JpaRepository<ASOrganizationEntity, String> {
    Page<ASOrganizationEntity> findAllByIdIn(Collection<String> id, Pageable pageable);

    Page<ASOrganizationEntity> findAllByIdContainingOrNameContaining(String id, String name, Pageable pageable);

    long countAllByIdIn(Collection<String> id);

    long countAllByIdContaining(String id);

}
