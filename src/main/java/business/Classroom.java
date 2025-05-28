package business;

import java.util.ArrayList;
import java.util.List;

public class Classroom extends Entity{


    private String classroomType; //dedicada o general
    private int capacity;
    private List<Subject> assignedSubjects; // Asignaturas asignadas a esta aula


    public Classroom(String id, String name, String abbreviation) {
        super(id, name, abbreviation);
        this.capacity = 30;
        this.assignedSubjects = new ArrayList<>();// Default capacity
    }

    public String getClassroomType() {
        return classroomType;
    }

    public void setClassroomType(String classroomType) {
        this.classroomType = classroomType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<Subject> getAssignedSubjects() {
        return assignedSubjects;
    }

    public void setAssignedSubjects(List<Subject> assignedSubjects) {
        this.assignedSubjects = assignedSubjects;
    }
    public void addAssignedSubject(Subject subject) {
        this.assignedSubjects.add(subject);
    }
    public boolean isAvailableForSubject(Subject subject) {
        // Check if the classroom is available for the given subject
        if (assignedSubjects == null || assignedSubjects.isEmpty()) {
            return true; // If no subjects are assigned, the classroom is available
        }
        return !assignedSubjects.contains(subject);
    }
}
