package persistence.DAO;

import business.Classroom;
import business.Schedule;
import business.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassroomDAO implements DAO<Classroom>{
   private final ClassroomAssignedSubjectsDAO classroomAssignedSubjectsDAO;
   private final ClassroomAssignedSchedulesDAO classroomAssignedSchedulesDAO;



    private Connection connection;


    public ClassroomDAO(Connection connection) {

        this.connection = connection;
        this.classroomAssignedSubjectsDAO = new ClassroomAssignedSubjectsDAO(connection, new SubjectDAO(connection));
        this.classroomAssignedSchedulesDAO = new ClassroomAssignedSchedulesDAO(connection, new ScheduleDAO(connection));
    }

    @Override
    public void add(Classroom classroom) throws SQLException {
        String query = "INSERT INTO classrooms (id, name, abbreviation, classroomType, capacity) VALUES (?, ?, ?, ?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,classroom.getId());
            preparedStatement.setString(2,classroom.getName());
            preparedStatement.setString(3,classroom.getAbbreviation());
            preparedStatement.setString(4,classroom.getClassroomType());
            preparedStatement.setInt(5,classroom.getCapacity());

            for(Subject subject :classroom.getAssignedSubjects()){
                classroomAssignedSubjectsDAO.classroomAssignedSubjects(classroom.getId(), subject.getId());

            }

            for(Schedule schedule :classroom.getSchedules()){
                classroomAssignedSchedulesDAO.classroomAssignedSchedules(classroom.getId(), schedule.getId());


            }


            preparedStatement.executeUpdate();
        }

    }

    @Override
    public List<Classroom> getAll() throws SQLException{
        List<Classroom> classrooms = new ArrayList<>();
        String query = "SELECT * FROM classrooms";
        try(Statement createStatement = connection.createStatement();
            ResultSet resultset = createStatement.executeQuery(query)) {

            while (resultset.next()) {
                String id = resultset.getString("id");
                String name = resultset.getString("name");
                String abbreviation = resultset.getString("abbreviation");
                String classroomType = resultset.getString("classroomType");
                int capacity = resultset.getInt("capacity");


                Classroom classroom = new Classroom(id, name, abbreviation);
                classroom.setClassroomType(classroomType);
                classroom.setCapacity(capacity);

                classrooms.add(classroom);
            }


        }
        return classrooms;

    }

    @Override
    public void update(Classroom classroom) throws SQLException {
        String query = "UPDATE classrooms SET name = ?, abbreviation = ?, classroomType = ?, capacity = ? WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, classroom.getName());
            preparedStatement.setString(2, classroom.getAbbreviation());
            preparedStatement.setString(3, classroom.getClassroomType());
            preparedStatement.setInt(4, classroom.getCapacity());
            preparedStatement.setString(5, classroom.getId());

            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void delete (Classroom classroom) throws SQLException{
        String query = "DELETE FROM classrooms WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,classroom.getId());
            preparedStatement.executeUpdate();
        }

    }

    @Override
    public Classroom getOne(String classroomId) throws SQLException {
        //Subject subject = new Subject<>();
        String query = "SELECT * FROM classrooms WHERE id = ? ";


        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, classroomId);

            ResultSet resultset = preparedStatement.executeQuery();
            if (resultset.next()) {
                String id = resultset.getString("id");
                String name = resultset.getString("name");
                String abbreviation = resultset.getString("abbreviation");
                String classroomType = resultset.getString("classroomType");
                int capacity = resultset.getInt("capacity");


                Classroom classroom = new Classroom(id, name, abbreviation);
                classroom.setClassroomType(classroomType);
                classroom.setCapacity(capacity);

                return classroom;
            }else {
                return null;
            }

        }
    }
}
