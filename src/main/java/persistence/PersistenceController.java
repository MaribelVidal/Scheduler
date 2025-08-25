package persistence;

import persistence.DAO.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PersistenceController {
    private final DataBaseConnection dataBaseConnection;




    public PersistenceController () throws SQLException {

        this.dataBaseConnection = new DataBaseConnection();
        Connection connection = dataBaseConnection.getConnection();




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

}
