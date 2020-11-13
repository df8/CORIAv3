package com.coria.v3.dbmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by David Fradin, 2020
 */
@JacksonXmlRootElement(localName = "as-organization")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
@Table(name = "as_organization", schema = "coria")
public class ASOrganizationEntity {
    private String id;
    private String name;
    private String country;
    private String source;
    private Collection<NodeASOrganizationEntity> nodes;

    public ASOrganizationEntity() {
    }

    public ASOrganizationEntity(String id, String name, String country, String source) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.source = source;
    }

    @Id
    @Column(name = "organization_id", nullable = false, length = 256)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JacksonXmlProperty(localName = "name", isAttribute = true)
    @Basic
    @Column(name = "name", length = 256)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JacksonXmlProperty(localName = "country", isAttribute = true)
    @Basic
    @Column(name = "country", length = 20)
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }


    @JacksonXmlProperty(localName = "source", isAttribute = true)
    @Basic
    @Column(name = "source")
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }


    @JacksonXmlElementWrapper(localName = "nodes")
    @JacksonXmlProperty(localName = "node")
    @OneToMany(mappedBy = "organization", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<NodeASOrganizationEntity> getNodes() {
        return nodes;
    }

    public void setNodes(Collection<NodeASOrganizationEntity> nodes) {
        this.nodes = nodes;
    }

    public void addNode(NodeASOrganizationEntity nodeASOrganizationEntity) {
        if (nodeASOrganizationEntity != null) {
            if (this.nodes == null)
                this.nodes = new ArrayList<>();
            this.nodes.add(nodeASOrganizationEntity);
            nodeASOrganizationEntity.setOrganization(this);
        }
    }

    @Override
    public String toString() {
        return "ASOrganizationEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}
