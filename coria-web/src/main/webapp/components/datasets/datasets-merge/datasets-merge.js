'use strict';

angular.module('coria.components')
    .component('datasetsMerge', {
        templateUrl: 'components/datasets/datasets-merge/datasets-merge.html',
        controller: ["$scope", "modulesService", "dataSetService",
            function( $scope,   modulesService,   dataSetService){

            var vm = this;
            vm.datasets = [];
            dataSetService.shortDataSets().then(function(data){
                vm.datasets = data;
            }, function(error){
                vm.merge.errorMessage = "Could not load DataSets! " + error;
            });

            vm.merge = {
                first: "",
                second: "",
                name: "",
                message: "",
                errorMessage: ""
            };

            vm.submitMerge = function submitMerge(){
                vm.merge.isActive = true;
                vm.merge.errorMessage = "";
                vm.merge.message = "";
                dataSetService.mergeDataSets(vm.merge)
                    .then(function success(response){
                        vm.merge.message = "DataSets were successfully merged!";
                        vm.merge.isActive = false;
                    }, function error(errors){
                        vm.merge.errorMessage = errors.data.error;
                        vm.merge.isActive = false;
                    });
            };
        }]
    });