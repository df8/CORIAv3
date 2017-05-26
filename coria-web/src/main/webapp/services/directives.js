'use strict';
/**
 * Created by Sebastian Gross
 */
angular.module('coria.components')
    .directive('fileUpload', function(){
        return {
            scope : true,
            link: function (scope, el, attr){
                el.bind('change', function (event){
                    var files = event.target.files;
                    for(var i= 0; i<files.length;i++){
                        scope.$emit("fileSelected", {file: files[i]});
                    }
                })
            }
        }
    });