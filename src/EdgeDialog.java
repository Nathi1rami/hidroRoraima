import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * VIEW LAYER - EdgeDialog.java
 * Modal dialog for adding or editing a pipe (edge) in the water distribution network.
 * Provides fields for source node, destination node, and pipe capacity.
 *
 * @author HidroRoraima
 * @version 1.0
 */
public class EdgeDialog extends JDialog {

    private JComboBox<Node> fromCombo;
    private JComboBox<Node> toCombo;
    private JSpinner capacitySpinner;
    private boolean confirmed = false;

    private Node resultFrom;
    private Node resultTo;
    private double resultCapacity;

    /**
     * Creates a dialog for adding a new edge.
     */
    public EdgeDialog(JFrame parent, List<Node> nodes, Node preselectedFrom, Node preselectedTo) {
        this(parent, nodes, null, preselectedFrom, preselectedTo);
    }

    /**
     * Creates a dialog for editing an existing edge, or adding with preselected nodes.
     */
    public EdgeDialog(JFrame parent, List<Node> nodes, Edge existingEdge,
                      Node preselectedFrom, Node preselectedTo) {
        super(parent, existingEdge != null ? "Editar Tuberia" : "Agregar Tuberia", true);
        setSize(380, 260);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        mainPanel.setBackground(ThemeManager.BG_SECONDARY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel(existingEdge != null ? "Editar Tuberia" : "Agregar Tuberia");
        titleLabel.setFont(ThemeManager.FONT_SUBTITLE);
        titleLabel.setForeground(ThemeManager.BG_HEADER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        gbc.gridwidth = 1;

        // From node
        JLabel fromLbl = new JLabel("Desde:");
        fromLbl.setFont(ThemeManager.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(fromLbl, gbc);

        fromCombo = new JComboBox<Node>(nodes.toArray(new Node[0]));
        fromCombo.setFont(ThemeManager.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
        mainPanel.add(fromCombo, gbc);
        gbc.weightx = 0;

        // To node
        JLabel toLbl = new JLabel("Hasta:");
        toLbl.setFont(ThemeManager.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(toLbl, gbc);

        toCombo = new JComboBox<Node>(nodes.toArray(new Node[0]));
        toCombo.setFont(ThemeManager.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 2;
        mainPanel.add(toCombo, gbc);

        // Capacity
        JLabel capLbl = new JLabel("Capacidad (L/s):");
        capLbl.setFont(ThemeManager.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(capLbl, gbc);

        capacitySpinner = new JSpinner(new SpinnerNumberModel(20.0, 1.0, 10000.0, 5.0));
        capacitySpinner.setFont(ThemeManager.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 3;
        mainPanel.add(capacitySpinner, gbc);

        // Preselect or populate from existing
        if (existingEdge != null) {
            fromCombo.setSelectedItem(existingEdge.getFrom());
            toCombo.setSelectedItem(existingEdge.getTo());
            capacitySpinner.setValue(existingEdge.getCapacity());
            fromCombo.setEnabled(false);
            toCombo.setEnabled(false);
        } else {
            if (preselectedFrom != null) fromCombo.setSelectedItem(preselectedFrom);
            if (preselectedTo != null) toCombo.setSelectedItem(preselectedTo);
        }

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        JButton cancelBtn = ThemeManager.createStyledButton("Cancelar", new Color(158, 158, 158),
                new Color(117, 117, 117));
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                confirmed = false;
                dispose();
            }
        });

        JButton okBtn = ThemeManager.createStyledButton("Aceptar", ThemeManager.BTN_SUCCESS,
                ThemeManager.BTN_SUCCESS_HOVER);
        okBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (validateInput()) {
                    resultFrom = (Node) fromCombo.getSelectedItem();
                    resultTo = (Node) toCombo.getSelectedItem();
                    resultCapacity = (Double) capacitySpinner.getValue();
                    confirmed = true;
                    dispose();
                }
            }
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(okBtn);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(16, 6, 0, 6);
        mainPanel.add(buttonPanel, gbc);

        setContentPane(mainPanel);
    }

    private boolean validateInput() {
        Node from = (Node) fromCombo.getSelectedItem();
        Node to = (Node) toCombo.getSelectedItem();

        if (from == null || to == null) {
            JOptionPane.showMessageDialog(this, "Seleccione nodos validos.",
                    "Validacion", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (from.equals(to)) {
            JOptionPane.showMessageDialog(this, "Los nodos de origen y destino deben ser diferentes.",
                    "Validacion", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    // ─── Results ───────────────────────────────────
    public boolean isConfirmed() { return confirmed; }
    public Node getResultFrom() { return resultFrom; }
    public Node getResultTo() { return resultTo; }
    public double getResultCapacity() { return resultCapacity; }
}
