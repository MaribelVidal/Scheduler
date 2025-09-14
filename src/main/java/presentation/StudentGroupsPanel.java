package presentation;

import business.StudentGroup;
import business.Teacher;
import business.Subject;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

public class StudentGroupsPanel extends JPanel {
    private final PresentationController controller;
    private final JTable table;
    private final SGTableModel model;
    private final TableRowSorter<SGTableModel> sorter;

    // Form fields
    private final JTextField txtId = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtAbrev = new JTextField();
    private final JTextField txtCourse = new JTextField();
    private final JComboBox<Teacher> cbTutor = new JComboBox<>();
    private final JSpinner spHoras = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
    private final JSpinner spNumAlumnos = new JSpinner(new SpinnerNumberModel(0, 0, 2000, 1));
    private final JTextField txtSubjectsCount = new JTextField();

    private final JButton btnEditSubjects = new JButton("Editar asignaturas...");
    private final JButton btnGuardar = new JButton("Guardar cambios");
    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnEliminar = new JButton("Eliminar");

    public StudentGroupsPanel(PresentationController controller) {
        this.controller = Objects.requireNonNull(controller);
        setLayout(new BorderLayout(12, 12));

        model = new SGTableModel(controller.getStudentGroups());
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
        btnNuevo.addActionListener(e -> addNewGroup());
        btnEliminar.addActionListener(e -> deleteSelectedGroup());

        refreshTutorsCombo();

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
        filter.setToolTipText("Filtrar por nombre, curso, tutor");
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
        addField(form, gbc, row++, "Curso:", txtCourse, false);
        addField(form, gbc, row++, "Tutor asignado:", cbTutor, false);
        addField(form, gbc, row++, "Horas semanales:", spHoras, false);
        addField(form, gbc, row++, "# Alumnos:", spNumAlumnos, false);
        addField(form, gbc, row++, "# Asignaturas requeridas:", txtSubjectsCount, true);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 3;
        JPanel buttons = new JPanel(new GridLayout(2, 2, 8, 8));
        buttons.add(btnEditSubjects);
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

    private void clearForm() {
        txtId.setText("");
        txtNombre.setText("");
        txtAbrev.setText("");
        txtCourse.setText("");
        if (cbTutor.getItemCount() > 0) cbTutor.setSelectedIndex(0);
        spHoras.setValue(0);
        spNumAlumnos.setValue(0);
        txtSubjectsCount.setText("0");
    }

    private void refreshTutorsCombo() {
        cbTutor.removeAllItems();
        for (Teacher t : controller.getTeachers()) cbTutor.addItem(t);
        cbTutor.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Teacher) setText(((Teacher) value).getName());
                return c;
            }
        });
    }

    private void loadSelectedIntoForm() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { clearForm(); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        StudentGroup g = model.getAt(modelRow);

        txtId.setText(g.getId());
        txtNombre.setText(g.getName());
        txtAbrev.setText(g.getAbbreviation());
        txtCourse.setText(g.getCourse() == null ? "" : g.getCourse());
        spHoras.setValue(g.getWeeklyGroupHours());
        spNumAlumnos.setValue(g.getNumberOfStudents());
        txtSubjectsCount.setText(String.valueOf(g.getRequiredSubjects() == null ? 0 : g.getRequiredSubjects().size()));


    }

    private void saveEdits() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Selecciona un grupo."); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        StudentGroup g = model.getAt(modelRow);

        g.setName(txtNombre.getText().trim());
        g.setAbbreviation(txtAbrev.getText().trim());
        g.setCourse(txtCourse.getText().trim());
        Object tutorSel = cbTutor.getSelectedItem();
        g.setTutor(tutorSel instanceof Teacher ? (Teacher) tutorSel : null);
        g.setWeeklyGroupHours((Integer) spHoras.getValue());
        g.setNumberOfStudents((Integer) spNumAlumnos.getValue());

        controller.updateStudentGroup(g);
        model.fireTableRowsUpdated(modelRow, modelRow);
        controller.refreshAllUI();
    }

    private void editSubjects() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Selecciona un grupo."); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        StudentGroup g = model.getAt(modelRow);
        java.util.List<Subject> all = controller.getSubjects();
        java.util.List<Subject> selected = new java.util.ArrayList<>(
                g.getRequiredSubjects() == null ? java.util.List.of() : g.getRequiredSubjects());

        java.util.List<Subject> newSelected = dualListDialog(
                this, "Asignaturas requeridas", all, selected, Subject::getName);

        if (newSelected != null) {
            g.setRequiredSubjects(newSelected);
            controller.updateStudentGroup(g);
            loadSelectedIntoForm();
            model.fireTableDataChanged();
            controller.refreshAllUI();
        }
    }

    private void addNewGroup() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre del grupo:", "Nuevo grupo", JOptionPane.PLAIN_MESSAGE);
        if (nombre == null || nombre.isBlank()) return;
        String abrev = JOptionPane.showInputDialog(this, "Abreviatura:", nombre);
        if (abrev == null) return;
        String curso = JOptionPane.showInputDialog(this, "Curso:", "");

        business.StudentGroup g = new business.StudentGroup(java.util.UUID.randomUUID().toString(), nombre, abrev);
        if (curso != null) g.setCourse(curso);
        Object tutorSel = cbTutor.getSelectedItem();
        g.setTutor(tutorSel instanceof Teacher ? (Teacher) tutorSel : null);
        g.setWeeklyGroupHours((Integer) spHoras.getModel().getValue());
        g.setNumberOfStudents((Integer) spNumAlumnos.getModel().getValue());

        controller.addNewStudentGroup(g);
        model.reload(controller.getStudentGroups());
        int last = model.getRowCount() - 1;
        if (last >= 0) {
            table.setRowSelectionInterval(last, last);
            loadSelectedIntoForm();
        }
        controller.refreshAllUI();
    }

    private void deleteSelectedGroup() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Selecciona un grupo."); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        StudentGroup g = model.getAt(modelRow);

        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar el grupo \"" + g.getName() + "\"?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        controller.removeStudentGroup(g.getId());
        model.reload(controller.getStudentGroups());
        clearForm();
        controller.refreshAllUI();
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
    private static class SGTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Nombre", "Abrev.", "Curso", "Tutor", "Horas/sem", "#Alumnos", "#Asignaturas"};
        private List<StudentGroup> data;

        SGTableModel(List<StudentGroup> initial) { this.data = initial; }
        StudentGroup getAt(int row) { return data.get(row); }
        void reload(List<StudentGroup> newData) { this.data = newData; fireTableDataChanged(); }

        @Override public int getRowCount() { return data == null ? 0 : data.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }
        @Override public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 5,6 -> Integer.class;
                default -> String.class;
            };
        }
        @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            StudentGroup g = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> g.getId();
                case 1 -> g.getName();
                case 2 -> g.getAbbreviation();
                case 3 -> g.getCourse();
                case 4 -> (g.getTutor() == null ? "" : g.getTutor().getName());
                case 5 -> g.getWeeklyGroupHours();
                case 6 -> g.getNumberOfStudents();
                case 7 -> (g.getRequiredSubjects() == null ? 0 : g.getRequiredSubjects().size());
                default -> "";
            };
        }
    }
}
