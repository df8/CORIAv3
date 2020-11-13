package com.coria.v3.repository;

import com.coria.v3.dbmodel.DatasetEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

/**
 * Created by David Fradin, 2020
 */
@Repository
public interface DatasetRepository extends JpaRepository<DatasetEntity, UUID> {
    Page<DatasetEntity> findAllByIdIn(Collection<UUID> id, Pageable pageable);

    Page<DatasetEntity> findAllByNameContaining(String name, Pageable pageable);

    long countAllByIdIn(Collection<UUID> id);

    long countAllByNameContaining(String name);

    @Modifying
    @Query(value = "INSERT INTO `node_metric_result`  (`metric_id`,  `node_id`, `metric_result_value`) " +
            "SELECT :metricId, N.node_id, COUNT(*) AS `metric_result_value` FROM `node` N " +
            "INNER JOIN `edge` E " +
            "ON E.`source_node` = N.`node_id` OR E.`target_node` = N.`node_id` " +
            "WHERE N.dataset_id = :datasetId " +
            "GROUP BY N.node_id", nativeQuery = true)
    void calculateNodeDegree(@Param("datasetId") UUID datasetId, @Param("metricId") UUID metricId);

    //TODO /2 Change formula to simpler: |E| * 2 / |V|
    @Modifying
    @Query(value = "INSERT INTO `dataset_metric_result`  (`metric_id`,  `dataset_id`, `metric_result_value`) " +
            "SELECT :metricId, :datasetId, AVG(`degree`) AS `metric_result_value` FROM (" +
            "SELECT COUNT(*) AS `degree` FROM `node` N " +
            "INNER JOIN `edge` E " +
            "ON E.`source_node` = N.`node_id` OR E.`target_node` = N.`node_id` " +
            "WHERE N.dataset_id = :datasetId " +
            "GROUP BY N.node_id) D", nativeQuery = true)
    void calculateAverageNodeDegree(@Param("datasetId") UUID datasetId, @Param("metricId") UUID metricId);

    @Modifying
    @Query(value = "INSERT INTO `node_metric_result`  (`metric_id`,  `node_id`, `metric_result_value`) " +
            "SELECT :metricId, N1.node_id, AVG(NMR2.`metric_result_value`) AS `metric_result_value` " +
            "FROM `node` N1 " +
            "INNER JOIN `edge` E " +
            "ON E.`source_node` = N1.`node_id` OR E.`target_node` = N1.`node_id` " +
            "INNER JOIN `node_metric_result` NMR2 " +
            "ON N1.`node_id` <> NMR2.`node_id` AND (E.`source_node` = NMR2.`node_id` OR E.`target_node` = NMR2.`node_id`) " +
            "WHERE NMR2.`metric_id` = :metricIdNodeDegree AND N1.`dataset_id` = :datasetId " +
            "GROUP BY N1.`node_id`", nativeQuery = true)
    void calculateAverageNeighbourDegree(@Param("datasetId") UUID datasetId, @Param("metricId") UUID metricId, @Param("metricIdNodeDegree") UUID metricIdNodeDegree);


    @Modifying
    @Query(value = "INSERT INTO `node_metric_result`  (`metric_id`,  `node_id`, `metric_result_value`) " +
            "SELECT :metricId, node_id, IF(val_avg = 0 OR val_count = 0 OR val_stddev = 0, val_avg, val_avg + (((val_median - val_avg) / val_stddev) / val_count) * val_avg) AS `AverageNeighbourDegreeCorrected` " +
            "FROM ( " +
            "SELECT DISTINCT node_id, " +
            "AVG(`metric_result_value`) OVER (PARTITION BY node_id) AS val_avg, " +
            "MEDIAN(`metric_result_value`) OVER (PARTITION BY node_id) AS val_median, " +
            "STDDEV(`metric_result_value`) OVER (PARTITION BY node_id) AS val_stddev, " +
            "COUNT(`metric_result_value`) OVER (PARTITION BY node_id) AS val_count " +
            "FROM ( " +
            "SELECT N1.node_id, NMR2.`metric_result_value` " +
            "FROM `node` N1 " +
            "INNER JOIN `edge` E " +
            "ON E.`source_node` = N1.`node_id` OR E.`target_node` = N1.`node_id` " +
            "INNER JOIN `node_metric_result` NMR2 " +
            "ON N1.`node_id` <> NMR2.`node_id` AND (E.`source_node` = NMR2.`node_id` OR E.`target_node` = NMR2.`node_id`) " +
            "WHERE NMR2.`metric_id` = :metricIdNodeDegree " +
            "AND N1.`dataset_id` = :datasetId " +
            ") NDEG " +
            ") S;", nativeQuery = true)
    void calculateAverageNeighbourDegreeCorrected(@Param("datasetId") UUID datasetId, @Param("metricId") UUID metricId, @Param("metricIdNodeDegree") UUID metricIdNodeDegree);

