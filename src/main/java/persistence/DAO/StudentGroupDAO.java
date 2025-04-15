package persistence.DAO;

import business.StudentGroup;
import business.Teacher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentGroupDAO {

    private Connection connection;


    public StudentGroupDAO(Connection connection) {
        this.connection = connection;
    }


    public void addStudentGroup(StudentGroup studentGroup) throws SQLException {
        String query = "INSERT INTO studentGroups (id, name, abbreviation, course, assignedTutor, weeklyGroupHours, numberOfStudents) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1,studentGroup.getId());
            preparedStatement.setString(2,studentGroup.getName());
            preparedStatement.setString(3,studentGroup.getAbbreviation());
            preparedStatement.setString(4, studentGroup.getCourse());
            preparedStatement.setTeacher(5,studentGroup.getAssignedTutor());
            preparedStatement.setInt(6,studentGroup.getWeeklyGroupHours());
            preparedStatement.setInt(6,studentGroup.getNumberOfStudents());


            preparedStatement.executeUpdate();
        }

    }

    public List<StudentGroup> getAllStudentGroups() throws SQLException{
        List<StudentGroup> studentGroups = new ArrayList<>();
        String query = "SELECT * FROM studentGroups";
        try(Statement createStatement = connection.createStatement();
            ResultSet resultset = createStatement.executeQuery(query)) {

            while (resultset.next()) {
                int id = resultset.getInt("id");
                String name = resultset.getString("name");
                String abbreviation = resultset.getString("abbreviation");
                String course = resultset.getString("course");
                Teacher assignedTutor = resultset.getString("assignedTutor");
                int weeklyGroupHours = resultset.getInt("weeklyGroupHours");
                int numberOfStudents = resultset.getInt("numberOfStudents");

                StudentGroup studentGroup = new StudentGroup(id, name, abbreviation);
                studentGroup.setCourse(course);
                studentGroup.setAssignedTutor(assignedTutor);
                studentGroup.setWeeklyGroupHours(weeklyGroupHours);
                studentGroup.setNumberOfStudents(numberOfStudents);

                studentGroups.add(studentGroup);
            }


        }
        return studentGroups;

    }

    public void deleteStudentGroup (StudentGroup studentGroup) throws SQLException{
        String query = "DELETE FROM studentGroups WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1,studentGroup.getId());
            preparedStatement.executeUpdate();
        }

    }
}
