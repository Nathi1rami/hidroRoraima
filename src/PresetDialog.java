import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * VIEW LAYER - PresetDialog.java
 * Modal dialog for selecting network presets or configuring procedural generation.
 *
 * Features:
 * - List of geographic presets with descriptions
 * - Preview panel showing mini-map of selected preset
 * - Procedural generation configuration
 * - Modern dark-glass aesthetic matching HidroRoraima theme
 *
 * @author HidroRoraima
 * @version 1.0
 */
public class PresetDialog extends JDialog {

    private boolean confirmed = false;
    private NetworkPresets.PresetType selectedType = null;

    // UI Components
    private JList<NetworkPresets.PresetType> presetList;
    private JTextArea descriptionArea;
    private JLabel lblNodeCount;
    private JLabel lblEdgeCount;
    private JLabel lblRegion;
    private PreviewPanel previewPanel;

    public PresetDialog(JFrame parent) {
        super(parent, "Mapas y Presets - HidroRoraima", true);
        setSize(700, 520);
        setLocationRelativeTo(parent);
        setResizable(false);

        buildUI();
    }

    public boolean isConfirmed() { return confirmed; }
    public NetworkPresets.PresetType getSelectedType() { return selectedType; }

    // ═══════════════════════════════════════════════
    // UI CONSTRUCTION
    // ═══════════════════════════════════════════════

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(245, 247, 250));

        // ─── Header ────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ThemeManager.BG_HEADER);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel titleLabel = new JLabel("\uD83D\uDDFA\uFE0F  Mapas y Presets");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel subtitleLabel = new JLabel("Redes hidricas de Venezuela");
        subtitleLabel.setFont(ThemeManager.FONT_BODY);
        subtitleLabel.setForeground(new Color(180, 200, 220));
        headerPanel.add(subtitleLabel, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // ─── Content ───────────────────────────────
        JPanel contentPanel = new JPanel(new BorderLayout(12, 0));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        contentPanel.setOpaque(false);

        // Left: Preset list
        JPanel leftPanel = buildLeftPanel();
        contentPanel.add(leftPanel, BorderLayout.WEST);

        // Right: Details & Preview
        JPanel rightPanel = buildRightPanel();
        contentPanel.add(rightPanel, BorderLayout.CENTER);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // ─── Footer ───────────────────────────────
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        footerPanel.setBackground(new Color(240, 242, 245));
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.BORDER));

        JButton btnCancel = ThemeManager.createStyledButton("Cancelar",
                new Color(97, 97, 97), new Color(117, 117, 117));
        btnCancel.setPreferredSize(new Dimension(120, 36));
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = false;
                dispose();
            }
        });

        JButton btnLoad = ThemeManager.createStyledButton("\u25B6 Cargar Red",
                ThemeManager.BTN_SUCCESS, ThemeManager.BTN_SUCCESS_HOVER);
        btnLoad.setPreferredSize(new Dimension(140, 36));
        btnLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedType = presetList.getSelectedValue();
                if (selectedType != null) {
                    confirmed = true;
                    dispose();
                }
            }
        });

        footerPanel.add(btnCancel);
        footerPanel.add(btnLoad);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Select first item by default
        presetList.setSelectedIndex(0);
    }

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(240, 0));

        JLabel listLabel = new JLabel("Redes Disponibles");
        listLabel.setFont(ThemeManager.FONT_BODY_BOLD);
        listLabel.setForeground(ThemeManager.BG_HEADER);
        panel.add(listLabel, BorderLayout.NORTH);

        presetList = new JList<NetworkPresets.PresetType>(NetworkPresets.PresetType.values());
        presetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        presetList.setCellRenderer(new PresetCellRenderer());
        presetList.setFont(ThemeManager.FONT_BODY);
        presetList.setBackground(Color.WHITE);
        presetList.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER));

        presetList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateDetails();
                }
            }
        });

        // Double-click to confirm
        presetList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectedType = presetList.getSelectedValue();
                    if (selectedType != null) {
                        confirmed = true;
                        dispose();
                    }
                }
            }
        });

        JScrollPane listScroll = new JScrollPane(presetList);
        listScroll.setBorder(null);
        panel.add(listScroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        descriptionArea.setOpaque(false);
        descriptionArea.setFont(ThemeManager.FONT_BODY);
        descriptionArea.setForeground(ThemeManager.TEXT_PRIMARY);
        descriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(descriptionArea);
        infoPanel.add(Box.createVerticalStrut(8));

        // Stats row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 8, 0));
        statsRow.setOpaque(false);
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        lblNodeCount = createStatCard("Nodos", "--");
        lblEdgeCount = createStatCard("Tuberias", "--");
        lblRegion = createStatCard("Region", "--");

        statsRow.add(lblNodeCount);
        statsRow.add(lblEdgeCount);
        statsRow.add(lblRegion);
        infoPanel.add(statsRow);

        panel.add(infoPanel, BorderLayout.NORTH);

        // Preview panel
        previewPanel = new PreviewPanel();
        previewPanel.setPreferredSize(new Dimension(0, 250));
        previewPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        panel.add(previewPanel, BorderLayout.CENTER);

        return panel;
    }

    // ═══════════════════════════════════════════════
    // UPDATE DETAILS
    // ═══════════════════════════════════════════════

    private void updateDetails() {
        NetworkPresets.PresetType type = presetList.getSelectedValue();
        if (type == null) return;

        descriptionArea.setText(type.getDescription());
        lblNodeCount.setText(buildStatHTML("Nodos", "~" + type.getApproxNodes()));
        lblEdgeCount.setText(buildStatHTML("Tuberias",
                type.getApproxEdges() > 0 ? "~" + type.getApproxEdges() : "Auto"));
        lblRegion.setText(buildStatHTML("Region", type.getRegion()));

        previewPanel.setPresetType(type);
        previewPanel.repaint();
    }

    private JLabel createStatCard(String label, String value) {
        JLabel lbl = new JLabel(buildStatHTML(label, value));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    private String buildStatHTML(String label, String value) {
        return "<html><div style='text-align:center'>"
                + "<span style='font-size:9px;color:#757575'>" + label + "</span><br>"
                + "<b style='font-size:14px;color:#1C3F60'>" + value + "</b>"
                + "</div></html>";
    }

    // ═══════════════════════════════════════════════
    // PRESET LIST CELL RENDERER
    // ═══════════════════════════════════════════════

    private static class PresetCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            NetworkPresets.PresetType type = (NetworkPresets.PresetType) value;

            String icon;
            if (type.isProcedural()) {
                icon = "\uD83C\uDFB2 "; // dice
            } else if (type.getRegion().equals("Venezuela")) {
                icon = "\uD83C\uDDFB\uD83C\uDDEA "; // Venezuela flag
            } else {
                icon = "\uD83D\uDDFA\uFE0F "; // map
            }

            label.setText(icon + type.getDisplayName());
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

            if (isSelected) {
                label.setBackground(new Color(33, 150, 243, 30));
                label.setForeground(ThemeManager.BG_HEADER);
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(ThemeManager.TEXT_PRIMARY);
            }

            return label;
        }
    }

    // ═══════════════════════════════════════════════
    // PREVIEW PANEL (mini network visualization)
    // ═══════════════════════════════════════════════

    private static class PreviewPanel extends JPanel {
        private NetworkPresets.PresetType presetType;

        public PreviewPanel() {
            setBackground(new Color(250, 252, 255));
        }

        public void setPresetType(NetworkPresets.PresetType type) {
            this.presetType = type;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Background gradient
            GradientPaint bg = new GradientPaint(0, 0, new Color(250, 252, 255),
                    0, getHeight(), new Color(235, 240, 248));
            g2d.setPaint(bg);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            if (presetType == null) {
                g2d.setFont(ThemeManager.FONT_BODY);
                g2d.setColor(ThemeManager.TEXT_SECONDARY);
                String msg = "Seleccione un preset para ver la vista previa";
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
                g2d.dispose();
                return;
            }

            // Generate a temp graph to preview
            NetworkGraph tempGraph = new NetworkGraph();
            NetworkPresets.loadPreset(tempGraph, presetType);

            if (tempGraph.getNodeCount() == 0) {
                g2d.dispose();
                return;
            }

            // Calculate bounds and scale
            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
            for (Node n : tempGraph.getNodes()) {
                minX = Math.min(minX, n.getX());
                minY = Math.min(minY, n.getY());
                maxX = Math.max(maxX, n.getX());
                maxY = Math.max(maxY, n.getY());
            }

            double graphW = maxX - minX + 60;
            double graphH = maxY - minY + 60;
            double scaleX = (getWidth() - 40) / graphW;
            double scaleY = (getHeight() - 40) / graphH;
            double scale = Math.min(scaleX, scaleY);

            double centerX = (minX + maxX) / 2;
            double centerY = (minY + maxY) / 2;
            double offsetX = getWidth() / 2.0 - centerX * scale;
            double offsetY = getHeight() / 2.0 - centerY * scale;

            // Draw edges
            g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (Edge e : tempGraph.getEdges()) {
                double x1 = e.getFrom().getX() * scale + offsetX;
                double y1 = e.getFrom().getY() * scale + offsetY;
                double x2 = e.getTo().getX() * scale + offsetX;
                double y2 = e.getTo().getY() * scale + offsetY;
                g2d.setColor(new Color(189, 189, 189));
                g2d.draw(new Line2D.Double(x1, y1, x2, y2));
            }

            // Draw nodes
            for (Node n : tempGraph.getNodes()) {
                double x = n.getX() * scale + offsetX;
                double y = n.getY() * scale + offsetY;
                int r = 5;

                Color color;
                switch (n.getType()) {
                    case EMBALSE: color = ThemeManager.EMBALSE_PRIMARY; break;
                    case ESTACION: color = ThemeManager.ESTACION_PRIMARY; break;
                    case BARRIO: color = ThemeManager.BARRIO_PRIMARY; break;
                    default: color = ThemeManager.TEXT_PRIMARY;
                }

                g2d.setColor(color);
                g2d.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));

                // Tiny label for small networks
                if (tempGraph.getNodeCount() <= 25) {
                    g2d.setFont(new Font("Segoe UI", Font.PLAIN, 8));
                    g2d.setColor(ThemeManager.TEXT_SECONDARY);
                    FontMetrics fm = g2d.getFontMetrics();
                    String name = n.getName();
                    if (name.length() > 12) name = name.substring(0, 10) + "..";
                    g2d.drawString(name, (float) (x - fm.stringWidth(name) / 2.0), (float) (y - r - 3));
                }
            }

            // Legend
            paintLegend(g2d);

            // Info badge
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2d.setColor(new Color(28, 63, 96, 150));
            String info = tempGraph.getNodeCount() + " nodos, " + tempGraph.getEdgeCount() + " tuberias";
            g2d.drawString(info, 8, getHeight() - 8);

            g2d.dispose();
        }

        private void paintLegend(Graphics2D g2d) {
            int lx = getWidth() - 115;
            int ly = 10;

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
            g2d.setColor(new Color(255, 255, 255, 220));
            g2d.fillRoundRect(lx - 6, ly - 4, 112, 56, 8, 8);
            g2d.setColor(ThemeManager.BORDER);
            g2d.setStroke(new BasicStroke(0.5f));
            g2d.drawRoundRect(lx - 6, ly - 4, 112, 56, 8, 8);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 9));

            g2d.setColor(ThemeManager.EMBALSE_PRIMARY);
            g2d.fill(new Ellipse2D.Double(lx, ly + 2, 8, 8));
            g2d.setColor(ThemeManager.TEXT_PRIMARY);
            g2d.drawString("Embalse", lx + 12, ly + 10);

            g2d.setColor(ThemeManager.ESTACION_PRIMARY);
            g2d.fill(new Ellipse2D.Double(lx, ly + 18, 8, 8));
            g2d.setColor(ThemeManager.TEXT_PRIMARY);
            g2d.drawString("Estacion", lx + 12, ly + 26);

            g2d.setColor(ThemeManager.BARRIO_PRIMARY);
            g2d.fill(new Ellipse2D.Double(lx, ly + 34, 8, 8));
            g2d.setColor(ThemeManager.TEXT_PRIMARY);
            g2d.drawString("Barrio", lx + 12, ly + 42);
        }
    }
}
