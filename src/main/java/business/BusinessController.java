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

/*
        this.persistenceController = new PersistenceController();
        persistenceController.initialize();
        getFromDataBase();
*/
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

    public void solve (){
        //this.solver = new ScheduleSolver(teachers, studentGroups, subjects, classrooms, timePeriods);

    }

    // Método para crear datos de ejemplo
    public void createExampleData() {
        try {
            /* 1. Crear departamentos
            Department dep1 = new Department("DEP1", "Informática");
            Department dep2 = new Department("DEP2", "Matemáticas");
*/
            // 2. Crear profesores
            Teacher teacher1 = new Teacher("T4", "Juan Pérez", "JPZ");
            teacher1.setEmail("juan@ejemplo.com");
            teacher1.setPhone("666111222");
            teacher1.setHoursWork(15);

            Teacher teacher2 = new Teacher("T5", "María López", "MLZ");
            teacher2.setEmail("maria@ejemplo.com");
            teacher2.setPhone("666333444");
            teacher2.setHoursWork(15);

            Teacher teacher3 = new Teacher("T6", "Pepe Pez", "MLZ");
            teacher3.setEmail("maria@ejemplo.com");
            teacher3.setPhone("666333444");
            teacher3.setHoursWork(15);

            // 3. Crear aulas
            Classroom classroom1 = new Classroom("C4", "Aula 101", "A101");
            classroom1.setCapacity(30);
            classroom1.setClassroomType("Normal");

            Classroom classroom2 = new Classroom("C5", "Laboratorio", "LAB");
            classroom2.setCapacity(25);
            classroom2.setClassroomType("Laboratorio");


            // 4. Crear activities

            Activity activity1 = new Activity("A1", "Actividad 1", "ACT1");
            activity1.setClassroom(classroom1);
            activity1.setTeachers(List.of(teacher1, teacher2));

            // 4. Crear asignaturas
            Subject subject1 = new Subject("S5", "Programación", "PROG");
            subject1.setCourse("1º DAM");
            subject1.setDepartment("dep1");
            //subject1.setAssignedClassroom(classroom1);
            subject1.setWeeklyAssignedHours(1);

            Subject subject2 = new Subject("S6", "Bases de Datos", "BBDD");
            subject2.setCourse("1º DAM");
            subject2.setDepartment("dep2");
            subject2.setAssignedClassroom(classroom2);
            subject2.setWeeklyAssignedHours(3);
            //classroom1.addAssignedSubject(subject1);

            // 5. Crear grupos de estudiantes
            StudentGroup group1 = new StudentGroup("G3", "Primer curso DAM", "1DAM");
            group1.setCourse("1º DAM");
            group1.setAssignedTutor(teacher1);
            group1.setNumberOfStudents(25);
            group1.setRequiredSubjects(List.of(subject1, subject2));

            StudentGroup group2 = new StudentGroup("G4", "Primer curso DAM", "1DAM");
            group2.setCourse("2º DAM");
            group2.setAssignedTutor(teacher1);
            group2.setNumberOfStudents(25);
            group2.setRequiredSubjects(List.of(subject1, subject2));

            // 6. Crear periodos de tiempo
            LocalTime start = LocalTime.of(9, 0);
            LocalTime end = LocalTime.of(10, 0);
            TimePeriod period1 = new TimePeriod("TP4", "Lunes", start, end);
            //teacher1.addUnavailableTimePeriods(period1);

            start = LocalTime.of(11, 0);
            end = LocalTime.of(12, 0);
            TimePeriod period2 = new TimePeriod("TP5", "Lunes", start, end);


            // 7. Asignar asignaturas a profesores
            teacher1.addPossibleSubject(subject1);
            teacher1.addPossibleSubject(subject2);
            teacher2.addPossibleSubject(subject1);
            teacher2.addPossibleSubject(subject2);



            // 8. Guardar en base de datos
            /*
            departmentDAO.add(dep1);
            departmentDAO.add(dep2);


*/
            /*
            persistenceController.add(classroom1);
            persistenceController.add(classroom2);
            persistenceController.add(subject1);
            persistenceController.add(subject2);
            persistenceController.add(teacher1);
            persistenceController.add(teacher2);
            persistenceController.add(teacher3);
            persistenceController.add(activity1);
            persistenceController.add(group1);
            persistenceController.add(group2);
            persistenceController.add(period1);
            persistenceController.add(period2);

            System.out.println("Datos de ejemplo creados correctamente");
*/
            teachers.add(teacher1);
            teachers.add(teacher2);
            teachers.add(teacher3);
            classrooms.add(classroom1);
            classrooms.add(classroom2);
            subjects.add(subject1);
            subjects.add(subject2);
            studentGroups.add(group1);
            studentGroups.add(group2);
            timePeriods.add(period1);
            timePeriods.add(period2);


        } catch (Exception e) {
            System.err.println("Error creando datos de ejemplo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void testH() {
        // Define sample data (replace with your actual data loading)


        TimePeriod mon9 = new TimePeriod("mon9", "Lunes", LocalTime.of(9, 0), LocalTime.of(10, 0));
        TimePeriod mon10 = new TimePeriod("mon10", "Lunes", LocalTime.of(10, 0), LocalTime.of(11, 0));
        TimePeriod tue9 = new TimePeriod("tue9", "Martes", LocalTime.of(9, 0), LocalTime.of(10, 0));
        TimePeriod tue10 = new TimePeriod("tue10", "Martes", LocalTime.of(10, 0), LocalTime.of(11, 0));
        TimePeriod mon11 = new TimePeriod("mon11", "Lunes", LocalTime.of(9, 0), LocalTime.of(10, 0));
        //TimePeriod mon12= new TimePeriod("mon12", 1, LocalTime.of(10, 0), LocalTime.of(11, 0));
        TimePeriod tue11 = new TimePeriod("tue11", "Martes", LocalTime.of(11, 0), LocalTime.of(12, 0));
        TimePeriod tue12 = new TimePeriod("tue12", "Martes", LocalTime.of(10, 0), LocalTime.of(11, 0));





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
        ScheduleSolver scheduleSolver = new ScheduleSolver(teachers, classrooms, studentGroups, timePeriods);
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
