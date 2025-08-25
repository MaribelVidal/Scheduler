package persistence.DAO;

// Clase para manejar tipos de datos necesarios en los DAO, ejemplo Lista de subjects en Teacher

import business.Subject;
import business.TimePeriod;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherUnavailableTimePeriodsDAO {
    private Connection connection;
    private TimePeriodDAO timePeriodDAO;

    public TeacherUnavailableTimePeriodsDAO(Connection connection, TimePeriodDAO timePeriodDAO) {
        this.connection = connection;
        this.timePeriodDAO = timePeriodDAO;
    }


    public void addUnavailableTimePeriods(String teacherId, String timePeriodId) throws SQLException {
        String query = "INSERT INTO teacherUnavailableTimePeriods (teacherId, timePeriodId) VALUES (?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,teacherId);
            preparedStatement.setString(2,timePeriodId);


            preparedStatement.executeUpdate();
        }

    }

    public void updateUnavailableTimePeriods(String teacherId, String timePeriodId) throws SQLException {
        String query = "UPDATE teacherUnavailableTimePeriods SET timePeriodId = ? WHERE teacherId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, timePeriodId);
            preparedStatement.setString(2, teacherId);
            preparedStatement.executeUpdate();
        }
    }

    public List<TimePeriod> getAllUnavailableTimePeriods(String teacherId) throws SQLException{
        List<TimePeriod> timePeriods = new ArrayList<>();
        String query = "SELECT * FROM teacherUnavailableTimePeriods WHERE teacherId = ? " ;

        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, teacherId);

            ResultSet resultset = preparedStatement.executeQuery();


            while (resultset.next()) {
                String id = resultset.getString("timePeriodId");
                TimePeriod timePeriod = timePeriodDAO.getOne(id);
                timePeriods.add(timePeriod);

            }
        }
        return timePeriods;

    }



    public void deleteUnavailableTimePeriods (String teacherId, String timePeriodId) throws SQLException{
        String query = "DELETE FROM teacherUnavailableTimePeriods WHERE teacherId = ? AND timePeriodId = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, teacherId );
            preparedStatement.setString(2, timePeriodId );
            preparedStatement.executeUpdate();
        }

    }

    public void deleteTeacher (String teacherId) throws SQLException {
        String query = "DELETE FROM teacherUnavailableTimePeriods WHERE teacherId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, teacherId);
            preparedStatement.executeUpdate();
        }
    }



}
