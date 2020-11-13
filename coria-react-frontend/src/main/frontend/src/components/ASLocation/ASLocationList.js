/**
 * Created by David Fradin, 2020
 */

import React from 'react';

import {List, Datagrid, TextField, TextInput, Filter} from 'react-admin';

const ASLocationFilter = props => (
    <Filter {...props}>
        <TextInput label="Search" source="q" alwaysOn/>
    </Filter>
);

const ASLocationList = props => {
    console.log(props);
    //TODO /3: display a map on expand, or display a link button that opens maps
    return (
        <List {...props} filters={<ASLocationFilter/>} title={props.options ? props.options.label : props.resource}>
            <Datagrid isRowSelectable={() => false}>
                <TextField source="id"/>
                <TextField source="continent"/>
                <TextField source="country"/>
                <TextField source="region"/>
                <TextField source="city"/>
                <TextField source="latitude"/>
                <TextField source="longitude"/>
            </Datagrid>
        </List>
    );
}
export default ASLocationList;