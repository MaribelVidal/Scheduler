package business;

import org.chocosolver.solver.exception.ContradictionException;
import persistence.PersistenceController;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

public class BusinessController {

    private final PersistenceController persistenceController;

    // In-memory caches
    private List<Teacher> teachers;
    private List<StudentGroup> studentGroups;
    private List<Subject> subjects;
    private List<Classroom> classrooms;
    private List<TimePeriod> timePeriods;
    private List<Schedule> schedules;


    public BusinessController() throws Exception {
        this.persistenceController = new PersistenceController();
        this.persistenceController.initialize();
        getFromDataBase();
        // Ensure non-null lists
        if (teachers == null) teachers = new ArrayList<>();
        if (studentGroups == null) studentGroups = new ArrayList<>();
        if (subjects == null) subjects = new ArrayList<>();
        if (classrooms == null) classrooms = new ArrayList<>();
        if (timePeriods == null) timePeriods = new ArrayList<>();
        if (schedules == null) schedules = new ArrayList<>();

    }




    // ========================= Load / DB =========================
    private void getFromDataBase() throws Exception {
        teachers      = persistenceController.getAll(Teacher.class);
        studentGroups = persistenceController.getAll(StudentGroup.class);
        subjects      = persistenceController.getAll(Subject.class);
        classrooms    = persistenceController.getAll(Classroom.class);
        timePeriods   = persistenceController.getAll(TimePeriod.class);
    }

    // ========================= Basic getters =========================

    public List<Teacher> getTeachers()           { return teachers; }
    public List<Classroom> getClassrooms()       { return classrooms; }
    public List<StudentGroup> getStudentGroups() { return studentGroups; }
    public List<Subject> getSubjects()           { return subjects; }
    public List<TimePeriod> getTimePeriods()     { return timePeriods; }

    public List<String> getTeachersNames() {
        return teachers.stream().map(Teacher::getName).toList();
    }
    public List<String> getClassroomsNames() {
        return classrooms.stream().map(Classroom::getName).toList();
    }
    public List<String> getStudentGroupsNames() {
        return studentGroups.stream().map(StudentGroup::getName).toList();
    }
    public List<String> getSubjectsNames() {
        return subjects.stream().map(Subject::getName).toList();
    }

    // Optional helper used somewhere in UI
    public List<String> getTPNames() {
        List<String> out = new ArrayList<>();
        if (timePeriods == null) return out;
        // Build labels for first weekday found
        String firstDay = null;
        for (TimePeriod tp : timePeriods) {
            if (firstDay == null) firstDay = tp.getWeekday();
            if (Objects.equals(tp.getWeekday(), firstDay)) {
                out.add(formatRange(tp.getInitialHour(), tp.getFinalHour()));
            } else {
                break;
            }
        }
        return out;
    }

    private static String formatRange(LocalTime s, LocalTime e) {
        return String.format("%02d:%02d-%02d:%02d", s.getHour(), s.getMinute(), e.getHour(), e.getMinute());
    }

    // ========================= Find helpers =========================

    private Teacher findTeacherById(String teacherId) {
        if (teacherId == null) return null;
        return teachers.stream().filter(t -> teacherId.equals(t.getId())).findFirst().orElse(null);
    }
    private StudentGroup findStudentGroupById(String groupId) {
        if (groupId == null) return null;
        return studentGroups.stream().filter(g -> groupId.equals(g.getId())).findFirst().orElse(null);
    }
    private Subject findSubjectById(String subjectId) {
        if (subjectId == null) return null;
        return subjects.stream().filter(s -> subjectId.equals(s.getId())).findFirst().orElse(null);
    }
    private TimePeriod findTimePeriodById(String tpId) {
        if (tpId == null) return null;
        return timePeriods.stream().filter(tp -> tpId.equals(tp.getId())).findFirst().orElse(null);
    }
    private Classroom findClassroomById(String classroomId) {
        if (classroomId == null) return null;
        return classrooms.stream().filter(c -> classroomId.equals(c.getId())).findFirst().orElse(null);
    }

    // ========================= Add / Update / Delete entities =========================

    public void addTeacher(Teacher t) {
        if (t == null) return;
        teachers.add(t);
        try { persistenceController.add(t); } catch (Exception ignore) {}
    }

    public void updateTeacher(Teacher teacher) {
        if (teacher == null) return;
        Teacher current = findTeacherById(teacher.getId());
        if (current == null) return;

        // merge scalar fields only
        current.setName(teacher.getName());
        current.setAbbreviation(teacher.getAbbreviation());
        current.setEmail(teacher.getEmail());
        current.setPhone(teacher.getPhone());
        current.setDepartment(teacher.getDepartment());
        current.setHoursWork(teacher.getHoursWork());



        try { persistenceController.update(current); } catch (Exception ignore) {}
    }


    public void deleteTeacher(String id) {
        if (id == null) return;
        Teacher t = findTeacherById(id);
        if (t == null) return;
        teachers.remove(t);
        try { persistenceController.delete(t); } catch (Exception ignore) {}
    }


    public void addNewClassroom(Classroom classroom) {
        if (classroom == null) return;
        classrooms.add(classroom);
        try { persistenceController.add(classroom); } catch (Exception ignore) {}
    }

    public void updateClassroom(Classroom classroom) {
        if (classroom == null) return;
        Classroom c = findClassroomById(classroom.getId());
        if (c == null) return;
        int idx = classrooms.indexOf(c);
        classrooms.set(idx, classroom);
        try { persistenceController.update(classroom); } catch (Exception ignore) {}
    }

    public void removeClassroom(String classroomId) {
        if (classroomId == null) return;
        Classroom c = findClassroomById(classroomId);
        if (c == null) return;
        classrooms.remove(c);
        try { persistenceController.delete(c); } catch (Exception ignore) {}
    }


