/**
 * Created by David Fradin, 2020
 */

import React, {useState} from "react";
import CytoscapeComponent from "react-cytoscapejs";
import {Loading, useQuery} from "react-admin";
import {Select, MenuItem, Grid} from '@material-ui/core';

export const DatasetGraphVisualization = (props) => {
    if (!props.record.id) {
        return "No dataset selected.";
    }
    return <DatasetGraphVisualizationInner {...props} />;
};


const DatasetGraphVisualizationInner = (props) => {

    const [selectedMetric, setSelectedMetric] = useState("No layout selected");

    const {data: nodes, nodesTotal, nodesLoading, nodesError} = useQuery({
        type: 'GET_MANY_REFERENCE',
        resource: "Node",
        payload: {
            target: 'datasetId',
            id: props.record.id,
            filter: {},
            sort: {'field': 'name', 'order': 'ASC'}
        }
    });

    const {data: edges, edgesTotal, edgesLoading, edgesError} = useQuery({
        type: 'GET_MANY_REFERENCE',
        resource: "Edge",
        payload: {
            target: 'datasetId',
            id: props.record.id,
            filter: {},
            sort: {'field': 'name', 'order': 'ASC'}
        }
    });

    //console.log(nodes);
    let elements = [];
    let availableLayoutMetrics = {};
    if (nodes) {
        let xMin, xMax, yMin, yMax;
        nodes.forEach((node) => node.layoutPositions.forEach((pos) => {
            if (pos.metric.id === selectedMetric) {
                if (xMin === undefined || xMin > pos.x)
                    xMin = pos.x;
                if (xMax === undefined || xMax < pos.x)
                    xMax = pos.x;
                if (yMin === undefined || yMin > pos.y)
                    yMin = pos.y;
                if (yMax === undefined || yMax < pos.y)
                    yMax = pos.y;
                //console.log(pos.x, pos.y);
            }
        }));
        if (xMin === undefined || xMax === undefined || yMin === undefined || yMax === undefined) {
            xMin = xMax = yMin = yMax = 0;
        }
        //console.log("min max", xMin, xMax, yMin, yMax);
        const xRangeSrc = (xMax - xMin) || 1, yRangeSrc = (yMax - yMin) || 1;

        const xOffsetTarget = 100, yOffsetTarget = 50;
        const xRangeTarget = 800 - xOffsetTarget * 2, yRangeTarget = 800 - yOffsetTarget * 2;

        nodes.forEach((node) => node.layoutPositions.forEach((pos) => {
            //console.log(pos.metric.id, pos.metric.metricAlgorithmImplementation.id);
            availableLayoutMetrics[pos.metric.id] = pos.metric.metricAlgorithmImplementation.id;
            if (selectedMetric === "No layout selected")
                setSelectedMetric(pos.metric.id);
            if (pos.metric.id === selectedMetric) {
                //console.log(xRangeTarget * (pos.x - xMin) / (xRangeSrc) + xOffsetTarget, yRangeTarget * (pos.y - yMin) / (yRangeSrc) + yOffsetTarget);
                elements.push({
                    data: {
                        id: node.name,
                        label: node.name
                    },
                    position: {
                        x: xRangeTarget * (pos.x - xMin) / (xRangeSrc) + xOffsetTarget,
                        y: yRangeTarget * (pos.y - yMin) / (yRangeSrc) + yOffsetTarget
                    }
                });
            }
        }));

    }

    if (edges && elements.length)
        edges.forEach((edge) => {
            const parts = edge.name.split(" <-> ");
            elements.push({data: {source: parts[0], target: parts[1], label: edge.name}})
        });

    const handleSelectedMetricChange = (event) => {
        setSelectedMetric(event.target.value);
    };

    return nodesLoading || edgesLoading ? <Loading/> :
        nodesError || edgesError ? <Grid container>
                <Grid item xs={12} md={6}><h3>An error occurred</h3>
                    <div>{JSON.stringify(nodesError)}</div>
                    <div>{JSON.stringify(edgesError)}</div>
                </Grid>
            </Grid> :
            <div>
                <p>This visualization is powered by <a href={"js.cytoscape.org"}>Cytoscape.js</a>, a library for graph network visualisations. In order to display nodes on this page, you need to run one of the provided layout metrics:</p>
                <ol>
                    <li>Go to tab "Metrics"</li>
                    <li>Scroll down and click on "Add a metric"</li>
                    <li>In the <i>Run a metric</i> dialog, select one of the metrics starting with "Layout Position" e.g. "Layout Position Circular" and click "RUN".</li>
                    <li>Calculation times vary depending on the dataset size. Once the calculation of this metric has finished, switch to tab <i>Graph Visualization</i> and select the newly calculated layout from the dropdown.</li>
                </ol>
                <Select value={selectedMetric} onChange={handleSelectedMetricChange}>
                    {!(availableLayoutMetrics && Object.keys(availableLayoutMetrics).length) && <MenuItem value={"No layout selected"} key={"No layout selected"}>No layout selected</MenuItem>}
                    {Object.keys(availableLayoutMetrics).map((key) => <MenuItem value={key} key={key}>{availableLayoutMetrics[key]}</MenuItem>)}
                </Select>
                {nodesTotal && edgesTotal && <p>Displaying {nodesTotal} nodes and {edgesTotal} edges.</p>}

                <CytoscapeComponent elements={elements} style={{width: '100%', height: '800px'}}/></div>;
};


export default DatasetGraphVisualization;