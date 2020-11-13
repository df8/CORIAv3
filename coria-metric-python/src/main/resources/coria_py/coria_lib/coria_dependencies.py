# Created by David Fradin, 2020

from coria_lib.coria_aspl_eccentricity import average_shortest_path_length, eccentricity, graph_diameter
from coria_lib.coria_betweenness_centrality import betweenness_centrality, edge_betweenness_centrality
from coria_lib.coria_clustering_coefficient import local_clustering_coefficient_corrected, local_clustering_coefficient_default
from coria_lib.coria_config import USE_CUDA
from coria_lib.coria_layout_position import circular_layout, spring_layout, shell_layout, random_layout, spectral_layout
from coria_lib.coria_node_degree import node_degree, average_node_degree
from coria_lib.coria_normalise import normalise_min_max, normalise_max_min
from coria_lib.coria_average_neighbour_degree import average_neighbour_degree_default, average_neighbour_degree_corrected, iterated_average_neighbour_degree_default, iterated_average_neighbour_degree_corrected
from coria_lib.coria_risk_score import unified_risk_score, connectivity_risk_classification
from coria_lib.coria_shortest_path import shortest_path_length
import sys

#
# This tree covers the following logical layers, where a parent can have multiple children
#
# Metric Algorithm {}
#     Metric Algorithm Variant {}
#         Dependencies [] # List of Metric Algorithm Variants
#         Metric Algorithm Implementation {}
#

CORIA_METRIC_TREE = {
    'node-degree': {
        'node-degree--default': {
            'cpu': node_degree,
            'gpu': node_degree
        },
        'node-degree--normalised': {
            'cpu': normalise_min_max,
            'gpu': normalise_min_max
        }
    },
    'shortest-path-lengths': {
        'shortest-path-lengths--default': {
            'cpu': shortest_path_length,
            'gpu': shortest_path_length
        }
    },
    'average-shortest-path-length': {
        'average-shortest-path-length--default': {
            'cpu': average_shortest_path_length,
            'gpu': average_shortest_path_length
        },
        'average-shortest-path-length--normalised': {
            'cpu': normalise_max_min,
            'gpu': normalise_max_min
        }
    },
    'eccentricity': {
        'eccentricity--default': {
            'cpu': eccentricity,
            'gpu': eccentricity

        },
        'eccentricity--normalised': {
            'cpu': normalise_max_min,
            'gpu': normalise_max_min
        }
    },
    'betweenness-centrality': {
        'betweenness-centrality--default': {
            'cpu': betweenness_centrality,
            'gpu': betweenness_centrality
        },
        'betweenness-centrality--normalised': {
            'cpu': normalise_min_max,
            'gpu': normalise_min_max
        }
    },
    'average-neighbour-degree': {
        'average-neighbour-degree--default': {
            'cpu': average_neighbour_degree_default,
            'gpu': average_neighbour_degree_default
        },
        'average-neighbour-degree--corrected': {
            'cpu': average_neighbour_degree_corrected,
            'gpu': average_neighbour_degree_corrected

        },
        'average-neighbour-degree--corrected-and-normalised': {
            'cpu': normalise_min_max,
            'gpu': normalise_min_max
        }
    },
    'iterated-average-neighbour-degree': {
        'iterated-average-neighbour-degree--default': {
            'cpu': iterated_average_neighbour_degree_default,
            'gpu': iterated_average_neighbour_degree_default
        }
        ,
        'iterated-average-neighbour-degree--corrected': {
            'cpu': iterated_average_neighbour_degree_corrected,
            'gpu': iterated_average_neighbour_degree_corrected
        },
        'iterated-average-neighbour-degree--corrected-and-normalised': {
            'cpu': normalise_min_max,
            'gpu': normalise_min_max
        }
    },
    'local-clustering-coefficients': {
        'local-clustering-coefficients--default': {
            'cpu': local_clustering_coefficient_default,
            'gpu': local_clustering_coefficient_default
        },
        'local-clustering-coefficients--corrected': {
            'cpu': local_clustering_coefficient_corrected,
            'gpu': local_clustering_coefficient_corrected
        },
        'local-clustering-coefficients--corrected-and-normalised': {
            'cpu': normalise_min_max,
            'gpu': normalise_min_max
        }
    },
    'unified-risk-score': {
        'unified-risk-score--default': {
            'cpu': unified_risk_score,
            'gpu': unified_risk_score
        }
    },
    'connectivity-risk-classification': {
        'connectivity-risk-classification--default': {
            'cpu': connectivity_risk_classification,
            'gpu': connectivity_risk_classification
        }
    },
    'average-node-degree': {
        'average-node-degree--default': {
            'cpu': average_node_degree,
            'gpu': average_node_degree
        }
    },
    'graph-diameter': {
        'graph-diameter--default': {
            'cpu': graph_diameter
        }
    },
    'layout-position-circular': {
        'layout-position-circular--default': {
            'cpu': circular_layout
        }
    },
    'layout-position-spring': {
        'layout-position-spring--default': {
            'cpu': spring_layout
        }
    },
    'layout-position-spectral': {
        'layout-position-spectral--default': {
            'cpu': spectral_layout
        }
    },
    'layout-position-random': {
        'layout-position-random--default': {
            'cpu': random_layout
        }
    },
    'layout-position-shell': {
        'layout-position-shell--default': {
            'cpu': shell_layout
        }
    },
    'edge-betweenness-centrality': {
        'edge-betweenness-centrality--default': {
            'cpu': edge_betweenness_centrality,
            'gpu': edge_betweenness_centrality
        }
    }
}

