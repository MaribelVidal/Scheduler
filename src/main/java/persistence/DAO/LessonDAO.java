package persistence.DAO;


import business.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LessonDAO implements DAO<Lesson>{
    private Connection connection;

    TeacherDAO teacherDAO;
    StudentGroupDAO studentGroupDAO;
    SubjectDAO subjectDAO;
    ClassroomDAO classroomDAO;
    TimePeriodDAO timePeriodDAO;

    public LessonDAO(Connection connection) {
        this.connection = connection;
    }

    // setters for wiring after construction
    public void setTeacherDAO(TeacherDAO dao) { this.teacherDAO = dao; }
    public void setStudentGroupDAO(StudentGroupDAO dao) { this.studentGroupDAO = dao; }
    public void setSubjectDAO(SubjectDAO dao) { this.subjectDAO = dao; }
    public void setClassroomDAO(ClassroomDAO dao) { this.classroomDAO = dao; }
    public void setTimePeriodDAO(TimePeriodDAO dao) { this.timePeriodDAO = dao; }

    @Override
    public void add(Lesson lesson) throws SQLException {
        String query = "INSERT INTO lessons (id, idTeacher, idStudentGroup, idSubject, idClassroom, idTimePeriod) VALUES (?, ?, ?, ?, ?, ?)";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, lesson.getId());
            preparedStatement.setString(2,lesson.getTeacher().getId());
            preparedStatement.setString(3,lesson.getStudentGroup().getId());
            preparedStatement.setString(4,lesson.getSubject().getId());
            preparedStatement.setString(5,lesson.getClassroom().getId());
            preparedStatement.setString(6,lesson.getTimePeriod().getId());

            preparedStatement.executeUpdate();
        }

    }

    @Override
    public void update(Lesson lesson) throws SQLException {
        String query = "UPDATE lessons SET idTeacher = ?, idStudentGroup = ?, idSubject = ?, idClassroom = ?, idTimePeriod = ? WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1,lesson.getTeacher().getId());
            preparedStatement.setString(2,lesson.getStudentGroup().getId());
            preparedStatement.setString(3,lesson.getSubject().getId());
            preparedStatement.setString(4,lesson.getClassroom().getId());
            preparedStatement.setString(5,lesson.getTimePeriod().getId());
            preparedStatement.setString(6,lesson.getId());

            preparedStatement.executeUpdate();
        }
    }

    @Override
    public List<Lesson> getAll() throws SQLException{
        List<Lesson> lessons = new ArrayList<>();
        String query = "SELECT * FROM lessons";
        try(Statement createStatement = connection.createStatement();
            ResultSet resultset = createStatement.executeQuery(query)) {

            while (resultset.next()) {
                String idLesson = resultset.getString("id");
                String idTeacher = resultset.getString("idTeacher");
                String idStudentGroup = resultset.getString("idStudentGroup");
                String idSubject = resultset.getString("idSubject");
                String idClassroom = resultset.getString("idClassroom");
                String idTimePeriod = resultset.getString("idTimePeriod");

                Teacher teacher =teacherDAO.getOne(idTeacher);
                StudentGroup studentGroup = studentGroupDAO.getOne(idStudentGroup);
                Subject subject = subjectDAO.getOne(idSubject);
                Classroom classroom = classroomDAO.getOne(idClassroom);
                TimePeriod timePeriod = timePeriodDAO.getOne(idTimePeriod);

                Lesson lesson = new Lesson(teacher, studentGroup,  classroom,  timePeriod,  subject);
                lesson.setId(idLesson);

                lessons.add(lesson);
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return lessons;

    }

    @Override
    public Lesson getOne(String lessonId) throws SQLException {
        //Subject subject = new Subject<>();
        String query = "SELECT * FROM lessons WHERE id = ? ";


        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, lessonId);

            ResultSet resultset = preparedStatement.executeQuery();
            if (resultset.next()) {
                String idTeacher = resultset.getString("idTeacher");
                String idStudentGroup = resultset.getString("idStudentGroup");
                String idSubject = resultset.getString("idSubject");
                String idClassroom = resultset.getString("idClassroom");
                String idTimePeriod = resultset.getString("idTimePeriod");



                Teacher teacher =teacherDAO.getOne(idTeacher);
                StudentGroup studentGroup = studentGroupDAO.getOne(idStudentGroup);
                Subject subject = subjectDAO.getOne(idSubject);
                Classroom classroom = classroomDAO.getOne(idClassroom);
                TimePeriod timePeriod = timePeriodDAO.getOne(idTimePeriod);

                Lesson lesson = new Lesson(teacher, studentGroup,  classroom,  timePeriod,  subject);
                lesson.setId(lessonId);


                return lesson;
            }else {
                return null;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void delete (Lesson lesson) throws SQLException{
        String query = "DELETE FROM lessons WHERE id = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,lesson.getId());
            preparedStatement.executeUpdate();
        }

    }


}

