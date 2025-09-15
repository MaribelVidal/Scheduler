package persistence.DAO;

import business.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherDAO implements DAO<Teacher>{
    private Connection connection;
    private final TeacherPossibleSubjectsDAO teacherPossibleSubjectsDAO;
    private final TeacherPreferredConditionsDAO teacherPreferredConditionsDAO;
    private final TeacherUnpreferredConditionsDAO teacherUnpreferredConditionsDAO;
    private TeacherAssignedSchedulesDAO teacherAssignedSchedulesDAO;
    private final TeacherUnavailableTimePeriodsDAO teacherUnavailableTimePeriodsDAO;
    private final ConditionDAO conditionDAO;


    public TeacherDAO(Connection connection) {
        this.connection = connection;
        this.teacherPossibleSubjectsDAO = new TeacherPossibleSubjectsDAO(connection, new SubjectDAO(connection));
        this.teacherPreferredConditionsDAO = new TeacherPreferredConditionsDAO(connection, new ConditionDAO(connection));
        this.teacherUnpreferredConditionsDAO = new TeacherUnpreferredConditionsDAO(connection, new ConditionDAO(connection));
        this.teacherAssignedSchedulesDAO = new TeacherAssignedSchedulesDAO(connection, new ScheduleDAO(connection));
        this.teacherUnavailableTimePeriodsDAO = new TeacherUnavailableTimePeriodsDAO(connection, new TimePeriodDAO(connection));
        this.conditionDAO = new ConditionDAO(connection);
    }

    public void setScheduleDAO(ScheduleDAO scheduleDAO) {
        this.teacherAssignedSchedulesDAO = new TeacherAssignedSchedulesDAO(connection, scheduleDAO);
    }

    @Override
    public void add(Teacher teacher) throws SQLException {
        String query = "INSERT INTO teachers (id, name, abbreviation, email, phone, department, hoursWork) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,teacher.getId());
            preparedStatement.setString(2,teacher.getName());
            preparedStatement.setString(3,teacher.getAbbreviation());


            if(teacher.getEmail() == null || teacher.getEmail().isEmpty()){
                preparedStatement.setNull(4, Types.VARCHAR);
            } else {
                preparedStatement.setString(4,teacher.getEmail());
            }

            if(teacher.getPhone() == null || teacher.getPhone().isEmpty()){
                preparedStatement.setNull(5, Types.VARCHAR);
            } else {
                preparedStatement.setString(5,teacher.getPhone());
            }
            if(teacher.getDepartment() == null || teacher.getDepartment().isEmpty()){
                preparedStatement.setNull(6, Types.VARCHAR);
            } else {
                preparedStatement.setString(6,teacher.getDepartment());
            }


            preparedStatement.setInt(7,teacher.getHoursWork());

            preparedStatement.executeUpdate();

            for(Subject subject :teacher.getPossibleSubjects()){
                teacherPossibleSubjectsDAO.addPossibleSubjects(teacher.getId(), subject.getId());

            }

            for (Condition condition : teacher.getPreferredConditions()) {
                conditionDAO.add(condition);
                teacherPreferredConditionsDAO.addPreferredConditions(teacher.getId(), condition.getId());
            }
            for (Condition condition : teacher.getUnPreferredConditions()) {
                conditionDAO.add(condition);
                teacherUnpreferredConditionsDAO.addUnpreferredConditions(teacher.getId(), condition.getId());
            }

            for (Schedule schedule : teacher.getSchedules()) {
                teacherAssignedSchedulesDAO.addAssignedSchedules(teacher.getId(), schedule.getId());
            }
            for (TimePeriod timePeriod : teacher.getUnavailableTimePeriods()) {
                teacherUnavailableTimePeriodsDAO.addUnavailableTimePeriods(teacher.getId(), timePeriod.getId());
            }


        }

    }

    @Override
    public void update(Teacher teacher) throws SQLException {
        String query = "UPDATE teachers SET name = ?, abbreviation = ?, email = ?, phone = ?, hoursWork = ?, department = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, teacher.getName());
            preparedStatement.setString(2, teacher.getAbbreviation());

            if (teacher.getEmail() == null || teacher.getEmail().isEmpty())
                preparedStatement.setNull(3, Types.VARCHAR);
            else
                preparedStatement.setString(3, teacher.getEmail());

            if (teacher.getPhone() == null || teacher.getPhone().isEmpty())
                preparedStatement.setNull(4, Types.VARCHAR);
            else
                preparedStatement.setString(4, teacher.getPhone());

            preparedStatement.setInt(5, teacher.getHoursWork());

            if (teacher.getDepartment() == null || teacher.getDepartment().isEmpty())
                preparedStatement.setNull(6, Types.VARCHAR);
            else
                preparedStatement.setString(6, teacher.getDepartment());

            preparedStatement.setString(7, teacher.getId());
            preparedStatement.executeUpdate();
        }
    }


    @Override
    public List<Teacher> getAll() throws SQLException{
        List<Teacher> teachers = new ArrayList<>();
        String query = "SELECT * FROM teachers";
        try(Statement createStatement = connection.createStatement();
            ResultSet resultset = createStatement.executeQuery(query)) {

            while (resultset.next()) {
                String id = resultset.getString("id");
                String name = resultset.getString("name");
                String abbreviation = resultset.getString("abbreviation");

                String email = resultset.getString("email");
                if(email == null || email.isEmpty()){
                    email = null;
                }
                String phone = resultset.getString("phone");
                if(phone == null || phone.isEmpty()){
                    phone = null;
                }
                int hoursWork = resultset.getInt("hoursWork");
                String department = resultset.getString("department");
                if(department == null || department.isEmpty()){
                    department = null;
                }
                List<Subject> subjects = teacherPossibleSubjectsDAO.getAllPossibleSubjects(id);
                List<Condition> preferredConditions = teacherPreferredConditionsDAO.getAllPreferredConditions(id);
                List<Condition> unPreferredConditions = teacherUnpreferredConditionsDAO.getAllUnpreferredConditions(id);
                List<Schedule> schedules = teacherAssignedSchedulesDAO.getAllAssignedSchedules(id);
                List<TimePeriod> unavailableTimePeriods = teacherUnavailableTimePeriodsDAO.getAllUnavailableTimePeriods(id);

                Teacher teacher = new Teacher(id, name, abbreviation);
                teacher.setEmail(email);
                teacher.setPhone(phone);
                teacher.setHoursWork(hoursWork);
                teacher.setDepartment(department);
                teacher.setPossibleSubjects(subjects);
                teacher.setPreferredConditions(preferredConditions);
                teacher.setUnPreferredConditions(unPreferredConditions);
                teacher.setSchedules(schedules);
                teacher.setUnavailableTimePeriods(unavailableTimePeriods);
                teachers.add(teacher);
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return teachers;

    }

    public Teacher getOne(String id) throws SQLException {
        return getOne(id, false);
    }

    public Teacher getOne(String id, boolean shallow) throws SQLException {
        String sql = "SELECT * FROM teachers WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Teacher t = new Teacher(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("abbreviation")
                );
                t.setEmail(rs.getString("email"));
                t.setPhone(rs.getString("phone"));
                t.setDepartment(rs.getString("department"));
                t.setHoursWork(rs.getInt("hoursWork"));

                // Always-safe relations
                SubjectDAO subjectDAO = new SubjectDAO(connection);
                TimePeriodDAO timePeriodDAO = new TimePeriodDAO(connection);
                ScheduleDAO scheduleDAO = new ScheduleDAO(connection);
                ConditionDAO conditionDAO = new ConditionDAO(connection);

                t.setPossibleSubjects(new TeacherPossibleSubjectsDAO(connection, subjectDAO).getAllPossibleSubjects(id));
                t.setUnavailableTimePeriods(new TeacherUnavailableTimePeriodsDAO(connection, timePeriodDAO).getAllUnavailableTimePeriods(id));
                t.setSchedules(new TeacherAssignedSchedulesDAO(connection, scheduleDAO).getAllAssignedSchedules(id));

                // Only load conditions when NOT shallow, otherwise leave them empty
                if (!shallow) {
                    t.setPreferredConditions(new TeacherPreferredConditionsDAO(connection, conditionDAO).getAllPreferredConditions(id));
                    t.setUnPreferredConditions(new TeacherUnpreferredConditionsDAO(connection, conditionDAO).getAllUnpreferredConditions(id));
                } else {
                    t.setPreferredConditions(new java.util.ArrayList<>());
                    t.setUnPreferredConditions(new java.util.ArrayList<>());
                }
                return t;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public void delete (Teacher teacher) throws Exception {
        String query = "DELETE FROM teachers WHERE id = ?";

        teacherPreferredConditionsDAO.deleteTeacher(teacher.getId());
        teacherUnpreferredConditionsDAO.deleteTeacher(teacher.getId());
        teacherAssignedSchedulesDAO.deleteTeacher(teacher.getId());
        teacherUnavailableTimePeriodsDAO.deleteTeacher(teacher.getId());
        teacherPossibleSubjectsDAO.deleteTeacher(teacher.getId());

        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,teacher.getId());
            preparedStatement.executeUpdate();
        }

    }




}
