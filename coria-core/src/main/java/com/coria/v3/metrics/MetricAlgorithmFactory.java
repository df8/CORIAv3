package com.coria.v3.metrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by David Fradin, 2020
 * The MetricAlgorithmFactory singleton class contains all Algorithms and Algorithm Variants (such as "Normalised" or "Corrected") known to the system and their dependencies among each other.
 * Whenever you implement a new algorithm, please create a similar metricAlgorithms.add() directive as the ones below.
 * Contains descriptions of metrics written by Sebastian Gross, 2017.
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

        String formulaAverageNeighbourDegreeCorrected = "<img style=\"max-width: 100%;\" src=\"data:image/png;base64,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\" alt=\"formula for Average Neighbour Degree Corrected\" />";

        try {
            MetricAlgorithmVariant mavNodeDegreeDefault = new MetricAlgorithmVariant("Default", "");
            MetricAlgorithmVariant mavNodeDegreeNormalised = new MetricAlgorithmVariant("Normalised", "", List.of(mavNodeDegreeDefault));
            metricAlgorithms.add(new MetricAlgorithm("Node Degree",
                    "Computes the degree for each node in the dataset. The degree of a node i is defined as the number of nodes adjacent to this node, i.e. the number of direct neighbours. In the context of CORIA, Baumann (2013, p. 90) argues, that an Autonomous System with a low node degree is likely to be poorly connected to the network and thus rather error prone, while an AS with a high degree contributes to better “connectedness” and thus leads to the AS being rather vulnerable to targeted attacks instead.",
                    MetricAlgorithmType.Node, List.of(mavNodeDegreeDefault, mavNodeDegreeNormalised)));

            MetricAlgorithmVariant mavShortestPathLengthsDefault = new MetricAlgorithmVariant("Default", "", null);
            metricAlgorithms.add(new MetricAlgorithm("Shortest Path Lengths",
                    "Computes and stores into the database the distance for all possible shortest paths between any pair of nodes. A path from node u to node v is called shortest path if its sequence contains a minimal number of edges, as there is no shorter sequence from u to v in the graph. This metric computes the shortest paths from any node u to any other node v with u!=v and returns the distance d(u,v) as the number of edges between these two nodes. As CORIA is designed to work with undirected graphs we know d(u,v)=d(v,u) and can therefore reduce redundancy in the resulting matrix by omitting with (v,u,d(v,u)) if a tuple of (u,v,d(u,v)) has already been provided. The total number of rows is: 1/2 |V|*(|V|-1).",
                    MetricAlgorithmType.ShortestPathLength, List.of(mavShortestPathLengthsDefault)));

            MetricAlgorithmVariant mavAverageShortestPathLengthDefault = new MetricAlgorithmVariant("Default", "", List.of(mavShortestPathLengthsDefault));
            MetricAlgorithmVariant mavAverageShortestPathLengthNormalised = new MetricAlgorithmVariant("Normalised", "Applies Max-Min scaling to the results of Average Shortest Path Length / Default into the target range [0,1].", List.of(mavAverageShortestPathLengthDefault));
            metricAlgorithms.add(new MetricAlgorithm("Average Shortest Path Length",
                    "The local average shortest path length (ASPL) for an individual node i is defined as the mean number of hops on a shortest path from i to every other node j. In the context of network connectivity, Baumann (2013, p. 90) suggests that a lower value of local ASPL indicates a more robust and objectively more important node, while a node with higher local ASPL value is more vulnerable to link failures.",
                    MetricAlgorithmType.Node, List.of(
                    mavAverageShortestPathLengthDefault,
                    mavAverageShortestPathLengthNormalised)));

            MetricAlgorithmVariant mavEccentricityDefault = new MetricAlgorithmVariant("Default", "", List.of(mavShortestPathLengthsDefault));
            MetricAlgorithmVariant mavEccentricityNormalised = new MetricAlgorithmVariant("Normalised", "Applies Max-Min scaling to the results of Eccentricity / Default into the target range [0,1].", List.of(mavEccentricityDefault));
            metricAlgorithms.add(new MetricAlgorithm("Eccentricity",
                    "<p>Computes the eccentricity of a connected graph.</p><p>In a graph G, if d(u,v) is the shortest length between two nodes u and v (ie the number of edges of the shortest path) let e(u) be the d(u,v) such that v is the farthest of u. Eccentricity of a graph G is a subgraph induced by vertices u with minimum e(u). The eccentricity of a node v is the maximum distance from v to all other nodes in G.</p>",
                    MetricAlgorithmType.Node, List.of(
                    mavEccentricityDefault,
                    mavEccentricityNormalised)));


            MetricAlgorithmVariant mavBetweennessCentralityDefault = new MetricAlgorithmVariant("Default", "");
            MetricAlgorithmVariant mavBetweennessCentralityNormalised = new MetricAlgorithmVariant("Normalised", "", List.of(mavBetweennessCentralityDefault));
            metricAlgorithms.add(new MetricAlgorithm("Betweenness Centrality",
                    "Compute the betweenness centrality of each vertex of a given graph.<br/>" +
                            "<p>For each node u, betweenness centrality (BC) defines the number of times u is part of a shortest path between any other two nodes in the graph. This metric assigns a higher score to nodes which are more central and hence more often required for other nodes to communicate with each other. In contrast, nodes that are not part of any shortest path, except as a source node or target node, receive the betweenness centrality score of 0. The largest possible value for betweenness centrality is (|V|-1)*(|V|-2)</p>" +
                            "<img src=\"http://graphstream-project.org/media/img/betweennessCentrality.png\"><br/>" +
                            "The above graph shows the betweenness centrality applied to a grid graph, where green color indicates lower centrality and red higher centrality.", MetricAlgorithmType.Node, List.of(
                    mavBetweennessCentralityDefault,
                    mavBetweennessCentralityNormalised)));


            metricAlgorithms.add(new MetricAlgorithm("Edge Betweenness Centrality",
                    "Computes the betweenness centrality for each edge. A high Edge Betweenness Centrality value indicates that the given edge is part of a (comparably) high number of shortest paths between pairs of nodes and thus is an important link within the graph.",
                    MetricAlgorithmType.Edge, List.of(new MetricAlgorithmVariant("Default", ""))));

            MetricAlgorithmVariant mavAverageNeighbourDegreeDefault = new MetricAlgorithmVariant("Default", "", List.of(mavNodeDegreeDefault));
            MetricAlgorithmVariant mavAverageNeighbourDegreeCorrected = new MetricAlgorithmVariant("Corrected",
                    "Computes the Average Neighbour Degree and applies the correction method described by A. Baumann in her 2013 Master Thesis to each node's value. See pp. 93-94, Equation 28: Corrected average neighbor degree for node i. <p><strong>Formula:</strong><br/> " + formulaAverageNeighbourDegreeCorrected + "</p>",
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
                            "<p>Calculates the value of the average degree of the node 2nd hop nodes.</p><p>This metric is based on the 2013 master's thesis 'Internet Resilience and Connectivity Risks for Online Businesses' by Annika Baumann</p>",
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
                    "<p>The Unified Risk Score is computed based on a linear combination formula with the following weighting: <br/>" +
                            "<ul><li><code>Node Degree - Normalised</code>: 0.25</li>" +
                            "<li><code>Average Neighbour Degree - Corrected and Normalised</code>: 0.15</li>" +
                            "<li><code>Iterated Average Neighbour Degree - Corrected and Normalised</code>: 0.1</li>" +
                            "<li><code>Betweenness Centrality - Normalised</code>: 0.25</li>" +
                            "<li><code>Eccentricity - Normalised</code>: 0.125</li>" +
                            "<li><code>Average Shortest Path Length - Normalised</code>: 0.125</li></ul>" +
                            "<strong>NOTE:</strong> All six listed metrics are direct dependencies of Unified Risk Score, hence if not computed yet, they will be computed during the execution of this metric.</p>" +
                            "<p>This metric is based on the 2013 master's thesis 'Internet Resilience and Connectivity Risks for Online Businesses' by Annika Baumann.</p>",
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
                    "<p>Computes X and Y positions for each node such that the nodes build a circle.</p>" +
                            "<p>Source: [1] <a href=\"https://networkx.org/documentation/stable/reference/generated/networkx.drawing.layout.circular_layout.html#networkx.drawing.layout.circular_layout\">" +
                            "https://networkx.org/documentation/stable/reference/generated/networkx.drawing.layout.circular_layout.html#networkx.drawing.layout.circular_layout</a></p>",
                    MetricAlgorithmType.LayoutPosition, List.of(new MetricAlgorithmVariant("Default", ""))));

            metricAlgorithms.add(new MetricAlgorithm("Layout Position Spectral",
                    "<p>Computes X and Y positions for each node using the eigenvectors of the graph Laplacian." +
                            "<q>\"Using the unnormalised Laplacian, the layout shows possible clusters of nodes which are an approximation of the ratio cut.\"</q></p>" +
                            "<p>Source: [1] <a href=\"https://networkx.github.io/documentation/stable/reference/generated/networkx.drawing.layout.spectral_layout.html\">" +
                            "https://networkx.github.io/documentation/stable/reference/generated/networkx.drawing.layout.spectral_layout.html</a></p>",
                    MetricAlgorithmType.LayoutPosition, List.of(new MetricAlgorithmVariant("Default", ""))));

            metricAlgorithms.add(new MetricAlgorithm("Layout Position Spring",
                    "<p>Computes X and Y positions for each node using the Fruchterman-Reingold force-directed algorithm. </p>" +
                            "<p>Source: [1] <a href=\"https://networkx.org/documentation/stable/reference/generated/networkx.drawing.layout.spring_layout.html#networkx.drawing.layout.spring_layout\">" +
                            "https://networkx.org/documentation/stable/reference/generated/networkx.drawing.layout.spring_layout.html#networkx.drawing.layout.spring_layout</a></p>",
                    MetricAlgorithmType.LayoutPosition, List.of(new MetricAlgorithmVariant("Default", ""))));

            metricAlgorithms.add(new MetricAlgorithm("Layout Position Random",
                    "<p>Generates random X and Y positions for each node with both coordinates being in the range [0,1). </p>" +
                            "<p>Source: [1] <a href=\"https://networkx.org/documentation/stable/reference/generated/networkx.drawing.layout.random_layout.html#networkx.drawing.layout.random_layout\">" +
                            "https://networkx.org/documentation/stable/reference/generated/networkx.drawing.layout.random_layout.html#networkx.drawing.layout.random_layout</a></p>",
                    MetricAlgorithmType.LayoutPosition, List.of(new MetricAlgorithmVariant("Default", ""))));

            metricAlgorithms.add(new MetricAlgorithm("Layout Position Shell",
                    "<p>Computes X and Y positions for each node such that the nodes are arranged in concentric circles.</p>" +
                            "<p>Source: [1] <a href=\"https://networkx.org/documentation/stable/reference/generated/networkx.drawing.layout.shell_layout.html#networkx.drawing.layout.shell_layout\">" +
                            "https://networkx.org/documentation/stable/reference/generated/networkx.drawing.layout.shell_layout.html#networkx.drawing.layout.shell_layout</a></p>",
                    MetricAlgorithmType.LayoutPosition, List.of(new MetricAlgorithmVariant("Default", ""))));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return metricAlgorithms;
    }


}
