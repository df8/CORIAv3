package com.coria.v3.repository;

import com.coria.v3.dbmodel.MetricEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Created by David Fradin, 2020
 */
@Repository
public interface MetricRepository extends JpaRepository<MetricEntity, UUID> {

    long countByDataset_Id(UUID dataset_id);

    Page<MetricEntity> findAllByDataset_Id(UUID dataset_id, Pageable pageable);

    List<MetricEntity> findAllByDataset_IdAndStatus(UUID dataset_id, MetricEntity.MetricStatus metricStatus);
}