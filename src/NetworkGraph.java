import java.util.*;

/**
 * MODEL LAYER - NetworkGraph.java
 * Manages the water distribution network graph structure.
 * Provides CRUD operations for nodes and edges, network analysis,
 * and a preset network for testing.
 *
 * @author HidroRoraima
 * @version 1.0
 */
public class NetworkGraph {

    private final List<Node> nodes;
    private final List<Edge> edges;
    private int nextNodeId;
    private int nextEdgeId;

    public NetworkGraph() {
        this.nodes = new ArrayList<Node>();
        this.edges = new ArrayList<Edge>();
        this.nextNodeId = 1;
        this.nextEdgeId = 1;
    }

    // ═══════════════════════════════════════════════
    // NODE OPERATIONS
    // ═══════════════════════════════════════════════

    /**
     * Adds a new node to the network.
     */
    public Node addNode(String name, Node.NodeType type, double x, double y, double capacity) {
        Node node = new Node(nextNodeId++, name, type, x, y, capacity);
        nodes.add(node);
        return node;
    }

    /**
     * Adds a new intermediate node (ESTACION) with no capacity.
     */
    public Node addNode(String name, Node.NodeType type, double x, double y) {
        return addNode(name, type, x, y, 0);
    }

    /**
     * Removes a node and all its connected edges.
     */
    public void removeNode(Node node) {
        Iterator<Edge> it = edges.iterator();
        while (it.hasNext()) {
            Edge e = it.next();
            if (e.getFrom().equals(node) || e.getTo().equals(node)) {
                it.remove();
            }
        }
        nodes.remove(node);
    }

    /**
     * Finds a node by its unique ID.
     */
    public Node getNodeById(int id) {
        for (Node n : nodes) {
            if (n.getId() == id) return n;
        }
        return null;
    }

    /**
     * Finds a node at the given world coordinates (within tolerance).
     * Used for click detection on the canvas.
     */
    public Node getNodeAt(double x, double y, double tolerance) {
        for (Node n : nodes) {
            double dx = n.getX() - x;
            double dy = n.getY() - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (n.getType() == Node.NodeType.ESTACION) {
                if (dist <= ThemeManager.ESTACION_RADIUS + tolerance) return n;
            } else {
                double hw = ThemeManager.NODE_WIDTH / 2.0 + tolerance;
                double hh = ThemeManager.NODE_HEIGHT / 2.0 + tolerance;
                if (Math.abs(dx) <= hw && Math.abs(dy) <= hh) return n;
            }
        }
        return null;
    }

    public List<Node> getNodes() { return Collections.unmodifiableList(nodes); }

    /**
     * Returns all nodes of a specific type.
     */
    public List<Node> getNodesByType(Node.NodeType type) {
        List<Node> result = new ArrayList<Node>();
        for (Node n : nodes) {
            if (n.getType() == type) result.add(n);
        }
        return result;
    }

    // ═══════════════════════════════════════════════
    // EDGE OPERATIONS
    // ═══════════════════════════════════════════════

    /**
     * Adds a new edge (pipe) between two nodes.
     * Returns null if a duplicate edge already exists.
     */
    public Edge addEdge(Node from, Node to, double capacity) {
        for (Edge e : edges) {
            if (e.getFrom().equals(from) && e.getTo().equals(to)) return null;
        }
        Edge edge = new Edge(nextEdgeId++, from, to, capacity);
        edges.add(edge);
        return edge;
    }

    /**
     * Removes an edge from the network.
     */
    public void removeEdge(Edge edge) {
        edges.remove(edge);
    }

    /**
     * Finds an edge by its unique ID.
     */
    public Edge getEdgeById(int id) {
        for (Edge e : edges) {
            if (e.getId() == id) return e;
        }
        return null;
    }

    /**
     * Finds an edge between two specific nodes.
     */
    public Edge getEdge(Node from, Node to) {
        for (Edge e : edges) {
            if (e.getFrom().equals(from) && e.getTo().equals(to)) return e;
        }
        return null;
    }

