package business;

import java.util.List;

public class Subject extends Entity{


    private String department;
    private int weeklyAssignedHours;
    private String course;// ejemplo 4ESO
    private Classroom assignedClassroom;


    public Subject(String id, String name, String abbreviation) {
        super(id, name, abbreviation);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getWeeklyAssignedHours() {
        return weeklyAssignedHours;
    }

    public void setWeeklyAssignedHours(int weeklyAssignedHours) {
        this.weeklyAssignedHours = weeklyAssignedHours;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public Classroom getAssignedClassroom() {
        return assignedClassroom;
    }

    public void setAssignedClassroom(Classroom assignedClassroom) {
        this.assignedClassroom = assignedClassroom;
    }
}
