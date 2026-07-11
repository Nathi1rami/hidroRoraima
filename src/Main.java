import javax.swing.*;

/**
 * Main.java - Application Entry Point
 * HidroRoraima - Sistema de Distribucion de Agua Fluvial
 *
 * Modela la red de distribucion de agua usando el algoritmo de Flujo Maximo
 * Ford-Fulkerson (implementacion Edmonds-Karp) para optimizar la distribucion
 * de agua desde embalses hasta barrios sin superar la capacidad de presion.
 *
 * Arquitectura: MVC (Modelo-Vista-Controlador)
 *
 * MODELO:
 *   - Node.java         : Nodos del grafo (Embalse, Estacion, Barrio)
 *   - Edge.java         : Aristas/tuberias con capacidad y flujo
 *   - NetworkGraph.java  : Estructura del grafo con operaciones CRUD
 *   - FordFulkersonSolver.java : Algoritmo Edmonds-Karp
 *   - SimulationStep.java : Registro de pasos del algoritmo
 *   - FlowResult.java    : Resultados de la simulacion
 *
 * VISTA:
 *   - MainFrame.java     : Ventana principal
 *   - NetworkCanvas.java  : Canvas interactivo con animaciones
 *   - ControlPanel.java   : Panel lateral de control
 *   - NodeDialog.java     : Dialogo para agregar/editar nodos
 *   - EdgeDialog.java     : Dialogo para agregar/editar tuberias
 *   - ThemeManager.java   : Gestion centralizada del tema visual
 *
 * CONTROLADOR:
 *   - NetworkController.java : Coordinador MVC
 *
 * @author HidroRoraima Team
 * @version 1.0
 */
public class Main {
    public static void main(String[] args) {
        // Apply modern visual theme
        ThemeManager.applyGlobalTheme();

        // Launch application on Event Dispatch Thread (Swing threading)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Model
                NetworkGraph model = new NetworkGraph();

                // View
                MainFrame view = new MainFrame();

                // Controller (wires model and view together)
                NetworkController controller = new NetworkController(model, view);

                // Show the application window
                view.setVisible(true);
            }
        });
    }
}