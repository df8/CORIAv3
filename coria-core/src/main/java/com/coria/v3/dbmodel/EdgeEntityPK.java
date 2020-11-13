package com.coria.v3.dbmodel;

import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

/**
 * Created by David Fradin, 2020
 */
public class EdgeEntityPK implements Serializable {
    private NodeEntity nodeSource;
    private NodeEntity nodeTarget;

    @Id
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "source_node", referencedColumnName = "node_id", nullable = false, insertable = false, updatable = false)
    public NodeEntity getNodeSource() {
        return nodeSource;
    }

    public void setNodeSource(NodeEntity nodeBySourceNode) {
        this.nodeSource = nodeBySourceNode;
    }

    @Id
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "target_node", referencedColumnName = "node_id", nullable = false, insertable = false, updatable = false)
    public NodeEntity getNodeTarget() {
        return nodeTarget;
    }

    public void setNodeTarget(NodeEntity targetNode) {
        this.nodeTarget = targetNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeEntityPK that = (EdgeEntityPK) o;
        return Objects.equals(nodeSource, that.nodeSource) &&
                Objects.equals(nodeTarget, that.nodeTarget);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeSource, nodeTarget);
    }
}

