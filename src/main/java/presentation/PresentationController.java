package presentation;

import business.*;

import java.util.List;
import java.util.Map;

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
        businessController.regenerateSchedules();
    }

    public List<Subject> getTeacherPreferredSubjects(String teacherId) {
        return businessController.getTeacherPreferredSubjects(teacherId);
    }


    public List<StudentGroup> getTeacherPreferredStudentGroups(String teacherId) {
        return businessController.getTeacherPreferredStudentGroups(teacherId);
    }

    public List<StudentGroup> getTeacherUnpreferredStudentGroups(String teacherId) {
        return businessController.getTeacherUnpreferredStudentGroups(teacherId);
    }

    public List<TimePeriod> getTeacherPreferredTimePeriods(String teacherId) {
        return businessController.getTeacherPreferredTimePeriods(teacherId);
    }

    public List<TimePeriod> getTeacherUnpreferredTimePeriods(String teacherId) {
        return businessController.getTeacherUnpreferredTimePeriods(teacherId);
    }

    public List<TimePeriod> getTeacherUnavailableTimePeriods(String teacherId) {
        return businessController.getTeacherUnavailableTimePeriods(teacherId);
    }

    public void deleteTeacher(String id) {
        businessController.deleteTeacher(id);
    }

    public void addTeacherPreferredSubject(String currentTeacherId, String id, int w) {
        businessController.addTeacherPreferredSubject(currentTeacherId, id, w);
    }

    public void removeTeacherPreferredSubject(String currentTeacherId, String subjectId) {
        businessController.removeTeacherPreferredSubject(currentTeacherId, subjectId);
    }

    public Map<String, Integer> getTeacherPreferredSubjectWeights(String teacherId) {
        return businessController.getTeacherPreferredSubjectWeights(teacherId);
    }

    public Map<String, Integer> getTeacherPreferredStudentGroupWeights(String teacherId) {
        return businessController.getTeacherPreferredStudentGroupWeights(teacherId);
    }

    public Map<String, Integer> getTeacherUnpreferredStudentGroupWeights(String teacherId) {
        return businessController.getTeacherUnpreferredStudentGroupWeights(teacherId);
    }

    public Map<String, Integer> getTeacherPreferredTimePeriodWeights(String teacherId) {
        return businessController.getTeacherPreferredTimePeriodWeights(teacherId);
    }

    public Map<String, Integer> getTeacherUnpreferredTimePeriodWeights(String teacherId) {
        return businessController.getTeacherUnpreferredTimePeriodWeights(teacherId);
    }

    public void addTeacherPreferredStudentGroup(String currentTeacherId, String id, int w) {
        businessController.addTeacherPreferredStudentGroup(currentTeacherId, id, w);
    }

    public void addTeacherUnpreferredStudentGroup(String currentTeacherId, String id, int w) {
        businessController.addTeacherUnpreferredStudentGroup(currentTeacherId, id, w);
    }

    public void addTeacherPreferredTimePeriod(String currentTeacherId, String id, int w) {
        businessController.addTeacherPreferredTimePeriod(currentTeacherId, id, w);
    }

    public void addTeacherUnpreferredTimePeriod(String currentTeacherId, String id, int w) {
        businessController.addTeacherUnpreferredTimePeriod(currentTeacherId, id, w);
    }

    public void addTeacherUnavailableTimePeriod(String currentTeacherId, String id) {
        businessController.addTeacherUnavailableTimePeriod(currentTeacherId, id);
    }

    public void removeTeacherPreferredStudentGroup(String currentTeacherId, String groupId) {
        businessController.removeTeacherPreferredStudentGroup(currentTeacherId, groupId);
    }

    public void removeTeacherUnpreferredStudentGroup(String currentTeacherId, String groupId) {
        businessController.removeTeacherUnpreferredStudentGroup(currentTeacherId, groupId);
    }

    public void removeTeacherPreferredTimePeriod(String currentTeacherId, String tpId) {
        businessController.removeTeacherPreferredTimePeriod(currentTeacherId, tpId);
    }

    public void removeTeacherUnpreferredTimePeriod(String currentTeacherId, String tpId) {
        businessController.removeTeacherUnpreferredTimePeriod(currentTeacherId, tpId);
    }

    public void removeTeacherUnavailableTimePeriod(String currentTeacherId, String tpId) {
        businessController.removeTeacherUnavailableTimePeriod(currentTeacherId, tpId);
    }

    public void updateTeacherPreferredSubjectWeight(String currentTeacherId, String id, int w) {
        businessController.updateTeacherPreferredSubjectWeight(currentTeacherId, id, w);
    }

    public void updateTeacherPreferredStudentGroupWeight(String currentTeacherId, String id, int w) {
        businessController.updateTeacherPreferredStudentGroupWeight(currentTeacherId, id, w);
    }

    public void updateTeacherUnpreferredStudentGroupWeight(String currentTeacherId, String id, int w) {
        businessController.updateTeacherUnpreferredStudentGroupWeight(currentTeacherId, id, w);
    }

    public void updateTeacherPreferredTimePeriodWeight(String currentTeacherId, String id, int w) {
        businessController.updateTeacherPreferredTimePeriodWeight(currentTeacherId, id, w);
    }

    public void updateTeacherUnpreferredTimePeriodWeight(String currentTeacherId, String id, int w) {
        businessController.updateTeacherUnpreferredTimePeriodWeight(currentTeacherId, id, w);
    }
}
