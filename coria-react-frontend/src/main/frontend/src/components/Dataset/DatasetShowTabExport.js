/**
 * Created by David Fradin, 2020
 */

import React, {useState} from "react";
import {Button, Loading, useQuery} from "react-admin";
import {FormControl, FormControlLabel, Grid, Paper, Radio, RadioGroup} from "@material-ui/core";
import DownloadIcon from "@material-ui/icons/GetApp";
import {makeStyles} from "@material-ui/styles";

const EXPORT_API_URL = `http://${window.location.hostname}:8080/dataset-export/`;

const useStylesDatasetShowTabExport = makeStyles({
    "exportModuleDescriptionContainer": {
        padding: '1rem',
        marginBottom: '1rem',
    },
    "exportModuleDescriptionLabel": {
        paddingLeft: '1rem',
    },
    "exportModuleButton": {
        margin: "2rem 0.5rem",
        padding: "1rem 2rem"
    },
});

export const DatasetShowTabExport = props => {
    const classes = useStylesDatasetShowTabExport();
    const [selectedExportModule, setSelectedExportModule] = useState(0)
    const [selectedExportModuleParameters, setSelectedExportModuleParameters] = useState({})
    const {data: exportModules, loading, error} = useQuery({
        type: 'GET_LIST',
        resource: "ExportModule",
        payload: {
            target: 'datasetId',
            id: props.record.id,
            sort: {field: 'name', order: 'ASC'},
            //filter: {}, //optional filter
        }
    });

    const handleChange = event => {
        setSelectedExportModule(parseInt(event.target.value));
    }

    const handleChangeParameter = parameterKey => event => {
        setSelectedExportModuleParameters(prev => ({...prev, [parameterKey]: event.target.value}));
    }

    return loading ? <Loading/> :
        error ? <Grid container>
                <Grid item xs={12} md={6}><h3>An error occurred</h3>
                    <div>{JSON.stringify(error)}</div>
                </Grid>
            </Grid> :
            <Grid container>
                <Grid item xs={12} md={6}>
                    <Grid container direction="row" justify={"flex-start"}>
                        <Grid item xs={12}>
                            <h3>Select an export module</h3>
                            <FormControl component="fieldset">
                                <RadioGroup aria-label="selected-export-module" name="Export Module" value={selectedExportModule} onChange={handleChange}>
                                    {exportModules.map((exportModule, exportModuleIndex) => <FormControlLabel key={exportModuleIndex} value={exportModuleIndex} control={<Radio/>} label={exportModule.name}/>)}
                                </RadioGroup>
                            </FormControl>
                        </Grid>
                        {exportModules[selectedExportModule].parameters &&
                        exportModules[selectedExportModule].parameters.map((parameter) => {
                            return <Grid item xs={12} key={parameter.id}>
                                <FormControl component="fieldset"><h3>{parameter.name}</h3>
                                    <RadioGroup aria-label={parameter.id + "-options"} name={parameter.id} value={selectedExportModuleParameters[parameter.id] || parameter.options[0]} onChange={handleChangeParameter(parameter.id)}>
                                        {parameter.options.map(option => <FormControlLabel key={option} value={option} control={<Radio/>} label={option}/>)}
                                    </RadioGroup>
                                </FormControl>
                            </Grid>;
                        })}
                        <Grid item xs={12}>
                            <Button
                                className={classes.exportModuleButton}
                                href={EXPORT_API_URL + exportModules[selectedExportModule].id + "/" + props.record.id + exportModules[selectedExportModule].parameters.reduce(
                                    (prevString, currentParameter, currentIndex) =>
                                        prevString + (prevString.length ? "&" : "?") + currentParameter.id + "=" + (selectedExportModuleParameters[currentParameter.id] || currentParameter.options[0])
                                    , "")}
                                variant="contained"
                                label="Export"><DownloadIcon/>
                            </Button>
                        </Grid>
                    </Grid>
                </Grid>
                <Grid item xs={12} md={6}>
                    <h3 className={classes.exportModuleDescriptionLabel}>Export Module Description</h3>
                    <Paper elevation={3} className={classes.exportModuleDescriptionContainer}>
                        <div dangerouslySetInnerHTML={{__html: exportModules[selectedExportModule].description}}/>
                    </Paper>
                </Grid>
            </Grid>;
};

export default DatasetShowTabExport;