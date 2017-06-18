'use strict';

angular.module('coria.components')
    .component('datasetsDetails', {
        bindings: {},
        transclude: true,
        templateUrl: 'components/datasets/datasets-details/datasets-details.html',
        controller: ["dataSetService", "$scope", "$location", "$timeout", "$routeParams", "metricsService",
            function( dataSetService,   $scope,   $location,   $timeout,   $routeParams,   metricsService){
            var vm = this;
            vm.dataset = {};
            vm.allMetrics = metricsService.queryMetrics();

            //region METRICS
            vm.isMetricRefreshing = true;
            loadMetricsUpdate();
            vm.currentTime = new Date().getTime();
            var metricsRefreshTimer = function() {
                var cancelRefresh = $timeout(function myFunction() {
                    vm.isMetricRefreshing = true;
                    loadMetricsUpdate();
                    vm.currentTime = new Date().getTime();
                    cancelRefresh = $timeout(metricsRefreshTimer, 2500);
                },2500);
            }; metricsRefreshTimer();
            function loadMetricsUpdate(){
                metricsService.metricsForDataset({datasetId: $routeParams.datasetid}, {}, function(success){
                    console.dir(success);
                    if(vm.dataset.metricInfos === undefined){
                        vm.dataset.metricInfos = success;
                    }
                    updateMetricIinfos(success);
                    vm.isMetricRefreshing = false;
                });
            }
            function updateMetricIinfos(metrics){
                for(var i = 0; i < vm.dataset.metricInfos.length; i++){
                    var metric = vm.dataset.metricInfos[i];
                    for(var j = 0; j < metrics.length; j++){
                        var updatedMetric = metrics[j];
                        if(metric.shortcut === updatedMetric.shortcut){
                            metric.executionStarted = updatedMetric.executionStarted;
                            metric.executionFinished = updatedMetric.executionFinished;
                            metric.status = updatedMetric.status;
                            metric.value = updatedMetric.value;
                        }
                    }
                }
            }

            vm.datasetsPerPage = 10;

            vm.metrics = metricsService.queryMetrics();
            vm.metric = {
                description: "Select Metric Provider below"
            };
            vm.selectedMetric = undefined;

            vm.submitMetric = function submitMetric(){
                vm.metric.datasetid = $routeParams.datasetid;
                vm.metric.description = undefined;
                metricsService.startMetric({}, vm.metric, function(response){
                    vm.isDatasetRefreshing = false;
                }, function(error){
                    vm.isDatasetRefreshing = false;
                });
                vm.isMetricRefreshing = true;
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
            //endregion

            //region DATASET
            vm.isDatasetRefreshing = true;
            dataSetService.shortDataSet($routeParams.datasetid).then(function(data){
                vm.dataset = data;
                vm.isDatasetRefreshing = false;
            }, function(error){
                //TODO: errorhandling
            });

            vm.getMetricByShortcut = function getMetricByShortcut(shortcut){
                for(var i = 0; i < vm.allMetrics.length; i++){
                    var currentMetric = vm.allMetrics[i];
                    if(currentMetric.shortcut === shortcut){
                        return currentMetric.name;
                    }
                }
                return shortcut;
            }
            //endregion

        }]
    });