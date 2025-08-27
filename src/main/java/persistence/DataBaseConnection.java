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
    private static Connection connection;




    public DataBaseConnection(){

    }

    public static synchronized void connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to MySQL successfully!");
        }
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) connect();
        return connection;
    }

    public synchronized void close() {
        if (connection != null) try { connection.close(); } catch (SQLException ignored) {}
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
        public void createTeacherPossibleSubjectTable(){
            String createTableSQL = "CREATE TABLE IF NOT EXISTS teacherPossibleSubjects (\n"
                    + "    teacherId varchar(50) NOT NULL,\n"
                    + "    subjectId varchar(50) NOT NULL,\n"
                    + "    PRIMARY KEY (teacherId, subjectId),\n"
                    + "    FOREIGN KEY (teacherId) REFERENCES teachers(id),\n"
                    + "    FOREIGN KEY (subjectId) REFERENCES subjects(id)\n"
                    + ");";

            try (Statement stmt = getConnection().createStatement()) {
                stmt.executeUpdate(createTableSQL);
                System.out.println("Tabla TeacherPossibleSubjects creada correctamente");
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
/*
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
*/
    public void createTeacherAssignedSchedulesTable() {
    String createTableSQL = "CREATE TABLE IF NOT EXISTS teacherAssignedSchedules (\n"
            + "    teacherId varchar(50) NOT NULL,\n"
            + "    scheduleId varchar(50) NOT NULL,\n"
            + "    PRIMARY KEY (teacherId, scheduleId),\n"
            + "    FOREIGN KEY (teacherId) REFERENCES teachers(id),\n"
            + "    FOREIGN KEY (scheduleId) REFERENCES schedules(id)\n"

            + ");";

    try (Statement stmt = getConnection().createStatement()) {
        stmt.executeUpdate(createTableSQL);
        System.out.println("Tabla teacherAssignedSchedules creada correctamente");
    } catch(SQLException e) {
        e.printStackTrace();
    }
}

    public void createTeacherPreferredConditionsTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS teacherPreferredConditions (\n"
                + "    teacherId varchar(50) NOT NULL,\n"
                + "    conditionId varchar(50) NOT NULL,\n"
                + "    PRIMARY KEY (teacherId, conditionId),\n"
                + "    FOREIGN KEY (teacherId) REFERENCES teachers(id),\n"
                + "    FOREIGN KEY (conditionId) REFERENCES conditions(id)\n"
                + ");";

        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate(createTableSQL);
            System.out.println("Tabla teacherPreferredConditions creada correctamente");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTeacherUnpreferredConditionsTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS teacherUnpreferredConditions (\n"
                + "    teacherId varchar(50) NOT NULL,\n"
                + "    conditionId varchar(50) NOT NULL,\n"
                + "    PRIMARY KEY (teacherId, conditionId),\n"
                + "    FOREIGN KEY (teacherId) REFERENCES teachers(id),\n"
                + "    FOREIGN KEY (conditionId) REFERENCES conditions(id)\n"
                + ");";

        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate(createTableSQL);
            System.out.println("Tabla teacherUnpreferredConditions creada correctamente");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTeacherUnavailableTimePeriodsTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS teacherUnavailableTimePeriods (\n"
                + "    teacherId varchar(50) NOT NULL,\n"
                + "    timePeriodId varchar(50) NOT NULL,\n"
                + "    PRIMARY KEY (teacherId, timePeriodId),\n"
                + "    FOREIGN KEY (teacherId) REFERENCES teachers(id),\n"
                + "    FOREIGN KEY (timePeriodId) REFERENCES timePeriods(id)\n"
                + ");";

        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate(createTableSQL);
            System.out.println("Tabla teacherUnavailableTimePeriods creada correctamente");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }





    public void createTimePeriodTable (){

    String createTableSQL = "CREATE TABLE IF NOT EXISTS timePeriods (\n"
            + "	id varchar(50) PRIMARY KEY,\n"
            + "	weekDay varchar(50) NOT NULL,\n"
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


    public void createClassroomAssignedSubjectsTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS classroomAssignedSubjects (\n"
                + "    classroomId varchar(50) NOT NULL,\n"
                + "    subjectId varchar(50) NOT NULL,\n"
                + "    PRIMARY KEY (classroomId, subjectId),\n"
                + "    FOREIGN KEY (classroomId) REFERENCES classrooms(id),\n"
                + "    FOREIGN KEY (subjectId) REFERENCES subjects(id)\n"
                + ");";

        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate(createTableSQL);
            System.out.println("Tabla classroomAssignedSubjects creada correctamente");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void createClassroomAssignedSchedulesTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS classroomAssignedSchedules (\n"
                + "    classroomId varchar(50) NOT NULL,\n"
                + "    scheduleId varchar(50) NOT NULL,\n"
                + "    PRIMARY KEY (classroomId, scheduleId),\n"
                + "    FOREIGN KEY (classroomId) REFERENCES classrooms(id),\n"
                + "    FOREIGN KEY (scheduleId) REFERENCES schedules(id)\n"
                + ");";

        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate(createTableSQL);
            System.out.println("Tabla classroomAssignedSchedules creada correctamente");
        } catch(SQLException e) {
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
                + "	weeklyAssignedHours int NOT NULL, \n"
                + "	duration int NOT NULL, \n"
                + "	maxDailyHours int NOT NULL, \n"
                + "	assignedClassroom varchar(50) ,\n"
                + " FOREIGN KEY (assignedClassroom) REFERENCES classrooms(id) \n"

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
                + "	weeklyGroupHours int NOT NULL ,\n"
                + "	numberOfStudents int NOT NULL\n"

                + ");";

        try (
                Statement stmt = getConnection().createStatement()
        ) {
            stmt.executeUpdate(createTableSQL);
            System.out.println("Hemos creado bien la tabla");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

        public void createStudentGroupRequiredSubjectsTable() {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS studentGroupRequiredSubjects (\n"
                    + "    studentGroupId varchar(50) NOT NULL,\n"
                    + "    subjectId varchar(50) NOT NULL,\n"
                    + "    PRIMARY KEY (studentGroupId, subjectId),\n"
                    + "    FOREIGN KEY (studentGroupId) REFERENCES studentGroups(id),\n"
                    + "    FOREIGN KEY (subjectId) REFERENCES subjects(id)\n"
                    + ");";

            try (Statement stmt = getConnection().createStatement()) {
                stmt.executeUpdate(createTableSQL);
                System.out.println("Tabla studentGroupRequiredSubjects creada correctamente");
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }


        public void createStudentGroupAssignedSchedulesTable() {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS studentGroupAssignedSchedules (\n"
                    + "    studentGroupId varchar(50) NOT NULL,\n"
                    + "    scheduleId varchar(50) NOT NULL,\n"
                    + "    PRIMARY KEY (studentGroupId, scheduleId),\n"
                    + "    FOREIGN KEY (studentGroupId) REFERENCES studentGroups(id),\n"
                    + "    FOREIGN KEY (scheduleId) REFERENCES schedules(id)\n"
                    + ");";

            try (Statement stmt = getConnection().createStatement()) {
                stmt.executeUpdate(createTableSQL);
                System.out.println("Tabla studentGroupAssignedSchedules creada correctamente");
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }

        public void createScheduleTable () {

            String createTableSQL = "CREATE TABLE IF NOT EXISTS schedules (\n"
                    + "	id varchar(50) PRIMARY KEY,\n"
                    + "	name varchar(255) NOT NULL,\n"
                    + "	achievedConditions int NOT NULL ,\n"
                    + "	weightedConditions int NOT NULL \n"

                    + ");";

            try (
                    Statement stmt = getConnection().createStatement()
            ) {
                stmt.executeUpdate(createTableSQL);
                System.out.println("Hemos creado bien la tabla");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void createScheduleAssignedLessonsTable() {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS scheduleAssignedLessons (\n"
                    + "    scheduleId varchar(50) NOT NULL,\n"
                    + "    lessonId varchar(50) NOT NULL,\n"
                    + "    PRIMARY KEY (scheduleId, lessonId),\n"
                    + "    FOREIGN KEY (scheduleId) REFERENCES schedules(id),\n"
                    + "    FOREIGN KEY (lessonId) REFERENCES lessons(id)\n"
                    + ");";

            try (Statement stmt = getConnection().createStatement()) {
                stmt.executeUpdate(createTableSQL);
                System.out.println("Tabla scheduleAssignedLessons creada correctamente");
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }


        public void createLessonTable () {

            String createTableSQL = "CREATE TABLE IF NOT EXISTS lessons (\n"
                    + "	id varchar(50) PRIMARY KEY,\n"
                    + "	idTeacher varchar(255) NOT NULL,\n"
                    + "	idStudentGroup varchar(255) NOT NULL,\n"
                    + "	idSubject varchar(255) NOT NULL,\n"
                    + "	idClassroom varchar(255) NOT NULL,\n"
                    + "	idTimePeriod varchar(255) NOT NULL,\n"
                    + " FOREIGN KEY (idTeacher) REFERENCES teachers(id),\n"
                    + " FOREIGN KEY (idStudentGroup) REFERENCES studentGroups(id),\n"
                    + " FOREIGN KEY (idSubject) REFERENCES subjects(id),\n"
                    + " FOREIGN KEY (idClassroom) REFERENCES classrooms(id),\n"
                    + " FOREIGN KEY (idTimePeriod) REFERENCES timePeriods(id)\n"

                    + ");";

            try (
                    Statement stmt = getConnection().createStatement()
            ) {
                stmt.executeUpdate(createTableSQL);
                System.out.println("Hemos creado bien la tabla");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    public void createConditionTable () {
        String createTableSQL =
                "CREATE TABLE IF NOT EXISTS conditions (\n" +
                        "  id varchar(50) PRIMARY KEY,\n" +
                        "  teacher varchar(255) NOT NULL,\n" +
                        "  weight int NOT NULL,\n" +
                        "  subject varchar(255) NULL,\n" +
                        "  timePeriod varchar(255) NULL,\n" +
                        "  studentGroup varchar(255) NULL,\n" +
                        "  FOREIGN KEY (teacher) REFERENCES teachers(id),\n" +
                        "  FOREIGN KEY (subject) REFERENCES subjects(id),\n" +
                        "  FOREIGN KEY (studentGroup) REFERENCES studentGroups(id),\n" +
                        "  FOREIGN KEY (timePeriod) REFERENCES timePeriods(id)\n" +
                        ");";

        try (Statement stmt = getConnection().createStatement()) {
            stmt.executeUpdate(createTableSQL);
            System.out.println("Tabla conditions creada correctamente");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





    public void createAllTables(){
        createClassroomTable ();
        createTeacherTable ();
        createSubjectTable ();
        createStudentGroupTable ();
        createTimePeriodTable ();
        createScheduleTable();
        createLessonTable();
        createConditionTable();
        createClassroomAssignedSubjectsTable();
        createClassroomAssignedSchedulesTable();
        createStudentGroupRequiredSubjectsTable();
        createStudentGroupAssignedSchedulesTable();
        createTeacherPossibleSubjectTable();
        createTeacherAssignedSchedulesTable();
        createTeacherPreferredConditionsTable();
        createTeacherUnpreferredConditionsTable();
        createTeacherUnavailableTimePeriodsTable();
        createScheduleAssignedLessonsTable();

       }

}



