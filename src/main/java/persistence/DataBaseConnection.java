package persistence;

import business.Classroom;
import business.Department;

import java.sql.Connection;

import java.sql.DriverManager;

import java.sql.SQLException;
import java.sql.Statement;

public class DataBaseConnection {

    // Database connection details

    private static final String URL = "jdbc:mysql://localhost:3306/scheduler_db";

    private static final String USER = "root";  // Change if using another user

    private static final String PASSWORD = "mipassword";

    private Connection connection;




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

    public Connection getConnection() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
        return connection;
    }

    public void closeConnection() throws SQLException {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }


    public void createTeacherTable (){

        String createTableSQL = "CREATE TABLE IF NOT EXISTS teachers (\n"
                + "	id int PRIMARY KEY,\n"
                + "	name varchar(255) NOT NULL,\n"
                + "	abbreviation varchar(50) NOT NULL\n"
                + "	email varchar(255) \n"
                + "	phone varchar(255) \n"
                + "	hoursWork int NOT NULL\n"
                + ");";

        try(
            Statement stmt = connection.createStatement()
        ){
            stmt.executeUpdate(createTableSQL);
            System.out.println("Hemos creado bien la tabla");

        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }


    /*

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
    */

    public void createClassroomTable (){

        String createTableSQL = "CREATE TABLE IF NOT EXISTS classrooms (\n"
                + "	id int PRIMARY KEY,\n"
                + "	name varchar(255) NOT NULL,\n"
                + "	abbreviation varchar(50) NOT NULL\n"
                + "	classroomType varchar(255) \n"
                + "	capacity varchar(255) \n"
                + ");";


        try(
                Statement stmt = connection.createStatement()
        ){
            stmt.executeUpdate(createTableSQL);
            System.out.println("Hemos creado bien la tabla");

        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void createStudentGroupTable (){

        String createTableSQL = "CREATE TABLE IF NOT EXISTS teachers (\n"
                + "	id int PRIMARY KEY,\n"
                + "	name varchar(255) NOT NULL,\n"
                + "	abbreviation varchar(50) NOT NULL\n"
                + "	course varchar(255) \n"
                + "	assignedTutor varchar(255) \n"
                + "	weeklyGroupHours int NOT NULL\n"
                + "	NumberOfStudents int NOT NULL\n"
                + ");";


        try(
                Statement stmt = connection.createStatement()
        ){
            stmt.executeUpdate(createTableSQL);
            System.out.println("Hemos creado bien la tabla");

        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void createSubjectTable (){

        String createTableSQL = "CREATE TABLE IF NOT EXISTS subjects (\n"
                + "	id int PRIMARY KEY,\n"
                + "	name varchar(255) NOT NULL,\n"
                + "	abbreviation varchar(50) NOT NULL\n"
                + "	departmentId int \n"
                + "	course varchar(255) \n"
                + "	weeklyAssignedHours int NOT NULL\n"
                + "	classroomId int NOT NULL\n"
                + " FOREIGN KEY (departmentId) REFERENCES departments(id),\n"
                + " FOREIGN KEY (classroomId) REFERENCES classrooms(id)\n"
                + ");";


        try(
                Statement stmt = connection.createStatement()
        ){
            stmt.executeUpdate(createTableSQL);
            System.out.println("Hemos creado bien la tabla");

        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void createTeacherSubjectTable (){

        String createTableSQL = "CREATE TABLE IF NOT EXISTS teacherSubject (\n"
                + "	teacherId int NOT NULL,\n"
                + "	subjectId int NOT NULL,\n"
                + " PRIMARY KEY (teacherId, subjectId),\n"
                + " FOREIGN KEY (teacherId) REFERENCES teachers(id),\n"
                + " FOREIGN KEY (subjectId) REFERENCES subjects(id)\n"
                + ");";


        try(
                Statement stmt = connection.createStatement()
        ){
            stmt.executeUpdate(createTableSQL);
            System.out.println("Hemos creado bien la tabla");

        }
        catch(SQLException e){
            e.printStackTrace();
        }
    }

}


