/**
 * Created by David Fradin, 2020
 */

import React from "react";
import CreateWithImportModule from "../CommonComponents/CreateWithImportModule";


const DatasetCreate = props => <CreateWithImportModule {...props} title="Import a Dataset" withNameTextInput={true}/>;
export default DatasetCreate;