/**
 * Created by David Fradin, 2020
 */

import SignalCellular4BarIcon from "@material-ui/icons/SignalCellular4Bar";
import SignalCellular3BarIcon from "@material-ui/icons/SignalCellular3Bar";
import SignalCellular2BarIcon from "@material-ui/icons/SignalCellular2Bar";
import SignalCellular1BarIcon from "@material-ui/icons/SignalCellular1Bar";
import SignalCellular0BarIcon from "@material-ui/icons/SignalCellular0Bar";
import React from "react";

const MetricAlgorithmImplementationSpeedIcon = (props) => {
    switch (Math.floor(props.speed / 10)) {
        case 4:
            return <SignalCellular4BarIcon/>;
        case 3:
            return <SignalCellular3BarIcon/>;
        case 2:
            return <SignalCellular2BarIcon/>;
        case 1:
            return <SignalCellular1BarIcon/>;
        default:
            return <SignalCellular0BarIcon/>;
    }
};

export default MetricAlgorithmImplementationSpeedIcon;