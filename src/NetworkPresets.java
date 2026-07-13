import java.util.*;

/**
 * MODEL LAYER - NetworkPresets.java
 * Contains preset water distribution networks based on real Venezuelan geography
 * and a procedural network generator using Delaunay triangulation.
 *
 * Presets use geographic coordinates mapped through VenezuelaMapRenderer
 * so nodes align with the map background.
 *
 * @author HidroRoraima
 * @version 1.0
 */
public class NetworkPresets {

    /**
     * Available preset types.
     */
    public enum PresetType {
        RORAIMA_CLASSIC("Red Clasica Roraima",
                "Red original con 11 nodos. Embalses Guri, Caroni y Paragua.",
                11, 13, "Guayana"),

        GUAYANA("Red de Guayana",
                "Ciudad Guayana y Puerto Ordaz. Embalses Guri, Macagua y Caruachi.",
                15, 20, "Estado Bolivar"),

        CIUDAD_BOLIVAR("Red Ciudad Bolivar",
                "Red hidrica de Ciudad Bolivar con toma del Orinoco.",
                12, 16, "Ciudad Bolivar"),

        GRAN_SABANA("Red Gran Sabana",
                "Santa Elena de Uairen y comunidades indigenas con rios Kukenan y Aponwao.",
                10, 13, "Gran Sabana"),

        VENEZUELA_NACIONAL("Red Nacional de Venezuela",
                "Red completa con grandes embalses y principales ciudades del pais.",
                25, 35, "Venezuela"),

        PROCEDURAL_SMALL("Procedural Pequeña",
                "Red generada proceduralmente con 10-15 nodos.",
                12, 0, "Aleatoria"),

        PROCEDURAL_MEDIUM("Procedural Mediana",
                "Red generada proceduralmente con 20-30 nodos.",
                25, 0, "Aleatoria"),

        PROCEDURAL_LARGE("Procedural Grande",
                "Red generada proceduralmente con 50-75 nodos.",
                60, 0, "Aleatoria"),

        PROCEDURAL_EXTREME("Procedural Extrema",
                "Red generada proceduralmente con 100 nodos. Prueba de rendimiento.",
                100, 0, "Aleatoria");

        private final String displayName;
        private final String description;
        private final int approxNodes;
        private final int approxEdges;
        private final String region;

        PresetType(String displayName, String description, int approxNodes, int approxEdges, String region) {
            this.displayName = displayName;
            this.description = description;
            this.approxNodes = approxNodes;
            this.approxEdges = approxEdges;
            this.region = region;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public int getApproxNodes() { return approxNodes; }
        public int getApproxEdges() { return approxEdges; }
        public String getRegion() { return region; }
        public boolean isProcedural() { return name().startsWith("PROCEDURAL"); }
    }

    private NetworkPresets() {} // Utility class

    // ═══════════════════════════════════════════════
    // PUBLIC LOADER
    // ═══════════════════════════════════════════════

    /**
     * Loads a preset network into the given graph.
     * The graph is cleared before loading.
     */
    public static void loadPreset(NetworkGraph graph, PresetType type) {
        graph.clear();

        switch (type) {
            case RORAIMA_CLASSIC:
                loadRoraimaClassic(graph);
                break;
            case GUAYANA:
                loadGuayana(graph);
                break;
            case CIUDAD_BOLIVAR:
                loadCiudadBolivar(graph);
                break;
            case GRAN_SABANA:
                loadGranSabana(graph);
                break;
            case VENEZUELA_NACIONAL:
                loadVenezuelaNacional(graph);
                break;
            case PROCEDURAL_SMALL:
                generateProcedural(graph, 12, 800, 600);
                break;
            case PROCEDURAL_MEDIUM:
                generateProcedural(graph, 25, 900, 700);
                break;
            case PROCEDURAL_LARGE:
                generateProcedural(graph, 60, 1000, 750);
                break;
            case PROCEDURAL_EXTREME:
                generateProcedural(graph, 100, 1100, 800);
                break;
        }
    }

    // ═══════════════════════════════════════════════
    // PRESET: RORAIMA CLASSIC (original network)
    // ═══════════════════════════════════════════════

