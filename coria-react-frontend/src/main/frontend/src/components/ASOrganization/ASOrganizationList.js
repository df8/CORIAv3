/**
 * Created by David Fradin, 2020
 */

import React from 'react';

import {List, Datagrid, TextField, TextInput, Filter} from 'react-admin';

const ASOrganizationFilter = props => (
    <Filter {...props}>
        <TextInput label="Search" source="q" alwaysOn/>
    </Filter>
);

const ASOrganizationList = props => {
    console.log(props);
    //TODO /3: display a map on expand, or display a link button that opens maps
    return (
        <List {...props} filters={<ASOrganizationFilter/>} title={props.options ? props.options.label : props.resource}>
            <Datagrid isRowSelectable={() => false}>
                <TextField source="id"/>
                <TextField source="name"/>
                <TextField source="country"/>
            </Datagrid>
        </List>
    );
}
export default ASOrganizationList;