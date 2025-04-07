package persistence;

import java.sql.Connection;

import java.sql.DriverManager;

import java.sql.SQLException;
import java.sql.Statement;

public class DataBaseConnection {

    // Database connection details

    private static final String URL = "jdbc:mysql://localhost:3306/scheduler_db";

    private static final String USER = "root";  // Change if using another user

    private static final String PASSWORD = "mipassword";

    public DataBaseConnection(){

    }

    public static void connect(){

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            if (conn != null) {

                System.out.println("Connected to MySQL successfully!");

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

    }

    public void createTable (String tableName){

        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + "(\n"
                + "	id int PRIMARY KEY,\n"
                + "	name varchar(255) NOT NULL,\n"
                + "	abbreviation varchar(255) NOT NULL\n"
                + ");";

        try(Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);

            Statement stmt = conn.createStatement()
        ){
            stmt.executeUpdate(createTableSQL);
            System.out.println("Hemos creado bien la tabla");

        }

        catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void insertRows() {
        String createRowSQL = "A borrar";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);

             Statement stmt = conn.createStatement()
        ) {
            stmt.executeUpdate(createRowSQL);
            System.out.println("Hemos creado bien la fila");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

}


