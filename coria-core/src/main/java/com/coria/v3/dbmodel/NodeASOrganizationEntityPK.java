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
public class NodeASOrganizationEntityPK implements Serializable {
    private ASOrganizationEntity organization;
    private NodeEntity node;

    @Id
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "organization_id", referencedColumnName = "organization_id", nullable = false, insertable = false, updatable = false)
    public ASOrganizationEntity getOrganization() {
        return organization;
    }

    public void setOrganization(ASOrganizationEntity organization) {
        this.organization = organization;
    }

    @Id
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "node_id", referencedColumnName = "node_id", nullable = false, insertable = false, updatable = false)
    public NodeEntity getNode() {
        return node;
    }

    public void setNode(NodeEntity node) {
        this.node = node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeASOrganizationEntityPK that = (NodeASOrganizationEntityPK) o;
        return Objects.equals(organization, that.organization) &&
                Objects.equals(node, that.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organization, node);
    }
}
