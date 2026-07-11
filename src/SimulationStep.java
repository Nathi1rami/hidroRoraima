import java.util.*;

/**
 * MODEL LAYER - SimulationStep.java
 * Records a single step of the Ford-Fulkerson (Edmonds-Karp) algorithm execution.
 * Used for step-by-step visualization of the algorithm.
 *
 * @author HidroRoraima
 * @version 1.0
 */
public class SimulationStep {

    private final int stepNumber;
    private final List<Integer> augmentingPath; // Node IDs in order
    private final double pathFlow;              // Flow pushed in this step
    private final double totalFlowSoFar;        // Cumulative max flow after this step
    private final Map<Integer, Double> edgeFlows; // Edge ID -> flow snapshot
    private final int bottleneckEdgeId;         // ID of the bottleneck edge
    private final String description;           // Human-readable description

    public SimulationStep(int stepNumber, List<Integer> augmentingPath, double pathFlow,
                          double totalFlowSoFar, Map<Integer, Double> edgeFlows,
                          int bottleneckEdgeId, String description) {
        this.stepNumber = stepNumber;
        this.augmentingPath = Collections.unmodifiableList(new ArrayList<Integer>(augmentingPath));
        this.pathFlow = pathFlow;
        this.totalFlowSoFar = totalFlowSoFar;
        this.edgeFlows = Collections.unmodifiableMap(new HashMap<Integer, Double>(edgeFlows));
        this.bottleneckEdgeId = bottleneckEdgeId;
        this.description = description;
    }

    // ─── Getters ───────────────────────────────────
    public int getStepNumber() { return stepNumber; }
    public List<Integer> getAugmentingPath() { return augmentingPath; }
    public double getPathFlow() { return pathFlow; }
    public double getTotalFlowSoFar() { return totalFlowSoFar; }
    public Map<Integer, Double> getEdgeFlows() { return edgeFlows; }
    public int getBottleneckEdgeId() { return bottleneckEdgeId; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "Paso " + stepNumber + ": Flujo +" + (int) pathFlow +
               " L/s (Total: " + (int) totalFlowSoFar + " L/s) - " + description;
    }
}
