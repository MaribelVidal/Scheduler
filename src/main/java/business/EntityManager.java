package business;

import java.util.ArrayList;
import java.util.List;

// Clase para gestionar listas y otros de las diferentes entidades
public class EntityManager {
    private static List<Entity> entities;





    public static Entity getEntity(int id){
        return entities.get(id);
    }

    public static List<Schedule> getTeacherSchedules() {
    return new ArrayList<>(); // hay que cambiarlo
    }

    public static List<Schedule> getStudentGroupSchedules() {
        return new ArrayList<>(); // hay que cambiarlo
    }

    public static List<Schedule> getStudentSchedules() {
        return new ArrayList<>(); // hay que cambiarlo
    }

    public static List<Schedule> getClassroomSchedules() {
        return new ArrayList<>(); // hay que cambiarlo
    }
}
