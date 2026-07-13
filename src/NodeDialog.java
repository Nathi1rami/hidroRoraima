import javax.swing.*;
import java.awt.*;

/**
 * VIEW LAYER - NodeDialog.java
 * Modal dialog for adding or editing a node in the water distribution network.
 * Provides fields for name, type, and capacity/demand.
 *
 * @author HidroRoraima
 * @version 1.0
 */
public class NodeDialog extends JDialog {

    private JTextField nameField;
    private JComboBox<Node.NodeType> typeCombo;
    private JSpinner capacitySpinner;
    private JLabel capacityLabel;
    private JCheckBox activeCheck;
    private boolean confirmed = false;

    private String resultName;
    private Node.NodeType resultType;
    private double resultCapacity;
    private boolean resultActive = true;

    /**
     * Creates a dialog for adding a new node.
     */
    public NodeDialog(JFrame parent, Node.NodeType defaultType) {
        this(parent, null, defaultType);
    }

    /**
     * Creates a dialog for editing an existing node, or adding with a default type.
     */
    public NodeDialog(JFrame parent, Node existingNode, Node.NodeType defaultType) {
        super(parent, existingNode != null ? "Editar Nodo" : "Agregar Nodo", true);
        setSize(380, 310);
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
        JLabel titleLabel = new JLabel(existingNode != null ? "Editar Nodo" : "Agregar Nodo");
        titleLabel.setFont(ThemeManager.FONT_SUBTITLE);
        titleLabel.setForeground(ThemeManager.BG_HEADER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        gbc.gridwidth = 1;

        // Name field
        JLabel nameLbl = new JLabel("Nombre:");
        nameLbl.setFont(ThemeManager.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(nameLbl, gbc);

        nameField = new JTextField(20);
        nameField.setFont(ThemeManager.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
        mainPanel.add(nameField, gbc);
        gbc.weightx = 0;

        // Type selector
        JLabel typeLbl = new JLabel("Tipo:");
        typeLbl.setFont(ThemeManager.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(typeLbl, gbc);

        typeCombo = new JComboBox<Node.NodeType>(Node.NodeType.values());
        typeCombo.setFont(ThemeManager.FONT_BODY);
        typeCombo.setSelectedItem(defaultType != null ? defaultType : Node.NodeType.EMBALSE);
        gbc.gridx = 1; gbc.gridy = 2;
        mainPanel.add(typeCombo, gbc);

        // Capacity/Demand spinner
        capacityLabel = new JLabel("Capacidad (L/s):");
        capacityLabel.setFont(ThemeManager.FONT_BODY);
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(capacityLabel, gbc);

        capacitySpinner = new JSpinner(new SpinnerNumberModel(20.0, 0.0, 10000.0, 5.0));
        capacitySpinner.setFont(ThemeManager.FONT_BODY);
        gbc.gridx = 1; gbc.gridy = 3;
        mainPanel.add(capacitySpinner, gbc);

        // Update label when type changes
        typeCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                updateCapacityLabel();
            }
        });
        updateCapacityLabel();

        // Active Checkbox
        activeCheck = new JCheckBox("Activo (Operativo)", true);
        activeCheck.setFont(ThemeManager.FONT_BODY);
        activeCheck.setOpaque(false);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        mainPanel.add(activeCheck, gbc);

        // Populate fields if editing
        if (existingNode != null) {
            nameField.setText(existingNode.getName());
            typeCombo.setSelectedItem(existingNode.getType());
            capacitySpinner.setValue(existingNode.getCapacity());
            activeCheck.setSelected(existingNode.isActive());
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
                    resultName = nameField.getText().trim();
                    resultType = (Node.NodeType) typeCombo.getSelectedItem();
                    resultCapacity = (Double) capacitySpinner.getValue();
                    resultActive = activeCheck.isSelected();
                    confirmed = true;
                    dispose();
                }
            }
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(okBtn);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(16, 6, 0, 6);
        mainPanel.add(buttonPanel, gbc);

        setContentPane(mainPanel);
    }

    private void updateCapacityLabel() {
        Node.NodeType type = (Node.NodeType) typeCombo.getSelectedItem();
        if (type == Node.NodeType.EMBALSE) {
            capacityLabel.setText("Capacidad (L/s):");
            capacitySpinner.setEnabled(true);
        } else if (type == Node.NodeType.BARRIO) {
            capacityLabel.setText("Demanda (L/s):");
            capacitySpinner.setEnabled(true);
        } else {
            capacityLabel.setText("Capacidad (L/s):");
            capacitySpinner.setEnabled(false);
            capacitySpinner.setValue(0.0);
        }
    }

    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese un nombre.",
                    "Validacion", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    // ─── Results ───────────────────────────────────
    public boolean isConfirmed() { return confirmed; }
    public String getResultName() { return resultName; }
    public Node.NodeType getResultType() { return resultType; }
    public double getResultCapacity() { return resultCapacity; }
    public boolean getResultActive() { return resultActive; }
}
