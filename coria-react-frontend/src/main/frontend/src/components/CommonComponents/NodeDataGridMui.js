/*
Created by David Fradin, 2020
Based on example code from https://material-ui.com/components/tables
 */

import React, {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import clsx from 'clsx';
import {lighten, makeStyles} from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TablePagination from '@material-ui/core/TablePagination';
import TableRow from '@material-ui/core/TableRow';
import TableSortLabel from '@material-ui/core/TableSortLabel';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import Paper from '@material-ui/core/Paper';
import Checkbox from '@material-ui/core/Checkbox';
import IconButton from '@material-ui/core/IconButton';
import Tooltip from '@material-ui/core/Tooltip';
import DeleteIcon from '@material-ui/icons/Delete';
import {Button as MuiButton, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Grid} from "@material-ui/core";
import {get} from "lodash";
import {Loading, useDeleteMany, useNotify, useQuery, useRefresh} from "react-admin";
import DatasetGeolocationMap from "../Dataset/DatasetGeolocationMap";
import PinDropIcon from "@material-ui/icons/PinDrop";

const EnhancedTableHead = ({classes, onSelectAllClick, order, orderBy, numSelected, rowCount, onRequestSort, headerMetaData, entityName, chipFields, textFields, ...props}) => {
    const createSortHandler = (property) => (event) => {
        onRequestSort(event, property);
    };

    return (
        <TableHead>
            <TableRow>
                <TableCell padding="checkbox" className={classes.headerCell}>
                    <Checkbox
                        indeterminate={numSelected > 0 && numSelected < rowCount}
                        checked={rowCount > 0 && numSelected === rowCount}
                        onChange={onSelectAllClick}
                        inputProps={{'aria-label': 'select all'}}
                    />
                </TableCell>
                {entityName === "Edge" && <TableCell className={classes.headerCell}/>}
                {chipFields && chipFields.map((attrKey, attrIndex) =>
                        <TableCell key={attrKey.source} align={'center'} padding={'default'} sortDirection={orderBy === attrKey.source ? order : false} className={classes.headerCell}>
                            <TableSortLabel
                                active={orderBy === attrKey.source}
                                direction={orderBy === attrKey.source ? order : 'asc'}
                                onClick={createSortHandler(attrKey.source)}
                            >
                                {attrKey.label}
                                {orderBy === attrKey.source ? (
                                    <span className={classes.visuallyHidden}>
                  {order === 'desc' ? 'sorted descending' : 'sorted ascending'}
                </span>
                                ) : null}
                            </TableSortLabel>
                        </TableCell>
                )}
                {textFields && textFields.map((attrKey, attrIndex) =>
                        <TableCell key={attrKey.source} align={'center'} padding={'default'} sortDirection={orderBy === attrKey.source ? order : false} className={classes.headerCell}>
                            <TableSortLabel
                                active={orderBy === attrKey.source}
                                direction={orderBy === attrKey.source ? order : 'asc'}
                                onClick={createSortHandler(attrKey.source)}
                            >
                                {attrKey.label}
                                {orderBy === attrKey.source ? (
                                    <span className={classes.visuallyHidden}>
                  {order === 'desc' ? 'sorted descending' : 'sorted ascending'}
                </span>
                                ) : null}
                            </TableSortLabel>
                        </TableCell>
                )}
                {headerMetaData.map((headCell) => {
                    const key = headCell.length === 2 ? headCell[0] + headCell[1] : headCell[0];
                    return (
                        <TableCell key={key} align={'center'} padding={headCell.disablePadding ? 'none' : 'default'} sortDirection={orderBy === key ? order : false} className={classes.headerCell}>
                            <TableSortLabel
                                active={orderBy === key}
                                direction={orderBy === key ? order : 'asc'}
                                onClick={createSortHandler(key)}
                            >
                                {headCell.length === 2 ? headCell[1] : headCell[0]}
                                {orderBy === key ? (
                                    <span className={classes.visuallyHidden}>
                  {order === 'desc' ? 'sorted descending' : 'sorted ascending'}
                </span>
                                ) : null}
                            </TableSortLabel>
                        </TableCell>
                    )
                })}
            </TableRow>
        </TableHead>
    );
}

EnhancedTableHead.propTypes = {
    classes: PropTypes.object.isRequired,
    selectedRowIds: PropTypes.array.isRequired,
    onRequestSort: PropTypes.func.isRequired,
    onSelectAllClick: PropTypes.func.isRequired,
    order: PropTypes.oneOf(['asc', 'desc']).isRequired,
    orderBy: PropTypes.string.isRequired,
    rowCount: PropTypes.number.isRequired,
};

const useToolbarStyles = makeStyles((theme) => ({
    root: {
        paddingLeft: theme.spacing(2),
        paddingRight: theme.spacing(1),
    },
    highlight:
        theme.palette.type === 'light'
            ? {
                color: theme.palette.secondary.main,
                backgroundColor: lighten(theme.palette.secondary.light, 0.85),
            }
            : {
                color: theme.palette.text.primary,
                backgroundColor: theme.palette.secondary.dark,
            },
    title: {
        //flex: '1 1 100%',
    },
    deleteButton: {
        color: '#f44336'
    }
}));

const EnhancedTableToolbar = ({selectedRowIds, entityName, reloadCallback}) => {
    const notify = useNotify();
    const classes = useToolbarStyles();
    const [deleteMany, {data, loading, loaded, error}] = useDeleteMany(entityName, selectedRowIds, {
        //undoable: true,
        onSuccess: (event) => {
            console.log(data, loading, loaded, error);
            reloadCallback();
            notify("Successfully deleted " + entityName + (selectedRowIds.length > 1 ? "s" : ""))
        }
    });
    return (
        <Toolbar
            className={clsx(classes.root, {
                [classes.highlight]: selectedRowIds.length > 0,
            })}
        >
            {loading ? <Loading/> :
                error ? <Grid container>
                        <Grid item xs={12} md={6}><h3>An error occurred</h3>
                            <div>{JSON.stringify(error)}</div>
                        </Grid>
                    </Grid> :
                    <>{selectedRowIds.length > 0 && <Typography className={classes.title} color="inherit" variant="subtitle1" component="div">
                        {selectedRowIds.length} selected
                    </Typography>}

                        {selectedRowIds.length > 0 && entityName !== "ShortestPathLength" && (
                            <Tooltip title="Delete">
                                <IconButton aria-label="delete" className={classes.deleteButton} onClick={() => {
                                    //console.log("delete", selectedRowIds);
                                    deleteMany();
                                }}>
                                    <DeleteIcon/>
                                </IconButton>
                            </Tooltip>
                        )}</>}
        </Toolbar>
    );
};

EnhancedTableToolbar.propTypes = {
    numSelected: PropTypes.number.isRequired,
    entityName: PropTypes.string.isRequired
};

const useStyles = makeStyles((theme) => ({
    root: {
        width: '100%',
    },
    paper: {
        width: '100%',
        marginBottom: theme.spacing(2),
    },
    table: {
        minWidth: 750,
    },
    visuallyHidden: {
        border: 0,
        clip: 'rect(0 0 0 0)',
        height: 1,
        margin: -1,
        overflow: 'hidden',
        padding: 0,
        position: 'absolute',
        top: 20,
        width: 1,
    },
    headerCell: {
        "&, & *, & .MuiTableSortLabel-active, & .MuiTableSortLabel-root:hover": {
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

const NodeDataGridMui = ({subArrayProps, entityName, chipFields, textFields, ...props}) => {
    //console.log(subArrayProps, entityName, props);
    const classes = useStyles();
    const [order, setOrder] = useState('asc');
    const [orderBy, setOrderBy] = useState('name');
    const [selectedRowIds, setSelectedRowIds] = useState([]);
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(25);
    const [headerMetaData, setHeaderMetaData] = useState([]);

    const refresh = useRefresh();
    const [mapDialog, setMapDialog] = useState({open: false, record: {}});
    const closeDialog = () => setMapDialog({open: false, record: {}});
    const {data, total, loading, error} = useQuery({
        type: 'GET_MANY_REFERENCE',
        resource: entityName,
        payload: {
            target: 'datasetId',
            id: props.record.id,
            pagination: {page: page + 1, perPage: rowsPerPage},
            //sort: {field: props.entityName === "Metric" ? 'status' : 'name', order: 'ASC'},
            sort: {field: entityName === "ShortestPathLength" ? "nodeSource.name" : "name", order: "ASC"}
            //filter: {}, //optional filter
        }
    });


    useEffect(() => {
        let _headerMetadata = [];
        let keys = {};
        // console.log("data=", data);
        // Pivots attributes and metric results into key-value maps to display them all in one table
        if (subArrayProps && data) {
            subArrayProps.forEach((entityProp) => {
                Object.keys(data).forEach((entityId) => {
                    data[entityId][entityProp].forEach((attrItem) => {
                        const attrKey = get(attrItem, entityProp === 'metricResults' ? 'metric.metricAlgorithmImplementation.id' : 'key');
                        if (!keys[entityProp])
                            keys[entityProp] = [];
                        if (!keys[entityProp].includes(attrKey)) {
                            keys[entityProp].push(attrKey);
                        }
                        if (!data[entityId][entityProp + "Map"])
                            data[entityId][entityProp + "Map"] = {};
                        data[entityId][entityProp + "Map"][attrKey] = attrItem.value;
                    });
                });
            });
            subArrayProps.forEach((entityProp) => {
                if (keys[entityProp]) {
                    keys[entityProp] = keys[entityProp].sort();
                    keys[entityProp].forEach((attrKey) => _headerMetadata.push([entityProp + "Map", attrKey,]));
                }
            });
        }
        setHeaderMetaData(_headerMetadata);
    }, [data, subArrayProps]);


    const handleRequestSort = (event, property) => {
        const isAsc = orderBy === property && order === 'asc';
        setOrder(isAsc ? 'desc' : 'asc');
        setOrderBy(property);
    };

    const handleSelectAllClick = (event) => {
        if (event.target.checked) {
            const newSelecteds = data.map((n) => n.id);
            setSelectedRowIds(newSelecteds);
            return;
        }
        setSelectedRowIds([]);
    };

    const handleClick = (event, name) => {
        const selectedIndex = selectedRowIds.indexOf(name);
        let newSelected = [];

        if (selectedIndex === -1) {
            newSelected = newSelected.concat(selectedRowIds, name);
        } else if (selectedIndex === 0) {
            newSelected = newSelected.concat(selectedRowIds.slice(1));
        } else if (selectedIndex === selectedRowIds.length - 1) {
            newSelected = newSelected.concat(selectedRowIds.slice(0, -1));
        } else if (selectedIndex > 0) {
            newSelected = newSelected.concat(
                selectedRowIds.slice(0, selectedIndex),
                selectedRowIds.slice(selectedIndex + 1),
            );
        }

        setSelectedRowIds(newSelected);
    };

    const handleChangePage = (event, newPage) => {
        setPage(newPage);
    };

    const handleChangeRowsPerPage = (event) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };

    const isSelected = (name) => selectedRowIds.indexOf(name) !== -1;

    return (
        <div className={classes.root}>
            <Paper className={classes.paper}>
                {loading ? <Loading/> :
                    error ? <Grid container>
                        <Grid item xs={12} md={6}><h3>An error occurred</h3>
                            <div>{JSON.stringify(error)}</div>
                        </Grid>
                    </Grid> : <><TableContainer>
                        <Table
                            className={classes.table}
                            aria-labelledby="tableTitle"
                            size={'medium'}
                            aria-label="enhanced table"
                        >
                            <EnhancedTableHead
                                classes={classes}
                                numSelected={selectedRowIds.length}
                                order={order}
                                orderBy={orderBy}
                                onSelectAllClick={handleSelectAllClick}
                                onRequestSort={handleRequestSort}
                                rowCount={total || 0}
                                headerMetaData={headerMetaData}
                                entityName={entityName}
                                chipFields={chipFields}
                                textFields={textFields}
                            />
                            <TableBody>
                                {data && data.map((row, index) => {
                                    const isItemSelected = isSelected(row.id);
                                    const labelId = `enhanced-table-checkbox-${index}`;
                                    //console.log(data);
                                    return (
                                        <TableRow
                                            hover
                                            onClick={(event) => handleClick(event, row.id)}
                                            role="checkbox"
                                            aria-checked={isItemSelected}
                                            tabIndex={-1}
                                            key={index}
                                            selected={isItemSelected}
                                        ><TableCell padding="checkbox">
                                            <Checkbox
                                                checked={isItemSelected}
                                                inputProps={{'aria-labelledby': labelId}}
                                            />
                                        </TableCell>{entityName === "Edge" && <TableCell>
                                            <NodeDataGridEdgeOpenMapButton
                                                key="map" label="Open Map" title="Open Map" record={row}
                                                onClick={() => setMapDialog({open: true, record: row})}/>
                                        </TableCell>}
                                            {chipFields && chipFields.map((attrKey, attrIndex) => <TableCell key={attrIndex} component="th" id={labelId} scope="row" padding="none" align="center">
                                                <Chip label={get(row, attrKey.source)}/>
                                            </TableCell>)}
                                            {textFields && textFields.map((attrKey, attrIndex) => <TableCell key={attrIndex} id={labelId} scope="row" padding="none" align="center">
                                                {get(row, attrKey.source)}
                                            </TableCell>)}
                                            {headerMetaData && headerMetaData.map((attrKey, attrIndex) => (row[attrKey[0]] ? <TableCell key={attrKey[0] + attrKey[1]} align="center">{attrKey[0] === "metricResultsMap" ?
                                                Number(row[attrKey[0]] && row[attrKey[0]][attrKey[1]].toFixed(6)) : // 6 Digit rounding
                                                (row[attrKey[0]] && row[attrKey[0]][attrKey[1]])}</TableCell> : ""))}</TableRow>
                                    );
                                })}
                            </TableBody>
                        </Table>
                    </TableContainer>
                        <EnhancedTableToolbar selectedRowIds={selectedRowIds} entityName={entityName} reloadCallback={() => {
                            console.log("refresh");
                            refresh();
                        }
                        }/>
                        <TablePagination
                            rowsPerPageOptions={[10, 25, 50, 100, 200]}
                            component="div"
                            count={total || 0}
                            rowsPerPage={rowsPerPage}
                            page={page}
                            onChangePage={handleChangePage}
                            onChangeRowsPerPage={handleChangeRowsPerPage}
                        />
                    </>}
            </Paper>
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
        </div>
    );
}

const GeolocationTechniqueLabels = {
    bc: "BGP Communities",
    lg: "Looking Glass servers",
    mlp: "Multilateral Peering",
    idk: "Macroscopic Internet Topology Data Kit"
}

const NodeDataGridEdgeOpenMapButton = (props) => props.record ? <MuiButton disabled={!props.record.locations || !props.record.locations.length}
                                                                           color="primary"
                                                                           title={props.title}
                                                                           onClick={(event) => props.onClick(event, props.record)}><PinDropIcon/> {props.record.locations && props.record.locations.length ? props.label : "No Geolocation"}</MuiButton> : null;


export default NodeDataGridMui;