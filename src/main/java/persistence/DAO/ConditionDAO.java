package persistence.DAO;

import business.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConditionDAO implements DAO<Condition>{
    private Connection connection;


    public ConditionDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void add(Condition condition) throws SQLException {
        String query = "INSERT INTO conditions (id, teacher, weight, subject, timePeriod, studentGroup) VALUES (?, ?, ?, ?, ?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, condition.getId());
            preparedStatement.setString(2,condition.getTeacher().getId());
            preparedStatement.setInt(3,condition.getWeight());
            preparedStatement.setString(4,condition.getSubject().getId());
            preparedStatement.setString(5,condition.getTimePeriod().getId());
            preparedStatement.setString(6,condition.getStudentGroup().getId());


            preparedStatement.executeUpdate();
        }

    }

    @Override
    public void update(Condition condition) throws SQLException {

        String query = "UPDATE conditions SET  teacher = ?, weight = ?, subject = ?, timePeriod = ?, studentGroup = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, condition.getTeacher().getId());
            preparedStatement.setInt(2, condition.getWeight());
            preparedStatement.setString(3, condition.getSubject().getId());
            preparedStatement.setString(4, condition.getTimePeriod().getId());
            preparedStatement.setString(5, condition.getStudentGroup().getId());
            preparedStatement.setString(6, condition.getId());

            preparedStatement.executeUpdate();
        }

    }

    @Override
    public List<Condition> getAll() throws SQLException{
        List<Condition> conditions = new ArrayList<>();
        String query = "SELECT * FROM conditions";
        try(Statement createStatement = connection.createStatement();
            ResultSet resultset = createStatement.executeQuery(query)) {

            while (resultset.next()) {
                String conditionId = resultset.getString("id");
                String teacherId = resultset.getString("teacher");
                int weight = resultset.getInt("weight");
                String subjectId = resultset.getString("subject");
                String timePeriodId = resultset.getString("timePeriod");
                String studentGroupId = resultset.getString("studentGroup");

                Teacher teacher = new TeacherDAO(connection).getOne(teacherId);
                Condition condition = null;
                if (!Objects.equals(subjectId, "dummy")){
                    Subject subject = new SubjectDAO(connection).getOne(subjectId);
                    condition = new Condition(teacher, weight, subject);
                    condition.setId(conditionId);
                }
                else if (!Objects.equals(timePeriodId, "dummy")){
                    TimePeriod timePeriod = new TimePeriodDAO(connection).getOne(timePeriodId);
                    condition = new Condition(teacher, weight, timePeriod);
                    condition.setId(conditionId);
                }
                else if (!Objects.equals(studentGroupId, "dummy")){
                    StudentGroup studentGroup = new StudentGroupDAO(connection).getOne(studentGroupId);
                    condition = new Condition(teacher, weight, studentGroup);
                    condition.setId(conditionId);

                }


                conditions.add(condition);
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return conditions;

    }

    @Override
    public Condition getOne(String id) throws SQLException {
        String query = "";

        query = "SELECT * FROM conditions WHERE id = ? ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, id);

            ResultSet resultset = preparedStatement.executeQuery();

            if (resultset.next()) {
                String teacherId = resultset.getString("teacher");
                int weight = resultset.getInt("weight");
                String subjectId = resultset.getString("subject");
                String timePeriodId = resultset.getString("timePeriod");
                String studentGroupId = resultset.getString("studentGroup");

                Teacher teacher = new TeacherDAO(connection).getOne(teacherId);
                Condition condition = null;
                if (!Objects.equals(subjectId, "dummy")){
                    Subject subject = new SubjectDAO(connection).getOne(subjectId);
                    condition = new Condition(teacher, weight, subject);
                    condition.setId(id);
                }
                else if (!Objects.equals(timePeriodId, "dummy")){
                    TimePeriod timePeriod = new TimePeriodDAO(connection).getOne(timePeriodId);
                    condition = new Condition(teacher, weight, timePeriod);
                    condition.setId(id);
                }
                else if (!Objects.equals(studentGroupId, "dummy")){
                    StudentGroup studentGroup = new StudentGroupDAO(connection).getOne(studentGroupId);
                    condition = new Condition(teacher, weight, studentGroup);
                    condition.setId(id);
                }

                return condition;
            }else {
                return null;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void delete (Condition condition) throws SQLException{
            String query = "DELETE FROM conditions WHERE id = ? ";
            try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1,condition.getId());

                preparedStatement.executeUpdate();
            }
    }

}
