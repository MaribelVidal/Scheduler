package business;

//le añadimos un atributo troncal y optativa para establecer los grupos ES4A, bilingüe...
public class StudentGroup extends Entity{

    //private String name;
    //private String abbreviation;

    private String course;
    private Teacher assignedTutor;


    private int weeklyGroupHours; // Número de horas asignadas a este grupo
    private int numberOfStudents;

    public StudentGroup(int id, String name, String abbreviation) {
        super(id, name, abbreviation);
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public Teacher getAssignedTutor() {
        return assignedTutor;
    }

    public void setAssignedTutor(Teacher assignedTutor) {
        this.assignedTutor = assignedTutor;
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
}
