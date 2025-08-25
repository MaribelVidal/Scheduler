package presentation;

import business.Classroom;
import business.Subject;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

public class ClassroomsPanel extends JPanel {
    private final PresentationController controller;
    private final JTable table;
    private final ClassroomTableModel model;
    private final TableRowSorter<ClassroomTableModel> sorter;

    // Form fields
    private final JTextField txtId = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtAbrev = new JTextField();
    private final JComboBox<String> cbTipo = new JComboBox<>(new String[] {"general", "dedicada"});
    private final JSpinner spCapacidad = new JSpinner(new SpinnerNumberModel(30, 0, 1000, 1));
    private final JTextField txtAsignaturasCount = new JTextField();

    private final JButton btnEditSubjects = new JButton("Editar asignaturas...");
    private final JButton btnGuardar = new JButton("Guardar cambios");
    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnEliminar = new JButton("Eliminar");

    public ClassroomsPanel(PresentationController controller) {
        this.controller = java.util.Objects.requireNonNull(controller);
        setLayout(new BorderLayout(12, 12));

        model = new ClassroomTableModel(controller.getClassrooms());
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildFormPanel(), BorderLayout.EAST);

        table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) loadSelectedIntoForm();
        });

        btnEditSubjects.addActionListener(e -> editSubjects());
        btnGuardar.addActionListener(e -> saveEdits());
        btnNuevo.addActionListener(e -> onAddNewClassroom());
        btnEliminar.addActionListener(e -> onDeleteSelectedClassroom());

        if (model.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
            loadSelectedIntoForm();
        } else {
            clearForm();
        }
    }

    private void onAddNewClassroom() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre del aula:", "Nueva aula", JOptionPane.PLAIN_MESSAGE);
        if (nombre == null || nombre.isBlank()) return;
        String abrev = JOptionPane.showInputDialog(this, "Abreviatura:", nombre);
        if (abrev == null) return;
        String tipo = (String) JOptionPane.showInputDialog(this, "Tipo de aula:", "Nueva aula",
                JOptionPane.PLAIN_MESSAGE, null, new String[]{"general", "dedicada"}, "general");
        if (tipo == null) return;

        business.Classroom c = new business.Classroom(java.util.UUID.randomUUID().toString(), nombre, abrev);
        c.setClassroomType(tipo);
        c.setCapacity((Integer) spCapacidad.getModel().getValue());

        controller.addNewClassroom(c);        // <-- your PresentationController method
        model.reload(controller.getClassrooms());

        int last = model.getRowCount() - 1;
        if (last >= 0) {
            table.setRowSelectionInterval(last, last);
            loadSelectedIntoForm();
        }
    }

    private void onDeleteSelectedClassroom() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Selecciona un aula."); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        business.Classroom c = model.getAt(modelRow);

        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar el aula \"" + c.getName() + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        controller.removeClassroom(c.getId());
        model.reload(controller.getClassrooms());
        clearForm();
        controller.refreshAllUI();
    }


    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout(8, 8));
        JTextField filter = new JTextField();
        filter.setToolTipText("Filtrar por nombre, tipo o abrev.");
        filter.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(filter.getText()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(filter.getText()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(filter.getText()); }
        });
        top.add(new JLabel("Buscar:"), BorderLayout.WEST);
        top.add(filter, BorderLayout.CENTER);
        return top;
    }

    private void applyFilter(String text) {
        if (text == null || text.isBlank()) { sorter.setRowFilter(null); return; }
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,4,4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        int row = 0;

        addField(form, gbc, row++, "ID:", txtId, true);
        addField(form, gbc, row++, "Nombre:", txtNombre, false);
        addField(form, gbc, row++, "Abreviatura:", txtAbrev, false);
        addField(form, gbc, row++, "Tipo:", cbTipo, false);
        addField(form, gbc, row++, "Capacidad:", spCapacidad, false);
        addField(form, gbc, row++, "# Asignaturas:", txtAsignaturasCount, true);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        JPanel buttons = new JPanel(new GridLayout(2, 2, 8, 8));
        buttons.add(btnEditSubjects);
        buttons.add(btnGuardar);
        buttons.add(btnNuevo);
        buttons.add(btnEliminar);
        form.add(buttons, gbc);

        form.setPreferredSize(new Dimension(500, 0));
        return form;
    }

    private void addField(JPanel form, GridBagConstraints gbc, int row, String label, JComponent comp, boolean readOnly) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 1;
        if (comp instanceof JTextField tf) tf.setEditable(!readOnly);
        form.add(comp, gbc);
    }

    private void clearForm() {
        txtId.setText("");
        txtNombre.setText("");
        txtAbrev.setText("");
        cbTipo.setSelectedIndex(0);
        spCapacidad.setValue(30);
        txtAsignaturasCount.setText("0");
    }

    private void loadSelectedIntoForm() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { clearForm(); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Classroom c = model.getAt(modelRow);

        txtId.setText(c.getId());
        txtNombre.setText(c.getName());
        txtAbrev.setText(c.getAbbreviation());
        cbTipo.setSelectedItem(c.getClassroomType() == null ? "general" : c.getClassroomType());
        spCapacidad.setValue(c.getCapacity());
        txtAsignaturasCount.setText(String.valueOf(c.getAssignedSubjects() == null ? 0 : c.getAssignedSubjects().size()));
    }

    private void saveEdits() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Selecciona un aula."); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Classroom c = model.getAt(modelRow);

        c.setName(txtNombre.getText().trim());
        c.setAbbreviation(txtAbrev.getText().trim());
        c.setClassroomType((String) cbTipo.getSelectedItem());
        c.setCapacity((Integer) spCapacidad.getValue());

        controller.updateClassroom(c);
        model.fireTableRowsUpdated(modelRow, modelRow);
            controller.refreshAllUI();
}

    private void editSubjects() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Selecciona un aula."); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Classroom c = model.getAt(modelRow);

        java.util.List<Subject> all = controller.getSubjects();
        java.util.List<Subject> selected = new java.util.ArrayList<>(
                c.getAssignedSubjects() == null ? java.util.List.of() : c.getAssignedSubjects());

        java.util.List<Subject> newSelected = dualListDialog(
                this, "Asignaturas asignadas", all, selected, Subject::getName);

        if (newSelected != null) {
            c.setAssignedSubjects(newSelected);
            controller.updateClassroom(c);
            loadSelectedIntoForm();
            model.fireTableDataChanged();
                    controller.refreshAllUI();
}
    }

    // Reusable dual-list dialog
    private <T> java.util.List<T> dualListDialog(Component parent, String title,
                                                 java.util.List<T> allItems,
                                                 java.util.List<T> selectedItems,
                                                 java.util.function.Function<T, String> toLabel) {
        DefaultListModel<T> leftModel = new DefaultListModel<>();
        DefaultListModel<T> rightModel = new DefaultListModel<>();

        java.util.Set<T> selectedSet = new java.util.HashSet<>(selectedItems);
        for (T item : allItems) if (!selectedSet.contains(item)) leftModel.addElement(item);
        for (T item : selectedItems) rightModel.addElement(item);

        JList<T> left = new JList<>(leftModel);
        JList<T> right = new JList<>(rightModel);

        left.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(toLabel.apply((T) value));
                return c;
            }
        });
        right.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(toLabel.apply((T) value));
                return c;
            }
        });

        JButton toRight = new JButton("➜");
        JButton toLeft = new JButton("⬅");
        toRight.addActionListener(e -> {
            for (T sel : left.getSelectedValuesList()) {
                leftModel.removeElement(sel);
                rightModel.addElement(sel);
            }
        });
        toLeft.addActionListener(e -> {
            for (T sel : right.getSelectedValuesList()) {
                rightModel.removeElement(sel);
                leftModel.addElement(sel);
            }
        });

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1; gbc.weighty = 1;
        gbc.gridx = 0; gbc.gridy = 0; center.add(new JScrollPane(left), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JPanel arrows = new JPanel(new GridLayout(2,1,5,5));
        arrows.add(toRight); arrows.add(toLeft);
        center.add(arrows, gbc);
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 1; gbc.fill = GridBagConstraints.BOTH;
        center.add(new JScrollPane(right), gbc);

        int res = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(parent), center, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            java.util.List<T> out = new java.util.ArrayList<>();
            for (int i = 0; i < rightModel.size(); i++) out.add(rightModel.get(i));
            return out;
        }
        return null;
    }

    // Table model
    private static class ClassroomTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Nombre", "Abrev.", "Tipo", "Capacidad", "#Asignaturas"};
        private List<Classroom> data;

        ClassroomTableModel(List<Classroom> initial) { this.data = initial; }
        Classroom getAt(int row) { return data.get(row); }
        void reload(List<Classroom> newData) { this.data = newData; fireTableDataChanged(); }

        @Override public int getRowCount() { return data == null ? 0 : data.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }
        @Override public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 4 -> Integer.class; // Capacidad
                default -> String.class;
            };
        }
        @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            Classroom c = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> c.getId();
                case 1 -> c.getName();
                case 2 -> c.getAbbreviation();
                case 3 -> c.getClassroomType();
                case 4 -> c.getCapacity();
                case 5 -> (c.getAssignedSubjects() == null ? 0 : c.getAssignedSubjects().size());
                default -> "";
            };
        }
    }
}
