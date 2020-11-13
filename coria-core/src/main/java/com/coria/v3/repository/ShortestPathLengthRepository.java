package com.coria.v3.repository;

import com.coria.v3.dbmodel.ShortestPathLengthEntity;
import com.coria.v3.dbmodel.ShortestPathLengthEntityPK;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by David Fradin, 2020
 */
@Repository
public interface ShortestPathLengthRepository extends JpaRepository<ShortestPathLengthEntity, ShortestPathLengthEntityPK> {

    long countByMetric_Dataset_Id(UUID dataset_id);

    long countByMetric_Dataset_IdAndMetric_MetricAlgorithmImplementationIdContains(UUID dataset_id, String metric_id);

    Page<ShortestPathLengthEntity> findAllByMetric_Dataset_Id(UUID dataset_id, Pageable pageable);

    List<ShortestPathLengthEntity> findAllByMetric_Id(UUID metricId);


    Page<ShortestPathLengthEntity> findAllByMetric_Dataset_IdAndMetric_MetricAlgorithmImplementationIdContains(UUID dataset_id, String metricAlgorithmImplementationId, Pageable pageable);

    Optional<ShortestPathLengthEntity> findByMetric_IdAndNodeSource_IdAndNodeTarget_Id(UUID metric_id, UUID nodeSource_id, UUID nodeTarget_id);
}
