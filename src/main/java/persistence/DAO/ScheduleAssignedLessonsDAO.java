package persistence.DAO;


import business.Lesson;
import business.Schedule;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ScheduleAssignedLessonsDAO {

    private Connection connection;
    private LessonDAO lessonDAO;

    public ScheduleAssignedLessonsDAO(Connection connection, LessonDAO lessonDAO) {
        this.connection = connection;
        this.lessonDAO = lessonDAO;
    }


    public void addAssignedLessons(String scheduleId, String lessonId) throws SQLException {
        String query = "INSERT INTO scheduleAssignedLessons (scheduleId, lessonId) VALUES (?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,scheduleId);
            preparedStatement.setString(2,lessonId);

            preparedStatement.executeUpdate();
        }

    }

    public void updateAssignedLessons (String scheduleId, String lessonId) throws SQLException {
        String query = "UPDATE scheduleAssignedLessons SET lessonId = ? WHERE scheduleId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, lessonId);
            preparedStatement.setString(2, scheduleId);
            preparedStatement.executeUpdate();
        }
    }

    public List<Lesson> getAllAssignedLessons(String scheduleId) throws SQLException{
        List<Lesson> lessons = new ArrayList<>();
        String query = "SELECT * FROM scheduleAssignedSLessons WHERE scheduleId = ? " ;

        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, scheduleId);

            ResultSet resultset = preparedStatement.executeQuery();


            while (resultset.next()) {
                String id = resultset.getString("lessonId");
                Lesson lesson = lessonDAO.getOne(id);
                lessons.add(lesson);

            }


        }
        return lessons;

    }



    public void deleteAssignedLessons (String scheduleId, String lessonId) throws SQLException{
        String query = "DELETE FROM scheduleAssignedLessons WHERE scheduleId = ? AND lessonId = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, scheduleId );
            preparedStatement.setString(2, lessonId );
            preparedStatement.executeUpdate();
        }

    }

    public void deleteSchedule (String scheduleId) throws SQLException {
        String query = "DELETE FROM scheduleAssignedLessons WHERE scheduleId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, scheduleId);
            preparedStatement.executeUpdate();
        }

    }


}