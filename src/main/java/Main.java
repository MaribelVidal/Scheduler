import business.BusinessController;
import persistence.PersistenceController;

public class Main {

    public static void main (String[] args) throws Exception {
        BusinessController businessController = new BusinessController();
        businessController.createExampleData();
        businessController.solve();
        businessController.test();

        //businessController.debug();
/*
        PersistenceController persistenceController = new PersistenceController();
        persistenceController.connect();

 */
    }

}
