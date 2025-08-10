package presentation;

import business.BusinessController;
import business.Schedule;

public class PresentationController {




    private Calendar calendar;
    private BusinessController businessController;

    public PresentationController() throws Exception {
        this.businessController = new BusinessController();
        this.calendar = new Calendar();



        // Constructor logic if needed
    }

    public void startCalendar() {
        calendar.init(businessController.getTeachersNames(), businessController.getClassroomsNames(), businessController.getStudentGroupsNames(), businessController.getTPNames());

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



}
