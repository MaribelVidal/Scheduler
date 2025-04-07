package business;

import java.util.List;

public class Teacher extends Entity {
    //private int id; // pasa a tenerlo Entity
   //private String name; // Va a entity
    //private String abbreviation; // va a entity
    private String email;
    private String phone;

    private Department department;
    //private boolean tutorial;
    private List<Subject> possibleSubjects;
    //private TimePeriod timePeriod;
    private List<String> role; // tutor, profesor, jefe de estudios...
    private int hoursWork; // Máximo número de horas
    //private int assignedHoursWork; // horas de docencia asignadas

    //preferencias



    public Teacher(int id, String name, String abbreviation) {
        super(id, name, abbreviation);
    }





}
