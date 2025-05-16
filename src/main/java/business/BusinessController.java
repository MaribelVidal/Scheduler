package business;

import persistence.PersistenceController;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class BusinessController {
    private ScheduleSolver solver;

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
       solver.printMatrix();
    }



    public void createAllTimePeriods (int weekDay, LocalTime initialHourDay, LocalTime finalHourDay, LocalTime period){
        long totalMinutes = Duration.between(initialHourDay, finalHourDay).toMinutes();
        long periodMinutes = period.getHour()*60 + period.getMinute();
        int timePeriodNumber = (int) (totalMinutes / periodMinutes);

        for (int i = 0; i < weekDay; i++) {
            for (int j = 0; j < timePeriodNumber; j++) {
              TimePeriod timeperiod = new TimePeriod(UUID.randomUUID().toString(), weekDay, initialHourDay, period);
                timePeriods.add(timeperiod);


            }

        }



    }

    public void solve (){
        this.solver = new ScheduleSolver(teachers, studentGroups, subjects, classrooms, timePeriods);

    }

    // Método para crear datos de ejemplo
    public void createExampleData() {
        try {
            /* 1. Crear departamentos
            Department dep1 = new Department("DEP1", "Informática");
            Department dep2 = new Department("DEP2", "Matemáticas");
*/
            // 2. Crear profesores
            Teacher teacher1 = new Teacher("T1", "Juan Pérez", "JPZ");
            teacher1.setEmail("juan@ejemplo.com");
            teacher1.setPhone("666111222");
            teacher1.setHoursWork(20);

            Teacher teacher2 = new Teacher("T2", "María López", "MLZ");
            teacher2.setEmail("maria@ejemplo.com");
            teacher2.setPhone("666333444");
            teacher2.setHoursWork(15);

            Teacher teacher3 = new Teacher("T3", "Pepe Pez", "MLZ");
            teacher3.setEmail("maria@ejemplo.com");
            teacher3.setPhone("666333444");
            teacher3.setHoursWork(15);

            // 3. Crear aulas
            Classroom classroom1 = new Classroom("C1", "Aula 101", "A101");
            classroom1.setCapacity(30);
            classroom1.setClassroomType("Normal");

            Classroom classroom2 = new Classroom("C2", "Laboratorio", "LAB");
            classroom2.setCapacity(25);
            classroom2.setClassroomType("Laboratorio");


            // 4. Crear activities

            Activity activity1 = new Activity("A1", "Actividad 1", "ACT1");
            activity1.setClassroom(classroom1);
            activity1.setTeachers(List.of(teacher1, teacher2));

            // 4. Crear asignaturas
            Subject subject1 = new Subject("S1", "Programación", "PROG");
            subject1.setCourse("1º DAM");
            subject1.setDepartment("dep1");
            subject1.setAssignedClassroom(classroom1);
            subject1.setWeeklyAssignedHours(1);

            Subject subject2 = new Subject("S2", "Bases de Datos", "BBDD");
            subject2.setCourse("1º DAM");
            subject2.setDepartment("dep2");
            subject2.setAssignedClassroom(classroom2);
            subject2.setWeeklyAssignedHours(2);

            // 5. Crear grupos de estudiantes
            StudentGroup group1 = new StudentGroup("G1", "Primer curso DAM", "1DAM");
            group1.setCourse("1º DAM");
            group1.setAssignedTutor(teacher1);
            group1.setNumberOfStudents(25);

            StudentGroup group2 = new StudentGroup("G2", "Primer curso DAM", "1DAM");
            group2.setCourse("2º DAM");
            group2.setAssignedTutor(teacher1);
            group2.setNumberOfStudents(25);


            // 6. Crear periodos de tiempo
            LocalTime start = LocalTime.of(9, 0);
            LocalTime end = LocalTime.of(10, 0);
            TimePeriod period1 = new TimePeriod("TP1", 1, start, end);


            start = LocalTime.of(11, 0);
            end = LocalTime.of(12, 0);
            TimePeriod period2 = new TimePeriod("TP2", 1, start, end);


            // 7. Asignar asignaturas a profesores
            teacher1.addPossibleSubject(subject1);
            teacher1.addPossibleSubject(subject2);
            teacher2.addPossibleSubject(subject1);

            // 8. Guardar en base de datos
            /*
            departmentDAO.add(dep1);
            departmentDAO.add(dep2);


*/
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
}
