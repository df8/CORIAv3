/**
 * Created by David Fradin, 2020
 */

import {BulkDeleteButton, Datagrid, DeleteButton, EditButton, Filter, List, ShowButton, TextField, TextInput} from "react-admin";
import React, {Fragment} from "react";

const DatasetFilter = props => (
    <Filter {...props}>
        <TextInput label="Search" source="q" alwaysOn/>
    </Filter>
);

const DatasetList = props => (
    <List {...props}
          filters={<DatasetFilter/>}
          bulkActionButtons={<DatasetBulkActionButtons/>}
    >
        <Datagrid>
            <TextField source="name"/>
            <TextField source="created"/>
            <ShowButton label="Open"/>
            <EditButton label="Change name"/>
            <DeleteButton label="Delete"/>
        </Datagrid>
    </List>
);

const DatasetBulkActionButtons = props => (
    <Fragment>
        <BulkDeleteButton {...props} />
    </Fragment>
);

export default DatasetList;