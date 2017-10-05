'use strict';

angular.module('coria.components')
    .component('modulesOverview', {
        bindings: {},
        transclude: true,
        templateUrl: 'components/modules/modules-overview/modules-overview.html',
        controller: ["dataSetService", "modulesService", "$scope", "$location", "$ngConfirm", "$route",
            function( dataSetService,   modulesService,   $scope,   $location,   $ngConfirm,   $route){
            var vm = this;

            vm.importModules = modulesService.queryImportModules();
            vm.exportModules = modulesService.queryExportModules();
            vm.metricModules = modulesService.queryMetricModules();
            vm.storageModules = modulesService.queryStorageModules();

            vm.datasets = [];
            vm.datasetsPerPage = 15;
            dataSetService.shortDataSets().then(function(data){
                vm.datasets = data;
            }, function(error){
                console.dir(error);
                vm.errorOccured = error.data.error;
            });

            vm.openDataset = function openDataset(ds){
                $location.path("datasets/" + ds.id);
            };

            vm.deleteDataset = function deleteDataset(dataset){
                $ngConfirm({
                    title: "Delete Dataset",
                    content: "<strong>Do yoou really want to delete the dataset?</strong><br/>This will also delete all its data including nodes, edges and all calculated metrics!",
                    buttons: {
                        yes: {
                            text: 'Yes',
                            btnClass: 'btn-blue',
                            keys: ['enter', 'y'],
                            action: function(scope, button){
                                dataSetService.deleteDataset(dataset.id).then(function() {
                                    $route.reload();
                                });
                            }
                        },
                        no: {
                            text: 'No',
                            keys: ['esc', 'n'],
                            action: function(scope, button){

                            }
                        }
                    }
                });
            };
        }]
    });