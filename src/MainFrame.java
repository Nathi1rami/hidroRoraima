import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.Path2D;

/**
 * VIEW LAYER - MainFrame.java
 * Main application window for the HidroRoraima water distribution simulator.
 * Contains the header, network canvas, control panel sidebar, and status bar.
 *
 * @author HidroRoraima
 * @version 1.0
 */
public class MainFrame extends JFrame {

    private NetworkCanvas canvas;
    private ControlPanel controlPanel;
    private JLabel statusLabel;

    public MainFrame() {
        setTitle("HidroRoraima - Sistema de Distribucion de Agua Fluvial");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 850);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);

        // Set custom water drop icon
        try {
            setIconImage(createAppIcon());
        } catch (Exception e) {
            // Ignore icon errors
        }

        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // ═══════════════════════════════════════════
        // HEADER
        // ═══════════════════════════════════════════
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ThemeManager.BG_HEADER);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel titleLabel = new JLabel("HidroRoraima");
        titleLabel.setFont(ThemeManager.FONT_TITLE);
        titleLabel.setForeground(ThemeManager.TEXT_ON_DARK);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel subtitleLabel = new JLabel("Sistema de Distribucion de Agua Fluvial - Ford-Fulkerson (Edmonds-Karp)");
        subtitleLabel.setFont(ThemeManager.FONT_BODY);
        subtitleLabel.setForeground(new Color(180, 200, 220));
        headerPanel.add(subtitleLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // ═══════════════════════════════════════════
        // CANVAS (CENTER)
        // ═══════════════════════════════════════════
        canvas = new NetworkCanvas();
        JScrollPane scrollPane = new JScrollPane(canvas);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(ThemeManager.BG_CANVAS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // ═══════════════════════════════════════════
        // CONTROL PANEL (RIGHT)
        // ═══════════════════════════════════════════
        controlPanel = new ControlPanel();
        JScrollPane controlScroll = new JScrollPane(controlPanel);
        controlScroll.setBorder(null);
        controlScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        controlScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        controlScroll.setPreferredSize(new Dimension(290, 0));
        add(controlScroll, BorderLayout.EAST);

        // ═══════════════════════════════════════════
        // STATUS BAR (BOTTOM)
        // ═══════════════════════════════════════════
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(240, 242, 245));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.BORDER),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));

        statusLabel = new JLabel("Listo - Cargue una red o agregue nodos para comenzar");
        statusLabel.setFont(ThemeManager.FONT_SMALL);
        statusLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        statusBar.add(statusLabel, BorderLayout.WEST);

        JLabel versionLabel = new JLabel("v1.0 - Algoritmo Ford-Fulkerson (Edmonds-Karp)");
        versionLabel.setFont(ThemeManager.FONT_SMALL);
        versionLabel.setForeground(ThemeManager.TEXT_SECONDARY);
        statusBar.add(versionLabel, BorderLayout.EAST);

        add(statusBar, BorderLayout.SOUTH);
    }

    /**
     * Creates a programmatic water drop icon for the application window.
     */
    private Image createAppIcon() {
        int size = 32;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Path2D drop = new Path2D.Double();
        drop.moveTo(16, 2);
        drop.curveTo(8, 12, 4, 18, 4, 22);
        drop.curveTo(4, 28, 10, 30, 16, 30);
        drop.curveTo(22, 30, 28, 28, 28, 22);
        drop.curveTo(28, 18, 24, 12, 16, 2);

        g2d.setColor(ThemeManager.WATER_PRIMARY);
        g2d.fill(drop);
        g2d.setColor(ThemeManager.WATER_DARK);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(drop);

        g2d.dispose();
        return img;
    }

    // ─── Public API ────────────────────────────────
    public NetworkCanvas getCanvas() { return canvas; }
    public ControlPanel getControlPanel() { return controlPanel; }

    /**
     * Updates the status bar text.
     */
    public void setStatus(String text) {
        statusLabel.setText(text);
    }
}
