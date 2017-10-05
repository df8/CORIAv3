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
                queryExportModules: {url: "api/modules/export", method:'GET', params:{}, isArray:true},
                queryStorageModules: {url: "api/modules/storage", method:'GET', params:{}, isArray:true},
                queryMetricModules: {url: "api/modules/metrics", method:'GET', params:{}, isArray:true}
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
            function exportDataSet(id, exportAdapterId, addFields){
                var deferred = $q.defer();

                var fd = new FormData();

                fd.append("adapterid", exportAdapterId);
                Object.keys(addFields).forEach(function(key,index) {
                    fd.append(key, addFields[key]);
                });

                $http({
                    method: 'POST',
                    url: 'api/datasets/export/' + id,
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

            function uploadImportForm(files, parser, name, addFields){
                var deferred = $q.defer();

                var fd = new FormData();

                Object.keys(files).forEach(function(key,index) {
                    fd.append(key, files[key]);
                });

                // fd.append("file", file);    //ist nun ein object
                fd.append("parser", parser);
                fd.append("name", name);
                Object.keys(addFields).forEach(function(key,index) {
                    fd.append(key, addFields[key]);
                });
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
            function postMergeDataset(data){
                var str = [];
                Object.keys(data).forEach(function(key,index) {
                    str.push(encodeURIComponent(key) + "=" + encodeURIComponent(data[key]));
                });
                return $http.post("api/datasets/merge", str.join("&"), {headers:{ 'Content-Type': 'application/x-www-form-urlencoded' }})
                    .then(function(response){
                        return response.data;
                    });
            }

            return {
                uploadNewDataSet: uploadImportForm,
                shortDataSets: getShortDataSets,
                shortDataSet: getShortDataSet,
                deleteDataset: postDeleteDataset,
                exportDataset: exportDataSet,
                mergeDataSets: postMergeDataset
            }
        }])
;