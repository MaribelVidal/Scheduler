package business;

import org.chocosolver.solver.variables.IntVar;

public class Lesson {

    private IntVar teacher;
    private IntVar studentGroup;
    private IntVar classroom;
    private IntVar timePeriod;
    private IntVar subject;

    public Lesson(IntVar teacher, IntVar studentGroup, IntVar classroom, IntVar timePeriod, IntVar subject) {
        this.teacher = teacher;
        this.studentGroup = studentGroup;
        this.classroom = classroom;
        this.timePeriod = timePeriod;
        this.subject = subject;
    }

    public IntVar getTeacher() {
        return teacher;
    }

    public void setTeacher(IntVar teacher) {
        this.teacher = teacher;
    }

    public IntVar getStudentGroup() {
        return studentGroup;
    }

    public void setStudentGroup(IntVar studentGroup) {
        this.studentGroup = studentGroup;
    }

    public IntVar getClassroom() {
        return classroom;
    }

    public void setClassroom(IntVar classroom) {
        this.classroom = classroom;
    }

    public IntVar getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(IntVar timePeriod) {
        this.timePeriod = timePeriod;
    }

    public IntVar getSubject() {
        return subject;
    }

    public void setSubject(IntVar subject) {
        this.subject = subject;
    }
}
