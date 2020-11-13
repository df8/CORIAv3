/**
 * Created by David Fradin, 2020
 */

import React from 'react';

import {List, Datagrid, TextField, TextInput, Filter} from 'react-admin';
import {makeStyles} from '@material-ui/core/styles';
import {Paper, Grid, Tooltip, Table, TableBody, TableRow, TableCell, TableHead} from '@material-ui/core';
import HelpIcon from "@material-ui/icons/Help";
import CheckCircleIcon from "@material-ui/icons/CheckCircle";
import ErrorIcon from "@material-ui/icons/Error";
import MetricAlgorithmImplementationSpeedIcon from "../CommonComponents/MetricAlgorithmImplementationSpeedIcon";

const MetricAlgorithmFilter = props => (
    <Filter {...props}>
        <TextInput label="Search" source="q" alwaysOn/>
    </Filter>
);

const MetricAlgorithmList = props => {
    return (
        <List {...props} filters={<MetricAlgorithmFilter/>} title={props.options ? props.options.label : props.resource} perPage={25}>
            <Datagrid {...props} isRowSelectable={() => false} expand={<MetricAlgorithmExpand/>}>
                <TextField source="name"/>
                <TextField source="type" label={<span>Metric Result Type&nbsp;
                    <Tooltip placement="right"
                             title={<div>
                                 <p><b>Dataset:</b> This metric calculates and returns a single number for the entire dataset.</p>
                                 <p><b>Node:</b> This metric calculates and returns a number for every individual node.</p>
                                 <p><b>Edge:</b> This metric calculates and returns a number for every individual edge.</p>
                                 <p><b>Layout Position:</b> This metric calculates a tuple of X and Y coordinates for each node in order to arrange all nodes on a network graph plot.</p>
                                 <p><b>Shortest Path Length:</b> This metric calculates a distance (number of hops) for every possible pair of nodes.</p>
                             </div>}>
                        <HelpIcon/>
                    </Tooltip></span>}/>
            </Datagrid>

        </List>
    );
}

const useStylesExpand = makeStyles(theme => ({
    "p-1": {
        padding: '1rem',
    },
    "mx-1": {
        marginLeft: '1rem',
        marginRight: '1rem',
    },
    "mavPaper": {
        padding: '1rem',
        margin: '1rem',
    },
    head: {
        backgroundColor: theme.palette.common.black,
        color: theme.palette.common.white,
    },
    "green": {
        color: "green"
    },
    "red": {
        color: "red"
    }
}));

const MetricAlgorithmExpand = ({record}) => {
    const classes = useStylesExpand();
    //const toTitleCase = (str) => str[0].toUpperCase() + str.slice(1);
    return <Paper elevation={3} classes={{root: classes["p-1"]}}>
        <Grid container spacing={3} direction={"column"}>
            <Grid item xs={12}>
                <h2>Algorithm Description</h2>
                <div dangerouslySetInnerHTML={{__html: record.description}} className={classes["p-1"]}/>
            </Grid>
            <Grid item xs={12}>
                <h2>Algorithm Variants</h2>
                <div className={classes["p-1"]}>{record.metricAlgorithmVariants.map((mav) => {
                    return <div key={mav.id}>
                        <Paper elevation={5} className={classes["mavPaper"]}>
                            <h3>{record.name}/{mav.name}</h3>
                            {mav.description && mav.description.length && <>
                                <h4>Description</h4>
                                <div dangerouslySetInnerHTML={{__html: mav.description}} className={classes["p-1"]}/>
                            </>}
                            {mav.parameters && mav.parameters.length && <>
                                <h4>Parameters</h4>
                                <Table>
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Name</TableCell>
                                            <TableCell>Description</TableCell>
                                            <TableCell>Type</TableCell>
                                            <TableCell>Required</TableCell>
                                            <TableCell>Default Value</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {mav.parameters.map(mai => <TableRow>
                                            <TableCell>{mai['id']}</TableCell>
                                            <TableCell>{mai['description']}</TableCell>
                                            <TableCell>{mai['type']}</TableCell>
                                            <TableCell>{mai['isRequired'] ? "Yes" : "No"}</TableCell>
                                            <TableCell>{mai['defaultValue']}</TableCell>
                                        </TableRow>)}
                                    </TableBody>
                                </Table>
                            </>}

                            <h4>Available Implementations</h4>
                            {mav.implementations && mav.implementations.length ? <Table>
                                <TableHead>
                                    <TableRow>
                                        <TableCell>ID</TableCell>
                                        <TableCell>Technology</TableCell>
                                        <TableCell>Provider/Library</TableCell>
                                        <TableCell>Relative Calculation Speed</TableCell>
                                        <TableCell>Available</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {mav.implementations.map(mai => <TableRow>
                                        <TableCell>{mai['id']}</TableCell>
                                        <TableCell>{mai['technology']}</TableCell>
                                        <TableCell>{mai['provider']}</TableCell>
                                        <TableCell><MetricAlgorithmImplementationSpeedIcon speed={mai['speedIndex']}/></TableCell>
                                        <TableCell>{mai['available'] ? <CheckCircleIcon className={classes.green}/> :
                                            (mai['unavailableReason'] ?
                                                <Tooltip placement="right" title={<p>Reason: {mai['unavailableReason']}</p>}>
                                                    <ErrorIcon className={classes.red}/>
                                                </Tooltip> :
                                                <ErrorIcon className={classes.red}/>)
                                        }</TableCell>
                                    </TableRow>)}
                                </TableBody>
                            </Table> : <p>There are no implementations available for this metric variant.</p>}
                            <><h4>Dependencies on Other Metrics &nbsp;
                                <Tooltip placement="right"
                                         title={<p>When you launch the metric variant {record.name}/{mav.name}, the following list of metrics will be calculated beforehand.
                                             These dependencies may also have further dependencies, leading to a larger dependency tree and hence an
                                             automatically generated execution plan that will run and store intermediate results for each computed metric variant.</p>}>
                                    <HelpIcon/>
                                </Tooltip></h4>

                                {mav.dependencies ? <Table>
                                    <TableBody>
                                        {mav.dependencies.map(mavd => <TableRow key={mavd.id}>
                                            <TableCell>{mavd.metricAlgorithm.name}/{mavd.name}</TableCell>
                                        </TableRow>)}
                                    </TableBody>
                                </Table> : <p>This metric variant has no dependencies.</p>}</>
                        </Paper>
                    </div>;
                })}</div>
            </Grid>

        </Grid>
    </Paper>;
};

export default MetricAlgorithmList;