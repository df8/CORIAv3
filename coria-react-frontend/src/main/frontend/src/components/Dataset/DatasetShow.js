/**
 * Created by David Fradin, 2020
 */

import {makeStyles} from "@material-ui/styles";
import {ArrayField, Button, ChipField, Datagrid, DateField, DeleteButton, EditButton, ReferenceManyField, Show, SimpleShowLayout, Tab, TabbedShowLayout, TextField, TopToolbar, useRefresh} from "react-admin";
import NodeDataGrid from "../CommonComponents/NodeDataGrid";
import React, {useState} from "react";
import BlurOnIcon from "@material-ui/icons/BlurOn";
import * as moment from "moment";
import {Dialog, DialogContent, Grid, Tooltip} from "@material-ui/core";
import HelpIcon from "@material-ui/icons/Help";
import DatasetTitle from "./DatasetTitle";
import DownloadIcon from "@material-ui/icons/GetApp";
import NumberFieldWithParse from "../CommonComponents/NumberFieldWithParse";
import MetricCreate from "../Metric/MetricCreate";
import DatasetShowTabExport from "./DatasetShowTabExport";
import DatasetGraphVisualization from "./DatasetShowTabGraphVisualization";
import {get} from "lodash";

const useStylesDatasetShow = makeStyles({
    headerCell: {
        fontWeight: 'bold',
        color: 'white',
        backgroundColor: '#2196f3',
        "& > .MuiTableSortLabel-root": {
            color: "inherit",
            "& > .MuiTableSortLabel-icon": {
                color: "inherit !important"
            }
        },
    },
    table: {
        'width': '90%',
        '& * th:first-child': {
            'width': '1%'
        }
    },
});

const DatasetShow = props => {
    const classes = useStylesDatasetShow();
    const [dialogState, setDialogState] = useState({open: false});
    const refresh = useRefresh();

    const DatasetCreateMetricDialog_handleClose = (action, event) => {
        console.log(action, event);
        setDialogState(prev => ({...prev, open: false}));
        if (action === 'save') {
            refresh();
            setTimeout(refresh, 60);
        }
    }

    return <>
        <Show
            {...props}
            title={<DatasetTitle/>}
            actions={<DatasetShowActions/>}>
            <TabbedShowLayout>
                <Tab label="Dataset">
                    <DatasetShowTabDataset/>
                </Tab>
                <Tab label="Metrics" path="metrics">
                    <AddMetricButton onClick={() => {
                        setDialogState(prev => ({...prev, open: true}));
                    }}/>
                    <ReferenceManyField
                        addLabel={false}
                        reference="Metric"
                        target="datasetId"
                        sort={{field: 'metricAlgorithmImplementation.id', order: 'ASC'}}
                        perPage={100}
                    >
                        <Datagrid classes={{headerCell: classes.headerCell}}>
                            <ChipField key="metricAlgorithmImplementation.id" source="metricAlgorithmImplementation.id" label={"Metric module"}/>
                            <MetricStatusTextField key="status" source="status"/>
                            <MetricDurationField key="started" source="started" label="Execution time"/>
                            <DeleteButton1 undoable={false} redirect={false}/>
                        </Datagrid>
                    </ReferenceManyField>
                    <DatasetCreateMetricDialog open={dialogState.open} onClose={DatasetCreateMetricDialog_handleClose}/>
                </Tab>
                <Tab label="Nodes" path="nodes">
                    <NodeDataGrid resource={"Node"} entityName={"Node"}
                                  subArrayProps={['attributesList', 'metricResults']}>
                        <ChipField key="name" source="name" label="Node name"/>
                    </NodeDataGrid>
                </Tab>
                <Tab label="Edges" path="edges">
                    <NodeDataGrid resource={"Edge"} entityName={"Edge"}
                                  subArrayProps={['attributesList', 'metricResults']}>
                        <ChipField key="name" source="name" label="Edge name"/>
                    </NodeDataGrid>
                </Tab>
                <Tab label="Shortest Path Lengths" path="shortest-path-lengths">
                    <NodeDataGrid resource={"ShortestPathLength"} entityName={"ShortestPathLength"}>
                        <ChipField key="metric.metricAlgorithmImplementation.id" source="metric.metricAlgorithmImplementation.id" label="Implementation"/>
                        <ChipField key="nodeSourceName" source="nodeSourceName" label="Source Node"/>
                        <ChipField key="nodeTargetName" source="nodeTargetName" label="Target Node"/>
                        <TextField key="distance" source="distance" label="Distance (number of edges)"/>
                    </NodeDataGrid>
                </Tab>
                <Tab label="Graph Visualization">
                    <DatasetGraphVisualization/>
                </Tab>
                <Tab label="Export" path="export">
                    <DatasetShowTabExport/>
                </Tab>
            </TabbedShowLayout>
        </Show>
    </>;

};

