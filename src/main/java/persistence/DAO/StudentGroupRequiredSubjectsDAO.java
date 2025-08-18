package persistence.DAO;


import business.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentGroupRequiredSubjectsDAO {

    private Connection connection;
    private SubjectDAO subjectDAO;

    public StudentGroupRequiredSubjectsDAO(Connection connection, SubjectDAO subjectDAO) {
        this.connection = connection;
        this.subjectDAO = subjectDAO;
    }


    public void studentGroupRequiredSubjects(String studentGroupId, String subjectId) throws SQLException {
        String query = "INSERT INTO studentGroupRequiredSubjects (studentGroupId, subjectId) VALUES (?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,studentGroupId);
            preparedStatement.setString(2,subjectId);

            preparedStatement.executeUpdate();
        }

    }

    public List<Subject> getAllRequiredSubjects(String studentGroupId) throws SQLException{
        List<Subject> subjects = new ArrayList<>();
        String query = "SELECT * FROM studentGroupRequiredSubjects WHERE studentGroupId = ? " ;

        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, studentGroupId);

            ResultSet resultset = preparedStatement.executeQuery();


            while (resultset.next()) {
                String id = resultset.getString("subjectId");
                Subject subject = subjectDAO.getOne(id);
                subjects.add(subject);

            }


        }
        return subjects;

    }



    public void deleteRequiredSubject (String studentGroupId, String subjectId) throws SQLException{
        String query = "DELETE FROM studentGroupRequiredSubjects WHERE studentGroupId = ? AND subjectId = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, studentGroupId );
            preparedStatement.setString(2, subjectId );
            preparedStatement.executeUpdate();
        }

    }


}



