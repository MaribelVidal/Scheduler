package business;

import persistence.PersistenceController;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BusinessController {
    //private ScheduleSolver solver;

    private List<Teacher> teachers;
    private List<StudentGroup> studentGroups;
    private List<Subject> subjects;
    private List<Classroom> classrooms;
    private List<TimePeriod> timePeriods;





    private PersistenceController persistenceController;

    public BusinessController() throws Exception {

        this.persistenceController = new PersistenceController();
        persistenceController.initialize();
        getFromDataBase();

    }

    public List<String> getTeachersNames() {
        return teachers.stream()
                .map(Teacher::getName)
                .toList();
    }
    public List<String> getClassroomsNames() {
        return classrooms.stream()
                .map(Classroom::getName)
                .toList();
    }

    public List<String> getStudentGroupsNames() {
        return studentGroups.stream()
                .map(StudentGroup::getName)
                .toList();
    }

    public List<String> getTPNames() {
        List<String> tpNames = new ArrayList<>();
        for (TimePeriod tp : timePeriods) {
            if (tp.getWeekday() == "Monday") {
                tpNames.add( tp.getInitialHour() + "-" + tp.getFinalHour());
            }
            else return tpNames;

        }
        return tpNames;

    }

    public List<String> getSubjectsNames() {
        return subjects.stream()
                .map(Subject::getName)
                .toList();
    }

    public List<Teacher> getTeachers() {
        return teachers;
    }

    public List<Classroom> getClassrooms() {
        return classrooms;
    }

    public List<StudentGroup> getStudentGroups() {
        return studentGroups;
    }

    public Schedule getTeacherSchedule(String teacherId, String scheduleId) {
        if (teacherId == null || scheduleId == null) return null;
        var teacherOpt = teachers.stream().filter(t -> teacherId.equals(t.getId())).findFirst();
        if (teacherOpt.isEmpty()) return null; // <-- instead of orElseThrow
        var t = teacherOpt.get();
        var s = t.getSchedules() == null ? null :
                t.getSchedules().stream().filter(sc -> scheduleId.equals(sc.getId())).findFirst().orElse(null);
        return s;
    }



    public Schedule getStudentGroupSchedule(String studentGroupId, String scheduleId) {
        if (studentGroupId == null || scheduleId == null) return null;
        var studentGroupOpt = studentGroups.stream().filter(sg -> studentGroupId.equals(sg.getId())).findFirst();
        if (studentGroupOpt.isEmpty()) return null; // <-- instead of orElseThrow
        var sg = studentGroupOpt.get();
        var s = sg.getSchedules() == null ? null :
                sg.getSchedules().stream().filter(sc -> scheduleId.equals(sc.getId())).findFirst().orElse(null);
        return s;
    }

    public Schedule getClassroomSchedule (String classroomId, String scheduleId) {
        if (classroomId == null || scheduleId == null) return null;
        var classroomOpt = classrooms.stream().filter(c -> classroomId.equals(c.getId())).findFirst();
        if (classroomOpt.isEmpty()) return null; // <-- instead of orElseThrow
        var c = classroomOpt.get();
        var s = c.getSchedules() == null ? null :
                c.getSchedules().stream().filter(sc -> scheduleId.equals(sc.getId())).findFirst().orElse(null);
        return s;
    }


    private void getFromDataBase () throws Exception {
        teachers = persistenceController.getAll(Teacher.class);


        studentGroups = persistenceController.getAll(StudentGroup.class);


        subjects = persistenceController.getAll(Subject.class);

        classrooms = persistenceController.getAll(Classroom.class);



        timePeriods = persistenceController.getAll(TimePeriod.class);






    }

    public void debug() {
        //Teacher teacher = new Teacher(5);
        //solver.ScheduleShower(teacher.getId(), "teacher");
    }

    public void test () {
        //solver.printMatrix();
    }



    public void createAllTimePeriods (String weekDay, LocalTime initialHourDay, LocalTime finalHourDay, LocalTime period){
        long totalMinutes = Duration.between(initialHourDay, finalHourDay).toMinutes();
        long periodMinutes = period.getHour()*60 + period.getMinute();
        int timePeriodNumber = (int) (totalMinutes / periodMinutes);

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < timePeriodNumber; j++) {
                TimePeriod timeperiod = new TimePeriod(UUID.randomUUID().toString(), weekDay, initialHourDay, period);
                timePeriods.add(timeperiod);


            }

        }



    }

    public void addNewTeacher(Teacher teacher) {
        if (teachers == null) {
            teachers = new ArrayList<>();
        }
        teachers.add(teacher);
        // Optionally, save to database
        // persistenceController.add(teacher);
    }

    public void removeTeacher(String teacherId) {
        if (teachers == null) {
            return; // No teachers to remove
        }
        teachers.removeIf(teacher -> teacher.getId().equals(teacherId));
        // Optionally, remove from database
        // persistenceController.remove(Teacher.class, teacherId);
    }

    public void addNewClassroom(Classroom classroom) {
        if (classrooms == null) {
            classrooms = new ArrayList<>();
        }
        classrooms.add(classroom);
        // Optionally, save to database
        // persistenceController.add(classroom);
    }
    public void removeClassroom(String classroomId) {
        if (classrooms == null) {
            return; // No classrooms to remove
        }
        classrooms.removeIf(classroom -> classroom.getId().equals(classroomId));
        // Optionally, remove from database
        // persistenceController.remove(Classroom.class, classroomId);
    }

    public void updateClassroom(Classroom classroom) {
        if (classrooms == null) {
            classrooms = new ArrayList<>();
        }
        int index = classrooms.indexOf(classroom);
        if (index != -1) {
            classrooms.set(index, classroom);
            // Optionally, update in database
            // persistenceController.update(classroom);
        } else {
            System.out.println("Classroom not found for update: " + classroom.getId());
        }
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void addNewStudentGroup(StudentGroup studentGroup) {
        if (studentGroups == null) {
            studentGroups = new ArrayList<>();
        }
        studentGroups.add(studentGroup);
        // Optionally, save to database
        // persistenceController.add(studentGroup);
    }
    public void removeStudentGroup(String studentGroupId) {
        if (studentGroups == null) {
            return; // No student groups to remove
        }
        studentGroups.removeIf(studentGroup -> studentGroup.getId().equals(studentGroupId));
        // Optionally, remove from database
        // persistenceController.remove(StudentGroup.class, studentGroupId);
    }

    public void addNewTimePeriod(TimePeriod timePeriod) {
        if (timePeriods == null) {
            timePeriods = new ArrayList<>();
        }
        timePeriods.add(timePeriod);
        // Optionally, save to database
        // persistenceController.add(timePeriod);
    }

    public void removeTimePeriod(String timePeriodId) {
        if (timePeriods == null) {
            return; // No time periods to remove
        }
        timePeriods.removeIf(timePeriod -> timePeriod.getId().equals(timePeriodId));
        // Optionally, remove from database
        // persistenceController.remove(TimePeriod.class, timePeriodId);
    }

    public void addNewSubject(Subject subject) {
        if (subjects == null) {
            subjects = new ArrayList<>();
        }
        subjects.add(subject);
        // Optionally, save to database
        // persistenceController.add(subject);
    }
    public void removeSubject(String subjectId) {
        if (subjects == null) {
            return; // No subjects to remove
        }
        subjects.removeIf(subject -> subject.getId().equals(subjectId));
        // Optionally, remove from database
        // persistenceController.remove(Subject.class, subjectId);
    }

    public void solve (){
        //this.solver = new ScheduleSolver(teachers, studentGroups, subjects, classrooms, timePeriods);

    }



    public void testH() {
        // Define sample data (replace with your actual data loading)


        TimePeriod mon9 = new TimePeriod("mon9", "Lunes", LocalTime.of(9, 0), LocalTime.of(10, 0));
        TimePeriod mon10 = new TimePeriod("mon10", "Lunes", LocalTime.of(10, 0), LocalTime.of(11, 0));
        TimePeriod tue9 = new TimePeriod("tue9", "Martes", LocalTime.of(9, 0), LocalTime.of(10, 0));
        TimePeriod tue10 = new TimePeriod("tue10", "Martes", LocalTime.of(10, 0), LocalTime.of(11, 0));
        TimePeriod mon11 = new TimePeriod("mon11", "Lunes", LocalTime.of(11, 0), LocalTime.of(12, 0));
        //TimePeriod mon12= new TimePeriod("mon12", 1, LocalTime.of(10, 0), LocalTime.of(11, 0));
        TimePeriod tue11 = new TimePeriod("tue11", "Martes", LocalTime.of(11, 0), LocalTime.of(12, 0));
        TimePeriod tue12 = new TimePeriod("tue12", "Martes", LocalTime.of(12, 0), LocalTime.of(13, 0));





        Subject math = new Subject("math", "Mathematics", "MATH");
        math.setWeeklyAssignedHours(1);
        Subject physics = new Subject("physics", "Physics", "PHYS");
        physics.setWeeklyAssignedHours(2);
        Subject history = new Subject("history", "History", "HIST");
        history.setWeeklyAssignedHours(2);
        Subject chemistry = new Subject("chemistry", "Chemistry", "CHEM");
        chemistry.setWeeklyAssignedHours(3);
        chemistry.setMaxDailyHours(2);


        Classroom room101 = new Classroom("room101-10", "Room 101", "1");
        room101.setCapacity(10);
        Classroom room102 = new Classroom("room102", "Room 102", " 2");
        Classroom room103 = new Classroom("room103", "Room 103", "3");
        Classroom room104= new Classroom("room104-M", "Room 104", "4");
        //room104.setAssignedSubjects(List.of(math));
        //math.setAssignedClassroom(room104);

        StudentGroup grade9 = new StudentGroup("grade9-MHP", "Grade 9", "1");
        grade9.setRequiredSubjects(List.of(math, history, physics));
        StudentGroup grade10 = new StudentGroup("grade10-MC", "Grade 10", "2");
        grade10.setRequiredSubjects(List.of(chemistry, math));


        Teacher teacherAlice = new Teacher("AliceMP-pmon10-utue9", "Alice", "AL");
        teacherAlice.setPossibleSubjects(List.of(math, physics, chemistry, history));
        teacherAlice.addPreferredTimePeriod(tue9, 1);


        //teacherAlice.addUnPreferredTimePeriod(tue9);

        //teacherAlice.setPreferredStudentGroups(List.of(grade10));
        teacherAlice.setHoursWork(5);
        teacherAlice.addUnPreferredTimePeriod(mon9, 1);


        Teacher teacherBob = new Teacher("BobMHC", "Bob", "BOB");
        teacherBob.setPossibleSubjects(List.of(math, physics, history, chemistry));
        teacherBob.setPreferredSubjects(List.of(chemistry), 1);
        teacherBob.addPreferredTimePeriod(mon9, 1);
        teacherBob.setUnPreferredStudentGroups(List.of(grade9), 1);


        Teacher teacherCarol = new Teacher("CarolMH-ptue9-ptue10", "Carol", "CAR");
        teacherCarol.setPossibleSubjects(List.of(math, physics, history, chemistry));
        teacherCarol.addUnPreferredTimePeriod(tue9, 1);
        teacherCarol.addUnPreferredTimePeriod(tue10,1);
        teacherCarol.addPreferredTimePeriod(mon9, 1);
        teacherCarol.setPreferredStudentGroups(List.of(grade10), 1);
        teacherCarol.setHoursWork(5);



        //teacherAlice.addPreferredTimePeriod(mon9);

        //teacherAlice.addUnPreferredTimePeriod(mon9);


        teachers = new ArrayList<>(List.of(teacherAlice, teacherBob, teacherCarol));
        classrooms = new ArrayList<>(List.of(room101, room102, room103, room104));
        studentGroups = new ArrayList<>(List.of(grade9, grade10));
        timePeriods = new ArrayList<>(List.of(mon9, mon10, tue9, tue10, mon11, tue11));
        subjects = new ArrayList<>(List.of(math, physics, history, chemistry));

        //createExampleData();





        System.out.println("Attempting to create schedule...");
        generateSchedules(true);


    }

    public void generateSchedules(boolean test) {
        ScheduleSolver scheduleSolver = new ScheduleSolver(teachers, classrooms, studentGroups, timePeriods);
        if (test) {
            List<Schedule> generatedSchedules = scheduleSolver.createSchedule();
            for (Schedule generatedSchedule : generatedSchedules) {
                System.out.println("Generated Schedule:");

                if (generatedSchedule != null) {
                    generatedSchedule.printSchedule();
                    generatedSchedule.calculateConditions(teachers);

                } else {
                    System.out.println("Failed to generate a schedule.");
                }
            }
        }

    }

    public void updateTeacher(Teacher teacher) {
        if (teachers == null) {
            teachers = new ArrayList<>();
        }
        int index = teachers.indexOf(teacher);
        if (index != -1) {
            teachers.set(index, teacher);
            // Optionally, update in database
            // persistenceController.update(teacher);
        } else {
            System.out.println("Teacher not found for update: " + teacher.getId());
        }
    }

    public List<TimePeriod> getTimePeriods() {
        return timePeriods;
    }

    public void updateStudentGroup(StudentGroup g) {
        if (studentGroups == null) {
            studentGroups = new ArrayList<>();
        }
        int index = studentGroups.indexOf(g);
        if (index != -1) {
            studentGroups.set(index, g);
            // Optionally, update in database
            // persistenceController.update(g);
        } else {
            System.out.println("Student group not found for update: " + g.getId());
        }
    }

    public void updateSubject(Subject s) {
        if (subjects == null) {
            subjects = new ArrayList<>();
        }
        int index = subjects.indexOf(s);
        if (index != -1) {
            subjects.set(index, s);
            // Optionally, update in database
            // persistenceController.update(s);
        } else {
            System.out.println("Subject not found for update: " + s.getId());
        }
    }

    public void updateTimePeriod(TimePeriod tp) {
        if (timePeriods == null) {
            timePeriods = new ArrayList<>();
        }
        int index = timePeriods.indexOf(tp);
        if (index != -1) {
            timePeriods.set(index, tp);
            // Optionally, update in database
            // persistenceController.update(tp);
        } else {
            System.out.println("Time period not found for update: " + tp.getId());
        }
    }

    public List<String> getScheduleIds() {
        List<String> scheduleIds = new ArrayList<>();
        for (Teacher teacher : teachers) {
            for (Schedule schedule : teacher.getSchedules()) {
                scheduleIds.add(schedule.getId());
            }
        }

        return scheduleIds;
    }

    public void addTeacher(Teacher t) {
        if (teachers == null) {
            teachers = new ArrayList<>();
        }
        teachers.add(t);
        // Optionally, save to database
        // persistenceController.add(t);
    }

    public List<Schedule> getAllSchedules() {
        List<Schedule> schedules = new ArrayList<>();
        for (Teacher teacher : teachers) {
            schedules.addAll(teacher.getSchedules());
            break;
        }
        return schedules;
    }

    public void renameSchedule(String id, String name) {
        for (Teacher teacher : teachers) {
            for (Schedule schedule : teacher.getSchedules()) {
                if (schedule.getId().equals(id)) {
                    schedule.setName(name);
                    // Optionally, update in database
                    // persistenceController.update(schedule);
                    return; // Exit after renaming the first matching schedule
                }
            }
        }
        for (StudentGroup studentGroup : studentGroups) {
            for (Schedule schedule : studentGroup.getSchedules()) {
                if (schedule.getId().equals(id)) {
                    schedule.setName(name);
                    // Optionally, update in database
                    // persistenceController.update(schedule);
                    return; // Exit after renaming the first matching schedule
                }
            }
        }
        for (Classroom classroom : classrooms) {
            for (Schedule schedule : classroom.getSchedules()) {
                if (schedule.getId().equals(id)) {
                    schedule.setName(name);
                    // Optionally, update in database
                    // persistenceController.update(schedule);
                    return; // Exit after renaming the first matching schedule
                }
            }
        }
    }

    public void regenarateSchedules() {
        generateSchedules(false);

    }
}
