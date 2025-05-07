package business;

//Clase para que hereden el resto de clases (Teacher, student...)
public class Entity {
    private String id;
    private String name;
    private String abbreviation;
    private Schedule schedule;

    public Entity(String id, String name, String abbreviation){
        this.id = id;
        this.name = name;
        this.abbreviation = abbreviation;
        this.schedule = new Schedule();

    }




    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }


    /*public int[][] getSchedule() {
        return schedule.getSchedule();
    }*/

    public Schedule getSchedule() {
    return schedule;
}

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

}