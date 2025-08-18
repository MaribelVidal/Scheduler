package business;

import java.util.UUID;

public class Condition {

    private String id;
    private  String ConditionType;
    private Teacher teacher;
    private int weight;

    private Subject subject;
    private TimePeriod timePeriod;
    private StudentGroup studentGroup;

    private Subject dummySubject =  new Subject("dummy", "Dummy Subject", "dummy");
    private TimePeriod dummyTimePeriod = new TimePeriod("dummy", "Monday", null, null);
    private StudentGroup dummyStudentGroup = new StudentGroup("dummy", "Dummy Group", "dummy");

    public Condition(Teacher teacher, int weight) {
        this.id = UUID.randomUUID().toString();
        this.teacher = teacher;
        this.weight = weight;
        this.subject = dummySubject;
        this.timePeriod = dummyTimePeriod;
        this.studentGroup = dummyStudentGroup;
        this.ConditionType = "None";
    }

    public Condition (Teacher teacher, int weight, Subject subject) {
        this.id = UUID.randomUUID().toString();
        this.teacher = teacher;
        this.weight = weight;
        this.subject = subject;
        this.timePeriod = dummyTimePeriod;
        this.studentGroup = dummyStudentGroup;
        this.ConditionType = "Subject";
    }

    public Condition (Teacher teacher, int weight, TimePeriod timePeriod) {
        this.id = UUID.randomUUID().toString();
        this.teacher = teacher;
        this.weight = weight;
        this.subject = dummySubject;
        this.timePeriod = timePeriod;
        this.studentGroup = dummyStudentGroup;
        this.ConditionType = "TimePeriod";

    }

    public Condition (Teacher teacher, int weight, StudentGroup studentGroup) {
        this.id = UUID.randomUUID().toString();
        this.teacher = teacher;
        this.weight = weight;
        this.subject = dummySubject;
        this.timePeriod = dummyTimePeriod;
        this.studentGroup = studentGroup    ;
        this.ConditionType = "StudentGroup";
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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

    public String getConditionType() {
        return ConditionType;
    }
}
