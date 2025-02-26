package com.graphhopper.isochrone.algorithm;

import com.graphhopper.routing.RouterConfig;
import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.util.FiniteWeightFilter;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.index.LocationIndexTree;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.PointList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JTSTriangulatorTest extends IsochroneTestBase {
    private final GeometryFactory gf = new GeometryFactory();

    private final List<Coordinate> nodes = Arrays.asList(
            new Coordinate(0, 0),
            new Coordinate(.5, .5),
            new Coordinate(1, 0),
            new Coordinate(0, 1),
            new Coordinate(1, 1),
            new Coordinate(.5000001, .50000001),
            new Coordinate(.4999999, .50000001),
            new Coordinate(.99999977, .999999977)
    );

    private void addNode(int src, int dst) {
        graph.edge(src, dst);
        int weight = 10;
        if (dst >= 5) {
            weight = 100;
        }
        graph.getNodeAccess().setNode(src, nodes.get(src).y, nodes.get(src).x, 0);
        graph.getNodeAccess().setNode(dst, nodes.get(dst).y, nodes.get(dst).x, 0);
        GHUtility.setSpeed(10, true, false, accessEnc, speedEnc,
                graph.edge(src, dst).setDistance(weight)).setWayGeometry(PointList.from(gf.createLineString(new Coordinate[]{nodes.get(src), nodes.get(dst)})));
    }

    @BeforeEach
    public void setUp() {
        super.setUp();
        buildGraph();
    }

    private void buildGraph() {
//        Collections.shuffle(nodes);
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = 0; j < nodes.size(); j++) {
                if (i != j) {
                    addNode(i, j);
                }
            }
        }
    }

    @Test
    public void testTriangulate() {
        // try many times to find an invalid graph
        for (int i = 0; i < 1; i++) {
            graph = new BaseGraph.Builder(encodingManager).create();
            buildGraph();
            JTSTriangulator jtsTriangulator = new JTSTriangulator(new RouterConfig());
            LocationIndexTree locationIndex = new LocationIndexTree(graph, graph.getDirectory());
            locationIndex.prepareIndex();
            locationIndex.flush();

            Snap snap = locationIndex.findClosest(0.0, 0.0, new FiniteWeightFilter(createWeighting()));
            snap.setClosestNode(0);
            QueryGraph queryGraph = QueryGraph.create(graph, snap);
            ShortestPathTree tree = new ShortestPathTree(
                    queryGraph,
                    queryGraph.wrapWeighting(createWeighting()),
                    false,
                    TraversalMode.NODE_BASED
            );

            tree.setDistanceLimit(10000000);
            Triangulator.Result result = jtsTriangulator.triangulate(snap, queryGraph, tree, (ShortestPathTree.IsoLabel label) -> 10, 0);
            System.out.println(gf.createGeometryCollection(result.triangulation.getEdges().stream().map(e -> ((QuadEdgeAsReadableQuadEdge) e).toLineSegment().toGeometry(gf)).collect(Collectors.toList()).toArray(new Geometry[0])));
        }
    }
}
