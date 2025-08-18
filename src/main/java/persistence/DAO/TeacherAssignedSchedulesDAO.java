package persistence.DAO;

import business.Schedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TeacherAssignedSchedulesDAO {

    private Connection connection;
    private ScheduleDAO scheduleDAO;

    public TeacherAssignedSchedulesDAO(Connection connection, ScheduleDAO scheduleDAO) {
        this.connection = connection;
        this.scheduleDAO = scheduleDAO;
    }


    public void TeacherAssignedSchedules(String teacherId, String scheduleId) throws SQLException {
        String query = "INSERT INTO teacherAssignedSchedules (teacherId, scheduleId) VALUES (?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,teacherId);
            preparedStatement.setString(2,scheduleId);

            preparedStatement.executeUpdate();
        }

    }

    public List<Schedule> getAllAssignedSchedules(String teacherId) throws SQLException{
        List<Schedule> schedules = new ArrayList<>();
        String query = "SELECT * FROM teacherAssignedSchedules WHERE teacherId = ? " ;
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, teacherId);

            ResultSet resultset = preparedStatement.executeQuery();

            while (resultset.next()) {
                String id = resultset.getString("scheduleId");
                Schedule schedule = scheduleDAO.getOne(id);
                schedules.add(schedule);
            }
        }
        return schedules;

    }


    public void deleteAssignedSchedule (String teacherId, String scheduleId) throws SQLException{
        String query = "DELETE FROM teacherAssignedSchedules WHERE teacherId = ? AND scheduleId = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, teacherId );
            preparedStatement.setString(2, scheduleId );
            preparedStatement.executeUpdate();
        }
    }
}

