'use strict';

angular.module('coria.components')
    .component('datasetsDetails', {
        bindings: {},
        transclude: true,
        templateUrl: 'components/datasets/datasets-details/datasets-details.html',
        controller: ["dataSetService", "$scope", "$location", "$timeout", "$routeParams", "metricsService",
            function( dataSetService,   $scope,   $location,   $timeout,   $routeParams,   metricsService){
            var vm = this;
            vm.dataset = {};
            vm.isNodesRefreshing = true;
            vm.allMetrics = metricsService.queryMetrics();

            $('#nav-tabs a').click(function (e) {
                e.preventDefault();
                $(this).tab('show');
            });

            vm.display = {
                nodes: true,
                metric: false,
                metricLoading: false,
                currentMetric: {
                    shortcut: ""
                }
            };

            //region METRICS
            $(function () {
                $('[data-toggle="popover"]').popover()
            });

            vm.isMetricRefreshing = true;
            loadMetricsUpdate();
            var refreshSeconds = 0;
            vm.currentTime = new Date().getTime();
            var metricsRefreshTimer = function() {
                var cancelRefresh = $timeout(function myFunction() {
                    vm.isMetricRefreshing = true;
                    loadMetricsUpdate();
                    vm.currentTime = new Date().getTime();
                    cancelRefresh = $timeout(metricsRefreshTimer, refreshSeconds += 2500);
                },2500);
            }; metricsRefreshTimer();
            function loadMetricsUpdate(){
                metricsService.metricsForDataset({datasetId: $routeParams.datasetid}, {}, function(success){
                    if(vm.dataset.metricInfos === undefined){
                        vm.dataset.metricInfos = success;
                    }
                    updateMetricIinfos(success);
                    vm.isMetricRefreshing = false;
                });
            }
            function updateMetricIinfos(metrics){
                var finishedMetrics = [];
                for(var i = 0; i < vm.dataset.metricInfos.length; i++){
                    var metric = vm.dataset.metricInfos[i];
                    for(var j = 0; j < metrics.length; j++){
                        var updatedMetric = metrics[j];
                        if(metric.shortcut === updatedMetric.shortcut){
                            finishedMetrics.push(metric);
                            metric.executionStarted = updatedMetric.executionStarted;
                            metric.executionFinished = updatedMetric.executionFinished;
                            metric.status = updatedMetric.status;
                            metric.message = updatedMetric.message;
                            metric.name = updatedMetric.name;
                            metric.value = updatedMetric.value;
                        }
                    }
                }
                //check if new metrics were added
                if(finishedMetrics.length !== metrics.length){
                    for(var i = 0; i < metrics.length; i++){
                        var metricFound = false;
                        for(var j = 0; j < finishedMetrics.length; j++){
                            if(metrics[i].shortcut === finishedMetrics[j].shortcut){
                                metricFound = true;
                            }
                        }
                        if(!metricFound){
                            vm.dataset.metricInfos.push(metrics[i]);
                        }
                    }
                }
            }

            vm.datasetsPerPage = 10;

            vm.metrics = metricsService.queryMetrics();
            vm.metric = {
                description: "Select Metric Provider below"
            };
            vm.selectedMetric = undefined;

            vm.submitMetric = function submitMetric(){
                vm.metric.datasetid = $routeParams.datasetid;
                vm.metric.description = undefined;
                metricsService.startMetric({}, vm.metric, function(response){
                    vm.isDatasetRefreshing = false;
                }, function(error){
                    vm.isDatasetRefreshing = false;
                });
                vm.isMetricRefreshing = true;
                vm.cancelAddMetric();
            };

            vm.cancelAddMetric = function cancelAddMetric(){
                vm.displayAddMetric = false;
                vm.metric = {
                    description: "Select Metric Provider below"
                };
            };

            vm.metricProviderSelected = function metricProviderSelected(){
                for(var i = 0; i < vm.metrics.length; i++){
                    if(vm.metrics[i].identification === vm.selectedMetric){
                        vm.metric = vm.metrics[i];
                    }
                }
            };

            vm.displayMetricStats = function displayMetricStats(metric){
                $('.nav-tabs a[href="#metricinfo"]').tab('show');   //activate metricinfo tab
                vm.selectedMetric = {
                    name: metric.name,
                    shortcut: metric.shortcut
                };
                vm.selectedMetric.nodes = vm.dataset.nodes;
                vm.selectedMetric.nodes.sort(function(a, b) {
                    return parseFloat(b.attributes[metric.shortcut]) - parseFloat(a.attributes[metric.shortcut]);
                });
            };
            //endregion

            //region DATASET
            vm.isDatasetRefreshing = true;
            dataSetService.shortDataSet($routeParams.datasetid).then(function(data){
                vm.dataset = data;
                vm.isNodesRefreshing = false;
                vm.isDatasetRefreshing = false;
            }, function(error){
                //TODO: errorhandling
            });

            vm.getMetricByShortcut = function getMetricByShortcut(shortcut){
                for(var i = 0; i < vm.allMetrics.length; i++){
                    var currentMetric = vm.allMetrics[i];
                    if(currentMetric.shortcut === shortcut){
                        return currentMetric.name;
                    }
                }
                return shortcut;
            };
            vm.getSubMetrics = function getSubMetrics(node, shortcut){
                if(shortcut.indexOf('_') > 0){
                    return [];
                }
                var subMetrics = [];
                for(var a in node.attributes){
                    if(a===shortcut){continue;}
                    if(a.lastIndexOf(shortcut, 0)===0){     //a startsWith shortcut
                        if(a.indexOf('relative')>0){
                            subMetrics.push(vm.roundNumber(node.attributes[a], 2) + "%");
                        }else{
                            subMetrics.push(node.attributes[a]);
                        }
                    }
                }
                return subMetrics;
            };
            /**
             * It does not make sense to display all available metrics.
             * the blacklist array contains the shortcuts of the metrics which
             * are not relevant to the user but rather for internal computation
             * @param shortcut
             * @returns {boolean}
             */
            vm.allowDisplayShortcut = function allowDisplayShortcut(shortcut){
                var blacklist = [
                    "pos"
                ];
                for(var i = 0; i < blacklist.length; i++){
                    if(blacklist[i] === shortcut){return false;}
                }
                return true;
            };

            //endregion

            //region GRAPH
            vm.roundNumber = function roundNumber(number, digits) {
                var multiple = Math.pow(10, digits);
                var rndedNum = Math.round(number * multiple) / multiple;
                return rndedNum;
            };

            vm.displayMetric = function displayMetric(metric){
                $('.nav-tabs a[href="#graph"]').tab('show');   //activate graph tab

                if(metric.shortcut === "new"){
                    // render the graph new and live
                    var graph = Viva.Graph.graph();
                    var graphics = Viva.Graph.View.webglGraphics();
                    for(var e = 0; e < vm.dataset.edges.length; e++){
                        var edge = vm.dataset.edges[e];
                        graph.addLink(edge.sourceNode, edge.destinationNode);
                    }
                    var layout = Viva.Graph.Layout.forceDirected(graph, {
                        springLength : 50,
                        springCoeff : 0.0001,
                        dragCoeff : 0.02,
                        gravity : -1.5,
                        timeStep: 10
                    });
                    var renderer = Viva.Graph.View.renderer(graph, {
                        graphics : graphics,
                        layout : layout,
                        container: document.getElementById('metric-canvas')
                    });
                    renderer.run();
                }else if(metric.shortcut === "pos"){
                    //use cytoscape to display precalculated positions
                    var cy = window.cy = cytoscape({
                        container: document.getElementById('metric-canvas'),
                        elements: positions,
                        style: [
                            {
                                selector: "core",
                                style: {
                                    "selection-box-color": "#AAD8FF",
                                    "selection-box-border-color": "#8BB0D0",
                                    "selection-box-opacity": "0.5"
                                }
                            },
                            {
                                selector: "node",
                                style: {
                                    // width: "mapData(score, 0, 0.006769776522008331, 20, 60)",
                                    // height: "mapData(score, 0, 0.006769776522008331, 20, 60)",
                                    content: "data(id)",
                                    "font-size": "12px",
                                    "text-valign": "center",
                                    "text-halign": "center",
                                    "background-color": "#555",
                                    "text-outline-color": "#555",
                                    "text-outline-width": "2px",
                                    color: "#fff",
                                    "overlay-padding": "6px",
                                    "z-index": "10"
                                }
                            },
                            {
                                selector: "node[?attr]",
                                style: {
                                    shape: "rectangle",
                                    "background-color": "#aaa",
                                    "text-outline-color": "#aaa",
                                    width: "16px",
                                    height: "16px",
                                    "font-size": "6px",
                                    "z-index": "1"
                                }
                            }
                        ]
                    });

                    //on click display information on the node
                    cy.on('tap', 'node', function (event) {
                        var node = event.target;
                        var contentString = "";
                        Object.keys(node._private.data.attributes).forEach(function(key,index) {
                            if(""+key.indexOf('_')<0 && (""+key) !== "pos") {
                                contentString = contentString + "<strong>" + vm.getMetricByShortcut(key) + ":</strong> " + node._private.data.attributes[key] + "<br />";
                            }
                        });
                        // var nodeAttributes = node.attributes;
                        node.qtip({
                            content: contentString,
                            show: {
                                event: event.type,
                                ready: true
                            },
                            hide: {
                                event: 'mouseout unfocus'
                            }
                        }, event);
                        // console.log(evt.target.id())
                    });

                    var positions = {};
                    for(var n = 0; n < vm.dataset.nodes.length; n++){
                        var node = vm.dataset.nodes[n];
                        cy.add({
                            data: {
                                id: node.name,
                                attributes: node.attributes
                            },
                            style:{
                                width: 25 + parseInt((node.attributes["ndeg_relative"]?node.attributes["ndeg_relative"]:0)) + "px",
                                height: 25 + parseInt((node.attributes["ndeg_relative"]?node.attributes["ndeg_relative"]:0)) + "px"
                            }});
                        // console.log(node.name + ": " + node.attributes.pos.split(":")[0] + " * " + 100000000 + " = " + parseInt(node.attributes.pos.split(":")[0]*100000000));
                        // console.log(node.name + ": " + node.attributes.pos.split(":")[1] + " * " + 100000000 + " = " + parseInt(node.attributes.pos.split(":")[1]*100000000));
                        positions[node.name] = {
                            x: parseInt(node.attributes.pos?node.attributes.pos.split(":")[0]*100000:Math.random()),
                            y: parseInt(node.attributes.pos?node.attributes.pos.split(":")[1]*100000:Math.random())
                        };
                    }

                    // console.dir(positions);

                    for(var e = 0; e < vm.dataset.edges.length; e++){
                        var edge = vm.dataset.edges[e];
                        cy.add({
                            data: {
                                id: edge.name,
                                source: edge.sourceNode,
                                target: edge.destinationNode,
                                attributes: edge.attributes
                            },
                            selectable: false
                        });
                    }
                    var options = {
                        name: 'preset',
                        positions: positions, // map of (node id) => (position obj); or function(node){ return somPos; }
                        fit: true, // whether to fit to viewport
                        padding: 30, // padding on fit
                        animate: false, // whether to transition the node positions
                        animationDuration: 500 // duration of animation in ms if enabled
                    };
                    var layoutRunner = cy.makeLayout(options);
                    layoutRunner.run();
                }else{
                    //setup a circular cytoscape to highlight metrics special nodes
                    createCircleGraph('metric-canvas', vm.dataset.nodes, vm.dataset.edges, metric);
                }

                vm.display.metricLoading = false;
            };
            //endregion

            //region NODEDETAILS
            vm.selectedNodeInfos = {
                metrics: []
            };
            vm.showNode = function showNode(node){
                $('.nav-tabs a[href="#nodeinfo"]').tab('show');   //activate graph tab

                //prepare node attributes for display in view
                vm.selectedNodeInfos.metrics = [];
                vm.selectedNodeInfos.nodeName = node.name;
                var subAttributes = [];
                vm.selectedNodeInfos.metrics.push({
                    name: "Risc Score",
                    value: node.riscScore,
                    shortcut: "rs"
                });
                Object.keys(node.attributes).forEach(function(key,index) {
                    if(""+key.indexOf('_')<0 && (""+key) !== "pos") {
                        //metric attributes
                        vm.selectedNodeInfos.metrics.push({
                            name: vm.getMetricByShortcut(key),
                            value: node.attributes[key],
                            shortcut: key
                        });
                    }else if((""+key) !== "pos"){
                        subAttributes.push({name: key, value: node.attributes[key]})
                    }
                });
                // console.dir(vm.selectedNodeInfos);
                //match sub attributes to their parent
                for(var i = 0; i < subAttributes.length; i++){
                    var subAttr = subAttributes[i];
                    var parts = subAttr.name.split("_");
                    if(parts[1] === "relative"){
                        for(var j = 0; j < vm.selectedNodeInfos.metrics.length; j++){
                            var m = vm.selectedNodeInfos.metrics[j];
                            if(m.shortcut === parts[0]){
                                m.relative = vm.roundNumber(subAttr.value, 2);
                            }
                        }
                    }
                }

                //display graph
                var usedNames = [];
                var neighbourhoodNodes = [];
                var neighbourhoodEdges = [];
                for(var i = 0; i < vm.dataset.edges.length; i++){
                    if(vm.dataset.edges[i].sourceNode === node.name || vm.dataset.edges[i].destinationNode === node.name){
                        neighbourhoodEdges.push(vm.dataset.edges[i]);
                        if(usedNames.indexOf(vm.dataset.edges[i].sourceNode) === -1){
                            neighbourhoodNodes.push(getNodeByName(vm.dataset.edges[i].sourceNode));
                            usedNames.push(vm.dataset.edges[i].sourceNode);
                        }
                        if(usedNames.indexOf(vm.dataset.edges[i].destinationNode) === -1){
                            neighbourhoodNodes.push(getNodeByName(vm.dataset.edges[i].destinationNode));
                            usedNames.push(vm.dataset.edges[i].destinationNode);
                        }
                    }
                }
                // console.dir(neighbourhoodNodes);
                // console.dir(neighbourhoodEdges);
                createCircleGraph('node-canvas', neighbourhoodNodes, neighbourhoodEdges, {shortcut: "tmp"}, function (event) {
                    var node = event.target;
                    for(var i = 0; i < vm.dataset.nodes.length; i++){
                        if(vm.dataset.nodes[i].name === node._private.data.id){
                            console.dir(vm.dataset.nodes[i]);
                            vm.showNode(vm.dataset.nodes[i]);
                            return;
                        }
                    }
                });
            };
            //endregion

            //region HELPER
            var getNodeByName = function getNodeByName(name){
                for(var i = 0; i < vm.dataset.nodes.length; i++){
                    var n = vm.dataset.nodes[i];
                    if(n.name == name){
                        return n;
                    }
                }
                return null;
            };

            var sort_by = function(field, reverse, primer){

                var key = primer ?
                    function(x) {return primer(x[field])} :
                    function(x) {return x[field]};

                reverse = !reverse ? 1 : -1;

                return function (A, B) {
                    // return a = key(a), b = key(b), reverse * ((a > b) - (b > a));
                    return (A < B ? -1 : (A > B ? 1 : 0)) * [1,-1][+!!reverse];
                }

            };

            function createCircleGraph(canvasId, nodes, edges, metric, onClick){
                var cy = window.cy = cytoscape({
                    container: document.getElementById(canvasId),
                    elements: positions,
                    style: [
                        {
                            selector: "core",
                            style: {
                                "selection-box-color": "#AAD8FF",
                                "selection-box-border-color": "#8BB0D0",
                                "selection-box-opacity": "0.5"
                            }
                        },
                        {
                            selector: "node",
                            style: {
                                // width: "mapData(score, 0, 0.006769776522008331, 20, 60)",
                                // height: "mapData(score, 0, 0.006769776522008331, 20, 60)",
                                content: "data(id)",
                                "font-size": "12px",
                                "text-valign": "center",
                                "text-halign": "center",
                                "background-color": "#555",
                                "text-outline-color": "#555",
                                "text-outline-width": "2px",
                                color: "#fff",
                                "overlay-padding": "6px",
                                "z-index": "10"
                            }
                        },
                        {
                            selector: "node[?attr]",
                            style: {
                                shape: "rectangle",
                                "background-color": "#aaa",
                                "text-outline-color": "#aaa",
                                width: "20px",
                                height: "16px",
                                "font-size": "6px",
                                "z-index": "1"
                            }
                        }
                    ]
                });
                //on click display information on the node
                cy.on('tap', 'node', onClick ? onClick: function (event) {
                    var node = event.target;
                    var contentString = "";
                    console.dir(node._private.data.attributes);
                    Object.keys(node._private.data.attributes).forEach(function(key,index) {
                        if(""+key.indexOf('_')<0 && (""+key) !== "pos") {
                            contentString = contentString + "<strong>" + vm.getMetricByShortcut(key) + ":</strong> " + node._private.data.attributes[key] + "<br />";
                        }
                    });
                    // var nodeAttributes = node.attributes;
                    node.qtip({
                        content: contentString,
                        show: {
                            event: event.type,
                            ready: true
                        },
                        hide: {
                            event: 'mouseout unfocus'
                        }
                    }, event);
                    // console.log(evt.target.id())
                });

                var positions = {};
                var maxMetricValue = 0;
                for(var n = 0; n < nodes.length; n++){
                    var node = nodes[n];
                    if(node.attributes[metric.shortcut] > maxMetricValue){
                        maxMetricValue = node.attributes[metric.shortcut];
                    }
                    cy.add({
                        data: {
                            id: node.name,
                            attributes: node.attributes
                        },
                    style:{
                            width: 25 + parseInt((node.attributes["ndeg_relative"]?node.attributes["ndeg_relative"]:0)) + "px",
                            height: 25 + parseInt((node.attributes["ndeg_relative"]?node.attributes["ndeg_relative"]:0)) + "px"
                    }});
                    // console.log(node.attributes["ndeg_relative"]?node.attributes["ndeg_relative"]:0 , " -> " , parseInt((node.attributes["ndeg_relative"]?node.attributes["ndeg_relative"]:0)));
                    // console.log(node.name + ": " + node.attributes.pos.split(":")[0] + " * " + 100000000 + " = " + parseInt(node.attributes.pos.split(":")[0]*100000000));
                    // console.log(node.name + ": " + node.attributes.pos.split(":")[1] + " * " + 100000000 + " = " + parseInt(node.attributes.pos.split(":")[1]*100000000));
                    positions[node.name] = {
                        x: parseInt(node.attributes.pos?node.attributes.pos.split(":")[0]*100000:Math.random()),
                        y: parseInt(node.attributes.pos?node.attributes.pos.split(":")[1]*100000:Math.random())
                    };
                }

                // console.dir(positions);

                for(var e = 0; e < edges.length; e++){
                    var edge = edges[e];
                    cy.add({
                        data: {
                            id: edge.name,
                            source: edge.sourceNode,
                            target: edge.destinationNode,
                            attributes: edge.attributes
                        },
                        selectable: false
                    });
                }
                var options = {
                    name: 'concentric',

                    fit: true, // whether to fit the viewport to the graph
                    padding: 30, // the padding on fit
                    startAngle: 3 / 2 * Math.PI, // where nodes start in radians
                    sweep: undefined, // how many radians should be between the first and last node (defaults to full circle)
                    clockwise: true, // whether the layout should go clockwise (true) or counterclockwise/anticlockwise (false)
                    equidistant: false, // whether levels have an equal radial distance betwen them, may cause bounding box overflow
                    minNodeSpacing: 10, // min spacing between outside of nodes (used for radius adjustment)
                    boundingBox: undefined, // constrain layout bounds; { x1, y1, x2, y2 } or { x1, y1, w, h }
                    avoidOverlap: true, // prevents node overlap, may overflow boundingBox if not enough space
                    nodeDimensionsIncludeLabels: false, // Excludes the label when calculating node bounding boxes for the layout algorithm
                    height: undefined, // height of layout area (overrides container height)
                    width: undefined, // width of layout area (overrides container width)
                    spacingFactor: undefined, // Applies a multiplicative factor (>0) to expand or compress the overall area that the nodes take up
                    concentric: function( node ){ // returns numeric value for each node, placing higher nodes in levels towards the centre
                        // return node.degree();
                        return node._private.data.attributes[metric.shortcut];
                    },
                    levelWidth: function( nodes ){ // the variation of concentric values in each level
                        // return nodes.maxDegree() / 4;
                        return maxMetricValue / 10;
                    },
                    animate: false, // whether to transition the node positions
                    animationDuration: 500, // duration of animation in ms if enabled
                    animationEasing: undefined, // easing of animation if enabled
                    ready: undefined, // callback on layoutready
                    stop: undefined // callback on layoutstop
                };
                var layoutRunner = cy.makeLayout(options);
                layoutRunner.run();
            }
            //region
        }]
    });