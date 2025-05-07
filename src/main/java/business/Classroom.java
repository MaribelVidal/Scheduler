package business;

public class Classroom extends Entity{


    private String classroomType; //dedicada o general
    private int capacity;


    public Classroom(String id, String name, String abbreviation) {
        super(id, name, abbreviation);
    }

    public String getClassroomType() {
        return classroomType;
    }

    public void setClassroomType(String classroomType) {
        this.classroomType = classroomType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
