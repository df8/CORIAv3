'use strict';

angular.module('coria.components')
    .component('datasetsUpload', {
        templateUrl: 'components/datasets/datasets-upload/datasets-upload.html',
        controller: ["modulesService", function(modulesService){
            var vm = this;
            vm.selectedProvider = {name:"<- Select Import Parser"};
            vm.inputProviders = modulesService.queryImportModules();

            vm.import = {};

            vm.importProviderSelected = function importProviderSelected(){
                for(var i = 0; i < vm.inputProviders.length; i++){
                    if(vm.inputProviders[i].identification === vm.import.provider){
                        vm.selectedProvider = vm.inputProviders[i];
                    }
                }
            };
        }]
    });