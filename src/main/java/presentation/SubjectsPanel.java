package presentation;

import business.Subject;
import business.Classroom;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

public class SubjectsPanel extends JPanel {
    private final PresentationController controller;
    private final JTable table;
    private final SubjectsTableModel model;
    private final TableRowSorter<SubjectsTableModel> sorter;

    // Form fields
    private final JTextField txtId = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtAbrev = new JTextField();
    private final JTextField txtDepartamento = new JTextField();
    private final JTextField txtCurso = new JTextField();
    private final JComboBox<Classroom> cbAula = new JComboBox<>();
    private final JSpinner spHorasSemanales = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
    private final JSpinner spDuracion = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
    private final JSpinner spMaxDiarias = new JSpinner(new SpinnerNumberModel(0, 0, 24, 1));

    private final JButton btnGuardar = new JButton("Guardar cambios");
    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnEliminar = new JButton("Eliminar");

    public SubjectsPanel(PresentationController controller) {
        this.controller = Objects.requireNonNull(controller);
        setLayout(new BorderLayout(12, 12));

        model = new SubjectsTableModel(controller.getSubjects());
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

        btnGuardar.addActionListener(e -> saveEdits());
        btnNuevo.addActionListener(e -> addNewSubject());
        btnEliminar.addActionListener(e -> deleteSelectedSubject());

        refreshClassroomsCombo();

        if (model.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
            loadSelectedIntoForm();
        } else {
            clearForm();
        }
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout(8, 8));
        JTextField filter = new JTextField();
        filter.setToolTipText("Filtrar por nombre, departamento, curso o aula");
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
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
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
        addField(form, gbc, row++, "Departamento:", txtDepartamento, false);
        addField(form, gbc, row++, "Curso:", txtCurso, false);
        addField(form, gbc, row++, "Aula asignada:", cbAula, false);
        addField(form, gbc, row++, "Horas/sem:", spHorasSemanales, false);
        addField(form, gbc, row++, "Duración (horas):", spDuracion, false);
        addField(form, gbc, row++, "Máx. horas/día:", spMaxDiarias, false);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        JPanel buttons = new JPanel(new GridLayout(2, 2, 8, 8));;
        buttons.add(btnGuardar);
        buttons.add(btnNuevo);
        buttons.add(btnEliminar);
        form.add(buttons, gbc);

