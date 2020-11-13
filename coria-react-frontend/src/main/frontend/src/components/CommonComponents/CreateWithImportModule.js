/**
 * Created by David Fradin, 2020
 */

import {Create, FileField, FileInput, FormDataConsumer, Loading, RadioButtonGroupInput, SimpleForm, TextInput, useQuery} from "react-admin";
import React, {useEffect} from "react";
import {Grid} from "@material-ui/core";
import {makeStyles} from "@material-ui/styles";
import {keyBy} from "lodash";
import ModuleReferenceBox from "./ModuleReferenceBox";
import {useForm} from 'react-final-form';

const useStylesCreateWithImportModule = makeStyles({
    importModuleDescriptionLabel: {
        paddingLeft: '1rem',
    },
    dropZoneRoot: {
        backgroundColor: "#2196f3",
        color: "#ffffff",
        width: "88%"
    },
    textField: {
        width: "90%"
    }
});

const CreateWithImportModule = props => {
    let sanitizedProps = {...props};
    delete sanitizedProps.title;
    delete sanitizedProps.withNameTextInput;
    return <Create {...sanitizedProps}>
        <SimpleForm redirect="show">
            <FormDataConsumer>
                {formDataProps => <CreateWithImportModuleFormContent {...formDataProps} title={props.title} withNameTextInput={props.withNameTextInput}/>}
            </FormDataConsumer>
        </SimpleForm>
    </Create>
};

const CreateWithImportModuleFormContent = (props) => {
    //console.log(props);
    const classes = useStylesCreateWithImportModule();
    const form = useForm();
    const {data: importModules, total, loading, error} = useQuery({
        type: 'GET_MANY_REFERENCE',
        resource: "ImportModule",
        payload: {
            target: 'importResource',
            id: props.resource,
            sort: {field: 'name', order: 'ASC'},
        }
    });
    let importModulesObj = {};
    if (total > 0) {
        importModulesObj = keyBy(importModules, "id");
    }

    useEffect(() => {
        if (total > 0 && !props.formData.importModule) {
            form.change("importModule", importModules[0].id);
        }
    }, [form, importModules, props.formData.importModule, total]);
    //If there is only exactly one import module, we hide the import module radio input and make the form one step shorter.
    const totalSteps = 4 - (total === 1 ? 1 : 0) - (props.withNameTextInput ? 0 : 1);
    return loading ? <Loading/> :
        error ? <Grid container>
            <Grid item xs={12} md={6}><h3>An error occurred</h3>
                <div>{JSON.stringify(error)}</div>
            </Grid>
        </Grid> : <Grid container>
            <Grid item xs={12} md={6}>
                <h2 className={classes.importModuleDescriptionLabel}>{props.title} in {totalSteps} simple steps:</h2>
                <Grid container direction="row" justify={"flex-start"}>
                    {total > 1 && <Grid item xs={12}>
                        <h3 className={classes.importModuleDescriptionLabel}>1. Select an import module</h3>
                        {props.formData.importModule && <p>Please read the <b>Import Module Reference</b> box on this page for further information.</p>}
                        <RadioButtonGroupInput source="importModule" choices={importModules} row={false}/>
                    </Grid>}
                    <Grid item xs={12}>
                        <h3 className={classes.importModuleDescriptionLabel}>{total === 1 ? 1 : 2}. Drag&amp;Drop the files to upload</h3>
                        <FileInput
                            classes={{"dropZone": classes.dropZoneRoot}}
                            source="files"
                            multiple={true}
                            label="Drag and drop a text file here according to the specification">
                            <FileField source="file" title="title" download/>
                        </FileInput>
                    </Grid>
                    {props.withNameTextInput && <Grid item xs={12}>
                        <h3 className={classes.importModuleDescriptionLabel}>{total === 1 ? 2 : 3}. Provide a name for the new dataset</h3>
                        <TextInput source="name" className={classes.textField}/>
                    </Grid>}
                    <Grid item xs={12}>
                        <h3 className={classes.importModuleDescriptionLabel}>{totalSteps}. Click SAVE below:</h3>
                    </Grid>
                </Grid>
            </Grid>
            <Grid item xs={12} md={6}>
                {importModulesObj[props.formData.importModule] ?
                    <><h3 className={classes.importModuleDescriptionLabel}>Import Module Reference</h3>
                        <ModuleReferenceBox title={"About \"" + importModulesObj[props.formData.importModule].name + "\""} referenceHTML={importModulesObj[props.formData.importModule].description}/>
                    </> : <></>}
            </Grid>
        </Grid>;
}

export default CreateWithImportModule;