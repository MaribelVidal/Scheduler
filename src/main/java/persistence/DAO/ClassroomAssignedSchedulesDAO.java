package persistence.DAO;

import business.Schedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClassroomAssignedSchedulesDAO {

    private Connection connection;
    private ScheduleDAO scheduleDAO;

    public ClassroomAssignedSchedulesDAO(Connection connection, ScheduleDAO scheduleDAO) {
        this.connection = connection;
        this.scheduleDAO = scheduleDAO;
    }


    public void classroomAssignedSchedules(String classroomId, String scheduleId) throws SQLException {
        String query = "INSERT INTO classroomAssignedSchedules (classroomId, scheduleId) VALUES (?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,classroomId);
            preparedStatement.setString(2,scheduleId);


            preparedStatement.executeUpdate();
        }

    }

    public List<Schedule> getAllAssignedSchedules(String classroomId) throws SQLException{
        List<Schedule> schedules = new ArrayList<>();
        String query = "SELECT * FROM classroomAssignedSchedules WHERE classroomId = ? " ;

        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, classroomId);

            ResultSet resultset = preparedStatement.executeQuery();


            while (resultset.next()) {
                String id = resultset.getString("scheduleId");
                Schedule schedule = scheduleDAO.getOne(id);
                schedules.add(schedule);

            }


        }
        return schedules;

    }



    public void deleteAssignedSchedule (String classroomId, String scheduleId) throws SQLException{
        String query = "DELETE FROM classroomAssignedSchedules WHERE classroomId = ? AND scheduleId = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, classroomId );
            preparedStatement.setString(2, scheduleId );
            preparedStatement.executeUpdate();
        }

    }


}
