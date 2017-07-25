'use strict';

/*global angular */

/**
 * Created by Sebastian Gross
 */
angular.module('coria.components')
    .factory('modulesService', ['$resource',
        function($resource){
            return $resource('api/modules/import', {}, {
                queryImportModules: {url: "api/modules/import", method:'GET', params:{}, isArray:true},

                read: {url: 'service/pbs/input/order/:uuid', method:'GET', params:{}, isArray:false },
                queryCompleted: {url: 'service/pbs/input/orders/completed', method:'GET', params:{}, isArray:true },
                checkNumbers: {url: 'service/pbs/input/orders/check', method:'POST', params:{}, isArray:false },
                getFile: {url: 'service/pbs/input/order/download/:uuid', method:'GET', params:{}, isArray:false },
                getFilteredOrders: {url: 'service/pbs/completedCases/getFilteredCases', method:'POST', params:{}, isArray:false}
            })
        }])

    .factory('metricsService', ['$resource',
        function($resource){
            return $resource('api/metrics', {}, {
                queryMetrics: {url: "api/metrics", method:'GET', params:{}, isArray:true},
                metricsForDataset: {url: "api/metrics/dataset/:datasetId", method:'GET', params:{}, isArray:true},
                startMetric: {url: 'api/metrics/start', method:'POST', params:{}, isArray:false,
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    transformRequest: function (data, headersGetter) {
                        var str = [];
                        for (var d in data)
                            str.push(encodeURIComponent(d) + "=" + encodeURIComponent(data[d]));
                        return str.join("&");
                    }}
            })
        }])

    .factory('dataSetService', ['$http', '$q',
        function($http, $q) {

            function uploadImportForm(files, parser, name){
                var deferred = $q.defer();

                var fd = new FormData();

                Object.keys(files).forEach(function(key,index) {
                    fd.append(key, files[key]);
                });

                // fd.append("file", file);    //ist nun ein object
                fd.append("parser", parser);
                fd.append("name", name);
                // fd.append("data", JSON.stringify({parser:parser, name:name}))

                $http({
                    method: 'POST',
                    url: 'api/datasets/upload',
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
            function getShortDataSets(){
                return $http.get("api/datasets/short")
                    .then(function(response){
                        return response.data;
                    });
            }
            function getShortDataSet(id){
                return $http.get("api/datasets/short/" + id)
                    .then(function(response){
                        return response.data;
                    });
            }
            function postDeleteDataset(id){
                return $http.post("api/datasets/delete/" + id)
                    .then(function(response){
                        return response.data;
                    });
            }

            return {
                uploadNewDataSet: uploadImportForm,
                shortDataSets: getShortDataSets,
                shortDataSet: getShortDataSet,
                deleteDataset: postDeleteDataset
            }
        }])
;