    @Modifying
    @Query(value = "INSERT INTO `node_metric_result`  (`metric_id`,  `node_id`, `metric_result_value`) " +
            "SELECT DISTINCT :metricId, node_id, IF(S.val_max = S.val_min, 0, (metric_result_value - S.val_min) / (S.val_max - S.val_min)) AS val_normalised " +
            "FROM `node_metric_result`, " +
            "(SELECT MIN(metric_result_value) AS val_min, MAX(metric_result_value) AS val_max " +
            "FROM `node_metric_result` " +
            "WHERE metric_id = :metricIdToBeNormalised) S " +
            "WHERE metric_id = :metricIdToBeNormalised", nativeQuery = true)
    void calculateNodeMetricMinMaxNormalised(@Param("metricId") UUID metricId, @Param("metricIdToBeNormalised") UUID metricIdToBeNormalised);

    @Modifying
    @Query(value = "INSERT INTO `node_metric_result`  (`metric_id`,  `node_id`, `metric_result_value`) " +
            "SELECT DISTINCT :metricId, node_id, IF(S.val_max = S.val_min, 1, (S.val_max - metric_result_value) / (S.val_max - S.val_min)) AS val_normalised " +
            "FROM `node_metric_result`, " +
            "(SELECT MIN(metric_result_value) AS val_min, MAX(metric_result_value) AS val_max " +
            "FROM `node_metric_result` " +
            "WHERE metric_id = :metricIdToBeNormalised) S " +
            "WHERE metric_id = :metricIdToBeNormalised", nativeQuery = true)
    void calculateNodeMetricMaxMinNormalised(@Param("metricId") UUID metricId, @Param("metricIdToBeNormalised") UUID metricIdToBeNormalised);


    /**
     * Calculates for each given node n the average of node degrees of all nodes that lie exactly 2 hops away from n.
     * Nodes that are direct  Neighbours (distance = 1 hop) and the node n itself (distance = 0 hops) are ignored.
     * Inspired by the implementation in https://github.com/es1985/Coria_backend/blob/master/metrics.py, lines 33-54
     *
     * @param datasetId          UUID of dataset
     * @param metricId           UUID of the new metric
     * @param metricIdNodeDegree UUID of the node degree metric
     */
    @Modifying
    @Query(value = "INSERT INTO `node_metric_result`  (`metric_id`,  `node_id`, `metric_result_value`)  " +
            "SELECT :metricId, N_.node_id, IFNULL(AVG(M.`metric_result_value`),0) AS `metric_result_value` " +
            "FROM `node` N_ " +
            "LEFT OUTER JOIN ( " +
            "SELECT DISTINCT N0.node_id, N2.node_id AS `_2ndhop_node_id`, NMR2.`metric_result_value` " +
            "FROM `node` N0 " +
            "INNER JOIN `edge` E1 " +
            "ON E1.`source_node` = N0.`node_id` OR E1.`target_node` = N0.`node_id` " +
            "INNER JOIN `node` N1 " +
            "ON N0.`node_id` <> N1.`node_id` AND (E1.`source_node` = N1.`node_id` OR E1.`target_node` = N1.`node_id`) " +
            "INNER JOIN `edge` E2 " +
            "ON E2.`source_node` = N1.`node_id` OR E2.`target_node` = N1.`node_id` " +
            "INNER JOIN `node` N2 " +
            "ON N0.`node_id` <> N2.`node_id` AND N1.`node_id` <> N2.`node_id` AND (E2.`source_node` = N2.`node_id` OR E2.`target_node` = N2.`node_id`) " +
            "INNER JOIN `node_metric_result` NMR2 " +
            "ON N2.`node_id` = NMR2.`node_id` " +
            "WHERE NMR2.`metric_id` = :metricIdNodeDegree  " +
            "AND N0.`dataset_id` = :datasetId  " +
            "AND N2.node_id NOT IN ( " +
            "SELECT N11.node_id " +
            "FROM `node` N10 " +
            "INNER JOIN `edge` E11 " +
            "ON E11.`source_node` = N10.`node_id` OR E11.`target_node` = N10.`node_id` " +
            "INNER JOIN `node` N11 " +
            "ON N11.`node_id` <> N10.`node_id` AND (E11.`source_node` = N11.`node_id` OR E11.`target_node` = N11.`node_id`) " +
            "WHERE N10.node_id = N0.node_id)) M " +
            "ON N_.node_id = M.node_id " +
            "WHERE N_.dataset_id = :datasetId " +
            "GROUP BY N_.`node_id`;", nativeQuery = true)
    void calculateIteratedAverageNeighbourDegree(@Param("datasetId") UUID datasetId, @Param("metricId") UUID metricId, @Param("metricIdNodeDegree") UUID metricIdNodeDegree);


