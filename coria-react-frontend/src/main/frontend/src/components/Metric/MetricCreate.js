/**
 * Created by David Fradin, 2020
 */

import {Create, FormDataConsumer, Loading, RadioButtonGroupInput, SaveButton, SimpleForm, Toolbar, TextInput, useNotify, useQuery} from "react-admin";
import React, {useEffect, useState} from "react";
import {Grid, Table, TableBody, TableRow, TableCell, TableHead, Radio, Tooltip} from "@material-ui/core";
import {makeStyles} from "@material-ui/styles";
import {keyBy} from "lodash";
import ModuleReferenceBox from "../CommonComponents/ModuleReferenceBox";
import {useForm} from 'react-final-form';
import MetricAlgorithmImplementationSpeedIcon from "../CommonComponents/MetricAlgorithmImplementationSpeedIcon";
import CheckCircleIcon from "@material-ui/icons/CheckCircle";
import ErrorIcon from "@material-ui/icons/Error";

const useStylesMetricCreate = makeStyles({
    moduleDescriptionLabel: {
        paddingLeft: '1rem',
    },
    fullWidth: {
        width: '100%'
    },
    "green": {
        color: "green"
    },
    "red": {
        color: "red"
    }
});

const MetricCreate = ({datasetId, onSuccess, ...props}) => {
    const notify = useNotify();
    return <Create
        {...props}
        onSuccess={() => {
            notify('Metric successfully started');
            onSuccess();
        }}>
        <SimpleForm redirect="show" initialValues={{datasetId}} toolbar={<Toolbar><SaveButton label="Run"/></Toolbar>}
                    onSubmit={(record) => {
                        console.log(record);
                    }}>
            <FormDataConsumer>
                {formDataProps => <MetricCreateFormContent {...formDataProps} title={"Run a metric"} withNameTextInput={props.withNameTextInput}/>}
            </FormDataConsumer>
        </SimpleForm>
    </Create>
};

