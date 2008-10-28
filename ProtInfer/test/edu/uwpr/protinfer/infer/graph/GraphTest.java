package edu.uwpr.protinfer.infer.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

public class GraphTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public final void testAddVertex() {
        Graph graph = new Graph();
        Vertex v1 = new TestVertex("vertex1");
        Vertex v2 = new TestVertex("vertex2");
        
        Vertex added = graph.addVertex(v1);
        assertTrue(v1 == added);
        
        added = graph.addVertex(v2);
        assertTrue(v2 == added);
        
        graph.addVertex(added);
        assertEquals(2, graph.getVertices().size());
        
        graph.addVertex(v1);
        graph.addVertex(v2);
        assertEquals(2, graph.getVertices().size());
        
        Vertex v3 = new TestVertex("vertex3");
        Vertex v3_a = graph.addVertex(v3);
        Vertex v3_b = graph.addVertex(new TestVertex("vertex3"));
        assertTrue(v3_a == v3_b);
    }

    public final void testAddEdge() {
        IGraph graph = new Graph();
        Vertex v1 = new TestVertex("vertex1");
        Vertex v2 = new TestVertex("vertex2");
        
        // add an edge v1 <--> v2
        graph.addEdge(v1, v2);
        assertEquals(2, graph.getVertices().size());
        assertEquals(1, graph.getEdgeCount());
        assertEquals(1, graph.getAdjacentVertices(v1).size());
        assertEquals(1, graph.getAdjacentVertices(v2).size());
        
        // add another edge v1 <--> v3
        Vertex v3 = new TestVertex("vertex3");
        graph.addEdge(v1, v3);
        assertEquals(3, graph.getVertices().size());
        assertEquals(2, graph.getEdgeCount());
        assertEquals(2, graph.getAdjacentVertices(v1).size());
        assertEquals(1, graph.getAdjacentVertices(v2).size());
        assertEquals(1, graph.getAdjacentVertices(v3).size());
        
        // try to add v1 <--> v2 edge again. It should not get added a second time
        graph.addEdge(v1, v2);
        assertEquals(3, graph.getVertices().size());
        assertEquals(2, graph.getEdgeCount());
        assertEquals(2, graph.getAdjacentVertices(v1).size());
        assertEquals(1, graph.getAdjacentVertices(v2).size());
        
        // add an edge with vertices reversed
        graph.addEdge(v2, v1);
        assertEquals(3, graph.getVertices().size());
        assertEquals(2, graph.getEdgeCount());
        assertEquals(2, graph.getAdjacentVertices(v1).size());
        assertEquals(1, graph.getAdjacentVertices(v2).size());
        
        // add an edge with a new Vertex object having the same label as v1. Edge should not get added
        graph.addEdge(v1, new TestVertex("vertex2"));
        assertEquals(3, graph.getVertices().size());
        assertEquals(2, graph.getEdgeCount());
        assertEquals(2, graph.getAdjacentVertices(v1).size());
        assertEquals(1, graph.getAdjacentVertices(v2).size());
    }
    
    public final void testList() {
        List<TestVertex> vertices = new ArrayList<TestVertex>();
        TestVertex v1 = new TestVertex("vertex1");
        TestVertex v2 = new TestVertex("vertex2");
        vertices.add(v1);
        vertices.add(v2);
        assertEquals(2, vertices.size());
        if (!vertices.contains(v1))
            vertices.add(v1); // will not get added again
        assertEquals(2, vertices.size());
        
        TestVertex v1_b = new TestVertex(v1.getLabel());
        assertFalse(v1.equals(v1_b));
        
        if (!vertices.contains(v1_b))
            vertices.add(v1_b); // this will get added since v1.equals(v1_b) == false
        assertEquals(3, vertices.size());
    }
    
    public final void testSet() {
        Set<TestVertex> vertices = new HashSet<TestVertex>();
        TestVertex v1 = new TestVertex("vertex1");
        TestVertex v2 = new TestVertex("vertex2");
        vertices.add(v1);
        vertices.add(v2);
        assertEquals(2, vertices.size());
        vertices.add(v1); // will not get added since the set already contains it.
        assertEquals(2, vertices.size());
        vertices.add(new TestVertex("vertex1")); // this will have a different hashValue than v1, so it will get added
        assertEquals(3, vertices.size());
    }
    
    public final void testContainsEdge() {
        Graph graph = new Graph();
        Vertex v1 = new TestVertex("vertex1");
        Vertex v2 = new TestVertex("vertex2");
        
        graph.addEdge(v1, v2);
        assertTrue(graph.containsEdge(v1, v2));
        assertTrue(graph.containsEdge(v2, v1));
        // try with new vertex ovjects having same labels as v1 and v2
        // this will return false
        assertFalse(graph.containsEdge(new TestVertex(v1.getLabel()), new TestVertex(v2.getLabel())));
    }

    public static final class TestVertex implements Vertex {
        private String label;
        private int componentIndex = 0;
        private boolean visited = false;
        
        public TestVertex(String label) {
            this.label = label;
        }
        @Override
        public int getComponentIndex() {
            return componentIndex;
        }
        @Override
        public String getLabel() {
            return label;
        }
        @Override
        public void setComponentIndex(int index) {
            this.componentIndex = index;
        }
        @Override
        public boolean isVisited() {
            return visited;
        }
        @Override
        public void setVisited(boolean visited) {
            this.visited = visited;
        }
        @Override
        public Vertex combineWith(Vertex v) {
            TestVertex newVertex = new TestVertex(this.getLabel()+"_"+v.getLabel());
            return newVertex;
        }
        @Override
        public Vertex combineWith(List<Vertex> vertices) {
            StringBuilder buf = new StringBuilder();
            buf.append(this.getLabel());
            for(Vertex v: vertices)
                buf.append(v.getLabel()+"_");
            if(vertices.size() > 0)
                buf.deleteCharAt(buf.length() - 1);
            return new TestVertex(buf.toString());
        }
    }
}
