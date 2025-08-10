/**
 * Clase principal que muestra un calendario semanal con horarios y descansos.
 * Permite seleccionar diferentes categorías (Profesores, Clases, Alumnos)
 * y muestra dinámicamente el horario correspondiente.
 */
package presentation;

import business.Schedule;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;

public class Calendar extends JFrame {
    private JComboBox<String> categoryCombo;
    private JComboBox<String> nameCombo;
    private JLabel calendarTitle;

    /**
     * Índices de fila que corresponden a un descanso
     */
    private final Set<Integer> breakRows = Set.of(2, 5);

    /**
     * Mapa de categorías → lista de nombres
     */
    private final Map<String, String[]> dataMap = new LinkedHashMap<>();

    /** Modelos de lista para gestión */
    private final DefaultListModel<String> profesoresModel = new DefaultListModel<>();
    private final DefaultListModel<String> clasesModel = new DefaultListModel<>();
    private final DefaultListModel<String> alumnosModel = new DefaultListModel<>();

    private String typeEntity;
    private String nameEntity;
    private String idEntity;
    private int idSchedule;

    //Todo: Hace falta poner el valor correcto de la entidad seleccionada


    /**
     * Constructor: inicializa la ventana y los componentes UI
     */
    public Calendar() {
        super("Calendario Semanal");
        //initData(teachersNames, classroomsNames, studentsGroupsNames);
        //initUI();
    }


    public void init(List<String> teachersNames, List<String> classroomsNames, List<String> studentsGroupsNames, List<String> tpNames) {
        // Inicializar el mapa de datos con los nombres de ejemplo
        initData(teachersNames, classroomsNames, studentsGroupsNames);
        // Configurar la interfaz gráfica
        initUI(tpNames);
    }


    /**
     * Rellena el map de datos con valores de ejemplo
     */


    private void initData(List<String> teachersNames, List<String> classroomsNames, List<String> studentsGroupsNames) {
        dataMap.put("Profesores", teachersNames.toArray(new String[0]));
        dataMap.put("Aulas",     classroomsNames.toArray(new String[0]));
        dataMap.put("Grupos de Alumnos",    studentsGroupsNames.toArray(new String[0]));

        // Inicializar modelos de lista
        for (String p : dataMap.get("Profesores")) profesoresModel.addElement(p);
        for (String c : dataMap.get("Aulas"))     clasesModel.addElement(c);
        for (String a : dataMap.get("Grupos de Alumnos"))    alumnosModel.addElement(a);
    }

