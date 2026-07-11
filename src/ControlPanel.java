import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * VIEW LAYER - ControlPanel.java
 * Sidebar panel containing network controls, simulation buttons, and statistics.
 * Organized into sections: Node creation, Simulation, Statistics, and Tools.
 *
 * @author HidroRoraima
 * @version 1.0
 */
public class ControlPanel extends JPanel {

    private NetworkController controller;

    // ─── Control buttons ───────────────────────────
    private JButton btnAddEmbalse;
    private JButton btnAddEstacion;
    private JButton btnAddBarrio;
    private JButton btnAddConnection;
    private JButton btnRunSimulation;
    private JButton btnStepByStep;
    private JButton btnNextStep;
    private JButton btnPrevStep;
    private JButton btnReset;
    private JButton btnLoadPreset;
    private JButton btnClear;
    private JButton btnFitView;

    // ─── Statistics labels ─────────────────────────
    private JLabel lblMaxFlow;
    private JLabel lblTotalSupply;
    private JLabel lblTotalDemand;
    private JLabel lblSatisfaction;
    private JLabel lblStepInfo;
    private JLabel lblNodes;
    private JLabel lblEdges;
    private JLabel lblComputeTime;

    // ─── Step progress ─────────────────────────────
    private JProgressBar stepProgress;

    // ─── Per-barrio stats ──────────────────────────
    private JPanel barrioStatsPanel;

