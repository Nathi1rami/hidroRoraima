import javax.swing.*;
import java.util.*;

/**
 * CONTROLLER LAYER - NetworkController.java
 * Coordinates between the Model (NetworkGraph, FordFulkersonSolver) and
 * the View (MainFrame, NetworkCanvas, ControlPanel).
 *
 * Handles all user actions, runs simulations, and updates the UI accordingly.
 * This is the central mediator in the MVC architecture.
 *
 * @author HidroRoraima
 * @version 1.0
 */
public class NetworkController {

    private final NetworkGraph model;
    private final MainFrame view;
    private final NetworkCanvas canvas;
    private final ControlPanel controlPanel;
    private final FordFulkersonSolver solver;

    private FlowResult lastResult;
    private int currentStep = -1;
    private boolean inStepMode = false;

    public NetworkController(NetworkGraph model, MainFrame view) {
        this.model = model;
        this.view = view;
        this.canvas = view.getCanvas();
        this.controlPanel = view.getControlPanel();
        this.solver = new FordFulkersonSolver();

        // Wire view components to this controller
        canvas.setGraph(model);
        canvas.setController(this);
        controlPanel.setController(this);

        updateStats();
    }

    // ═══════════════════════════════════════════════
    // NODE OPERATIONS
    // ═══════════════════════════════════════════════