    @Modifying
    @Query(value = "INSERT INTO `node_metric_result`  (`metric_id`,  `node_id`, `metric_result_value`) " +
            "SELECT :metricId, node_id, IF(val_avg IS NULL, 0, IF(val_avg = 0 OR val_count = 0 OR val_stddev = 0, val_avg, val_avg + (((val_median - val_avg) / val_stddev) / val_count) * val_avg)) AS `IteratedAverageNeighbourDegreeCorrected` " +
            "FROM ( " +
            "SELECT DISTINCT N_.node_id, " +
            "name1, " +
            "AVG(`metric_result_value`) OVER (PARTITION BY node_id)    AS val_avg, " +
            "MEDIAN(`metric_result_value`) OVER (PARTITION BY node_id) AS val_median, " +
            "STDDEV(`metric_result_value`) OVER (PARTITION BY node_id) AS val_stddev, " +
            "COUNT(`metric_result_value`) OVER (PARTITION BY node_id)  AS val_count " +
            "FROM `node` N_ " +
            "LEFT OUTER JOIN ( " +
            "SELECT DISTINCT N0.node_id, N2.node_id AS `_2ndhop_node_id`, N0.name AS `name1`, N2.name AS `name2`, NMR2.`metric_result_value` " +
            "FROM `node` N0 " +
            "INNER JOIN `edge` E1 " +
            "  ON E1.`source_node` = N0.`node_id` OR E1.`target_node` = N0.`node_id` " +
            "INNER JOIN `node` N1 " +
            "  ON N0.`node_id` <> N1.`node_id` AND (E1.`source_node` = N1.`node_id` OR E1.`target_node` = N1.`node_id`) " +
            "INNER JOIN `edge` E2 " +
            "  ON E2.`source_node` = N1.`node_id` OR E2.`target_node` = N1.`node_id` " +
            "INNER JOIN `node` N2 " +
            "  ON N0.`node_id` <> N2.`node_id` AND N1.`node_id` <> N2.`node_id` AND (E2.`source_node` = N2.`node_id` OR E2.`target_node` = N2.`node_id`) " +
            "INNER JOIN `node_metric_result` NMR2 " +
            "  ON N2.`node_id` = NMR2.`node_id` " +
            "WHERE NMR2.`metric_id` = :metricIdNodeDegree " +
            "AND N0.`dataset_id` = :datasetId " +
            "AND N2.node_id NOT IN (" + // This NOT IN operation excludes all 2nd-hop-nodes that happen to be 1st-hop-nodes as well.
            "SELECT N11.node_id " +
            "FROM `node` N10 " +
            "INNER JOIN `edge` E11 " +
            "  ON E11.`source_node` = N10.`node_id` OR E11.`target_node` = N10.`node_id` " +
            "INNER JOIN `node` N11 " +
            "  ON N11.`node_id` <> N10.`node_id` AND (E11.`source_node` = N11.`node_id` OR E11.`target_node` = N11.`node_id`) " +
            "WHERE N10.node_id = N0.node_id) " +
            "ORDER BY N0.name, NMR2.metric_result_value, N2.name " +
            ") NDEG " +
            "ON N_.node_id = NDEG.node_id " +
            "WHERE N_.dataset_id = :datasetId " +
            ") S;", nativeQuery = true)
    void calculateIteratedAverageNeighbourDegreeCorrected(@Param("datasetId") UUID datasetId, @Param("metricId") UUID metricId, @Param("metricIdNodeDegree") UUID metricIdNodeDegree);