    public List<Edge> getEdges() { return Collections.unmodifiableList(edges); }

    /**
     * Returns all outgoing edges from a node.
     */
    public List<Edge> getOutgoingEdges(Node node) {
        List<Edge> result = new ArrayList<Edge>();
        for (Edge e : edges) {
            if (e.getFrom().equals(node)) result.add(e);
        }
        return result;
    }

    /**
     * Returns all incoming edges to a node.
     */
    public List<Edge> getIncomingEdges(Node node) {
        List<Edge> result = new ArrayList<Edge>();
        for (Edge e : edges) {
            if (e.getTo().equals(node)) result.add(e);
        }
        return result;
    }

    /**
     * Finds an edge near a given point (for click detection on edges).
     */
    public Edge getEdgeNear(double px, double py, double tolerance) {
        for (Edge e : edges) {
            double x1 = e.getFrom().getX(), y1 = e.getFrom().getY();
            double x2 = e.getTo().getX(), y2 = e.getTo().getY();
            double dist = pointToSegmentDistance(px, py, x1, y1, x2, y2);
            if (dist <= tolerance) return e;
        }
        return null;
    }

    /**
     * Calculates the minimum distance from a point to a line segment.
     */
    private double pointToSegmentDistance(double px, double py,
                                          double x1, double y1, double x2, double y2) {
        double dx = x2 - x1, dy = y2 - y1;
        double lenSq = dx * dx + dy * dy;
        if (lenSq == 0) return Math.sqrt((px - x1) * (px - x1) + (py - y1) * (py - y1));
        double t = Math.max(0, Math.min(1, ((px - x1) * dx + (py - y1) * dy) / lenSq));
        double projX = x1 + t * dx, projY = y1 + t * dy;
        return Math.sqrt((px - projX) * (px - projX) + (py - projY) * (py - projY));
    }

    // ═══════════════════════════════════════════════
    // NETWORK ANALYSIS
    // ═══════════════════════════════════════════════

    public int getNodeCount() { return nodes.size(); }
    public int getEdgeCount() { return edges.size(); }

    /**
     * Returns the total supply capacity of all reservoirs.
     */
    public double getTotalSupply() {
        double total = 0;
        for (Node n : nodes) {
            if (n.isSource()) total += n.getCapacity();
        }
        return total;
    }

    /**
     * Returns the total demand of all neighborhoods.
     */
    public double getTotalDemand() {
        double total = 0;
        for (Node n : nodes) {
            if (n.isSink()) total += n.getCapacity();
        }
        return total;
    }

    /**
     * Resets all flow values in edges and nodes.
     */
    public void resetFlows() {
        for (Edge e : edges) e.resetFlow();
        for (Node n : nodes) n.setCurrentFlow(0);
    }

    /**
     * Clears all highlights from nodes and edges.
     */
    public void clearHighlights() {
        for (Node n : nodes) n.clearHighlight();
        for (Edge e : edges) e.clearHighlight();
    }

    private boolean eventRain = false;
    private boolean eventDrought = false;
    private boolean eventDemandPeak = false;

    private double eventRainFactor = 1.5;         // Default: +50%
    private double eventDroughtFactor = 0.5;      // Default: -50%
    private double eventDemandPeakFactor = 1.5;   // Default: +50%

    /**
     * Clears the entire graph.
     */
    public void clear() {
        nodes.clear();
        edges.clear();
        nextNodeId = 1;
        nextEdgeId = 1;
        eventRain = false;
        eventDrought = false;
        eventDemandPeak = false;
        eventRainFactor = 1.5;
        eventDroughtFactor = 0.5;
        eventDemandPeakFactor = 1.5;
    }

    public boolean isEventRainActive() { return eventRain; }
    public void setEventRainActive(boolean active) { this.eventRain = active; }

    public boolean isEventDroughtActive() { return eventDrought; }
    public void setEventDroughtActive(boolean active) { this.eventDrought = active; }

