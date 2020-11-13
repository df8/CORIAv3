package com.coria.v3.parser;

import com.coria.v3.dbmodel.EdgeEntity;
import com.coria.v3.dbmodel.NodeEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by David Fradin, 2020
 */
public class ImportedStreamElement {
    public ImportedStreamElement() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public final List<NodeEntity> nodes;
    public final List<EdgeEntity> edges;
}
