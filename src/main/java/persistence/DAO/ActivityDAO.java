package persistence.DAO;

import business.Activity;
import business.Classroom;
import business.Teacher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityDAO implements DAO<Activity> {

    private final ClassroomDAO classroomDAO;
    private Connection connection;
    private final TeachersInActivityDAO teachersInActivityDAO;

    public ActivityDAO(Connection connection) {
        this.connection = connection;
        this.teachersInActivityDAO = new TeachersInActivityDAO(connection, new TeacherDAO(connection));
        this.classroomDAO = new ClassroomDAO(connection);
    }

    // Implementación de los métodos de la interfaz DAO para la clase Activity
    @Override
    public void add(Activity activity) throws Exception {
        // Lógica para agregar una actividad a la base de datos
        String query = "INSERT INTO activities (id, name, abbreviation, classroom) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, activity.getId());
            preparedStatement.setString(2, activity.getName());
            preparedStatement.setString(3, activity.getAbbreviation());




            if (activity.getClassroom() != null) {
                preparedStatement.setString(4, activity.getClassroom().getId());
            } else {
                preparedStatement.setNull(4, Types.VARCHAR);
            }

            preparedStatement.executeUpdate();

            for(Teacher teacher :activity.getTeachers()){
                teachersInActivityDAO.assignActivityTeachers(activity.getId(), teacher.getId());

            }
        }
    }




    @Override
    public List<Activity> getAll() throws Exception {
        // Lógica para obtener todas las actividades de la base de datos
        List<Activity> activities = new ArrayList<>();
        String query = "SELECT * FROM activities";
        try (Statement createStatement = connection.createStatement();
             ResultSet resultset = createStatement.executeQuery(query)) {

            while (resultset.next()) {
                String id = resultset.getString("id");
                String name = resultset.getString("name");
                String abbreviation = resultset.getString("abbreviation");

                String classroomId = resultset.getString("classroom");
                List<Teacher> teachers = teachersInActivityDAO.getAllActivityTeachers(id);
                Activity activity = new Activity(id, name, abbreviation);
                activity.setTeachers(teachers);

                if (classroomId == null || classroomId.isEmpty()) {
                    activity.setClassroom(null);
                } else {
                    Classroom classroom = classroomDAO.getOne(classroomId);
                    activity.setClassroom(classroom);
                }

                activities.add(activity);
            }
        }
        return null;
    }

    @Override
    public void delete(Activity activity) throws Exception {
        // Lógica para eliminar una actividad de la base de datos
        String query = "DELETE FROM activities WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, activity.getId());
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public Activity getOne(String id) throws Exception {
        // Lógica para obtener una actividad por su ID de la base de datos
        String query = "SELECT * FROM activities WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String name = resultSet.getString("name");
                String abbreviation = resultSet.getString("abbreviation");
                List<Teacher> teachers = teachersInActivityDAO.getAllActivityTeachers(id);
                String classroomId = resultSet.getString("classroom");

                Activity activity = new Activity(id, name, abbreviation);
                activity.setTeachers(teachers);


                if (classroomId == null || classroomId.isEmpty()) {
                    activity.setClassroom(null);
                } else {
                    Classroom classroom = classroomDAO.getOne(classroomId);
                    activity.setClassroom(classroom);
                }


                return activity;
            }
        }
        return null;
    }

    @Override
    public void update(Activity activity) throws Exception {
        // Lógica para actualizar una actividad en la base de datos
        String query = "UPDATE activities SET name = ?, abbreviation = ?, classroom = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, activity.getName());
            preparedStatement.setString(2, activity.getAbbreviation());
            for(Teacher teacher :activity.getTeachers()){
                teachersInActivityDAO.assignActivityTeachers(activity.getId(), teacher.getId());

            }

            if (activity.getClassroom() != null) {
                preparedStatement.setString(3, activity.getClassroom().getId());
            } else {
                preparedStatement.setNull(3, Types.VARCHAR);
            }

            preparedStatement.setString(4, activity.getId());
            preparedStatement.executeUpdate();
        }
    }
}
