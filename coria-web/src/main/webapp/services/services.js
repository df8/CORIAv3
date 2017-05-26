'use strict';

/*global angular */

/**
 * Created by Sebastian Gross
 */
angular.module('coria.components')
    .factory('modulesService', ['$resource',
        function($resource){
            return $resource('/api/modules/import', {}, {
                queryImportModules: {url: "/api/modules/import", method:'GET', params:{}, isArray:true},
                read: {url: 'service/pbs/input/order/:uuid', method:'GET', params:{}, isArray:false },
                queryCompleted: {url: 'service/pbs/input/orders/completed', method:'GET', params:{}, isArray:true },
                checkNumbers: {url: 'service/pbs/input/orders/check', method:'POST', params:{}, isArray:false },
                getFile: {url: 'service/pbs/input/order/download/:uuid', method:'GET', params:{}, isArray:false },
                getFilteredOrders: {url: 'service/pbs/completedCases/getFilteredCases', method:'POST', params:{}, isArray:false}
            })
        }])

    .factory('dataSetService', ['$http', '$q',
        function($http, $q) {

            function uploadImportForm(file, parser, name){
                var deferred = $q.defer();

                var fd = new FormData();
                fd.append("file", file);
                fd.append("parser", parser);
                fd.append("name", name);
                // fd.append("data", JSON.stringify({parser:parser, name:name}))

                $http({
                    method: 'POST',
                    url: '/api/datasets/upload',
                    data: fd,
                    withCredentials: false,
                    headers: {'Content-Type': undefined },
                    transformRequest: angular.identity
                }).then(function success(response){
                    deferred.resolve(response.data);
                }, function error(response){
                    deferred.reject(response.data);
                });

                return deferred.promise;
            }

            return {
                uploadNewDataSet: uploadImportForm
            }
        }])
;