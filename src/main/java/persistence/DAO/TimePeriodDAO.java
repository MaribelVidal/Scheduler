package persistence.DAO;

import business.Teacher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TimePeriodDAO {

    private Connection connection;


    public TimePeriodDAO(Connection connection) {
        this.connection = connection;
    }


    public void addTeacher(Teacher teacher) throws SQLException {
        String query = "INSERT INTO teachers (id, name, abbreviation, email, phone, hoursWork) VALUES (?, ?, ?, ?, ?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1,teacher.getId());
            preparedStatement.setString(2,teacher.getName());
            preparedStatement.setString(3,teacher.getAbbreviation());
            preparedStatement.setString(4,teacher.getEmail());
            preparedStatement.setString(5,teacher.getPhone());
            preparedStatement.setInt(6,teacher.getHoursWork());

            preparedStatement.executeUpdate();
        }

    }

    public List<Teacher> getAllTeachers() throws SQLException{
        List<Teacher> teachers = new ArrayList<>();
        String query = "SELECT * FROM teachers";
        try(Statement createStatement = connection.createStatement();
            ResultSet resultset = createStatement.executeQuery(query)) {

            while (resultset.next()) {
                int id = resultset.getInt("id");
                String name = resultset.getString("name");
                String abbreviation = resultset.getString("abbreviation");
                String email = resultset.getString("email");
                String phone = resultset.getString("phone");
                int hoursWork = resultset.getInt("hoursWork");

                Teacher teacher = new Teacher(id, name, abbreviation);
                teacher.setEmail(email);
                teacher.setPhone(phone);
                teacher.setHoursWork(hoursWork);

                teachers.add(teacher);
            }


        }
        return teachers;

    }

    public void deleteTeacher (Teacher teacher) throws SQLException{
        String query = "DELETE FROM teachers WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1,teacher.getId());
            preparedStatement.executeUpdate();
        }

    }
}
