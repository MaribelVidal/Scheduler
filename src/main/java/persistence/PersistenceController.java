package persistence;

import business.Subject;
import persistence.DAO.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PersistenceController {
    private  DataBaseConnection dataBaseConnection;

    private final TeacherDAO teacherDAO;
    private final SubjectDAO subjectDAO;
    private final ClassroomDAO classroomDAO;
    private final StudentGroupDAO studentGroupDAO;
    private final TimePeriodDAO timePeriodDAO;
    private final LessonDAO lessonDAO;
    private final ScheduleDAO scheduleDAO;


    public PersistenceController () throws SQLException {

        this.dataBaseConnection = new DataBaseConnection();
        dataBaseConnection.connect();
        this.dataBaseConnection.createAllTables();
        Connection connection = dataBaseConnection.getConnection();

        this.teacherDAO      = new TeacherDAO(connection);
        this.subjectDAO      = new SubjectDAO(connection);
        this.classroomDAO    = new ClassroomDAO(connection);
        this.studentGroupDAO = new StudentGroupDAO(connection);
        this.timePeriodDAO   = new TimePeriodDAO(connection);
        this.lessonDAO       = new LessonDAO(connection);
        this.scheduleDAO     = new ScheduleDAO(connection);

        lessonDAO.setTeacherDAO(teacherDAO);
        lessonDAO.setStudentGroupDAO(studentGroupDAO);
        lessonDAO.setSubjectDAO(subjectDAO);
        lessonDAO.setClassroomDAO(classroomDAO);
        lessonDAO.setTimePeriodDAO(timePeriodDAO);

        scheduleDAO.setLessonDAO(lessonDAO);
        teacherDAO.setScheduleDAO(scheduleDAO);

        classroomDAO.setClassroomAssignedSchedulesDAO(
                new ClassroomAssignedSchedulesDAO(connection, scheduleDAO));
        classroomDAO.setClassroomAssignedSubjectsDAO(
                new ClassroomAssignedSubjectsDAO(connection, subjectDAO));

        //Probatina




    }

    public void connect () throws SQLException {
        DataBaseConnection.connect();
    }

    public void initialize () throws SQLException {

        DataBaseConnection.connect();
        dataBaseConnection.createAllTables();


    }

    public <T> void add(T t) throws Exception {
        DAO<T> dao = DAOFactory.getDAO((Class<T>) t.getClass(), dataBaseConnection.getConnection());
        dao.add(t);
    }
    public<T> void update(T t) throws Exception {
        DAO<T> dao = DAOFactory.getDAO((Class<T>) t.getClass(), dataBaseConnection.getConnection());
        dao.update(t);
    }
    public <T> List<T> getAll(Class<T> c) throws Exception {
        DAO<T> dao = DAOFactory.getDAO(c, dataBaseConnection.getConnection());
        return dao.getAll();

    }
    public <T> T getOne (String id, Class<T> c) throws Exception {
        DAO<T> dao = DAOFactory.getDAO(c, dataBaseConnection.getConnection());
        return dao.getOne(id);
    }
    public <T> void delete(T t) throws Exception {
        DAO<T> dao = DAOFactory.getDAO((Class<T>) t.getClass(), dataBaseConnection.getConnection());
        dao.delete(t);
    }

    // === Possible subjects (teacher ↔ subject) ===
    public void insertTeacherPossibleSubject(String teacherId, String subjectId) throws SQLException {
        TeacherPossibleSubjectsDAO d = new TeacherPossibleSubjectsDAO(
                dataBaseConnection.getConnection(), new SubjectDAO(dataBaseConnection.getConnection()));
        d.addPossibleSubjects(teacherId, subjectId);
    }

    public void deleteTeacherPossibleSubject(String teacherId, String subjectId) throws SQLException {
        TeacherPossibleSubjectsDAO d = new TeacherPossibleSubjectsDAO(
                dataBaseConnection.getConnection(), new SubjectDAO(dataBaseConnection.getConnection()));
        d.deletePossibleSubjects(teacherId, subjectId);
    }

    public List<Subject> getTeacherPossibleSubjects(String teacherId) throws SQLException {
        TeacherPossibleSubjectsDAO d = new TeacherPossibleSubjectsDAO(
                dataBaseConnection.getConnection(), new SubjectDAO(dataBaseConnection.getConnection()));
        return d.getAllPossibleSubjects(teacherId);
    }

    public Subject getSubject(String subjectId) throws SQLException {
        return new SubjectDAO(dataBaseConnection.getConnection()).getOne(subjectId);
    }

    // --- Required subjects (group ↔ subject) ---
    public List<Subject> getRequiredSubjects(String groupId) throws SQLException {
        var conn = dataBaseConnection.getConnection();
        var d = new StudentGroupRequiredSubjectsDAO(conn, new SubjectDAO(conn));
        return d.getAllRequiredSubjects(groupId);
    }

    public void addRequiredSubject(String groupId, String subjectId) throws SQLException {
        var conn = dataBaseConnection.getConnection();
        var d = new StudentGroupRequiredSubjectsDAO(conn, new SubjectDAO(conn));
        d.insertIgnore(groupId, subjectId);
    }

    public void removeRequiredSubject(String groupId, String subjectId) throws SQLException {
        var conn = dataBaseConnection.getConnection();
        var d = new StudentGroupRequiredSubjectsDAO(conn, new SubjectDAO(conn));
        d.delete(groupId, subjectId);
    }



}
