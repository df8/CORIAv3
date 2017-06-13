'use strict';

angular.module('coria.components')
    .component('datasetsDetails', {
        bindings: {},
        transclude: true,
        templateUrl: 'components/datasets/datasets-details/datasets-details.html',
        controller: ["dataSetService", "$scope", "$location", "$timeout", "$routeParams", "metricsService",
            function( dataSetService,   $scope,   $location,   $timeout,   $routeParams,   metricsService){
            var vm = this;
            vm.isRefreshing = false;
            vm.currentTime = new Date().getTime();
            var metricsRefreshTimer = function() {
                var cancelRefresh = $timeout(function myFunction() {
                    vm.isRefreshing = true;
                    metricsService.metricsForDataset({datasetId: $routeParams.datasetid}, {}, function(success){
                        console.dir(success);
                        vm.dataset.metricInfos = success;
                        vm.isRefreshing = false;
                    });
                    vm.currentTime = new Date().getTime();
                    cancelRefresh = $timeout(metricsRefreshTimer, 10000);
                },10000);
            }; metricsRefreshTimer();

            vm.datasetsPerPage = 10;

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
                vm.metric.datasetid = $routeParams.datasetid;
                vm.metric.description = undefined;
                metricsService.startMetric({}, vm.metric, function(response){
                    vm.loading = false;
                }, function(error){
                    vm.loading = false;
                });
                vm.isRefreshing = true;
                vm.cancelAddMetric();
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