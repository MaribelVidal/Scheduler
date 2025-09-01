import business.BusinessController;
import persistence.PersistenceController;
import presentation.PresentationController;

import javax.swing.*;

public class Main {

    public static void main (String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            try {
                PresentationController pc = new PresentationController();
                //pc.test();
                pc.startCalendar();           // builds the UI
                // make sure the frame becomes visible
                // (see tweak in B below, or call it here)
                // pc.getCalendar().setVisible(true); // if you expose a getter
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        //BusinessController businessController = new BusinessController();
        //businessController.createExampleData();
        //businessController.solve();
        //businessController.test();
        //businessController.testH();

        //businessController.debug();
/*
        PersistenceController persistenceController = new PersistenceController();
        persistenceController.connect();

 */
    }

}
