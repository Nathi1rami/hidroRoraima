import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * VIEW LAYER - VenezuelaMapRenderer.java
 * Renders the map of Venezuela as background for the network canvas.
 *
 * Features:
 * - Real PNG map image loaded from file ("Mapa Venezuela.png")
 * - Semi-transparent rendering to not overpower the network graph
 * - Major rivers: Orinoco, Caroní, Paragua drawn as overlays
 * - City labels for geographic context
 * - Bolívar state outline highlight
 *
 * Coordinate system: World coordinates (matching NetworkCanvas)
 * Geographic mapping: Longitude/Latitude mapped to x/y canvas coordinates
 *
 * @author HidroRoraima
 * @version 1.1
 */
public class VenezuelaMapRenderer {

    private boolean visible = true;
    private BufferedImage mapImage;
    private boolean imageLoaded = false;
    private boolean imageLoadAttempted = false;

    // ═══════════════════════════════════════════════
    // COORDINATE MAPPING
    // ═══════════════════════════════════════════════

    // Geographic bounds of Venezuela (approximate)
    // Lon: -73.3 to -59.8  Lat: 0.6 to 12.2
    private static final double GEO_LON_MIN = -73.5;
    private static final double GEO_LON_MAX = -59.5;
    private static final double GEO_LAT_MIN = 0.5;
    private static final double GEO_LAT_MAX = 12.5;

    // World coordinate mapping (canvas space)
    // Maps to roughly 0-1200 x, 0-900 y
    private static final double WORLD_X_MIN = -50;
    private static final double WORLD_X_MAX = 1150;
    private static final double WORLD_Y_MIN = -50;
    private static final double WORLD_Y_MAX = 850;

    // ─── Image positioning calibration ─────────────
    // The PNG image has its own bounds. These offsets align the image pixels
    // with our geographic coordinate system so nodes land on correct locations.
    // The image covers the full Venezuela silhouette:
    //   Top-left corner corresponds to roughly (-73.4°, 12.3°)
    //   Bottom-right corner corresponds to roughly (-59.6°, 0.6°)
    // We compute the pixel-to-world mapping from these.

    // Image geographic coverage (matching the PNG extent)
    private static final double IMG_LON_LEFT   = -73.8;
    private static final double IMG_LON_RIGHT  = -58.5;
    private static final double IMG_LAT_TOP    = 13.5;
    private static final double IMG_LAT_BOTTOM = -0.5;

    /**
     * Converts geographic longitude to world X coordinate.
     */
    public static double lonToX(double lon) {
        double ratio = (lon - GEO_LON_MIN) / (GEO_LON_MAX - GEO_LON_MIN);
        return WORLD_X_MIN + ratio * (WORLD_X_MAX - WORLD_X_MIN);
    }

    /**
     * Converts geographic latitude to world Y coordinate (inverted: north = up = lower Y).
     */
    public static double latToY(double lat) {
        double ratio = (lat - GEO_LAT_MIN) / (GEO_LAT_MAX - GEO_LAT_MIN);
        return WORLD_Y_MAX - ratio * (WORLD_Y_MAX - WORLD_Y_MIN); // Invert Y axis
    }

    // ═══════════════════════════════════════════════
    // RIVERS
    // ═══════════════════════════════════════════════

    // Rio Orinoco (main course simplified)
    private static final double[][] RIO_ORINOCO = {
        {-62.0, 8.3},   // Delta
        {-62.5, 8.0},   // Near Ciudad Guayana
        {-63.0, 7.8},
        {-63.5, 7.5},   // Caicara
        {-64.5, 7.3},
        {-65.5, 7.3},
        {-66.5, 7.5},   // San Fernando de Apure area
        {-67.0, 7.0},
        {-67.5, 6.5},
        {-67.5, 6.0},   // Atures
        {-67.5, 5.0},   // Puerto Ayacucho area
        {-67.8, 4.0},   // Upper Orinoco
        {-67.5, 3.5},   // Source area
    };

    // Rio Caroní
    private static final double[][] RIO_CARONI = {
        {-62.6, 8.0},   // Mouth at Ciudad Guayana
        {-62.8, 7.3},   // Guri Dam area
        {-63.0, 6.5},   // Upper Caroní
        {-63.0, 6.0},
        {-62.5, 5.5},   // La Paragua
        {-62.0, 5.0},   // Upper reaches
        {-61.5, 4.5},   // Near Gran Sabana
    };