    public void addNewStudentGroup(StudentGroup g) {
        if (g == null) return;
        studentGroups.add(g);
        try { persistenceController.add(g); } catch (Exception ignore) {}
    }

    public void updateStudentGroup(StudentGroup incoming) {
        if (incoming == null) return;
        StudentGroup cur = findStudentGroupById(incoming.getId());
        if (cur == null) return;

        // merge SCALARS only
        cur.setName(incoming.getName());
        cur.setAbbreviation(incoming.getAbbreviation());
        cur.setCourse(incoming.getCourse());
        cur.setWeeklyGroupHours(incoming.getWeeklyGroupHours());
        cur.setNumberOfStudents(incoming.getNumberOfStudents());
        cur.setTutor(incoming.getTutor());

        try { persistenceController.update(cur); } catch (Exception e) { e.printStackTrace(); }
    }

    public List<Subject> getStudentGroupRequiredSubjects(String groupId) {
        try { return persistenceController.getRequiredSubjects(groupId); }
        catch (Exception e) { e.printStackTrace(); return List.of(); }
    }

    public void addStudentGroupRequiredSubject(String groupId, String subjectId) {
        try {
            persistenceController.addRequiredSubject(groupId, subjectId);
            StudentGroup g = findStudentGroupById(groupId);
            Subject s = findSubjectById(subjectId);
            if (g != null && s != null) {
                if (g.getRequiredSubjects() == null) g.setRequiredSubjects(new ArrayList<>());
                boolean exists = g.getRequiredSubjects().stream().anyMatch(x -> x.getId().equals(subjectId));
                if (!exists) g.getRequiredSubjects().add(s);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void removeStudentGroupRequiredSubject(String groupId, String subjectId) {
        try {
            persistenceController.removeRequiredSubject(groupId, subjectId);
            StudentGroup g = findStudentGroupById(groupId);
            if (g != null && g.getRequiredSubjects() != null) {
                g.getRequiredSubjects().removeIf(s -> subjectId.equals(s.getId()));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }



    public void removeStudentGroup(String groupId) {
        if (groupId == null) return;
        StudentGroup g = findStudentGroupById(groupId);
        if (g == null) return;
        studentGroups.remove(g);
        try { persistenceController.delete(g); } catch (Exception ignore) {}
    }


    public void addNewSubject(Subject s) {
        if (s == null) return;
        subjects.add(s);
        try { persistenceController.add(s); } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSubject(Subject s) {
        if (s == null) return;
        Subject cur = findSubjectById(s.getId());
        if (cur == null) return;
        int idx = subjects.indexOf(cur);
        subjects.set(idx, s);
        try { persistenceController.update(s); } catch (Exception ignore) {}
    }

    public void removeSubject(String subjectId) {
        if (subjectId == null) return;
        Subject s = findSubjectById(subjectId);
        if (s == null) return;
        subjects.remove(s);
        try { persistenceController.delete(s); } catch (Exception ignore) {}
    }


    public void addNewTimePeriod(TimePeriod tp) {
        if (tp == null) return;
        timePeriods.add(tp);
        try { persistenceController.add(tp); } catch (Exception ignore) {}
    }

    public void updateTimePeriod(TimePeriod tp) {
        if (tp == null) return;
        TimePeriod cur = findTimePeriodById(tp.getId());
        if (cur == null) return;
        int idx = timePeriods.indexOf(cur);
        timePeriods.set(idx, tp);
        try { persistenceController.update(tp); } catch (Exception ignore) {}
    }

    public void removeTimePeriod(String timePeriodId) {
        if (timePeriodId == null) return;
        TimePeriod tp = findTimePeriodById(timePeriodId);
        if (tp == null) return;
        timePeriods.remove(tp);
        try { persistenceController.delete(tp); } catch (Exception ignore) {}
    }


    // ========================= Schedules: get by entity =========================

    public Schedule getTeacherSchedule(String teacherId, String scheduleId) {
        if (teacherId == null || scheduleId == null) return null;
        Teacher t = findTeacherById(teacherId);
        if (t == null || t.getSchedules() == null) return null;
        return t.getSchedules().stream().filter(sc -> scheduleId.equals(sc.getId())).findFirst().orElse(null);
    }

    public Schedule getStudentGroupSchedule(String studentGroupId, String scheduleId) {
        if (studentGroupId == null || scheduleId == null) return null;
        StudentGroup g = findStudentGroupById(studentGroupId);
        if (g == null || g.getSchedules() == null) return null;
        return g.getSchedules().stream().filter(sc -> scheduleId.equals(sc.getId())).findFirst().orElse(null);
    }

    public Schedule getClassroomSchedule(String classroomId, String scheduleId) {
        if (classroomId == null || scheduleId == null) return null;
        Classroom c = findClassroomById(classroomId);
        if (c == null || c.getSchedules() == null) return null;
        return c.getSchedules().stream().filter(sc -> scheduleId.equals(sc.getId())).findFirst().orElse(null);
    }

    // ========================= Schedules: CRUD & regenerate =========================

    /** All known schedule IDs across Teachers, Groups, Classrooms (unique). */
    public List<String> getScheduleIds() {
        Set<String> ids = new LinkedHashSet<>();
        if (teachers != null) {
            for (Teacher t : teachers) {
                if (t.getSchedules() != null) {
                    for (Schedule s : t.getSchedules()) ids.add(s.getId());
                }
            }
        }
        if (studentGroups != null) {
            for (StudentGroup g : studentGroups) {
                if (g.getSchedules() != null) {
                    for (Schedule s : g.getSchedules()) ids.add(s.getId());
                }
            }
        }
        if (classrooms != null) {
            for (Classroom c : classrooms) {
                if (c.getSchedules() != null) {
                    for (Schedule s : c.getSchedules()) ids.add(s.getId());
                }
            }
        }
        return new ArrayList<>(ids);
    }

    /** Unique set of schedules (by ID) aggregated (teachers-first). */
    /**public List<Schedule> getAllSchedules() {
        Map<String, Schedule> map = new LinkedHashMap<>();
        if (teachers != null) {
            for (Teacher t : teachers) {
                if (t.getSchedules() != null) {
                    for (Schedule s : t.getSchedules()) map.putIfAbsent(s.getId(), s);
                }
            }
        }
        if (map.isEmpty() && studentGroups != null) {
            for (StudentGroup g : studentGroups) {
                if (g.getSchedules() != null) {
                    for (Schedule s : g.getSchedules()) map.putIfAbsent(s.getId(), s);
                }
            }
        }
        if (map.isEmpty() && classrooms != null) {
            for (Classroom c : classrooms) {
                if (c.getSchedules() != null) {
                    for (Schedule s : c.getSchedules()) map.putIfAbsent(s.getId(), s);
                }
            }
        }
        System.out.println("[BC] getAllSchedules size = " + map.size());
        List<Schedule> list = new ArrayList<>(map.values());
        for (Schedule s : list) {
            System.out.println("  Schedule id=" + s.getId() + " name=" + s.getName() +
                    " lessons=" + ((s.getLessons() == null) ? 0 : s.getLessons().size()));
        }
        return list;

    }
     **/

    public List<Schedule> getAllSchedules() {
        return schedules;
    }

    public void renameSchedule(String id, String name) {
        if (id == null) return;
        boolean changed = false;
        if (teachers != null) {
            for (Teacher t : teachers) {
                if (t.getSchedules() == null) continue;
                for (Schedule s : t.getSchedules()) {
                    if (id.equals(s.getId())) { s.setName(name); changed = true; }
                }
            }
        }
        if (studentGroups != null) {
            for (StudentGroup g : studentGroups) {
                if (g.getSchedules() == null) continue;
                for (Schedule s : g.getSchedules()) {
                    if (id.equals(s.getId())) { s.setName(name); changed = true; }
                }
            }
        }
        if (classrooms != null) {
            for (Classroom c : classrooms) {
                if (c.getSchedules() == null) continue;
                for (Schedule s : c.getSchedules()) {
                    if (id.equals(s.getId())) { s.setName(name); changed = true; }
                }
            }
        }
        if (changed) {
            try {
                if (teachers != null) for (Teacher t : teachers) persistenceController.update(t);
                if (studentGroups != null) for (StudentGroup g : studentGroups) persistenceController.update(g);
                if (classrooms != null) for (Classroom c : classrooms) persistenceController.update(c);
            } catch (Exception ignore) {}
        }
    }

    /** Delete a schedule ID from all owners (teachers, groups, rooms) and persist owners. */
    public void deleteSchedule(String id) {
        if (id == null) return;

        if (teachers != null) {
            for (Teacher t : teachers) {
                if (t.getSchedules() != null) {
                    t.getSchedules().removeIf(s -> id.equals(s.getId()));
                }
            }
        }
        if (studentGroups != null) {
            for (StudentGroup g : studentGroups) {
                if (g.getSchedules() != null) {
                    g.getSchedules().removeIf(s -> id.equals(s.getId()));
                }
            }
        }
        if (classrooms != null) {
            for (Classroom c : classrooms) {
                if (c.getSchedules() != null) {
                    c.getSchedules().removeIf(s -> id.equals(s.getId()));
                }
            }
        }

        // Persist owners
        try {
            if (teachers != null) for (Teacher t : teachers) persistenceController.update(t);
            if (studentGroups != null) for (StudentGroup g : studentGroups) persistenceController.update(g);
            if (classrooms != null) for (Classroom c : classrooms) persistenceController.update(c);
        } catch (Exception ignore) {}
    }

    /**
     * Regenerates schedules: deletes ALL existing schedules first (from all owners),
     * then generates new ones from current business data and persists owners.
     */
    public void regenerateSchedules() throws ContradictionException {
        List<String> allIds = new ArrayList<>(getScheduleIds());
        for (String sid : allIds) deleteSchedule(sid);
        generateSchedules(false);
    }

    /**
     * Generates schedules using ScheduleSolver.
     * If test==true, it prints/calculates only.
     * If test==false, it attaches schedules to owners and persists them.
     */
    public void generateSchedules(boolean test) throws ContradictionException {
        ScheduleSolver solver = new ScheduleSolver(teachers, classrooms, studentGroups, timePeriods);
        List<Schedule> generated = solver.createSchedule();
        if (generated == null) generated = List.of();
        schedules = generated; // cache last generated

        if (test) {
            for (Schedule s : generated) {
                if (s != null) {
                    s.printSchedule();
                    s.calculateConditions(teachers);
                }
            }
            return;
        }

        // For each global schedule, build per-entity projections and attach them.
        for (Schedule global : generated) {
            if (global == null || global.getLessons() == null) continue;

            // Ensure the global schedule has an id/name (solver might set them; if not, set here)
            if (global.getId() == null)   global.setId(java.util.UUID.randomUUID().toString());
            if (global.getName() == null) global.setName("Solución " + global.getId().substring(0, 8));

            // Prepare empty schedules per entity (they will share the same id & name)
            Map<Teacher, Schedule> tMap = new HashMap<>();
            for (Teacher t : teachers) {
                Schedule s = new Schedule();
                s.setId(global.getId());
                s.setName(global.getName());
                tMap.put(t, s);
            }
            Map<Classroom, Schedule> cMap = new HashMap<>();
            for (Classroom c : classrooms) {
                Schedule s = new Schedule();
                s.setId(global.getId());
                s.setName(global.getName());
                cMap.put(c, s);
            }
            Map<StudentGroup, Schedule> gMap = new HashMap<>();
            for (StudentGroup g : studentGroups) {
                Schedule s = new Schedule();
                s.setId(global.getId());
                s.setName(global.getName());
                gMap.put(g, s);
            }

            // Distribute lessons into the right per-entity schedules
            for (Lesson l : global.getLessons()) {
                if (l.getTeacher() != null) {
                    Schedule ts = tMap.get(l.getTeacher());
                    if (ts != null) ts.addLesson(l);
                }
                if (l.getClassroom() != null) {
                    Schedule cs = cMap.get(l.getClassroom());
                    if (cs != null) cs.addLesson(l);
                }
                if (l.getStudentGroup() != null) {
                    Schedule gs = gMap.get(l.getStudentGroup());
                    if (gs != null) gs.addLesson(l);
                }
            }

            // Attach per-entity schedules to owners (skip empty ones)
            for (var e : tMap.entrySet()) {
                Teacher t = e.getKey();
                Schedule s = e.getValue();
                if (s.getLessons() != null && !s.getLessons().isEmpty()) {
                    if (t.getSchedules() == null) t.setSchedules(new ArrayList<>());
                    boolean exists = t.getSchedules().stream().anyMatch(sc -> global.getId().equals(sc.getId()));
                    if (!exists) t.getSchedules().add(s);
                }
            }
            for (var e : cMap.entrySet()) {
                Classroom c = e.getKey();
                Schedule s = e.getValue();
                if (s.getLessons() != null && !s.getLessons().isEmpty()) {
                    if (c.getSchedules() == null) c.setSchedules(new ArrayList<>());
                    boolean exists = c.getSchedules().stream().anyMatch(sc -> global.getId().equals(sc.getId()));
                    if (!exists) c.getSchedules().add(s);
                }
            }
            for (var e : gMap.entrySet()) {
                StudentGroup g = e.getKey();
                Schedule s = e.getValue();
                if (s.getLessons() != null && !s.getLessons().isEmpty()) {
                    if (g.getSchedules() == null) g.setSchedules(new ArrayList<>());
                    boolean exists = g.getSchedules().stream().anyMatch(sc -> global.getId().equals(sc.getId()));
                    if (!exists) g.getSchedules().add(s);
                }
            }
        }

        // Persist owners after attaching
        try {
            if (teachers != null)      for (Teacher t : teachers)      persistenceController.update(t);
            if (studentGroups != null) for (StudentGroup g : studentGroups) persistenceController.update(g);
            if (classrooms != null)    for (Classroom c : classrooms)    persistenceController.update(c);
        } catch (Exception ignore) {}

    }


    // ========================= Teacher preferences / possible subjects =========================
    // (Delegated to Teacher to avoid immutable list issues)

    // --- Possible Subjects ---
    public List<Subject> getTeacherPossibleSubjects(String teacherId) {
        Teacher t = findTeacherById(teacherId);
        return (t == null) ? new ArrayList<>() : t.getPossibleSubjects();
    }
    public void addTeacherPossibleSubject(String teacherId, String subjectId) {
        Teacher t = findTeacherById(teacherId);
        Subject s = findSubjectById(subjectId);
        if (t == null || s == null) return;
        try {
            persistenceController.insertTeacherPossibleSubject(teacherId, subjectId); // persist relation, idempotent
            t.addPossibleSubject(s);                                                  // keep cache in sync
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeTeacherPossibleSubject(String teacherId, String subjectId) {
        Teacher t = findTeacherById(teacherId);
        if (t == null) return;
        try {
            persistenceController.deleteTeacherPossibleSubject(teacherId, subjectId); // persist relation
            t.removePossibleSubjectById(subjectId);                                   // keep cache in sync
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // --- Preferred Subjects ---
    public List<Subject> getTeacherPreferredSubjects(String teacherId) {
        Teacher t = findTeacherById(teacherId);
        return (t == null) ? new ArrayList<>() : t.getPreferredSubjects();
    }
    public Map<String,Integer> getTeacherPreferredSubjectWeights(String teacherId) {
        Teacher t = findTeacherById(teacherId);
        return (t == null) ? Map.of() : t.getPreferredSubjectWeights();
    }
    public void addTeacherPreferredSubject(String teacherId, String subjectId, int weight) {
        Teacher t = findTeacherById(teacherId);
        Subject s = findSubjectById(subjectId);
        if (t == null || s == null) return;
        t.addPreferredSubject(s, weight);
        try { persistenceController.update(t); } catch (Exception ignore) {}
    }
    public void removeTeacherPreferredSubject(String teacherId, String subjectId) {
        Teacher t = findTeacherById(teacherId);
        if (t == null) return;
        t.removePreferredSubjectById(subjectId);
        try { persistenceController.update(t); } catch (Exception ignore) {}
    }
    public void updateTeacherPreferredSubjectWeight(String teacherId, String subjectId, int w) {
        Teacher t = findTeacherById(teacherId);
        if (t == null) return;
        t.updatePreferredSubjectWeight(subjectId, w);
        try { persistenceController.update(t); } catch (Exception ignore) {}
    }

    // --- Preferred / Unpreferred Groups ---
    public List<StudentGroup> getTeacherPreferredStudentGroups(String teacherId) {
        Teacher t = findTeacherById(teacherId);
        return (t == null) ? new ArrayList<>() : t.getPreferredStudentGroups();
    }
    public Map<String,Integer> getTeacherPreferredStudentGroupWeights(String teacherId) {
        Teacher t = findTeacherById(teacherId);
        return (t == null) ? Map.of() : t.getPreferredStudentGroupWeights();
    }
    public void addTeacherPreferredStudentGroup(String teacherId, String groupId, int weight) {
        Teacher t = findTeacherById(teacherId);
        StudentGroup g = findStudentGroupById(groupId);
        if (t == null || g == null) return;
        t.addPreferredStudentGroup(g, weight);
        try { persistenceController.update(t); } catch (Exception ignore) {}
    }
    public void removeTeacherPreferredStudentGroup(String teacherId, String groupId) {
        Teacher t = findTeacherById(teacherId);
        if (t == null) return;
        t.removePreferredStudentGroupById(groupId);
        try { persistenceController.update(t); } catch (Exception ignore) {}
    }

    public List<StudentGroup> getTeacherUnpreferredStudentGroups(String teacherId) {
        Teacher t = findTeacherById(teacherId);
        return (t == null) ? new ArrayList<>() : t.getUnPreferredStudentGroups();
    }
    public Map<String,Integer> getTeacherUnpreferredStudentGroupWeights(String teacherId) {
        Teacher t = findTeacherById(teacherId);
        return (t == null) ? Map.of() : t.getUnPreferredStudentGroupWeights();
    }
    public void addTeacherUnpreferredStudentGroup(String teacherId, String groupId, int weight) {
        Teacher t = findTeacherById(teacherId);
        StudentGroup g = findStudentGroupById(groupId);
        if (t == null || g == null) return;
        t.addUnPreferredStudentGroup(g, weight);
        try { persistenceController.update(t); } catch (Exception ignore) {}
    }
    public void removeTeacherUnpreferredStudentGroup(String teacherId, String groupId) {
        Teacher t = findTeacherById(teacherId);
        if (t == null) return;
        t.removeUnPreferredStudentGroupById(groupId);
        try { persistenceController.update(t); } catch (Exception ignore) {}
    }
    public void updateTeacherPreferredStudentGroupWeight(String teacherId, String groupId, int w) {
        Teacher t = findTeacherById(teacherId);
        if (t == null) return;
        t.updatePreferredStudentGroupWeight(groupId, w);
        try { persistenceController.update(t); } catch (Exception ignore) {}
    }
    public void updateTeacherUnpreferredStudentGroupWeight(String teacherId, String groupId, int w) {
        Teacher t = findTeacherById(teacherId);
        if (t == null) return;
        t.updateUnPreferredStudentGroupWeight(groupId, w);
        try { persistenceController.update(t); } catch (Exception ignore) {}
    }

    // --- Preferred / Unpreferred / Unavailable TimePeriods ---
    public List<TimePeriod> getTeacherPreferredTimePeriods(String teacherId) {
        Teacher t = findTeacherById(teacherId);
        return (t == null) ? new ArrayList<>() : t.getPreferredTimePeriods();
    }
    public Map<String,Integer> getTeacherPreferredTimePeriodWeights(String teacherId) {
        Teacher t = findTeacherById(teacherId);
        return (t == null) ? Map.of() : t.getPreferredTimePeriodWeights();
    }
    public void addTeacherPreferredTimePeriod(String teacherId, String tpId, int weight) {
        Teacher t = findTeacherById(teacherId);
        TimePeriod tp = findTimePeriodById(tpId);
        if (t == null || tp == null) return;
        t.addPreferredTimePeriod(tp, weight);
        try { persistenceController.update(t); } catch (Exception ignore) {}
    }
    public void removeTeacherPreferredTimePeriod(String teacherId, String tpId) {
        Teacher t = findTeacherById(teacherId);
        if (t == null) return;
        t.removePreferredTimePeriodById(tpId);
        try { persistenceController.update(t); } catch (Exception ignore) {}
    }
    public void updateTeacherPreferredTimePeriodWeight(String teacherId, String tpId, int w) {
        Teacher t = findTeacherById(teacherId);
        if (t == null) return;
        t.updatePreferredTimePeriodWeight(tpId, w);
        try { persistenceController.update(t); } catch (Exception ignore) {}
    }

    public List<TimePeriod> getTeacherUnpreferredTimePeriods(String teacherId) {
        Teacher t = findTeacherById(teacherId);
        return (t == null) ? new ArrayList<>() : t.getUnPreferredTimePeriods();
    }
    public Map<String,Integer> getTeacherUnpreferredTimePeriodWeights(String teacherId) {
        Teacher t = findTeacherById(teacherId);
        return (t == null) ? Map.of() : t.getUnPreferredTimePeriodWeights();
    }
    public void addTeacherUnpreferredTimePeriod(String teacherId, String tpId, int weight) {
        Teacher t = findTeacherById(teacherId);
        TimePeriod tp = findTimePeriodById(tpId);
        if (t == null || tp == null) return;
        t.addUnPreferredTimePeriod(tp, weight);
        try { persistenceController.update(t); } catch (Exception ignore) {}
    }
    public void removeTeacherUnpreferredTimePeriod(String teacherId, String tpId) {
        Teacher t = findTeacherById(teacherId);
        if (t == null) return;
        t.removeUnPreferredTimePeriodById(tpId);
        try { persistenceController.update(t); } catch (Exception ignore) {}
    }
    public void updateTeacherUnpreferredTimePeriodWeight(String teacherId, String tpId, int w) {
        Teacher t = findTeacherById(teacherId);
        if (t == null) return;
        t.updateUnPreferredTimePeriodWeight(tpId, w);
        try { persistenceController.update(t); } catch (Exception ignore) {}
    }

    public List<TimePeriod> getTeacherUnavailableTimePeriods(String teacherId) {
        Teacher t = findTeacherById(teacherId);
        return (t == null) ? new ArrayList<>() : t.getUnavailableTimePeriods();
    }
    public void addTeacherUnavailableTimePeriod(String teacherId, String tpId) {
        Teacher t = findTeacherById(teacherId);
        TimePeriod tp = findTimePeriodById(tpId);
        if (t == null || tp == null) return;
        t.addUnavailableTimePeriod(tp);
        try { persistenceController.update(t); } catch (Exception ignore) {}
    }
    public void removeTeacherUnavailableTimePeriod(String teacherId, String tpId) {
        Teacher t = findTeacherById(teacherId);
        if (t == null) return;
        t.removeUnavailableTimePeriodById(tpId);
        try { persistenceController.update(t); } catch (Exception ignore) {}
    }

    // ========================= Misc helpers =========================

    public void createAllTimePeriods(String weekDay, LocalTime dayStart, LocalTime dayEnd, LocalTime period) {
        long totalMinutes = Duration.between(dayStart, dayEnd).toMinutes();
        long periodMinutes = period.getHour() * 60L + period.getMinute();
        int count = (int) (totalMinutes / periodMinutes);

        for (int i = 0; i < 5; i++) { // 5 weekdays
            LocalTime start = dayStart;
            for (int j = 0; j < count; j++) {
                LocalTime end = start.plusMinutes(periodMinutes);
                TimePeriod tp = new TimePeriod(UUID.randomUUID().toString(), weekDay, start, end);
                addNewTimePeriod(tp);
                start = end;
            }
        }
    }


    // ===================== DEMO / TEST SEEDING =====================

    /**
     * Seed a small, consistent demo dataset and (optionally) generate schedules.
     * Inserts in FK-safe order and initializes list fields to avoid DAO NPEs.
     *
     * NOTE: This version intentionally does NOT add teacher preference "conditions"
     * during seeding because your `conditions` table requires subject+timePeriod+studentGroup
     * to be NOT NULL. Add preferences later (after adjusting schema/DAO) via updates.
     */
    public void seedDemoData(boolean clearExisting, boolean generateSchedulesToo) throws Exception {
        if (clearExisting) {
            deleteAllEntities(); // uses persistenceController.delete(T) internally
        }

        if (subjects == null) subjects = new ArrayList<>();
        if (teachers == null) teachers = new ArrayList<>();
        if (studentGroups == null) studentGroups = new ArrayList<>();
        if (classrooms == null) classrooms = new ArrayList<>();
        if (timePeriods == null) timePeriods = new ArrayList<>();

        // ===== 1) SUBJECTS (must exist before groups/conditions) =====
        Subject math      = new Subject(newId(), "Matemáticas", "MATH");  math.setWeeklyAssignedHours(3);
        Subject physics   = new Subject(newId(), "Física",       "PHYS"); physics.setWeeklyAssignedHours(2);
        Subject history   = new Subject(newId(), "Historia",     "HIST"); history.setWeeklyAssignedHours(2);
        Subject chemistry = new Subject(newId(), "Química",      "CHEM"); chemistry.setWeeklyAssignedHours(3); chemistry.setMaxDailyHours(2);

        persistenceController.add(math);      subjects.add(math);
        persistenceController.add(physics);   subjects.add(physics);
        persistenceController.add(history);   subjects.add(history);
        persistenceController.add(chemistry); subjects.add(chemistry);

        // ===== 2) TIME PERIODS (used later by schedules/conditions) =====
        TimePeriod mon9  = new TimePeriod(newId(), "Lunes",  java.time.LocalTime.of(9,0),  java.time.LocalTime.of(10,0));
        TimePeriod mon10 = new TimePeriod(newId(), "Lunes",  java.time.LocalTime.of(10,0), java.time.LocalTime.of(11,0));
        TimePeriod mon11 = new TimePeriod(newId(), "Lunes",  java.time.LocalTime.of(11,0), java.time.LocalTime.of(12,0));
        TimePeriod tue9  = new TimePeriod(newId(), "Martes", java.time.LocalTime.of(9,0),  java.time.LocalTime.of(10,0));
        TimePeriod tue10 = new TimePeriod(newId(), "Martes", java.time.LocalTime.of(10,0), java.time.LocalTime.of(11,0));
        TimePeriod tue11 = new TimePeriod(newId(), "Martes", java.time.LocalTime.of(11,0), java.time.LocalTime.of(12,0));
        TimePeriod wed9  = new TimePeriod(newId(), "Miércoles", LocalTime.of(9,0),  LocalTime.of(10,0));
        TimePeriod wed10 = new TimePeriod(newId(), "Miércoles", LocalTime.of(10,0), LocalTime.of(11,0));
        TimePeriod wed11 = new TimePeriod(newId(), "Miércoles", LocalTime.of(11,0), LocalTime.of(12,0));

        for (TimePeriod tp : new TimePeriod[]{mon9, mon10, mon11, tue9, tue10, tue11, wed9, wed10, wed11}) {
            persistenceController.add(tp);
            timePeriods.add(tp);
        }

        // ===== 3) STUDENT GROUPS (requires subjects to exist; DAO writes SG + requiredSubjects link table) =====
        StudentGroup g1 = new StudentGroup(newId(), "1º ESO", "G1");
        g1.setRequiredSubjects(new ArrayList<>(Arrays.asList(math, history, physics)));
        // ensure schedules list exists to avoid DAO NPEs when it iterates
        if (g1.getSchedules() == null) g1.setSchedules(new ArrayList<>());
        persistenceController.add(g1);
        studentGroups.add(g1);

        StudentGroup g2 = new StudentGroup(newId(), "2º ESO", "G2");
        g2.setRequiredSubjects(new ArrayList<>(Arrays.asList(chemistry, math)));
        if (g2.getSchedules() == null) g2.setSchedules(new ArrayList<>());
        persistenceController.add(g2);
        studentGroups.add(g2);

        // ===== 4) CLASSROOMS (init schedules list to avoid DAO NPEs) =====
        Classroom room101 = new Classroom(newId(), "Aula 101", "1"); room101.setCapacity(30);
        Classroom room102 = new Classroom(newId(), "Aula 102", "2"); room102.setCapacity(28);
        Classroom room103 = new Classroom(newId(), "Aula 103", "3"); room103.setCapacity(25);
        if (room101.getSchedules() == null) room101.setSchedules(new ArrayList<>());
        if (room102.getSchedules() == null) room102.setSchedules(new ArrayList<>());
        if (room103.getSchedules() == null) room103.setSchedules(new ArrayList<>());
        persistenceController.add(room101); classrooms.add(room101);
        persistenceController.add(room102); classrooms.add(room102);
        persistenceController.add(room103); classrooms.add(room103);

        // ===== 5) TEACHERS (persist without preferences/conditions to avoid FK issues for now) =====
        Teacher tAlice = new Teacher(newId(), "Alice", "AL");
        tAlice.setPossibleSubjects(new ArrayList<>(Arrays.asList(math, physics, chemistry, history)));
        tAlice.setHoursWork(5);
        if (tAlice.getSchedules() == null) tAlice.setSchedules(new ArrayList<>());
        tAlice.addUnPreferredStudentGroup(g1, 2);
        tAlice.addPreferredSubject(math, 5);
        persistenceController.add(tAlice);
        teachers.add(tAlice);

        Teacher tBob = new Teacher(newId(), "Bob", "BOB");
        tBob.setPossibleSubjects(new ArrayList<>(Arrays.asList(math, physics, history, chemistry)));
        tBob.setHoursWork(5);
        if (tBob.getSchedules() == null) tBob.setSchedules(new ArrayList<>());
        persistenceController.add(tBob);
        teachers.add(tBob);

        Teacher tCarol = new Teacher(newId(), "Carol", "CAR");
        tCarol.setPossibleSubjects(new ArrayList<>(Arrays.asList(math, physics, history, chemistry)));
        tCarol.setHoursWork(5);
        if (tCarol.getSchedules() == null) tCarol.setSchedules(new ArrayList<>());
        persistenceController.add(tCarol);
        teachers.add(tCarol);

        System.out.printf(
                "Seeded: %d teachers, %d groups, %d classrooms, %d subjects, %d time periods%n",
                teachers.size(), studentGroups.size(), classrooms.size(), subjects.size(), timePeriods.size()
        );

        // ===== 6) Optionally generate schedules and persist owners =====
        if (generateSchedulesToo) {
            generateSchedules(false); // your existing generator; attaches schedules to owners

            // Persist owners again so their schedules get written (as your DAOs support)
            for (Teacher t : teachers) {
                try { persistenceController.update(t); } catch (Exception ignored) {}
            }
            for (StudentGroup sg : studentGroups) {
                try { persistenceController.update(sg); } catch (Exception ignored) {}
            }
            for (Classroom c : classrooms) {
                try { persistenceController.update(c); } catch (Exception ignored) {}
            }
        }
    }


    /** Danger: deletes ALL existing entities currently loaded, then clears in-memory lists. */
    private void deleteAllEntities() throws Exception {
        if (teachers != null)      for (Teacher t : new java.util.ArrayList<>(teachers))      persistenceController.delete(t);
        if (studentGroups != null) for (StudentGroup g : new java.util.ArrayList<>(studentGroups)) persistenceController.delete(g);
        if (classrooms != null)    for (Classroom c : new java.util.ArrayList<>(classrooms))    persistenceController.delete(c);
        if (subjects != null)      for (Subject s : new java.util.ArrayList<>(subjects))      persistenceController.delete(s);
        if (timePeriods != null)   for (TimePeriod tp : new java.util.ArrayList<>(timePeriods))   persistenceController.delete(tp);

        teachers = new java.util.ArrayList<>();
        studentGroups = new java.util.ArrayList<>();
        classrooms = new java.util.ArrayList<>();
        subjects = new java.util.ArrayList<>();
        timePeriods = new java.util.ArrayList<>();
    }

    private static String newId() {
        return java.util.UUID.randomUUID().toString();
    }

    public Subject getSubjectById(String id) {
        return findSubjectById(id);
    }

    // ---- helper DTOs ----
    public static final class ConditionHit {
        public final Condition condition;
        public final Lesson lesson;     // the lesson that satisfied/violated it
        public ConditionHit(Condition c, Lesson l) { this.condition = c; this.lesson = l; }
    }

    public static final class ConditionSummary {
        public final List<ConditionHit> preferredAchieved = new ArrayList<>();
        public final List<ConditionHit> unpreferredViolated = new ArrayList<>();
        public int preferredScore = 0;     // sum of weights of achieved preferred
        public int unpreferredPenalty = 0; // sum of weights (or penalties) of violated unpreferred
    }

    // ---- public API: per-teacher summary for a given schedule id ----
    public Map<Teacher, ConditionSummary> summarizeConditionsByTeacher(String scheduleId) {
        Schedule schedule = findScheduleById(scheduleId);
        Map<Teacher, ConditionSummary> out = new LinkedHashMap<>();
        if (schedule == null || schedule.getLessons() == null) return out;

        // Make sure all teachers present in memory have an entry
        for (Teacher t : teachers) out.put(t, new ConditionSummary());

        for (Lesson l : schedule.getLessons()) {
            Teacher t = l.getTeacher();
            if (t == null) continue;
            ConditionSummary sum = out.computeIfAbsent(t, k -> new ConditionSummary());

            // Preferred achieved?
            if (t.getPreferredConditions() != null) {
                for (Condition c : t.getPreferredConditions()) {
                    if (matches(c, l)) {
                        sum.preferredAchieved.add(new ConditionHit(c, l));
                        sum.preferredScore += c.getWeight();
                    }
                }
            }
            // Unpreferred violated?
            if (t.getUnPreferredConditions() != null) {
                for (Condition c : t.getUnPreferredConditions()) {
                    if (matches(c, l)) {
                        sum.unpreferredViolated.add(new ConditionHit(c, l));
                        sum.unpreferredPenalty += c.getWeight();
                    }
                }
            }
        }
        return out;
    }

    // ---- quick pass-through for UI counts only (optional) ----
    public Map<Teacher, int[]> summarizeConditionCounts(String scheduleId) {
        Map<Teacher, ConditionSummary> m = summarizeConditionsByTeacher(scheduleId);
        Map<Teacher, int[]> out = new LinkedHashMap<>();
        for (var e : m.entrySet()) {
            ConditionSummary cs = e.getValue();
            out.put(e.getKey(), new int[]{ cs.preferredAchieved.size(), cs.unpreferredViolated.size(), cs.preferredScore, cs.unpreferredPenalty });
        }
        return out;
    }

    // ---- helpers ----
/*
    private Schedule findScheduleById(String id) {
        if (id == null) return null;
        for (Teacher t : teachers) {
            if (t.getSchedules() != null) {
                for (Schedule s : t.getSchedules()) if (id.equals(s.getId())) return s;
            }
        }
        for (StudentGroup g : studentGroups) {
            if (g.getSchedules() != null) {
                for (Schedule s : g.getSchedules()) if (id.equals(s.getId())) return s;
            }
        }
        for (Classroom c : classrooms) {
            if (c.getSchedules() != null) {
                for (Schedule s : c.getSchedules()) if (id.equals(s.getId())) return s;
            }
        }
        return null;
    }
*/
    /*
    private boolean matches(Condition c, Lesson l) {
        if (c == null || l == null) return false;
        String type = c.getConditionType();
        if ("Subject".equals(type)) {
            return c.getSubject() != null && l.getSubject() != null &&
                    Objects.equals(c.getSubject().getId(), l.getSubject().getId());
        }
        if ("TimePeriod".equals(type)) {
            return c.getTimePeriod() != null && l.getTimePeriod() != null &&
                    Objects.equals(c.getTimePeriod().getId(), l.getTimePeriod().getId());
        }
        if ("StudentGroup".equals(type)) {
            return c.getStudentGroup() != null && l.getStudentGroup() != null &&
                    Objects.equals(c.getStudentGroup().getId(), l.getStudentGroup().getId());
        }
        return false; // "None" or unknown type
    }
*/
    // ===== Detailed condition reporting =====
    public static final class ConditionCheck {
        public final Condition condition;
        public final boolean matched;            // true = achieved (preferred) / violated (unpreferred)
        public final List<Lesson> witnesses;     // lessons that caused the match

        public ConditionCheck(Condition c, boolean matched, List<Lesson> witnesses) {
            this.condition = c;
            this.matched = matched;
            this.witnesses = witnesses;
        }
    }

    public static final class TeacherConditionReport {
        public final Teacher teacher;
        public final List<ConditionCheck> preferred = new ArrayList<>();   // achieved + unmet
        public final List<ConditionCheck> unpreferred = new ArrayList<>(); // violated + respected

        public TeacherConditionReport(Teacher t) { this.teacher = t; }
    }

    /** Detailed report for ONE teacher on ONE schedule. */
    public TeacherConditionReport getConditionReportForTeacher(String scheduleId, String teacherId) {
        Schedule schedule = findScheduleById(scheduleId);
        Teacher teacher = findTeacherById(teacherId);
        TeacherConditionReport report = new TeacherConditionReport(teacher);

        if (schedule == null || teacher == null || schedule.getLessons() == null) return report;

        // Collect this teacher's lessons in the schedule
        List<Lesson> myLessons = new ArrayList<>();
        for (Lesson l : schedule.getLessons()) {
            if (l.getTeacher() != null && teacherId.equals(l.getTeacher().getId())) {
                myLessons.add(l);
            }
        }

        // Preferred: achieved (matched) vs unmet (no witness)
        if (teacher.getPreferredConditions() != null) {
            for (Condition c : teacher.getPreferredConditions()) {
                List<Lesson> witnesses = new ArrayList<>();
                for (Lesson l : myLessons) if (matches(c, l)) witnesses.add(l);
                report.preferred.add(new ConditionCheck(c, !witnesses.isEmpty(), witnesses));
            }
        }

        // Unpreferred: violated (matched) vs respected (no witness)
        if (teacher.getUnPreferredConditions() != null) {
            for (Condition c : teacher.getUnPreferredConditions()) {
                List<Lesson> witnesses = new ArrayList<>();
                for (Lesson l : myLessons) if (matches(c, l)) witnesses.add(l);
                report.unpreferred.add(new ConditionCheck(c, !witnesses.isEmpty(), witnesses));
            }
        }

        return report;
    }

    // ---- helpers re-used ----
    private boolean matches(Condition c, Lesson l) {
        if (c == null || l == null) return false;
        String type = c.getConditionType();
        if ("Subject".equals(type)) {
            return c.getSubject() != null && l.getSubject() != null &&
                    Objects.equals(c.getSubject().getId(), l.getSubject().getId());
        }
        if ("TimePeriod".equals(type)) {
            return c.getTimePeriod() != null && l.getTimePeriod() != null &&
                    Objects.equals(c.getTimePeriod().getId(), l.getTimePeriod().getId());
        }
        if ("StudentGroup".equals(type)) {
            return c.getStudentGroup() != null && l.getStudentGroup() != null &&
                    Objects.equals(c.getStudentGroup().getId(), l.getStudentGroup().getId());
        }
        return false; // "None" or unknown
    }

    // existing helper from before (or keep this here)
    private Schedule findScheduleById(String id) {
        if (id == null) return null;
        if (teachers != null) for (Teacher t : teachers)
            if (t.getSchedules()!=null) for (Schedule s : t.getSchedules()) if (id.equals(s.getId())) return s;
        if (studentGroups != null) for (StudentGroup g : studentGroups)
            if (g.getSchedules()!=null) for (Schedule s : g.getSchedules()) if (id.equals(s.getId())) return s;
        if (classrooms != null) for (Classroom c : classrooms)
            if (c.getSchedules()!=null) for (Schedule s : c.getSchedules()) if (id.equals(s.getId())) return s;
        return null;
    }


}
