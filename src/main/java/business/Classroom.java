package business;

public class Classroom extends Entity{

    //private String name;
    //private String abbreviation;

    private String classroomType; //dedicada o general
    private int capacity;


    public Classroom(int id, String name, String abbreviation) {
        super(id, name, abbreviation);
    }
}