    // Rio Paragua
    private static final double[][] RIO_PARAGUA = {
        {-63.0, 6.5},   // Confluence with Caroní
        {-63.5, 6.0},
        {-63.5, 5.5},
        {-63.5, 4.5},   // Upstream
        {-63.5, 3.8},   // Near Roraima
    };

    // ═══════════════════════════════════════════════
    // BOLÍVAR STATE OUTLINE (simplified)
    // ═══════════════════════════════════════════════

    private static final double[][] BOLIVAR_STATE = {
        {-65.2, 7.5},   // NW corner (Orinoco)
        {-64.5, 7.3},
        {-63.5, 7.5},   // Caicara
        {-63.0, 7.8},
        {-62.5, 8.0},   // Ciudad Guayana
        {-62.0, 7.5},
        {-61.5, 6.8},
        {-61.0, 6.2},   // East border
        {-61.0, 5.0},
        {-61.0, 4.5},   // Santa Elena
        {-61.5, 4.0},   // South east
        {-62.0, 4.0},
        {-63.0, 3.5},   // Roraima
        {-63.5, 3.0},   // Gran Sabana south
        {-63.4, 2.5},
        {-63.5, 2.1},
        {-64.0, 2.0},   // SW corner
        {-64.5, 3.0},
        {-65.0, 4.0},
        {-65.5, 5.0},
        {-65.5, 6.0},
        {-65.2, 6.5},
        {-65.2, 7.5},   // Close
    };

    // ═══════════════════════════════════════════════
    // CITY LABELS
    // ═══════════════════════════════════════════════

    private static final Object[][] CITIES = {
        // {lon, lat, name, isCapital}
        {-66.9, 10.5, "Caracas", true},
        {-63.2, 8.3, "Cd. Guayana", false},
        {-63.5, 8.1, "Cd. Bolivar", false},
        {-62.7, 7.3, "Embalse Guri", false},
        {-61.1, 4.6, "Sta. Elena", false},
        {-62.4, 5.7, "La Paragua", false},
        {-67.6, 5.0, "Pto. Ayacucho", false},
        {-69.3, 10.1, "Barquisimeto", false},
        {-71.6, 10.7, "Maracaibo", false},
        {-63.3, 10.5, "Cumana", false},
        {-64.7, 10.2, "Barcelona", false},
        {-68.0, 10.2, "Valencia", false},
        {-67.9, 7.9, "San Fernando", false},
        {-63.0, 5.5, "Kamarata", false},
    };

    // ═══════════════════════════════════════════════
    // IMAGE LOADING
    // ═══════════════════════════════════════════════

    /**
     * Loads the Venezuela map PNG image.
     * Searches in multiple locations relative to the class file and working directory.
     */
    private void loadMapImage() {
        if (imageLoadAttempted) return;
        imageLoadAttempted = true;

        // Try multiple paths to find the image
        String[] searchPaths = {
            "Mapa Venezuela.png",
            "../Mapa Venezuela.png",
            "src/Mapa Venezuela.png",
            System.getProperty("user.dir") + "/Mapa Venezuela.png",
            System.getProperty("user.dir") + "/../Mapa Venezuela.png",
        };

        for (String path : searchPaths) {
            try {
                File file = new File(path);
                if (file.exists() && file.canRead()) {
                    mapImage = ImageIO.read(file);
                    if (mapImage != null) {
                        imageLoaded = true;
                        return;
                    }
                }
            } catch (Exception e) {
                // Try next path
            }
        }

        // If all paths failed, log it silently
        System.err.println("VenezuelaMapRenderer: Could not load 'Mapa Venezuela.png'. Using vector fallback.");
    }

