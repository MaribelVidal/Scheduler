package business;

import java.util.List;

public class Activity extends Entity{
    private List<Teacher> teachers;
    private Classroom classroom;

public Activity(String id, String name, String abbreviation) {
        super(id, name, abbreviation);
    }

    public List<Teacher> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<Teacher> teachers) {
        this.teachers = teachers;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
    }
}
