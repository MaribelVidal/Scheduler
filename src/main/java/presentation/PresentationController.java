package presentation;

import business.BusinessController;
import business.Schedule;
import business.StudentGroup;
import business.Teacher;

import java.util.List;

public class PresentationController {




    private Calendar calendar;
    private BusinessController businessController;

    public PresentationController() throws Exception {
        this.businessController = new BusinessController();
        this.calendar = new Calendar(this);


        // Constructor logic if needed
    }

    public void test() {
        businessController.testH();
    }

    public void startCalendar() {
        calendar.init(businessController.getTeachersNames(), businessController.getClassroomsNames(), businessController.getStudentGroupsNames(), businessController.getTPNames(), businessController.getSubjectsNames());
        calendar.setVisible(true);
    }

    public Schedule getTeacherSchedule(String teacherId, int Id) {
        return businessController.getTeacherSchedule(teacherId, Id);
    }
    public Schedule getClassroomSchedule(String classroomId, int Id) {
        return businessController.getClassroomSchedule(classroomId, Id);
    }
    public Schedule getStudentGroupSchedule(String studentGroupId, int Id) {
        return businessController.getStudentGroupSchedule(studentGroupId, Id);
    }

    // PresentationController: expose name→id lists, or a list of entities
    public List<business.Teacher> getTeachers() { return businessController.getTeachers(); }
    public List<business.Classroom> getClassrooms() { return businessController.getClassrooms(); }
    public List<StudentGroup> getStudentGroups() { return businessController.getStudentGroups(); }


    public void addNewTeacher (Teacher teacher) {
        businessController.addNewTeacher(teacher);
    }

    public void removeTeacher (String teacherId) {
        businessController.removeTeacher(teacherId);
    }

    public void addNewClassroom (business.Classroom classroom) {
        businessController.addNewClassroom(classroom);
    }
    public void removeClassroom (String classroomId) {
        businessController.removeClassroom(classroomId);
    }

    public void addNewStudentGroup (StudentGroup studentGroup) {
        businessController.addNewStudentGroup(studentGroup);
    }

    public void removeStudentGroup (String studentGroupId) {
        businessController.removeStudentGroup(studentGroupId);
    }
    public void addNewTimePeriod (business.TimePeriod timePeriod) {
        businessController.addNewTimePeriod(timePeriod);
    }
    public void removeTimePeriod (String timePeriodId) {
        businessController.removeTimePeriod(timePeriodId);
    }
    public void addNewSubject (business.Subject subject) {
        businessController.addNewSubject(subject);
    }
    public void removeSubject (String subjectId) {
        businessController.removeSubject(subjectId);
    }

    public void refreshTeachersUI() {
        if (calendar != null) {
            calendar.refreshEntities();
        }
    }


    public void updateTeacher(Teacher teacher) {
        businessController.updateTeacher(teacher);
        // If calendar open, refresh entities (e.g., name changed)
        if (calendar != null) {
            calendar.refreshEntities();
        }
    }

}
