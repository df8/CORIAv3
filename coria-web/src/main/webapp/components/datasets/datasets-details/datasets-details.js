'use strict';

angular.module('coria.components')
    .component('datasetsDetails', {
        bindings: {},
        transclude: true,
        templateUrl: 'components/datasets/datasets-details/datasets-details.html',
        controller: ["dataSetService", "$scope", "$location", "$routeParams",
            function( dataSetService,   $scope,   $location,   $routeParams){
            var vm = this;

            vm.datasets = [];
            vm.datasetsPerPage = 15;
            dataSetService.shortDataSets().then(function(data){
                vm.datasets = data;
            });

            vm.openDataset = function openDataset(ds){
                $location.path("datasets/" + ds.id);
            };
        }]
    });