    /**
     * Calculates for each given node n the clustering coefficient.
     * Source: https://en.wikipedia.org/wiki/Clustering_coefficient#Local_clustering_coefficient
     *
     * @param datasetId          UUID of dataset
     * @param metricId           UUID of the new metric
     * @param metricIdNodeDegree UUID of the node degree metric
     */
    @Modifying
    @Query(value = "INSERT INTO `node_metric_result`  (`metric_id`,  `node_id`, `metric_result_value`)  " +
            "SELECT :metricId, N.node_id, IF(NMR.metric_result_value <= 1, 0, 2 * IFNULL(T1.count_triangles, 0) / (NMR.metric_result_value * (NMR.metric_result_value - 1))) AS local_clustering_coefficient " +
            "FROM `node` N " +
            "LEFT OUTER JOIN " +
            "(SELECT N00.node_id, COUNT(*) AS count_triangles " +
            "FROM `node` N00 " +
            "INNER JOIN `edge` E01 " +
            "      ON E01.`source_node` = N00.`node_id` OR E01.`target_node` = N00.`node_id` " +
            "INNER JOIN `node` N01 " +
            "      ON N00.`node_id` <> N01.`node_id` AND (E01.`source_node` = N01.`node_id` OR E01.`target_node` = N01.`node_id`) " +
            "INNER JOIN `edge` E02 " +
            "      ON E02.`source_node` = N01.`node_id` OR E02.`target_node` = N01.`node_id` " +
            "INNER JOIN `node` N02 " +
            "      ON N00.`node_id` <> N02.`node_id` AND N01.`node_id` < N02.`node_id` AND (E02.`source_node` = N02.`node_id` OR E02.`target_node` = N02.`node_id`) " +
            "INNER JOIN `edge` E03 " +
            "      ON (E03.`source_node` = N02.`node_id` AND E03.`target_node` = N00.`node_id`) " +
            "          OR (E03.`source_node` = N00.`node_id` AND E03.`target_node` = N02.`node_id`) " +
            "WHERE N00.`dataset_id` = :datasetId " +
            "GROUP BY N00.node_id) T1 " +
            "ON N.node_id = T1.node_id " +
            "LEFT OUTER JOIN node_metric_result NMR " +
            "     ON N.node_id = NMR.node_id AND NMR.metric_id = :metricIdNodeDegree " +
            "WHERE N.`dataset_id` = :datasetId " +
            "ORDER BY N.name", nativeQuery = true)
    void calculateLocalClusteringCoefficients(@Param("datasetId") UUID datasetId, @Param("metricId") UUID metricId, @Param("metricIdNodeDegree") UUID metricIdNodeDegree);

    @Modifying
    @Query(value = "INSERT INTO `node_metric_result`  (`metric_id`,  `node_id`, `metric_result_value`)  " +
            "SELECT :metricId, NMR_CC.node_id, NMR_CC.metric_result_value + NMR_NDEG.metric_result_value * NMR_CC.metric_result_value / 4 AS LocalClusteringCoefficientCorrected " +
            "FROM `node_metric_result` NMR_CC " +
            "INNER JOIN `node_metric_result` NMR_NDEG " +
            "ON NMR_CC.node_id = NMR_NDEG.node_id " +
            "WHERE NMR_CC.metric_id = :metricIdLocalClusteringCoefficient " +
            "AND NMR_NDEG.metric_id = :metricIdNodeDegree", nativeQuery = true)
    void calculateLocalClusteringCoefficientsCorrected(@Param("metricId") UUID metricId, @Param("metricIdLocalClusteringCoefficient") UUID metricIdLocalClusteringCoefficient, @Param("metricIdNodeDegree") UUID metricIdNodeDegree);


    @Modifying
    @Query(value = "INSERT INTO `node_metric_result`  (`metric_id`,  `node_id`, `metric_result_value`) " +
            "SELECT :metricId, `source_node`, SUM(distance) / (COUNT(*)+1) AS `distance` " +
            "FROM ( " +
            "    SELECT S.`source_node`, S.`target_node`, distance FROM `shortest_path_length_metric_results` S WHERE S.`metric_id` = :metricIdShortestPathLength " +
            "    UNION " +
            "    SELECT S.`target_node`, S.`source_node`, distance FROM `shortest_path_length_metric_results` S WHERE S.`metric_id` = :metricIdShortestPathLength " +
            ") U   " +
            "GROUP BY `U`.`source_node` " +
            "ORDER BY `U`.`source_node`", nativeQuery = true)
    void calculateAverageShortestPathLength(@Param("metricId") UUID metricId, @Param("metricIdShortestPathLength") UUID metricIdShortestPathLength);

