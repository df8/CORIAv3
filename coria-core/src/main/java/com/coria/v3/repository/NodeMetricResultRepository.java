package com.coria.v3.repository;

import com.coria.v3.dbmodel.NodeMetricResultEntity;
import com.coria.v3.dbmodel.NodeMetricResultEntityPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Created by David Fradin, 2020
 */
@Repository
public interface NodeMetricResultRepository extends JpaRepository<NodeMetricResultEntity, NodeMetricResultEntityPK> {
    List<NodeMetricResultEntity> findAllByMetric_Id(UUID metric_id);

}