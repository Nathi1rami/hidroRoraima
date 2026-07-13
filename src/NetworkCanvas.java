import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * VIEW LAYER - NetworkCanvas.java
 * Custom-painted interactive canvas for visualizing the water distribution network.
 *
 * Features:
 * - Custom node rendering with gradients, shadows, and icons
 * - Animated flow particles along pipes (Option A)
 * - Drag-and-drop node positioning
 * - Zoom and pan support (mouse wheel + Ctrl-drag)
 * - Algorithm step visualization with highlighting
 * - Context menus for node/edge operations
 *
 * @author HidroRoraima
 * @version 1.0
 */
public class NetworkCanvas extends JPanel {

    private NetworkGraph graph;
    private NetworkController controller;
    private final VenezuelaMapRenderer mapRenderer = new VenezuelaMapRenderer();

    // ─── Transform state ───────────────────────────
    private double zoom = 1.0;
    private double panX = 0, panY = 0;
    private Point lastMousePoint;
    private boolean panning = false;

    // ─── Interaction state ─────────────────────────
    private Node draggedNode = null;
    private Node hoveredNode = null;
    private Edge hoveredEdge = null;
    private boolean connecting = false;
    private Node connectFrom = null;
    private Point connectMousePos = null;

    // ─── Animation ─────────────────────────────────
    private javax.swing.Timer animationTimer;
    private final List<FlowParticle> particles = new ArrayList<FlowParticle>();
    private long lastFrameTime = System.nanoTime();
    private double animationTime = 0;

    /**
     * Inner class representing an animated flow particle on an edge.
     */
    private static class FlowParticle {
        Edge edge;
        double position; // 0.0 to 1.0 along edge
        double speed;

        FlowParticle(Edge edge, double position, double speed) {
            this.edge = edge;
            this.position = position;
            this.speed = speed;
        }
    }

    private List<String> activeAlerts = new ArrayList<String>();

    public void setAlerts(List<String> alerts) {
        this.activeAlerts = alerts;
        repaint();
    }

