package presentation;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.*;
import business.Teacher;
import business.TimePeriod;
import business.Subject;
import business.StudentGroup;

public class ProfesoresPanel extends JPanel {

    private final PresentationController controller;
    private JTable table;
    private TeacherTableModel model;

    private JTextField tfNombre, tfAbrev, tfEmail, tfPhone, tfDepto;
    private JSpinner spHoras;
    private JLabel lblCondLogr, lblCondPond;

    private final JButton btnGuardar = new JButton("Guardar cambios");
    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnEliminar = new JButton("Eliminar");
    private final JButton btnEditPeriodos = new JButton("Editar períodos...");
    private final JButton btnEditAsignaturas = new JButton("Editar asignaturas...");
    private final JButton btnEditGrupos = new JButton("Editar grupos...");
    private final JButton btnEditPosibles = new JButton("Asignaturas posibles...");

    public ProfesoresPanel(PresentationController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        model = new TeacherTableModel(controller.getTeachers());
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int posiblesCol = model.columnIndex("Posibles");
        if (posiblesCol >= 0) {
            table.getColumnModel().getColumn(posiblesCol)
                    .setCellRenderer(new DefaultTableCellRenderer() {
                        @Override
                        public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                                       boolean isSelected, boolean hasFocus,
                                                                       int row, int column) {
                            Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                            int modelRow = tbl.convertRowIndexToModel(row);
                            Teacher t = model.getAt(modelRow);
                            setToolTipText(subjectsTooltip(t.getPossibleSubjects()));
                            return c; // keep the default text (the count)
                        }
                    });
        }

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0, 2));
        tfNombre = new JTextField();
        tfAbrev = new JTextField();
        tfEmail = new JTextField();
        tfPhone = new JTextField();
        tfDepto = new JTextField();
        spHoras = new JSpinner(new SpinnerNumberModel(25, 0, 40, 1));
        lblCondLogr = new JLabel("-");
        lblCondPond = new JLabel("-");

        form.add(new JLabel("Nombre:"));
        form.add(tfNombre);
        form.add(new JLabel("Abrev:"));
        form.add(tfAbrev);
        form.add(new JLabel("Email:"));
        form.add(tfEmail);
        form.add(new JLabel("Teléfono:"));
        form.add(tfPhone);
        form.add(new JLabel("Departamento:"));
        form.add(tfDepto);
        form.add(new JLabel("Horas trabajo:"));
        form.add(spHoras);
        form.add(new JLabel("Cond. logradas:"));
        form.add(lblCondLogr);
        form.add(new JLabel("Cond. ponderadas:"));
        form.add(lblCondPond);

        JPanel buttons = new JPanel();
        buttons.add(btnGuardar);
        buttons.add(btnNuevo);
        buttons.add(btnEliminar);
        buttons.add(btnEditPeriodos);
        buttons.add(btnEditAsignaturas);
        buttons.add(btnEditGrupos);
        buttons.add(btnEditPosibles);

        add(form, BorderLayout.EAST);
        add(buttons, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> loadSelected());

        btnGuardar.addActionListener(e -> saveSelected());
        btnNuevo.addActionListener(e -> newTeacher());
        btnEliminar.addActionListener(e -> deleteSelected());
        btnEditPeriodos.addActionListener(e -> editPeriodos());
        btnEditAsignaturas.addActionListener(e -> editAsignaturas());
        btnEditGrupos.addActionListener(e -> editGrupos());
        btnEditPosibles.addActionListener(e -> editAsignaturasPosibles());
    }

    private void editAsignaturasPosibles() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un profesor primero.");
            return;
        }
        Teacher t = model.getAt(row);

        // All subjects from controller
        List<Subject> all = controller.getSubjects();
        JList<Subject> list = new JList<>(new Vector<>(all));
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Nice labels
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Subject s) setText(subjectLabel(s));
                return this;
            }
        });

        // Preselect current possible subjects (by id if available)
        java.util.Set<String> currentIds = new java.util.HashSet<>();
        if (t.getPossibleSubjects() != null) {
            for (Subject s : t.getPossibleSubjects()) {
                try { currentIds.add(s.getId()); } catch (Throwable ignore) { /* fallback below */ }
            }
        }
        java.util.List<Integer> indices = new java.util.ArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            Subject s = all.get(i);
            boolean match = false;
            // Prefer ID match when possible
            try { match = currentIds.contains(s.getId()); } catch (Throwable ignore) {}
            // Fallback: object identity (if same instances are reused)
            if (!match && t.getPossibleSubjects() != null) {
                for (Subject ps : t.getPossibleSubjects()) {
                    if (ps == s) { match = true; break; }
                }
            }
            if (match) indices.add(i);
        }
        int[] selIdx = indices.stream().mapToInt(Integer::intValue).toArray();
        list.setSelectedIndices(selIdx);

        int res = JOptionPane.showConfirmDialog(this, new JScrollPane(list),
                "Asignaturas que puede impartir", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        List<Subject> selected = list.getSelectedValuesList();
        t.setPossibleSubjects(selected);

        controller.updateTeacher(t);
        // Refresh the row so the "Posibles" count updates immediately
        model.fireTableRowsUpdated(row, row);
    }

    private void loadSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Teacher t = model.getAt(row);
        tfNombre.setText(t.getName());
        tfAbrev.setText(t.getAbbreviation());
        tfEmail.setText(t.getEmail());
        tfPhone.setText(t.getPhone());
        tfDepto.setText(t.getDepartment());
        spHoras.setValue(t.getHoursWork());
        lblCondLogr.setText(String.valueOf(t.getAchievedConditions()));
        lblCondPond.setText(String.valueOf(t.getWeightedConditions()));
    }

    private void saveSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Teacher t = model.getAt(row);
        t.setName(tfNombre.getText());
        t.setAbbreviation(tfAbrev.getText());
        t.setEmail(tfEmail.getText());
        t.setPhone(tfPhone.getText());
        t.setDepartment(tfDepto.getText());
        t.setHoursWork((Integer) spHoras.getValue());

        controller.updateTeacher(t);
        model.fireTableRowsUpdated(row, row);
    }

    private void newTeacher() {
        Teacher t = new Teacher(UUID.randomUUID().toString(), "Nuevo", "N");
        controller.addTeacher(t);
        model.reload(controller.getTeachers());
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Teacher t = model.getAt(row);
        controller.removeTeacher(t.getId());
        model.reload(controller.getTeachers());
    }

    // ====== Editors for complex lists ======
    private void editPeriodos() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Teacher t = model.getAt(row);

        List<TimePeriod> all = controller.getTimePeriods();
        JList<TimePeriod> list = new JList<>(new Vector<>(all));
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TimePeriod tp) {
                    setText(tpLabel(tp));
                }
                return this;
            }
        });
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        int opt = JOptionPane.showOptionDialog(this, new JScrollPane(list),
                "Seleccione períodos", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"Preferidos", "No preferidos", "No disponibles", "Cancelar"}, "Cancelar");

        if (opt == 3 || opt == JOptionPane.CLOSED_OPTION) return;

        List<TimePeriod> selected = list.getSelectedValuesList();
        if (selected.isEmpty()) return;

        if (opt == 0) { // preferidos
            int weight = askWeight();
            t.setPreferredTimePeriods(selected, weight);
        } else if (opt == 1) { // no preferidos
            int weight = askWeight();
            t.setUnPreferredTimePeriods(selected, weight);
        } else if (opt == 2) { // no disponibles
            t.setUnavailableTimePeriods(selected);
        }

        controller.updateTeacher(t);
        loadSelected();
    }

    private void editAsignaturas() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Teacher t = model.getAt(row);

        List<Subject> all = controller.getSubjects();
        JList<Subject> list = new JList<>(new Vector<>(all));
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Show nice labels for subjects
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Subject s) setText(subjectLabel(s));
                return this;
            }
        });

        int res = JOptionPane.showConfirmDialog(this, new JScrollPane(list),
                "Seleccione asignaturas preferidas", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;

        List<Subject> selected = list.getSelectedValuesList();
        int weight = askWeight();
        t.setPreferredSubjects(selected, weight);

        controller.updateTeacher(t);
        loadSelected();
    }

    private void editGrupos() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Teacher t = model.getAt(row);

        List<StudentGroup> all = controller.getStudentGroups();
        JList<StudentGroup> list = new JList<>(new Vector<>(all));
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Show nice labels for groups
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof StudentGroup g) setText(groupLabel(g));
                return this;
            }
        });

        int opt = JOptionPane.showOptionDialog(this, new JScrollPane(list),
                "Seleccione grupos", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"Preferidos", "No preferidos", "Cancelar"}, "Cancelar");

        if (opt == 2 || opt == JOptionPane.CLOSED_OPTION) return;

        List<StudentGroup> selected = list.getSelectedValuesList();
        if (selected.isEmpty()) return;

        int weight = askWeight();
        if (opt == 0) {
            t.setPreferredStudentGroups(selected, weight);
        } else if (opt == 1) {
            t.setUnPreferredStudentGroups(selected, weight);
        }

        controller.updateTeacher(t);
        loadSelected();
    }

    private int askWeight() {
        String s = JOptionPane.showInputDialog(this, "Peso (entero):", "1");
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 1;
        }
    }

    static class TeacherTableModel extends AbstractTableModel {
        private List<Teacher> data;
        // NEW column "Posibles" at the end
        private final String[] cols = {"Nombre", "Abrev", "Email", "Teléfono", "Depto", "Horas", "CondLogr", "CondPond", "Posibles"};

        public TeacherTableModel(List<Teacher> teachers) {
            this.data = teachers;
        }

        public Teacher getAt(int r) { return data.get(r); }

        public void reload(List<Teacher> teachers) {
            this.data = teachers;
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        // Helper so the outer class can find a column by name
        public int columnIndex(String name) {
            for (int i = 0; i < cols.length; i++) if (cols[i].equals(name)) return i;
            return -1;
        }

        @Override
        public Object getValueAt(int r, int c) {
            Teacher t = data.get(r);
            return switch (c) {
                case 0 -> t.getName();
                case 1 -> t.getAbbreviation();
                case 2 -> t.getEmail();
                case 3 -> t.getPhone();
                case 4 -> t.getDepartment();
                case 5 -> t.getHoursWork();
                case 6 -> t.getAchievedConditions();
                case 7 -> t.getWeightedConditions();
                case 8 -> (t.getPossibleSubjects() == null ? 0 : t.getPossibleSubjects().size()); // NEW
                default -> "";
            };
        }
    }

    // Prefer TimePeriod.getName(); fallback to weekday + hours
    private String tpLabel(TimePeriod tp) {
        try {
            String n = tp.getName();
            if (n != null && !n.isBlank()) return n;
        } catch (Throwable ignore) {}
        try {
            return tp.getWeekday() + " " + tp.getInitialHour() + "-" + tp.getFinalHour();
        } catch (Throwable ignore) {}
        return String.valueOf(tp);
    }

    private String subjectLabel(Subject s) {
        if (s == null) return "";
        if (s.getName() != null && !s.getName().isBlank()) return s.getName();
        if (s.getAbbreviation() != null && !s.getAbbreviation().isBlank()) return s.getAbbreviation();
        return String.valueOf(s);
    }

    private String groupLabel(StudentGroup g) {
        if (g == null) return "";
        if (g.getName() != null && !g.getName().isBlank()) return g.getName();
        if (g.getAbbreviation() != null && !g.getAbbreviation().isBlank()) return g.getAbbreviation();
        return String.valueOf(g);
    }

    // Tooltip helper for Posibles
    private String subjectsTooltip(List<Subject> subs) {
        if (subs == null || subs.isEmpty()) return "(ninguna)";
        StringBuilder sb = new StringBuilder();
        for (Subject s : subs) {
            sb.append(subjectLabel(s)).append(", ");
        }
        // drop trailing comma
        return sb.substring(0, sb.length() - 2);
    }
}
