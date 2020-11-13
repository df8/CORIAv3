/**
 * Created by David Fradin, 2020
 */

import React from "react";
import {get} from "lodash";

const NumberFieldWithParse = (props) => <span>{parseFloat(get(props.record, props.source)).toLocaleString(undefined, props.options)}</span>;

export default NumberFieldWithParse;