    @Modifying
    @Query(value = "INSERT INTO `node_metric_result`  (`metric_id`,  `node_id`, `metric_result_value`) " +
            "SELECT :metricId, `source_node`, MAX(distance) " +
            "FROM ( " +
            "    SELECT S.`source_node`, S.`target_node`, distance FROM `shortest_path_length_metric_results` S WHERE S.`metric_id` = :metricIdShortestPathLength " +
            "    UNION " +
            "    SELECT S.`target_node`, S.`source_node`, distance FROM `shortest_path_length_metric_results` S WHERE S.`metric_id` = :metricIdShortestPathLength " +
            ") U   " +
            "GROUP BY `U`.`source_node` " +
            "ORDER BY `U`.`source_node`", nativeQuery = true)
    void calculateEccentricity(@Param("metricId") UUID metricId, @Param("metricIdShortestPathLength") UUID metricIdShortestPathLength);

    @Modifying
    @Query(value = "INSERT INTO `node_metric_result` (`metric_id`,  `node_id`, `metric_result_value`) \n" +
            "SELECT :metricId, N.node_id, \n" +
            "      (NMR_ND.metric_result_value * 0.25 + \n" +
            "       NMR_AND.metric_result_value * 0.15 + \n" +
            "       NMR_IAND.metric_result_value * 0.1 + \n" +
            "       NMR_BC.metric_result_value * 0.25 + \n" +
            "       NMR_ECC.metric_result_value * 0.125 + \n" +
            "       NMR_ASPL.metric_result_value * 0.125) AS `urs` \n" +
            "FROM `node` N \n" +
            "INNER JOIN `node_metric_result` NMR_ND \n" +
            "ON N.node_id = NMR_ND.node_id \n" +
            "INNER JOIN `node_metric_result` NMR_AND \n" +
            "ON N.node_id = NMR_AND.node_id \n" +
            "INNER JOIN `node_metric_result` NMR_IAND \n" +
            "ON N.node_id = NMR_IAND.node_id \n" +
            "INNER JOIN `node_metric_result` NMR_BC \n" +
            "ON N.node_id = NMR_BC.node_id \n" +
            "INNER JOIN `node_metric_result` NMR_ECC \n" +
            "ON N.node_id = NMR_ECC.node_id \n" +
            "INNER JOIN `node_metric_result` NMR_ASPL \n" +
            "ON N.node_id = NMR_ASPL.node_id \n" +
            "WHERE \n" +
            "N.dataset_id = :datasetId \n" +
            "AND NMR_ND.metric_id = (SELECT M1.metric_id FROM metric M1 WHERE M1.dataset_id = :datasetId AND M1.metric_algorithm_implementation LIKE 'node-degree--normalised--%' AND status = 'FINISHED' LIMIT 1) \n" +
            "AND NMR_AND.metric_id = (SELECT M1.metric_id FROM metric M1 WHERE M1.dataset_id = :datasetId AND M1.metric_algorithm_implementation LIKE 'average-neighbour-degree--corrected-and-normalised--%' AND status = 'FINISHED' LIMIT 1) \n" +
            "AND NMR_IAND.metric_id = (SELECT M1.metric_id FROM metric M1 WHERE M1.dataset_id = :datasetId AND M1.metric_algorithm_implementation LIKE 'iterated-average-neighbour-degree--corrected-and-normalised--%' AND status = 'FINISHED' LIMIT 1) \n" +
            "AND NMR_BC.metric_id = (SELECT M1.metric_id FROM metric M1 WHERE M1.dataset_id = :datasetId AND M1.metric_algorithm_implementation LIKE 'betweenness-centrality--normalised--%' AND status = 'FINISHED' LIMIT 1) \n" +
            "AND NMR_ECC.metric_id = (SELECT M1.metric_id FROM metric M1 WHERE M1.dataset_id = :datasetId AND M1.metric_algorithm_implementation LIKE 'eccentricity--normalised--%' AND status = 'FINISHED' LIMIT 1) \n" +
            "AND NMR_ASPL.metric_id = (SELECT M1.metric_id FROM metric M1 WHERE M1.dataset_id = :datasetId AND M1.metric_algorithm_implementation LIKE 'average-shortest-path-length--normalised--%' AND status = 'FINISHED' LIMIT 1) \n" +
            "GROUP BY N.node_id, N.name, urs"
            , nativeQuery = true)
    void calculateUnifiedRiskScore(@Param("datasetId") UUID datasetId, @Param("metricId") UUID metricId);
}