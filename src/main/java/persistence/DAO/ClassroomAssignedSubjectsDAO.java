package persistence.DAO;

import business.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClassroomAssignedSubjectsDAO {

        private Connection connection;
        private SubjectDAO subjectDAO;

        public ClassroomAssignedSubjectsDAO(Connection connection, SubjectDAO subjectDAO) {
            this.connection = connection;
            this.subjectDAO = subjectDAO;
        }


        public void addAssignedSubjects(String classroomId, String subjectId) throws SQLException {
            String query = "INSERT INTO classroomAssignedSubjects (classroomId, subjectId) VALUES (?, ?)";
            try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1,classroomId);
                preparedStatement.setString(2,subjectId);


                preparedStatement.executeUpdate();
            }

        }

        public void updateAssignedSubjects (String classroomId, String subjectId) throws SQLException {
            String query = "UPDATE classroomAssignedSubjects SET subjectId = ? WHERE classroomId = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, subjectId);
                preparedStatement.setString(2, classroomId);
                preparedStatement.executeUpdate();
            }
        }

        public List<Subject> getAllAssignedSubjects(String classroomId) throws SQLException{
            List<Subject> subjects = new ArrayList<>();
            String query = "SELECT * FROM classroomAssignedSubjects WHERE classroomId = ? " ;

            try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, classroomId);

                ResultSet resultset = preparedStatement.executeQuery();


                while (resultset.next()) {
                    String id = resultset.getString("subjectId");
                    Subject subject = subjectDAO.getOne(id);
                    subjects.add(subject);

                }


            }
            return subjects;

        }



        public void deleteAssignedSubjects (String classroomId, String subjectId) throws SQLException{
            String query = "DELETE FROM classroomAssignedSubjects WHERE classroomId = ? AND subjectId = ?";
            try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, classroomId );
                preparedStatement.setString(2, subjectId );
                preparedStatement.executeUpdate();
            }

        }

    public void deleteClassroom (String classroomId) throws SQLException {
        String query = "DELETE FROM classroomAssignedSubjects WHERE classroomId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, classroomId);
            preparedStatement.executeUpdate();
        }

    }


    }


