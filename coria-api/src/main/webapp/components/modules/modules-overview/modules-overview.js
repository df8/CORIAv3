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
        }]
    });