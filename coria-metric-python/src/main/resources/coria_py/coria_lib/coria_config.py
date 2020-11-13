# Created by David Fradin, 2020

USE_CUDA = False
pd = None
cugraph = None
nx = None


def set_global_settings(use_cuda):
    global USE_CUDA
    USE_CUDA = use_cuda


def coria_imports():
    global USE_CUDA
    global pd
    if USE_CUDA:
        print("Using Architecture: GPU-accelerated")
        global cugraph        
        import cudf as pd
        import cugraph
        # cudf is a GPU DataFrame library that has been built as a drop-in replacement for the library pandas.
        # As in most cases we can call the same methods with the same parameters using either library, we will import either one using the same alias "pd".

    else:
        print("Using Architecture: CPU only")
        global nx
        import pandas as pd
        import networkx as nx