    public ControlPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ThemeManager.BG_SIDEBAR);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, ThemeManager.BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        setPreferredSize(new Dimension(280, 0));

        buildUI();
    }

    public void setController(NetworkController controller) {
        this.controller = controller;
        wireListeners();
    }

    // ═══════════════════════════════════════════════
    // UI CONSTRUCTION
    // ═══════════════════════════════════════════════

    private void buildUI() {
        // ─── Header ────────────────────────────────
        JLabel header = new JLabel("Panel de Control");
        header.setFont(ThemeManager.FONT_HEADER);
        header.setForeground(ThemeManager.BG_HEADER);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(header);
        add(Box.createVerticalStrut(16));

        // ─── Node Section ──────────────────────────
        add(createSectionLabel("Agregar Nodos"));
        add(Box.createVerticalStrut(6));

        btnAddEmbalse = ThemeManager.createStyledButton("+ Embalse", ThemeManager.EMBALSE_PRIMARY,
                new Color(56, 145, 60));
        btnAddEstacion = ThemeManager.createStyledButton("+ Estacion", ThemeManager.ESTACION_PRIMARY,
                new Color(101, 65, 188));
        btnAddBarrio = ThemeManager.createStyledButton("+ Barrio", ThemeManager.BARRIO_PRIMARY,
                new Color(250, 101, 20));

        addFullWidthButton(btnAddEmbalse);
        add(Box.createVerticalStrut(4));
        addFullWidthButton(btnAddEstacion);
        add(Box.createVerticalStrut(4));
        addFullWidthButton(btnAddBarrio);
        add(Box.createVerticalStrut(8));

        // ─── Connection Section ────────────────────
        btnAddConnection = ThemeManager.createStyledButton("Conectar Nodos", ThemeManager.BTN_PRIMARY,
                ThemeManager.BTN_PRIMARY_HOVER);
        addFullWidthButton(btnAddConnection);
        add(Box.createVerticalStrut(16));
        add(createSeparator());
        add(Box.createVerticalStrut(16));

        // ─── Simulation Section ────────────────────
        add(createSectionLabel("Simulacion"));
        add(Box.createVerticalStrut(6));

        btnRunSimulation = ThemeManager.createStyledButton("\u25B6 Simular Flujo", ThemeManager.BTN_SUCCESS,
                ThemeManager.BTN_SUCCESS_HOVER);
        btnStepByStep = ThemeManager.createStyledButton("\u25B6\u25B6 Paso a Paso", new Color(0, 121, 107),
                new Color(0, 151, 137));

        addFullWidthButton(btnRunSimulation);
        add(Box.createVerticalStrut(4));
        addFullWidthButton(btnStepByStep);
        add(Box.createVerticalStrut(8));

        // Step navigation
        JPanel stepPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        stepPanel.setOpaque(false);
        stepPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        stepPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        btnPrevStep = ThemeManager.createStyledButton("\u25C0 Anterior", ThemeManager.BTN_PRIMARY,
                ThemeManager.BTN_PRIMARY_HOVER);
        btnNextStep = ThemeManager.createStyledButton("Siguiente \u25B6", ThemeManager.BTN_PRIMARY,
                ThemeManager.BTN_PRIMARY_HOVER);
        btnPrevStep.setEnabled(false);
        btnNextStep.setEnabled(false);
        stepPanel.add(btnPrevStep);
        stepPanel.add(btnNextStep);
        add(stepPanel);
        add(Box.createVerticalStrut(4));

        lblStepInfo = new JLabel("Paso: --");
        lblStepInfo.setFont(ThemeManager.FONT_SMALL);
        lblStepInfo.setForeground(ThemeManager.TEXT_SECONDARY);
        lblStepInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lblStepInfo);

        stepProgress = new JProgressBar(0, 100);
        stepProgress.setValue(0);
        stepProgress.setStringPainted(true);
        stepProgress.setString("--");
        stepProgress.setFont(ThemeManager.FONT_SMALL);
        stepProgress.setAlignmentX(Component.LEFT_ALIGNMENT);
        stepProgress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        add(Box.createVerticalStrut(4));
        add(stepProgress);
        add(Box.createVerticalStrut(16));
        add(createSeparator());
        add(Box.createVerticalStrut(16));

        // ─── Statistics Section ────────────────────
        add(createSectionLabel("Estadisticas"));
        add(Box.createVerticalStrut(8));

        lblMaxFlow = createStatLabel("Flujo Maximo:", "--");
        lblTotalSupply = createStatLabel("Oferta Total:", "--");
        lblTotalDemand = createStatLabel("Demanda Total:", "--");
        lblSatisfaction = createStatLabel("Satisfaccion:", "--");
        lblNodes = createStatLabel("Nodos:", "--");
        lblEdges = createStatLabel("Tuberias:", "--");
        lblComputeTime = createStatLabel("Tiempo:", "--");

        add(Box.createVerticalStrut(12));

        // Per-barrio demand stats
        add(createSectionLabel("Demanda por Barrio"));
        add(Box.createVerticalStrut(4));
        barrioStatsPanel = new JPanel();
        barrioStatsPanel.setLayout(new BoxLayout(barrioStatsPanel, BoxLayout.Y_AXIS));
        barrioStatsPanel.setOpaque(false);
        barrioStatsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(barrioStatsPanel);

        add(Box.createVerticalGlue());
        add(Box.createVerticalStrut(16));
        add(createSeparator());
        add(Box.createVerticalStrut(12));

        // ─── Tools Section ─────────────────────────
        btnLoadPreset = ThemeManager.createStyledButton("Cargar Red Predeterminada", ThemeManager.BTN_PRIMARY,
                ThemeManager.BTN_PRIMARY_HOVER);
        btnFitView = ThemeManager.createStyledButton("Ajustar Vista", new Color(97, 97, 97),
                new Color(117, 117, 117));
        btnReset = ThemeManager.createStyledButton("\u21BA Reiniciar Flujo", ThemeManager.BTN_WARNING,
                new Color(255, 144, 20));
        btnClear = ThemeManager.createStyledButton("Limpiar Todo", ThemeManager.BTN_DANGER,
                ThemeManager.BTN_DANGER_HOVER);

        addFullWidthButton(btnLoadPreset);
        add(Box.createVerticalStrut(4));
        addFullWidthButton(btnFitView);
        add(Box.createVerticalStrut(4));
        addFullWidthButton(btnReset);
        add(Box.createVerticalStrut(4));
        addFullWidthButton(btnClear);
    }

    private void wireListeners() {
        if (controller == null) return;

        btnAddEmbalse.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { controller.addNodeInteractive(Node.NodeType.EMBALSE); }
        });
        btnAddEstacion.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { controller.addNodeInteractive(Node.NodeType.ESTACION); }
        });
        btnAddBarrio.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { controller.addNodeInteractive(Node.NodeType.BARRIO); }
        });
        btnAddConnection.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { controller.startConnecting(); }
        });
        btnRunSimulation.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { controller.runSimulation(); }
        });
        btnStepByStep.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { controller.runStepByStep(); }
        });
        btnNextStep.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { controller.nextStep(); }
        });
        btnPrevStep.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { controller.prevStep(); }
        });
        btnReset.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { controller.resetSimulation(); }
        });
        btnLoadPreset.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { controller.loadPreset(); }
        });
        btnClear.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { controller.clearNetwork(); }
        });
        btnFitView.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { controller.fitView(); }
        });
    }

    // ═══════════════════════════════════════════════
    // UPDATE METHODS
    // ═══════════════════════════════════════════════

    /**
     * Updates all statistics labels with current model and result data.
     */
    public void updateStats(NetworkGraph graph, FlowResult result) {
        if (graph != null) {
            lblNodes.setText(formatStat("Nodos:", String.valueOf(graph.getNodeCount())));
            lblEdges.setText(formatStat("Tuberias:", String.valueOf(graph.getEdgeCount())));
            lblTotalSupply.setText(formatStat("Oferta Total:", (int) graph.getTotalSupply() + " L/s"));
            lblTotalDemand.setText(formatStat("Demanda Total:", (int) graph.getTotalDemand() + " L/s"));
        }

        if (result != null) {
            lblMaxFlow.setText(formatStat("Flujo Maximo:", (int) result.getMaxFlow() + " L/s"));
            lblSatisfaction.setText(formatStat("Satisfaccion:",
                    String.format("%.1f%%", result.getDemandSatisfactionPercentage())));
            lblComputeTime.setText(formatStat("Tiempo:", result.getComputationTimeMs() + " ms"));

            double sat = result.getDemandSatisfactionPercentage();
            lblSatisfaction.setForeground(ThemeManager.getSatisfactionColor(sat));

            updateBarrioStats(graph, result);
        } else {
            lblMaxFlow.setText(formatStat("Flujo Maximo:", "--"));
            lblSatisfaction.setText(formatStat("Satisfaccion:", "--"));
            lblComputeTime.setText(formatStat("Tiempo:", "--"));
            lblSatisfaction.setForeground(ThemeManager.TEXT_PRIMARY);
            barrioStatsPanel.removeAll();
            barrioStatsPanel.revalidate();
        }
    }

    private void updateBarrioStats(NetworkGraph graph, FlowResult result) {
        barrioStatsPanel.removeAll();
        if (graph == null) return;

        for (Node node : graph.getNodesByType(Node.NodeType.BARRIO)) {
            Double flow = result.getNodeFlows().get(node.getId());
            double currentFlow = flow != null ? flow : 0;
            double demand = node.getCapacity();
            double satisfaction = demand > 0 ? (currentFlow / demand) * 100 : 100;

            JPanel barPanel = new JPanel(new BorderLayout(4, 0));
            barPanel.setOpaque(false);
            barPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            barPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

            JLabel nameLabel = new JLabel(node.getName());
            nameLabel.setFont(ThemeManager.FONT_SMALL);
            nameLabel.setForeground(ThemeManager.TEXT_PRIMARY);
            barPanel.add(nameLabel, BorderLayout.WEST);

            JProgressBar bar = new JProgressBar(0, 100);
            bar.setValue((int) Math.min(100, satisfaction));
            bar.setString(String.format("%.0f/%.0f", currentFlow, demand));
            bar.setStringPainted(true);
            bar.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            bar.setPreferredSize(new Dimension(100, 16));
            bar.setForeground(ThemeManager.getSatisfactionColor(satisfaction));
            barPanel.add(bar, BorderLayout.EAST);

            barrioStatsPanel.add(barPanel);
            barrioStatsPanel.add(Box.createVerticalStrut(2));
        }

        barrioStatsPanel.revalidate();
        barrioStatsPanel.repaint();
    }

    /**
     * Updates step navigation controls and progress bar.
     */
    public void updateStepControls(int currentStep, int totalSteps) {
        btnPrevStep.setEnabled(currentStep > 0);
        btnNextStep.setEnabled(currentStep < totalSteps - 1);

        if (totalSteps > 0) {
            lblStepInfo.setText("Paso: " + (currentStep + 1) + " / " + totalSteps);
            stepProgress.setMaximum(totalSteps);
            stepProgress.setValue(currentStep + 1);
            stepProgress.setString("Paso " + (currentStep + 1) + "/" + totalSteps);
        } else {
            lblStepInfo.setText("Paso: --");
            stepProgress.setValue(0);
            stepProgress.setString("--");
        }
    }

    public void setStepControlsEnabled(boolean enabled) {
        btnPrevStep.setEnabled(enabled);
        btnNextStep.setEnabled(enabled);
    }

    // ═══════════════════════════════════════════════
    // UI HELPERS
    // ═══════════════════════════════════════════════

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(ThemeManager.FONT_BODY_BOLD);
        label.setForeground(ThemeManager.BG_HEADER);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createStatLabel(String label, String value) {
        JLabel lbl = new JLabel(formatStat(label, value));
        lbl.setFont(ThemeManager.FONT_BODY);
        lbl.setForeground(ThemeManager.TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lbl);
        add(Box.createVerticalStrut(3));
        return lbl;
    }

    private String formatStat(String label, String value) {
        return "<html><span style='color:#757575'>" + label + "</span> <b>" + value + "</b></html>";
    }

    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(ThemeManager.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    private void addFullWidthButton(JButton button) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        add(button);
    }
}
