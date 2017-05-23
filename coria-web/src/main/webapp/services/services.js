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
                getFilteredOrders: {url: 'service/pbs/completedCases/getFilteredCases', method:'POST', params:{}, isArray:true}
            })
        }]);