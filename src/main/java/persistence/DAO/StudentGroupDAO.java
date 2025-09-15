package persistence.DAO;

import business.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentGroupDAO implements DAO<StudentGroup> {

    private Connection connection;
    private TeacherDAO teacherDAO;
    private final StudentGroupRequiredSubjectsDAO studentGroupRequiredSubjectsDAO;
    private final StudentGroupAssignedSchedulesDAO studentGroupAssignedSchedulesDAO;

    public StudentGroupDAO(Connection connection) {
        this.connection = connection;
        this.teacherDAO = new TeacherDAO(connection);
        this.studentGroupRequiredSubjectsDAO = new StudentGroupRequiredSubjectsDAO(connection, new SubjectDAO(connection));
        this.studentGroupAssignedSchedulesDAO = new StudentGroupAssignedSchedulesDAO(connection, new ScheduleDAO(connection));
    }

    @Override
    public void add(StudentGroup studentGroup) throws SQLException {
        String query = "INSERT INTO studentGroups (id, name, abbreviation, course, weeklyGroupHours, numberOfStudents) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, studentGroup.getId());
            preparedStatement.setString(2, studentGroup.getName());
            preparedStatement.setString(3, studentGroup.getAbbreviation());
            preparedStatement.setString(4, studentGroup.getCourse());
            preparedStatement.setInt(5, studentGroup.getWeeklyGroupHours());
            preparedStatement.setInt(6, studentGroup.getNumberOfStudents());
            preparedStatement.executeUpdate();
            for (Subject subject : studentGroup.getRequiredSubjects()) {
                studentGroupRequiredSubjectsDAO.addRequiredSubjects(studentGroup.getId(), subject.getId());

            }

            for (Schedule schedule : studentGroup.getSchedules()) {
                studentGroupAssignedSchedulesDAO.addAssignedSchedules(studentGroup.getId(), schedule.getId());


            }


        }

    }



    @Override
    public void update(StudentGroup g) throws SQLException {
        String sql = "UPDATE studentGroups SET name=?, abbreviation=?, course=?, weeklyGroupHours=?, numberOfStudents=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, g.getName());
            ps.setString(2, g.getAbbreviation());
            ps.setString(3, g.getCourse());
            ps.setInt(4, g.getWeeklyGroupHours());
            ps.setInt(5, g.getNumberOfStudents());
            ps.setString(6, g.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public List<StudentGroup> getAll() throws SQLException {
        List<StudentGroup> studentGroups = new ArrayList<>();
        String query = "SELECT * FROM studentGroups";
        try (Statement createStatement = connection.createStatement();
             ResultSet resultset = createStatement.executeQuery(query)) {

            while (resultset.next()) {
                String id = resultset.getString("id");
                String name = resultset.getString("name");
                String abbreviation = resultset.getString("abbreviation");
                String course = resultset.getString("course");
                int weeklyGroupHours = resultset.getInt("weeklyGroupHours");
                int numberOfStudents = resultset.getInt("numberOfStudents");

                StudentGroup studentGroup = new StudentGroup(id, name, abbreviation);
                studentGroup.setCourse(course);
                studentGroup.setWeeklyGroupHours(weeklyGroupHours);
                studentGroup.setNumberOfStudents(numberOfStudents);

                studentGroups.add(studentGroup);
            }


        }
        return studentGroups;

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
                int weeklyGroupHours = resultset.getInt("weeklyGroupHours");
                int numberOfStudents = resultset.getInt("numberOfStudents");

                StudentGroup studentGroup = new StudentGroup(id, name, abbreviation);
                studentGroup.setCourse(course);
                studentGroup.setWeeklyGroupHours(weeklyGroupHours);
                studentGroup.setNumberOfStudents(numberOfStudents);

                return studentGroup;
            } else {
                return null;
            }
        }

    }

    @Override
    public void delete (StudentGroup studentGroup) throws SQLException {
        String query = "DELETE FROM studentGroups WHERE id = ?";
        studentGroupRequiredSubjectsDAO.deleteStudentGroup(studentGroup.getId());
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,studentGroup.getId());
            preparedStatement.executeUpdate();
        }

    }

    public StudentGroup getOneLight(String id) throws SQLException {
        String sql = "SELECT id, name, abbreviation, course, weeklyGroupHours, numberOfStudents FROM studentGroups WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                StudentGroup g = new StudentGroup(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("abbreviation")
                );
                g.setWeeklyGroupHours(rs.getInt("weeklyGroupHours"));
                g.setNumberOfStudents(rs.getInt("numberOfStudents"));
                // DO NOT load requiredSubjects or schedules here
                return g;
            }
        }
    }


}




