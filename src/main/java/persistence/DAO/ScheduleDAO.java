package persistence.DAO;


import business.Lesson;
import business.Schedule;
import business.Subject;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO implements DAO<Schedule>{
    private Connection connection;
    private final ScheduleAssignedLessonsDAO scheduleAssignedLessonsDAO;

    public ScheduleDAO(Connection connection) {

        this.connection = connection;
        this.scheduleAssignedLessonsDAO = new ScheduleAssignedLessonsDAO(connection, new LessonDAO(connection));

    }

    @Override
    public void add(Schedule schedule) throws SQLException {
        String query = "INSERT INTO schedules (id, name, achievesConditions, weightedConditions) VALUES (?, ?, ?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, schedule.getId());
            preparedStatement.setString(2, schedule.getName());
            preparedStatement.setInt(3,schedule.getAchievedConditions());
            preparedStatement.setInt(4,schedule.getWeightedConditions());

            for(Lesson lesson : schedule.getLessons()){
                scheduleAssignedLessonsDAO.addAssignedLessons(schedule.getId(), lesson.getId());

            }
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void update(Schedule schedule) throws SQLException {
        String query = "UPDATE schedule SET name = ?, achievedConditions = ?, weightedConditions = ?,  WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(2, schedule.getName());
            preparedStatement.setInt(3,schedule.getAchievedConditions());
            preparedStatement.setInt(4,schedule.getWeightedConditions());
            preparedStatement.setString(1, schedule.getId());

            preparedStatement.executeUpdate();
        }
    }

    @Override
    public List<Schedule> getAll() throws SQLException{
        List<Schedule> schedules = new ArrayList<>();
        String query = "SELECT * FROM schedules";
        try(Statement createStatement = connection.createStatement();
            ResultSet resultset = createStatement.executeQuery(query)) {

            while (resultset.next()) {
                String id = resultset.getString("id");
                String name = resultset.getString("name");
                int achievedConditions = resultset.getInt("achievedConditions");
                int weightedConditions = resultset.getInt("weightedConditions");

                List<Lesson> lesson = scheduleAssignedLessonsDAO.getAllAssignedLessons(id);

                Schedule schedule = new Schedule();
                schedule.setId(id);
                schedule.setName(name);
                schedule.setAchievedConditions(achievedConditions);
                schedule.setWeightedConditions(weightedConditions);
                schedule.setLessons(lesson);


                schedules.add(schedule);
            }


        }
        return schedules;
    }

    @Override
    public Schedule getOne(String scheduleId) throws SQLException {
        //Subject subject = new Subject<>();
        String query = "SELECT * FROM schedules WHERE id = ? ";


        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, scheduleId);

            ResultSet resultset = preparedStatement.executeQuery();
            if (resultset.next()) {
                String id = resultset.getString("id");
                String name = resultset.getString("name");
                int achievedConditions = resultset.getInt("achievedConditions");
                int weightedConditions = resultset.getInt("weightedConditions");

                List<Lesson> lesson = scheduleAssignedLessonsDAO.getAllAssignedLessons(id);

                Schedule schedule = new Schedule();
                schedule.setId(id);
                schedule.setName(name);
                schedule.setAchievedConditions(achievedConditions);
                schedule.setWeightedConditions(weightedConditions);
                schedule.setLessons(lesson);

                return schedule;
            }else {
                return null;
            }
        }
    }


    @Override
    public void delete (Schedule schedule) throws SQLException{
        String query = "DELETE FROM schedules WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,schedule.getId());
            preparedStatement.executeUpdate();
        }

    }


}