    public NetworkCanvas() {
        setBackground(ThemeManager.BG_CANVAS);
        setDoubleBuffered(true);

        // ─── Mouse listeners ───────────────────────
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { handleMousePressed(e); }
            @Override
            public void mouseReleased(MouseEvent e) { handleMouseReleased(e); }
            @Override
            public void mouseDragged(MouseEvent e) { handleMouseDragged(e); }
            @Override
            public void mouseMoved(MouseEvent e) { handleMouseMoved(e); }
            @Override
            public void mouseClicked(MouseEvent e) { handleMouseClicked(e); }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        // ─── Mouse wheel for zoom ──────────────────
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double oldZoom = zoom;
                if (e.getWheelRotation() < 0) {
                    zoom = Math.min(3.0, zoom * 1.1);
                } else {
                    zoom = Math.max(0.3, zoom / 1.1);
                }
                double mx = e.getX(), my = e.getY();
                panX = mx - (mx - panX) * (zoom / oldZoom);
                panY = my - (my - panY) * (zoom / oldZoom);
                repaint();
            }
        });

        // ─── Animation timer (60 FPS) ──────────────
        animationTimer = new javax.swing.Timer(ThemeManager.ANIMATION_INTERVAL, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAnimation();
                repaint();
            }
        });
        animationTimer.start();
    }

    // ═══════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════

    public void setGraph(NetworkGraph graph) {
        this.graph = graph;
        rebuildParticles();
        repaint();
    }

    public void setController(NetworkController controller) {
        this.controller = controller;
    }

    public void setConnectingMode(boolean connecting) {
        this.connecting = connecting;
        this.connectFrom = null;
        setCursor(connecting ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
                : Cursor.getDefaultCursor());
    }

    public double getZoom() { return zoom; }

    public boolean isShowMap() { return mapRenderer.isVisible(); }
    public void setShowMap(boolean show) {
        mapRenderer.setVisible(show);
        repaint();
    }
    public void toggleMap() {
        mapRenderer.setVisible(!mapRenderer.isVisible());
        repaint();
    }

    // ═══════════════════════════════════════════════
    // COORDINATE TRANSFORMS
    // ═══════════════════════════════════════════════

    private Point2D screenToWorld(Point p) {
        return new Point2D.Double((p.x - panX) / zoom, (p.y - panY) / zoom);
    }

    @SuppressWarnings("unused")
    private Point2D worldToScreen(double wx, double wy) {
        return new Point2D.Double(wx * zoom + panX, wy * zoom + panY);
    }

    // ═══════════════════════════════════════════════
    // MOUSE HANDLING
    // ═══════════════════════════════════════════════

    private void handleMousePressed(MouseEvent e) {
        if (graph == null) return;
        Point2D world = screenToWorld(e.getPoint());

        // Middle-click or Ctrl+click for panning
        if (SwingUtilities.isMiddleMouseButton(e) ||
                (SwingUtilities.isLeftMouseButton(e) && e.isControlDown())) {
            panning = true;
            lastMousePoint = e.getPoint();
            return;
        }

        if (SwingUtilities.isLeftMouseButton(e)) {
            Node node = graph.getNodeAt(world.getX(), world.getY(), 5);

            if (connecting) {
                if (node != null) {
                    if (connectFrom == null) {
                        connectFrom = node;
                    } else if (!connectFrom.equals(node)) {
                        if (controller != null) {
                            controller.requestAddEdge(connectFrom, node);
                        }
                        connectFrom = null;
                        connecting = false;
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
                return;
            }

            if (node != null) {
                if (controller != null) controller.selectNode(node);
                draggedNode = node;
                lastMousePoint = e.getPoint();
            } else {
                if (controller != null) controller.deselectAll();
            }
        }
    }

    private void handleMouseReleased(MouseEvent e) {
        draggedNode = null;
        panning = false;
    }

    private void handleMouseDragged(MouseEvent e) {
        if (panning && lastMousePoint != null) {
            panX += e.getX() - lastMousePoint.x;
            panY += e.getY() - lastMousePoint.y;
            lastMousePoint = e.getPoint();
            repaint();
            return;
        }

        if (draggedNode != null) {
            Point2D world = screenToWorld(e.getPoint());
            draggedNode.setPosition(world.getX(), world.getY());
            rebuildParticles();
            repaint();
        }

        if (connecting) {
            connectMousePos = e.getPoint();
            repaint();
        }
    }

    private void handleMouseMoved(MouseEvent e) {
        if (graph == null) return;
        Point2D world = screenToWorld(e.getPoint());

        Node newHover = graph.getNodeAt(world.getX(), world.getY(), 5);
        Edge newEdgeHover = null;
        if (newHover == null) {
            newEdgeHover = graph.getEdgeNear(world.getX(), world.getY(), 8);
        }

        if (newHover != hoveredNode || newEdgeHover != hoveredEdge) {
            hoveredNode = newHover;
            hoveredEdge = newEdgeHover;

            if (hoveredNode != null) {
                setToolTipText(hoveredNode.toString());
                setCursor(connecting ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
                        : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else if (hoveredEdge != null) {
                setToolTipText(hoveredEdge.toString());
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                setToolTipText(null);
                setCursor(connecting ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
                        : Cursor.getDefaultCursor());
            }
            repaint();
        }

        if (connecting) {
            connectMousePos = e.getPoint();
            repaint();
        }
    }

    private void handleMouseClicked(MouseEvent e) {
        if (graph == null) return;

        // Double-click to edit
        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
            Point2D world = screenToWorld(e.getPoint());
            Node node = graph.getNodeAt(world.getX(), world.getY(), 5);
            if (node != null && controller != null) {
                controller.editNode(node);
            } else {
                Edge edge = graph.getEdgeNear(world.getX(), world.getY(), 8);
                if (edge != null && controller != null) {
                    controller.editEdge(edge);
                }
            }
        }

        // Right-click for context menu
        if (SwingUtilities.isRightMouseButton(e)) {
            Point2D world = screenToWorld(e.getPoint());
            showContextMenu(e, world);
        }
    }

    private void showContextMenu(MouseEvent e, Point2D world) {
        JPopupMenu menu = new JPopupMenu();
        Node node = graph.getNodeAt(world.getX(), world.getY(), 5);
        Edge edge = (node == null) ? graph.getEdgeNear(world.getX(), world.getY(), 8) : null;

        if (node != null) {
            JMenuItem editItem = new JMenuItem("Editar " + node.getName());
            editItem.setFont(ThemeManager.FONT_BODY);
            editItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    if (controller != null) controller.editNode(node);
                }
            });
            menu.add(editItem);

            JMenuItem deleteItem = new JMenuItem("Eliminar " + node.getName());
            deleteItem.setFont(ThemeManager.FONT_BODY);
            deleteItem.setForeground(ThemeManager.BTN_DANGER);
            deleteItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    if (controller != null) controller.deleteNode(node);
                }
            });
            menu.add(deleteItem);

            JMenuItem toggleActiveItem = new JMenuItem(node.isActive() ? "Simular Falla (Desactivar)" : "Restaurar (Activar)");
            toggleActiveItem.setFont(ThemeManager.FONT_BODY);
            toggleActiveItem.setForeground(node.isActive() ? ThemeManager.COLOR_ALERT_CRITICAL : ThemeManager.BTN_SUCCESS);
            toggleActiveItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    if (controller != null) controller.toggleNodeActive(node);
                }
            });
            menu.add(toggleActiveItem);

            menu.addSeparator();

            JMenuItem connectItem = new JMenuItem("Conectar desde aqui");
            connectItem.setFont(ThemeManager.FONT_BODY);
            connectItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    connecting = true;
                    connectFrom = node;
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                }
            });
            menu.add(connectItem);

        } else if (edge != null) {
            JMenuItem editItem = new JMenuItem("Editar tuberia");
            editItem.setFont(ThemeManager.FONT_BODY);
            editItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    if (controller != null) controller.editEdge(edge);
                }
            });
            menu.add(editItem);

            JMenuItem deleteItem = new JMenuItem("Eliminar tuberia");
            deleteItem.setFont(ThemeManager.FONT_BODY);
            deleteItem.setForeground(ThemeManager.BTN_DANGER);
            deleteItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    if (controller != null) controller.deleteEdge(edge);
                }
            });
            menu.add(deleteItem);

            JMenuItem toggleActiveItem = new JMenuItem(edge.isActive() ? "Simular Falla (Desactivar)" : "Restaurar (Activar)");
            toggleActiveItem.setFont(ThemeManager.FONT_BODY);
            toggleActiveItem.setForeground(edge.isActive() ? ThemeManager.COLOR_ALERT_CRITICAL : ThemeManager.BTN_SUCCESS);
            toggleActiveItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    if (controller != null) controller.toggleEdgeActive(edge);
                }
            });
            menu.add(toggleActiveItem);

        } else {
            // Click on empty space — add nodes
            final double wx = world.getX();
            final double wy = world.getY();

            JMenuItem addEmbalse = new JMenuItem("Agregar Embalse aqui");
            addEmbalse.setFont(ThemeManager.FONT_BODY);
            addEmbalse.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    if (controller != null) controller.addNodeAt(Node.NodeType.EMBALSE, wx, wy);
                }
            });
            menu.add(addEmbalse);

            JMenuItem addEstacion = new JMenuItem("Agregar Estacion aqui");
            addEstacion.setFont(ThemeManager.FONT_BODY);
            addEstacion.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    if (controller != null) controller.addNodeAt(Node.NodeType.ESTACION, wx, wy);
                }
            });
            menu.add(addEstacion);

            JMenuItem addBarrio = new JMenuItem("Agregar Barrio aqui");
            addBarrio.setFont(ThemeManager.FONT_BODY);
            addBarrio.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    if (controller != null) controller.addNodeAt(Node.NodeType.BARRIO, wx, wy);
                }
            });
            menu.add(addBarrio);
        }

        menu.show(this, e.getX(), e.getY());
    }

    // ═══════════════════════════════════════════════
    // ANIMATION
    // ═══════════════════════════════════════════════

    /**
     * Rebuilds the flow particles based on current edge flows.
     */
    public void rebuildParticles() {
        particles.clear();
        if (graph == null) return;

        for (Edge edge : graph.getEdges()) {
            if (edge.getFlow() > 0) {
                double util = edge.getUtilization();
                int count = (int) (ThemeManager.PARTICLES_PER_EDGE * util) + 1;
                double speed = ThemeManager.PARTICLE_BASE_SPEED * (0.5 + util * 0.5);

                for (int i = 0; i < count; i++) {
                    double pos = (double) i / count;
                    particles.add(new FlowParticle(edge, pos, speed));
                }
            }
        }
    }

    private void updateAnimation() {
        long now = System.nanoTime();
        double dt = (now - lastFrameTime) / 1_000_000.0; // milliseconds
        lastFrameTime = now;
        animationTime += dt;

        for (FlowParticle p : particles) {
            p.position += p.speed * (dt / 16.0); // normalized to 60fps
            if (p.position > 1.0) p.position -= 1.0;
        }
    }

    public void startAnimation() {
        if (!animationTimer.isRunning()) animationTimer.start();
    }

    public void stopAnimation() {
        if (animationTimer.isRunning()) animationTimer.stop();
    }

    // ═══════════════════════════════════════════════
    // PAINTING
    // ═══════════════════════════════════════════════

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Paint background gradient
        paintBackground(g2d);

        if (graph == null || graph.getNodeCount() == 0) {
            paintWelcomeMessage(g2d);
            g2d.dispose();
            return;
        }

        // Apply zoom and pan transform
        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(panX, panY);
        g2d.scale(zoom, zoom);

        // Render layers in order
        mapRenderer.paintMap(g2d);
        paintGrid(g2d);

        for (Edge edge : graph.getEdges()) {
            paintEdge(g2d, edge);
        }

        paintParticles(g2d);

        if (connecting && connectFrom != null && connectMousePos != null) {
            paintConnectingLine(g2d);
        }

        for (Node node : graph.getNodes()) {
            paintNode(g2d, node);
        }

        g2d.setTransform(oldTransform);

        // Paint overlay (not affected by zoom/pan)
        paintOverlay(g2d);

        g2d.dispose();
    }

    // ─── Background ────────────────────────────────

    private void paintBackground(Graphics2D g2d) {
        GradientPaint gradient = new GradientPaint(
                0, 0, ThemeManager.BG_CANVAS,
                0, getHeight(), new Color(235, 240, 248)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    // ─── Grid ──────────────────────────────────────

    private void paintGrid(Graphics2D g2d) {
        g2d.setColor(ThemeManager.GRID_LINE);
        g2d.setStroke(new BasicStroke(0.5f));
        int spacing = ThemeManager.GRID_SPACING;

        Point2D topLeft = screenToWorld(new Point(0, 0));
        Point2D bottomRight = screenToWorld(new Point(getWidth(), getHeight()));

        int startX = (int) (Math.floor(topLeft.getX() / spacing) * spacing);
        int startY = (int) (Math.floor(topLeft.getY() / spacing) * spacing);
        int endX = (int) (Math.ceil(bottomRight.getX() / spacing) * spacing);
        int endY = (int) (Math.ceil(bottomRight.getY() / spacing) * spacing);

        for (int x = startX; x <= endX; x += spacing) {
            g2d.drawLine(x, startY, x, endY);
        }
        for (int y = startY; y <= endY; y += spacing) {
            g2d.drawLine(startX, y, endX, y);
        }
    }

    // ─── Edges (Pipes) ─────────────────────────────

    private void paintEdge(Graphics2D g2d, Edge edge) {
        Node from = edge.getFrom();
        Node to = edge.getTo();

        double x1 = from.getX(), y1 = from.getY();
        double x2 = to.getX(), y2 = to.getY();

        // Pipe width based on capacity ratio
        float maxCap = 1;
        for (Edge e : graph.getEdges()) maxCap = Math.max(maxCap, (float) e.getCapacity());
        float widthRatio = (float) (edge.getCapacity() / maxCap);
        float pipeWidth = ThemeManager.PIPE_MIN_WIDTH +
                (ThemeManager.PIPE_MAX_WIDTH - ThemeManager.PIPE_MIN_WIDTH) * widthRatio;

        // Color based on state
        Color pipeColor;
        if (!edge.isActive()) {
            pipeColor = ThemeManager.COLOR_FAILED_ELEMENT;
        } else if (edge.isHighlighted() && edge.getHighlightColor() != null) {
            pipeColor = edge.getHighlightColor();
        } else if (edge.isBottleneck()) {
            pipeColor = ThemeManager.ALGO_BOTTLENECK;
        } else if (edge.getUtilization() >= 1.0) {
            pipeColor = ThemeManager.COLOR_ALERT_MEDIUM;
        } else if (edge.getUtilization() >= 0.8) {
            pipeColor = ThemeManager.COLOR_ALERT_WARNING;
        } else {
            pipeColor = ThemeManager.getPipeFlowColor(edge.getUtilization());
        }

        // Shadow
        g2d.setColor(new Color(0, 0, 0, 20));
        g2d.setStroke(new BasicStroke(pipeWidth + 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(new Line2D.Double(x1 + 2, y1 + 2, x2 + 2, y2 + 2));

        // Outline
        g2d.setColor(ThemeManager.PIPE_BORDER);
        if (!edge.isActive()) {
            g2d.setStroke(new BasicStroke(pipeWidth + 1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1.0f, new float[]{6f, 6f}, 0f));
        } else {
            g2d.setStroke(new BasicStroke(pipeWidth + 1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }
        g2d.draw(new Line2D.Double(x1, y1, x2, y2));

        // Fill
        g2d.setColor(pipeColor);
        if (!edge.isActive()) {
            g2d.setStroke(new BasicStroke(pipeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1.0f, new float[]{6f, 6f}, 0f));
        } else {
            g2d.setStroke(new BasicStroke(pipeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }
        g2d.draw(new Line2D.Double(x1, y1, x2, y2));

        // Direction arrow
        drawArrow(g2d, x1, y1, x2, y2, pipeColor);

        // Label
        paintEdgeLabel(g2d, edge, x1, y1, x2, y2);
    }

    private void drawArrow(Graphics2D g2d, double x1, double y1, double x2, double y2, Color color) {
        double dx = x2 - x1, dy = y2 - y1;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 1) return;

        double ux = dx / len, uy = dy / len;
        double mx = x1 + dx * 0.6, my = y1 + dy * 0.6;

        int size = ThemeManager.ARROW_SIZE;
        Path2D arrow = new Path2D.Double();
        arrow.moveTo(mx + ux * size, my + uy * size);
        arrow.lineTo(mx - ux * size * 0.5 + uy * size * 0.5, my - uy * size * 0.5 - ux * size * 0.5);
        arrow.lineTo(mx - ux * size * 0.5 - uy * size * 0.5, my - uy * size * 0.5 + ux * size * 0.5);
        arrow.closePath();

        g2d.setColor(color);
        g2d.fill(arrow);
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(arrow);
    }

    private void paintEdgeLabel(Graphics2D g2d, Edge edge, double x1, double y1, double x2, double y2) {
        double mx = (x1 + x2) / 2;
        double my = (y1 + y2) / 2;

        String label = edge.getFlowLabel();
        g2d.setFont(ThemeManager.FONT_EDGE_LABEL);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(label);
        int textHeight = fm.getHeight();

        // Offset label perpendicular to the edge
        double dx = x2 - x1, dy = y2 - y1;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len > 0) {
            double perpX = -dy / len * 15;
            double perpY = dx / len * 15;
            AffineTransform old = g2d.getTransform();
            g2d.translate(perpX, perpY);

            int pad = 4;
            RoundRectangle2D bg = new RoundRectangle2D.Double(
                    mx - textWidth / 2.0 - pad, my - textHeight / 2.0 - pad + 2,
                    textWidth + pad * 2, textHeight + pad, 6, 6
            );

            g2d.setColor(new Color(255, 255, 255, 220));
            g2d.fill(bg);
            g2d.setColor(ThemeManager.BORDER);
            g2d.setStroke(new BasicStroke(0.5f));
            g2d.draw(bg);

            g2d.setColor(ThemeManager.TEXT_PRIMARY);
            g2d.drawString(label, (float) (mx - textWidth / 2.0), (float) (my + fm.getAscent() / 2.0));

            g2d.setTransform(old);
        }
    }

    // ─── Flow Particles ────────────────────────────

    private void paintParticles(Graphics2D g2d) {
        for (FlowParticle p : particles) {
            Edge edge = p.edge;
            double x1 = edge.getFrom().getX(), y1 = edge.getFrom().getY();
            double x2 = edge.getTo().getX(), y2 = edge.getTo().getY();

            double px = x1 + (x2 - x1) * p.position;
            double py = y1 + (y2 - y1) * p.position;

            int glowR = ThemeManager.PARTICLE_GLOW_RADIUS;
            int partR = ThemeManager.PARTICLE_RADIUS;

            // Glow
            g2d.setColor(ThemeManager.WATER_GLOW);
            g2d.fill(new Ellipse2D.Double(px - glowR, py - glowR, glowR * 2, glowR * 2));

            // Particle
            g2d.setColor(ThemeManager.WATER_PARTICLE);
            g2d.fill(new Ellipse2D.Double(px - partR, py - partR, partR * 2, partR * 2));

            // Bright center
            g2d.setColor(new Color(130, 200, 255, 255));
            g2d.fill(new Ellipse2D.Double(px - partR / 2.0, py - partR / 2.0, partR, partR));
        }
    }

    // ─── Nodes ─────────────────────────────────────

    private void paintNode(Graphics2D g2d, Node node) {
        double x = node.getX();
        double y = node.getY();

        boolean isHovered = node.equals(hoveredNode);
        boolean isSelected = node.isSelected();
        boolean isHighlighted = node.isHighlighted();

        Color primary;
        Color secondary;
        Color gradient;
        if (!node.isActive()) {
            primary = ThemeManager.COLOR_FAILED_ELEMENT;
            secondary = new Color(180, 180, 180);
            gradient = new Color(210, 210, 210);
        } else {
            primary = ThemeManager.getNodePrimaryColor(node.getType());
            secondary = ThemeManager.getNodeSecondaryColor(node.getType());
            gradient = ThemeManager.getNodeGradientColor(node.getType());
        }

        if (node.getType() == Node.NodeType.ESTACION) {
            paintStationNode(g2d, node, x, y, primary, secondary, gradient, isHovered, isSelected, isHighlighted);
        } else {
            paintRectNode(g2d, node, x, y, primary, secondary, gradient, isHovered, isSelected, isHighlighted);
        }
    }

    private void paintRectNode(Graphics2D g2d, Node node, double x, double y,
                                Color primary, Color secondary, Color gradient,
                                boolean isHovered, boolean isSelected, boolean isHighlighted) {
        int w = ThemeManager.NODE_WIDTH;
        int h = ThemeManager.NODE_HEIGHT;
        int r = ThemeManager.NODE_RADIUS;
        double drawX = x - w / 2.0;
        double drawY = y - h / 2.0;

        // Scale on hover
        AffineTransform old = g2d.getTransform();
        if (isHovered) {
            g2d.translate(x, y);
            g2d.scale(1.05, 1.05);
            g2d.translate(-x, -y);
        }

        RoundRectangle2D shape = new RoundRectangle2D.Double(drawX, drawY, w, h, r, r);

        // Shadow
        g2d.setColor(ThemeManager.SHADOW);
        g2d.fill(new RoundRectangle2D.Double(drawX + 3, drawY + 3, w, h, r, r));

        // Gradient fill
        GradientPaint gp = new GradientPaint(
                (float) drawX, (float) drawY, secondary,
                (float) drawX, (float) (drawY + h), gradient
        );
        g2d.setPaint(gp);
        g2d.fill(shape);

        // Border
        if (isHighlighted && node.getHighlightColor() != null) {
            g2d.setColor(node.getHighlightColor());
            g2d.setStroke(new BasicStroke(3));
        } else if (isSelected) {
            g2d.setColor(ThemeManager.WATER_PRIMARY);
            g2d.setStroke(new BasicStroke(2.5f));
        } else {
            g2d.setColor(primary);
            g2d.setStroke(new BasicStroke(1.5f));
        }
        g2d.draw(shape);

        // Warning Badge if Inactive
        if (!node.isActive()) {
            double bx = drawX + w - 18;
            double by = drawY + 4;
            g2d.setColor(ThemeManager.COLOR_ALERT_CRITICAL);
            g2d.fill(new Ellipse2D.Double(bx, by, 14, 14));
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
            FontMetrics metrics = g2d.getFontMetrics();
            g2d.drawString("!", (float)(bx + 7 - metrics.stringWidth("!") / 2.0), (float)(by + 11));
        }

        // Icon
        paintNodeIcon(g2d, node, x, y - 8);

        // Name
        g2d.setFont(ThemeManager.FONT_NODE_NAME);
        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        String name = node.getName();
        if (fm.stringWidth(name) > w - 10) {
            while (fm.stringWidth(name + "...") > w - 10 && name.length() > 3) {
                name = name.substring(0, name.length() - 1);
            }
            name += "...";
        }
        g2d.drawString(name, (float) (x - fm.stringWidth(name) / 2.0), (float) (y + 5));

        // Capacity/Demand label
        if (node.getType() != Node.NodeType.ESTACION) {
            g2d.setFont(ThemeManager.FONT_NODE_DETAIL);
            fm = g2d.getFontMetrics();
            String detail;
            if (node.isSource()) {
                detail = "Cap: " + (int) node.getCapacity() + " L/s";
            } else {
                detail = "Dem: " + (int) node.getCapacity() + " L/s";
            }
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.drawString(detail, (float) (x - fm.stringWidth(detail) / 2.0), (float) (y + 20));

            // Flow info (after simulation)
            if (node.getCurrentFlow() > 0) {
                String flowInfo = "Flujo: " + (int) node.getCurrentFlow() + " L/s";
                g2d.setColor(new Color(255, 255, 200));
                g2d.drawString(flowInfo, (float) (x - fm.stringWidth(flowInfo) / 2.0), (float) (y + 32));
            }
        }

        if (isHovered) {
            g2d.setTransform(old);
        }
    }

    private void paintStationNode(Graphics2D g2d, Node node, double x, double y,
                                   Color primary, Color secondary, Color gradient,
                                   boolean isHovered, boolean isSelected, boolean isHighlighted) {
        int radius = ThemeManager.ESTACION_RADIUS;

        AffineTransform old = g2d.getTransform();
        if (isHovered) {
            g2d.translate(x, y);
            g2d.scale(1.08, 1.08);
            g2d.translate(-x, -y);
        }

        Ellipse2D shape = new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);

        // Shadow
        g2d.setColor(ThemeManager.SHADOW);
        g2d.fill(new Ellipse2D.Double(x - radius + 3, y - radius + 3, radius * 2, radius * 2));

        // Gradient fill
        GradientPaint gp = new GradientPaint(
                (float) (x - radius), (float) (y - radius), secondary,
                (float) (x + radius), (float) (y + radius), gradient
        );
        g2d.setPaint(gp);
        g2d.fill(shape);

        // Border
        if (isHighlighted && node.getHighlightColor() != null) {
            g2d.setColor(node.getHighlightColor());
            g2d.setStroke(new BasicStroke(3));
        } else if (isSelected) {
            g2d.setColor(ThemeManager.WATER_PRIMARY);
            g2d.setStroke(new BasicStroke(2.5f));
        } else {
            g2d.setColor(primary);
            g2d.setStroke(new BasicStroke(1.5f));
        }
        g2d.draw(shape);

        // Warning Badge if Inactive
        if (!node.isActive()) {
            double bx = x + radius - 16;
            double by = y - radius + 2;
            g2d.setColor(ThemeManager.COLOR_ALERT_CRITICAL);
            g2d.fill(new Ellipse2D.Double(bx, by, 14, 14));
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
            FontMetrics metrics = g2d.getFontMetrics();
            g2d.drawString("!", (float)(bx + 7 - metrics.stringWidth("!") / 2.0), (float)(by + 11));
        }

        // Gear icon
        paintGearIcon(g2d, x, y - 3, 10);

        // Name below
        g2d.setFont(ThemeManager.FONT_NODE_NAME);
        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        String name = node.getName();
        if (fm.stringWidth(name) > radius * 2 - 6) {
            while (fm.stringWidth(name + "..") > radius * 2 - 6 && name.length() > 3) {
                name = name.substring(0, name.length() - 1);
            }
            name += "..";
        }
        g2d.drawString(name, (float) (x - fm.stringWidth(name) / 2.0), (float) (y + 12));

        if (isHovered) {
            g2d.setTransform(old);
        }
    }

    // ─── Node Icons ────────────────────────────────

    private void paintNodeIcon(Graphics2D g2d, Node node, double x, double y) {
        g2d.setColor(new Color(255, 255, 255, 180));

        if (node.getType() == Node.NodeType.EMBALSE) {
            // Water drop icon
            Path2D drop = new Path2D.Double();
            drop.moveTo(x, y - 10);
            drop.curveTo(x - 6, y - 2, x - 8, y + 4, x, y + 8);
            drop.curveTo(x + 8, y + 4, x + 6, y - 2, x, y - 10);
            g2d.fill(drop);
        } else if (node.getType() == Node.NodeType.BARRIO) {
            // House icon
            Path2D house = new Path2D.Double();
            house.moveTo(x - 10, y);
            house.lineTo(x, y - 10);
            house.lineTo(x + 10, y);
            house.closePath();
            g2d.fill(house);
            g2d.fillRect((int) x - 7, (int) y, 14, 10);
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.fillRect((int) x - 2, (int) y + 3, 4, 7);
        }
    }

    private void paintGearIcon(Graphics2D g2d, double cx, double cy, double r) {
        g2d.setColor(new Color(255, 255, 255, 180));

        int teeth = 6;
        Path2D gear = new Path2D.Double();
        for (int i = 0; i < teeth * 2; i++) {
            double angle = Math.PI * 2 * i / (teeth * 2);
            double rad = (i % 2 == 0) ? r : r * 0.65;
            double gx = cx + Math.cos(angle) * rad;
            double gy = cy + Math.sin(angle) * rad;
            if (i == 0) gear.moveTo(gx, gy);
            else gear.lineTo(gx, gy);
        }
        gear.closePath();
        g2d.fill(gear);

        // Center hole
        g2d.setColor(ThemeManager.ESTACION_PRIMARY);
        g2d.fill(new Ellipse2D.Double(cx - r * 0.3, cy - r * 0.3, r * 0.6, r * 0.6));
    }

    // ─── Connecting Line ───────────────────────────

    private void paintConnectingLine(Graphics2D g2d) {
        if (connectFrom == null || connectMousePos == null) return;

        Point2D mouseWorld = screenToWorld(connectMousePos);

        g2d.setColor(new Color(33, 150, 243, 150));
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1, new float[]{8, 4}, (float) animationTime * 0.05f));
        g2d.draw(new Line2D.Double(connectFrom.getX(), connectFrom.getY(),
                mouseWorld.getX(), mouseWorld.getY()));
    }

    // ─── Welcome Message ───────────────────────────

    private void paintWelcomeMessage(Graphics2D g2d) {
        g2d.setColor(ThemeManager.TEXT_SECONDARY);
        g2d.setFont(ThemeManager.FONT_SUBTITLE);
        String msg = "Cargue una red predeterminada o agregue nodos para comenzar";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
    }

    // ─── Overlay ───────────────────────────────────

    private void paintOverlay(Graphics2D g2d) {
        String zoomText = String.format("Zoom: %.0f%%", zoom * 100);
        g2d.setFont(ThemeManager.FONT_SMALL);
        g2d.setColor(ThemeManager.TEXT_SECONDARY);
        g2d.drawString(zoomText, 10, getHeight() - 10);

        // Glassmorphic Alerts Panel
        if (activeAlerts != null && !activeAlerts.isEmpty()) {
            int cardX = 16;
            int cardY = 16;
            int cardW = 280;
            int itemH = 20;
            int cardH = 32 + activeAlerts.size() * itemH;

            // Background shadow
            g2d.setColor(new Color(0, 0, 0, 15));
            g2d.fillRoundRect(cardX + 2, cardY + 2, cardW, cardH, 12, 12);

            // Glassmorphic Background (opaque white-ish with border)
            g2d.setColor(new Color(255, 255, 255, 220));
            g2d.fillRoundRect(cardX, cardY, cardW, cardH, 12, 12);
            g2d.setColor(new Color(210, 215, 225));
            g2d.setStroke(new BasicStroke(1.2f));
            g2d.drawRoundRect(cardX, cardY, cardW, cardH, 12, 12);

            // Title
            g2d.setFont(ThemeManager.FONT_BODY_BOLD);
            g2d.setColor(ThemeManager.BG_HEADER);
            g2d.drawString("Alertas del Sistema (" + activeAlerts.size() + ")", cardX + 12, cardY + 22);

            // Items
            g2d.setFont(ThemeManager.FONT_SMALL);
            int y = cardY + 42;
            for (String alert : activeAlerts) {
                if (alert.startsWith("🔴")) {
                    g2d.setColor(ThemeManager.COLOR_ALERT_CRITICAL);
                    g2d.drawString("●", cardX + 12, y);
                    g2d.setColor(ThemeManager.TEXT_PRIMARY);
                    g2d.drawString(alert.substring(2), cardX + 26, y);
                } else if (alert.startsWith("🟠")) {
                    g2d.setColor(ThemeManager.COLOR_ALERT_MEDIUM);
                    g2d.drawString("■", cardX + 12, y);
                    g2d.setColor(ThemeManager.TEXT_PRIMARY);
                    g2d.drawString(alert.substring(2), cardX + 26, y);
                } else if (alert.startsWith("🟡")) {
                    g2d.setColor(ThemeManager.COLOR_ALERT_WARNING);
                    g2d.drawString("▲", cardX + 12, y);
                    g2d.setColor(ThemeManager.TEXT_PRIMARY);
                    g2d.drawString(alert.substring(2), cardX + 26, y);
                } else {
                    g2d.setColor(ThemeManager.TEXT_SECONDARY);
                    g2d.drawString("-", cardX + 12, y);
                    g2d.setColor(ThemeManager.TEXT_PRIMARY);
                    g2d.drawString(alert, cardX + 26, y);
                }
                y += itemH;
            }
        }
    }

    // ═══════════════════════════════════════════════
    // VIEW HELPERS
    // ═══════════════════════════════════════════════

    /**
     * Centers and fits the graph in the viewport.
     */
    public void fitToView() {
        if (graph == null || graph.getNodes().isEmpty()) return;

        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (Node n : graph.getNodes()) {
            minX = Math.min(minX, n.getX());
            minY = Math.min(minY, n.getY());
            maxX = Math.max(maxX, n.getX());
            maxY = Math.max(maxY, n.getY());
        }

        double graphWidth = maxX - minX + 200;
        double graphHeight = maxY - minY + 200;

        double zoomX = getWidth() / graphWidth;
        double zoomY = getHeight() / graphHeight;
        zoom = Math.min(zoomX, zoomY) * 0.85;
        zoom = Math.max(0.3, Math.min(3.0, zoom));

        double centerX = (minX + maxX) / 2;
        double centerY = (minY + maxY) / 2;
        panX = getWidth() / 2.0 - centerX * zoom;
        panY = getHeight() / 2.0 - centerY * zoom;

        repaint();
    }
}
