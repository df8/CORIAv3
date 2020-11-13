/**
 * Created by David Fradin, 2020
 */

import {Edit, SimpleForm, TextInput} from "react-admin";
import DatasetTitle from "./DatasetTitle";
import React from "react";

export const DatasetEdit = props => (
    <Edit title={<DatasetTitle/>} {...props}>
        <SimpleForm>
            <TextInput disabled source="id"/>
            <TextInput source="name"/>
        </SimpleForm>
    </Edit>
);

export default DatasetEdit;