    public boolean isEventDemandPeakActive() { return eventDemandPeak; }
    public void setEventDemandPeakActive(boolean active) { this.eventDemandPeak = active; }

    public double getEventRainFactor() { return eventRainFactor; }
    public void setEventRainFactor(double factor) { this.eventRainFactor = factor; }

    public double getEventDroughtFactor() { return eventDroughtFactor; }
    public void setEventDroughtFactor(double factor) { this.eventDroughtFactor = factor; }

    public double getEventDemandPeakFactor() { return eventDemandPeakFactor; }
    public void setEventDemandPeakFactor(double factor) { this.eventDemandPeakFactor = factor; }

    /**
     * Validates that the network has at least one source, one sink, and edges.
     */
    public boolean isValid() {
        boolean hasSource = false, hasSink = false;
        for (Node n : nodes) {
            if (n.isSource()) hasSource = true;
            if (n.isSink()) hasSink = true;
        }
        return hasSource && hasSink && !edges.isEmpty();
    }

    /**
     * Updates node flow values based on current edge flows.
     */
    public void updateNodeFlows() {
        for (Node n : nodes) {
            double flow = 0;
            if (n.isSource()) {
                for (Edge e : getOutgoingEdges(n)) flow += e.getFlow();
            } else if (n.isSink()) {
                for (Edge e : getIncomingEdges(n)) flow += e.getFlow();
            }
            n.setCurrentFlow(flow);
        }
    }

    // ═══════════════════════════════════════════════
    // PRESET NETWORK
    // ═══════════════════════════════════════════════

    /**
     * Loads a preset network: "Red Hidrica de Roraima"
     * Includes 3 reservoirs, 3 pumping stations, and 5 neighborhoods
     * with 13 pipe connections of varying capacities.
     */
    public void loadPresetRoraima() {
        clear();

        // ─── Embalses (Sources) ────────────────────
        Node guri = addNode("Embalse Guri", Node.NodeType.EMBALSE, 150, 80, 50);
        Node caroni = addNode("Embalse Caroni", Node.NodeType.EMBALSE, 450, 60, 35);
        Node paragua = addNode("Embalse Paragua", Node.NodeType.EMBALSE, 750, 80, 25);

        // ─── Estaciones (Intermediate) ─────────────
        Node estNorte = addNode("Est. Bombeo Norte", Node.NodeType.ESTACION, 200, 280);
        Node estCentral = addNode("Est. Bombeo Central", Node.NodeType.ESTACION, 500, 260);
        Node estSur = addNode("Est. Bombeo Sur", Node.NodeType.ESTACION, 750, 300);

        // ─── Barrios (Sinks) ───────────────────────
        Node santaElena = addNode("Santa Elena", Node.NodeType.BARRIO, 100, 480, 20);
        Node boaVista = addNode("Boa Vista", Node.NodeType.BARRIO, 300, 500, 25);
        Node ciudadBolivar = addNode("Ciudad Bolivar", Node.NodeType.BARRIO, 500, 480, 30);
        Node puertoOrdaz = addNode("Puerto Ordaz", Node.NodeType.BARRIO, 680, 510, 20);
        Node tumeremo = addNode("Tumeremo", Node.NodeType.BARRIO, 850, 480, 15);

        // ─── Tuberias (Pipes) ──────────────────────
        addEdge(guri, estNorte, 30);
        addEdge(guri, estCentral, 25);
        addEdge(caroni, estNorte, 20);
        addEdge(caroni, estCentral, 30);
        addEdge(paragua, estCentral, 15);
        addEdge(paragua, estSur, 25);
        addEdge(estNorte, santaElena, 18);
        addEdge(estNorte, boaVista, 22);
        addEdge(estCentral, boaVista, 15);
        addEdge(estCentral, ciudadBolivar, 30);
        addEdge(estCentral, puertoOrdaz, 12);
        addEdge(estSur, puertoOrdaz, 20);
        addEdge(estSur, tumeremo, 18);
    }
}
