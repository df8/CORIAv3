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
                files: {},
                isActive: false,
                message: "",
                errorMessage: ""
            };
            vm.addFields = {};

            vm.importProviderSelected = function importProviderSelected(){
                vm.addFields = {};
                vm.import.files = {};
                for(var i = 0; i < vm.inputProviders.length; i++){
                    if(vm.inputProviders[i].identification === vm.import.provider){
                        vm.selectedProvider = vm.inputProviders[i];
                        checkForAdditionalParamater(vm.inputProviders[i]);
                    }
                }
            };

            function checkForAdditionalParamater(inputProvider){
                Object.keys(inputProvider.additionalFields).forEach(function(key,index) {
                    vm.addFields[key] = inputProvider.additionalFields[key];
                });
            }

            $scope.$on("fileSelected", function (event, args){
                $scope.$apply(function(){
                    vm.import.files[args.field] = args.file;
                });
            });

            vm.submitImport = function submitImport(){
                vm.import.isActive = true;
                vm.import.errorMessage = "";
                vm.import.message = "";
                dataSetService.uploadNewDataSet(vm.import.files, vm.import.provider, vm.import.name)
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