const MetricCreateFormContent = (props) => {
    console.log(props);
    const classes = useStylesMetricCreate();
    const form = useForm();
    const [metricAlgorithmsByKey, setMetricAlgorithmsByKey] = useState({});
    const [metricAlgorithmVariantsByKey, setMetricAlgorithmVariantsByKey] = useState({});
    const [metricAlgorithmImplementationsByKey, setMetricAlgorithmImplementationsByKey] = useState({});
    const {data: metricAlgorithms, total, loading, error} = useQuery({
        type: 'GET_LIST',
        resource: "MetricAlgorithm",
        payload: {
            sort: {field: 'name', order: 'ASC'},
            filter: {}
        }
    });

    useEffect(() => {
        if (total > 0) {
            let _metricAlgorithmsByKey = {};
            for (let i in metricAlgorithms) {
                _metricAlgorithmsByKey[metricAlgorithms[i]['id']] = {
                    ...metricAlgorithms[i],
                    disabled: !metricAlgorithms[i]['metricAlgorithmVariants'].some((mav) => mav['implementations'].some((mai) => mai['available'])) //Check that at least one child is available
                };
            }

            setMetricAlgorithmsByKey(_metricAlgorithmsByKey);
        }
    }, [metricAlgorithms, total, props.formData.metricAlgorithm, props.formData.metricAlgorithmVariant]);

    useEffect(() => {
        if (metricAlgorithmsByKey) {
            if (props.formData.metricAlgorithm && metricAlgorithmsByKey[props.formData.metricAlgorithm]) {
                let _metricAlgorithmVariantsByKey = {};
                for (let i in metricAlgorithmsByKey[props.formData.metricAlgorithm]['metricAlgorithmVariants']) {
                    _metricAlgorithmVariantsByKey[metricAlgorithmsByKey[props.formData.metricAlgorithm]['metricAlgorithmVariants'][i]['id']] = {
                        ...metricAlgorithmsByKey[props.formData.metricAlgorithm]['metricAlgorithmVariants'][i],
                        disabled: !metricAlgorithmsByKey[props.formData.metricAlgorithm]['metricAlgorithmVariants'][i]['implementations'].some((mai) => mai['available'])
                    }
                }
                setMetricAlgorithmVariantsByKey(_metricAlgorithmVariantsByKey);
                if (props.formData.metricAlgorithmVariant && _metricAlgorithmVariantsByKey[props.formData.metricAlgorithmVariant]) {
                    setMetricAlgorithmImplementationsByKey(keyBy(_metricAlgorithmVariantsByKey[props.formData.metricAlgorithmVariant]['implementations'], "id"));
                }
            }
        }
    }, [metricAlgorithmsByKey, props.formData.metricAlgorithm, props.formData.metricAlgorithmVariant]);


    useEffect(() => {
        //Automatically select the first *available* child when a selection was made.
        if (total > 0 && !props.formData.metricAlgorithm) {
            const firstIndex = metricAlgorithms.findIndex((ma) => ma['metricAlgorithmVariants'].some(mav => mav['implementations'].some(mai => mai['available'])));
            form.change("metricAlgorithm", metricAlgorithms[firstIndex].id);
        }
        if (total > 0 && metricAlgorithmsByKey[props.formData.metricAlgorithm]) {
            const firstIndex = metricAlgorithmsByKey[props.formData.metricAlgorithm]['metricAlgorithmVariants'].findIndex(mav => mav['implementations'].some(mai => mai['available']));
            form.change("metricAlgorithmVariant", metricAlgorithmsByKey[props.formData.metricAlgorithm]['metricAlgorithmVariants'][firstIndex].id);
        }
        if (total > 0 && metricAlgorithmsByKey[props.formData.metricAlgorithm] &&
            metricAlgorithmVariantsByKey[props.formData.metricAlgorithmVariant] &&
            metricAlgorithmVariantsByKey[props.formData.metricAlgorithmVariant]['implementations'].length
        ) {
            const firstIndex = metricAlgorithmVariantsByKey[props.formData.metricAlgorithmVariant]['implementations'].findIndex(mai => mai['available']);
            form.change("metricAlgorithmImplementation", metricAlgorithmVariantsByKey[props.formData.metricAlgorithmVariant]['implementations'][firstIndex].id);
        }
    }, [metricAlgorithmsByKey, metricAlgorithmVariantsByKey, form, metricAlgorithms, props.formData.metricAlgorithm, props.formData.metricAlgorithmVariant, props.formData.metricAlgorithmImplementation, total]);

    const onMetricAlgorithmImplementationRadioChange = (event) => {
        form.change("metricAlgorithmImplementation", event.target.value);
    };

    return loading ? <Loading/> :
        error ? <Grid container>
            <Grid item xs={12} md={6}><h3>An error occurred</h3>
                <div>{JSON.stringify(error)}</div>
            </Grid>
        </Grid> : <Grid container>
            <Grid item xs={12} md={6}>
                <h2 className={classes.moduleDescriptionLabel}>{props.title} in 4 simple steps:</h2>
                <Grid container direction="row" justify={"flex-start"}>
                    {total > 0 && <>
                        <Grid item xs={12}>
                            <h3 className={classes.moduleDescriptionLabel}>1. Select a metric algorithm</h3>
                            {props.formData['metricAlgorithm'] &&
                            <p>Please read the <b>Metric Module Reference</b> box on this page for further information.
                            </p>}
                            <RadioButtonGroupInput
                                source="metricAlgorithm"
                                choices={metricAlgorithms}
                                row={false}
                                optionText={(record) => `${record.name}`}
                                onClick={(event) => {
                                    //User changes the choice of metricAlgorithm, hence we automatically update the choice of metricAlgorithmVariant and metricAlgorithmImplementation
                                    if (event.target.value &&
                                        metricAlgorithmsByKey[event.target.value] &&
                                        metricAlgorithmsByKey[event.target.value]['metricAlgorithmVariants'] &&
                                        metricAlgorithmsByKey[event.target.value]['metricAlgorithmVariants'][0]
                                    ) {
                                        form.change("metricAlgorithmVariant", metricAlgorithmsByKey[event.target.value]['metricAlgorithmVariants'][0].id);
                                        form.change("metricAlgorithmImplementation", metricAlgorithmsByKey[event.target.value]['metricAlgorithmVariants'][0]['implementations'][0].id);
                                    }
                                }}
                            />
                        </Grid>
                        {props.formData['metricAlgorithm'] && metricAlgorithmsByKey[props.formData['metricAlgorithm']] &&
                        <Grid item xs={12}>
                            <h3 className={classes.moduleDescriptionLabel}>2. Select a variant of {metricAlgorithmsByKey[props.formData['metricAlgorithm']].name}</h3>
                            {props.formData['metricAlgorithm'] &&
                            <p>Please read the <b>Metric Module Reference</b> box on this page for further information.
                            </p>}
                            <RadioButtonGroupInput
                                source="metricAlgorithmVariant"
                                choices={metricAlgorithmsByKey[props.formData['metricAlgorithm']]['metricAlgorithmVariants']}
                                row={false}
                                optionText={(record) => `${record.name}`}
                                onClick={(event) => {
                                    //User changes the choice of metricAlgorithmVariant, hence we automatically update the choice of metricAlgorithmImplementation
                                    if (event.target.value &&
                                        metricAlgorithmVariantsByKey[event.target.value]
                                    )
                                        form.change("metricAlgorithmImplementation", metricAlgorithmVariantsByKey[event.target.value]['implementations'][0].id);
                                }}
                            />
                        </Grid>}
                        {props.formData['metricAlgorithm'] &&
                        metricAlgorithmsByKey[props.formData['metricAlgorithm']] &&
                        props.formData['metricAlgorithmVariant'] &&
                        metricAlgorithmVariantsByKey[props.formData['metricAlgorithmVariant']] &&
                        <Grid item xs={12}>
                            <h3 className={classes.moduleDescriptionLabel}>3. Select an implementation of {metricAlgorithmsByKey[props.formData['metricAlgorithm']].name}/{metricAlgorithmVariantsByKey[props.formData['metricAlgorithmVariant']].name}</h3>
                            {props.formData['metricAlgorithm'] &&
                            <p>Please read the <b>Metric Module Reference</b> box on this page for further information.
                            </p>}
                            <Table className={classes['fullWidth']}>
                                <TableHead>
                                    <TableRow>
                                        <TableCell/>
                                        <TableCell>Speed</TableCell>
                                        <TableCell>Implementation in</TableCell>
                                        <TableCell>Provided by</TableCell>
                                        <TableCell>Available</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {metricAlgorithmVariantsByKey[props.formData['metricAlgorithmVariant']]['implementations'].map(record =>
                                        <TableRow key={record.id}>
                                            <TableCell>
                                                <Radio
                                                    color={"primary"}
                                                    checked={props.formData['metricAlgorithmImplementation'] === record.id}
                                                    onChange={onMetricAlgorithmImplementationRadioChange}
                                                    value={record.id}
                                                    name={record.id}
                                                    disabled={!record.available}
                                                />
                                            </TableCell>
                                            <TableCell><MetricAlgorithmImplementationSpeedIcon
                                                speed={record['speedIndex']}/></TableCell>
                                            <TableCell>{record['technology']}</TableCell>
                                            <TableCell>{record['provider']}</TableCell>
                                            <TableCell>{record['available'] ? <CheckCircleIcon className={classes.green}/> :
                                                (record['unavailableReason'] ?
                                                    <Tooltip placement="right" title={<p>Reason: {record['unavailableReason']}</p>}>
                                                        <ErrorIcon className={classes.red}/>
                                                    </Tooltip> :
                                                    <ErrorIcon className={classes.red}/>)
                                            }</TableCell>
                                        </TableRow>)}
                                </TableBody>
                            </Table>
                        </Grid>}
                        {props.formData['metricAlgorithm'] &&
                        metricAlgorithmsByKey[props.formData['metricAlgorithm']] &&
                        props.formData['metricAlgorithmVariant'] &&
                        metricAlgorithmVariantsByKey[props.formData['metricAlgorithmVariant']] &&
                        props.formData['metricAlgorithmImplementation'] &&
                        metricAlgorithmVariantsByKey[props.formData['metricAlgorithmVariant']]['parameters'] &&
                        metricAlgorithmVariantsByKey[props.formData['metricAlgorithmVariant']]['parameters'].length &&
                        <Grid item xs={12}>
                            <h3 className={classes.moduleDescriptionLabel}>4. Enter the following parameters for {metricAlgorithmsByKey[props.formData['metricAlgorithm']].name}/{metricAlgorithmVariantsByKey[props.formData['metricAlgorithmVariant']].name}</h3>
                            {metricAlgorithmVariantsByKey[props.formData['metricAlgorithmVariant']]['parameters']
                                .map((param, paramIndex) =>
                                    <div key={paramIndex}><TextInput
                                        source={param['id']}
                                        initialValue={param['defaultValue']}
                                        validate={value => {
                                            switch (param['type']) {
                                                case "FLOAT":
                                                    return (value && value.length > 0 && !isNaN(value) && parseFloat(value) > 0 && parseFloat(value) <= 1) ? undefined : "Not a valid decimal number";
                                                case "INT":
                                                    let x;
                                                    return (value && value.length && !isNaN(value) && (x = parseFloat(value), (0 | x) === x)) ? undefined : "Not a valid number";
                                                default:
                                                    return undefined;
                                            }
                                        }}
                                        helperText={param['description']}
                                    /></div>
                                )}
                        </Grid>}
                    </>}
                    <Grid item xs={12}>
                        <h3 className={classes.moduleDescriptionLabel}>{props.formData['metricAlgorithm'] &&
                        metricAlgorithmsByKey[props.formData['metricAlgorithm']] &&
                        props.formData['metricAlgorithmVariant'] &&
                        metricAlgorithmVariantsByKey[props.formData['metricAlgorithmVariant']] &&
                        props.formData['metricAlgorithmImplementation'] &&
                        metricAlgorithmVariantsByKey[props.formData['metricAlgorithmVariant']]['parameters'] &&
                        metricAlgorithmVariantsByKey[props.formData['metricAlgorithmVariant']]['parameters'].length ? "5" : "4"}. Click RUN below:</h3>
                    </Grid>
                </Grid>
            </Grid>
            <Grid item xs={12} md={6}>
                {metricAlgorithmsByKey[props.formData['metricAlgorithm']] ?
                    <><h3 className={classes.moduleDescriptionLabel}>Metric Module Reference</h3>
                        <ModuleReferenceBox
                            title={"About metric algorithm \"" + metricAlgorithmsByKey[props.formData['metricAlgorithm']].name + "\""}
                            referenceHTML={metricAlgorithmsByKey[props.formData['metricAlgorithm']].description}
                        />
                        {metricAlgorithmVariantsByKey[props.formData['metricAlgorithmVariant']] && <ModuleReferenceBox
                            title={`About variant "${metricAlgorithmsByKey[props.formData['metricAlgorithm']].name}/${metricAlgorithmVariantsByKey[props.formData['metricAlgorithmVariant']].name}"`}
                            referenceHTML={metricAlgorithmVariantsByKey[props.formData['metricAlgorithmVariant']].description}
                            //TODO /2 add dependency tree / execution plan with icons (already computed vs will be computed)
                        />}
                        {metricAlgorithmImplementationsByKey[props.formData['metricAlgorithmImplementation']] &&
                        <ModuleReferenceBox
                            title={`About implementation in ${metricAlgorithmImplementationsByKey[props.formData['metricAlgorithmImplementation']]['technology']} provided by ${metricAlgorithmImplementationsByKey[props.formData['metricAlgorithmImplementation']]['provider']}`}
                            referenceHTML={metricAlgorithmImplementationsByKey[props.formData['metricAlgorithmImplementation']].description || "No description provided."}
                        />}
                    </> : <></>}
            </Grid>
        </Grid>;
}

export default MetricCreate;