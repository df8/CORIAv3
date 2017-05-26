'use strict';

angular.module('coria.components')
    .component('datasetsUpload', {
        templateUrl: 'components/datasets/datasets-upload/datasets-upload.html',
        controller: ["$scope", "modulesService", "dataSetService",
            function( $scope,   modulesService,   dataSetService){

            var vm = this;
            vm.selectedProvider = {name:"<= Select Import Parser"};
            vm.inputProviders = modulesService.queryImportModules();

            vm.import = {
                provider: "",
                name: "",
                file: undefined,
                isActive: false,
                message: "",
                errorMessage: ""
            };

            vm.importProviderSelected = function importProviderSelected(){
                for(var i = 0; i < vm.inputProviders.length; i++){
                    if(vm.inputProviders[i].identification === vm.import.provider){
                        vm.selectedProvider = vm.inputProviders[i];
                    }
                }
            };

            $scope.$on("fileSelected", function (event, args){
                $scope.$apply(function(){
                    vm.import.file = args.file;
                });
            });

            vm.submitImport = function submitImport(){
                vm.import.isActive = true;
                vm.import.errorMessage = "";
                vm.import.message = "";
                dataSetService.uploadNewDataSet(vm.import.file, vm.import.provider, vm.import.name)
                    .then(function success(response){
                        console.dir(response);
                        vm.import.message = "Upload was successful! You can see the new Data Set in the Dataset section.";
                        vm.import.isActive = false;
                    }, function error(errors){
                        console.dir(errors);
                        vm.import.errorMessage = errors.error;
                        vm.import.isActive = false;
                    });
            };
        }]
    });