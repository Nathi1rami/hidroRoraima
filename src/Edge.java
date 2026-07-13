import java.awt.Color;

/**
 * MODEL LAYER - Edge.java
 * Represents a pipe (tuberia) connecting two nodes in the water distribution network.
 * Each edge has a maximum capacity (pressure limit) and tracks current flow.
 *
 * @author HidroRoraima
 * @version 1.0
 */
public class Edge {

    private final int id;
    private final Node from;
    private final Node to;
    private double capacity;    // Maximum flow capacity (L/s)
    private double flow;        // Current flow through the pipe
    private boolean highlighted;
    private Color highlightColor;
    private boolean isBottleneck;
    private boolean active;     // Active status for failure simulation

    public Edge(int id, Node from, Node to, double capacity) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.capacity = Math.max(0, capacity);
        this.flow = 0;
        this.highlighted = false;
        this.highlightColor = null;
        this.isBottleneck = false;
        this.active = true;
    }

    // ─── Getters ───────────────────────────────────
    public int getId() { return id; }
    public Node getFrom() { return from; }
    public Node getTo() { return to; }
    public double getCapacity() { return capacity; }
    public double getFlow() { return flow; }
    public boolean isHighlighted() { return highlighted; }
    public Color getHighlightColor() { return highlightColor; }
    public boolean isBottleneck() { return isBottleneck; }
    public boolean isActive() { return active; }

    /**
     * Returns the residual capacity (capacity - flow).
     */
    public double getResidualCapacity() {
        return capacity - flow;
    }

    /**
     * Returns the utilization ratio (0.0 to 1.0).
     */
    public double getUtilization() {
        return capacity > 0 ? flow / capacity : 0;
    }

    /**
     * Returns true if this edge is at full capacity (saturated).
     */
    public boolean isSaturated() {
        return Math.abs(capacity - flow) < 0.001;
    }

    /**
     * Returns a display label showing flow/capacity.
     */
    public String getFlowLabel() {
        return String.format("%.0f / %.0f", flow, capacity);
    }

    // ─── Setters ───────────────────────────────────
    public void setCapacity(double capacity) { this.capacity = Math.max(0, capacity); }
    public void setFlow(double flow) { this.flow = Math.max(0, Math.min(this.capacity, flow)); }

    public void addFlow(double additionalFlow) {
        this.flow = Math.max(0, Math.min(this.capacity, this.flow + additionalFlow));
    }

    public void setHighlighted(boolean highlighted, Color color) {
        this.highlighted = highlighted;
        this.highlightColor = color;
    }

    public void clearHighlight() {
        this.highlighted = false;
        this.highlightColor = null;
        this.isBottleneck = false;
    }

    public void setBottleneck(boolean bottleneck) { this.isBottleneck = bottleneck; }
    public void resetFlow() { this.flow = 0; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return from.getName() + " -> " + to.getName() +
               " [" + (int) flow + "/" + (int) capacity + " L/s]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id == ((Edge) o).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