CORIA_METRIC_DEPENDENCIES = {
    'node-degree--default': [],
    'node-degree--normalised': ['node-degree--default'],
    'shortest-path-lengths--default': [],
    'average-shortest-path-length--default': ['shortest-path-lengths--default'],
    'average-shortest-path-length--normalised': ['average-shortest-path-length--default'],
    'eccentricity--default': ['shortest-path-lengths--default'],
    'eccentricity--normalised': ['eccentricity--default'],
    'betweenness-centrality--default': [],
    'betweenness-centrality--normalised': ['betweenness-centrality--default'],
    'average-neighbour-degree--default': ['node-degree--default'],
    'average-neighbour-degree--corrected': ['node-degree--default'],
    'average-neighbour-degree--corrected-and-normalised': ['average-neighbour-degree--corrected'],
    'iterated-average-neighbour-degree--default': ['node-degree--default', 'shortest-path-lengths--default'],
    'iterated-average-neighbour-degree--corrected': ['node-degree--default', 'shortest-path-lengths--default'],
    'iterated-average-neighbour-degree--corrected-and-normalised': ['iterated-average-neighbour-degree--corrected'],
    'local-clustering-coefficients--default': [],
    'local-clustering-coefficients--corrected': ['local-clustering-coefficients--default'],
    'local-clustering-coefficients--corrected-and-normalised': ['local-clustering-coefficients--corrected'],
    'unified-risk-score--default': [
        'node-degree--normalised',
        'average-neighbour-degree--corrected-and-normalised',
        'iterated-average-neighbour-degree--corrected-and-normalised',
        'betweenness-centrality--normalised',
        'eccentricity--normalised',
        'average-shortest-path-length--normalised'
    ],
    'connectivity-risk-classification--default': ['unified-risk-score--default', 'local-clustering-coefficients--corrected-and-normalised'],
    'average-node-degree--default': [],
    'graph-diameter--default': [],
    'layout-position-circular--default': [],
    'layout-position-spring--default': [],
    'layout-position-spectral--default': [],
    'layout-position-random--default': [],
    'layout-position-shell--default': [],
    'edge-betweenness-centrality--default': [],
}


def parse_coria_metric_id(_metric_id):
    parts = _metric_id.split('--', 2)
    parts[1] = parts[0] + "--" + parts[1]
    if parts[0] not in CORIA_METRIC_TREE or parts[1] not in CORIA_METRIC_TREE[parts[0]] or not CORIA_METRIC_TREE[parts[0]][parts[1]]:
        sys.exit('Unknown metric: ' + _metric_id)
    return {
        'metric_algorithm': parts[0],
        'metric_variant': parts[1],
        'architecture': 'gpu' if (len(parts) > 2 and parts[2] == 'python3-c-cuda--rapids-cugraph') or (USE_CUDA and 'gpu' in CORIA_METRIC_TREE[parts[0]][parts[1]]) else 'cpu'
    }


def get_spl_dependent_metrics(execution_queue):
    """
Collects all metrics in the computation queue that depend on shortest-path-lengths into a list.
    """
    _spl_dependent_metrics = []
    for _metric_id in execution_queue:
        _metric_keys = parse_coria_metric_id(_metric_id)
        if _metric_keys['metric_variant'] in CORIA_METRIC_DEPENDENCIES and \
                'shortest-path-lengths--default' in CORIA_METRIC_DEPENDENCIES[_metric_keys['metric_variant']]:
            _spl_dependent_metrics.append(_metric_keys['metric_variant'])
    return _spl_dependent_metrics


def collect_dependencies_recursive(metric_variant, execution_queue=[]):
    for metric_variant_dep in CORIA_METRIC_DEPENDENCIES[metric_variant]:
        collect_dependencies_recursive(metric_variant_dep, execution_queue)
    if metric_variant not in execution_queue:
        execution_queue.append(metric_variant)

    return execution_queue