const MetricStatusTextField = props => {
    const statusText = get(props.record, props.source);
    return statusText === "FAILED" ? <span>{statusText}&nbsp;
        <Tooltip title={props.record.message}><HelpIcon/>
    </Tooltip></span> : <span>{statusText}</span>;
}

const DeleteButton1 = props => props.record ? <DeleteButton {...props}/> : null;

const DatasetShowActions = ({basePath, data, resource}) => {
    return <TopToolbar>
        {data && data.id && <Button color="primary" href={'#/Dataset/' + data.id + '/show/export'} label="Export dataset" title="Export dataset"><DownloadIcon/></Button>}
        <EditButton basePath={basePath} record={data} resource={resource} label={"Rename dataset"}/>
        <DeleteButton basePath={basePath} record={data} resource={resource} label={"Delete dataset"}/>
    </TopToolbar>;
}


const DatasetCreateMetricDialog = ({open, onClose, datasetId, ...props}) => {
    return props.record ? <Dialog open={open} anchor="right" onClose={(event) => onClose('cancel', event)} maxWidth={'lg'}>
        <DialogContent dividers>
            <MetricCreate
                onSuccess={(event) => onClose('save', event)}
                onCancel={(event) => onClose('cancel', event)}
                datasetId={props.record.id}
                basePath={"/Dataset"}
                resource={"Metric"}/>
        </DialogContent>
    </Dialog> : null;
}

const useStylesAddMetricButton = makeStyles({
    button: {
        margin: '1rem'
    }
})


const AddMetricButton = (props) => {
    const classes = useStylesAddMetricButton();
    return (
        <Button
            classes={classes}
            variant="contained"
            label="Add a metric"
            title="Add a metric"
            onClick={props.onClick}>
            <BlurOnIcon/>
        </Button>
    );
}


const MetricDurationField = (props) => {
    //console.log(props);
    if (!props.record) return "-";
    const s = moment(props.record.started);
    if (!props.record.finished)
        return <span>{(props.record.status === "SCHEDULED" ? "scheduled " : "started ") + s.fromNow()}</span>;
    let diffObj = moment.duration(moment(props.record.finished).diff(s));
    diffObj = {
        "days": diffObj.days(),
        "hours": diffObj.hours(),
        "min": diffObj.minutes(),
        's': diffObj.seconds(),
        'ms': diffObj.milliseconds()
    };
    const keys = Object.keys(diffObj);
    let formattedDifference = ""; //collect the two largest time units to be printed out.
    for (let i = 0; i < keys.length; i++) {
        if (diffObj[keys[i]] > 0) {
            formattedDifference = diffObj[keys[i]] + keys[i];
            if (i < 4 && diffObj[keys[i + 1]] > 0) formattedDifference += " " + diffObj[keys[i + 1]] + keys[i + 1];
            break;
        }
    }
    if (!formattedDifference.length)
        formattedDifference = "0ms";
    return <span>{formattedDifference}</span>;
}

export const DatasetShowTabDataset = props => {
    console.log(props);
    const classes = useStylesDatasetShow();
    return <Grid container>
        <Grid item container xs={12} md={6} direction={"column"} lg={4}>
            <h3>About this dataset</h3>
            <SimpleShowLayout {...props}>
                <TextField source="id" label={"ID"} addLabel={true}/>
                <TextField source="name"/>
                <DateField label="Created" source="created" showTime locales={['de-DE']}/>
            </SimpleShowLayout>
        </Grid>
        <Grid item xs={12} md={6} lg={4}>
            <h3>Dataset Attributes</h3>
            <ArrayField {...props} source={"attributesList"}>
                <Datagrid classes={{headerCell: classes.headerCell, table: classes.table}}>
                    <ChipField source="key"/>
                    <DatasetAttributeField source="value"/>
                </Datagrid>
            </ArrayField>
        </Grid>
        <Grid item xs={12} md={6} lg={4}>
            <Grid container direction="row" alignItems="center">
                <Grid item>
                    <h3>Dataset Metric Results</h3>
                </Grid>
                <Grid item>
                    &nbsp;
                    <Tooltip
                        title="These values were calculated by metrics that return a single result for the entire network graph.">
                        <HelpIcon/>
                    </Tooltip>
                </Grid>
            </Grid>
            <ArrayField {...props} source={"metricResults"}>
                <Datagrid classes={{headerCell: classes.headerCell, table: classes.table}}>
                    <ChipField source="metric.metricAlgorithmImplementation.id" label="Metric module"/>
                    <NumberFieldWithParse source="value" options={{maximumFractionDigits: 6}}/>
                </Datagrid>
            </ArrayField>
        </Grid>
    </Grid>;
}

const DatasetAttributeField = props => {
    if (props.record.key === "uploaded_filenames") {
        const lines = props.record[props.source].split("\t");
        return <ul style={{paddingInlineStart: 0}}>{lines.map((line, index) => <li key={index}>{line}</li>)}</ul>;
    } else {
        return <TextField {...props} />;
    }
}

export default DatasetShow;