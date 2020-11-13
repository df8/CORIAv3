package com.coria.v3.dbmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

/**
 * Created by David Fradin, 2020
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
@Table(name = "edge_location", schema = "coria")
@IdClass(EdgeASLocationEntityPK.class)
public class EdgeASLocationEntity {
    private EdgeEntity edge;
    private ASLocationEntity location;
    private String source;

    private final Logger logger = LoggerFactory.getLogger(EdgeASLocationEntity.class);

    @JsonIgnore
    @Id
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "location_id", referencedColumnName = "location_id", nullable = false)
    public ASLocationEntity getLocation() {
        return location;
    }

    public void setLocation(ASLocationEntity location) {
        this.location = location;
    }

    @JsonIgnore
    @Id
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumns({@
            JoinColumn(name = "source_node", referencedColumnName = "source_node", nullable = false),
            @JoinColumn(name = "target_node", referencedColumnName = "target_node", nullable = false)})
    public EdgeEntity getEdge() {
        return edge;
    }

    public void setEdge(EdgeEntity edge) {
        this.edge = edge;
    }

    @JacksonXmlProperty(localName = "source", isAttribute = true)
    @Basic
    @Column(name = "source", nullable = false)
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }


    public EdgeASLocationEntity() {
    }

    public EdgeASLocationEntity(EdgeEntity edge, ASLocationEntity location, String source) {
        this.location = location;
        this.edge = edge;
        this.source = source;
    }

    // Additional helper methods for JSON and XML serialization.
    @JsonProperty("location")
    @JacksonXmlProperty(localName = "locationId", isAttribute = true)
    @Transient
    public String getLocationId() {
        return this.location.getId();
    }
}
