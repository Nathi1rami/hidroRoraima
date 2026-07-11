import java.util.*;

/**
 * MODEL LAYER - FlowResult.java
 * Contains the complete results of a Ford-Fulkerson maximum flow computation.
 * Stores the max flow value, per-edge flows, step-by-step data, and demand satisfaction metrics.
 *
 * @author HidroRoraima
 * @version 1.0
 */
public class FlowResult {

    private final double maxFlow;
    private final Map<Integer, Double> edgeFlows;     // Edge ID -> final flow
    private final List<SimulationStep> steps;
    private final Map<Integer, Double> nodeFlows;     // Node ID -> incoming/outgoing flow
    private final boolean allDemandsSatisfied;
    private final double totalDemand;
    private final double totalSupply;
    private final long computationTimeMs;

    public FlowResult(double maxFlow, Map<Integer, Double> edgeFlows,
                      List<SimulationStep> steps, Map<Integer, Double> nodeFlows,
                      boolean allDemandsSatisfied, double totalDemand, double totalSupply,
                      long computationTimeMs) {
        this.maxFlow = maxFlow;
        this.edgeFlows = Collections.unmodifiableMap(new HashMap<Integer, Double>(edgeFlows));
        this.steps = Collections.unmodifiableList(new ArrayList<SimulationStep>(steps));
        this.nodeFlows = Collections.unmodifiableMap(new HashMap<Integer, Double>(nodeFlows));
        this.allDemandsSatisfied = allDemandsSatisfied;
        this.totalDemand = totalDemand;
        this.totalSupply = totalSupply;
        this.computationTimeMs = computationTimeMs;
    }

    // ─── Getters ───────────────────────────────────
    public double getMaxFlow() { return maxFlow; }
    public Map<Integer, Double> getEdgeFlows() { return edgeFlows; }
    public List<SimulationStep> getSteps() { return steps; }
    public Map<Integer, Double> getNodeFlows() { return nodeFlows; }
    public boolean isAllDemandsSatisfied() { return allDemandsSatisfied; }
    public double getTotalDemand() { return totalDemand; }
    public double getTotalSupply() { return totalSupply; }
    public long getComputationTimeMs() { return computationTimeMs; }

    /**
     * Returns overall demand satisfaction as a percentage (0-100).
     */
    public double getDemandSatisfactionPercentage() {
        return totalDemand > 0 ? Math.min(100.0, (maxFlow / totalDemand) * 100.0) : 100.0;
    }

    public int getTotalSteps() { return steps.size(); }

    @Override
    public String toString() {
        return String.format("Flujo Maximo: %.0f L/s | Demanda: %.0f L/s | Satisfecho: %.1f%% | Pasos: %d",
                maxFlow, totalDemand, getDemandSatisfactionPercentage(), steps.size());
    }
}
