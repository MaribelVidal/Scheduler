package business;

public class Condition {
    private Teacher teacher;
    private int weight;

    private Subject subject;
    private TimePeriod timePeriod;
    private StudentGroup studentGroup;

    private Subject dummySubject =  new Subject("dummy", "Dummy Subject", "dummy");
    private TimePeriod dummyTimePeriod = new TimePeriod("dummy", "Monday", null, null);
    private StudentGroup dummyStudentGroup = new StudentGroup("dummy", "Dummy Group", "dummy");

    public Condition(Teacher teacher, int weight) {
        this.teacher = teacher;
        this.weight = weight;
        this.subject = dummySubject;
        this.timePeriod = dummyTimePeriod;
        this.studentGroup = dummyStudentGroup;
    }

    public Condition (Teacher teacher, int weight, Subject subject) {
        this.teacher = teacher;
        this.weight = weight;
        this.subject = subject;
        this.timePeriod = dummyTimePeriod;
        this.studentGroup = dummyStudentGroup;
    }

    public Condition (Teacher teacher, int weight, TimePeriod timePeriod) {
        this.teacher = teacher;
        this.weight = weight;
        this.subject = dummySubject;
        this.timePeriod = timePeriod;
        this.studentGroup = dummyStudentGroup;

    }

    public Condition (Teacher teacher, int weight, StudentGroup studentGroup) {
        this.teacher = teacher;
        this.weight = weight;
        this.subject = dummySubject;
        this.timePeriod = dummyTimePeriod;
        this.studentGroup = studentGroup    ;
    }



    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public TimePeriod getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(TimePeriod timePeriod) {
        this.timePeriod = timePeriod;
    }

    public StudentGroup getStudentGroup() {
        return studentGroup;
    }

    public void setStudentGroup(StudentGroup studentGroup) {
        this.studentGroup = studentGroup;
    }
}
