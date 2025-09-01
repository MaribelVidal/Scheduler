package presentation;

import business.Condition;
import business.Lesson;
import business.Schedule;
import business.Teacher;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Calendar extends JFrame {
    private JComboBox<String> categoryCombo;
    private JComboBox<ComboItem> nameCombo;
    private JLabel calendarTitle;

    SubjectsPanel subjectsPanel;

    private JScrollPane calendarScroll;
    private List<String> lastTpNames;

    private final Set<Integer> breakRows = Set.of(2, 5);

    private final Map<String, String[]> dataMap = new LinkedHashMap<>();
    private final DefaultListModel<String> profesoresModel = new DefaultListModel<>();
    private final DefaultListModel<String> clasesModel = new DefaultListModel<>();
    private final DefaultListModel<String> alumnosModel = new DefaultListModel<>();
    private final DefaultListModel<String> asignaturasModel = new DefaultListModel<>();
    private final DefaultListModel<String> periodosModel = new DefaultListModel<>();

    private JComboBox<ComboItem> scheduleCombo;

    private String typeEntity;  // "Profesor" | "Clase" | "Grupo de Alumnos"
    private String nameEntity;
    private String idEntity;
    private String idSchedule;

    private boolean isRefreshing = false;

    private final PresentationController presentationControler;

    public Calendar(PresentationController presentationController) {
        super("Calendario Semanal");
        this.presentationControler = presentationController;
        this.subjectsPanel  = new SubjectsPanel(presentationControler);
    }

    public void init(List<String> teachersNames, List<String> classroomsNames,
                     List<String> studentsGroupsNames, List<String> tpNames, List<String> subjectsNames) {
        this.lastTpNames = tpNames;
        initData(teachersNames, classroomsNames, studentsGroupsNames, subjectsNames);
        initUI(tpNames);
        setVisible(true);
    }

    private void initData(List<String> teachersNames, List<String> classroomsNames,
                          List<String> studentsGroupsNames, List<String> subjectsNames) {
        dataMap.put("Profesores", teachersNames.toArray(new String[0]));
        dataMap.put("Aulas", classroomsNames.toArray(new String[0]));
        dataMap.put("Grupos de Alumnos", studentsGroupsNames.toArray(new String[0]));
        dataMap.put("Asignaturas", subjectsNames.toArray(new String[0]));

        for (String p : dataMap.get("Profesores")) profesoresModel.addElement(p);
        for (String c : dataMap.get("Aulas")) clasesModel.addElement(c);
        for (String a : dataMap.get("Grupos de Alumnos")) alumnosModel.addElement(a);
        for (String s : dataMap.get("Asignaturas")) asignaturasModel.addElement(s);
    }

    private void initUI(List<String> tpNames) {
        dataMap.put("Periodos", tpNames.toArray(new String[0]));
        for (String p : dataMap.get("Periodos")) periodosModel.addElement(p);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Horario", createCalendarPanel(tpNames));
        tabs.addTab("Profesores", new ProfesoresPanel(presentationControler));
        tabs.addTab("Clases", new ClassroomsPanel(presentationControler));
        tabs.addTab("Grupos", new StudentGroupsPanel(presentationControler));
        tabs.addTab("Asignaturas", subjectsPanel);
        tabs.addTab("Periodos", new TimePeriodsPanel(presentationControler));

        add(tabs);

        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private JPanel createCalendarPanel(List<String> tpNames) {
        JPanel main = new JPanel(new BorderLayout());
        main.add(createNorthPanel(), BorderLayout.NORTH);

        calendarScroll = createCenterPanel(tpNames);
        main.add(calendarScroll, BorderLayout.CENTER);

        return main;
    }

    private JPanel createNorthPanel() {
        JPanel north = new JPanel(new BorderLayout(0, 5));
        north.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel selectors = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        selectors.add(new JLabel("Categoría:"));
        categoryCombo = new JComboBox<>(new String[] {"Profesores", "Aulas", "Grupos de Alumnos"});
        selectors.add(categoryCombo);

        selectors.add(new JLabel("Nombre:"));
        nameCombo = new JComboBox<>();
        selectors.add(nameCombo);

        selectors.add(new JLabel("Horario:"));
        scheduleCombo = new JComboBox<>();
        scheduleCombo.setPrototypeDisplayValue(new ComboItem("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", "Horario xxxxxxxx"));
        selectors.add(scheduleCombo);

        // --- ÚNICO botón: Regenerar (elimina y vuelve a generar) ---
        JButton regenReplaceBtn = new JButton("Regenerar (reemplaza)");
        regenReplaceBtn.addActionListener(e -> regenerateReplaceSchedules());
        selectors.add(regenReplaceBtn);

        // --- ÚNICO botón: Condiciones ---
        JButton condBtn = new JButton("Condiciones…");
        condBtn.addActionListener(e -> showConditionsDialog());
        selectors.add(condBtn);

        JButton condDetailsBtn = new JButton("Condiciones (detalles)...");
        condDetailsBtn.addActionListener(e -> showTeacherConditionDetails());
        selectors.add(condDetailsBtn);


        // Renombrar sigue igual
        JButton renameBtn = new JButton("Renombrar...");
        renameBtn.addActionListener(e -> {
            ComboItem sel = (ComboItem) scheduleCombo.getSelectedItem();
            if (sel == null) return;
            String current = sel.label;
            String name = JOptionPane.showInputDialog(this, "Nuevo nombre del horario:", current);
            if (name == null) return;
            name = name.trim();
            if (name.isBlank()) return;
            presentationControler.renameSchedule(sel.id, name);
            reloadScheduleCombo();
            ensureValidEntitySelection();
            for (int i = 0; i < scheduleCombo.getItemCount(); i++) {
                ComboItem it = scheduleCombo.getItemAt(i);
                if (it.id.equals(sel.id)) { scheduleCombo.setSelectedIndex(i); break; }
            }
            refreshCalendarTable(lastTpNames);
        });
        selectors.add(renameBtn);

        north.add(selectors, BorderLayout.NORTH);

        calendarTitle = new JLabel("", SwingConstants.CENTER);
        calendarTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        north.add(calendarTitle, BorderLayout.SOUTH);

        // Listeners
        categoryCombo.addActionListener(e -> {
            if(isRefreshing) return;
            isRefreshing = true;
            try {
                updateNames();
                typeEntity = normalizeType((String) categoryCombo.getSelectedItem());
                ComboItem ci = (ComboItem) nameCombo.getSelectedItem();
                if (ci != null) {
                    idEntity = ci.id; nameEntity = ci.label;
                } else { idEntity = null; nameEntity = null; }
                refreshCalendarTable(lastTpNames);
            } finally { isRefreshing = false; }
        });

        nameCombo.addActionListener(e -> {
            if(isRefreshing) return;
            isRefreshing = true;
            try {
                ComboItem ci = (ComboItem) nameCombo.getSelectedItem();
                if (ci != null) {
                    idEntity = ci.id; nameEntity = ci.label;
                    calendarTitle.setText("Horario de " + nameEntity);
                    refreshCalendarTable(lastTpNames);
                }
            } finally { isRefreshing = false; }
        });

        scheduleCombo.addActionListener(e -> {
            if (isRefreshing) return;
            isRefreshing = true;
            try {
                ComboItem sel = (ComboItem) scheduleCombo.getSelectedItem();
                idSchedule = (sel == null) ? null : sel.id;
                ensureValidEntitySelection();
                refreshCalendarTable(lastTpNames);
            } finally { isRefreshing = false; }
        });

        // Inicialización por defecto
        if (!dataMap.isEmpty()) {
            categoryCombo.setSelectedIndex(0);
            typeEntity = normalizeType((String) categoryCombo.getSelectedItem());
            updateNames();
            ComboItem first = (ComboItem) nameCombo.getSelectedItem();
            if (first != null) {
                idEntity = first.id; nameEntity = first.label;
            }
            calendarTitle.setText(nameEntity == null ? "Horario" : ("Horario de " + nameEntity));
        }

        // Cargar horarios y seleccionar algo
        reloadScheduleCombo();
        ensureValidEntitySelection();

        return north;
    }

    /** Delete ALL schedules and generate fresh ones; refresh UI. */
    private void regenerateReplaceSchedules() {
        setAllEnabled(false);
        Cursor old = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            int before = safeCountSchedules();

            // 1) Borrar TODOS los horarios existentes
            List<Schedule> all = presentationControler.getAllSchedules();
            if (all != null) {
                for (Schedule s : all) {
                    try {
                        presentationControler.deleteSchedule(s.getId());
                    } catch (Exception ex) {
                        // Si tu controller usa otro método (p.ej. deleteAllSchedules),
                        // deja este catch silencioso y confía en que al menos algunos se borren.
                        ex.printStackTrace();
                    }
                }
            }

            // 2) Generar de nuevo con la información actual
            presentationControler.generateSchedules();

            // 3) Recargar y seleccionar el primero (o único)
            reloadScheduleCombo();
            if (scheduleCombo.getItemCount() > 0) {
                scheduleCombo.setSelectedIndex(0);
                idSchedule = scheduleCombo.getItemAt(0).id;
            } else {
                idSchedule = null;
            }

            ensureValidEntitySelection();
            refreshCalendarTable(lastTpNames);

            int after = safeCountSchedules();
            JOptionPane.showMessageDialog(this,
                    "Regeneración completada.\nHorarios antes: " + before + "  →  ahora: " + after,
                    "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al regenerar horarios:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(old);
            setAllEnabled(true);
        }
    }

    private int safeCountSchedules() {
        try {
            List<Schedule> all = presentationControler.getAllSchedules();
            return (all == null) ? 0 : all.size();
        } catch (Exception e) { return -1; }
    }

    private void setAllEnabled(boolean enabled) {
        categoryCombo.setEnabled(enabled);
        nameCombo.setEnabled(enabled);
        scheduleCombo.setEnabled(enabled);
        Container parent = categoryCombo.getParent();
        for (Component c : parent.getComponents()) c.setEnabled(enabled);
    }

    /** Load schedules into combo and keep previous selection if possible. */
    private void reloadScheduleCombo() {
        List<Schedule> all = presentationControler.getAllSchedules();
        DefaultComboBoxModel<ComboItem> m = new DefaultComboBoxModel<>();
        if (all != null) {
            for (Schedule s : all) {
                m.addElement(new ComboItem(s.getId(), scheduleLabel(s)));
            }
        }
        scheduleCombo.setModel(m);

        if (idSchedule != null) {
            for (int i = 0; i < m.getSize(); i++) {
                if (idSchedule.equals(m.getElementAt(i).id)) {
                    scheduleCombo.setSelectedIndex(i);
                    return;
                }
            }
        }
        if (m.getSize() > 0) {
            scheduleCombo.setSelectedIndex(0);
            idSchedule = m.getElementAt(0).id;
        } else {
            idSchedule = null;
        }
        scheduleCombo.setEnabled(m.getSize() > 0);
    }

    private String scheduleLabel(Schedule s) {
        String n = s.getName();
        if (n != null && !n.isBlank()) return n;
        String shortId = s.getId() != null && s.getId().length() >= 8 ? s.getId().substring(0, 8) : String.valueOf(s.getId());
        return "Horario " + shortId;
    }

    private JScrollPane createCenterPanel(List<String> tpNames) {
        String[] columnNames = {"", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};
        ensureValidEntitySelection();

        String[] timeSlots = (tpNames == null) ? new String[0] : tpNames.toArray(new String[0]);
        Object[][] data = new Object[timeSlots.length][columnNames.length];
        for (int i = 0; i < timeSlots.length; i++) data[i][0] = timeSlots[i];

        if (typeEntity == null || idEntity == null || idEntity.isEmpty()) {
            DefaultTableModel model = createTableModel(data, columnNames);
            return new JScrollPane(createTable(model, columnNames.length));
        }

        Schedule schedule = null;
        if ("Profesor".equals(typeEntity)) {
            schedule = presentationControler.getTeacherSchedule(idEntity, idSchedule);
        } else if ("Clase".equals(typeEntity)) {
            schedule = presentationControler.getClassroomSchedule(idEntity, idSchedule);
        } else if ("Grupo de Alumnos".equals(typeEntity)) {
            schedule = presentationControler.getStudentGroupSchedule(idEntity, idSchedule);
        }

        // Prefer provided tpNames; else derive from schedule
        List<String> slotList =
                (tpNames != null && !tpNames.isEmpty())
                        ? tpNames
                        : deriveTimeSlotsFromSchedule(schedule);

        timeSlots = slotList.toArray(new String[0]);
        data = new Object[timeSlots.length][columnNames.length];
        for (int i = 0; i < timeSlots.length; i++) data[i][0] = timeSlots[i];

        if (schedule == null || schedule.getLessons() == null) {
            DefaultTableModel model = createTableModel(data, columnNames);
            return new JScrollPane(createTable(model, columnNames.length));
        }

        List<Lesson> lessons = schedule.getLessons();
        int placed = 0;

        for (int i = 0; i < timeSlots.length; i++) {
            int rowHour = parseRowStartHour(timeSlots[i]);
            for (int j = 1; j < columnNames.length; j++) {
                int headerIdx = toDayIndex(columnNames[j]);
                Lesson match = null;
                for (Lesson l : lessons) {
                    boolean dayHour =
                            lessonDayIndex(l) == headerIdx &&
                                    lessonStartHour(l) == rowHour;

                    if (!dayHour) continue;

                    boolean ok = switch (typeEntity) {
                        case "Profesor" -> (l.getTeacher() != null && l.getTeacher().getId().equals(idEntity));
                        case "Clase" -> (l.getClassroom() != null && l.getClassroom().getId().equals(idEntity));
                        case "Grupo de Alumnos" -> (l.getStudentGroup() != null && l.getStudentGroup().getId().equals(idEntity));
                        default -> false;
                    };

                    if (ok) { match = l; break; }
                }
                data[i][j] = (match == null) ? "" : formatCell(match, typeEntity);
            }
        }

        System.out.println("Placed lessons into cells = " + placed);

        DefaultTableModel model = createTableModel(data, columnNames);
        return new JScrollPane(createTable(model, columnNames.length));
    }

    private void refreshCalendarTable(List<String> tpNames) {
        if (tpNames == null) return;
        Container parent = (calendarScroll != null) ? calendarScroll.getParent() : null;
        JScrollPane newScroll = createCenterPanel(tpNames);

        if (parent != null && parent.getLayout() instanceof BorderLayout) {
            parent.remove(calendarScroll);
            calendarScroll = newScroll;
            parent.add(calendarScroll, BorderLayout.CENTER);
            parent.revalidate();
            parent.repaint();
        } else {
            if (calendarScroll != null) {
                Container oldParent = calendarScroll.getParent();
                if (oldParent != null) oldParent.remove(calendarScroll);
            }
            calendarScroll = newScroll;
            getContentPane().add(calendarScroll, BorderLayout.CENTER);
            revalidate();
            repaint();
        }
    }

    private String normalizeType(String categoryLabel) {
        if ("Profesores".equals(categoryLabel)) return "Profesor";
        if ("Aulas".equals(categoryLabel)) return "Clase";
        if ("Grupos de Alumnos".equals(categoryLabel)) return "Grupo de Alumnos";
        return categoryLabel;
    }

    private String formatCell(business.Lesson lesson, String typeEntity) {
        if ("Clase".equals(typeEntity)) {
            return lesson.getSubject().getName() + " — " +
                    lesson.getTeacher().getName() + " — " +
                    lesson.getStudentGroup().getName();
        } else if ("Profesor".equals(typeEntity)) {
            return lesson.getSubject().getName() + " — " +
                    lesson.getStudentGroup().getName() + " — " +
                    lesson.getClassroom().getName();
        } else {
            return lesson.getSubject().getName() + " — " +
                    lesson.getTeacher().getName() + " — " +
                    lesson.getClassroom().getName();
        }
    }

    private DefaultTableModel createTableModel(Object[][] data, String[] columnNames) {
        return new DefaultTableModel(data, columnNames) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private JTable createTable(DefaultTableModel model, int columnCount) {
        JTable table = new JTable(model) {
            @Override
            public Rectangle getCellRect(int row, int column, boolean includeSpacing) {
                if (breakRows.contains(row) && column > 0) {
                    Rectangle r = super.getCellRect(row, 1, includeSpacing);
                    for (int c = 2; c < columnCount; c++) {
                        Rectangle r2 = super.getCellRect(row, c, includeSpacing);
                        r.width = (r2.x + r2.width) - r.x;
                    }
                    return r;
                }
                return super.getCellRect(row, column, includeSpacing);
            }

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (breakRows.contains(row)) {
                    c.setBackground(Color.LIGHT_GRAY);
                    c.setForeground(Color.DARK_GRAY);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };

        table.setRowHeight(40);
        table.getTableHeader().setReorderingAllowed(false);
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        return table;
    }

    public void refreshClassrooms() {
        subjectsPanel.refreshClassroomsCombo();
    }

    private static final class ComboItem {
        final String id;
        final String label;
        ComboItem(String id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    private void updateNames() {
        String cat = (String) categoryCombo.getSelectedItem();
        DefaultComboBoxModel<ComboItem> model = new DefaultComboBoxModel<>();

        if ("Profesores".equals(cat)) {
            for (var t : presentationControler.getTeachers()) {
                model.addElement(new ComboItem(t.getId(), t.getName()));
            }
            typeEntity = "Profesor";
        } else if ("Aulas".equals(cat)) {
            for (var c : presentationControler.getClassrooms()) {
                model.addElement(new ComboItem(c.getId(), c.getName()));
            }
            typeEntity = "Clase";
        } else if ("Grupos de Alumnos".equals(cat)) {
            for (var g : presentationControler.getStudentGroups()) {
                model.addElement(new ComboItem(g.getId(), g.getName()));
            }
            typeEntity = "Grupo de Alumnos";
        }

        nameCombo.setModel(model);

        ComboItem first = model.getSize() > 0 ? model.getElementAt(0) : null;
        if (first != null) {
            idEntity = first.id;
            nameEntity = first.label;
            calendarTitle.setText("Horario de " + nameEntity);
        } else {
            idEntity = null;
            nameEntity = null;
            calendarTitle.setText("Horario");
        }
    }

    public void refreshEntities() {
        ComboItem selected = (ComboItem) nameCombo.getSelectedItem();
        String prevId = (selected != null) ? selected.id : null;

        updateNames();

        if (scheduleCombo != null) {
            reloadScheduleCombo();
            ensureValidEntitySelection();
        }

        if (prevId != null) {
            ComboBoxModel<ComboItem> model = nameCombo.getModel();
            for (int i = 0; i < model.getSize(); i++) {
                ComboItem it = model.getElementAt(i);
                if (prevId.equals(it.id)) {
                    nameCombo.setSelectedIndex(i);
                    idEntity = it.id;
                    nameEntity = it.label;
                    calendarTitle.setText("Horario de " + nameEntity);
                    break;
                }
            }
        }

        refreshCalendarTable(lastTpNames);
    }

    private boolean isCurrentEntityValid() {
        if (idEntity == null || typeEntity == null) return false;
        try {
            switch (typeEntity) {
                case "Profesor":
                    for (var t : presentationControler.getTeachers()) if (idEntity.equals(t.getId())) return true;
                    return false;
                case "Clase":
                    for (var c : presentationControler.getClassrooms()) if (idEntity.equals(c.getId())) return true;
                    return false;
                case "Grupo de Alumnos":
                    for (var g : presentationControler.getStudentGroups()) if (idEntity.equals(g.getId())) return true;
                    return false;
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private void ensureValidEntitySelection() {
        if (isCurrentEntityValid()) return;
        updateNames();
        ComboItem ci = (ComboItem) nameCombo.getSelectedItem();
        if (ci != null) {
            idEntity = ci.id;
            nameEntity = ci.label;
            calendarTitle.setText("Horario de " + nameEntity);
        } else {
            idEntity = null;
            nameEntity = null;
            calendarTitle.setText("Horario");
        }
    }

    // ================= helpers =================

    private static int toDayIndex(String d) {
        if (d == null) return -1;
        d = d.trim().toLowerCase();
        if (d.startsWith("lu")) return 1;
        if (d.startsWith("ma") && !d.startsWith("mi")) return 2;
        if (d.startsWith("mi")) return 3;     // Miércoles/Miercoles
        if (d.startsWith("ju")) return 4;
        if (d.startsWith("vi")) return 5;
        if (d.startsWith("mon")) return 1;
        if (d.startsWith("tue")) return 2;
        if (d.startsWith("wed")) return 3;
        if (d.startsWith("thu")) return 4;
        if (d.startsWith("fri")) return 5;
        if (d.matches("\\d")) {
            int v = Integer.parseInt(d);
            if (v >= 1 && v <= 5) return v;
        }
        return -1;
    }

    private static int lessonDayIndex(business.Lesson l) {
        return toDayIndex(l.getTimePeriod().getWeekday());
    }

    private static int lessonStartHour(business.Lesson l) {
        return l.getTimePeriod().getInitialHour().getHour();
    }

    private int parseRowStartHour(String label) {
        if (label == null) return -1;
        label = label.trim();
        try {
            String first = label.split("[–-]")[0].trim();
            if (first.contains(":")) {
                return java.time.LocalTime.parse(first).getHour();
            }
        } catch (Exception ignore) {}
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{1,2})$").matcher(label);
        if (m.find()) return Integer.parseInt(m.group(1));
        return -1;
    }

    private static String formatRange(java.time.LocalTime s, java.time.LocalTime e) {
        return String.format("%02d:%02d-%02d:%02d", s.getHour(), s.getMinute(), e.getHour(), e.getMinute());
    }

    private List<String> deriveTimeSlotsFromSchedule(Schedule schedule) {
        if (schedule == null || schedule.getLessons() == null) return List.of();
        TreeMap<java.time.LocalTime, java.time.LocalTime> ranges = new TreeMap<>();
        for (Lesson l : schedule.getLessons()) {
            var s = l.getTimePeriod().getInitialHour();
            var e = l.getTimePeriod().getFinalHour();
            ranges.merge(s, e, (oldE, newE) -> newE.isAfter(oldE) ? newE : oldE);
        }
        List<String> out = new ArrayList<>();
        for (var en : ranges.entrySet()) out.add(formatRange(en.getKey(), en.getValue()));
        return out;
    }

    private void showConditionsDialog() {
        ComboItem sel = (ComboItem) scheduleCombo.getSelectedItem();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "No hay un horario seleccionado.", "Condiciones", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String schedId = sel.id;

        var summary = presentationControler.getConditionCounts(schedId);
        if (summary == null || summary.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sin datos de condiciones para este horario.", "Condiciones", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] cols = { "Profesor", "Pref. cumplidas (#)", "No pref. violadas (#)", "Puntuación pref.", "Penalización no pref." };
        Object[][] rows = new Object[summary.size()][cols.length];
        int r = 0;
        for (var e : summary.entrySet()) {
            Teacher t = e.getKey();
            int[] v = e.getValue(); // [countPref, countUnpref, sumPref, sumUnpref]
            rows[r][0] = t.getName();
            rows[r][1] = v[0];
            rows[r][2] = v[1];
            rows[r][3] = v[2];
            rows[r][4] = v[3];
            r++;
        }

        JTable table = new JTable(new DefaultTableModel(rows, cols) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(600, 300));
        JOptionPane.showMessageDialog(this, sp, "Condiciones por profesor", JOptionPane.PLAIN_MESSAGE);
    }

    private void showTeacherConditionDetails() {
        ComboItem sched = (ComboItem) scheduleCombo.getSelectedItem();
        ComboItem teacher = (ComboItem) nameCombo.getSelectedItem();

        if (sched == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un horario.", "Condiciones", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!"Profesor".equals(typeEntity) || teacher == null) {
            JOptionPane.showMessageDialog(this, "Selecciona la categoría 'Profesores' y un profesor.", "Condiciones", JOptionPane.WARNING_MESSAGE);
            return;
        }

        var report = presentationControler.getConditionReportForTeacher(sched.id, teacher.id);

        // Build two tables: Preferred (achieved / unmet) and Unpreferred (violated / respected)
        String[] cols = { "Tipo", "Entidad", "Peso", "Estado", "Lecciones que lo cumplen" };

        Object[][] prefRows = buildRowsForChecks(report.preferred, true);
        Object[][] unprefRows = buildRowsForChecks(report.unpreferred, false);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Preferidas", new JScrollPane(makeTable(prefRows, cols)));
        tabs.addTab("No preferidas", new JScrollPane(makeTable(unprefRows, cols)));

        tabs.setPreferredSize(new Dimension(800, 350));
        JOptionPane.showMessageDialog(this, tabs, "Condiciones de " + (teacher.label), JOptionPane.PLAIN_MESSAGE);
    }

    private JTable makeTable(Object[][] data, String[] cols) {
        JTable t = new JTable(new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    private Object[][] buildRowsForChecks(java.util.List<business.BusinessController.ConditionCheck> checks, boolean preferred) {
        Object[][] rows = new Object[checks.size()][5];
        int i = 0;
        for (var cc : checks) {
            Condition c = cc.condition;
            rows[i][0] = c.getConditionType();
            rows[i][1] = describeEntity(c);
            rows[i][2] = c.getWeight();
            rows[i][3] = preferred
                    ? (cc.matched ? "Cumplida" : "No cumplida")
                    : (cc.matched ? "Violada" : "Respetada");
            rows[i][4] = witnessesToString(cc.witnesses);
            i++;
        }
        return rows;
    }

    private String describeEntity(Condition c) {
        switch (c.getConditionType()) {
            case "Subject":
                return (c.getSubject() == null) ? "-" : c.getSubject().getName();
            case "TimePeriod":
                if (c.getTimePeriod() == null) return "-";
                var tp = c.getTimePeriod();
                return tp.getWeekday() + " " + String.format("%02d:%02d-%02d:%02d",
                        tp.getInitialHour().getHour(), tp.getInitialHour().getMinute(),
                        tp.getFinalHour().getHour(), tp.getFinalHour().getMinute());
            case "StudentGroup":
                return (c.getStudentGroup() == null) ? "-" : c.getStudentGroup().getName();
            default:
                return "-";
        }
    }

    private String witnessesToString(java.util.List<business.Lesson> lessons) {
        if (lessons == null || lessons.isEmpty()) return "";
        java.util.List<String> parts = new java.util.ArrayList<>();
        for (business.Lesson l : lessons) {
            String subj = (l.getSubject() == null) ? "-" : l.getSubject().getName();
            String grp  = (l.getStudentGroup() == null) ? "-" : l.getStudentGroup().getName();
            String room = (l.getClassroom() == null) ? "-" : l.getClassroom().getName();
            var tp = l.getTimePeriod();
            String time = (tp == null) ? "-" : (tp.getWeekday() + " " +
                    String.format("%02d:%02d-%02d:%02d", tp.getInitialHour().getHour(), tp.getInitialHour().getMinute(),
                            tp.getFinalHour().getHour(), tp.getFinalHour().getMinute()));
            parts.add(subj + " / " + grp + " / " + room + " @ " + time);
        }
        return String.join(" | ", parts);
    }


}
