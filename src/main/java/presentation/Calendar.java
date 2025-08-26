/**
 * Clase principal que muestra un calendario semanal con horarios y descansos.
 * Permite seleccionar diferentes categorías (Profesores, Clases, Alumnos)
 * y muestra dinámicamente el horario correspondiente.
 */
package presentation;

import business.Lesson;
import business.Schedule;
import persistence.DAO.LessonDAO;

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

    /** Mantener referencia al scroll del calendario para refrescar la tabla */
    private JScrollPane calendarScroll;

    /** Última lista de tramos horarios para reconstrucción */
    private List<String> lastTpNames;

    /** Índices de fila que corresponden a un descanso */
    private final Set<Integer> breakRows = Set.of(2, 5);

    /** Mapa de categorías → lista de nombres */
    private final Map<String, String[]> dataMap = new LinkedHashMap<>();

    /** Modelos de lista para gestión */
    private final DefaultListModel<String> profesoresModel = new DefaultListModel<>();
    private final DefaultListModel<String> clasesModel = new DefaultListModel<>();
    private final DefaultListModel<String> alumnosModel = new DefaultListModel<>();
    private final DefaultListModel<String> asignaturasModel = new DefaultListModel<>();
    private final DefaultListModel<String> periodosModel = new DefaultListModel<>();

    private JComboBox<ComboItem> scheduleCombo;

    private String typeEntity;  // "Profesor" | "Clase" | "Grupo de Alumnos"
    private String nameEntity;  // nombre seleccionado (para mostrar)
    private String idEntity;    // id seleccionado (aquí usamos el mismo valor que nombre, ajusta si tienes un map nombre->id)
    private String idSchedule; // ajusta si necesitas otro id
    private List<String> scheduleIds; // ids de horarios, si necesitas mantenerlos

    private boolean isRefreshing = false; // para evitar bucles infinitos al refrescar

    private PresentationController presentationControler;

    public Calendar(PresentationController presentationController) {
        super("Calendario Semanal");
        this.presentationControler = presentationController;
        this.scheduleIds = new ArrayList<>();
    }

    public void init(List<String> teachersNames, List<String> classroomsNames,
                     List<String> studentsGroupsNames, List<String> tpNames, List<String> subjectsNames) {
        this.lastTpNames = tpNames;
        this.scheduleIds = presentationControler.getScheduleIds();
        this.idSchedule = (scheduleIds == null || scheduleIds.isEmpty()) ? null : scheduleIds.get(0); // primer id por defecto

        initData(teachersNames, classroomsNames, studentsGroupsNames, subjectsNames);
        initUI(tpNames);
        setVisible(true);
    }

    /** Rellena el map de datos con valores */
    private void initData(List<String> teachersNames, List<String> classroomsNames, List<String> studentsGroupsNames, List<String> subjectsNames) {
        dataMap.put("Profesores", teachersNames.toArray(new String[0]));
        dataMap.put("Aulas", classroomsNames.toArray(new String[0]));
        dataMap.put("Grupos de Alumnos", studentsGroupsNames.toArray(new String[0]));
        dataMap.put("Asignaturas", subjectsNames.toArray(new String[0]));


        for (String p : dataMap.get("Profesores")) profesoresModel.addElement(p);
        for (String c : dataMap.get("Aulas")) clasesModel.addElement(c);
        for (String a : dataMap.get("Grupos de Alumnos")) alumnosModel.addElement(a);
        for (String s : dataMap.get("Asignaturas")) asignaturasModel.addElement(s);
    }

    /** Configura la interfaz gráfica: paneles, tablas y listeners */
    private void initUI(List<String> tpNames) {
        dataMap.put("Periodos", tpNames.toArray(new String[0]));
        for (String p : dataMap.get("Periodos")) periodosModel.addElement(p);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Horario", createCalendarPanel(tpNames));
        tabs.addTab("Profesores", new ProfesoresPanel(presentationControler));
        tabs.addTab("Clases", new ClassroomsPanel(presentationControler));
        tabs.addTab("Grupos", new StudentGroupsPanel(presentationControler));
        tabs.addTab("Asignaturas", new SubjectsPanel(presentationControler));
        tabs.addTab("Periodos", new TimePeriodsPanel(presentationControler));

        add(tabs);

        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /** Crea el panel del calendario con selector y tabla */
    private JPanel createCalendarPanel(List<String> tpNames) {
        JPanel main = new JPanel(new BorderLayout());
        main.add(createNorthPanel(), BorderLayout.NORTH);

        calendarScroll = createCenterPanel(tpNames);
        main.add(calendarScroll, BorderLayout.CENTER);

        return main;
    }

    /** Panel superior con combos y título dinámico */
    private JPanel createNorthPanel() {
        JPanel north = new JPanel(new BorderLayout(0, 5));
        north.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel selectors = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        selectors.add(new JLabel("Categoría:"));
        categoryCombo = new JComboBox<>(new String[] {"Profesores", "Aulas", "Grupos de Alumnos"});
        selectors.add(categoryCombo);

        selectors.add(new JLabel("Nombre:"));
        nameCombo = new JComboBox<>();
        selectors.add(nameCombo);

        // === selector de Horario (Schedule) ===
        selectors.add(new JLabel("Horario:"));
        scheduleCombo = new JComboBox<>(); // JComboBox<ComboItem>
        scheduleCombo.setPrototypeDisplayValue(new ComboItem("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
                "Horario xxxxxxxx")); // ancho cómodo
        selectors.add(scheduleCombo);

        // Botón Renombrar
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
            reloadScheduleCombo();                         // recargar nombres
            ensureValidEntitySelection();
            // Mantener el mismo id seleccionado
            for (int i = 0; i < scheduleCombo.getItemCount(); i++) {
                ComboItem it = scheduleCombo.getItemAt(i);
                if (it.id.equals(sel.id)) { scheduleCombo.setSelectedIndex(i); break; }
            }
            refreshCalendarTable(lastTpNames);
        });
        selectors.add(renameBtn);

        // Botón Regenerar
        JButton regenerateBtn = new JButton("Regenerar horarios");
        regenerateBtn.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this,
                    "¿Regenerar los horarios con la información actual?\n" +
                            "Esto reemplazará las soluciones previas.",
                    "Confirmar", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) return;

            presentationControler.regenerateSchedules();
            reloadScheduleCombo();                         // ids/nombres pueden cambiar
            ensureValidEntitySelection();
            refreshCalendarTable(lastTpNames);
        });
        selectors.add(regenerateBtn);

        north.add(selectors, BorderLayout.NORTH);

        calendarTitle = new JLabel("", SwingConstants.CENTER);
        calendarTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        north.add(calendarTitle, BorderLayout.SOUTH);

        // Listeners: cuando cambian, actualizamos type/id y refrescamos tabla
        categoryCombo.addActionListener(e -> {
            if(isRefreshing) return;
            isRefreshing = true; // evita bucles infinitos
            try {
                updateNames(); // también fija title provisional
                // Normaliza tipo (Profesores -> Profesor, Aulas -> Clase, Grupos de Alumnos -> Grupo de Alumnos)
                typeEntity = normalizeType((String) categoryCombo.getSelectedItem());
                // idEntity provisional al primer nombre si existe
                ComboItem ci = (ComboItem) nameCombo.getSelectedItem();
                if (ci != null) {
                    idEntity = ci.id;
                    nameEntity = ci.label;
                } else {
                    idEntity = null;
                    nameEntity = null;
                }
                System.out.printf("Category change -> type=%s, id=%s, label=%s%n", typeEntity, idEntity, nameEntity);

                refreshCalendarTable(lastTpNames);
            }
            finally {
                isRefreshing = false; // permite futuros cambios
            }
        });

        nameCombo.addActionListener(e -> {
            if(isRefreshing) return;
            isRefreshing = true; // evita bucles infinitos
            try {
                ComboItem ci = (ComboItem) nameCombo.getSelectedItem();
                if (ci != null) {
                    idEntity = ci.id;          // <-- pass this to BusinessController
                    nameEntity = ci.label;
                    calendarTitle.setText("Horario de " + nameEntity);
                    refreshCalendarTable(lastTpNames);
                }
            }
            finally {
                isRefreshing = false; // permite futuros cambios
            }
        });

        // Cambio de horario → refrescar tabla
        scheduleCombo.addActionListener(e -> {
            if (isRefreshing) return;
            isRefreshing = true;
            try {
                ComboItem sel = (ComboItem) scheduleCombo.getSelectedItem();
                idSchedule = (sel == null) ? null : sel.id;
                ensureValidEntitySelection();
                refreshCalendarTable(lastTpNames);
            } finally {
                isRefreshing = false;
            }
        });

        // Inicialización por defecto
        // Selecciona la primera categoría y nombres
        if (!dataMap.isEmpty()) {
            categoryCombo.setSelectedIndex(0);
            typeEntity = normalizeType((String) categoryCombo.getSelectedItem());
            updateNames();
            ComboItem first = (ComboItem) nameCombo.getSelectedItem();
            if (first != null) {
                idEntity = first.id;
                nameEntity = first.label;
            } else {
                idEntity = null;
                nameEntity = null;
            }
            calendarTitle.setText(nameEntity == null ? "Horario" : ("Horario de " + nameEntity));
        }

        // Cargar IDs/nombres de horarios al final
        reloadScheduleCombo();
        ensureValidEntitySelection();

        return north;
    }


    /** Recarga el combo de IDs de horario desde PresentationController y mantiene selección si es posible. */
    /** Recarga el combo de IDs de horario mostrando nombre si existe. */
    private void reloadScheduleCombo() {
        java.util.List<business.Schedule> all = presentationControler.getAllSchedules(); // add this in PresentationController
        DefaultComboBoxModel<ComboItem> m = new DefaultComboBoxModel<>();
        if (all != null) {
            for (business.Schedule s : all) {
                m.addElement(new ComboItem(s.getId(), scheduleLabel(s)));
            }
        }
        scheduleCombo.setModel(m);

        // Mantener selección previa si es posible
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

    private String scheduleLabel(business.Schedule s) {
        String n = s.getName();
        if (n != null && !n.isBlank()) return n;
        // Fallback: etiqueta corta con ID
        String shortId = s.getId() != null && s.getId().length() >= 8 ? s.getId().substring(0, 8) : String.valueOf(s.getId());
        return "Horario " + shortId;
    }



    /** Construye el panel central con la JTable del horario */
    private JScrollPane createCenterPanel(List<String> tpNames) {
        String[] columnNames = {"", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};
        ensureValidEntitySelection();

        // If there’s no valid selection yet, show an empty table with whatever rows we got (maybe zero).
        if (typeEntity == null || idEntity == null || idEntity.isEmpty()) {
            String[] timeSlots = (tpNames == null) ? new String[0] : tpNames.toArray(new String[0]);
            Object[][] data = new Object[timeSlots.length][columnNames.length];
            for (int i = 0; i < timeSlots.length; i++) data[i][0] = timeSlots[i];
            DefaultTableModel model = createTableModel(data, columnNames);
            return new JScrollPane(createTable(model, columnNames.length));
        }

        // Fetch schedule for the current selection
        Schedule schedule = null;
        if ("Profesor".equals(typeEntity)) {
            schedule = presentationControler.getTeacherSchedule(idEntity, idSchedule);
        } else if ("Clase".equals(typeEntity)) {
            schedule = presentationControler.getClassroomSchedule(idEntity, idSchedule);
        } else if ("Grupo de Alumnos".equals(typeEntity)) {
            schedule = presentationControler.getStudentGroupSchedule(idEntity, idSchedule);
        }

        // --- SANITY CHECK ---
        System.out.println("tpNames (incoming) = " + (tpNames == null ? "null" : tpNames));
        System.out.printf("Selected type=%s, id=%s, label=%s%n", typeEntity, idEntity, nameEntity);
        if (schedule == null) {
            System.out.println("schedule = null");
        } else if (schedule.getLessons() == null) {
            System.out.println("schedule.getLessons() = null");
        } else {
            System.out.println("lessons.size = " + schedule.getLessons().size());
            for (Lesson l : schedule.getLessons()) {
                System.out.printf("  lesson: %s | day=%s | start=%s | end=%s | tpId=%s | teacher=%s | group=%s | room=%s%n",
                        l.getSubject().getName(),
                        l.getTimePeriod().getWeekday(),
                        l.getTimePeriod().getInitialHour(),
                        l.getTimePeriod().getFinalHour(),
                        l.getTimePeriod().getId(),
                        l.getTeacher().getId(),
                        l.getStudentGroup().getId(),
                        l.getClassroom().getId()
                );
            }
        }
        // --- END SANITY CHECK ---

        // Decide final timeSlots: prefer provided tpNames; if empty, derive from schedule
        java.util.List<String> slotList =
                (tpNames != null && !tpNames.isEmpty())
                        ? tpNames
                        : deriveTimeSlotsFromSchedule(schedule);

        String[] timeSlots = slotList.toArray(new String[0]);
        Object[][] data = new Object[timeSlots.length][columnNames.length];

        // Fill column 0 with labels
        for (int i = 0; i < timeSlots.length; i++) {
            data[i][0] = timeSlots[i];
        }

        // Nothing to match? return the header-only table
        if (schedule == null || schedule.getLessons() == null) {
            DefaultTableModel model = createTableModel(data, columnNames);
            return new JScrollPane(createTable(model, columnNames.length));
        }
        List<Lesson> lessons = (schedule == null) ? List.of() : schedule.getLessons();
        int placed = 0;
        // Populate cells by comparing weekday + start hour
        for (int i = 0; i < timeSlots.length; i++) {
            int rowHour = parseRowStartHour(timeSlots[i]);
            for (int j = 1; j < columnNames.length; j++) {  // j=0 is time
                int headerIdx = toDayIndex(columnNames[j]);

                Lesson match = null;

                // inside the fill loop, after setting data[i][j]:
                if (match != null) placed++;


                for (Lesson l : lessons) {
                    if (lessonDayIndex(l) == headerIdx && lessonStartHour(l) == rowHour) {
                        match = l;
                        break;
                    }
                }
                data[i][j] = (match == null) ? "" : formatCell(match, typeEntity);

            }
        }
        System.out.println("Placed lessons into cells = " + placed);

        DefaultTableModel model = createTableModel(data, columnNames);
        return new JScrollPane(createTable(model, columnNames.length));
    }



    /** Reconstruye la tabla cuando cambian los combos */
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
            // Fallback
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

    /** Convierte etiqueta de pestaña en tipo usado por la lógica */
    private String normalizeType(String categoryLabel) {
        if ("Profesores".equals(categoryLabel)) return "Profesor";
        if ("Aulas".equals(categoryLabel)) return "Clase";
        if ("Grupos de Alumnos".equals(categoryLabel)) return "Grupo de Alumnos";
        return categoryLabel;
    }

    /** Formatea el contenido de la celda dependiendo de la vista */
    private String formatCell(Lesson lesson, String typeEntity) {
        if ("Clase".equals(typeEntity)) {
            return lesson.getSubject().getName() + " — " +
                    lesson.getTeacher().getId() + " — " +
                    lesson.getStudentGroup().getId();
        } else if ("Profesor".equals(typeEntity)) {
            return lesson.getSubject().getName() + " — " +
                    lesson.getStudentGroup().getId() + " — " +
                    lesson.getClassroom().getId();
        } else { // Grupo de Alumnos
            return lesson.getSubject().getName() + " — " +
                    lesson.getTeacher().getId() + " — " +
                    lesson.getClassroom().getId();
        }
    }

    /** Extrae el día del Lesson (ajusta a tu API real) */
    private String getLessonDay(Lesson l) {
        // TODO: adapta a tu TimePeriod real, por ejemplo:
        // return l.getTimePeriod().getDayName(); // "Lunes", ...
        return l.getTimePeriod().getWeekday(); // si tu TimePeriod tiene getDay() devolviendo "Lunes"... "Viernes"
    }

    /** Extrae el slot/etiqueta de hora del Lesson (ajusta a tu API real) */
    private String getLessonSlot(Lesson l) {
        // TODO: adapta a tu TimePeriod real, por ejemplo:
        // return l.getTimePeriod().getSlotLabel(); // "08:00–09:00"
        return l.getTimePeriod().getName(); // si tu TimePeriod tiene getName() con "08:00–09:00"
    }

    /** Crea el modelo de la tabla bloqueando la edición */
    private DefaultTableModel createTableModel(Object[][] data, String[] columnNames) {
        return new DefaultTableModel(data, columnNames) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    /** Construye la JTable con renderizado de descansos fusionados y celdas centradas */
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

    /** Panel lateral de color y etiqueta centrada (no usado ahora) */
    private JPanel createSidePanel(Color color, String text) {
        JPanel panel = new JPanel();
        panel.setBackground(color);
        panel.add(new JLabel(text, SwingConstants.CENTER));
        return panel;
    }

    private static final class ComboItem {
        final String id;
        final String label;
        ComboItem(String id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; } // shown in UI
    }

    /** Actualiza nombres según categoría y título */
    private void updateNames() {
        String cat = (String) categoryCombo.getSelectedItem();
        DefaultComboBoxModel<ComboItem> model = new DefaultComboBoxModel<>();

        if ("Profesores".equals(cat)) {
            for (var t : presentationControler.getTeachers()) {
                model.addElement(new ComboItem(t.getId(), t.getName())); // id + display name
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

    /** Crea panel de gestión (alta/baja) */
    private JPanel createManagementPanel(String title, DefaultListModel<String> model) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lbl = new JLabel(title );
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(lbl, BorderLayout.NORTH);

        JList<String> list = new JList<>(model);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JTextField txt = new JTextField(15);
        btns.add(txt);
        JButton add = new JButton("Agregar");
        JButton remove = new JButton("Borrar");
        btns.add(add);
        btns.add(remove);
        panel.add(btns, BorderLayout.SOUTH);

        add.addActionListener(e -> {
            String val = txt.getText().trim();
            if (!val.isEmpty() && !model.contains(val)) {
                model.addElement(val);
                txt.setText("");
            }
        });
        remove.addActionListener(e -> {
            String sel = list.getSelectedValue();
            if (sel != null) model.removeElement(sel);
        });
        return panel;
    }

    // Parses the row label (e.g., "09:00-10:00", "09:00–10:00", "9-10", "mon9", "tue11") to the START hour.
    private int parseRowStartHour(String label) {
        if (label == null) return -1;
        label = label.trim();

        // Case 1: "HH:MM-..." or "H:MM–..."
        try {
            String first = label.split("[–-]")[0].trim(); // split on hyphen or en-dash
            if (first.contains(":")) {
                return java.time.LocalTime.parse(first).getHour();
            }
        } catch (Exception ignore) {}

        // Case 2: trailing digits: "mon9", "tue11", "9"
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{1,2})$").matcher(label);
        if (m.find()) return Integer.parseInt(m.group(1));

        return -1;
    }

    private String lessonWeekday(Lesson l) {
        return l.getTimePeriod().getWeekday(); // "Lunes", "Martes", ...
    }


    private static String formatRange(java.time.LocalTime s, java.time.LocalTime e) {
        return String.format("%02d:%02d-%02d:%02d", s.getHour(), s.getMinute(), e.getHour(), e.getMinute());
    }

    // Build unique, sorted row labels from a schedule’s lessons
    private java.util.List<String> deriveTimeSlotsFromSchedule(Schedule schedule) {
        if (schedule == null || schedule.getLessons() == null) return java.util.List.of();
        java.util.TreeMap<java.time.LocalTime, java.time.LocalTime> ranges = new java.util.TreeMap<>();
        for (Lesson l : schedule.getLessons()) {
            var s = l.getTimePeriod().getInitialHour();
            var e = l.getTimePeriod().getFinalHour();
            ranges.merge(s, e, (oldE, newE) -> newE.isAfter(oldE) ? newE : oldE);
        }
        java.util.List<String> out = new java.util.ArrayList<>();
        for (var en : ranges.entrySet()) out.add(formatRange(en.getKey(), en.getValue()));
        return out;
    }


    /**
     * Reconstruye la lista de entidades (según la categoría seleccionada)
     * e intenta mantener la selección previa si sigue existiendo.
     * También refresca la tabla del calendario.
     */
    public void refreshEntities() {
        // Guardar selección actual por id
        ComboItem selected = (ComboItem) nameCombo.getSelectedItem();
        String prevId = (selected != null) ? selected.id : null;

        // Reconstruir el combo de entidades según la categoría
        updateNames();

        // Mantener/actualizar lista de IDs de horario (por si cambian entre soluciones)
        if (scheduleCombo != null) {
            reloadScheduleCombo();
            ensureValidEntitySelection();
        }


        // Intentar restaurar la selección anterior (si el id sigue en la lista)
        if (prevId != null) {
            ComboBoxModel<ComboItem> model = nameCombo.getModel();
            for (int i = 0; i < model.getSize(); i++) {
                ComboItem it = model.getElementAt(i);
                if (prevId.equals(it.id)) {
                    nameCombo.setSelectedIndex(i);
                    // Alinear estado interno y título
                    idEntity = it.id;
                    nameEntity = it.label;
                    calendarTitle.setText("Horario de " + nameEntity);
                    break;
                }
            }
        }

        // Volver a crear la tabla con los mismos tramos horarios (si aplica)
        refreshCalendarTable(lastTpNames);
    }

    /** Does the currently selected idEntity exist in the current category? */
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

    /** Ensure we have a valid selection in nameCombo; if not, pick the first one (or clear). */
    private void ensureValidEntitySelection() {
        if (isCurrentEntityValid()) return;

        // rebuild names list for current category
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





    // ---- helpers (put in Calendar.java) ----
    private static int toDayIndex(String d) {
        if (d == null) return -1;
        d = d.trim().toLowerCase();

        // Spanish
        if (d.startsWith("lu")) return 1;                 // Lunes
        if (d.startsWith("ma") && !d.startsWith("mi")) return 2; // Martes
        if (d.startsWith("mi")) return 3;                 // Miércoles/Miercoles
        if (d.startsWith("ju")) return 4;                 // Jueves
        if (d.startsWith("vi")) return 5;                 // Viernes

        // English (defensive)
        if (d.startsWith("mon")) return 1;
        if (d.startsWith("tue")) return 2;
        if (d.startsWith("wed")) return 3;
        if (d.startsWith("thu")) return 4;
        if (d.startsWith("fri")) return 5;

        // Numeric (1..5)
        if (d.matches("\\d")) {
            int v = Integer.parseInt(d);
            if (v >= 1 && v <= 5) return v;
        }
        return -1;
    }

    private static int lessonDayIndex(Lesson l) {
        return toDayIndex(l.getTimePeriod().getWeekDay());
    }

    private static int lessonStartHour(Lesson l) {           // 9..17 etc.
        return l.getTimePeriod().getInitialHour().getHour();
    }

    private static int rowStartHour(String slotLabel) {       // from "09:00-10:00"
        // assume labels like "09:00-10:00"; adjust if you use a different format
        int colon = slotLabel.indexOf(':');
        return colon > 0 ? Integer.parseInt(slotLabel.substring(0, colon)) : -1;
    }



}