package com.coria.v3.dbmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Collection;

/**
 * Created by David Fradin, 2020
 */
@JacksonXmlRootElement(localName = "as-location")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
@Table(name = "as_location", schema = "coria")
public class ASLocationEntity {
    private String id;
    private String continent;
    private String country;
    private String region;
    private String city;
    private String latitude;
    private String longitude;
    private Collection<EdgeASLocationEntity> edgeLocations;

    public ASLocationEntity() {
    }

    public ASLocationEntity(String id, String continent, String country, String region, String city, String latitude, String longitude) {
        this.id = id;
        this.continent = continent;
        this.country = country;
        this.region = region;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Id
    @Column(name = "location_id", nullable = false, length = 48)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JacksonXmlProperty(localName = "continent", isAttribute = true)
    @Basic
    @Column(name = "continent", length = 2)
    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    @JacksonXmlProperty(localName = "country", isAttribute = true)
    @Basic
    @Column(name = "country", length = 2)
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @JacksonXmlProperty(localName = "region", isAttribute = true)
    @Basic
    @Column(name = "region", length = 48)
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @JacksonXmlProperty(localName = "city", isAttribute = true)
    @Basic
    @Column(name = "city", length = 48)
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @JacksonXmlProperty(localName = "latitude", isAttribute = true)
    @Basic
    @Column(name = "latitude", length = 16)
    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    @JacksonXmlProperty(localName = "longitude", isAttribute = true)
    @Basic
    @Column(name = "longitude", length = 16)
    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    @JsonIgnore
    @OneToMany(mappedBy = "location", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<EdgeASLocationEntity> getEdgeLocations() {
        return edgeLocations;
    }

    public void setEdgeLocations(Collection<EdgeASLocationEntity> edgeLocations) {
        this.edgeLocations = edgeLocations;
    }

    @Override
    public String toString() {
        return "ASLocationEntity{" +
                "id='" + id + '\'' +
                ", continent='" + continent + '\'' +
                ", country='" + country + '\'' +
                ", region='" + region + '\'' +
                ", city='" + city + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                '}';
    }
}
