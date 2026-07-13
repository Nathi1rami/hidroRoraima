import java.util.*;

/**
 * MODEL LAYER - FordFulkersonSolver.java
 * Implements the Edmonds-Karp algorithm (BFS-based Ford-Fulkerson) for computing
 * maximum flow in the water distribution network.
 *
 * Uses a Super-Source / Super-Sink approach:
 * - A virtual super-source is connected to all EMBALSE nodes (capacity = embalse supply)
 * - A virtual super-sink is connected from all BARRIO nodes (capacity = barrio demand)
 * - Maximum flow is computed from super-source to super-sink
 *
 * Records step-by-step execution data for visualization.
 *
 * @author HidroRoraima
 * @version 1.0
 */
public class FordFulkersonSolver {

    private static final int SUPER_SOURCE_ID = -1;
    private static final int SUPER_SINK_ID = -2;

    /**
     * Solves the maximum flow problem on the given network graph
     * using the Edmonds-Karp (BFS-based Ford-Fulkerson) algorithm.
     *
     * @param graph The water distribution network
     * @return FlowResult containing max flow, edge flows, and step-by-step data
     */
    public FlowResult solve(NetworkGraph graph) {
        long startTime = System.currentTimeMillis();

        // Collect nodes and edges
        List<Node> allNodes = graph.getNodes();
        List<Edge> allEdges = graph.getEdges();
        List<Node> sources = graph.getNodesByType(Node.NodeType.EMBALSE);
        List<Node> sinks = graph.getNodesByType(Node.NodeType.BARRIO);

        if (sources.isEmpty() || sinks.isEmpty() || allEdges.isEmpty()) {
            return createEmptyResult(graph, startTime);
        }

        // ─── Build index mappings ──────────────────
        // Index 0 = super-source, index n-1 = super-sink
        int n = allNodes.size() + 2;
        Map<Integer, Integer> nodeIdToIndex = new HashMap<Integer, Integer>();
        Map<Integer, Integer> indexToNodeId = new HashMap<Integer, Integer>();

        int superSourceIdx = 0;
        int superSinkIdx = n - 1;
        nodeIdToIndex.put(SUPER_SOURCE_ID, superSourceIdx);
        indexToNodeId.put(superSourceIdx, SUPER_SOURCE_ID);
        nodeIdToIndex.put(SUPER_SINK_ID, superSinkIdx);
        indexToNodeId.put(superSinkIdx, SUPER_SINK_ID);

        int idx = 1;
        for (Node node : allNodes) {
            nodeIdToIndex.put(node.getId(), idx);
            indexToNodeId.put(idx, node.getId());
            idx++;
        }

        // ─── Build capacity and flow matrices ──────
        double[][] capacity = new double[n][n];
        double[][] flow = new double[n][n];

        // Super-source to all embalses
        for (Node source : sources) {
            int srcIdx = nodeIdToIndex.get(source.getId());
            double cap = source.getCapacity();
            if (graph.isEventRainActive()) {
                cap *= graph.getEventRainFactor();
            } else if (graph.isEventDroughtActive()) {
                cap *= graph.getEventDroughtFactor();
            }
            capacity[superSourceIdx][srcIdx] = source.isActive() ? cap : 0;
        }

        // All barrios to super-sink
        for (Node sink : sinks) {
            int sinkIdx = nodeIdToIndex.get(sink.getId());
            double dem = sink.getCapacity();
            if (graph.isEventDemandPeakActive()) {
                dem *= graph.getEventDemandPeakFactor();
            }
            capacity[sinkIdx][superSinkIdx] = sink.isActive() ? dem : 0;
        }

        // Network edges
        Map<String, Edge> edgeLookup = new HashMap<String, Edge>();
        for (Edge edge : allEdges) {
            int fromIdx = nodeIdToIndex.get(edge.getFrom().getId());
            int toIdx = nodeIdToIndex.get(edge.getTo().getId());
            if (edge.isActive() && edge.getFrom().isActive() && edge.getTo().isActive()) {
                capacity[fromIdx][toIdx] = edge.getCapacity();
            } else {
                capacity[fromIdx][toIdx] = 0;
            }
            edgeLookup.put(fromIdx + "," + toIdx, edge);
        }

        // ─── Edmonds-Karp BFS loop ─────────────────
        double maxFlow = 0;
        List<SimulationStep> steps = new ArrayList<SimulationStep>();
        int stepNumber = 0;

        while (true) {
            // BFS to find shortest augmenting path
            int[] parent = new int[n];
            Arrays.fill(parent, -1);
            parent[superSourceIdx] = superSourceIdx;

            Queue<Integer> queue = new LinkedList<Integer>();
            queue.add(superSourceIdx);

            boolean found = false;
            while (!queue.isEmpty() && !found) {
                int u = queue.poll();
                for (int v = 0; v < n; v++) {
                    if (parent[v] == -1 && capacity[u][v] - flow[u][v] > 0.001) {
                        parent[v] = u;
                        if (v == superSinkIdx) {
                            found = true;
                            break;
                        }
                        queue.add(v);
                    }
                }
            }

            if (!found) break; // No more augmenting paths

            // ─── Find bottleneck ───────────────────
            double pathFlow = Double.MAX_VALUE;
            int bottleneckFrom = -1, bottleneckTo = -1;
            for (int v = superSinkIdx; v != superSourceIdx; v = parent[v]) {
                int u = parent[v];
                double residual = capacity[u][v] - flow[u][v];
                if (residual < pathFlow) {
                    pathFlow = residual;
                    bottleneckFrom = u;
                    bottleneckTo = v;
                }
            }

            // ─── Update flow along the path ────────
            for (int v = superSinkIdx; v != superSourceIdx; v = parent[v]) {
                int u = parent[v];
                flow[u][v] += pathFlow;
                flow[v][u] -= pathFlow; // Reverse edge for residual graph
            }

            maxFlow += pathFlow;
            stepNumber++;

            // ─── Record step ───────────────────────
            // Build augmenting path (excluding super-source/sink)
            List<Integer> augPath = new ArrayList<Integer>();
            for (int v = superSinkIdx; v != superSourceIdx; v = parent[v]) {
                int nodeId = indexToNodeId.get(v);
                if (nodeId != SUPER_SOURCE_ID && nodeId != SUPER_SINK_ID) {
                    augPath.add(0, nodeId);
                }
            }

            // Snapshot of edge flows
            Map<Integer, Double> edgeFlowSnapshot = new HashMap<Integer, Double>();
            for (Edge e : allEdges) {
                int fi = nodeIdToIndex.get(e.getFrom().getId());
                int ti = nodeIdToIndex.get(e.getTo().getId());
                edgeFlowSnapshot.put(e.getId(), Math.max(0, flow[fi][ti]));
            }

            // Find bottleneck edge ID
            int bottleneckEdgeId = -1;
            String bottleneckKey = bottleneckFrom + "," + bottleneckTo;
            if (edgeLookup.containsKey(bottleneckKey)) {
                bottleneckEdgeId = edgeLookup.get(bottleneckKey).getId();
            }

            // Build description
            StringBuilder desc = new StringBuilder();
            desc.append("Camino: ");
            for (int i = 0; i < augPath.size(); i++) {
                Node pathNode = graph.getNodeById(augPath.get(i));
                desc.append(pathNode != null ? pathNode.getName() : "?");
                if (i < augPath.size() - 1) desc.append(" -> ");
            }
            desc.append(String.format(" | Flujo: %.0f L/s", pathFlow));

            SimulationStep step = new SimulationStep(
                    stepNumber, augPath, pathFlow, maxFlow,
                    edgeFlowSnapshot, bottleneckEdgeId, desc.toString()
            );
            steps.add(step);
        }

        // ─── Apply final flows to graph ────────────
        for (Edge e : allEdges) {
            int fi = nodeIdToIndex.get(e.getFrom().getId());
            int ti = nodeIdToIndex.get(e.getTo().getId());
            e.setFlow(Math.max(0, flow[fi][ti]));
        }
        graph.updateNodeFlows();

        // ─── Calculate demand satisfaction ──────────
        double totalDemand = graph.getTotalDemand();
        double totalSupply = graph.getTotalSupply();
        boolean allSatisfied = true;
        Map<Integer, Double> nodeFlows = new HashMap<Integer, Double>();

        for (Node node : allNodes) {
            if (node.isSink()) {
                double incoming = 0;
                for (Edge e : graph.getIncomingEdges(node)) {
                    incoming += e.getFlow();
                }
                nodeFlows.put(node.getId(), incoming);
                if (incoming < node.getCapacity() - 0.001) {
                    allSatisfied = false;
                }
            } else if (node.isSource()) {
                double outgoing = 0;
                for (Edge e : graph.getOutgoingEdges(node)) {
                    outgoing += e.getFlow();
                }
                nodeFlows.put(node.getId(), outgoing);
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;

        return new FlowResult(maxFlow, buildEdgeFlowMap(allEdges), steps,
                nodeFlows, allSatisfied, totalDemand, totalSupply, elapsed);
    }

    /**
     * Builds a map of edge ID to current flow value.
     */
    private Map<Integer, Double> buildEdgeFlowMap(List<Edge> edges) {
        Map<Integer, Double> map = new HashMap<Integer, Double>();
        for (Edge e : edges) {
            map.put(e.getId(), e.getFlow());
        }
        return map;
    }

    /**
     * Creates an empty result for invalid networks.
     */
    private FlowResult createEmptyResult(NetworkGraph graph, long startTime) {
        return new FlowResult(0, new HashMap<Integer, Double>(),
                new ArrayList<SimulationStep>(), new HashMap<Integer, Double>(),
                false, graph.getTotalDemand(), graph.getTotalSupply(),
                System.currentTimeMillis() - startTime);
    }
}
