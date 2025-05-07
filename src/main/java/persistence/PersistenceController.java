package persistence;

import persistence.DAO.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PersistenceController {
    private final DataBaseConnection dataBaseConnection;
    private TeacherDAO teacherDAO;
    private StudentGroupDAO studentGroupDAO;
    private SubjectDAO subjectDAO;
    private ClassroomDAO classroomDAO;
    private TimePeriodDAO timePeriodDAO;
    private PossibleTeacherSubjectsDAO possibleTeacherSubjectsDAO;
    private ActivityDAO activityDAO;
    private TeachersInActivityDAO teeachersInActivityDAO;


    public PersistenceController () throws SQLException {

        this.dataBaseConnection = new DataBaseConnection();
        Connection connection = dataBaseConnection.getConnection();
        this.teacherDAO = new TeacherDAO(connection);
        this.studentGroupDAO = new StudentGroupDAO(connection);
        this.subjectDAO = new SubjectDAO(connection);
        this.classroomDAO = new ClassroomDAO(connection);
        this.timePeriodDAO = new TimePeriodDAO(connection);
        this.activityDAO = new ActivityDAO(connection);
        this.teeachersInActivityDAO = new TeachersInActivityDAO(connection, teacherDAO);
        this.possibleTeacherSubjectsDAO = new PossibleTeacherSubjectsDAO(connection, subjectDAO);
    }

    public void connect (){
        DataBaseConnection.connect();
    }

    public void initialize (){

        DataBaseConnection.connect();
        dataBaseConnection.createAllTables();


    }

    public <T> void add(T t) throws Exception {
        DAO<T> dao = DAOFactory.getDAO((Class<T>) t.getClass(), dataBaseConnection.getConnection());
        dao.add(t);
    }
    public <T> List<T> getAll(Class<T> c) throws Exception {
        DAO<T> dao = DAOFactory.getDAO(c, dataBaseConnection.getConnection());
        return dao.getAll();
    }
    public <T> void delete(T t) throws Exception {
        DAO<T> dao = DAOFactory.getDAO((Class<T>) t.getClass(), dataBaseConnection.getConnection());
        dao.delete(t);
    }

    public <T> T getOne (String id, Class<T> c) throws Exception {
        DAO<T> dao = DAOFactory.getDAO(c, dataBaseConnection.getConnection());
        return dao.getOne(id);
    }



}
