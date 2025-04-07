package persistence;

public class PersistenceController {
    private final DataBaseConnection dataBaseConnection;


    public PersistenceController (){
        this.dataBaseConnection = new DataBaseConnection();
    }

    public void connect (){
        dataBaseConnection.connect();
    }

}
