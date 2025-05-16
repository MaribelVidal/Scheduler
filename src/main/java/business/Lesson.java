package business;

import org.chocosolver.solver.variables.IntVar;

public class Lesson {

    private int teacher;
    private int studentGroup;
    private int classroom;
    private int timePeriod;
    private int subject;

    public Lesson(int teacher, int studentGroup, int classroom, int timePeriod, int subject) {
        this.teacher = teacher;
        this.studentGroup = studentGroup;
        this.classroom = classroom;
        this.timePeriod = timePeriod;
        this.subject = subject;
    }

    public int getTeacher() {
        return teacher;
    }

    public void setTeacher(int teacher) {
        this.teacher = teacher;
    }

    public int getStudentGroup() {
        return studentGroup;
    }

    public void setStudentGroup(int studentGroup) {
        this.studentGroup = studentGroup;
    }

    public int getClassroom() {
        return classroom;
    }

    public void setClassroom(int classroom) {
        this.classroom = classroom;
    }

    public int getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(int timePeriod) {
        this.timePeriod = timePeriod;
    }

    public int getSubject() {
        return subject;
    }

    public void setSubject(int subject) {
        this.subject = subject;
    }
}
