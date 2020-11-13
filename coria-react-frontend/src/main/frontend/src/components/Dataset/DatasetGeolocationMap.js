/**
 * Created by David Fradin, 2020
 */

import {ComposableMap, Geographies, Geography, Marker, ZoomableGroup} from "react-simple-maps";
import React, {useState} from "react";

const geoUrl = "https://raw.githubusercontent.com/zcreativelabs/react-simple-maps/master/topojson-maps/world-110m.json";
/*
Renders a number of markers on a static world map image.
Source: https://www.react-simple-maps.io/examples/custom-markers/
*/
const DatasetGeolocationMap = (props) => {

    const [zoomSettings, setZoomSettings] = useState(.5);
    //We calculate the mean vector of all coordinates and negate. This will rotate the map (around the earth's axis) such that the most markers are focused.
    const rotateMap = props.locations ? props.locations.reduce((sums, {location}) => {
        sums[0] += parseFloat(location.longitude);
        sums[1] += parseFloat(location.latitude);
        return sums;
    }, [0, 0, 0]).map(rotateMapItem => -rotateMapItem / props.locations.length) : [0, 0, 0];

    const MAX_ZOOM = 20;
    const onMapMove = (position) => {
        const newZoom = (MAX_ZOOM - position.k) * .5 / (MAX_ZOOM - 1) + .05;
        console.log(position, newZoom);
        setZoomSettings(newZoom);
    }

    return <ComposableMap projection="geoEqualEarth" projectionConfig={props.locations.length > 1 ? {scale: 100} : {rotate: rotateMap, scale: 400}}>
        <ZoomableGroup zoom={1} onMove={onMapMove} maxZoom={MAX_ZOOM}>
            <Geographies geography={geoUrl}>
                {({geographies}) => geographies.map(geo => (<Geography key={geo.rsmKey} geography={geo} fill="#EAEAEC" stroke="#D6D6DA"/>))}
            </Geographies>
            {props.locations.map(({location}, index) => (
                <Marker key={location.id} coordinates={[location.longitude, location.latitude]}>
                    <g fill="none"
                       stroke="#FF5533"
                       strokeWidth="2"
                       strokeLinecap="round"
                       strokeLinejoin="round"
                       transform={"translate(" + (-12 * zoomSettings) + ", " + (-24 * zoomSettings) + ") scale(" + zoomSettings + ")"}>
                        <circle cx="12" cy="10" r="3"/>
                        <path d="M12 21.7C17.3 17 20 13 20 10a8 8 0 1 0-16 0c0 3 2.7 6.9 8 11.7z"/>
                    </g>
                    <text textAnchor="middle" y={12}
                          transform={"scale(" + zoomSettings + ")"}
                          style={{fontFamily: "system-ui", fill: "#5D5A6D"}}>{props.locations.length > 2 ? index + 1 : location.city + ", " + location.region + ", " + location.country}</text>
                </Marker>
            ))}
        </ZoomableGroup>
    </ComposableMap>;
}

export default DatasetGeolocationMap;