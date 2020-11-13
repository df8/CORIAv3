/**
 * Created by David Fradin, 2020
 */

import React, {useEffect, useState} from 'react';
import {Admin, Resource} from 'react-admin';
import './App.css';
import MetricAlgorithm from "./components/MetricAlgorithm";
import CudaDevice from "./components/CudaDevice";
import coriaDataProvider from "./services/dataProvider";
import Dashboard from "./components/Dashboard/Dashboard";
import Dataset from "./components/Dataset";
import Metric from "./components/Metric";
import ASLocation from "./components/ASLocation";
import ASOrganization from "./components/ASOrganization";


const App = () => {

    const [dataProvider, setDataProvider] = useState(null);
    const [dataProviderState, setDataProviderState] = useState({loading: true, error: false});
    useEffect(() => {
        coriaDataProvider()
            .then(dataProvider => {
                setDataProvider(() => dataProvider);
                setDataProviderState(({error}) => ({error: error, loading: false}));
            })
            .catch(error => {
                setDataProviderState(prev => ({...prev, loading: false, error: true, errorMessage: error.message}));
            });
    }, []);

    if (dataProviderState.loading)
        return <div>Loading...</div>;
    else if (dataProviderState.error)
        return <div>Error: {dataProviderState.errorMessage}</div>;
    else
        return <Admin
            title="My Custom Admin"
            dashboard={Dashboard}
            dataProvider={dataProvider}>
            <Resource name="Dataset" {...Dataset}/>
            <Resource name="Node"/>
            <Resource name="Edge"/>
            <Resource name="ShortestPathLength"/>
            <Resource name="Metric" {...Metric} />
            <Resource name="ImportModule" options={{label: 'Import Modules'}}/>
            <Resource name="ExportModule" options={{label: 'Export Modules'}}/>
            <Resource name="MetricAlgorithm" options={{label: 'Metric Algorithms'}} {...MetricAlgorithm}/>
            <Resource name="MetricAlgorithmImplementation"/>
            <Resource name="CudaDevice" options={{label: 'CUDA Devices Info'}} {...CudaDevice}/>
            <Resource name="ASLocation" options={{label: 'AS Locations'}} {...ASLocation}/>
            <Resource name="ASOrganization" options={{label: 'AS Organizations'}} {...ASOrganization}/>
        </Admin>;
};

export default App;
