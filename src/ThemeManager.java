import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * VIEW LAYER - ThemeManager.java
 * Centralized theme manager for the HidroRoraima application.
 * Provides colors, fonts, dimensions, and utility methods for consistent styling.
 *
 * Theme: Modern Light Mode with professional color palette.
 *
 * @author HidroRoraima
 * @version 1.0
 */
public class ThemeManager {

    // ═══════════════════════════════════════════════
    // BACKGROUND COLORS
    // ═══════════════════════════════════════════════
    public static final Color BG_PRIMARY = new Color(245, 247, 250);
    public static final Color BG_SECONDARY = Color.WHITE;
    public static final Color BG_CANVAS = new Color(250, 252, 255);
    public static final Color BG_HEADER = new Color(28, 63, 96);
    public static final Color BG_SIDEBAR = new Color(255, 255, 255);

    // ═══════════════════════════════════════════════
    // NODE COLORS
    // ═══════════════════════════════════════════════
    public static final Color EMBALSE_PRIMARY = new Color(46, 125, 50);
    public static final Color EMBALSE_SECONDARY = new Color(76, 175, 80);
    public static final Color EMBALSE_GRADIENT = new Color(129, 199, 132);

    public static final Color ESTACION_PRIMARY = new Color(81, 45, 168);
    public static final Color ESTACION_SECONDARY = new Color(126, 87, 194);
    public static final Color ESTACION_GRADIENT = new Color(179, 157, 219);

    public static final Color BARRIO_PRIMARY = new Color(230, 81, 0);
    public static final Color BARRIO_SECONDARY = new Color(255, 152, 0);
    public static final Color BARRIO_GRADIENT = new Color(255, 183, 77);

    // ═══════════════════════════════════════════════
    // WATER & FLOW COLORS
    // ═══════════════════════════════════════════════
    public static final Color WATER_PRIMARY = new Color(33, 150, 243);
    public static final Color WATER_LIGHT = new Color(100, 181, 246);
    public static final Color WATER_DARK = new Color(21, 101, 192);
    public static final Color WATER_PARTICLE = new Color(33, 150, 243, 200);
    public static final Color WATER_GLOW = new Color(33, 150, 243, 60);
    public static final Color PIPE_EMPTY = new Color(189, 189, 189);
    public static final Color PIPE_BORDER = new Color(117, 117, 117);

    // ═══════════════════════════════════════════════
    // ALGORITHM VISUALIZATION COLORS
    // ═══════════════════════════════════════════════
    public static final Color ALGO_PATH = new Color(76, 175, 80);
    public static final Color ALGO_FRONTIER = new Color(255, 193, 7);
    public static final Color ALGO_BOTTLENECK = new Color(244, 67, 54);
    public static final Color ALGO_VISITED = new Color(156, 204, 101, 100);

    // ═══════════════════════════════════════════════
    // UI COLORS
    // ═══════════════════════════════════════════════
    public static final Color TEXT_PRIMARY = new Color(33, 33, 33);
    public static final Color TEXT_SECONDARY = new Color(117, 117, 117);
    public static final Color TEXT_ON_DARK = Color.WHITE;
    public static final Color BORDER = new Color(224, 224, 224);
    public static final Color SHADOW = new Color(0, 0, 0, 30);
    public static final Color SELECTION = new Color(33, 150, 243, 40);

    public static final Color BTN_PRIMARY = new Color(28, 63, 96);
    public static final Color BTN_PRIMARY_HOVER = new Color(38, 83, 126);
    public static final Color BTN_SUCCESS = new Color(46, 125, 50);
    public static final Color BTN_SUCCESS_HOVER = new Color(56, 145, 60);
    public static final Color BTN_DANGER = new Color(211, 47, 47);
    public static final Color BTN_DANGER_HOVER = new Color(231, 67, 67);
    public static final Color BTN_WARNING = new Color(245, 124, 0);

    // ═══════════════════════════════════════════════
    // SATISFACTION COLORS
    // ═══════════════════════════════════════════════
    public static final Color SATISFACTION_FULL = new Color(76, 175, 80);
    public static final Color SATISFACTION_PARTIAL = new Color(255, 193, 7);
    public static final Color SATISFACTION_LOW = new Color(244, 67, 54);

    // ═══════════════════════════════════════════════
    // ALERT COLORS
    // ═══════════════════════════════════════════════
    public static final Color COLOR_ALERT_CRITICAL = new Color(211, 47, 47);
    public static final Color COLOR_ALERT_MEDIUM = new Color(230, 81, 0);
    public static final Color COLOR_ALERT_WARNING = new Color(251, 192, 45);
    public static final Color COLOR_FAILED_ELEMENT = new Color(120, 120, 120);

