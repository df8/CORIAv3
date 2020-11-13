package com.coria.v3.metrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by David Fradin, 2020
 * The MetricAlgorithmFactory singleton class contains all Algorithms and Algorithm Variants (such as "Normalised" or "Corrected") known to the system and their dependencies among each other.
 * Whenever you implement a new algorithm, please create a similar metricAlgorithms.add() directive as the ones below.
 */
public class MetricAlgorithmFactory {

    private static MetricAlgorithmFactory instance;

    public static MetricAlgorithmFactory getInstance() {
        if (instance == null)
            instance = new MetricAlgorithmFactory();
        return instance;
    }

    public MetricAlgorithmFactory() {
        instance = this;
    }

    public List<MetricAlgorithm> getList() {
        List<MetricAlgorithm> metricAlgorithms = new ArrayList<>();
        //TODO /2 Set up image hosting to display formulas in frontend
        //TODO /1 collect all descriptions
        //TODO /1 Description with formula for AverageNeighbourDegreeCorrected
        //TODO /1 Description with formula for AverageNeighbourDegreeCorrectedAndNormalised

        try {
            MetricAlgorithmVariant mavNodeDegreeDefault = new MetricAlgorithmVariant("Default", "");
            MetricAlgorithmVariant mavNodeDegreeNormalised = new MetricAlgorithmVariant("Normalised", "", List.of(mavNodeDegreeDefault));
            metricAlgorithms.add(new MetricAlgorithm("Node Degree",
                    "Computes the degree for each node in the dataset. The degree of a node degree is the number of edges adjacent to the node.",
                    MetricAlgorithmType.Node, List.of(mavNodeDegreeDefault, mavNodeDegreeNormalised)));

            MetricAlgorithmVariant mavShortestPathLengthsDefault = new MetricAlgorithmVariant("Default", "", null);
            metricAlgorithms.add(new MetricAlgorithm("Shortest Path Lengths",
                    "Computes and stores into the database the lengths for all possible shortest paths between any pair of nodes.",
                    MetricAlgorithmType.ShortestPathLength, List.of(mavShortestPathLengthsDefault)));

            //TODO /1 add Global Average Shortest Path Length (vs Local per node)
            MetricAlgorithmVariant mavAverageShortestPathLengthDefault = new MetricAlgorithmVariant("Default", "", List.of(mavShortestPathLengthsDefault));
            MetricAlgorithmVariant mavAverageShortestPathLengthNormalised = new MetricAlgorithmVariant("Normalised", "", List.of(mavAverageShortestPathLengthDefault));
            metricAlgorithms.add(new MetricAlgorithm("Average Shortest Path Length",
                    "Computes the average of all shortest path lengths between all possible pairs of nodes.",
                    MetricAlgorithmType.Node, List.of(
                    mavAverageShortestPathLengthDefault,
                    mavAverageShortestPathLengthNormalised)));

            MetricAlgorithmVariant mavEccentricityDefault = new MetricAlgorithmVariant("Default", "", List.of(mavShortestPathLengthsDefault));
            MetricAlgorithmVariant mavEccentricityNormalised = new MetricAlgorithmVariant("Normalised", "", List.of(mavEccentricityDefault));
            metricAlgorithms.add(new MetricAlgorithm("Eccentricity",
                    "<p>Computes the eccentricity of a connected graph.</p><p>In a graph G, if d(u,v) is the shortest length between two nodes u and v (ie the number of edges of the shortest path) let e(u) be the d(u,v) such that v is the farthest of u. Eccentricity of a graph G is a subgraph induced by vertices u with minimum e(u). The eccentricity of a node v is the maximum distance from v to all other nodes in G.</p>",
                    MetricAlgorithmType.Node, List.of(
                    mavEccentricityDefault,
                    mavEccentricityNormalised)));


            MetricAlgorithmVariant mavBetweennessCentralityDefault = new MetricAlgorithmVariant("Default", "");
            MetricAlgorithmVariant mavBetweennessCentralityNormalised = new MetricAlgorithmVariant("Normalised", "", List.of(mavBetweennessCentralityDefault));
            metricAlgorithms.add(new MetricAlgorithm("Betweenness Centrality",
                    "Compute the betweenness centrality of each vertex of a given graph.<br/>" +
                            "The betweenness centrality counts how many shortest paths between each pair of nodes of the graph pass by a node. It does it for all nodes of the graph.<br/>" +
                            "<img src=\"http://graphstream-project.org/media/img/betweennessCentrality.png\"><br/>" +
                            "The above graph shows the betweenness centrality applied to a grid graph, where color indicates centrality, green is lower centrality and red is maximal centrality.", MetricAlgorithmType.Node, List.of(
                    mavBetweennessCentralityDefault,
                    mavBetweennessCentralityNormalised)));


            metricAlgorithms.add(new MetricAlgorithm("Edge Betweenness Centrality",
                    "Computes the betweenness centrality for each edge. A high Edge Betweenness Centrality value indicates that the given edge is part of a (comparably) high number of shortest paths between pairs of nodes.",
                    MetricAlgorithmType.Edge, List.of(new MetricAlgorithmVariant("Default", ""))));

            MetricAlgorithmVariant mavAverageNeighbourDegreeDefault = new MetricAlgorithmVariant("Default", "", List.of(mavNodeDegreeDefault));
            MetricAlgorithmVariant mavAverageNeighbourDegreeCorrected = new MetricAlgorithmVariant("Corrected",
                    "Computes the Average Neighbour Degree and applies the correction method described by A. Baumann in her 2013 Master Thesis to each node's value. See pp. 93-94, Equation 28: Corrected average neighbor degree for node i",
                    List.of(mavNodeDegreeDefault));
            MetricAlgorithmVariant mavAverageNeighbourDegreeCorrectedAndNormalised = new MetricAlgorithmVariant("Corrected and Normalised",
                    "Applies the Min-Max-Normalization to the values produced by the <b>Corrected</b> variant of <b>Average Neighbour Degree</b>. Every value is linearly transformed into a decimal between 0 and 1.",
                    List.of(mavAverageNeighbourDegreeCorrected));
            metricAlgorithms.add(
                    new MetricAlgorithm("Average Neighbour Degree",
                            "Computes the average degree of the neighborhood of each node.<br/>" +
                                    "The average degree of a node i is <br/>" +
                                    "<img src=\"https://networkx.github.io/documentation/networkx-1.10/_images/math/ef382473d588681f1efa5156fdf589c8cc3d7fb0.png\"/>" +
                                    "<br/>where N(i) are the neighbors of node i and k_j is the degree of node j which belongs to N(i). For weighted graphs, an analogous measure can be defined <br/>" +
                                    "<img src=\"https://networkx.github.io/documentation/networkx-1.10/_images/math/20848505733e71688e5bfbe310322bac4b4ac407.png\"/>" +
                                    "<br/>where s_i is the weighted degree of node i, w_{ij} is the weight of the edge that links i and j and N(i) are the neighbors of node i.", MetricAlgorithmType.Node, List.of(
                            mavAverageNeighbourDegreeDefault,
                            mavAverageNeighbourDegreeCorrected,
                            mavAverageNeighbourDegreeCorrectedAndNormalised)));

            MetricAlgorithmVariant mavIteratedAverageNeighbourDegreeDefault = new MetricAlgorithmVariant("Default", "", List.of(mavNodeDegreeDefault, mavShortestPathLengthsDefault));
            MetricAlgorithmVariant mavIteratedAverageNeighbourDegreeCorrected = new MetricAlgorithmVariant("Corrected", "", List.of(mavNodeDegreeDefault, mavShortestPathLengthsDefault));
            MetricAlgorithmVariant mavIteratedAverageNeighbourDegreeCorrectedAndNormalised = new MetricAlgorithmVariant("Corrected and Normalised", "", List.of(mavIteratedAverageNeighbourDegreeCorrected));
            metricAlgorithms.add(
                    new MetricAlgorithm("Iterated Average Neighbour Degree",
                            "<p>Calculates the value of the average degree of the node 2nd hop nodes.</p><p>This Metric is based on the Master Thesis 'Internet Resilience and Connectivity Risks for Online Businesses' by Annika Baumann</p>",
                            MetricAlgorithmType.Node, List.of(
                            mavIteratedAverageNeighbourDegreeDefault,
                            mavIteratedAverageNeighbourDegreeCorrected,
                            mavIteratedAverageNeighbourDegreeCorrectedAndNormalised)));

            MetricAlgorithmVariant mavLocalClusteringCoefficientsDefault = new MetricAlgorithmVariant("Default",
                    "Computes the clustering coefficient for all nodes in the graph. The complexity if O(d^2) where d is the degree of the node. Compute the clustering coefficient for nodes." +
                            "<br/>" +
                            "For unweighted graphs, the clustering of a node u is the fraction of possible triangles through that node that exist," +
                            "<br/>" +
                            "<img src=\"https://networkx.github.io/documentation/networkx-1.10/_images/math/e02119031edbc373d28811944663c191c9e53d1c.png\"/>" +
                            "<br/>where T(u) is the number of triangles through node u and deg(u) is the degree of u. For weighted graphs, the clustering is defined as the geometric average of the" +
                            "subgraph edge weights" +
                            "<br/>" +
                            "<img src=\"https://networkx.github.io/documentation/networkx-1.10/_images/math/ea71c34aaf758ad8c19f0129bc003db96c8ca69b.png\"/>" +
                            "<br/>The edge weights \\\\hat{w}_{uv} are normalised by the maximum weight in the network" +
                            "<img src=\"https://networkx.github.io/documentation/networkx-1.10/_images/math/fd424b86ab2590d0323aae53278c3fa4ca6cabf7.png\"/>" +
                            "<br/>The value of c_u is assigned to 0 if deg(u) &lt; 2.",
                    List.of(mavNodeDegreeDefault));
            MetricAlgorithmVariant mavLocalClusteringCoefficientsCorrected = new MetricAlgorithmVariant("Corrected", "", List.of(mavNodeDegreeDefault, mavLocalClusteringCoefficientsDefault));
            MetricAlgorithmVariant mavLocalClusteringCoefficientsCorrectedAndNormalised = new MetricAlgorithmVariant("Corrected and Normalised", "", List.of(mavLocalClusteringCoefficientsCorrected));
            metricAlgorithms.add(
                    new MetricAlgorithm("Local Clustering Coefficients", "", MetricAlgorithmType.Node, List.of(
                            mavLocalClusteringCoefficientsDefault,
                            mavLocalClusteringCoefficientsCorrected,
                            mavLocalClusteringCoefficientsCorrectedAndNormalised)));

            metricAlgorithms.add(
                    new MetricAlgorithm("Average Clustering Coefficient", "Computes the value of the average clustering coefficient of the graph", MetricAlgorithmType.Dataset, List.of(
                            new MetricAlgorithmVariant("Default", ""))));

            metricAlgorithms.add(
                    new MetricAlgorithm("Average Node Degree", "Computes the value of the average degree for the entire dataset.", MetricAlgorithmType.Dataset, List.of(
                            new MetricAlgorithmVariant("Default", ""))));

            metricAlgorithms.add(
                    new MetricAlgorithm("Graph Density",
                            "The density is the number of links in the graph divided by the total number of possible links.",
                            MetricAlgorithmType.Dataset, List.of(
                            new MetricAlgorithmVariant("Default", ""))));

            metricAlgorithms.add(
                    new MetricAlgorithm("Graph Diameter",
                            "Computes the diameter of the graph. The diameter of the graph is the largest of all the shortest paths from any node to any other node.<br/>Note that this operation can be quite costly, the algorithm used to compute all shortest paths is the Floyd-Warshall algorithm whose complexity is at worst of O(n^3).",
                            MetricAlgorithmType.Dataset, List.of(
                            new MetricAlgorithmVariant("Default", ""))));


            MetricAlgorithmVariant mavUnifiedRiskScoreDefault = new MetricAlgorithmVariant("Default", "", List.of(
                    mavNodeDegreeNormalised,
                    mavAverageNeighbourDegreeCorrectedAndNormalised,
                    mavIteratedAverageNeighbourDegreeCorrectedAndNormalised,
                    mavBetweennessCentralityNormalised,
                    mavEccentricityNormalised,
                    mavAverageShortestPathLengthNormalised));

            metricAlgorithms.add(new MetricAlgorithm("Unified Risk Score",
                    "<p>1. Executes correction of <code>Clustering Coefficients, Average Neighbour Degree</code> and <code>Iterated Average Neighbour Degree</code> algorithms</p>" +
                            "<p>2. Executes normalization of all metrics listed below</p>" +
                            "<p>3. Calculates Risk Score based on the following weighting: <br/>" +
                            "<ul><li><code>Node Degree - Normalised</code>: 0.25</li>" +
                            "<li><code>Average Neighbour Degree - Corrected and Normalised</code>: 0.15</li>" +
                            "<li><code>Iterated Average Neighbour Degree - Corrected and Normalised</code>: 0.1</li>" +
                            "<li><code>Betweenness Centrality - Normalised</code>: 0.25</li>" +
                            "<li><code>Eccentricity - Normalised</code>: 0.125</li>" +
                            "<li><code>Average Shortest Path Length - Normalised</code>: 0.125</li></ul>" +
                            "<strong>NOTE:</strong> All the metrics above are dependencies of Unified Risk Score, hence they will be computed during the execution of this metric!</p>" +
                            "<p>This Metric is based on the Master Thesis 'Internet Resilience and Connectivity Risks for Online Businesses' by Annika Baumann</p>",
                    MetricAlgorithmType.Node, List.of(mavUnifiedRiskScoreDefault)));

            metricAlgorithms.add(new MetricAlgorithm("Connectivity Risk Classification",
                    "",
                    MetricAlgorithmType.Node, List.of(
                    new MetricAlgorithmVariant("Default", "", List.of(mavLocalClusteringCoefficientsCorrectedAndNormalised, mavUnifiedRiskScoreDefault),
                            List.of(
                                    new MetricAlgorithmVariantParameter(0, "threshold-low", "", MetricAlgorithmVariantParameter.MetricAlgorithmVariantParameterType.FLOAT, "0.45", true),
                                    new MetricAlgorithmVariantParameter(1, "threshold-high", "", MetricAlgorithmVariantParameter.MetricAlgorithmVariantParameterType.FLOAT, "0.55", true)
                            ))
            )));

            metricAlgorithms.add(new MetricAlgorithm("Layout Position Circular",
                    "Computes X and Y positions for each node such that the nodes build a circle." +
                            "Source: <a href=\"https://networkx.org/documentation/stable/reference/generated/networkx.drawing.layout.circular_layout.html#networkx.drawing.layout.circular_layout\">" +
                            "https://networkx.org/documentation/stable/reference/generated/networkx.drawing.layout.circular_layout.html#networkx.drawing.layout.circular_layout</a>",
                    MetricAlgorithmType.LayoutPosition, List.of(new MetricAlgorithmVariant("Default", ""))));

            metricAlgorithms.add(new MetricAlgorithm("Layout Position Spectral",
                    "Computes X and Y positions for each node using the eigenvectors of the graph Laplacian." +
                            "<q>\"Using the unnormalised Laplacian, the layout shows possible clusters of nodes which are an approximation of the ratio cut.\"</q>" +
                            "Source: <a href=\"https://networkx.github.io/documentation/stable/reference/generated/networkx.drawing.layout.spectral_layout.html\">" +
                            "https://networkx.github.io/documentation/stable/reference/generated/networkx.drawing.layout.spectral_layout.html</a>",
                    MetricAlgorithmType.LayoutPosition, List.of(new MetricAlgorithmVariant("Default", ""))));

            metricAlgorithms.add(new MetricAlgorithm("Layout Position Spring",
                    "Computes X and Y positions for each node using the Fruchterman-Reingold force-directed algorithm. " +
                            "Source: <a href=\"https://networkx.org/documentation/stable/reference/generated/networkx.drawing.layout.spring_layout.html#networkx.drawing.layout.spring_layout\">" +
                            "https://networkx.org/documentation/stable/reference/generated/networkx.drawing.layout.spring_layout.html#networkx.drawing.layout.spring_layout</a>",
                    MetricAlgorithmType.LayoutPosition, List.of(new MetricAlgorithmVariant("Default", ""))));

            metricAlgorithms.add(new MetricAlgorithm("Layout Position Random",
                    "Generates random X and Y positions for each node with both coordinates being in the range [0,1). " +
                            "Source: <a href=\"https://networkx.org/documentation/stable/reference/generated/networkx.drawing.layout.random_layout.html#networkx.drawing.layout.random_layout\">" +
                            "https://networkx.org/documentation/stable/reference/generated/networkx.drawing.layout.random_layout.html#networkx.drawing.layout.random_layout</a>",
                    MetricAlgorithmType.LayoutPosition, List.of(new MetricAlgorithmVariant("Default", ""))));

            metricAlgorithms.add(new MetricAlgorithm("Layout Position Shell",
                    "Computes X and Y positions for each node such that the nodes are arranged in concentric circles. " +
                            "Source: <a href=\"https://networkx.org/documentation/stable/reference/generated/networkx.drawing.layout.shell_layout.html#networkx.drawing.layout.shell_layout\">" +
                            "https://networkx.org/documentation/stable/reference/generated/networkx.drawing.layout.shell_layout.html#networkx.drawing.layout.shell_layout</a>",
                    MetricAlgorithmType.LayoutPosition, List.of(new MetricAlgorithmVariant("Default", ""))));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return metricAlgorithms;
    }


}
