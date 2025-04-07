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
}
