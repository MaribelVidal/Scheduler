package presentation;

import business.*;

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

    public Schedule getTeacherSchedule(String teacherId, String Id) {
        return businessController.getTeacherSchedule(teacherId, Id);
    }
    public Schedule getClassroomSchedule(String classroomId, String Id) {
        return businessController.getClassroomSchedule(classroomId, Id);
    }
    public Schedule getStudentGroupSchedule(String studentGroupId, String Id) {
        return businessController.getStudentGroupSchedule(studentGroupId, Id);
    }

    // PresentationController: expose name→id lists, or a list of entities
    public List<Teacher> getTeachers() { return businessController.getTeachers(); }
    public List<Classroom> getClassrooms() { return businessController.getClassrooms(); }
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
    // Back-compat shim for older panels
    public void refreshTeachersUI() {
        // If you added refreshAllUI() earlier, reuse it:
        refreshAllUI();
    }

    public List<Subject> getSubjects() {
        return businessController.getSubjects();
    }



    public void registerCalendar(presentation.Calendar calendar) {
        this.calendar = calendar;
    }

    /** Notify the Calendar UI to refresh combos and table after data changes. */
    public void refreshAllUI() {
        if (this.calendar != null) {
            this.calendar.refreshEntities();
        }
    }

    public void updateClassroom(Classroom c) {
        businessController.updateClassroom(c);
    }

    public void updateTeacher(Teacher t) {
        businessController.updateTeacher(t);
    }

    public void updateStudentGroup(StudentGroup g) {
        businessController.updateStudentGroup(g);
    }

    public void updateSubject(Subject s) {
        businessController.updateSubject(s);
    }

    public List<TimePeriod> getTimePeriods() {
        return businessController.getTimePeriods();
    }

    public void updateTimePeriod(TimePeriod tp) {
        businessController.updateTimePeriod(tp);
    }

    public List<String> getScheduleIds() {
        return businessController.getScheduleIds();
    }

    public void addTeacher(Teacher t) {
        businessController.addTeacher(t);
    }


    public List<Schedule> getAllSchedules() {
        return businessController.getAllSchedules();
    }

    public void renameSchedule(String id, String name) {
        businessController.renameSchedule(id, name);
    }

    public void regenerateSchedules() {
        businessController.regenarateSchedules();
    }
}
