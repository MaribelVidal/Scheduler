package persistence.DAO;

import business.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConditionDAO implements DAO<Condition>{
    private Connection connection;


    public ConditionDAO(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void add(Condition condition) throws SQLException {
        String q = "INSERT INTO conditions " +
                "(id, teacher, weight, subject, timePeriod, studentGroup) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(q)) {
            ps.setString(1, condition.getId());
            ps.setString(2, condition.getTeacher().getId());
            ps.setInt(3, condition.getWeight());

            if (condition.getSubject() != null)
                ps.setString(4, condition.getSubject().getId());
            else
                ps.setNull(4, java.sql.Types.VARCHAR);

            if (condition.getTimePeriod() != null)
                ps.setString(5, condition.getTimePeriod().getId());
            else
                ps.setNull(5, java.sql.Types.VARCHAR);

            if (condition.getStudentGroup() != null)
                ps.setString(6, condition.getStudentGroup().getId());
            else
                ps.setNull(6, java.sql.Types.VARCHAR);

            ps.executeUpdate();
        }
    }


    @Override
    public void update(Condition condition) throws SQLException {

        String query = "UPDATE conditions SET  teacher = ?, weight = ?, subject = ?, timePeriod = ?, studentGroup = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, condition.getId());
            ps.setString(2, condition.getTeacher().getId());
            ps.setInt(3, condition.getWeight());

            if (condition.getSubject() != null)
                ps.setString(4, condition.getSubject().getId());
            else
                ps.setNull(4, java.sql.Types.VARCHAR);

            if (condition.getTimePeriod() != null)
                ps.setString(5, condition.getTimePeriod().getId());
            else
                ps.setNull(5, java.sql.Types.VARCHAR);

            if (condition.getStudentGroup() != null)
                ps.setString(6, condition.getStudentGroup().getId());
            else
                ps.setNull(6, java.sql.Types.VARCHAR);

            ps.executeUpdate();
        }

    }

    @Override
    public List<Condition> getAll() throws SQLException{
        List<Condition> conditions = new ArrayList<>();
        String query = "SELECT * FROM conditions";
        try(Statement createStatement = connection.createStatement();
            ResultSet resultset = createStatement.executeQuery(query)) {

            while (resultset.next()) {
                String conditionId = resultset.getString("id");
                String teacherId = resultset.getString("teacher");
                int weight = resultset.getInt("weight");
                String subjectId = resultset.getString("subject");
                String timePeriodId = resultset.getString("timePeriod");
                String studentGroupId = resultset.getString("studentGroup");

                Teacher teacher = new TeacherDAO(connection).getOne(teacherId);
                Condition condition = null;
                if (!Objects.equals(subjectId, "dummy")){
                    Subject subject = new SubjectDAO(connection).getOne(subjectId);
                    condition = new Condition(teacher, weight, subject);
                    condition.setId(conditionId);
                }
                else if (!Objects.equals(timePeriodId, "dummy")){
                    TimePeriod timePeriod = new TimePeriodDAO(connection).getOne(timePeriodId);
                    condition = new Condition(teacher, weight, timePeriod);
                    condition.setId(conditionId);
                }
                else if (!Objects.equals(studentGroupId, "dummy")){
                    StudentGroup studentGroup = new StudentGroupDAO(connection).getOne(studentGroupId);
                    condition = new Condition(teacher, weight, studentGroup);
                    condition.setId(conditionId);

                }


                conditions.add(condition);
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return conditions;

    }

    public Condition getOne(String id) throws SQLException {
        return getOne(id, false);
    }

    public Condition getOne(String id, boolean shallow) throws SQLException {
        String query = "SELECT * FROM conditions WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String teacherId      = rs.getString("teacher");
                int    weight         = rs.getInt("weight");
                String subjectId      = rs.getString("subject");
                String timePeriodId   = rs.getString("timePeriod");
                String studentGroupId = rs.getString("studentGroup");

                Teacher teacher = (shallow)
                        ? new TeacherDAO(connection).getOne(teacherId, true)   // ← shallow teacher (no recursive conditions)
                        : new TeacherDAO(connection).getOne(teacherId);

                Subject subject = (subjectId != null)
                        ? new SubjectDAO(connection).getOne(subjectId)
                        : null;

                TimePeriod timePeriod = (timePeriodId != null)
                        ? new TimePeriodDAO(connection).getOne(timePeriodId)
                        : null;

                StudentGroup studentGroup = (studentGroupId != null)
                        ? new StudentGroupDAO(connection).getOne(studentGroupId)
                        : null;

                Condition condition = new Condition(teacher, weight);
                condition.setId(id);
                if (subject != null)      condition.setSubject(subject);
                if (timePeriod != null)   condition.setTimePeriod(timePeriod);
                if (studentGroup != null) condition.setStudentGroup(studentGroup);

                // If you store the condition type in DB, set it here as well
                // condition.setConditionType(rs.getString("conditionType"));

                return condition;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }




    public void delete(String conditionId) throws SQLException {
        String delPref = "DELETE FROM teacherPreferredConditions WHERE conditionId = ?";
        String delUnpref = "DELETE FROM teacherUnpreferredConditions WHERE conditionId = ?";
        String delCond = "DELETE FROM conditions WHERE id = ?";

        boolean oldAuto = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(delPref)) {
                ps.setString(1, conditionId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(delUnpref)) {
                ps.setString(1, conditionId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(delCond)) {
                ps.setString(1, conditionId);
                ps.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(oldAuto);
        }
    }

    @Override
    public void delete(Condition c) throws SQLException {
        if (c != null) delete(c.getId());
    }


}