        form.setPreferredSize(new Dimension(460, 0));
        return form;
    }

    private void addField(JPanel form, GridBagConstraints gbc, int row, String label, JComponent comp, boolean readOnly) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 1;
        if (comp instanceof JTextField tf) tf.setEditable(!readOnly);
        form.add(comp, gbc);
    }

    private void refreshClassroomsCombo() {
        cbAula.removeAllItems();
        for (Classroom c : controller.getClassrooms()) cbAula.addItem(c);
        cbAula.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Classroom) setText(((Classroom) value).getName());
                return c;
            }
        });
    }

    private void clearForm() {
        txtId.setText("");
        txtNombre.setText("");
        txtAbrev.setText("");
        txtDepartamento.setText("");
        txtCurso.setText("");
        if (cbAula.getItemCount() > 0) cbAula.setSelectedIndex(0);
        spHorasSemanales.setValue(0);
        spDuracion.setValue(0);
        spMaxDiarias.setValue(0);
    }

    private void loadSelectedIntoForm() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { clearForm(); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Subject s = model.getAt(modelRow);

        txtId.setText(s.getId());
        txtNombre.setText(s.getName());
        txtAbrev.setText(s.getAbbreviation());
        txtDepartamento.setText(s.getDepartment() == null ? "" : s.getDepartment());
        txtCurso.setText(s.getCourse() == null ? "" : s.getCourse());
        spHorasSemanales.setValue(s.getWeeklyAssignedHours());
        spDuracion.setValue(s.getDuration());
        spMaxDiarias.setValue(s.getMaxDailyHours());

        // Aula asignada
        Classroom asg = s.getAssignedClassroom();
        if (asg != null) {
            for (int i = 0; i < cbAula.getItemCount(); i++) {
                Classroom it = cbAula.getItemAt(i);
                if (it != null && asg.getId().equals(it.getId())) {
                    cbAula.setSelectedIndex(i);
                    break;
                }
            }
        } else if (cbAula.getItemCount() > 0) {
            cbAula.setSelectedIndex(0);
        }
    }

    private void saveEdits() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Selecciona una asignatura."); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Subject s = model.getAt(modelRow);

        s.setName(txtNombre.getText().trim());
        s.setAbbreviation(txtAbrev.getText().trim());
        s.setDepartment(txtDepartamento.getText().trim());
        s.setCourse(txtCurso.getText().trim());
        Object aulaSel = cbAula.getSelectedItem();
        s.setAssignedClassroom(aulaSel instanceof Classroom ? (Classroom) aulaSel : null);
        s.setWeeklyAssignedHours((Integer) spHorasSemanales.getValue());
        s.setDuration((Integer) spDuracion.getValue());
        s.setMaxDailyHours((Integer) spMaxDiarias.getValue());

        controller.updateSubject(s);
        model.fireTableRowsUpdated(modelRow, modelRow);
        controller.refreshAllUI();
    }

    private void addNewSubject() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre de la asignatura:", "Nueva asignatura", JOptionPane.PLAIN_MESSAGE);
        if (nombre == null || nombre.isBlank()) return;
        String abrev = JOptionPane.showInputDialog(this, "Abreviatura:", nombre);
        if (abrev == null) return;
        String dep = JOptionPane.showInputDialog(this, "Departamento:", "");

        business.Subject s = new business.Subject(java.util.UUID.randomUUID().toString(), nombre, abrev);
        if (dep != null) s.setDepartment(dep);
        s.setCourse(txtCurso.getText().trim());
        Object aulaSel = cbAula.getSelectedItem();
        s.setAssignedClassroom(aulaSel instanceof Classroom ? (Classroom) aulaSel : null);
        s.setWeeklyAssignedHours((Integer) spHorasSemanales.getModel().getValue());
        s.setDuration((Integer) spDuracion.getModel().getValue());
        s.setMaxDailyHours((Integer) spMaxDiarias.getModel().getValue());

        controller.addNewSubject(s);
        model.reload(controller.getSubjects());
        int last = model.getRowCount() - 1;
        if (last >= 0) {
            table.setRowSelectionInterval(last, last);
            loadSelectedIntoForm();
        }
        controller.refreshAllUI();
    }

    private void deleteSelectedSubject() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Selecciona una asignatura."); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Subject s = model.getAt(modelRow);

        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar la asignatura \"" + s.getName() + "\"?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        controller.removeSubject(s.getId());
        model.reload(controller.getSubjects());
        clearForm();
        controller.refreshAllUI();
    }

    // Table model
    private static class SubjectsTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Nombre", "Abrev.", "Departamento", "Curso", "Aula", "Horas/sem", "Duración", "Máx/día"};
        private List<Subject> data;

        SubjectsTableModel(List<Subject> initial) { this.data = initial; }
        Subject getAt(int row) { return data.get(row); }
        void reload(List<Subject> newData) { this.data = newData; fireTableDataChanged(); }

        @Override public int getRowCount() { return data == null ? 0 : data.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }
        @Override public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 6,7,8 -> Integer.class;
                default -> String.class;
            };
        }
        @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            Subject s = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> s.getId();
                case 1 -> s.getName();
                case 2 -> s.getAbbreviation();
                case 3 -> s.getDepartment();
                case 4 -> s.getCourse();
                case 5 -> (s.getAssignedClassroom() == null ? "" : s.getAssignedClassroom().getName());
                case 6 -> s.getWeeklyAssignedHours();
                case 7 -> s.getDuration();
                case 8 -> s.getMaxDailyHours();
                default -> "";
            };
        }
    }
}
