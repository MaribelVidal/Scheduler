package presentation;

import business.Subject;
import business.StudentGroup;
import business.Teacher;
import business.TimePeriod;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

/**
 * ProfesoresPanel
 * - Edita TODOS los atributos del profesor (id, nombre, abreviatura, email, teléfono, depto., horas)
 * - Gestiona preferencias con peso por elemento:
 *   * Asignaturas: Preferidas / No preferidas  (peso por asignatura)
 *   * Grupos:      Preferidos / No preferidos  (peso por grupo)
 *   * Franjas:     Preferidas / No preferidas / No disponibles (peso por franja)
 *
 * UI:
 *  - "Añadir..." abre un selector con elementos que aún no están en ninguna lista del mismo tipo.
 *  - Click derecho en una "chip": Editar peso… | Quitar
 */
public class ProfesoresPanel extends JPanel {

    private final PresentationController presentationControler;

    // Left: teachers list
    private final DefaultListModel<Teacher> teachersModel = new DefaultListModel<>();
    private final JList<Teacher> teachersList = new JList<>(teachersModel);

    // Right-top: form
    private JTextField idField, nameField, abbrField, emailField, phoneField, deptField;
    private JSpinner hoursSpinner;
    private JButton newBtn, saveBtn, deleteBtn, revertBtn;
    private boolean creatingNew = false;
    private String currentTeacherId = null;

    // Right-bottom: preference panels (per-item weights)
    private WeightedChipPanel subjPreferredPanel;
    private WeightedChipPanel groupPreferredPanel, groupUnpreferredPanel;
    private WeightedChipPanel tpPreferredPanel, tpUnpreferredPanel, tpUnavailablePanel;

    // In-memory items (id, label, weight)
    private List<ItemRef> subjPrefItems = List.of();
    private List<ItemRef> groupPrefItems = List.of(), groupUnprefItems = List.of();
    private List<ItemRef> tpPrefItems = List.of(), tpUnprefItems = List.of(), tpUnavailItems = List.of();

    public ProfesoresPanel(PresentationController presentationControler) {
        super(new BorderLayout());
        this.presentationControler = presentationControler;
        buildUI();
        loadTeachers();
        if (!teachersModel.isEmpty()) {
            teachersList.setSelectedIndex(0);
            showTeacher(teachersModel.getElementAt(0));
        } else {
            setCreatingNew(true);
        }
    }

