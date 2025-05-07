package persistence.DAO;

import business.StudentGroup;
import business.Teacher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentGroupDAO implements DAO<StudentGroup>{

    private Connection connection;
    private TeacherDAO teacherDAO;

    public StudentGroupDAO(Connection connection) {

        this.connection = connection;
        this.teacherDAO = new TeacherDAO(connection);
    }

    @Override
    public void add (StudentGroup studentGroup) throws SQLException {
        String query = "INSERT INTO studentGroups (id, name, abbreviation, course, assignedTutor, weeklyGroupHours, numberOfStudents) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,studentGroup.getId());
            preparedStatement.setString(2,studentGroup.getName());
            preparedStatement.setString(3,studentGroup.getAbbreviation());
            preparedStatement.setString(4, studentGroup.getCourse());
            preparedStatement.setString(5,studentGroup.getAssignedTutor().getId());
            preparedStatement.setInt(6,studentGroup.getWeeklyGroupHours());
            preparedStatement.setInt(7,studentGroup.getNumberOfStudents());

            preparedStatement.executeUpdate();
        }

    }

    @Override
    public void update(StudentGroup studentGroup) throws SQLException {
        String query = "UPDATE studentGroups SET name = ?, abbreviation = ?, course = ?, assignedTutor = ?, weeklyGroupHours = ?, numberOfStudents = ? WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, studentGroup.getName());
            preparedStatement.setString(2, studentGroup.getAbbreviation());
            preparedStatement.setString(3, studentGroup.getCourse());
            preparedStatement.setString(4, studentGroup.getAssignedTutor().getId());
            preparedStatement.setInt(5, studentGroup.getWeeklyGroupHours());
            preparedStatement.setInt(6, studentGroup.getNumberOfStudents());
            preparedStatement.setString(7, studentGroup.getId());

            preparedStatement.executeUpdate();
        }
    }

    @Override
    public List<StudentGroup> getAll() throws SQLException{
        List<StudentGroup> studentGroups = new ArrayList<>();
        String query = "SELECT * FROM studentGroups";
        try(Statement createStatement = connection.createStatement();
            ResultSet resultset = createStatement.executeQuery(query)) {

            while (resultset.next()) {
                String id = resultset.getString("id");
                String name = resultset.getString("name");
                String abbreviation = resultset.getString("abbreviation");
                String course = resultset.getString("course");
                String assignedTutor = resultset.getString("assignedTutor");
                int weeklyGroupHours = resultset.getInt("weeklyGroupHours");
                int numberOfStudents = resultset.getInt("numberOfStudents");

                StudentGroup studentGroup = new StudentGroup(id, name, abbreviation);
                studentGroup.setCourse(course);
                Teacher teacher = teacherDAO.getOne(assignedTutor);
                studentGroup.setAssignedTutor(teacher);
                studentGroup.setWeeklyGroupHours(weeklyGroupHours);
                studentGroup.setNumberOfStudents(numberOfStudents);

                studentGroups.add(studentGroup);
            }


        }
        return studentGroups;

    }

    @Override
    public void delete (StudentGroup studentGroup) throws SQLException{
        String query = "DELETE FROM studentGroups WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,studentGroup.getId());
            preparedStatement.executeUpdate();
        }

    }

    @Override
    public StudentGroup getOne(String studentGroupId) throws Exception {
        String query = "SELECT * FROM studentGroups WHERE id = ? ";


        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, studentGroupId);

            ResultSet resultset = preparedStatement.executeQuery();
            if (resultset.next()) {
                String id = resultset.getString("id");
                String name = resultset.getString("name");
                String abbreviation = resultset.getString("abbreviation");
                String course = resultset.getString("course");
                String assignedTutor = resultset.getString("assignedTutor");
                int weeklyGroupHours = resultset.getInt("weeklyGroupHours");
                int numberOfStudents = resultset.getInt("numberOfStudents");

                StudentGroup studentGroup = new StudentGroup(id, name, abbreviation);
                studentGroup.setCourse(course);
                Teacher teacher = teacherDAO.getOne(assignedTutor);
                studentGroup.setAssignedTutor(teacher);
                studentGroup.setWeeklyGroupHours(weeklyGroupHours);
                studentGroup.setNumberOfStudents(numberOfStudents);

                return studentGroup;
            }else{
                return null;
            }
}

        }


}
