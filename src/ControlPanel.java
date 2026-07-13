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
    private JPanel alertsPanel;

    // ─── Cards for CardLayout ──────────────────────
    private CardLayout cardLayout;
    private JPanel mainCard;
    private JPanel eventsCard;
    private JPanel activeContainer = null; // Redirects add() calls to build cards dynamically

    // ─── Event components ──────────────────────────
    private JButton btnGoToEvents;
    private JButton btnBackToMain;
    private JCheckBox chkRain;
    private JCheckBox chkDrought;
    private JCheckBox chkDemandPeak;
    private JSpinner spinRainFactor;
    private JSpinner spinDroughtFactor;
    private JSpinner spinDemandFactor;
    private JButton btnRandomBreakdown;
    private JButton btnRestoreNet;

    public ControlPanel() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);
        setBackground(ThemeManager.BG_SIDEBAR);
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ThemeManager.BORDER));

        mainCard = new JPanel();
        mainCard.setLayout(new BoxLayout(mainCard, BoxLayout.Y_AXIS));
        mainCard.setBackground(ThemeManager.BG_SIDEBAR);
        mainCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        eventsCard = new JPanel();
        eventsCard.setLayout(new BoxLayout(eventsCard, BoxLayout.Y_AXIS));
        eventsCard.setBackground(ThemeManager.BG_SIDEBAR);
        eventsCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Redirect add() calls to mainCard
        activeContainer = mainCard;
        buildUI();

        // Redirect add() calls to eventsCard
        activeContainer = eventsCard;
        buildEventsUI();

        // Disable redirection to add cards directly to this panel
        activeContainer = null;
        super.add(mainCard, "MAIN");
        super.add(eventsCard, "EVENTS");

        cardLayout.show(this, "MAIN");
    }

    @Override
    public Component add(Component comp) {
        if (activeContainer != null) {
            return activeContainer.add(comp);
        }
        return super.add(comp);
    }

    @Override
    public void add(Component comp, Object constraints) {
        if (activeContainer != null) {
            activeContainer.add(comp, constraints);
        } else {
            super.add(comp, constraints);
        }
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

        add(Box.createVerticalStrut(12));

        // System Alerts Section
        add(createSectionLabel("Alertas del Sistema"));
        add(Box.createVerticalStrut(4));
        alertsPanel = new JPanel();
        alertsPanel.setLayout(new BoxLayout(alertsPanel, BoxLayout.Y_AXIS));
        alertsPanel.setOpaque(false);
        alertsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(alertsPanel);

        add(Box.createVerticalGlue());
        add(Box.createVerticalStrut(16));
        add(createSeparator());
        add(Box.createVerticalStrut(12));

        // ─── Tools Section ─────────────────────────
        btnGoToEvents = ThemeManager.createStyledButton("Eventos de Simulacion \u2699\uFE0F", new Color(63, 81, 181),
                new Color(92, 107, 192));
        btnLoadPreset = ThemeManager.createStyledButton("Cargar Red Predeterminada", ThemeManager.BTN_PRIMARY,
                ThemeManager.BTN_PRIMARY_HOVER);
        btnFitView = ThemeManager.createStyledButton("Ajustar Vista", new Color(97, 97, 97),
                new Color(117, 117, 117));
        btnReset = ThemeManager.createStyledButton("\u21BA Reiniciar Flujo", ThemeManager.BTN_WARNING,
                new Color(255, 144, 20));
        btnClear = ThemeManager.createStyledButton("Limpiar Todo", ThemeManager.BTN_DANGER,
                ThemeManager.BTN_DANGER_HOVER);

        addFullWidthButton(btnGoToEvents);
        add(Box.createVerticalStrut(4));
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
        btnGoToEvents.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { cardLayout.show(ControlPanel.this, "EVENTS"); }
        });
        btnBackToMain.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { cardLayout.show(ControlPanel.this, "MAIN"); }
        });

        // Event change listeners
        chkRain.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (chkRain.isSelected()) {
                    chkDrought.setSelected(false);
                }
                updateEventsInController();
            }
        });

        chkDrought.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (chkDrought.isSelected()) {
                    chkRain.setSelected(false);
                }
                updateEventsInController();
            }
        });

        chkDemandPeak.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateEventsInController();
            }
        });

        javax.swing.event.ChangeListener spinListener = new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                updateEventsInController();
            }
        };
        spinRainFactor.addChangeListener(spinListener);
        spinDroughtFactor.addChangeListener(spinListener);
        spinDemandFactor.addChangeListener(spinListener);

        btnRandomBreakdown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controller != null) controller.triggerRandomBreakdown();
            }
        });

        btnRestoreNet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controller != null) controller.restoreAllInfrastructure();
            }
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
            alertsPanel.removeAll();
            alertsPanel.revalidate();
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
     * Updates the system alerts panel in the sidebar.
     */
    public void updateAlerts(java.util.List<String> alerts) {
        alertsPanel.removeAll();
        if (alerts == null || alerts.isEmpty()) {
            JLabel lbl = new JLabel("<html><span style='color:#2E7D32'>✔ Sin alertas activas</span></html>");
            lbl.setFont(ThemeManager.FONT_SMALL);
            alertsPanel.add(lbl);
        } else {
            for (String alert : alerts) {
                String colorStr = "#212121";
                if (alert.startsWith("🔴")) {
                    colorStr = "#D32F2F";
                } else if (alert.startsWith("🟠")) {
                    colorStr = "#E65100";
                } else if (alert.startsWith("🟡")) {
                    colorStr = "#FBC02D";
                }
                
                JLabel lbl = new JLabel("<html><span style='color:" + colorStr + "'>" + alert + "</span></html>");
                lbl.setFont(ThemeManager.FONT_SMALL);
                alertsPanel.add(lbl);
                alertsPanel.add(Box.createVerticalStrut(2));
            }
        }
        alertsPanel.revalidate();
        alertsPanel.repaint();
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

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(280, d.height);
    }

    /**
     * Resets the events user interface components to their default states.
     */
    public void resetEventUI() {
        if (chkRain != null) chkRain.setSelected(false);
        if (chkDrought != null) chkDrought.setSelected(false);
        if (chkDemandPeak != null) chkDemandPeak.setSelected(false);
        if (spinRainFactor != null) spinRainFactor.setValue(50);
        if (spinDroughtFactor != null) spinDroughtFactor.setValue(50);
        if (spinDemandFactor != null) spinDemandFactor.setValue(50);
    }

    /**
     * Converts UI state into factor parameters and notifies the controller.
     */
    private void updateEventsInController() {
        if (controller == null) return;
        
        boolean rain = chkRain.isSelected();
        boolean drought = chkDrought.isSelected();
        boolean peak = chkDemandPeak.isSelected();

        // Convert percentage to factor
        double rainPct = ((Number) spinRainFactor.getValue()).doubleValue();
        double rainFactor = 1.0 + (rainPct / 100.0);

        double droughtPct = ((Number) spinDroughtFactor.getValue()).doubleValue();
        double droughtFactor = 1.0 - (droughtPct / 100.0);

        double demandPct = ((Number) spinDemandFactor.getValue()).doubleValue();
        double demandFactor = 1.0 + (demandPct / 100.0);

        controller.updateSimulationEvents(rain, rainFactor, drought, droughtFactor, peak, demandFactor);
    }

    /**
     * Builds the Events Card UI inside ControlPanel.
     */
    private void buildEventsUI() {
        // Header
        JLabel header = new JLabel("Eventos de Simulacion");
        header.setFont(ThemeManager.FONT_HEADER);
        header.setForeground(ThemeManager.BG_HEADER);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(header);
        add(Box.createVerticalStrut(16));

        // Back button
        btnBackToMain = ThemeManager.createStyledButton("\u2190 Volver al Menu", new Color(97, 97, 97), new Color(117, 117, 117));
        addFullWidthButton(btnBackToMain);
        add(Box.createVerticalStrut(20));
        add(createSeparator());
        add(Box.createVerticalStrut(16));

        // Event checkboxes and Spinners
        add(createSectionLabel("Eventos Ambientales"));
        add(Box.createVerticalStrut(8));

        // 1. Rain
        chkRain = new JCheckBox("Lluvia / Tormenta \uD83C\uDF27\uFE0F");
        chkRain.setFont(ThemeManager.FONT_BODY_BOLD);
        chkRain.setOpaque(false);
        chkRain.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(chkRain);
        
        JPanel rainFactorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        rainFactorPanel.setOpaque(false);
        rainFactorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblRain = new JLabel("Aumento oferta:");
        lblRain.setFont(ThemeManager.FONT_SMALL);
        spinRainFactor = new JSpinner(new SpinnerNumberModel(50, 0, 1000, 10));
        spinRainFactor.setPreferredSize(new Dimension(65, 20));
        JLabel lblRainPct = new JLabel("%");
        lblRainPct.setFont(ThemeManager.FONT_SMALL);
        rainFactorPanel.add(lblRain);
        rainFactorPanel.add(spinRainFactor);
        rainFactorPanel.add(lblRainPct);
        add(rainFactorPanel);
        add(Box.createVerticalStrut(12));

        // 2. Drought
        chkDrought = new JCheckBox("Sequia Extrema \u2600\uFE0F");
        chkDrought.setFont(ThemeManager.FONT_BODY_BOLD);
        chkDrought.setOpaque(false);
        chkDrought.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(chkDrought);
        
        JPanel droughtFactorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        droughtFactorPanel.setOpaque(false);
        droughtFactorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblDrought = new JLabel("Reduccion oferta:");
        lblDrought.setFont(ThemeManager.FONT_SMALL);
        spinDroughtFactor = new JSpinner(new SpinnerNumberModel(50, 0, 100, 10));
        spinDroughtFactor.setPreferredSize(new Dimension(65, 20));
        JLabel lblDroughtPct = new JLabel("%");
        lblDroughtPct.setFont(ThemeManager.FONT_SMALL);
        droughtFactorPanel.add(lblDrought);
        droughtFactorPanel.add(spinDroughtFactor);
        droughtFactorPanel.add(lblDroughtPct);
        add(droughtFactorPanel);
        add(Box.createVerticalStrut(12));

        // 3. Demand Peak
        chkDemandPeak = new JCheckBox("Pico de Demanda \uD83D\uDD25");
        chkDemandPeak.setFont(ThemeManager.FONT_BODY_BOLD);
        chkDemandPeak.setOpaque(false);
        chkDemandPeak.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(chkDemandPeak);
        
        JPanel demandFactorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        demandFactorPanel.setOpaque(false);
        demandFactorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblDemand = new JLabel("Aumento demanda:");
        lblDemand.setFont(ThemeManager.FONT_SMALL);
        spinDemandFactor = new JSpinner(new SpinnerNumberModel(50, 0, 1000, 10));
        spinDemandFactor.setPreferredSize(new Dimension(65, 20));
        JLabel lblDemandPct = new JLabel("%");
        lblDemandPct.setFont(ThemeManager.FONT_SMALL);
        demandFactorPanel.add(lblDemand);
        demandFactorPanel.add(spinDemandFactor);
        demandFactorPanel.add(lblDemandPct);
        add(demandFactorPanel);
        add(Box.createVerticalStrut(20));
        add(createSeparator());
        add(Box.createVerticalStrut(16));

        // Infrastructure events
        add(createSectionLabel("Eventos de Infraestructura"));
        add(Box.createVerticalStrut(8));

        btnRandomBreakdown = ThemeManager.createStyledButton("Provocar Averia Aleatoria \uD83D\uDCA5", ThemeManager.BTN_DANGER, ThemeManager.BTN_DANGER_HOVER);
        btnRestoreNet = ThemeManager.createStyledButton("Restaurar Red Completa \u2705", ThemeManager.BTN_SUCCESS, ThemeManager.BTN_SUCCESS_HOVER);

        addFullWidthButton(btnRandomBreakdown);
        add(Box.createVerticalStrut(8));
        addFullWidthButton(btnRestoreNet);
        add(Box.createVerticalStrut(16));
    }
}
