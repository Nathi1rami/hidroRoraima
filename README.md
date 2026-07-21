# 💧 HidroRoraima

**Sistema de Simulación y Optimización de Distribución de Agua Fluvial**

> Modela redes hídricas de Venezuela como grafos dirigidos y calcula el flujo máximo de distribución de agua usando el algoritmo **Ford-Fulkerson (Edmonds-Karp)**.

---

## 📋 Descripción

HidroRoraima es una aplicación de escritorio desarrollada en **Java Swing** que permite construir, visualizar y analizar redes de distribución de agua fluvial. El sistema modela la infraestructura hídrica como un **grafo dirigido y ponderado**, donde los nodos representan embalses, estaciones de bombeo y barrios, y las aristas representan tuberías con capacidades máximas en litros por segundo (L/s).

El núcleo del sistema aplica el algoritmo **Edmonds-Karp** (variante BFS de Ford-Fulkerson) para calcular el flujo máximo desde múltiples fuentes hasta múltiples destinos, determinando cuánta agua puede distribuirse a cada comunidad sin exceder la capacidad de ninguna tubería.

---

## ✨ Características

- 🗺️ **Mapa georreferenciado** de Venezuela como fondo para redes geográficas reales
- 🏗️ **Constructor interactivo** de redes: agregar, editar y eliminar nodos y tuberías con clic
- ⚡ **Simulación completa** con el algoritmo Edmonds-Karp (O(V·E²))
- 🔢 **Modo paso a paso** para visualizar cada iteración del algoritmo
- 🔴 **Cuello de botella** resaltado automáticamente en rojo en cada paso
- 🟢 **Camino aumentante** resaltado en verde durante la simulación paso a paso
- 💧 **Animación de partículas** de agua proporcional al flujo calculado
- 📊 **Panel de estadísticas** con flujo máximo, oferta total, demanda total y satisfacción por barrio
- 🚨 **Sistema de alertas** con notificaciones visuales y sonoras ante fallas críticas
- 🌧️ **Eventos climáticos**: simulación de lluvia, sequía y pico de demanda con factores ajustables
- ⚠️ **Simulación de fallas** de nodos y tuberías individualmente o de forma aleatoria
- 🌐 **Redes preconfiguradas** basadas en geografía venezolana real
- 🔀 **Generador procedural** de redes de hasta 100 nodos

---

## 🗂️ Redes Preconfiguradas

| Preset | Nodos | Tuberías | Región |
|--------|-------|----------|--------|
| Red Clásica Roraima | 11 | 13 | Guayana |
| Red de Guayana | 15 | 20 | Estado Bolívar |
| Red Ciudad Bolívar | 12 | 16 | Ciudad Bolívar |
| Red Gran Sabana | 10 | 13 | Gran Sabana |
| Red Nacional de Venezuela | 25 | 35 | Venezuela |
| Procedural Pequeña | ~12 | — | Aleatoria |
| Procedural Mediana | ~25 | — | Aleatoria |
| Procedural Grande | ~60 | — | Aleatoria |
| Procedural Extrema | ~100 | — | Aleatoria |

---

## 🏛️ Arquitectura — Patrón MVC

```
src/
├── MODELO
│   ├── Node.java                # Nodo del grafo (Embalse, Estacion, Barrio)
│   ├── Edge.java                # Arista/tubería con capacidad y flujo
│   ├── NetworkGraph.java        # Grafo: operaciones CRUD, análisis, eventos
│   ├── FordFulkersonSolver.java # Algoritmo Edmonds-Karp (flujo máximo)
│   ├── FlowResult.java          # DTO inmutable con resultados de simulación
│   ├── SimulationStep.java      # DTO de un paso del algoritmo
│   └── NetworkPresets.java      # Fábrica de redes preconfiguradas y procedurales
│
├── VISTA
│   ├── MainFrame.java           # Ventana principal (layout general)
│   ├── NetworkCanvas.java       # Canvas interactivo con animaciones
│   ├── ControlPanel.java        # Panel lateral de controles y estadísticas
│   ├── NodeDialog.java          # Diálogo para agregar/editar nodos
│   ├── EdgeDialog.java          # Diálogo para agregar/editar tuberías
│   ├── PresetDialog.java        # Selector de redes preconfiguradas
│   ├── AlertDialog.java         # Diálogo de alertas críticas
│   ├── VenezuelaMapRenderer.java# Renderizador del mapa de Venezuela
│   └── ThemeManager.java        # Sistema de diseño centralizado
│
└── CONTROLADOR
    ├── NetworkController.java   # Mediador MVC central
    └── Main.java                # Punto de entrada
```

