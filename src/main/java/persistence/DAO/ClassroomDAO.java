package persistence.DAO;

import business.Classroom;
import business.Schedule;
import business.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassroomDAO implements DAO<Classroom>{
   private Connection connection;
   private  ClassroomAssignedSubjectsDAO classroomAssignedSubjectsDAO;
   private  ClassroomAssignedSchedulesDAO classroomAssignedSchedulesDAO;



    public ClassroomDAO(Connection connection) {
        this.connection = connection;

    }

    public void setClassroomAssignedSubjectsDAO(ClassroomAssignedSubjectsDAO classroomAssignedSubjectsDAO) {
        this.classroomAssignedSubjectsDAO = classroomAssignedSubjectsDAO;
    }
    public void setClassroomAssignedSchedulesDAO(ClassroomAssignedSchedulesDAO classroomAssignedSchedulesDAO) {
        this.classroomAssignedSchedulesDAO = classroomAssignedSchedulesDAO;
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
            preparedStatement.executeUpdate();
            for(Subject subject :classroom.getAssignedSubjects()){
                classroomAssignedSubjectsDAO.addAssignedSubjects(classroom.getId(), subject.getId());
            }

            for(Schedule schedule :classroom.getSchedules()){
                classroomAssignedSchedulesDAO.addAssignedSchedules(classroom.getId(), schedule.getId());
            }


        }

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
            for(Subject subject :classroom.getAssignedSubjects()){
                classroomAssignedSubjectsDAO.addAssignedSubjects(classroom.getId(), subject.getId());
            }

            for(Schedule schedule :classroom.getSchedules()){
                classroomAssignedSchedulesDAO.addAssignedSchedules(classroom.getId(), schedule.getId());
            }
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

    @Override
    public void delete (Classroom classroom) throws SQLException{
        String query = "DELETE FROM classrooms WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,classroom.getId());
            preparedStatement.executeUpdate();
        }
    }

    public Classroom getOneLight(String id) throws SQLException {
        String sql = "SELECT id, name, abbreviation, classroomType, capacity FROM classrooms WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Classroom c = new Classroom(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("abbreviation")
                );
                c.setCapacity(rs.getInt("capacity"));
                // DO NOT load assignedSubjects or schedules here
                return c;
            }
        }
    }


}