    /**
     * Opens a dialog to add a new node interactively (placed at random position).
     */
    public void addNodeInteractive(Node.NodeType type) {
        NodeDialog dialog = new NodeDialog(view, type);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            double x = 100 + Math.random() * 600;
            double y = 100 + Math.random() * 400;
            model.addNode(dialog.getResultName(), dialog.getResultType(), x, y, dialog.getResultCapacity());
            refreshView("Nodo agregado: " + dialog.getResultName());
        }
    }

    /**
     * Opens a dialog to add a new node at a specific canvas position.
     */
    public void addNodeAt(Node.NodeType type, double x, double y) {
        NodeDialog dialog = new NodeDialog(view, type);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            model.addNode(dialog.getResultName(), dialog.getResultType(), x, y, dialog.getResultCapacity());
            refreshView("Nodo agregado: " + dialog.getResultName());
        }
    }

    /**
     * Opens a dialog to edit an existing node.
     */
    public void editNode(Node node) {
        NodeDialog dialog = new NodeDialog(view, node, node.getType());
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            node.setName(dialog.getResultName());
            node.setType(dialog.getResultType());
            node.setCapacity(dialog.getResultCapacity());
            refreshView("Nodo editado: " + node.getName());
        }
    }

    /**
     * Deletes a node after confirmation.
     */
    public void deleteNode(Node node) {
        int confirm = JOptionPane.showConfirmDialog(view,
                "Eliminar el nodo \"" + node.getName() + "\" y todas sus conexiones?",
                "Confirmar eliminacion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            model.removeNode(node);
            refreshView("Nodo eliminado: " + node.getName());
        }
    }

    /**
     * Selects a node (deselects all others).
     */
    public void selectNode(Node node) {
        for (Node n : model.getNodes()) n.setSelected(false);
        node.setSelected(true);
        canvas.repaint();
    }

    /**
     * Deselects all nodes.
     */
    public void deselectAll() {
        for (Node n : model.getNodes()) n.setSelected(false);
        canvas.repaint();
    }

    // ═══════════════════════════════════════════════
    // EDGE OPERATIONS
    // ═══════════════════════════════════════════════

    /**
     * Activates connection mode on the canvas.
     */
    public void startConnecting() {
        canvas.setConnectingMode(true);
        view.setStatus("Modo conexion: haga clic en el nodo origen y luego en el destino");
    }

    /**
     * Opens a dialog to add an edge between two nodes.
     */
    public void requestAddEdge(Node from, Node to) {
        java.util.List<Node> nodeList = new ArrayList<Node>(model.getNodes());
        EdgeDialog dialog = new EdgeDialog(view, nodeList, from, to);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Edge edge = model.addEdge(dialog.getResultFrom(), dialog.getResultTo(), dialog.getResultCapacity());
            if (edge != null) {
                refreshView("Tuberia agregada: " + edge);
            } else {
                JOptionPane.showMessageDialog(view, "Ya existe una conexion entre estos nodos.",
                        "Conexion duplicada", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * Opens a dialog to edit an existing edge.
     */
    public void editEdge(Edge edge) {
        java.util.List<Node> nodeList = new ArrayList<Node>(model.getNodes());
        EdgeDialog dialog = new EdgeDialog(view, nodeList, edge, null, null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            edge.setCapacity(dialog.getResultCapacity());
            refreshView("Tuberia editada: " + edge);
        }
    }

    /**
     * Deletes an edge after confirmation.
     */
    public void deleteEdge(Edge edge) {
        int confirm = JOptionPane.showConfirmDialog(view,
                "Eliminar la tuberia " + edge + "?",
                "Confirmar eliminacion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            model.removeEdge(edge);
            refreshView("Tuberia eliminada");
        }
    }

    // ═══════════════════════════════════════════════
    // SIMULATION
    // ═══════════════════════════════════════════════

    /**
     * Runs the Ford-Fulkerson algorithm and shows the final result with animation.
     */
    public void runSimulation() {
        if (!model.isValid()) {
            JOptionPane.showMessageDialog(view,
                    "La red necesita al menos un embalse, un barrio y una conexion.",
                    "Red incompleta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        model.resetFlows();
        model.clearHighlights();
        inStepMode = false;
        currentStep = -1;

        lastResult = solver.solve(model);

        canvas.rebuildParticles();
        controlPanel.updateStats(model, lastResult);
        controlPanel.updateStepControls(-1, 0);

        String status;
        if (lastResult.isAllDemandsSatisfied()) {
            status = String.format("Simulacion completa - Flujo maximo: %.0f L/s - Todas las demandas satisfechas",
                    lastResult.getMaxFlow());
        } else {
            status = String.format("Simulacion completa - Flujo maximo: %.0f L/s - Satisfaccion: %.1f%%",
                    lastResult.getMaxFlow(), lastResult.getDemandSatisfactionPercentage());
        }
        view.setStatus(status);
        canvas.repaint();
    }

    /**
     * Runs the Ford-Fulkerson algorithm in step-by-step mode.
     * Records all steps and replays them one at a time.
     */
    public void runStepByStep() {
        if (!model.isValid()) {
            JOptionPane.showMessageDialog(view,
                    "La red necesita al menos un embalse, un barrio y una conexion.",
                    "Red incompleta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Run algorithm to completion and record all steps
        model.resetFlows();
        model.clearHighlights();

        lastResult = solver.solve(model);

        if (lastResult.getTotalSteps() == 0) {
            JOptionPane.showMessageDialog(view,
                    "No se encontraron caminos aumentantes en la red.",
                    "Sin flujo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Reset flows to replay step by step
        model.resetFlows();
        inStepMode = true;
        currentStep = -1;

        controlPanel.updateStats(model, null);
        controlPanel.updateStepControls(-1, lastResult.getTotalSteps());

        view.setStatus("Modo paso a paso - " + lastResult.getTotalSteps() + " pasos - Presione 'Siguiente'");
        canvas.rebuildParticles();
        canvas.repaint();

        // Auto-advance to first step
        nextStep();
    }

    /**
     * Advances to the next step in step-by-step mode.
     */
    public void nextStep() {
        if (!inStepMode || lastResult == null) return;

        if (currentStep < lastResult.getTotalSteps() - 1) {
            currentStep++;
            applyStep(currentStep);
        }
    }

    /**
     * Goes back to the previous step in step-by-step mode.
     */
    public void prevStep() {
        if (!inStepMode || lastResult == null) return;

        if (currentStep > 0) {
            currentStep--;
            applyStep(currentStep);
        }
    }

    /**
     * Applies a specific step's state to the model and view.
     */
    private void applyStep(int stepIndex) {
        SimulationStep step = lastResult.getSteps().get(stepIndex);

        // Clear previous highlights
        model.clearHighlights();

        // Apply edge flows from this step's snapshot
        for (Edge e : model.getEdges()) {
            Double flowVal = step.getEdgeFlows().get(e.getId());
            e.setFlow(flowVal != null ? flowVal : 0);
        }
        model.updateNodeFlows();

        // Highlight augmenting path nodes (green)
        for (int nodeId : step.getAugmentingPath()) {
            Node node = model.getNodeById(nodeId);
            if (node != null) {
                node.setHighlighted(true, ThemeManager.ALGO_PATH);
            }
        }

        // Highlight augmenting path edges (green)
        java.util.List<Integer> path = step.getAugmentingPath();
        for (int i = 0; i < path.size() - 1; i++) {
            Node from = model.getNodeById(path.get(i));
            Node to = model.getNodeById(path.get(i + 1));
            if (from != null && to != null) {
                Edge edge = model.getEdge(from, to);
                if (edge != null) {
                    edge.setHighlighted(true, ThemeManager.ALGO_PATH);
                }
            }
        }

        // Highlight bottleneck edge (red)
        Edge bottleneck = model.getEdgeById(step.getBottleneckEdgeId());
        if (bottleneck != null) {
            bottleneck.setBottleneck(true);
            bottleneck.setHighlighted(true, ThemeManager.ALGO_BOTTLENECK);
        }

        // Update particles and controls
        canvas.rebuildParticles();
        controlPanel.updateStepControls(currentStep, lastResult.getTotalSteps());

        // Build current node flows for stats display
        Map<Integer, Double> currentNodeFlows = new HashMap<Integer, Double>();
        for (Node n : model.getNodes()) {
            currentNodeFlows.put(n.getId(), n.getCurrentFlow());
        }

        controlPanel.updateStats(model, new FlowResult(
                step.getTotalFlowSoFar(), step.getEdgeFlows(),
                lastResult.getSteps().subList(0, stepIndex + 1),
                currentNodeFlows, false,
                lastResult.getTotalDemand(), lastResult.getTotalSupply(),
                lastResult.getComputationTimeMs()
        ));

        view.setStatus(step.toString());
        canvas.repaint();
    }

    // ═══════════════════════════════════════════════
    // OTHER CONTROLS
    // ═══════════════════════════════════════════════

    /**
     * Resets all flows and highlights, keeping the network structure.
     */
    public void resetSimulation() {
        model.resetFlows();
        model.clearHighlights();
        lastResult = null;
        inStepMode = false;
        currentStep = -1;
        canvas.rebuildParticles();
        controlPanel.updateStats(model, null);
        controlPanel.updateStepControls(-1, 0);
        view.setStatus("Simulacion reiniciada");
        canvas.repaint();
    }

    /**
     * Loads the preset Roraima water network.
     */
    public void loadPreset() {
        int confirm = JOptionPane.showConfirmDialog(view,
                "Cargar la red predeterminada 'Red Hidrica de Roraima'?\nEsto reemplazara la red actual.",
                "Cargar Red Predeterminada", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            model.loadPresetRoraima();
            lastResult = null;
            inStepMode = false;
            currentStep = -1;
            canvas.setGraph(model);
            canvas.rebuildParticles();
            controlPanel.updateStats(model, null);
            controlPanel.updateStepControls(-1, 0);

            // Fit view after layout
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    canvas.fitToView();
                }
            });

            view.setStatus("Red 'Hidrica de Roraima' cargada - " +
                    model.getNodeCount() + " nodos, " + model.getEdgeCount() + " tuberias");
        }
    }

    /**
     * Clears the entire network after confirmation.
     */
    public void clearNetwork() {
        int confirm = JOptionPane.showConfirmDialog(view,
                "Limpiar toda la red? Esta accion no se puede deshacer.",
                "Confirmar limpieza", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            model.clear();
            lastResult = null;
            inStepMode = false;
            currentStep = -1;
            canvas.setGraph(model);
            canvas.rebuildParticles();
            controlPanel.updateStats(model, null);
            controlPanel.updateStepControls(-1, 0);
            view.setStatus("Red limpiada");
        }
    }

    /**
     * Fits the graph view to the canvas viewport.
     */
    public void fitView() {
        canvas.fitToView();
    }

    // ─── Private helpers ───────────────────────────

    private void refreshView(String statusMessage) {
        controlPanel.updateStats(model, lastResult);
        canvas.rebuildParticles();
        canvas.repaint();
        view.setStatus(statusMessage);
    }

    private void updateStats() {
        controlPanel.updateStats(model, lastResult);
    }
}
