package persistence.DAO;

import business.Subject;
import business.Teacher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeacherDAO implements DAO<Teacher>{
    private Connection connection;
    private final PossibleTeacherSubjectsDAO possibleTeacherSubjectsDAO;
    private final TeacherPreferredConditionsDAO teacherPreferredConditionsDAO;
    private final TeacherUnpreferredConditionsDAO teacherUnpreferredConditionsDAO;
    private final TeacherAssignedSchedulesDAO teacherAssignedSchedulesDAO;
    private final TeacherUnavailableTimePeriodsDAO teacherUnavailableTimePeriodsDAO;


    public TeacherDAO(Connection connection) {
        this.connection = connection;
        this.possibleTeacherSubjectsDAO = new PossibleTeacherSubjectsDAO(connection, new SubjectDAO(connection));
        this.teacherPreferredConditionsDAO = new TeacherPreferredConditionsDAO(connection, new ConditionDAO(connection));
        this.teacherUnpreferredConditionsDAO = new TeacherUnpreferredConditionsDAO(connection, new ConditionDAO(connection));
        this.teacherAssignedSchedulesDAO = new TeacherAssignedSchedulesDAO(connection, new ScheduleDAO(connection));
        this.teacherUnavailableTimePeriodsDAO = new TeacherUnavailableTimePeriodsDAO(connection, new TimePeriodDAO(connection));

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
                possibleTeacherSubjectsDAO.assignTeacherSubjects(teacher.getId(), subject.getId());

            }

            for (String conditionId : teacher.getPreferredConditions()) {
                teacherPreferredConditionsDAO.assignTeacherCondition(teacher.getId(), conditionId);
            }
            for (String conditionId : teacher.getUnpreferredConditions()) {
                teacherUnpreferredConditionsDAO.assignTeacherCondition(teacher.getId(), conditionId);
            }



            for (String scheduleId : teacher.getAssignedSchedules()) {
                teacherAssignedSchedulesDAO.assignTeacherSchedule(teacher.getId(), scheduleId);
            }


        }

    }

    @Override
    public void update(Teacher teacher) throws SQLException {
        String query = "UPDATE teachers SET name = ?, abbreviation = ?, email = ?, phone = ?, hoursWork = ?, department = ? WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, teacher.getName());
            preparedStatement.setString(2, teacher.getAbbreviation());
            if(teacher.getEmail() == null || teacher.getEmail().isEmpty()){
                preparedStatement.setNull(3, Types.VARCHAR);
            } else {
                preparedStatement.setString(3,teacher.getEmail());
            }

            if(teacher.getPhone() == null || teacher.getPhone().isEmpty()){
                preparedStatement.setNull(4, Types.VARCHAR);
            } else {
                preparedStatement.setString(4,teacher.getPhone());
            }

            preparedStatement.setInt(5, teacher.getHoursWork());
            if(teacher.getDepartment() == null || teacher.getDepartment().isEmpty()){
                preparedStatement.setNull(6, Types.VARCHAR);
            } else {
                preparedStatement.setString(6,teacher.getDepartment());
            }

            preparedStatement.setString(7, teacher.getId());

            preparedStatement.executeUpdate();
        }
    }

    @Override
    public Teacher getOne(String id, String id2, String type) throws Exception {
        return null;
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
                List<Subject> subjects = possibleTeacherSubjectsDAO.getAllTeacherSubjects(id);

                Teacher teacher = new Teacher(id, name, abbreviation);
                teacher.setEmail(email);
                teacher.setPhone(phone);
                teacher.setHoursWork(hoursWork);
                teacher.setDepartment(department);
                teacher.setPossibleSubjects(subjects);
                teachers.add(teacher);
            }


        }
        return teachers;

    }

    @Override
    public void delete (Teacher teacher) throws SQLException{
        String query = "DELETE FROM teachers WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,teacher.getId());
            preparedStatement.executeUpdate();
        }

    }

    @Override
    public Teacher getOne(String teacherId) throws SQLException {
        //Subject subject = new Subject<>();
        String query = "SELECT * FROM teachers WHERE id = ? ";


        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, teacherId);

            ResultSet resultset = preparedStatement.executeQuery();
            if (resultset.next()) {
                String id = resultset.getString("id");
                String name = resultset.getString("name");
                String abbreviation = resultset.getString("abbreviation");
                String email = resultset.getString("email");
                if (email == null || email.isEmpty()) {
                    email = null;
                }
                String phone = resultset.getString("phone");
                if (phone == null || phone.isEmpty()) {
                    phone = null;
                }
                int hoursWork = resultset.getInt("hoursWork");
                String department = resultset.getString("department");
                if (department == null || department.isEmpty()) {
                    department = null;
                }
                List<Subject> subjects = possibleTeacherSubjectsDAO.getAllTeacherSubjects(id);

                Teacher teacher = new Teacher(id, name, abbreviation);
                teacher.setEmail(email);
                teacher.setPhone(phone);
                teacher.setHoursWork(hoursWork);
                teacher.setDepartment(department);
                teacher.setPossibleSubjects(subjects);

                return teacher;

            }else {
                return null;
            }
        }
    }
}
