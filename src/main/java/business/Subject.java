package business;

import java.util.List;

public class Subject extends Entity{


    private String department;
    private int weeklyAssignedHours;
    private String course;// ejemplo 4ESO
    private Classroom assignedClassroom;
    private int duration; // Duración de la asignatura en horas semanales
    private int maxDailyHours; // Horas diarias máximas de la asignatura;

    public Subject(String id, String name, String abbreviation) {
        super(id, name, abbreviation);
        this.weeklyAssignedHours = 1; // Inicializar a 0 por defecto
        this.duration = 1;
        this.maxDailyHours = 2;// Default duration is 2 hour
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

    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getMaxDailyHours() {
        return maxDailyHours;
    }
    public void setMaxDailyHours(int maxDailyHours) {
        this.maxDailyHours = maxDailyHours;
    }
}
