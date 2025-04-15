package persistence.DAO;

import business.Classroom;
import business.Department;
import business.Subject;
import business.Teacher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubjectDAO {

    private Connection connection;


    public SubjectDAO(Connection connection) {
        this.connection = connection;
    }


    public void addSubject(Subject subject) throws SQLException {
        String query = "INSERT INTO subjects (id, name, abbreviation, department, course, weeklyAssignedHours, assignedClassroom) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1,subject.getId());
            preparedStatement.setString(2,subject.getName());
            preparedStatement.setString(3,subject.getAbbreviation());
            preparedStatement.setInt(4,subject.getDepartment().getId());
            preparedStatement.setString(5, subject.getCourse());
            preparedStatement.setInt(6,subject.getWeeklyAssignedHours());
            preparedStatement.setInt(7,subject.getAssignedClassroom().getId());

            preparedStatement.executeUpdate();
        }

    }

    public List<Subject> getAllSubjects() throws SQLException{
        List<Subject> subjects = new ArrayList<>();
        String query = "SELECT * FROM subjects";
        try(Statement createStatement = connection.createStatement();
            ResultSet resultset = createStatement.executeQuery(query)) {

            while (resultset.next()) {
                int id = resultset.getInt("id");
                String name = resultset.getString("name");
                String abbreviation = resultset.getString("abbreviation");
                int departmentId = resultset.getInt("departmentId");
                String course = resultset.getString("course");
                int weeklyAssignedHours = resultset.getInt("weeklyAssignedHours");
                int classroomId = resultset.getInt("classroomId");

                //todo

                Subject subject = new Subject(id, name, abbreviation);
                //subject.setDepartment(department);
                subject.setCourse(course);
                subject.setWeeklyAssignedHours(weeklyAssignedHours);
                //Subject.setAssignedClassroom(assignedClassroom);

                subjects.add(subject);
            }


        }
        return subjects;

    }


    public Subject getOneSubjects(int subjectId) throws SQLException{
        //Subject subject = new Subject<>();
        String query = "SELECT * FROM subjects WHERE id = ? ";


        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, subjectId);

            ResultSet resultset = preparedStatement.executeQuery(query);

            int id = resultset.getInt("id");
            String name = resultset.getString("name");
            String abbreviation = resultset.getString("abbreviation");
            int departmentId = resultset.getInt("departmentId");
            String course = resultset.getString("course");
            int weeklyAssignedHours = resultset.getInt("weeklyAssignedHours");
            int classroomId = resultset.getInt("classroomId");

            //TOdo acabar de arreglar departamento y classroom

            Subject subject = new Subject(id, name, abbreviation);
            //subject.setDepartment(department);
            subject.setCourse(course);
            subject.setWeeklyAssignedHours(weeklyAssignedHours);
            //Subject.setAssignedClassroom(assignedClassroom);

            return subject;


        }


    }


    public void deleteSubject (Subject subject) throws SQLException{
        String query = "DELETE FROM subjects WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1,subject.getId());
            preparedStatement.executeUpdate();
        }

    }
}
