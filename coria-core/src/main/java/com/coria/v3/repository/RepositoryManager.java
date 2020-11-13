package com.coria.v3.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * A singleton class to manage references to all JPA repositories in one place.
 * Created by David Fradin, 2020
 */
@Component
public class RepositoryManager {
    protected DatasetRepository datasetRepository;
    protected MetricRepository metricRepository;
    protected NodeRepository nodeRepository;
    protected EdgeRepository edgeRepository;
    protected ASLocationRepository asLocationRepository;
    protected ASOrganizationRepository asOrganizationRepository;
    protected ShortestPathLengthRepository shortestPathLengthRepository;
    protected NodeMetricResultRepository nodeMetricResultRepository;


    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setDatasetRepository(DatasetRepository datasetRepository) {
        this.datasetRepository = datasetRepository;
    }

    @Autowired
    public void setMetricRepository(MetricRepository metricRepository) {
        this.metricRepository = metricRepository;
    }

    @Autowired
    public void setNodeRepository(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @Autowired
    public void setEdgeRepository(EdgeRepository edgeRepository) {
        this.edgeRepository = edgeRepository;
    }

    @Autowired
    public void setAsLocationRepository(ASLocationRepository asLocationRepository) {
        this.asLocationRepository = asLocationRepository;
    }

    @Autowired
    public void setAsOrganizationRepository(ASOrganizationRepository asOrganizationRepository) {
        this.asOrganizationRepository = asOrganizationRepository;
    }

    @Autowired
    public void setShortestPathLengthRepository(ShortestPathLengthRepository shortestPathLengthRepository) {
        this.shortestPathLengthRepository = shortestPathLengthRepository;
    }

    @Autowired
    public void setNodeMetricResultRepository(NodeMetricResultRepository nodeMetricResultRepository) {
        this.nodeMetricResultRepository = nodeMetricResultRepository;
    }

    public DatasetRepository getDatasetRepository() {
        return datasetRepository;
    }

    public MetricRepository getMetricRepository() {
        return metricRepository;
    }

    public NodeRepository getNodeRepository() {
        return nodeRepository;
    }

    public EdgeRepository getEdgeRepository() {
        return edgeRepository;
    }

    public ASLocationRepository getAsLocationRepository() {
        return asLocationRepository;
    }

    public ASOrganizationRepository getAsOrganizationRepository() {
        return asOrganizationRepository;
    }

    public ShortestPathLengthRepository getShortestPathLengthRepository() {
        return shortestPathLengthRepository;
    }

    public NodeMetricResultRepository getNodeMetricResultRepository() {
        return nodeMetricResultRepository;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
