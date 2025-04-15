package persistence.DAO;

// Clase para manejar tipos de datos necesarios en los DAO, ejemplo Lista de subjects en Teacher

import business.Classroom;
import business.Department;
import business.Subject;
import business.Teacher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HybridTableManager {
    private Connection connection;
    private SubjectDAO subjectDAO;

    public HybridTableManager(Connection connection, SubjectDAO subjectDAO) {
        this.connection = connection;
        this.subjectDAO = subjectDAO;
    }


    public void assignTeacherSubjects(int teacherId, int subjectId) throws SQLException {
        String query = "INSERT INTO teacherSubjects (teacherId, subjectId) VALUES (?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1,teacherId);
            preparedStatement.setInt(2,subjectId);


            preparedStatement.executeUpdate();
        }

    }

    public List<Subject> getAllTeacherSubjects(int Id) throws SQLException{
        List<Subject> subjects = new ArrayList<>();
        String query = "SELECT s.* FROM teacherSubjects WHERE teacherId = ? " ;

        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, Id);

            ResultSet resultset = preparedStatement.executeQuery(query);


            while (resultset.next()) {
                int id = resultset.getInt("id");
                Subject subject = subjectDAO.getOneSubjects(id);
                subjects.add(subject);

            }


        }
        return subjects;

    }



    public void deleteTeacherSubject (int teacherId, int subjectId) throws SQLException{
        String query = "DELETE FROM teacherSubject WHERE teacherId = ? AND subjectId = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, teacherId );
            preparedStatement.setInt(2, subjectId );
            preparedStatement.executeUpdate();
        }

    }
}
