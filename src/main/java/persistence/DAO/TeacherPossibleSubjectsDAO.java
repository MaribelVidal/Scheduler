package persistence.DAO;

// Clase para manejar tipos de datos necesarios en los DAO, ejemplo Lista de subjects en Teacher

import business.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherPossibleSubjectsDAO {
    private Connection connection;
    private SubjectDAO subjectDAO;

    public TeacherPossibleSubjectsDAO(Connection connection, SubjectDAO subjectDAO) {
        this.connection = connection;
        this.subjectDAO = subjectDAO;
    }


    public void addPossibleSubjects(String teacherId, String subjectId) throws SQLException {
        String sql = "INSERT INTO teacherPossibleSubjects(teacherId, subjectId) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE subjectId = subjectId";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, teacherId);
            ps.setString(2, subjectId);
            ps.executeUpdate();
        }
    }

    public void deletePossibleSubjects(String teacherId, String subjectId) throws SQLException {
        String sql = "DELETE FROM teacherPossibleSubjects WHERE teacherId = ? AND subjectId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, teacherId);
            ps.setString(2, subjectId);
            ps.executeUpdate();
        }
    }


    public void updatePossibleSubjects(String teacherId, String subjectId) throws SQLException {
        String query = "UPDATE teacherPossibleSubjects SET subjectId = ? WHERE teacherId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, subjectId);
            preparedStatement.setString(2, teacherId);
            preparedStatement.executeUpdate();
        }
    }

    public List<Subject> getAllPossibleSubjects(String teacherId) throws SQLException{
        List<Subject> subjects = new ArrayList<>();
        String query = "SELECT * FROM teacherPossibleSubjects WHERE teacherId = ? " ;

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


    public void deleteTeacher (String teacherId) throws SQLException {
        String query = "DELETE FROM teacherPossibleSubjects WHERE teacherId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, teacherId);
            preparedStatement.executeUpdate();
        }
    }



}
