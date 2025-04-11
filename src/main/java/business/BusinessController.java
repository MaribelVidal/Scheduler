package business;

import persistence.PersistenceController;

import java.util.List;

public class BusinessController {
    private ScheduleSolver solver;

    private List<Teacher> teachers;
    private List<StudentGroup> studentGroups;
    private List<Subject> subjects;
    private List<Classroom> classrooms;
    private List<TimePeriod> timePeriods;

    private PersistenceController persistenceController;

    public BusinessController(){

        this.persistenceController = new PersistenceController();
        getFromDataBase();

        this.solver = new ScheduleSolver(teachers, studentGroups, subjects, classrooms, timePeriods);
    }

    private void getFromDataBase (){}

    public void debug() {
        //Teacher teacher = new Teacher(5);
        //solver.ScheduleShower(teacher.getId(), "teacher");
    }

    public void test () {
        solver.printMatrix();
    }
}
