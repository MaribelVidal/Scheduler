package persistence.DAO;


import business.Schedule;
import business.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentGroupAssignedSchedulesDAO {

    private Connection connection;
    private SubjectDAO scheduleDAO;

    public StudentGroupAssignedSchedulesDAO(Connection connection, ScheduleDAO scheduleDAO) {
        this.connection = connection;
        this.scheduleDAO = scheduleDAO;
    }


    public void studentGroupAssignedSchedules(String studentGroupId, String scheduleId) throws SQLException {
        String query = "INSERT INTO studentGroupAssignedSchedules (studentGroupId, scheduleId) VALUES (?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,studentGroupId);
            preparedStatement.setString(2,scheduleId);

            preparedStatement.executeUpdate();
        }

    }

    public List<Subject> getAllAssignedSchedules(String studentGroupId) throws SQLException{
        List<Subject> schedules = new ArrayList<>();
        String query = "SELECT * FROM studentGroupAssignedSchedules WHERE studentGroupId = ? " ;

        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, studentGroupId);

            ResultSet resultset = preparedStatement.executeQuery();


            while (resultset.next()) {
                String id = resultset.getString("scheduleId");
                Schedule schedule = scheduleDAO.getOne(id);
                schedules.add(schedule);

            }


        }
        return schedules;

    }



    public void deleteAssignedSchedule (String studentGroupId, String scheduleId) throws SQLException{
        String query = "DELETE FROM studentGroupAssignedSchedules WHERE studentGroupId = ? AND scheduleId = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, studentGroupId );
            preparedStatement.setString(2, scheduleId );
            preparedStatement.executeUpdate();
        }

    }


}




