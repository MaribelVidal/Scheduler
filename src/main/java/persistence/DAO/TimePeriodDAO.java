package persistence.DAO;

import business.Subject;
import business.Teacher;
import business.TimePeriod;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TimePeriodDAO implements DAO<TimePeriod>{

    private Connection connection;


    public TimePeriodDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void add(TimePeriod timePeriod) throws SQLException {
        String query = "INSERT INTO timePeriods (id, weekDay, initialHour, finalHour, idTeacher, idSubject, idStudentGroup, idClassroom) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,timePeriod.getId());
            preparedStatement.setInt(2,timePeriod.getWeekday());
            preparedStatement.setTime(3,java.sql.Time.valueOf(timePeriod.getInitialHour()));
            preparedStatement.setTime(4,java.sql.Time.valueOf(timePeriod.getFinalHour()));
            preparedStatement.setString(5,timePeriod.getIdTeacher());
            preparedStatement.setString(6,timePeriod.getIdSubject());
            preparedStatement.setString(7,timePeriod.getIdStudentGroup());
            preparedStatement.setString(8,timePeriod.getIdClassroom());
            preparedStatement.executeUpdate();
        }

    }

    @Override
    public void update(TimePeriod timePeriod) throws SQLException {
        String query = "UPDATE timePeriods SET weekDay = ?, initialHour = ?, finalHour = ?, idTeacher = ?, idSubject = ?, idStudentGroup = ?, idClassroom = ? WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, timePeriod.getWeekday());
            preparedStatement.setTime(2,java.sql.Time.valueOf(timePeriod.getInitialHour()));
            preparedStatement.setTime(3,java.sql.Time.valueOf(timePeriod.getFinalHour()));
            preparedStatement.setString(4, timePeriod.getIdTeacher());
            preparedStatement.setString(5, timePeriod.getIdSubject());
            preparedStatement.setString(6, timePeriod.getIdStudentGroup());
            preparedStatement.setString(7, timePeriod.getIdClassroom());
            preparedStatement.setString(8, timePeriod.getId());

            preparedStatement.executeUpdate();
        }
    }

    @Override
    public List<TimePeriod> getAll() throws SQLException{
        List<TimePeriod> timePeriods = new ArrayList<>();
        String query = "SELECT * FROM timePeriods";
        try(Statement createStatement = connection.createStatement();
            ResultSet resultset = createStatement.executeQuery(query)) {

            while (resultset.next()) {
                String id = resultset.getString("id");
                int weekDay = resultset.getInt("weekDay");
                LocalTime initialHour = resultset.getTime("initialHour").toLocalTime();
                LocalTime finalHour = resultset.getTime("finalHour").toLocalTime();
                String idTeacher = resultset.getString("idTeacher");
                String idSubject = resultset.getString("idSubject");
                String idStudentGroup = resultset.getString("idStudentGroup");
                String idClassroom = resultset.getString("idClassroom");



                TimePeriod timePeriod = new TimePeriod(id, weekDay, initialHour, finalHour);
                timePeriod.setIdTeacher(idTeacher);
                timePeriod.setIdSubject(idSubject);
                timePeriod.setIdStudentGroup(idStudentGroup);
                timePeriod.setIdClassroom(idClassroom);

                timePeriods.add(timePeriod);
            }


        }
        return timePeriods;

    }

    @Override
    public void delete (TimePeriod timePeriod) throws SQLException{
        String query = "DELETE FROM timePeriods WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,timePeriod.getId());
            preparedStatement.executeUpdate();


        }

    }

    @Override
    public TimePeriod getOne(String timePeriodId) throws SQLException {

        String query = "SELECT * FROM timePeriods WHERE id = ? ";


        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, timePeriodId);

            ResultSet resultset = preparedStatement.executeQuery();
            if (resultset.next()) {
                String id = resultset.getString("id");
                int weekDay = resultset.getInt("weekDay");
                LocalTime initialHour = resultset.getTime("initialHour").toLocalTime();
                LocalTime finalHour = resultset.getTime("finalHour").toLocalTime();
                String idTeacher = resultset.getString("idTeacher");
                String idSubject = resultset.getString("ideSubject");
                String idStudentGroup = resultset.getString("idStudentGroup");
                String idClassroom = resultset.getString("idClassroom");


                TimePeriod timePeriod = new TimePeriod(id, weekDay, initialHour, finalHour);
                timePeriod.setIdTeacher(idTeacher);
                timePeriod.setIdSubject(idSubject);
                timePeriod.setIdStudentGroup(idStudentGroup);
                timePeriod.setIdClassroom(idClassroom);

                return timePeriod;

            }else{
                return null;
            }
        }

    }
}
