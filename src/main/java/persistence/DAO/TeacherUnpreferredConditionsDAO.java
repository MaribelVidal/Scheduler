package persistence.DAO;


import business.Condition;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TeacherUnpreferredConditionsDAO {

    private Connection connection;
    private ConditionDAO conditionDAO;

    public TeacherUnpreferredConditionsDAO(Connection connection, ConditionDAO conditionDAO) {
        this.connection = connection;
        this.conditionDAO = conditionDAO;
    }


    public void addUnpreferredConditions(String teacherId, String conditionId) throws SQLException {
        String query = "INSERT INTO teacherUnpreferredConditions (teacherId, conditionId) VALUES (?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,teacherId);
            preparedStatement.setString(2,conditionId);

            preparedStatement.executeUpdate();
        }

    }



    public void updateUnpreferredConditions(String teacherId, String conditionId) throws SQLException {
        String query = "UPDATE teacherUnpreferredConditions SET conditionId = ? WHERE teacherId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, conditionId);
            preparedStatement.setString(2, teacherId);
            preparedStatement.executeUpdate();
        }
    }

    public List<Condition> getAllUnpreferredConditions(String teacherId) throws Exception {
        List<Condition> conditions = new ArrayList<>();
        String query = "SELECT * FROM teacherUnpreferredConditions WHERE teacherId = ? " ;
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, teacherId);

            ResultSet resultset = preparedStatement.executeQuery();

            while (resultset.next()) {
                String id = resultset.getString("conditionId");
                Condition condition = conditionDAO.getOne(id, true);
                conditions.add(condition);
            }
        }
        return conditions;

    }


    public void deleteUnpreferredConditions (String teacherId, String conditionId) throws SQLException{
        String query = "DELETE FROM teacherUnpreferredConditions WHERE teacherId = ? AND subjectId = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, teacherId );
            preparedStatement.setString(2, conditionId );
            preparedStatement.executeUpdate();
        }
    }

    public void deleteTeacher (String teacherId) throws Exception {
        String query = "DELETE FROM teacherUnpreferredConditions WHERE teacherId = ?";
        List<Condition> conditions = getAllUnpreferredConditions(teacherId);
        for(Condition condition : conditions){
            conditionDAO.delete(condition);
        }
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, teacherId);
            preparedStatement.executeUpdate();
        }
    }
}