    // ═══════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    /**
     * Renders the complete Venezuela map onto the given Graphics2D context.
     * Should be called AFTER the background but BEFORE the grid and nodes.
     * The Graphics2D should already have zoom/pan transforms applied.
     */
    public void paintMap(Graphics2D g2d) {
        if (!visible) return;

        // Lazy-load the image on first paint
        loadMapImage();

        // Save original state
        Composite originalComposite = g2d.getComposite();
        Stroke originalStroke = g2d.getStroke();

        // ─── Draw the PNG map image ────────────────
        if (imageLoaded && mapImage != null) {
            paintMapImage(g2d);
        } else {
            // Fallback: draw vector outline if image not available
            paintVectorFallback(g2d);
        }

        // ─── Bolívar State highlight ───────────────
        Path2D bolivarPath = buildPath(BOLIVAR_STATE);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.10f));
        g2d.setColor(new Color(33, 150, 243));
        g2d.fill(bolivarPath);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f));
        g2d.setColor(new Color(33, 150, 243));
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1.0f, new float[]{6f, 4f}, 0f));
        g2d.draw(bolivarPath);

        // ─── Rivers ────────────────────────────────
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        paintRiver(g2d, RIO_ORINOCO, 3.0f, new Color(33, 150, 243));
        paintRiver(g2d, RIO_CARONI, 2.0f, new Color(66, 165, 245));
        paintRiver(g2d, RIO_PARAGUA, 1.5f, new Color(100, 181, 246));

        // ─── City Labels ───────────────────────────
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        paintCityLabels(g2d);

        // ─── "Estado Bolívar" label ────────────────
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
        g2d.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 24));
        g2d.setColor(new Color(33, 150, 243));
        double bLabelX = lonToX(-64.0);
        double bLabelY = latToY(5.5);
        g2d.drawString("Estado Bolivar", (float) bLabelX, (float) bLabelY);

        // ─── Compass rose ──────────────────────────
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.20f));
        paintCompassRose(g2d, lonToX(-60.5), latToY(11.5), 30);

        // Restore
        g2d.setComposite(originalComposite);
        g2d.setStroke(originalStroke);
    }

    // ═══════════════════════════════════════════════
    // IMAGE RENDERING
    // ═══════════════════════════════════════════════

    /**
     * Draws the PNG map image, positioned and scaled to align with
     * the geographic coordinate system.
     */
    private void paintMapImage(Graphics2D g2d) {
        // Calculate where the image corners fall in world coordinates
        double imgWorldLeft   = lonToX(IMG_LON_LEFT);
        double imgWorldRight  = lonToX(IMG_LON_RIGHT);
        double imgWorldTop    = latToY(IMG_LAT_TOP);
        double imgWorldBottom = latToY(IMG_LAT_BOTTOM);

        double drawX = imgWorldLeft;
        double drawY = imgWorldTop;
        double drawW = imgWorldRight - imgWorldLeft;
        double drawH = imgWorldBottom - imgWorldTop;

        // Draw with transparency so the grid and network show through
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
        g2d.drawImage(mapImage,
                (int) drawX, (int) drawY,
                (int) (drawX + drawW), (int) (drawY + drawH),
                0, 0,
                mapImage.getWidth(), mapImage.getHeight(),
                null);

        // Draw a slightly more visible version with tint
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));

        // Create a tinted version using the water color
        BufferedImage tinted = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        // Instead of full tint, just add a subtle border effect
        g2d.setColor(new Color(28, 63, 96));
        g2d.setStroke(new BasicStroke(1.5f));

        // Country label
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.10f));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 48));
        g2d.setColor(new Color(28, 63, 96));
        double titleX = lonToX(-68.0);
        double titleY = latToY(9.0);
        g2d.drawString("VENEZUELA", (float) titleX, (float) titleY);
    }

    // ═══════════════════════════════════════════════
    // VECTOR FALLBACK (when image not found)
    // ═══════════════════════════════════════════════

    // Simplified Venezuela outline for fallback
    private static final double[][] VENEZUELA_OUTLINE = {
        {-72.2, 11.8}, {-71.6, 11.0}, {-71.8, 10.0}, {-72.3, 9.3},
        {-72.4, 8.4}, {-72.0, 7.5}, {-71.1, 7.0}, {-70.7, 7.1},
        {-70.1, 6.9}, {-69.3, 6.2}, {-68.5, 6.2}, {-67.8, 6.3},
        {-67.4, 6.0}, {-67.5, 5.5}, {-67.8, 4.6}, {-67.3, 3.5},
        {-66.8, 2.0}, {-66.0, 1.3}, {-65.5, 1.0}, {-64.2, 1.5},
        {-63.5, 2.1}, {-63.4, 2.5}, {-63.8, 3.0}, {-64.0, 3.8},
        {-63.3, 4.0}, {-62.0, 4.1}, {-61.4, 4.3}, {-61.0, 4.5},
        {-61.0, 5.0}, {-60.7, 5.2}, {-61.0, 6.0}, {-61.3, 6.5},
        {-61.8, 7.0}, {-62.0, 7.5}, {-62.3, 8.0}, {-61.0, 8.5},
        {-60.5, 9.0}, {-60.1, 9.8}, {-61.0, 10.2}, {-62.0, 10.6},
        {-63.0, 10.7}, {-63.5, 10.5}, {-64.2, 10.6}, {-65.0, 10.5},
        {-66.0, 10.5}, {-66.9, 10.4}, {-67.3, 10.7}, {-67.8, 10.5},
        {-68.0, 10.7}, {-68.5, 11.0}, {-69.0, 11.5}, {-69.6, 11.5},
        {-70.0, 11.8}, {-70.5, 12.2}, {-71.0, 12.0}, {-71.3, 11.7},
        {-71.6, 11.7}, {-72.2, 11.8},
    };

    private void paintVectorFallback(Graphics2D g2d) {
        Path2D countryPath = buildPath(VENEZUELA_OUTLINE);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
        g2d.setColor(new Color(28, 63, 96));
        g2d.fill(countryPath);

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        g2d.setColor(new Color(28, 63, 96));
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(countryPath);

        // Country label
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.10f));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 48));
        g2d.setColor(new Color(28, 63, 96));
        double titleX = lonToX(-68.0);
        double titleY = latToY(9.0);
        g2d.drawString("VENEZUELA", (float) titleX, (float) titleY);
    }

    // ═══════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════

    /**
     * Builds a Path2D from lon/lat coordinate pairs.
     */
    private Path2D buildPath(double[][] coords) {
        Path2D path = new Path2D.Double();
        for (int i = 0; i < coords.length; i++) {
            double x = lonToX(coords[i][0]);
            double y = latToY(coords[i][1]);
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.closePath();
        return path;
    }

    /**
     * Paints a river as a smooth curved line.
     */
    private void paintRiver(Graphics2D g2d, double[][] coords, float width, Color color) {
        if (coords.length < 2) return;

        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Path2D river = new Path2D.Double();
        double x0 = lonToX(coords[0][0]);
        double y0 = latToY(coords[0][1]);
        river.moveTo(x0, y0);

        for (int i = 1; i < coords.length - 1; i++) {
            double x1 = lonToX(coords[i][0]);
            double y1 = latToY(coords[i][1]);
            double x2 = lonToX(coords[i + 1][0]);
            double y2 = latToY(coords[i + 1][1]);
            double cx = (x1 + x2) / 2;
            double cy = (y1 + y2) / 2;
            river.quadTo(x1, y1, cx, cy);
        }
        double xLast = lonToX(coords[coords.length - 1][0]);
        double yLast = latToY(coords[coords.length - 1][1]);
        river.lineTo(xLast, yLast);

        g2d.draw(river);
    }

    /**
     * Paints city markers and labels.
     */
    private void paintCityLabels(Graphics2D g2d) {
        for (Object[] city : CITIES) {
            double lon = (Double) city[0];
            double lat = (Double) city[1];
            String name = (String) city[2];
            boolean isCapital = (Boolean) city[3];

            double x = lonToX(lon);
            double y = latToY(lat);

            // Dot
            int dotSize = isCapital ? 7 : 5;
            g2d.setColor(isCapital ? new Color(211, 47, 47) : new Color(28, 63, 96));
            g2d.fill(new Ellipse2D.Double(x - dotSize / 2.0, y - dotSize / 2.0, dotSize, dotSize));

            // Label
            g2d.setFont(isCapital
                    ? new Font("Segoe UI", Font.BOLD, 11)
                    : new Font("Segoe UI", Font.PLAIN, 9));
            g2d.setColor(new Color(28, 63, 96));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(name, (float) (x - fm.stringWidth(name) / 2.0), (float) (y - dotSize / 2.0 - 4));
        }
    }

    /**
     * Paints a simple compass rose (N arrow).
     */
    private void paintCompassRose(Graphics2D g2d, double cx, double cy, double size) {
        g2d.setColor(new Color(28, 63, 96));
        g2d.setStroke(new BasicStroke(1.5f));

        g2d.draw(new Line2D.Double(cx, cy - size, cx, cy + size));
        g2d.draw(new Line2D.Double(cx - size, cy, cx + size, cy));

        Path2D nArrow = new Path2D.Double();
        nArrow.moveTo(cx, cy - size - 5);
        nArrow.lineTo(cx - 4, cy - size + 5);
        nArrow.lineTo(cx + 4, cy - size + 5);
        nArrow.closePath();
        g2d.fill(nArrow);

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString("N", (float) (cx - fm.stringWidth("N") / 2.0), (float) (cy - size - 10));
    }
}
