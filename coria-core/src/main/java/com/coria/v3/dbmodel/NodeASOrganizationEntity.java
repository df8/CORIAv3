package com.coria.v3.dbmodel;

import javax.persistence.*;

/**
 * Created by David Fradin, 2020
 */
@Entity
@Table(name = "node_organization", schema = "coria")
@IdClass(NodeASOrganizationEntityPK.class)
public class NodeASOrganizationEntity {

    private ASOrganizationEntity organization;
    private NodeEntity node;
    private String changeDate;

    @Id
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "organization_id", referencedColumnName = "organization_id", nullable = false)
    public ASOrganizationEntity getOrganization() {
        return organization;
    }

    public void setOrganization(ASOrganizationEntity asOrganization) {
        this.organization = asOrganization;
    }

    @Id
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "node_id", referencedColumnName = "node_id", nullable = false)
    public NodeEntity getNode() {
        return node;
    }

    public void setNode(NodeEntity node) {
        this.node = node;
    }

    @Basic
    @Column(name = "change_date", nullable = false)
    public String getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(String value) {
        this.changeDate = value;
    }


    public NodeASOrganizationEntity() {
    }

    public NodeASOrganizationEntity(ASOrganizationEntity organization, NodeEntity node, String changeDate) {
        this.organization = organization;
        this.node = node;
        this.changeDate = changeDate;
    }

}
