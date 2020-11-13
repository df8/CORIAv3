package com.coria.v3.repository;

import com.coria.v3.dbmodel.EdgeEntity;
import com.coria.v3.dbmodel.EdgeEntityPK;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by David Fradin, 2020
 */
@Repository
public interface EdgeRepository extends JpaRepository<EdgeEntity, EdgeEntityPK> {

    long countByDataset_Id(UUID dataset_id);

    long countByDataset_IdAndNameContains(UUID dataset_id, String name);

    Page<EdgeEntity> findAllByDataset_Id(UUID dataset_id, Pageable pageable);

    Page<EdgeEntity> findAllByDataset_IdAndNameContains(UUID dataset_id, String name, Pageable pageable);

    Optional<EdgeEntity> findByNodeSource_IdAndNodeTarget_Id(UUID nodeSource_id, UUID nodeTarget_id);
}
