/**
 * Created by David Fradin, 2020
 */

import React, {useCallback} from "react";
import {
    Button, Datagrid, DeleteButton, Filter, List, Pagination, TextField, TextInput, useListContext, TopToolbar, sanitizeListRestProps, downloadCSV
} from "react-admin";
import {get} from "lodash";
import {Button as MuiButton, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid, makeStyles, Tooltip, Typography} from "@material-ui/core";
import PinDropIcon from "@material-ui/icons/PinDrop";
import DatasetGeolocationMap from "../Dataset/DatasetGeolocationMap";
import NumberFieldWithParse from "./NumberFieldWithParse";
import {cloneElement} from 'react';
import {fetchRelatedRecords, useDataProvider, useNotify} from "ra-core";
import DownloadIcon from '@material-ui/icons/GetApp';
import jsonExport from 'jsonexport/dist';

const useStylesNodeDataGrid = makeStyles((theme) => ({
    headerCell: {
        "&, & *": {
            fontWeight: 'bold',
            color: 'white',
            backgroundColor: '#2196f3',
        },
    },
    chipContainer: {
        display: 'inline-flex',
        '& > *': {
            margin: theme.spacing(0.5),
        },
    }
}));

const NodeDataGridFilter = (props) => {
    //console.log(props);
    return (
        <Filter {...props}>
            {props.resource === "ShortestPathLength" && <TextInput label="Filter by Implementation" source="metricAlgorithmImplementationId" alwaysOn/>}
            {props.resource === "Node" && <TextInput label="Filter by node name" source="name" alwaysOn/>}
            {props.resource === "Edge" && <TextInput label="Filter by edge name" source="name" alwaysOn/>}
        </Filter>
    );
}

const ListActions = ({className, exporter, filters, maxResults, permanentFilter, ...rest}) => {
    const {currentSort, resource, displayedFilters, filterValues, showFilter, total} = useListContext();
    console.log({...filterValues, ...permanentFilter});
    return (
        <TopToolbar className={className} {...sanitizeListRestProps(rest)}>
            {filters && cloneElement(filters, {
                resource,
                showFilter,
                displayedFilters,
                filterValues,
                context: 'button',
            })}
            <ExportButton
                disabled={total === 0}
                resource={resource}
                sort={currentSort}
                filterValues={{...filterValues, ...permanentFilter}}
                maxResults={maxResults}
            />
        </TopToolbar>
    );
};

/**
 * Source for this ExportButton element was taken from ra-ui-materialui/src/button/ExportButton.tsx.
 * Fixed a bug where permanentFilters provided via the filter={} attribute in <List /> element was ignored in ExportButton queries.
 * @param props
 * @returns {JSX.Element}
 * @constructor
 */
const ExportButton = props => {
    //console.log(props);
    const {
        maxResults = 1e7,
        onClick,
        label = 'ra.action.export',
        exporter: customExporter,
        sort,
        ...rest
    } = props;
    const {
        resource,
        currentSort,
        exporter: exporterFromContext,
        total,
    } = useListContext(props);
    const exporter = customExporter || exporterFromContext;
    const dataProvider = useDataProvider();
    const notify = useNotify();
    const handleClick = useCallback(
        event => {
            dataProvider
                .getList(resource, {
                    sort: currentSort || sort,
                    filter: rest.filterValues,
                    pagination: {page: 1, perPage: maxResults},
                })
                .then(
                    ({data}) => exporter && exporter(data, fetchRelatedRecords(dataProvider), dataProvider, resource))
                .catch(error => {
                    console.error(error);
                    notify('ra.notification.http_error', 'warning');
                });
            if (typeof onClick === 'function') {
                onClick(event);
            }
        },
        [currentSort, dataProvider, exporter, rest.filterValues, maxResults, notify, onClick, resource, sort,]
    );

    return (
        <Button
            onClick={handleClick}
            label={label}
            disabled={total === 0}
            {...sanitizeRestProps(rest)}
        >
            <DownloadIcon/>
        </Button>
    );
};

const sanitizeRestProps = ({basePath, filterValues, resource, sort, maxResults, label, exporter, ...rest}) => rest;

