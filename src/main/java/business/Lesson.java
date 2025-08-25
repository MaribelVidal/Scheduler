package business;

import org.chocosolver.solver.variables.IntVar;

import java.util.UUID;

public class Lesson {

    private String id;
    private Teacher teacher;
    private StudentGroup studentGroup;
    private Subject subject;
    private Classroom classroom;
    private TimePeriod timePeriod;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Lesson(Teacher teacher, StudentGroup studentGroup, Classroom classroom, TimePeriod timePeriod, Subject subject) {
        this.id = UUID.randomUUID().toString();
        this.teacher = teacher;
        this.studentGroup = studentGroup;
        this.classroom = classroom;
        this.timePeriod = timePeriod;
        this.subject = subject;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public StudentGroup getStudentGroup() {
        return studentGroup;
    }

    public void setStudentGroup(StudentGroup studentGroup) {
        this.studentGroup = studentGroup;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    public TimePeriod getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }
}
