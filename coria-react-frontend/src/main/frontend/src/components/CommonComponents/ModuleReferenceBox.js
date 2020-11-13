/**
 * Created by David Fradin, 2020
 */

import {makeStyles} from "@material-ui/styles";
import {Paper} from "@material-ui/core";
import React from "react";

const useStylesModuleReferenceBox = makeStyles({
    moduleReferenceContainer: {
        padding: '1rem',
        marginBottom: '1rem',
        fontFamily: 'Georgia,serif',
        "& * pre": {
            padding: '1em 2em',
            width: 'fit-content',
            backgroundColor: '#f5f5f5',
        },
        "& * code": {
            padding: '0 .5em',
            backgroundColor: '#e8e8e8',
        },
        "& * table, & * th, & * td": {
            padding: '0.5em 1em',
            border: 'solid thin black',
            borderCollapse: 'collapse',
        },
        "& * table": {
            margin: '2em',
        },
        "& > div": {
            paddingInlineStart: '1em'
        },
        "& * tr.headerRow": {
            backgroundColor: '#e8e8e8',
        }
    },
    importModuleDescriptionLabel: {
        paddingLeft: '1rem',
    },
})

const ModuleReferenceBox = props => {
    const classes = useStylesModuleReferenceBox();
    return <Paper elevation={3} className={classes.moduleReferenceContainer}>
        <h4>{props.title}</h4>
        <div dangerouslySetInnerHTML={{__html: props.referenceHTML}}/>
    </Paper>;
}

export default ModuleReferenceBox;