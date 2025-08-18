package persistence.DAO;


import business.Condition;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TeacherPreferredConditionsDAO {

    private Connection connection;
    private ConditionDAO conditionDAO;

    public TeacherPreferredConditionsDAO(Connection connection, ConditionDAO conditionDAO) {
        this.connection = connection;
        this.conditionDAO = conditionDAO;
    }


    public void assignPreferredConditions(String teacherId, String conditionId) throws SQLException {
        String query = "INSERT INTO teacherPreferredConditions (teacherId, conditionId) VALUES (?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,teacherId);
            preparedStatement.setString(2,conditionId);

            preparedStatement.executeUpdate();
        }

    }

    public List<Condition> getAllPreferredConditions(String teacherId) throws Exception {
        List<Condition> conditions = new ArrayList<>();
        String query = "SELECT * FROM teacherPreferredConditions WHERE teacherId = ? " ;
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, teacherId);

            ResultSet resultset = preparedStatement.executeQuery();

            while (resultset.next()) {
                String id = resultset.getString("conditionId");
                Condition condition = conditionDAO.getOne(id);
                conditions.add(condition);
            }
        }
        return conditions;

    }


    public void deletePreferredConditions (String teacherId, String conditionId) throws SQLException{
        String query = "DELETE FROM teacherPreferredConditions WHERE teacherId = ? AND subjectId = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, teacherId );
            preparedStatement.setString(2, conditionId );
            preparedStatement.executeUpdate();
        }
    }
}


