package presentation;

import business.TimePeriod;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalTime;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

public class TimePeriodsPanel extends JPanel {
    private final PresentationController controller;
    private final JTable table;
    private final TPTableModel model;
    private final TableRowSorter<TPTableModel> sorter;

    // Form fields
    private final JTextField txtId = new JTextField();
    private final JComboBox<String> cbWeekday = new JComboBox<>(new String[]{"Lunes","Martes","Miércoles","Jueves","Viernes","Sábado","Domingo"});
    private final JTextField txtInicio = new JTextField(); // HH:mm
    private final JTextField txtFin = new JTextField();    // HH:mm

    private final JButton btnGuardar = new JButton("Guardar cambios");
    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnEliminar = new JButton("Eliminar");

    public TimePeriodsPanel(PresentationController controller) {
        this.controller = Objects.requireNonNull(controller);
        setLayout(new BorderLayout(12,12));

        model = new TPTableModel(controller.getTimePeriods());
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
        btnNuevo.addActionListener(e -> addNewTimePeriod());
        btnEliminar.addActionListener(e -> deleteSelectedTimePeriod());

        if (model.getRowCount() > 0) {
            table.setRowSelectionInterval(0,0);
            loadSelectedIntoForm();
        } else {
            clearForm();
        }
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout(8,8));
        JTextField filter = new JTextField();
        filter.setToolTipText("Filtrar por día o hora (HH:mm)");
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
        addField(form, gbc, row++, "Día de semana:", cbWeekday, false);
        addField(form, gbc, row++, "Hora inicio (HH:mm):", txtInicio, false);
        addField(form, gbc, row++, "Hora fin (HH:mm):", txtFin, false);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JPanel buttons = new JPanel(new GridLayout(2, 2, 8, 8));;
        buttons.add(btnGuardar);
        buttons.add(btnNuevo);
        buttons.add(btnEliminar);
        form.add(buttons, gbc);

        form.setPreferredSize(new Dimension(420, 0));
        return form;
    }

    private void addField(JPanel form, GridBagConstraints gbc, int row, String label, JComponent comp, boolean readOnly) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 1;
        if (comp instanceof JTextField tf) tf.setEditable(!readOnly);
        form.add(comp, gbc);
    }

    private void clearForm() {
        txtId.setText("");
        cbWeekday.setSelectedIndex(0);
        txtInicio.setText("");
        txtFin.setText("");
    }

    private void loadSelectedIntoForm() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { clearForm(); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        TimePeriod tp = model.getAt(modelRow);

        txtId.setText(tp.getId());
        cbWeekday.setSelectedItem(tp.getWeekday());
        txtInicio.setText(tp.getInitialHour() == null ? "" : tp.getInitialHour().toString());
        txtFin.setText(tp.getFinalHour() == null ? "" : tp.getFinalHour().toString());
    }

    private void saveEdits() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Selecciona un período."); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        TimePeriod tp = model.getAt(modelRow);

        tp.setWeekday(Objects.toString(cbWeekday.getSelectedItem(), ""));
        try {
            tp.setInitialHour(parseTime(txtInicio.getText().trim()));
            tp.setFinalHour(parseTime(txtFin.getText().trim()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Formato de hora inválido. Usa HH:mm");
            return;
        }

        controller.updateTimePeriod(tp);
        model.fireTableRowsUpdated(modelRow, modelRow);
        controller.refreshAllUI();
    }

    private void addNewTimePeriod() {
        String dia = (String) JOptionPane.showInputDialog(this, "Día de semana:", "Nuevo período",
                JOptionPane.PLAIN_MESSAGE, null, new String[]{"Lunes","Martes","Miércoles","Jueves","Viernes","Sábado","Domingo"}, "Lunes");
        if (dia == null) return;
        String ini = JOptionPane.showInputDialog(this, "Hora inicio (HH:mm):", "08:00");
        if (ini == null) return;
        String fin = JOptionPane.showInputDialog(this, "Hora fin (HH:mm):", "09:00");
        if (fin == null) return;

        TimePeriod tp = new TimePeriod(java.util.UUID.randomUUID().toString(), dia, parseTime(ini.trim()), parseTime(fin.trim()));
        try {
            tp.setInitialHour(parseTime(ini.trim()));
            tp.setFinalHour(parseTime(fin.trim()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Formato de hora inválido. Usa HH:mm");
            return;
        }

        controller.addNewTimePeriod(tp);
        model.reload(controller.getTimePeriods());
        int last = model.getRowCount() - 1;
        if (last >= 0) {
            table.setRowSelectionInterval(last, last);
            loadSelectedIntoForm();
        }
        controller.refreshAllUI();
    }

    private void deleteSelectedTimePeriod() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Selecciona un período."); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        TimePeriod tp = model.getAt(modelRow);

        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar el período " + tp.getWeekday() +
                " " + tp.getInitialHour() + "-" + tp.getFinalHour() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        controller.removeTimePeriod(tp.getId());
        model.reload(controller.getTimePeriods());
        clearForm();
        controller.refreshAllUI();
    }

    private static LocalTime parseTime(String hhmm) {
        if (hhmm == null || hhmm.isBlank()) throw new IllegalArgumentException("empty");
        // Accept "9:00" or "09:00"
        return LocalTime.parse(hhmm.length()==4 ? "0"+hhmm : hhmm);
    }

    // Table model
    private static class TPTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Día", "Inicio", "Fin"};
        private List<TimePeriod> data;

        TPTableModel(List<TimePeriod> initial) { this.data = initial; }
        TimePeriod getAt(int row) { return data.get(row); }
        void reload(List<TimePeriod> newData) { this.data = newData; fireTableDataChanged(); }

        @Override public int getRowCount() { return data == null ? 0 : data.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }
        @Override public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }
        @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            TimePeriod tp = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> tp.getId();
                case 1 -> tp.getWeekday();
                case 2 -> tp.getInitialHour() == null ? "" : tp.getInitialHour().toString();
                case 3 -> tp.getFinalHour() == null ? "" : tp.getFinalHour().toString();
                default -> "";
            };
        }
    }
}
