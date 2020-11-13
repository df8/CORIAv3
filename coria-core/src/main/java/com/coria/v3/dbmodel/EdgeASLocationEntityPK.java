package com.coria.v3.dbmodel;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Created by David Fradin, 2020
 */
public class EdgeASLocationEntityPK implements Serializable {
    private ASLocationEntity asLocation;
    private EdgeEntity edge;

    @Id
    @ManyToOne(cascade = {CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", referencedColumnName = "location_id", nullable = false, insertable = false, updatable = false)
    public ASLocationEntity getLocation() {
        return asLocation;
    }

    public void setLocation(ASLocationEntity asLocation) {
        this.asLocation = asLocation;
    }

    @Id
    @ManyToOne(cascade = {CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "source_node", referencedColumnName = "source_node", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "target_node", referencedColumnName = "target_node", nullable = false, insertable = false, updatable = false)})
    public EdgeEntity getEdge() {
        return edge;
    }

    public void setEdge(EdgeEntity edge) {
        this.edge = edge;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeASLocationEntityPK that = (EdgeASLocationEntityPK) o;
        return Objects.equals(asLocation, that.asLocation) &&
                Objects.equals(edge, that.edge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(asLocation, edge);
    }
}
