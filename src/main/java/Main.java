import business.BusinessController;
import persistence.PersistenceController;

public class Main {

    public static void main (String[] args){
        //BusinessController businessController = new BusinessController();

        //businessController.debug();

        PersistenceController persistenceController = new PersistenceController();
        persistenceController.connect();
    }

}