---

## 🧮 Algoritmo — Ford-Fulkerson (Edmonds-Karp)

El sistema resuelve el problema del **Flujo Máximo** en redes multi-fuente / multi-sumidero mediante la técnica del **super-nodo virtual**:

1. Se añade un **super-fuente virtual** conectado a todos los embalses (capacidad = oferta del embalse)
2. Se añade un **super-sumidero virtual** conectado desde todos los barrios (capacidad = demanda del barrio)
3. Se ejecuta **BFS** para encontrar el camino aumentante más corto con capacidad residual > 0
4. Se identifica el **cuello de botella** (arista de menor capacidad residual)
5. Se actualiza el flujo en todas las aristas del camino y en el **grafo residual**
6. Se repite hasta que no existan más caminos aumentantes

**Complejidad:** O(V · E²) — eficiente para redes de hasta 100+ nodos.

---

## ⚙️ Requisitos del Sistema

- **Java:** JRE 8 o superior (JDK para compilar)
- **Sistema Operativo:** Windows, Linux o macOS
- **RAM:** 256 MB mínimo
- **Pantalla:** Resolución mínima 1000 × 600 px

---

## 🚀 Compilación y Ejecución

### Compilar

```bash
javac -d bin src/*.java
```

### Ejecutar

```bash
java -cp bin Main
```

### Con IDE (IntelliJ IDEA / Eclipse)

1. Importar la carpeta `hidroRoraima` como proyecto Java
2. Marcar `src/` como Sources Root
3. Ejecutar `Main.java`

---

## 🎮 Guía Rápida de Uso

### Cargar una red predefinida
1. Clic en **"Cargar Red"** en el panel lateral derecho
2. Seleccionar un preset de la lista (ej. *Red Clásica Roraima*)
3. La red se carga sobre el mapa de Venezuela automáticamente

### Construir una red personalizada
1. Clic en **"Agregar Embalse"**, **"Agregar Estación"** o **"Agregar Barrio"**
2. Completar el nombre y la capacidad en el diálogo
3. Clic en **"Conectar"** y luego clic en nodo origen → nodo destino para añadir tuberías

### Ejecutar la simulación
- **"Simular"**: ejecuta el algoritmo completo y muestra el resultado final con animaciones
- **"Paso a Paso"**: ejecuta iteración por iteración; usar **"Siguiente"** / **"Anterior"** para navegar

### Simular escenarios
- Activar **Lluvia / Sequía / Pico de Demanda** con los controles deslizantes del panel
- Clic derecho sobre un nodo o tubería → **"Simular Falla"** para desactivar ese elemento
- **"Falla Aleatoria"**: desactiva un elemento al azar de la red
- **"Restaurar Todo"**: reactiva todos los elementos de la red

---

## 🎨 Sistema de Colores

| Color | Significado |
|-------|-------------|
| 🟢 Verde | Embalse activo / Camino aumentante |
| 🟣 Morado | Estación de bombeo |
| 🟠 Naranja | Barrio / destino |
| 🔵 Azul | Flujo de agua en tuberías |
| 🔴 Rojo | Cuello de botella / falla crítica |
| ⚫ Gris | Nodo o tubería inactiva |

---

## 📁 Estructura del Proyecto

```
hidroRoraima/
├── src/                     # Código fuente Java (18 clases)
├── bin/                     # Clases compiladas
├── Mapa Venezuela.png       # Imagen del mapa base
├── .gitignore
└── README.md
```

---

## 👥 Autores

**HidroRoraima Team** — Proyecto académico de Análisis y Diseño Orientado a Objetos

---

## 📄 Licencia

Proyecto académico — Todos los derechos reservados © 2026