const NodeDataGrid = ({entityName, subArrayProps, ...props}) => {
    console.log("[NodeDataGridProps]", props);
    const classes = useStylesNodeDataGrid();
    const [mapDialog, setMapDialog] = React.useState({open: false, record: {}});
    const closeDialog = () => setMapDialog({open: false, record: {}});

    const exporter = items => {
        console.log(items);
        let headers = [entityName];
        const rowsForExport = items.map(row => {
            console.log(row);
            Object.keys(row.metricResultsMap).forEach(key => (headers.indexOf(key) === -1) && headers.push(key));
            return {[entityName]: row.name, ...row.metricResultsMap};
        });

        jsonExport(rowsForExport, {
            headers // order fields in the export
        }, (err, csv) => {
            downloadCSV(csv, 'rows'); // download as 'rows.csv` file
        });
    };

    let renderedTextFields = [props.children];
    if (entityName === "Edge")
        renderedTextFields.push(<NodeDataGridEdgeOpenMapButton
            key="map" label="Open Map" title="Open Map"
            onClick={(event, edgeRecord) => setMapDialog({open: true, record: edgeRecord})}/>);
    if (entityName !== "ShortestPathLength")
        renderedTextFields.push(<DeleteButton key="delete" label="Delete" redirect={false} undoable={false}/>);
    console.log(entityName);
    return <><List
        hasCreate={false} hasEdit={false} hasList={true} hasShow={false}
        resource={entityName}
        basePath={'/Node'}
        filter={{datasetId: props.record.id}}
        filters={<NodeDataGridFilter/>}
        sort={{field: entityName === "ShortestPathLength" ? "nodeSource.name" : "name", order: "ASC"}}
        exporter={exporter}
        actions={<ListActions
            filters={<NodeDataGridFilter/>}
            permanentFilter={{datasetId: props.record.id}}
        />}
        bulkActionButtons={false}
        pagination={<Pagination/>}>
        <NodeDataGridInner renderedTextFields={renderedTextFields} subArrayProps={subArrayProps}/>
    </List>

        {entityName === "Edge" &&
        <Dialog onClose={closeDialog} aria-labelledby="edge-map-dialog-title" open={mapDialog.open} maxWidth={"xl"}
                fullWidth>
            {mapDialog.record && mapDialog.record.locations && <>
                <DialogTitle id="edge-map-dialog-title" onClose={closeDialog}>
                    Geographic annotation for the AS relationship between {mapDialog.record.name}
                </DialogTitle>
                <DialogContent dividers>
                    <Typography gutterBottom>
                        How does CAIDA collect and assign this information? See <a
                        href="https://www.caida.org/data/as-relationships-geo/">CAIDA.org</a>
                    </Typography>
                    <Grid container>
                        <Grid item xs={12} sm={4}>
                            <h4>{mapDialog.record.locations.length} Known
                                Location{mapDialog.record.locations.length > 1 && "s"} for this AS link:</h4>
                            <ol>{mapDialog.record.locations.map((edgeLocation, index) =>
                                <li key={index}>
                                    {edgeLocation.location.country} - {edgeLocation.location.region} - {edgeLocation.location.city}
                                    <div className={classes.chipContainer}>{edgeLocation.source.split(',')
                                        .map((sourceName, index1) => <Tooltip key={index1}
                                                                              title={GeolocationTechniqueLabels[props.label] || ""}><Chip
                                            size="small"
                                            label={sourceName}/></Tooltip>)
                                    }</div>
                                </li>)
                            }</ol>
                        </Grid>
                        <Grid item xs={12} sm={8}>
                            <DatasetGeolocationMap locations={mapDialog.record.locations}/>
                        </Grid>
                    </Grid>
                </DialogContent>
            </>}
            <DialogActions>
                <MuiButton autoFocus onClick={closeDialog} color="primary">Close</MuiButton>
            </DialogActions>
        </Dialog>}
    </>;
}

const NodeDataGridInner = ({renderedTextFields, subArrayProps, ...props}) => {
    let _renderedTextFields = [...renderedTextFields];
    const classes = useStylesNodeDataGrid();
    let keys = {};
    // console.log("data=", props.data);
    // Pivots attributes and metric results into key-value maps to display them all in one table
    if (subArrayProps) {
        subArrayProps.forEach((entityProp) => {
            Object.keys(props.data).forEach((entityId) => {
                props.data[entityId][entityProp].forEach((attrItem) => {
                    const attrKey = get(attrItem, entityProp === 'metricResults' ? 'metric.metricAlgorithmImplementation.id' : 'key');
                    if (!keys[entityProp])
                        keys[entityProp] = [];
                    if (!keys[entityProp].includes(attrKey)) {
                        keys[entityProp].push(attrKey);
                    }
                    if (!props.data[entityId][entityProp + "Map"])
                        props.data[entityId][entityProp + "Map"] = {};
                    props.data[entityId][entityProp + "Map"][attrKey] = attrItem.value;
                });
            });
        });
        subArrayProps.forEach((entityProp) => {
            if (keys[entityProp]) {
                keys[entityProp] = keys[entityProp].sort();
                keys[entityProp].forEach((attrKey) =>
                    _renderedTextFields.push(entityProp === "metricResults" ?
                        <NumberFieldWithParse key={attrKey} source={`${entityProp + "Map"}[${attrKey}]`} label={attrKey} options={{maximumFractionDigits: 6}}/> :
                        <TextField key={attrKey} source={`${entityProp + "Map"}[${attrKey}]`} label={attrKey} emptyText="-"/>));
            }
        });
    }
    // console.log(keys);
    return <Datagrid
        isRowSelectable={() => false}
        currentSort={{field: 'id', order: 'ASC'}}
        {...props}
        resource={props.entityName}
        classes={{headerCell: classes.headerCell}}
    >{_renderedTextFields}</Datagrid>
}

const NodeDataGridEdgeOpenMapButton = (props) =>
    <Button disabled={!props.record.locations || !props.record.locations.length}
            label={props.record.locations && props.record.locations.length ? props.label : "No Geolocation"}
            title={props.title}
            onClick={(event) => props.onClick(event, props.record)}><PinDropIcon/></Button>

const GeolocationTechniqueLabels = {
    bc: "BGP Communities",
    lg: "Looking Glass servers",
    mlp: "Multilateral Peering",
    idk: "Macroscopic Internet Topology Data Kit"
}


export default NodeDataGrid;