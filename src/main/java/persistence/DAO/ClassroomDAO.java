package persistence.DAO;

import business.Classroom;
import business.Teacher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassroomDAO {

    private Connection connection;


    public ClassroomDAO(Connection connection) {
        this.connection = connection;
    }


    public void addClassroom(Classroom classroom) throws SQLException {
        String query = "INSERT INTO classrooms (id, name, abbreviation, classroomType, capacity) VALUES (?, ?, ?, ?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1,classroom.getId());
            preparedStatement.setString(2,classroom.getName());
            preparedStatement.setString(3,classroom.getAbbreviation());
            preparedStatement.setString(4,classroom.getClassroomType());
            preparedStatement.setString(5,classroom.getCapacity());


            preparedStatement.executeUpdate();
        }

    }

    public List<Classroom> getAllClassrooms() throws SQLException{
        List<Classroom> classrooms = new ArrayList<>();
        String query = "SELECT * FROM classrooms";
        try(Statement createStatement = connection.createStatement();
            ResultSet resultset = createStatement.executeQuery(query)) {

            while (resultset.next()) {
                int id = resultset.getInt("id");
                String name = resultset.getString("name");
                String abbreviation = resultset.getString("abbreviation");
                String classroomType = resultset.getString("classroomType");
                int capacity = resultset.getInt("capacity");


                Classroom classroom = new Classroom(id, name, abbreviation);
                classroom.setClassroomType(classroomType);
                classroom.setCapacity(capacity);

                classrooms.add(classroom);
            }


        }
        return classrooms;

    }

    public void deleteClassroom (Classroom classroom) throws SQLException{
        String query = "DELETE FROM classrooms WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1,classroom.getId());
            preparedStatement.executeUpdate();
        }

    }
}
