'use strict';

angular.module('coria.components')
    .component('datasetsDetails', {
        bindings: {},
        transclude: true,
        templateUrl: 'components/datasets/datasets-details/datasets-details.html',
        controller: ["dataSetService", "$scope", "$location", "$routeParams", "metricsService",
            function( dataSetService,   $scope,   $location,   $routeParams,  metricsService){
            var vm = this;

            vm.metrics = metricsService.queryMetrics();
            vm.metric = {
                description: "Select Metric Provider below"
            };
            vm.selectedMetric = undefined;

            vm.loading = true;
            vm.dataset = {};
            dataSetService.shortDataSet($routeParams.datasetid).then(function(data){
                vm.dataset = data;
                vm.loading = false;
            }, function(error){
                //TODO: errorhandling
            });

            vm.submitMetric = function submitMetric(){
                vm.loading = true;
                vm.metric.datasetid = $routeParams.datasetid;
                vm.metric.description = undefined;
                metricsService.startMetric({}, vm.metric, function(response){
                    vm.loading = false;
                }, function(error){
                    vm.loading = false;
                });
            };

            vm.cancelAddMetric = function cancelAddMetric(){
                vm.displayAddMetric = false;
                vm.metric = {
                    description: "Select Metric Provider below"
                };
            };

            vm.metricProviderSelected = function metricProviderSelected(){
                for(var i = 0; i < vm.metrics.length; i++){
                    if(vm.metrics[i].identification === vm.selectedMetric){
                        vm.metric = vm.metrics[i];
                    }
                }
            };
        }]
    });