package persistence.DAO;

// Clase para manejar tipos de datos necesarios en los DAO, ejemplo Lista de subjects en Teacher

import business.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PossibleTeacherSubjectsDAO {
    private Connection connection;
    private SubjectDAO subjectDAO;

    public PossibleTeacherSubjectsDAO(Connection connection, SubjectDAO subjectDAO) {
        this.connection = connection;
        this.subjectDAO = subjectDAO;
    }


    public void assignTeacherSubjects(String teacherId, String subjectId) throws SQLException {
        String query = "INSERT INTO possibleSubjects (teacherId, subjectId) VALUES (?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,teacherId);
            preparedStatement.setString(2,subjectId);


            preparedStatement.executeUpdate();
        }

    }

    public List<Subject> getAllTeacherSubjects(String teacherId) throws SQLException{
        List<Subject> subjects = new ArrayList<>();
        String query = "SELECT * FROM possibleSubjects WHERE teacherId = ? " ;

        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, teacherId);

            ResultSet resultset = preparedStatement.executeQuery();


            while (resultset.next()) {
                String id = resultset.getString("subjectId");
                Subject subject = subjectDAO.getOne(id);
                subjects.add(subject);

            }


        }
        return subjects;

    }



    public void deleteTeacherSubject (String teacherId, String subjectId) throws SQLException{
        String query = "DELETE FROM possibleSubjects WHERE teacherId = ? AND subjectId = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, teacherId );
            preparedStatement.setString(2, subjectId );
            preparedStatement.executeUpdate();
        }

    }


}
