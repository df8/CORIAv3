/**
 * Created by David Fradin, 2020
 */

import React from 'react';

import {List, Datagrid, TextField, TextInput, Filter} from 'react-admin';
import {makeStyles} from '@material-ui/core/styles';
import {Paper, Grid, Typography} from '@material-ui/core';

const CudaDeviceFilter = props => (
    <Filter {...props}>
        <TextInput label="Search" source="q" alwaysOn/>
    </Filter>
);

const CudaDeviceList = props => {
    return (
        <List {...props} filters={<CudaDeviceFilter/>} title={props.options ? props.options.label : props.resource} perPage={25}>
            <Datagrid isRowSelectable={() => false} expand={<CudaDeviceExpand/>}>
                <TextField source="id"/>
                <TextField source="name"/>
            </Datagrid>
        </List>
    );
}


const useStylesExpand = makeStyles({
    "p-1": {
        padding: '1rem',
    },
    "mx-1": {
        marginLeft: '1rem',
        marginRight: '1rem',
    }
});

const CudaDeviceExpand = ({id, record, resource}) => {
    console.log(id, record, resource);
    const classes = useStylesExpand();
    return <Paper elevation={3} classes={{root: classes["p-1"]}}>
        <Grid container spacing={3} direction={"column"}>
            <Grid item xs={12}>
                <Typography variant={"h5"}>Description</Typography>
                <pre>{record.description}</pre>
            </Grid>
        </Grid>
    </Paper>;
};

export default CudaDeviceList;