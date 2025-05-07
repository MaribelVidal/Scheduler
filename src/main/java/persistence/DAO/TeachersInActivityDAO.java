package persistence.DAO;

import business.Teacher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TeachersInActivityDAO {
        private Connection connection;
        private TeacherDAO teacherDAO;

        public TeachersInActivityDAO(Connection connection, TeacherDAO teacherDAO) {
            this.connection = connection;
            this.teacherDAO = teacherDAO;
        }


        public void assignActivityTeachers(String activityId, String teacherId) throws SQLException {
            String query = "INSERT INTO teachersInActivity (activityId, teacherId) VALUES (?, ?)";
            try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1,activityId);
                preparedStatement.setString(2,teacherId);


                preparedStatement.executeUpdate();
            }

        }

        public List<Teacher> getAllActivityTeachers(String activityId) throws SQLException{
            List<Teacher> teachers = new ArrayList<>();
            String query = "SELECT * FROM teachersInActivity WHERE activityId = ? " ;

            try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, activityId);

                ResultSet resultset = preparedStatement.executeQuery();


                while (resultset.next()) {
                    String id = resultset.getString("teacherId");
                    Teacher teacher = teacherDAO.getOne(id);
                    teachers.add(teacher);

                }


            }
            return teachers;

        }



        public void deleteTeacherInActivity (String activityId, String teacherId) throws SQLException{
            String query = "DELETE FROM teachersInActivity WHERE activityId = ? AND teacherId = ?";
            try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, activityId );
                preparedStatement.setString(2, teacherId );
                preparedStatement.executeUpdate();
            }

        }




}
