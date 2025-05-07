package business;

import java.time.LocalTime;
import java.util.List;

public class TimePeriod {

   private String id;
   private int weekday;
   private LocalTime initialHour;
   private LocalTime finalHour;
   private String idTeacher;
   private String idSubject;
   private String idStudentGroup;
   private String idClassroom;

    public TimePeriod(String id, int weekday, LocalTime initialHour, LocalTime finalHour) {

        this.id = id;
        this.weekday = weekday;
        this.initialHour = initialHour;
        this.finalHour = finalHour;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getWeekday() {
        return weekday;
    }

    public void setWeekday(int weekday) {
        this.weekday = weekday;
    }

    public LocalTime getInitialHour() {
        return initialHour;
    }

    public void setInitialHour(LocalTime initialHour) {
        this.initialHour = initialHour;
    }

    public LocalTime getFinalHour() {
        return finalHour;
    }

    public void setFinalHour(LocalTime finalHour) {
        this.finalHour = finalHour;
    }

    public String getIdTeacher() {
        return idTeacher;
    }

    public void setIdTeacher(String idTeacher) {
        this.idTeacher = idTeacher;
    }

    public String getIdSubject() {
        return idSubject;
    }

    public void setIdSubject(String idSubject) {
        this.idSubject = idSubject;
    }

    public String getIdStudentGroup() {
        return idStudentGroup;
    }

    public void setIdStudentGroup(String idStudentGroup) {
        this.idStudentGroup = idStudentGroup;
    }

    public String getIdClassroom() {
        return idClassroom;
    }

    public void setIdClassroom(String idClassroom) {
        this.idClassroom = idClassroom;
    }
}