    // ===================== UI LAYOUT =====================
    private void buildUI() {
        setBorder(new EmptyBorder(10,10,10,10));

        // LEFT
        JPanel left = new JPanel(new BorderLayout(0,8));
        JLabel leftTitle = new JLabel("Profesores");
        leftTitle.setFont(leftTitle.getFont().deriveFont(Font.BOLD, 14f));
        left.add(leftTitle, BorderLayout.NORTH);

        teachersList.setCellRenderer(new TeacherRenderer());
        left.add(new JScrollPane(teachersList), BorderLayout.CENTER);

        teachersList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Teacher t = teachersList.getSelectedValue();
                if (t != null) showTeacher(t);
            }
        });

        // RIGHT
        JPanel right = new JPanel(new BorderLayout(0,10));
        right.add(buildTeacherFormUI(), BorderLayout.NORTH);
        right.add(buildPreferencesUI(), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, new JScrollPane(right));
        split.setResizeWeight(0.30);
        add(split, BorderLayout.CENTER);
    }

    private JComponent buildTeacherFormUI() {
        JPanel wrap = new JPanel(new BorderLayout(0,6));
        JLabel title = new JLabel("Ficha del profesor");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        wrap.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(6,6,6,6));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        int r=0;
        gc.gridx=0; gc.gridy=r; form.add(new JLabel("ID:"), gc);
        idField = new JTextField(18);
        gc.gridx=1; gc.gridy=r++; gc.weightx=1; form.add(idField, gc); gc.weightx=0;

        gc.gridx=0; gc.gridy=r; form.add(new JLabel("Nombre:"), gc);
        nameField = new JTextField(18);
        gc.gridx=1; gc.gridy=r++; gc.weightx=1; form.add(nameField, gc); gc.weightx=0;

        gc.gridx=0; gc.gridy=r; form.add(new JLabel("Abreviatura:"), gc);
        abbrField = new JTextField(10);
        gc.gridx=1; gc.gridy=r++; gc.weightx=1; form.add(abbrField, gc); gc.weightx=0;

        gc.gridx=0; gc.gridy=r; form.add(new JLabel("Email:"), gc);
        emailField = new JTextField(18);
        gc.gridx=1; gc.gridy=r++; gc.weightx=1; form.add(emailField, gc); gc.weightx=0;

        gc.gridx=0; gc.gridy=r; form.add(new JLabel("Teléfono:"), gc);
        phoneField = new JTextField(14);
        gc.gridx=1; gc.gridy=r++; gc.weightx=1; form.add(phoneField, gc); gc.weightx=0;

        gc.gridx=0; gc.gridy=r; form.add(new JLabel("Departamento:"), gc);
        deptField = new JTextField(14);
        gc.gridx=1; gc.gridy=r++; gc.weightx=1; form.add(deptField, gc); gc.weightx=0;

        gc.gridx=0; gc.gridy=r; form.add(new JLabel("Horas lectivas:"), gc);
        hoursSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 60, 1));
        gc.gridx=1; gc.gridy=r++; gc.weightx=1; form.add(hoursSpinner, gc);

        wrap.add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        newBtn = new JButton("Nuevo");
        saveBtn = new JButton("Guardar");
        revertBtn = new JButton("Revertir");
        deleteBtn = new JButton("Eliminar");
        btns.add(newBtn); btns.add(saveBtn); btns.add(revertBtn); btns.add(deleteBtn);
        wrap.add(btns, BorderLayout.SOUTH);

        newBtn.addActionListener(e -> { clearForm(); setCreatingNew(true); });
        saveBtn.addActionListener(e -> onSave());
        revertBtn.addActionListener(e -> {
            if (creatingNew) clearForm();
            else {
                Teacher t = findTeacherById(currentTeacherId);
                if (t != null) populateForm(t);
            }
        });
        deleteBtn.addActionListener(e -> onDelete());

        return wrap;
    }

    private JComponent buildPreferencesUI() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(8,8,8,8));

        root.add(buildLegend());

        // Subjects
        root.add(sectionTitle("Asignaturas"));
        JPanel subjRow = new JPanel(new GridLayout(1,2,8,8));
        subjPreferredPanel   = new WeightedChipPanel("Preferidas", Status.PREFERRED);
        subjRow.add(subjPreferredPanel);
        root.add(subjRow);
        root.add(Box.createVerticalStrut(8));

        // Groups
        root.add(sectionTitle("Grupos de alumnos"));
        JPanel grpRow = new JPanel(new GridLayout(1,2,8,8));
        groupPreferredPanel   = new WeightedChipPanel("Preferidos", Status.PREFERRED);
        groupUnpreferredPanel = new WeightedChipPanel("No preferidos", Status.UNPREFERRED);
        grpRow.add(groupPreferredPanel);
        grpRow.add(groupUnpreferredPanel);
        root.add(grpRow);
        root.add(Box.createVerticalStrut(8));

        // Time periods
        root.add(sectionTitle("Franjas horarias"));
        JPanel tpRow1 = new JPanel(new GridLayout(1,2,8,8));
        tpPreferredPanel   = new WeightedChipPanel("Preferidas", Status.PREFERRED);
        tpUnpreferredPanel = new WeightedChipPanel("No preferidas", Status.UNPREFERRED);
        tpRow1.add(tpPreferredPanel);
        tpRow1.add(tpUnpreferredPanel);
        JPanel tpRow2 = new JPanel(new GridLayout(1,1,8,8));
        tpUnavailablePanel = new WeightedChipPanel("No disponibles", Status.UNAVAILABLE);
        tpRow2.add(tpUnavailablePanel);
        root.add(tpRow1);
        root.add(Box.createVerticalStrut(4));
        root.add(tpRow2);

        // Wire actions
        subjPreferredPanel.setOnAdd(e -> onAddSubjects(true));
        subjPreferredPanel.setOnRemove(id -> onRemoveSubject(true, id));
        subjPreferredPanel.setOnEditWeight(ir -> onEditWeightSubject(true, ir));

        groupPreferredPanel.setOnAdd(e -> onAddGroups(true));
        groupUnpreferredPanel.setOnAdd(e -> onAddGroups(false));
        groupPreferredPanel.setOnRemove(id -> onRemoveGroup(true, id));
        groupUnpreferredPanel.setOnRemove(id -> onRemoveGroup(false, id));
        groupPreferredPanel.setOnEditWeight(ir -> onEditWeightGroup(true, ir));
        groupUnpreferredPanel.setOnEditWeight(ir -> onEditWeightGroup(false, ir));

        tpPreferredPanel.setOnAdd(e -> onAddTimePeriods(Status.PREFERRED));
        tpUnpreferredPanel.setOnAdd(e -> onAddTimePeriods(Status.UNPREFERRED));
        tpUnavailablePanel.setOnAdd(e -> onAddTimePeriods(Status.UNAVAILABLE));
        tpPreferredPanel.setOnRemove(id -> onRemoveTimePeriod(Status.PREFERRED, id));
        tpUnpreferredPanel.setOnRemove(id -> onRemoveTimePeriod(Status.UNPREFERRED, id));
        tpUnavailablePanel.setOnRemove(id -> onRemoveTimePeriod(Status.UNAVAILABLE, id));
        tpPreferredPanel.setOnEditWeight(ir -> onEditWeightTP(Status.PREFERRED, ir));
        tpUnpreferredPanel.setOnEditWeight(ir -> onEditWeightTP(Status.UNPREFERRED, ir));
        tpUnavailablePanel.setOnEditWeight(ir -> onEditWeightTP(Status.UNAVAILABLE, ir));

        return root;
    }

    private JLabel sectionTitle(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 14f));
        l.setBorder(new EmptyBorder(6,0,4,0));
        return l;
    }

    private JComponent buildLegend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        p.add(new LegendDot(Status.PREFERRED));   p.add(new JLabel("Preferida/o"));
        p.add(new LegendDot(Status.UNPREFERRED)); p.add(new JLabel("No preferida/o"));
        p.add(new LegendDot(Status.UNAVAILABLE)); p.add(new JLabel("No disponible"));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    // ===================== DATA FLOW =====================
    private void loadTeachers() {
        teachersModel.clear();
        try {
            List<Teacher> list = presentationControler.getTeachers();
            if (list != null) list.forEach(teachersModel::addElement);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar profesores: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTeacher(Teacher t) {
        if (t == null) return;
        populateForm(t);
        setCreatingNew(false);
        currentTeacherId = t.getId();
        loadTeacherPreferences(currentTeacherId);
    }

    private void populateForm(Teacher t) {
        idField.setText(nz(t.getId()));
        nameField.setText(nz(t.getName()));
        abbrField.setText(nz(t.getAbbreviation()));
        emailField.setText(nz(t.getEmail()));
        phoneField.setText(nz(t.getPhone()));
        deptField.setText(nz(t.getDepartment()));
        try { hoursSpinner.setValue(t.getHoursWork()); } catch (Exception ignore) { hoursSpinner.setValue(0); }
    }

    private void clearForm() {
        idField.setText(""); nameField.setText(""); abbrField.setText("");
        emailField.setText(""); phoneField.setText(""); deptField.setText("");
        hoursSpinner.setValue(0);
        currentTeacherId = null;

        groupPrefItems = groupUnprefItems = List.of();
        tpPrefItems = tpUnprefItems = tpUnavailItems = List.of();

        if (subjPreferredPanel != null) subjPreferredPanel.setItems(List.of());
        if (groupPreferredPanel != null) groupPreferredPanel.setItems(List.of());
        if (groupUnpreferredPanel != null) groupUnpreferredPanel.setItems(List.of());
        if (tpPreferredPanel != null) tpPreferredPanel.setItems(List.of());
        if (tpUnpreferredPanel != null) tpUnpreferredPanel.setItems(List.of());
        if (tpUnavailablePanel != null) tpUnavailablePanel.setItems(List.of());
    }

    private void onSave() {
        try {
            String id   = trimOrNull(idField.getText());
            String name = trimOrNull(nameField.getText());
            String abbr = trimOrNull(abbrField.getText());

            if (id == null || id.isEmpty()) { warn("El ID es obligatorio."); idField.requestFocus(); return; }
            if (name == null || name.isEmpty()) { warn("El nombre es obligatorio."); nameField.requestFocus(); return; }
            if (creatingNew && (abbr == null || abbr.isEmpty())) { warn("La abreviatura es obligatoria."); abbrField.requestFocus(); return; }

            if (creatingNew) {
                if (findTeacherById(id) != null) { warn("Ya existe un profesor con ese ID."); idField.requestFocus(); return; }
                Teacher t = new Teacher(id, name, abbr);
                readOptionalFieldsInto(t);
                presentationControler.addTeacher(t);
                teachersModel.addElement(t);
                teachersList.setSelectedValue(t, true);
                setCreatingNew(false);
                currentTeacherId = t.getId();
            } else {
                Teacher t = findTeacherById(currentTeacherId);
                if (t == null) { error("No se encontró el profesor."); return; }
                t.setName(name);
                t.setAbbreviation(abbr);
                readOptionalFieldsInto(t);
                presentationControler.updateTeacher(t);
                teachersList.repaint();
            }
            JOptionPane.showMessageDialog(this, "Guardado correctamente.", "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            error("Error al guardar: " + ex.getMessage());
        }
    }

    private void onDelete() {
        if (creatingNew) { clearForm(); return; }
        Teacher t = findTeacherById(currentTeacherId);
        if (t == null) return;
        int ok = JOptionPane.showConfirmDialog(this, "¿Eliminar al profesor \""+t.getName()+"\"?",
                "Confirmar", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        try {
            presentationControler.deleteTeacher(t.getId());
            teachersModel.removeElement(t);
            clearForm();
            setCreatingNew(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            error("Error al eliminar: " + ex.getMessage());
        }
    }

    private Teacher readOptionalFieldsInto(Teacher t) {
        t.setEmail(trimOrNull(emailField.getText()));
        t.setPhone(trimOrNull(phoneField.getText()));
        t.setDepartment(trimOrNull(deptField.getText()));
        t.setHoursWork((Integer) hoursSpinner.getValue());
        return t;
    }

    private Teacher findTeacherById(String id) {
        if (id == null) return null;
        for (int i = 0; i < teachersModel.size(); i++) {
            Teacher t = teachersModel.elementAt(i);
            if (id.equals(t.getId())) return t;
        }
        return null;
    }

    // ===================== PREFERENCES (per-item weights) =====================
    private void loadTeacherPreferences(String teacherId) {
        if (teacherId == null) {
            clearForm();
            return;
        }

        // Subjects
        List<Subject> prefSubj   = nvl(presentationControler.getTeacherPreferredSubjects(teacherId));
        Map<String,Integer> prefSubjW   = nvlMap(presentationControler.getTeacherPreferredSubjectWeights(teacherId));
        subjPrefItems   = mapSubjects(prefSubj,   prefSubjW);
        subjPreferredPanel.setItems(subjPrefItems);

        // Groups
        List<StudentGroup> prefGroups   = nvl(presentationControler.getTeacherPreferredStudentGroups(teacherId));
        List<StudentGroup> unprefGroups = nvl(presentationControler.getTeacherUnpreferredStudentGroups(teacherId));
        Map<String,Integer> prefGroupsW   = nvlMap(presentationControler.getTeacherPreferredStudentGroupWeights(teacherId));
        Map<String,Integer> unprefGroupsW = nvlMap(presentationControler.getTeacherUnpreferredStudentGroupWeights(teacherId));
        groupPrefItems   = mapGroups(prefGroups,   prefGroupsW);
        groupUnprefItems = mapGroups(unprefGroups, unprefGroupsW);
        groupPreferredPanel.setItems(groupPrefItems);
        groupUnpreferredPanel.setItems(groupUnprefItems);

        // Time periods
        List<TimePeriod> prefTP    = nvl(presentationControler.getTeacherPreferredTimePeriods(teacherId));
        List<TimePeriod> unprefTP  = nvl(presentationControler.getTeacherUnpreferredTimePeriods(teacherId));
        List<TimePeriod> unavailTP = nvl(presentationControler.getTeacherUnavailableTimePeriods(teacherId));
        Map<String,Integer> prefTPW    = nvlMap(presentationControler.getTeacherPreferredTimePeriodWeights(teacherId));
        Map<String,Integer> unprefTPW  = nvlMap(presentationControler.getTeacherUnpreferredTimePeriodWeights(teacherId));
        tpPrefItems    = mapTimePeriods(prefTP,    prefTPW);
        tpUnprefItems  = mapTimePeriods(unprefTP,  unprefTPW);
        tpPreferredPanel.setItems(tpPrefItems);
        tpUnpreferredPanel.setItems(tpUnprefItems);
        tpUnavailablePanel.setItems(tpUnavailItems);
    }

    // ---- Add handlers (ask for weight) ----
    private void onAddSubjects(boolean preferred) {
        ensureTeacherSelected();
        Set<String> exclude = new HashSet<>();
        subjPrefItems.forEach(i -> exclude.add(i.id));

        List<Subject> all = nvl(presentationControler.getSubjects());
        List<ItemRef> available = new ArrayList<>();
        for (Subject s : all) if (!exclude.contains(s.getId())) available.add(new ItemRef(s.getId(), s.getName(), 1));

        List<ItemRef> picked = pickFromList("Seleccionar asignaturas", available);
        if (picked.isEmpty()) return;

        try {
            for (ItemRef it : picked) {
                int w = askWeight("Peso para \"" + it.label + "\"", 1);
                if (w < 0) continue;
                presentationControler.addTeacherPreferredSubject(currentTeacherId, it.id, w);
            }
            loadTeacherPreferences(currentTeacherId);
        } catch (Exception ex) { ex.printStackTrace(); error("Error al agregar asignaturas: " + ex.getMessage()); }
    }

    private void onAddGroups(boolean preferred) {
        ensureTeacherSelected();
        Set<String> exclude = new HashSet<>();
        groupPrefItems.forEach(i -> exclude.add(i.id));
        groupUnprefItems.forEach(i -> exclude.add(i.id));

        List<StudentGroup> all = nvl(presentationControler.getStudentGroups());
        List<ItemRef> available = new ArrayList<>();
        for (StudentGroup g : all) if (!exclude.contains(g.getId())) available.add(new ItemRef(g.getId(), g.getName(), 1));

        List<ItemRef> picked = pickFromList("Seleccionar grupos", available);
        if (picked.isEmpty()) return;

        try {
            for (ItemRef it : picked) {
                int w = askWeight("Peso para \"" + it.label + "\"", 1);
                if (w < 0) continue;
                if (preferred) presentationControler.addTeacherPreferredStudentGroup(currentTeacherId, it.id, w);
                else           presentationControler.addTeacherUnpreferredStudentGroup(currentTeacherId, it.id, w);
            }
            loadTeacherPreferences(currentTeacherId);
        } catch (Exception ex) { ex.printStackTrace(); error("Error al agregar grupos: " + ex.getMessage()); }
    }

    private void onAddTimePeriods(Status status) {
        ensureTeacherSelected();
        Set<String> exclude = new HashSet<>();
        tpPrefItems.forEach(i -> exclude.add(i.id));
        tpUnprefItems.forEach(i -> exclude.add(i.id));
        tpUnavailItems.forEach(i -> exclude.add(i.id));

        List<TimePeriod> all = nvl(presentationControler.getTimePeriods());
        List<ItemRef> available = new ArrayList<>();
        for (TimePeriod tp : all) if (!exclude.contains(tp.getId())) available.add(new ItemRef(tp.getId(), tpLabel(tp), 1));

        List<ItemRef> picked = pickFromList("Seleccionar franjas", available);
        if (picked.isEmpty()) return;

        try {
            for (ItemRef it : picked) {
                int w = askWeight("Peso para \"" + it.label + "\"", 1);
                if (w < 0) continue;
                switch (status) {
                    case PREFERRED   -> presentationControler.addTeacherPreferredTimePeriod(currentTeacherId, it.id, w);
                    case UNPREFERRED -> presentationControler.addTeacherUnpreferredTimePeriod(currentTeacherId, it.id, w);
                    case UNAVAILABLE -> presentationControler.addTeacherUnavailableTimePeriod(currentTeacherId, it.id);
                }
            }
            loadTeacherPreferences(currentTeacherId);
        } catch (Exception ex) { ex.printStackTrace(); error("Error al agregar franjas: " + ex.getMessage()); }
    }

    // ---- Remove handlers ----
    private void onRemoveSubject(boolean preferred, String subjectId) {
        ensureTeacherSelected();
        try {
            presentationControler.removeTeacherPreferredSubject(currentTeacherId, subjectId);
            loadTeacherPreferences(currentTeacherId);
        } catch (Exception ex) { ex.printStackTrace(); error("Error al quitar asignatura: " + ex.getMessage()); }
    }

    private void onRemoveGroup(boolean preferred, String groupId) {
        ensureTeacherSelected();
        try {
            if (preferred) presentationControler.removeTeacherPreferredStudentGroup(currentTeacherId, groupId);
            else           presentationControler.removeTeacherUnpreferredStudentGroup(currentTeacherId, groupId);
            loadTeacherPreferences(currentTeacherId);
        } catch (Exception ex) { ex.printStackTrace(); error("Error al quitar grupo: " + ex.getMessage()); }
    }

    private void onRemoveTimePeriod(Status status, String tpId) {
        ensureTeacherSelected();
        try {
            switch (status) {
                case PREFERRED   -> presentationControler.removeTeacherPreferredTimePeriod(currentTeacherId, tpId);
                case UNPREFERRED -> presentationControler.removeTeacherUnpreferredTimePeriod(currentTeacherId, tpId);
                case UNAVAILABLE -> presentationControler.removeTeacherUnavailableTimePeriod(currentTeacherId, tpId);
            }
            loadTeacherPreferences(currentTeacherId);
        } catch (Exception ex) { ex.printStackTrace(); error("Error al quitar franja: " + ex.getMessage()); }
    }

    // ---- Edit weight handlers ----
    private void onEditWeightSubject(boolean preferred, ItemRef ir) {
        int w = askWeight("Peso de \"" + ir.label + "\"", ir.weight);
        if (w < 0) return;
        try {
            presentationControler.updateTeacherPreferredSubjectWeight(currentTeacherId, ir.id, w);
            loadTeacherPreferences(currentTeacherId);
        } catch (Exception ex) { ex.printStackTrace(); error("Error al editar peso: " + ex.getMessage()); }
    }

    private void onEditWeightGroup(boolean preferred, ItemRef ir) {
        int w = askWeight("Peso de \"" + ir.label + "\"", ir.weight);
        if (w < 0) return;
        try {
            if (preferred) presentationControler.updateTeacherPreferredStudentGroupWeight(currentTeacherId, ir.id, w);
            else           presentationControler.updateTeacherUnpreferredStudentGroupWeight(currentTeacherId, ir.id, w);
            loadTeacherPreferences(currentTeacherId);
        } catch (Exception ex) { ex.printStackTrace(); error("Error al editar peso: " + ex.getMessage()); }
    }

    private void onEditWeightTP(Status status, ItemRef ir) {
        int w = askWeight("Peso de \"" + ir.label + "\"", ir.weight);
        if (w < 0) return;
        try {
            switch (status) {
                case PREFERRED   -> presentationControler.updateTeacherPreferredTimePeriodWeight(currentTeacherId, ir.id, w);
                case UNPREFERRED -> presentationControler.updateTeacherUnpreferredTimePeriodWeight(currentTeacherId, ir.id, w);
            }
            loadTeacherPreferences(currentTeacherId);
        } catch (Exception ex) { ex.printStackTrace(); error("Error al editar peso: " + ex.getMessage()); }
    }

    // ===================== Helpers =====================
    private void ensureTeacherSelected() {
        if (currentTeacherId == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un profesor primero.", "Información", JOptionPane.INFORMATION_MESSAGE);
            throw new IllegalStateException("No teacher selected");
        }
    }

    private List<ItemRef> pickFromList(String title, List<ItemRef> available) {
        if (available == null || available.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay elementos disponibles para agregar.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return List.of();
        }
        DefaultListModel<ItemRef> model = new DefaultListModel<>();
        for (ItemRef it : available) model.addElement(it);

        JList<ItemRef> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ItemRef ir) setText(ir.label);
                return c;
            }
        });

        JScrollPane sp = new JScrollPane(list);
        sp.setPreferredSize(new Dimension(360, 300));
        int ok = JOptionPane.showConfirmDialog(this, sp, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return List.of();

        return new ArrayList<>(list.getSelectedValuesList());
    }

    private List<ItemRef> mapSubjects(List<Subject> list, Map<String,Integer> weights) {
        List<ItemRef> out = new ArrayList<>();
        for (Subject s : list) out.add(new ItemRef(s.getId(), s.getName(), weights.getOrDefault(s.getId(), 1)));
        return out;
    }
    private List<ItemRef> mapGroups(List<StudentGroup> list, Map<String,Integer> weights) {
        List<ItemRef> out = new ArrayList<>();
        for (StudentGroup g : list) out.add(new ItemRef(g.getId(), g.getName(), weights.getOrDefault(g.getId(), 1)));
        return out;
    }
    private List<ItemRef> mapTimePeriods(List<TimePeriod> list, Map<String,Integer> weights) {
        List<ItemRef> out = new ArrayList<>();
        for (TimePeriod tp : list) out.add(new ItemRef(tp.getId(), tpLabel(tp), weights.getOrDefault(tp.getId(), 1)));
        return out;
    }

    private String tpLabel(TimePeriod tp) {
        String day = abbreviateDay(tp.getWeekday());
        LocalTime s = tp.getInitialHour(), e = tp.getFinalHour();
        return String.format("%s %02d:%02d–%02d:%02d", day, s.getHour(), s.getMinute(), e.getHour(), e.getMinute());
    }

    private String abbreviateDay(String d) {
        if (d == null) return "";
        String s = d.toLowerCase(Locale.ROOT).trim();
        if (s.startsWith("lu")) return "Lun";
        if (s.startsWith("ma") && !s.startsWith("mi")) return "Mar";
        if (s.startsWith("mi")) return "Mié";
        if (s.startsWith("ju")) return "Jue";
        if (s.startsWith("vi")) return "Vie";
        if (s.startsWith("mon")) return "Mon";
        if (s.startsWith("tue")) return "Tue";
        if (s.startsWith("wed")) return "Wed";
        if (s.startsWith("thu")) return "Thu";
        if (s.startsWith("fri")) return "Fri";
        return d;
    }

    private int askWeight(String title, int current) {
        JSpinner sp = new JSpinner(new SpinnerNumberModel(current, 0, 100, 1));
        int ok = JOptionPane.showConfirmDialog(this, sp, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return ok == JOptionPane.OK_OPTION ? (Integer) sp.getValue() : -1;
    }

    private String trimOrNull(String s){ if (s==null) return null; s=s.trim(); return s.isEmpty()?null:s; }
    private String nz(Object o){ return o==null?"":String.valueOf(o); }
    private void warn(String m){ JOptionPane.showMessageDialog(this, m, "Validación", JOptionPane.WARNING_MESSAGE); }
    private void error(String m){ JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE); }

    private <T> List<T> nvl(List<T> l){ return (l==null)?List.of():l; }
    private Map<String,Integer> nvlMap(Map<String,Integer> m){ return (m==null)?Map.of():m; }

    // ===================== Small components =====================
    private enum Status { PREFERRED, UNPREFERRED, UNAVAILABLE }

    private static class ItemRef {
        final String id;
        final String label;
        final int weight;
        ItemRef(String id, String label, int weight){ this.id = id; this.label = label; this.weight = weight; }
        @Override public String toString(){ return label; }
    }

    private static class WeightedChipPanel extends JPanel {
        private final JLabel title;
        private final JPanel flow;
        private final JButton addBtn;
        private final Status status;

        private Consumer<String> onRemove = id -> {};
        private Consumer<ItemRef> onEditWeight = ir -> {};

        WeightedChipPanel(String titleText, Status s) {
            super(new BorderLayout(0,4));
            this.status = s;
            title = new JLabel(titleText);
            title.setFont(title.getFont().deriveFont(Font.PLAIN, 12f));
            title.setBorder(new EmptyBorder(0,2,0,0));

            addBtn = new JButton("Añadir...");
            JPanel top = new JPanel(new BorderLayout());
            top.add(title, BorderLayout.WEST);
            top.add(addBtn, BorderLayout.EAST);
            add(top, BorderLayout.NORTH);

            flow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
            flow.setOpaque(false);
            JScrollPane sp = new JScrollPane(flow,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            sp.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(235,235,235)),
                    new EmptyBorder(4,4,4,4)));
            add(sp, BorderLayout.CENTER);
        }

        void setOnAdd(java.awt.event.ActionListener a){ addBtn.addActionListener(a); }
        void setOnRemove(Consumer<String> c){ this.onRemove = (c==null)?id->{}:c; }
        void setOnEditWeight(Consumer<ItemRef> c){ this.onEditWeight = (c==null)?ir->{}:c; }

        void setItems(List<ItemRef> items) {
            flow.removeAll();
            if (items == null || items.isEmpty()) {
                JLabel empty = new JLabel("—");
                empty.setForeground(Color.GRAY);
                flow.add(empty);
            } else {
                for (ItemRef it : items) flow.add(makeChip(it));
            }
            revalidate(); repaint();
        }

        private JComponent makeChip(ItemRef it) {
            JLabel txt = new JLabel(it.label + "  (w=" + it.weight + ")");
            txt.setBorder(new EmptyBorder(4,8,4,8));

            JComponent chip = new JComponent(){};
            chip.setLayout(new BorderLayout());
            chip.add(txt, BorderLayout.CENTER);
            chip.setBorder(new EmptyBorder(0,0,0,0));
            chip.setOpaque(false);

            JComponent finalChip = chip;
            chip.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { maybePopup(e); }
                @Override public void mouseReleased(MouseEvent e) { maybePopup(e); }
                private void maybePopup(MouseEvent e) {
                    if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                        JPopupMenu pm = new JPopupMenu();
                        JMenuItem edit = new JMenuItem("Editar peso…");
                        edit.addActionListener(ev -> onEditWeight.accept(it));
                        JMenuItem remove = new JMenuItem("Quitar");
                        remove.addActionListener(ev -> onRemove.accept(it.id));
                        pm.add(edit); pm.add(remove);
                        pm.show(finalChip, e.getX(), e.getY());
                    }
                }
            });

            // paint rounded background
            chip = wrapRounded(chip, color(status), border(status));
            return chip;
        }

        private static JComponent wrapRounded(JComponent inner, Color bg, Color br) {
            return new RoundedWrapper(inner, bg, br);
        }

        private static Color color(Status s) {
            return switch (s) {
                case PREFERRED -> new Color(0xD6F5D6);
                case UNPREFERRED -> new Color(0xFFE9C6);
                case UNAVAILABLE -> new Color(0xFFD6D6);
            };
        }
        private static Color border(Status s) {
            return switch (s) {
                case PREFERRED -> new Color(0x55AA55);
                case UNPREFERRED -> new Color(0xCC8A00);
                case UNAVAILABLE -> new Color(0xCC5555);
            };
        }

        private static class RoundedWrapper extends JComponent {
            private final JComponent inner;
            private final Color bg, br;
            RoundedWrapper(JComponent inner, Color bg, Color br){
                this.inner = inner; this.bg = bg; this.br = br;
                setLayout(new BorderLayout());
                add(inner, BorderLayout.CENTER);
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(bg); g2.fillRoundRect(0,0,w-1,h-1,16,16);
                g2.setColor(br); g2.drawRoundRect(0,0,w-1,h-1,16,16);
                g2.dispose();
                super.paintComponent(g);
            }
        }
    }

    private static class LegendDot extends JComponent {
        private final Status status;
        LegendDot(Status s){ this.status = s; setPreferredSize(new Dimension(12,12)); }
        @Override protected void paintComponent(Graphics g){
            g.setColor(WeightedChipPanel.border(status)); g.fillOval(0,0,getWidth(),getHeight());
        }
    }

    private static class TeacherRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Teacher t) {
                String id = String.valueOf(t.getId());
                String name = String.valueOf(t.getName());
                setText("<html><b>" + esc(name) + "</b><br><span style='color:gray;'>"+esc(id)+"</span></html>");
            }
            return c;
        }
        private String esc(String s){ return s==null?"":s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;"); }
    }

    // Put this inside ProfesoresPanel (e.g., near other small helpers)
    private void setCreatingNew(boolean creating) {
        this.creatingNew = creating;
        // ID is only editable when creating a brand-new teacher
        idField.setEditable(creating);
        if (creating) {
            currentTeacherId = null;
        }
    }

}
