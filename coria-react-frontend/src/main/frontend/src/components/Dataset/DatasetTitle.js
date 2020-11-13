/**
 * Created by David Fradin, 2020
 */

import React from "react";

const DatasetTitle = ({record}) => {
    return <span>Dataset {record ? `"${record.name}"` : ''}</span>;
};

export default DatasetTitle;