    /**
     * Configura la interfaz gráfica: paneles, tablas y listeners
     */
    private void initUI(List<String> tpNames) {
        /*
        setLayout(new BorderLayout());

        add(createNorthPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        //add(createSidePanel(Color.GREEN, "WEST"), BorderLayout.WEST);
        //add(createSidePanel(Color.ORANGE, "EAST"), BorderLayout.EAST);
        add(createSidePanel(Color.BLUE, "SOUTH"), BorderLayout.SOUTH);

        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

         */

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Horario", createCalendarPanel(tpNames));
        tabs.addTab("Profesores", createManagementPanel("Profesor", profesoresModel));
        tabs.addTab("Clases", createManagementPanel("Clase", clasesModel));
        tabs.addTab("Alumnos", createManagementPanel("Alumno", alumnosModel));

        add(tabs);

        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /** Crea el panel del calendario con selector y tabla */
    private JPanel createCalendarPanel(List<String> tpNames) {
        JPanel main = new JPanel(new BorderLayout());

        // NORTH
        main.add(createNorthPanel(), BorderLayout.NORTH);
        // CENTER
        main.add(createCenterPanel(tpNames), BorderLayout.CENTER);
        //add(createSidePanel(Color.GREEN, "WEST"), BorderLayout.WEST);
        //add(createSidePanel(Color.ORANGE, "EAST"), BorderLayout.EAST);
       // add(createSidePanel(Color.BLUE, "SOUTH"), BorderLayout.SOUTH);

        return main;
    }

    /**
     * Construye el panel superior con los JComboBox y el título dinámico
     *
     * @return JPanel configurado en BorderLayout
     */
    private JPanel createNorthPanel() {
        JPanel north = new JPanel(new BorderLayout(0, 5));
        north.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de selectores
        JPanel selectors = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        selectors.add(new JLabel("Categoría:"));
        categoryCombo = new JComboBox<>(dataMap.keySet().toArray(new String[0]));
        selectors.add(categoryCombo);

        selectors.add(new JLabel("Nombre:"));
        nameCombo = new JComboBox<>();
        selectors.add(nameCombo);
        north.add(selectors, BorderLayout.NORTH);

        // Título dinámico
        calendarTitle = new JLabel("", SwingConstants.CENTER);
        calendarTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        north.add(calendarTitle, BorderLayout.SOUTH);

        // Listeners
        categoryCombo.addActionListener(e -> updateNames());
        nameCombo.addActionListener(e ->
                calendarTitle.setText("Horario de " + nameCombo.getSelectedItem())
        );
        updateNames();

        return north;
    }

    /**
     * Construye el panel central con la JTable del horario
     *
     * @return JScrollPane contenedor de la tabla
     */
    private JScrollPane createCenterPanel(List<String> tpNames) {
        String[] columnNames = {"", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};
        String[] timeSlots = tpNames.toArray(new String[0]);


        Object[][] data = new Object[timeSlots.length][columnNames.length];
        Schedule schedule = null;
        if (typeEntity == "Profesor"){
            schedule = presentationControler.getTeacherSchedule(idEntity, idSchedule);

        }

        if (typeEntity == "Clase"){
            schedule = presentationControler.getClassroomSchedule(idEntity, idSchedule);
        }

        if (typeEntity == "Grupo de Alumnos"){
            schedule = presentationControler.getStudentGroupSchedule(idEntity, idSchedule);
        }



        for (int i = 0; i < timeSlots.length; i++) {
            data[i][0] = timeSlots[i];
            for (int j = 1; j < columnNames.length; j++) {
                data[i][j] = "asd";
            }
        }

        DefaultTableModel model = createTableModel(data, columnNames);
        JTable table = createTable(model, columnNames.length);

        JScrollPane scroll = new JScrollPane(table);
        return scroll;
    }

    /**
     * Crea el modelo de la tabla bloqueando la edición donde corresponda
     *
     * @param data         datos de la tabla
     * @param columnNames  nombres de columnas
     * @return DefaultTableModel configurado
     */
    private DefaultTableModel createTableModel(Object[][] data, String[] columnNames) {
        return new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    /**
     * Construye la JTable con renderizado de descansos fusionados y celdas centradas
     *
     * @param model         modelo de datos
     * @param columnCount   número de columnas en la tabla
     * @return JTable configurada
     */
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
        ((DefaultTableCellRenderer)
                table.getTableHeader().getDefaultRenderer()
        ).setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        return table;
    }

    /**
     * Construye un panel lateral de color y etiqueta centrada
     *
     * @param color   fondo del panel
     * @param text    texto a mostrar
     * @return JPanel configurado
     */
    private JPanel createSidePanel(Color color, String text) {
        JPanel panel = new JPanel();
        panel.setBackground(color);
        panel.add(new JLabel(text, SwingConstants.CENTER));
        return panel;
    }

    /**
     * Actualiza el combo de nombres según la categoría seleccionada
     * y establece el título correspondiente
     */
    private void updateNames() {
        String cat = (String) categoryCombo.getSelectedItem();
        String[] names = dataMap.getOrDefault(cat, new String[0]);
        nameCombo.setModel(new DefaultComboBoxModel<>(names));
        if (names.length > 0) {
            calendarTitle.setText("Horario de " + names[0]);
        } else {
            calendarTitle.setText("Horario");
        }
    }

    /**
     * Crea un panel de gestión (alta/baja) con lista y botones
     * @param title   Nombre de la entidad (Profesor, Clase, Alumno)
     * @param model   Modelo de lista correspondiente
     * @return JPanel con la UI de gestión
     */
    private JPanel createManagementPanel(String title, DefaultListModel<String> model) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lbl = new JLabel(title + "s");
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

        // Listeners de alta/baja
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



}
