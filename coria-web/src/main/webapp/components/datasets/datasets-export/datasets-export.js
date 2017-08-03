'use strict';

angular.module('coria.components')
    .component('datasetsExport', {
        templateUrl: 'components/datasets/datasets-export/datasets-export.html',
        controller: ["$scope", "modulesService", "dataSetService",
            function( $scope,   modulesService,   dataSetService){

            var vm = this;
            vm.datasets = [];
            vm.selectedProvider = {name:"<= Select Export Adapter"};
            vm.exportProviders = modulesService.queryExportModules();
            dataSetService.shortDataSets().then(function(data){
                vm.datasets = data;
            });

            vm.export = {
                provider: "",
                name: "",
                isActive: false,
                message: "",
                selectedDataset: "",
                errorMessage: "",
                addTextFields: []
            };
            vm.addFields = {};

            vm.exportProviderSelected = function exportProviderSelected(){
                vm.addFields = {};
                vm.export.addTextFields = [];
                for(var i = 0; i < vm.exportProviders.length; i++){
                    if(vm.exportProviders[i].identification === vm.export.provider){
                        vm.selectedProvider = vm.exportProviders[i];
                        checkForAdditionalParamater(vm.exportProviders[i]);
                    }
                }
            };

            function checkForAdditionalParamater(exportProvider){
                Object.keys(exportProvider.additionalFields).forEach(function(key,index) {
                    vm.addFields[key] = exportProvider.additionalFields[key];
                });
            }

            vm.submitExport = function submitExport(){
                dataSetService.exportDataset(vm.export.selectedDataset, vm.export.provider, vm.export.addTextFields)
                    .then(function success(data){
                       console.dir(data);
                        var blob = new Blob([data], { type:"text/plain;charset=utf-8;" });
                        var downloadLink = angular.element('<a></a>');
                        downloadLink.attr('href',window.URL.createObjectURL(blob));
                        downloadLink.attr('download', vm.export.selectedDataset + '.txt');
                        downloadLink[0].click();
                    }, function error(errordata){
                        //todo errorhandling
                        console.dir(errordata);
                    });
            }

        }]
    });