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
                + "	id varchar(50) PRIMARY KEY,\n"
                + "	name varchar(255) NOT NULL,\n"
                + "	abbreviation varchar(50) NOT NULL,\n"
                + "	email varchar(255), \n"
                + "	phone varchar(255), \n"
                + "	department varchar(50) ,\n"
               //+ " FOREIGN KEY (possibleSubjects) REFERENCES possibleSubjects(subjectId),\n"
                + "	role varchar(255), \n"
                + "	hoursWork int NOT NULL\n"
               // " FOREIGN KEY (subjectId) REFERENCES preferencedSubjects(id),\n"
               // " FOREIGN KEY (timePeriodId) REFERENCES preferencedTimePeriods(id),\n"
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
        public void createTeacherPossibleSubjectTable(){
            String createTableSQL = "CREATE TABLE IF NOT EXISTS possibleSubjects (\n"
                    + "    teacherId varchar(50) NOT NULL,\n"
                    + "    subjectId varchar(50) NOT NULL,\n"
                    + "    PRIMARY KEY (teacherId, subjectId),\n"
                    + "    FOREIGN KEY (teacherId) REFERENCES teachers(id),\n"
                    + "    FOREIGN KEY (subjectId) REFERENCES subjects(id)\n"
                    + ");";

            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(createTableSQL);
                System.out.println("Tabla teacher_possibleSubject creada correctamente");
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }

    public void createActivityTable () {

        String createTableSQL = "CREATE TABLE IF NOT EXISTS activities (\n"
                + "	id varchar(50) PRIMARY KEY,\n"
                + "	name varchar(255) NOT NULL,\n"
                + "	abbreviation varchar(50) NOT NULL,\n"
                + "	classroom varchar(50),\n"
                + " FOREIGN KEY (classroom) REFERENCES classrooms(id) \n"
                + ");";

        try (
                Statement stmt = connection.createStatement()
        ) {
            stmt.executeUpdate(createTableSQL);
            System.out.println("Hemos creado bien la tabla");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

        public void createTeachersInActivity(){
        String createTableSQL = "CREATE TABLE IF NOT EXISTS teachersInActivity (\n"
                + "    activityId varchar(50) NOT NULL,\n"
                + "    teacherId varchar(50) NOT NULL,\n"
                + "    PRIMARY KEY (activityId, teacherId),\n"
                + "    FOREIGN KEY (activityId) REFERENCES activities(id),\n"
                + "    FOREIGN KEY (teacherId) REFERENCES teachers(id)\n"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createTableSQL);
            System.out.println("Tabla teacher_possibleSubject creada correctamente");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

/*
    public void createTeacherPreferencedSubjectTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS teacher_preferencedSubject (\n"
                + "    teacherId int NOT NULL,\n"
                + "    subjectId int NOT NULL,\n"
                + "    PRIMARY KEY (teacherId, subjectId),\n"
                + "    FOREIGN KEY (teacherId) REFERENCES teachers(id),\n"
                + "    FOREIGN KEY (subjectId) REFERENCES subjects(id)\n"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createTableSQL);
            System.out.println("Tabla teacher_preferencedSubject creada correctamente");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }


public void createTeacherPreferencedTimePeriod() {
    String createTableSQL = "CREATE TABLE IF NOT EXISTS teacher_preferencedTimePeriod (\n"
            + "    teacherId int NOT NULL,\n"
            + "    subjectId int NOT NULL,\n"
            + "    PRIMARY KEY (teacherId, timePeriodId),\n"
            + "    FOREIGN KEY (teacherId) REFERENCES teachers(id),\n"
            + "    FOREIGN KEY (timePeriodId) REFERENCES timePeriods(id)\n"
            + ");";

    try (Statement stmt = connection.createStatement()) {
        stmt.executeUpdate(createTableSQL);
        System.out.println("Tabla teacher_preferencedTimePeriod creada correctamente");
    } catch(SQLException e) {
        e.printStackTrace();
    }
}

*/



    public void createTimePeriodTable (){

    String createTableSQL = "CREATE TABLE IF NOT EXISTS timePeriods (\n"
            + "	id varchar(50) PRIMARY KEY,\n"
            + "	weekDay int NOT NULL,\n"
            + "	initialHour TIME NOT NULL,\n"
            + "	finalHour TIME NOT NULL,\n"
            + "	idTeacher varchar(50) ,\n"
            + "	idSubject varchar(50) ,\n"
            + "	idStudentGroup varchar(50) ,\n"
            + "	idClassroom varchar(50) ,\n"

            + " FOREIGN KEY (idTeacher) REFERENCES teachers(id) ,\n"
            + " FOREIGN KEY (idSubject) REFERENCES subjects(id) ,\n"
            + " FOREIGN KEY (idStudentGroup) REFERENCES studentGroups(id) ,\n"
            + " FOREIGN KEY (idClassroom) REFERENCES classrooms(id) \n"

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


    public void createClassroomTable (){

        String createTableSQL = "CREATE TABLE IF NOT EXISTS classrooms (\n"
                + "	id varchar(50) PRIMARY KEY,\n"
                + "	name varchar(255) NOT NULL,\n"
                + "	abbreviation varchar(50) NOT NULL,\n"
                + "	classroomType varchar(255), \n"
                + "	capacity int NOT NULL\n"
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
                + "	id varchar(50) PRIMARY KEY,\n"
                + "	name varchar(255) NOT NULL,\n"
                + "	abbreviation varchar(50) NOT NULL,\n"
                + "	department varchar(50) ,\n"
                + "	course varchar(255), \n"
                + "	assignedClassroom varchar(50) ,\n"
                + " FOREIGN KEY (assignedClassroom) REFERENCES classrooms(id),\n"
                + "	weeklyAssignedHours int NOT NULL\n"
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

    public void createStudentGroupTable () {

        String createTableSQL = "CREATE TABLE IF NOT EXISTS studentGroups (\n"
                + "	id varchar(50) PRIMARY KEY,\n"
                + "	name varchar(255) NOT NULL,\n"
                + "	abbreviation varchar(50) NOT NULL,\n"
                + "	course varchar(255), \n"
                + "	assignedTutor varchar(50) ,\n"
                + "	FOREIGN KEY (assignedTutor) REFERENCES teachers(id),\n"
                + "	numberOfStudents int NOT NULL,\n"
                + "	weeklyGroupHours int NOT NULL\n"
                //+ "	FOREIGN KEY (subjects) REFERENCES studentGroupSubjects(id)\n"
                + ");";

        try (
                Statement stmt = connection.createStatement()
        ) {
            stmt.executeUpdate(createTableSQL);
            System.out.println("Hemos creado bien la tabla");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

        public void createStudentGroupSubjectTable() {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS studentGroupSubjects (\n"
                    + "    groupId varchar(50) NOT NULL,\n"
                    + "    subjectId varchar(50) NOT NULL,\n"
                    + "    PRIMARY KEY (groupId, subjectId),\n"
                    + "    FOREIGN KEY (groupId) REFERENCES studentGroups(id),\n"
                    + "    FOREIGN KEY (subjectId) REFERENCES subjects(id)\n"
                    + ");";

            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate(createTableSQL);
                System.out.println("Tabla studentgroup_subject creada correctamente");
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }




    public void createAllTables(){
        createClassroomTable ();
        createActivityTable();
        createTeacherTable ();
        createTeachersInActivity();
        createSubjectTable ();
        createTeacherPossibleSubjectTable();

        //createTeacherPreferencedSubjectTable();
        //createTeacherPreferencedTimePeriod();



        createStudentGroupTable ();
        createTimePeriodTable ();
        createStudentGroupSubjectTable();
       }

}