    // ═══════════════════════════════════════════════
    // GRID
    // ═══════════════════════════════════════════════
    public static final Color GRID_LINE = new Color(230, 235, 240);
    public static final int GRID_SPACING = 30;

    // ═══════════════════════════════════════════════
    // FONTS
    // ═══════════════════════════════════════════════
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_NODE_NAME = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_NODE_DETAIL = new Font("Segoe UI", Font.PLAIN, 10);
    public static final Font FONT_EDGE_LABEL = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_STAT_VALUE = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_STAT_LABEL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 12);

    // ═══════════════════════════════════════════════
    // DIMENSIONS
    // ═══════════════════════════════════════════════
    public static final int NODE_WIDTH = 130;
    public static final int NODE_HEIGHT = 70;
    public static final int NODE_RADIUS = 15;
    public static final int ESTACION_RADIUS = 30;
    public static final int PIPE_MIN_WIDTH = 2;
    public static final int PIPE_MAX_WIDTH = 8;
    public static final int PARTICLE_RADIUS = 4;
    public static final int PARTICLE_GLOW_RADIUS = 10;
    public static final int SHADOW_OFFSET = 3;
    public static final int ARROW_SIZE = 10;

    // ═══════════════════════════════════════════════
    // ANIMATION
    // ═══════════════════════════════════════════════
    public static final int ANIMATION_FPS = 60;
    public static final int ANIMATION_INTERVAL = 1000 / ANIMATION_FPS;
    public static final double PARTICLE_BASE_SPEED = 0.008;
    public static final int PARTICLES_PER_EDGE = 5;

    private ThemeManager() {} // Utility class — no instances

    // ═══════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════

    /**
     * Returns the primary color for a given node type.
     */
    public static Color getNodePrimaryColor(Node.NodeType type) {
        switch (type) {
            case EMBALSE: return EMBALSE_PRIMARY;
            case ESTACION: return ESTACION_PRIMARY;
            case BARRIO: return BARRIO_PRIMARY;
            default: return TEXT_PRIMARY;
        }
    }

    /**
     * Returns the secondary (lighter) color for a given node type.
     */
    public static Color getNodeSecondaryColor(Node.NodeType type) {
        switch (type) {
            case EMBALSE: return EMBALSE_SECONDARY;
            case ESTACION: return ESTACION_SECONDARY;
            case BARRIO: return BARRIO_SECONDARY;
            default: return TEXT_SECONDARY;
        }
    }

    /**
     * Returns the gradient end color for a given node type.
     */
    public static Color getNodeGradientColor(Node.NodeType type) {
        switch (type) {
            case EMBALSE: return EMBALSE_GRADIENT;
            case ESTACION: return ESTACION_GRADIENT;
            case BARRIO: return BARRIO_GRADIENT;
            default: return BORDER;
        }
    }

    /**
     * Creates a styled button with custom painting and hover effect.
     */
    public static JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text) {
            private boolean hovering = false;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                setForeground(TEXT_ON_DARK);
                setFont(FONT_BUTTON);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hovering = true;
                        repaint();
                    }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hovering = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bg = isEnabled() ? (hovering ? hoverColor : bgColor) : new Color(180, 180, 180);
                g2d.setColor(bg);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2d.dispose();
                super.paintComponent(g);
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(Math.max(d.width + 24, 120), 36);
            }
        };
        return button;
    }

    /**
     * Interpolates between two colors based on a ratio (0.0 to 1.0).
     */
    public static Color interpolateColor(Color c1, Color c2, double ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * ratio);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * ratio);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * ratio);
        int a = (int) (c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * ratio);
        return new Color(r, g, b, a);
    }

    /**
     * Returns a color for a pipe based on utilization (0=gray, 1=blue).
     */
    public static Color getPipeFlowColor(double utilization) {
        return interpolateColor(PIPE_EMPTY, WATER_PRIMARY, utilization);
    }

    /**
     * Returns a color for satisfaction percentage.
     */
    public static Color getSatisfactionColor(double percentage) {
        if (percentage >= 90) return SATISFACTION_FULL;
        if (percentage >= 50) return SATISFACTION_PARTIAL;
        return SATISFACTION_LOW;
    }

    /**
     * Creates a rounded card border with padding.
     */
    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        );
    }

    /**
     * Applies global look and feel settings for modern appearance.
     */
    public static void applyGlobalTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fallback to default L&F
        }
        UIManager.put("Panel.background", BG_PRIMARY);
        UIManager.put("ToolTip.font", FONT_BODY);
        UIManager.put("ToolTip.background", BG_SECONDARY);
        UIManager.put("ToolTip.foreground", TEXT_PRIMARY);
    }
}
