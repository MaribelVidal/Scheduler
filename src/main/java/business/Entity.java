package business;

//Clase para que hereden el resto de clases (Teacher, student...)
public class Entity {
    private int id;
    private String name;
    private String abbreviation;
    private Schedule schedule;

    public Entity(int id, String name, String abbreviation){
        this.id = id;
        this.name = name;
        this.abbreviation = abbreviation;
        this.schedule = new Schedule();

    }




    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }


    /*public int[][] getSchedule() {
        return schedule.getSchedule();
    }*/


    public Schedule getSchedule() {
    return schedule;
}

}