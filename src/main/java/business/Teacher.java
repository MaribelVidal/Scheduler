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


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public List<Subject> getPossibleSubjects() {
        return possibleSubjects;
    }

    public void setPossibleSubjects(List<Subject> possibleSubjects) {
        this.possibleSubjects = possibleSubjects;
    }

    public List<String> getRole() {
        return role;
    }

    public void setRole(List<String> role) {
        this.role = role;
    }

    public int getHoursWork() {
        return hoursWork;
    }

    public void setHoursWork(int hoursWork) {
        this.hoursWork = hoursWork;
    }
}