    private static void loadRoraimaClassic(NetworkGraph graph) {
        // Use geographic coordinates mapped through VenezuelaMapRenderer
        double gLon = -62.7, gLat = 7.3;  // Guri
        double cLon = -63.0, cLat = 6.5;  // Caroní mid
        double pLon = -63.5, pLat = 5.5;  // Paragua

        Node guri = graph.addNode("Embalse Guri",
                Node.NodeType.EMBALSE,
                VenezuelaMapRenderer.lonToX(gLon), VenezuelaMapRenderer.latToY(gLat), 50);
        Node caroni = graph.addNode("Embalse Caroni",
                Node.NodeType.EMBALSE,
                VenezuelaMapRenderer.lonToX(cLon), VenezuelaMapRenderer.latToY(cLat), 35);
        Node paragua = graph.addNode("Embalse Paragua",
                Node.NodeType.EMBALSE,
                VenezuelaMapRenderer.lonToX(pLon), VenezuelaMapRenderer.latToY(pLat), 25);

        Node estNorte = graph.addNode("Est. Bombeo Norte",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-63.0), VenezuelaMapRenderer.latToY(7.8));
        Node estCentral = graph.addNode("Est. Bombeo Central",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-63.5), VenezuelaMapRenderer.latToY(7.0));
        Node estSur = graph.addNode("Est. Bombeo Sur",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-62.0), VenezuelaMapRenderer.latToY(5.5));

        Node santaElena = graph.addNode("Santa Elena",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-61.1), VenezuelaMapRenderer.latToY(4.6), 20);
        Node boaVista = graph.addNode("Upata",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-62.4), VenezuelaMapRenderer.latToY(8.0), 25);
        Node ciudadBolivar = graph.addNode("Cd. Bolivar",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-63.5), VenezuelaMapRenderer.latToY(8.1), 30);
        Node puertoOrdaz = graph.addNode("Puerto Ordaz",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-62.7), VenezuelaMapRenderer.latToY(8.3), 20);
        Node tumeremo = graph.addNode("Tumeremo",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-61.5), VenezuelaMapRenderer.latToY(7.3), 15);

        graph.addEdge(guri, estNorte, 30);
        graph.addEdge(guri, estCentral, 25);
        graph.addEdge(caroni, estNorte, 20);
        graph.addEdge(caroni, estCentral, 30);
        graph.addEdge(paragua, estCentral, 15);
        graph.addEdge(paragua, estSur, 25);
        graph.addEdge(estNorte, boaVista, 18);
        graph.addEdge(estNorte, puertoOrdaz, 22);
        graph.addEdge(estCentral, ciudadBolivar, 15);
        graph.addEdge(estCentral, puertoOrdaz, 30);
        graph.addEdge(estCentral, tumeremo, 12);
        graph.addEdge(estSur, santaElena, 20);
        graph.addEdge(estSur, tumeremo, 18);
    }

    // ═══════════════════════════════════════════════
    // PRESET: GUAYANA (Ciudad Guayana/Puerto Ordaz)
    // ═══════════════════════════════════════════════

    private static void loadGuayana(NetworkGraph graph) {
        // Embalses
        Node guri = graph.addNode("Represa Guri",
                Node.NodeType.EMBALSE,
                VenezuelaMapRenderer.lonToX(-62.7), VenezuelaMapRenderer.latToY(7.3), 80);
        Node macagua = graph.addNode("Represa Macagua",
                Node.NodeType.EMBALSE,
                VenezuelaMapRenderer.lonToX(-62.6), VenezuelaMapRenderer.latToY(7.8), 45);
        Node caruachi = graph.addNode("Represa Caruachi",
                Node.NodeType.EMBALSE,
                VenezuelaMapRenderer.lonToX(-62.6), VenezuelaMapRenderer.latToY(7.6), 35);

        // Estaciones de Bombeo
        Node estAlta = graph.addNode("Est. Alta Vista",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-62.5), VenezuelaMapRenderer.latToY(8.0));
        Node estUnare = graph.addNode("Est. Unare",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-62.4), VenezuelaMapRenderer.latToY(8.2));
        Node estCastillito = graph.addNode("Est. Castillito",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-62.8), VenezuelaMapRenderer.latToY(8.1));
        Node estSanFelix = graph.addNode("Est. San Felix",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-62.3), VenezuelaMapRenderer.latToY(8.3));

        // Barrios
        Node ptoOrdaz = graph.addNode("Puerto Ordaz",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-62.7), VenezuelaMapRenderer.latToY(8.3), 30);
        Node sanFelix = graph.addNode("San Felix",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-62.3), VenezuelaMapRenderer.latToY(8.4), 25);
        Node altaVista = graph.addNode("Alta Vista",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-62.5), VenezuelaMapRenderer.latToY(8.4), 15);
        Node unare = graph.addNode("Unare",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-62.4), VenezuelaMapRenderer.latToY(8.5), 20);
        Node castillito = graph.addNode("Castillito",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-62.8), VenezuelaMapRenderer.latToY(8.4), 18);
        Node cdGuayana = graph.addNode("Cd. Guayana Centro",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-62.6), VenezuelaMapRenderer.latToY(8.2), 20);
        Node losOlivos = graph.addNode("Los Olivos",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-62.3), VenezuelaMapRenderer.latToY(8.1), 12);
        Node matanzas = graph.addNode("Zona Ind. Matanzas",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-62.8), VenezuelaMapRenderer.latToY(8.0), 15);

        // Tuberias
        graph.addEdge(guri, caruachi, 60);
        graph.addEdge(caruachi, macagua, 50);
        graph.addEdge(macagua, estAlta, 35);
        graph.addEdge(macagua, estUnare, 30);
        graph.addEdge(guri, estCastillito, 25);
        graph.addEdge(caruachi, estSanFelix, 20);
        graph.addEdge(estAlta, altaVista, 15);
        graph.addEdge(estAlta, cdGuayana, 20);
        graph.addEdge(estAlta, ptoOrdaz, 25);
        graph.addEdge(estUnare, unare, 18);
        graph.addEdge(estUnare, losOlivos, 12);
        graph.addEdge(estCastillito, castillito, 18);
        graph.addEdge(estCastillito, ptoOrdaz, 15);
        graph.addEdge(estCastillito, matanzas, 15);
        graph.addEdge(estSanFelix, sanFelix, 20);
        graph.addEdge(estSanFelix, cdGuayana, 10);
        graph.addEdge(macagua, estCastillito, 20);
        graph.addEdge(estAlta, matanzas, 10);
        graph.addEdge(caruachi, estAlta, 15);
        graph.addEdge(estSanFelix, losOlivos, 8);
    }

    // ═══════════════════════════════════════════════
    // PRESET: CIUDAD BOLÍVAR
    // ═══════════════════════════════════════════════

    private static void loadCiudadBolivar(NetworkGraph graph) {
        // Embalses
        Node tomaOrinoco = graph.addNode("Toma del Orinoco",
                Node.NodeType.EMBALSE,
                VenezuelaMapRenderer.lonToX(-63.5), VenezuelaMapRenderer.latToY(8.3), 60);
        Node embalseLlano = graph.addNode("Embalse Llano",
                Node.NodeType.EMBALSE,
                VenezuelaMapRenderer.lonToX(-63.8), VenezuelaMapRenderer.latToY(7.8), 30);

        // Estaciones
        Node estNorte = graph.addNode("Est. Angostura",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-63.5), VenezuelaMapRenderer.latToY(8.0));
        Node estSur = graph.addNode("Est. Vista al Sol",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-63.4), VenezuelaMapRenderer.latToY(7.8));
        Node estOeste = graph.addNode("Est. La Sabanita",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-63.7), VenezuelaMapRenderer.latToY(8.0));

        // Barrios
        Node cascoHistorico = graph.addNode("Casco Historico",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-63.6), VenezuelaMapRenderer.latToY(8.1), 15);
        Node vistaAlSol = graph.addNode("Vista al Sol",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-63.3), VenezuelaMapRenderer.latToY(7.9), 20);
        Node laSabanita = graph.addNode("La Sabanita",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-63.8), VenezuelaMapRenderer.latToY(8.1), 18);
        Node medina = graph.addNode("Medina Angarita",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-63.4), VenezuelaMapRenderer.latToY(8.2), 22);
        Node mariposa = graph.addNode("La Mariposa",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-63.2), VenezuelaMapRenderer.latToY(8.0), 12);
        Node perroSeco = graph.addNode("Perro Seco",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-63.6), VenezuelaMapRenderer.latToY(7.7), 10);
        Node marhuanta = graph.addNode("Marhuanta",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-63.3), VenezuelaMapRenderer.latToY(7.6), 14);

        // Tuberias
        graph.addEdge(tomaOrinoco, estNorte, 40);
        graph.addEdge(tomaOrinoco, estOeste, 30);
        graph.addEdge(embalseLlano, estSur, 25);
        graph.addEdge(embalseLlano, estOeste, 20);
        graph.addEdge(estNorte, cascoHistorico, 15);
        graph.addEdge(estNorte, medina, 22);
        graph.addEdge(estSur, vistaAlSol, 18);
        graph.addEdge(estSur, mariposa, 12);
        graph.addEdge(estSur, marhuanta, 14);
        graph.addEdge(estOeste, laSabanita, 18);
        graph.addEdge(estOeste, cascoHistorico, 10);
        graph.addEdge(estOeste, perroSeco, 10);
        graph.addEdge(estNorte, vistaAlSol, 15);
        graph.addEdge(estSur, perroSeco, 8);
        graph.addEdge(tomaOrinoco, estSur, 15);
        graph.addEdge(estNorte, mariposa, 10);
    }

    // ═══════════════════════════════════════════════
    // PRESET: GRAN SABANA
    // ═══════════════════════════════════════════════

    private static void loadGranSabana(NetworkGraph graph) {
        // Embalses (ríos de la Gran Sabana)
        Node rioKukenan = graph.addNode("Rio Kukenan",
                Node.NodeType.EMBALSE,
                VenezuelaMapRenderer.lonToX(-61.0), VenezuelaMapRenderer.latToY(5.2), 25);
        Node rioAponwao = graph.addNode("Rio Aponwao",
                Node.NodeType.EMBALSE,
                VenezuelaMapRenderer.lonToX(-61.8), VenezuelaMapRenderer.latToY(5.8), 20);

        // Estaciones
        Node estSantaElena = graph.addNode("Est. Santa Elena",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-61.0), VenezuelaMapRenderer.latToY(4.8));
        Node estKavanayen = graph.addNode("Est. Kavanayen",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-61.8), VenezuelaMapRenderer.latToY(5.3));
        Node estWonken = graph.addNode("Est. Wonken",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-61.5), VenezuelaMapRenderer.latToY(5.5));

        // Barrios / comunidades
        Node santaElena = graph.addNode("Santa Elena Uairen",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-61.1), VenezuelaMapRenderer.latToY(4.5), 15);
        Node sanRafael = graph.addNode("San Rafael Kamoiran",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-61.4), VenezuelaMapRenderer.latToY(4.8), 8);
        Node kavanayen = graph.addNode("Kavanayen",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-61.8), VenezuelaMapRenderer.latToY(5.6), 10);
        Node wonken = graph.addNode("Wonken",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-61.6), VenezuelaMapRenderer.latToY(5.8), 6);
        Node paratepui = graph.addNode("Paraitepui",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-61.5), VenezuelaMapRenderer.latToY(5.1), 5);

        // Tuberias
        graph.addEdge(rioKukenan, estSantaElena, 20);
        graph.addEdge(rioKukenan, estWonken, 15);
        graph.addEdge(rioAponwao, estKavanayen, 18);
        graph.addEdge(rioAponwao, estWonken, 12);
        graph.addEdge(estSantaElena, santaElena, 15);
        graph.addEdge(estSantaElena, sanRafael, 8);
        graph.addEdge(estSantaElena, paratepui, 5);
        graph.addEdge(estKavanayen, kavanayen, 10);
        graph.addEdge(estKavanayen, wonken, 6);
        graph.addEdge(estWonken, wonken, 6);
        graph.addEdge(estWonken, paratepui, 5);
        graph.addEdge(estWonken, sanRafael, 7);
        graph.addEdge(rioKukenan, estKavanayen, 10);
    }

    // ═══════════════════════════════════════════════
    // PRESET: VENEZUELA NACIONAL
    // ═══════════════════════════════════════════════

    private static void loadVenezuelaNacional(NetworkGraph graph) {
        // ─── Grandes Embalses ──────────────────────
        Node guri = graph.addNode("Embalse Guri",
                Node.NodeType.EMBALSE,
                VenezuelaMapRenderer.lonToX(-62.7), VenezuelaMapRenderer.latToY(7.3), 100);
        Node uribante = graph.addNode("Embalse Uribante",
                Node.NodeType.EMBALSE,
                VenezuelaMapRenderer.lonToX(-71.5), VenezuelaMapRenderer.latToY(7.8), 60);
        Node camatagua = graph.addNode("Embalse Camatagua",
                Node.NodeType.EMBALSE,
                VenezuelaMapRenderer.lonToX(-67.0), VenezuelaMapRenderer.latToY(9.5), 45);
        Node taguaza = graph.addNode("Embalse Taguaza",
                Node.NodeType.EMBALSE,
                VenezuelaMapRenderer.lonToX(-66.5), VenezuelaMapRenderer.latToY(10.2), 35);
        Node tuleFa = graph.addNode("Embalse Tule",
                Node.NodeType.EMBALSE,
                VenezuelaMapRenderer.lonToX(-71.0), VenezuelaMapRenderer.latToY(10.2), 30);

        // ─── Estaciones de Bombeo ──────────────────
        Node estCaracas = graph.addNode("Est. Caracas",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-66.9), VenezuelaMapRenderer.latToY(10.3));
        Node estValencia = graph.addNode("Est. Valencia",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-68.0), VenezuelaMapRenderer.latToY(10.0));
        Node estMaracaibo = graph.addNode("Est. Maracaibo",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-71.6), VenezuelaMapRenderer.latToY(10.5));
        Node estOriente = graph.addNode("Est. Oriente",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-64.2), VenezuelaMapRenderer.latToY(10.3));
        Node estGuayana = graph.addNode("Est. Guayana",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-62.7), VenezuelaMapRenderer.latToY(8.1));
        Node estLlanos = graph.addNode("Est. Llanos",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-67.8), VenezuelaMapRenderer.latToY(8.5));
        Node estBarquisimeto = graph.addNode("Est. Barquisimeto",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-69.3), VenezuelaMapRenderer.latToY(9.8));
        Node estAndes = graph.addNode("Est. Andes",
                Node.NodeType.ESTACION,
                VenezuelaMapRenderer.lonToX(-71.1), VenezuelaMapRenderer.latToY(8.5));

        // ─── Ciudades (Barrios) ────────────────────
        Node caracas = graph.addNode("Caracas",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-66.9), VenezuelaMapRenderer.latToY(10.5), 50);
        Node maracaibo = graph.addNode("Maracaibo",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-71.6), VenezuelaMapRenderer.latToY(10.7), 35);
        Node valencia = graph.addNode("Valencia",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-68.0), VenezuelaMapRenderer.latToY(10.2), 30);
        Node barquisimeto = graph.addNode("Barquisimeto",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-69.3), VenezuelaMapRenderer.latToY(10.1), 25);
        Node cdBolivar = graph.addNode("Cd. Bolivar",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-63.5), VenezuelaMapRenderer.latToY(8.1), 20);
        Node ptoOrdaz = graph.addNode("Puerto Ordaz",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-62.7), VenezuelaMapRenderer.latToY(8.3), 22);
        Node barcelona = graph.addNode("Barcelona",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-64.7), VenezuelaMapRenderer.latToY(10.2), 18);
        Node cumana = graph.addNode("Cumana",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-64.2), VenezuelaMapRenderer.latToY(10.5), 15);
        Node merida = graph.addNode("Merida",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-71.1), VenezuelaMapRenderer.latToY(8.6), 12);
        Node sanFernando = graph.addNode("San Fernando",
                Node.NodeType.BARRIO,
                VenezuelaMapRenderer.lonToX(-67.5), VenezuelaMapRenderer.latToY(7.9), 10);

        // ─── Tuberias nacionales ───────────────────
        // Guri -> Guayana
        graph.addEdge(guri, estGuayana, 80);
        graph.addEdge(estGuayana, ptoOrdaz, 30);
        graph.addEdge(estGuayana, cdBolivar, 25);

        // Camatagua/Taguaza -> Caracas
        graph.addEdge(camatagua, estCaracas, 35);
        graph.addEdge(taguaza, estCaracas, 30);
        graph.addEdge(estCaracas, caracas, 50);

        // Valencia supply
        graph.addEdge(camatagua, estValencia, 25);
        graph.addEdge(estValencia, valencia, 30);
        graph.addEdge(estValencia, estCaracas, 15);

        // Barquisimeto
        graph.addEdge(camatagua, estBarquisimeto, 20);
        graph.addEdge(estBarquisimeto, barquisimeto, 25);
        graph.addEdge(estBarquisimeto, estValencia, 12);

        // Maracaibo
        graph.addEdge(tuleFa, estMaracaibo, 30);
        graph.addEdge(estMaracaibo, maracaibo, 35);

        // Uribante -> Andes -> Merida
        graph.addEdge(uribante, estAndes, 40);
        graph.addEdge(estAndes, merida, 12);
        graph.addEdge(estAndes, estMaracaibo, 20);
        graph.addEdge(estAndes, estLlanos, 15);

        // Llanos
        graph.addEdge(camatagua, estLlanos, 15);
        graph.addEdge(estLlanos, sanFernando, 10);

        // Oriente
        graph.addEdge(guri, estOriente, 30);
        graph.addEdge(estOriente, barcelona, 18);
        graph.addEdge(estOriente, cumana, 15);

        // Cross connections
        graph.addEdge(estValencia, barquisimeto, 10);
        graph.addEdge(estLlanos, estGuayana, 10);
        graph.addEdge(estOriente, cdBolivar, 10);
        graph.addEdge(tuleFa, estAndes, 15);
        graph.addEdge(uribante, estBarquisimeto, 15);
    }

    // ═══════════════════════════════════════════════
    // PROCEDURAL GENERATOR
    // ═══════════════════════════════════════════════

    /**
     * Generates a procedural network using simplified Delaunay triangulation.
     *
     * @param graph      Target graph (will be cleared)
     * @param numNodes   Number of nodes to generate
     * @param areaWidth  Width of the generation area in world coordinates
     * @param areaHeight Height of the generation area in world coordinates
     */
    public static void generateProcedural(NetworkGraph graph, int numNodes, double areaWidth, double areaHeight) {
        Random rand = new Random();
        graph.clear();

        double offsetX = 100;
        double offsetY = 80;

        // ─── Distribute nodes by type ──────────────
        int numEmbalses = Math.max(2, (int) (numNodes * 0.15));
        int numEstaciones = Math.max(2, (int) (numNodes * 0.25));
        int numBarrios = numNodes - numEmbalses - numEstaciones;

        List<Node> allNodes = new ArrayList<Node>();

        // Embalses: top zone (y: 0-25% of area)
        for (int i = 0; i < numEmbalses; i++) {
            double x = offsetX + rand.nextDouble() * areaWidth;
            double y = offsetY + rand.nextDouble() * (areaHeight * 0.25);
            double cap = 20 + rand.nextInt(60);
            String name = "Embalse " + (char) ('A' + i);
            Node n = graph.addNode(name, Node.NodeType.EMBALSE, x, y, cap);
            allNodes.add(n);
        }

        // Estaciones: middle zone (y: 25-60% of area)
        for (int i = 0; i < numEstaciones; i++) {
            double x = offsetX + rand.nextDouble() * areaWidth;
            double y = offsetY + areaHeight * 0.25 + rand.nextDouble() * (areaHeight * 0.35);
            String name = "Estacion " + (i + 1);
            Node n = graph.addNode(name, Node.NodeType.ESTACION, x, y);
            allNodes.add(n);
        }

        // Barrios: bottom zone (y: 50-100% of area)
        for (int i = 0; i < numBarrios; i++) {
            double x = offsetX + rand.nextDouble() * areaWidth;
            double y = offsetY + areaHeight * 0.50 + rand.nextDouble() * (areaHeight * 0.50);
            double dem = 10 + rand.nextInt(30);
            String name = "Barrio " + (i + 1);
            Node n = graph.addNode(name, Node.NodeType.BARRIO, x, y, dem);
            allNodes.add(n);
        }

        // ─── Delaunay triangulation (simplified) ───
        // We connect nodes to their nearest neighbors forming a graph that
        // approximates the Delaunay triangulation
        delaunayConnect(graph, allNodes, rand);
    }

    /**
     * Simplified Delaunay-like connectivity:
     * For each node, find 2-4 nearest neighbors and create edges.
     * Ensures directional flow (sources → stations → sinks) and connectivity.
     */
    private static void delaunayConnect(NetworkGraph graph, List<Node> allNodes, Random rand) {
        int n = allNodes.size();

        // Compute distance matrix
        double[][] dist = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double dx = allNodes.get(i).getX() - allNodes.get(j).getX();
                double dy = allNodes.get(i).getY() - allNodes.get(j).getY();
                dist[i][j] = Math.sqrt(dx * dx + dy * dy);
            }
        }

        // Separate nodes by type
        List<Integer> embalses = new ArrayList<Integer>();
        List<Integer> estaciones = new ArrayList<Integer>();
        List<Integer> barrios = new ArrayList<Integer>();

        for (int i = 0; i < n; i++) {
            switch (allNodes.get(i).getType()) {
                case EMBALSE: embalses.add(i); break;
                case ESTACION: estaciones.add(i); break;
                case BARRIO: barrios.add(i); break;
            }
        }

        // 1. Connect each embalse to 1-3 nearest estaciones
        for (int ei : embalses) {
            List<Integer> sorted = sortByDistance(dist, ei, estaciones);
            int connections = Math.min(sorted.size(), 1 + rand.nextInt(3));
            for (int k = 0; k < connections; k++) {
                double cap = 15 + rand.nextInt(40);
                graph.addEdge(allNodes.get(ei), allNodes.get(sorted.get(k)), cap);
            }
        }

        // 2. Connect each estacion to 1-3 nearest barrios
        for (int si : estaciones) {
            List<Integer> sorted = sortByDistance(dist, si, barrios);
            int connections = Math.min(sorted.size(), 1 + rand.nextInt(3));
            for (int k = 0; k < connections; k++) {
                double cap = 10 + rand.nextInt(25);
                graph.addEdge(allNodes.get(si), allNodes.get(sorted.get(k)), cap);
            }
        }

        // 3. Inter-estacion connections (some stations connect to each other)
        for (int si : estaciones) {
            List<Integer> sorted = sortByDistance(dist, si, estaciones);
            // Remove self
            sorted.remove(Integer.valueOf(si));
            if (!sorted.isEmpty() && rand.nextDouble() < 0.4) {
                double cap = 10 + rand.nextInt(20);
                graph.addEdge(allNodes.get(si), allNodes.get(sorted.get(0)), cap);
            }
        }

        // 4. Ensure every barrio has at least one incoming edge
        for (int bi : barrios) {
            boolean hasIncoming = false;
            for (Edge e : graph.getEdges()) {
                if (e.getTo().equals(allNodes.get(bi))) {
                    hasIncoming = true;
                    break;
                }
            }
            if (!hasIncoming) {
                // Connect from nearest estacion
                List<Integer> sorted = sortByDistance(dist, bi, estaciones);
                if (!sorted.isEmpty()) {
                    double cap = 10 + rand.nextInt(20);
                    graph.addEdge(allNodes.get(sorted.get(0)), allNodes.get(bi), cap);
                }
            }
        }

        // 5. Ensure every embalse has at least one outgoing edge
        for (int ei : embalses) {
            boolean hasOutgoing = false;
            for (Edge e : graph.getEdges()) {
                if (e.getFrom().equals(allNodes.get(ei))) {
                    hasOutgoing = true;
                    break;
                }
            }
            if (!hasOutgoing && !estaciones.isEmpty()) {
                List<Integer> sorted = sortByDistance(dist, ei, estaciones);
                double cap = 15 + rand.nextInt(30);
                graph.addEdge(allNodes.get(ei), allNodes.get(sorted.get(0)), cap);
            }
        }

        // 6. Add some direct embalse -> barrio edges for larger networks
        if (allNodes.size() > 20) {
            for (int ei : embalses) {
                if (rand.nextDouble() < 0.3) {
                    List<Integer> sorted = sortByDistance(dist, ei, barrios);
                    if (!sorted.isEmpty()) {
                        double cap = 10 + rand.nextInt(15);
                        graph.addEdge(allNodes.get(ei), allNodes.get(sorted.get(0)), cap);
                    }
                }
            }
        }
    }

    /**
     * Returns indices from 'candidates' sorted by distance from node 'fromIdx'.
     */
    private static List<Integer> sortByDistance(final double[][] dist, final int fromIdx, List<Integer> candidates) {
        List<Integer> sorted = new ArrayList<Integer>(candidates);
        Collections.sort(sorted, new java.util.Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return Double.compare(dist[fromIdx][a], dist[fromIdx][b]);
            }
        });
        return sorted;
    }
}
