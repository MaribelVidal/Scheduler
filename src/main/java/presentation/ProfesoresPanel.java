
package presentation;

import business.Teacher;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * ProfesoresPanel (Presentation-aware)
 * -----------------------------------
 * Same UI as before, but now wired to PresentationController, which
 * delegates to BusinessController and holds a reference to Calendar.
 *
 * After add/edit/delete, we call presentationController.refreshTeachersUI()
 * so the Calendar can immediately refresh its "Profesores" dropdown.
 */
public class ProfesoresPanel extends JPanel {
    private final PresentationController presentationController;
    private final JTable table;
    private final TeacherTableModel model;
    private final TableRowSorter<TeacherTableModel> sorter;

    // Form fields
    private final JTextField txtId = new JTextField();
    private final JTextField txtNombre = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtTelefono = new JTextField();
    private final JTextField txtDepartamento = new JTextField();
    private final JSpinner spHoras = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
    private final JTextField txtAchieved = new JTextField();
    private final JTextField txtWeighted = new JTextField();

    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnGuardar = new JButton("Guardar cambios");
    private final JButton btnEliminar = new JButton("Eliminar");

    public ProfesoresPanel(PresentationController presentationController) {
        this.presentationController = Objects.requireNonNull(presentationController, "presentationController");

        setLayout(new BorderLayout(12, 12));

        // Table + model
        model = new TeacherTableModel(presentationController.getTeachers());
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setFillsViewportHeight(true);

        // Left: table
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Right: details form
        JPanel form = buildFormPanel();
        add(form, BorderLayout.EAST);

        // Top bar (optional filter)
        add(buildTopBar(), BorderLayout.NORTH);

        // Hook selection -> form
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    loadSelectedTeacherIntoForm();
                }
            }
        });

        // Actions
        btnNuevo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewTeacher();
            }
        });

        btnGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveEditsToSelectedTeacher();
            }
        });

        btnEliminar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedTeacher();
            }
        });

        // Initial state
        if (model.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
            loadSelectedTeacherIntoForm();
        } else {
            clearForm();
        }
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout(8, 8));
        JTextField filter = new JTextField();
        filter.setToolTipText("Filtrar por nombre, email o departamento");

        filter.addActionListener(e -> applyFilter(filter.getText()));
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
        if (text == null || text.isBlank()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel();
        form.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        // ID (read-only)
        addField(form, gbc, row++, "ID:", txtId, true);

        addField(form, gbc, row++, "Nombre:", txtNombre, false);
        addField(form, gbc, row++, "Email:", txtEmail, false);
        addField(form, gbc, row++, "Teléfono:", txtTelefono, false);
        addField(form, gbc, row++, "Departamento:", txtDepartamento, false);
        addField(form, gbc, row++, "Horas asignadas:", spHoras, false);
        addField(form, gbc, row++, "Condiciones logradas:", txtAchieved, true);
        addField(form, gbc, row++, "Condiciones ponderadas:", txtWeighted, true);

        // Buttons
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        form.add(btnNuevo, gbc);

        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.5;
        form.add(btnGuardar, gbc);

        gbc.gridx = 2; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.5;
        form.add(btnEliminar, gbc);

        // Give the form a fixed width so it looks like a side panel
        form.setPreferredSize(new Dimension(420, 0));
        return form;
    }

    private void addField(JPanel form, GridBagConstraints gbc, int row, String label, JComponent comp, boolean readOnly) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1; gbc.gridwidth = 2;
        if (comp instanceof JTextField tf) {
            tf.setEditable(!readOnly);
        }
        form.add(comp, gbc);
    }

    private void clearForm() {
        txtId.setText("");
        txtNombre.setText("");
        txtEmail.setText("");
        txtTelefono.setText("");
        txtDepartamento.setText("");
        spHoras.setValue(0);
        txtAchieved.setText("");
        txtWeighted.setText("");
    }

    private void loadSelectedTeacherIntoForm() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            clearForm();
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Teacher t = model.getAt(modelRow);

        txtId.setText(safe(t.getId()));
        txtNombre.setText(safe(t.getName()));
        txtEmail.setText(safe(t.getEmail()));
        txtTelefono.setText(safe(t.getPhone()));
        txtDepartamento.setText(safe(t.getDepartment()));
        spHoras.setValue(t.getHoursWork());
        // Non-editable, computed from the selected schedule elsewhere in the app
        txtAchieved.setText(String.valueOf(t.getAchievedConditions()));
        txtWeighted.setText(String.valueOf(t.getWeightedConditions()));

    }

    private void addNewTeacher() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre del profesor:", "Nuevo profesor", JOptionPane.PLAIN_MESSAGE);
        if (nombre == null || nombre.isBlank()) return;

        Teacher t = new Teacher(UUID.randomUUID().toString(), nombre.trim(), nombre.trim().substring(0, 2));
        t.setEmail("");
        t.setPhone("");
        t.setDepartment("");
        t.setHoursWork(0);

        presentationController.addNewTeacher(t);
        model.reload(presentationController.getTeachers());

        // Inform Calendar to refresh the 'Profesores' dropdown
        presentationController.refreshTeachersUI();

        int last = model.getRowCount() - 1;
        if (last >= 0) {
            table.setRowSelectionInterval(last, last);
            loadSelectedTeacherIntoForm();
        }
    }

    private void saveEditsToSelectedTeacher() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un profesor primero.", "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Teacher t = model.getAt(modelRow);

        // Apply edits
        t.setName(txtNombre.getText().trim());
        t.setEmail(txtEmail.getText().trim());
        t.setPhone(txtTelefono.getText().trim());
        t.setDepartment(txtDepartamento.getText().trim());
        t.setHoursWork((Integer) spHoras.getValue());

        // If you add a `updateTeacher(Teacher t)` to PresentationController/BusinessController, call it here.
        // presentationController.updateTeacher(t);

        model.fireTableRowsUpdated(modelRow, modelRow);

        // Names may have changed -> refresh Calendar combos
        presentationController.refreshTeachersUI();
    }

    private void deleteSelectedTeacher() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un profesor primero.", "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Teacher t = model.getAt(modelRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar al profesor \"" + t.getName() + "\"?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        presentationController.removeTeacher(t.getId());
        model.reload(presentationController.getTeachers());
        clearForm();

        // Inform Calendar to rebuild the 'Profesores' combo
        presentationController.refreshTeachersUI();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    // -------------------- Table model --------------------
    private static class TeacherTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Nombre", "Email", "Teléfono", "Departamento", "Horas"};
        private List<Teacher> data;

        public TeacherTableModel(List<Teacher> initial) {
            this.data = initial;
        }

        public void reload(List<Teacher> newData) {
            this.data = newData;
            fireTableDataChanged();
        }

        public Teacher getAt(int row) {
            return data.get(row);
        }

        @Override
        public int getRowCount() {
            return data == null ? 0 : data.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 5 -> Integer.class; // Horas
                default -> String.class;
            };
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            // We edit in the side form, not in the table
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Teacher t = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> t.getId();
                case 1 -> t.getName();
                case 2 -> t.getEmail();
                case 3 -> t.getPhone();
                case 4 -> t.getDepartment();
                case 5 -> t.getHoursWork();
                default -> "";
            };
        }
    }


    /** Call this when the selected schedule changes to refresh the metrics. */
    public void onScheduleChanged() {
        // Just reload the current selection to pull latest achieved/weighted from Teacher
        loadSelectedTeacherIntoForm();
    }
}