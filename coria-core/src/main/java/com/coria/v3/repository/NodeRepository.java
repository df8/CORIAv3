package com.coria.v3.repository;

import com.coria.v3.dbmodel.NodeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Created by David Fradin, 2020
 */
@Repository
public interface NodeRepository extends JpaRepository<NodeEntity, UUID> {

    long countByDataset_Id(UUID dataset_id);

    Page<NodeEntity> findAllByDataset_Id(UUID dataset_id, Pageable pageable);

    long countByDataset_IdAndNameContains(UUID dataset_id, String name);

    Page<NodeEntity> findAllByDataset_IdAndNameContains(UUID dataset_id, String name, Pageable pageable);
}
