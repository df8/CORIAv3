'use strict';

angular.module('coria.components')
    .component('datasetsOverview', {
        bindings: {},
        transclude: true,
        templateUrl: 'components/datasets/datasets-overview/datasets-overview.html',
        controller: ["dataSetService", "$scope",
            function( dataSetService,   $scope){
            var vm = this;

            vm.datasets = [];
            vm.datasetsPerPage = 15;
            dataSetService.shortDataSets().then(function(data){
                vm.datasets = data;
            });
        }]
    });