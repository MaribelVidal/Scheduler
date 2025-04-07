package business;

public class ScheduledClass {
    private Teacher teacher;
    private StudentGroup studentgroup;
    private Subject subject;
    private Classroom classroom;
    private TimePeriod timeperiod;


    public ScheduledClass(Teacher teacher, StudentGroup studentgroup, Subject subject, Classroom classroom, TimePeriod timeperiod) {
        this.teacher = teacher;
        this.studentgroup = studentgroup;
        this.subject = subject;
        this.classroom = classroom;
        this.timeperiod = timeperiod;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public StudentGroup getStudentgroup() {
        return studentgroup;
    }

    public void setStudentgroup(StudentGroup studentgroup) {
        this.studentgroup = studentgroup;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }

    public TimePeriod getTimeperiod() {
        return timeperiod;
    }

    public void setTimeperiod(TimePeriod timeperiod) {
        this.timeperiod = timeperiod;
    }
}
