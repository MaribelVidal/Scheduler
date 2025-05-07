package business;

import java.util.List;

public class Student extends Entity{

    //private String name;
    //private String email;
    private List<StudentGroup> studentGroup; //Esto es una lista. Ej. hamza en 1ESO y en 1ESOPAI

    public Student(String id, String name, String abbreviation) {
        super(id, name, abbreviation);
    }
}
