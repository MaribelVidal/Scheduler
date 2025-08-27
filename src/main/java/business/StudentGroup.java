package business;

import java.util.ArrayList;
import java.util.List;

//le añadimos un atributo troncal y optativa para establecer los grupos ES4A, bilingüe...
public class StudentGroup extends Entity{


    private String course;
    private int weeklyGroupHours; // Número de horas asignadas a este grupo
    private int numberOfStudents;
    private List<Subject> requiredSubjects; // Unidades programadas para este grupo
    private List<Schedule> schedules = new ArrayList<>(); // Horarios asignados a este grupo

    public StudentGroup(String id, String name, String abbreviation) {
        super(id, name, abbreviation);
        this.numberOfStudents = 25; // Inicializar a 25 por defecto
    }


    public List<Schedule> getSchedules() {
        if (schedules == null) schedules = new ArrayList<>();
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    public void addSchedule(Schedule schedule) {
        if (this.schedules == null) {
            this.schedules = new ArrayList<>();
        }
        this.schedules.add(schedule);
    }

    public Schedule getScheduleById(String Id){
        for (Schedule schedule : schedules) {
            if (schedule.getId().equals(Id)) {
                return schedule;
            }
        }
        return null; // or throw an exception if not found
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }


    public int getWeeklyGroupHours() {
        return weeklyGroupHours;
    }

    public void setWeeklyGroupHours(int weeklyGroupHours) {
        this.weeklyGroupHours = weeklyGroupHours;
    }

    public int getNumberOfStudents() {
        return numberOfStudents;
    }

    public void setNumberOfStudents(int numberOfStudents) {
        this.numberOfStudents = numberOfStudents;
    }

    public List<Subject> getRequiredSubjects() {    return requiredSubjects;}

    public void setRequiredSubjects(List<Subject> requiredSubjects) {    this.requiredSubjects = requiredSubjects;}

    public void addRequiredSubject(Subject subject) {    this.requiredSubjects.add(subject);}


}
