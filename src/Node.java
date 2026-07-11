import java.awt.Color;

/**
 * MODEL LAYER - Node.java
 * Represents a node in the water distribution network.
 *
 * Three types of nodes:
 * - EMBALSE (Reservoir): Water source with maximum supply capacity
 * - ESTACION (Pumping Station): Intermediate pass-through node
 * - BARRIO (Neighborhood): Water sink with demand requirements
 *
 * @author HidroRoraima
 * @version 1.0
 */
public class Node {

    /**
     * Enum representing the type of node in the water network.
     */
    public enum NodeType {
        EMBALSE("Embalse", "Fuente de agua"),
        ESTACION("Estacion de Bombeo", "Nodo intermedio"),
        BARRIO("Barrio", "Destino del agua");

        private final String displayName;
        private final String description;

        NodeType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }

        @Override
        public String toString() { return displayName; }
    }

    private final int id;
    private String name;
    private NodeType type;
    private double x;
    private double y;
    private double capacity; // Supply for EMBALSE, demand for BARRIO, 0 for ESTACION
    private boolean selected;
    private boolean highlighted;
    private Color highlightColor;
    private double currentFlow; // Current incoming/outgoing flow (post-simulation)

    public Node(int id, String name, NodeType type, double x, double y, double capacity) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.x = x;
        this.y = y;
        this.capacity = Math.max(0, capacity);
        this.selected = false;
        this.highlighted = false;
        this.highlightColor = null;
        this.currentFlow = 0;
    }

    // ─── Getters ───────────────────────────────────
    public int getId() { return id; }
    public String getName() { return name; }
    public NodeType getType() { return type; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getCapacity() { return capacity; }
    public boolean isSelected() { return selected; }
    public boolean isHighlighted() { return highlighted; }
    public Color getHighlightColor() { return highlightColor; }
    public double getCurrentFlow() { return currentFlow; }

    public boolean isSource() { return type == NodeType.EMBALSE; }
    public boolean isSink() { return type == NodeType.BARRIO; }
    public boolean isIntermediate() { return type == NodeType.ESTACION; }

    /**
     * Returns demand satisfaction percentage (0-100) for BARRIO nodes.
     */
    public double getDemandSatisfaction() {
        if (type != NodeType.BARRIO || capacity <= 0) return 100.0;
        return Math.min(100.0, (currentFlow / capacity) * 100.0);
    }

    // ─── Setters ───────────────────────────────────
    public void setName(String name) { this.name = name; }
    public void setType(NodeType type) { this.type = type; }
    public void setPosition(double x, double y) { this.x = x; this.y = y; }
    public void setCapacity(double capacity) { this.capacity = Math.max(0, capacity); }
    public void setSelected(boolean selected) { this.selected = selected; }

    public void setHighlighted(boolean highlighted, Color color) {
        this.highlighted = highlighted;
        this.highlightColor = color;
    }

    public void clearHighlight() {
        this.highlighted = false;
        this.highlightColor = null;
    }

    public void setCurrentFlow(double flow) { this.currentFlow = flow; }

    /**
     * Returns the display label for this node.
     */
    public String getDisplayLabel() {
        switch (type) {
            case EMBALSE:
                return name + " [Cap: " + (int) capacity + " L/s]";
            case BARRIO:
                return name + " [Dem: " + (int) capacity + " L/s]";
            default:
                return name;
        }
    }

    @Override
    public String toString() {
        return type.getDisplayName() + ": " + name + " (ID=" + id + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id == ((Node) o).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
