package persistence.DAO;

import business.Classroom;
import business.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubjectDAO implements DAO<Subject> {

    private Connection connection;
    private ClassroomDAO classroomDAO;

    public SubjectDAO(Connection connection) {
        this.connection = connection;
        this.classroomDAO = new ClassroomDAO(connection);
    }

    @Override
    public void add(Subject subject) throws SQLException {
        //String query = "INSERT INTO subjects (id, name, abbreviation, department, course, weeklyAssignedHours, assignedClassroom) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String query = "INSERT INTO subjects (id, name, abbreviation, department, course, weeklyAssignedHours, assignedClassroom) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,subject.getId());
            preparedStatement.setString(2,subject.getName());
            preparedStatement.setString(3,subject.getAbbreviation());
            preparedStatement.setString(4,subject.getDepartment());
            preparedStatement.setString(5, subject.getCourse());
            preparedStatement.setInt(6,subject.getWeeklyAssignedHours());

            if(subject.getAssignedClassroom() != null) {
                preparedStatement.setString(7, subject.getAssignedClassroom().getId());
            } else {
                preparedStatement.setNull(7, Types.VARCHAR);
            }

            preparedStatement.executeUpdate();
        }

    }

    @Override
    public void update(Subject subject) throws SQLException {
        String query = "UPDATE subjects SET name = ?, abbreviation = ?, department = ?, course = ?, weeklyAssignedHours = ?, assignedClassroom = ? WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, subject.getName());
            preparedStatement.setString(2, subject.getAbbreviation());
            preparedStatement.setString(3,subject.getDepartment());
            preparedStatement.setString(4, subject.getCourse());
            preparedStatement.setInt(5, subject.getWeeklyAssignedHours());
            if(subject.getAssignedClassroom() != null) {
                preparedStatement.setString(6, subject.getAssignedClassroom().getId());
            } else {
                preparedStatement.setNull(6, Types.VARCHAR);
            }
            preparedStatement.setString(7, subject.getId());
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public List<Subject> getAll() throws SQLException{
        List<Subject> subjects = new ArrayList<>();
        String query = "SELECT * FROM subjects";
        try(Statement createStatement = connection.createStatement();
            ResultSet resultset = createStatement.executeQuery(query)) {

            while (resultset.next()) {
                String id = resultset.getString("id");
                String name = resultset.getString("name");
                String abbreviation = resultset.getString("abbreviation");
                String department = resultset.getString("department");
                String course = resultset.getString("course");
                int weeklyAssignedHours = resultset.getInt("weeklyAssignedHours");
                String classroomId = resultset.getString("assignedClassroom");
                Classroom assignedClassroom = classroomDAO.getOne(classroomId);


                Subject subject = new Subject(id, name, abbreviation);
                subject.setDepartment(department);
                subject.setCourse(course);
                subject.setWeeklyAssignedHours(weeklyAssignedHours);
                subject.setAssignedClassroom(assignedClassroom);

                subjects.add(subject);
            }


        }
        return subjects;

    }

    @Override

    public Subject getOne(String subjectId) throws SQLException{
        //Subject subject = new Subject<>();
        String query = "SELECT * FROM subjects WHERE id = ? ";


        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, subjectId);

            ResultSet resultset = preparedStatement.executeQuery();
            if (resultset.next()) {
                String id = resultset.getString("id");
                String name = resultset.getString("name");
                String abbreviation = resultset.getString("abbreviation");
                String department = resultset.getString("department");
                String course = resultset.getString("course");
                int weeklyAssignedHours = resultset.getInt("weeklyAssignedHours");
                String classroomId = resultset.getString("assignedClassroom");
                Classroom assignedClassroom = classroomDAO.getOne(classroomId);
                //TOdo acabar de arreglar departamento

                Subject subject = new Subject(id, name, abbreviation);
                subject.setDepartment(department);
                subject.setCourse(course);
                subject.setWeeklyAssignedHours(weeklyAssignedHours);
                subject.setAssignedClassroom(assignedClassroom);

                return subject;
            }else{
                return null;
            }

        }


    }

    @Override
    public void delete (Subject subject) throws SQLException{
        String query = "DELETE FROM subjects WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,subject.getId());
            preparedStatement.executeUpdate();
        }

    }
}
