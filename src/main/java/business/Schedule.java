package business;

import java.util.ArrayList;
import java.util.List;

public class Schedule {
    public List<String> assignments; // Example: storing assignments as strings

    public Schedule() {
        this.assignments = new ArrayList<>();
    }

    public void addAssignment(ScheduledUnit unit, Teacher teacher, Classroom classroom, TimePeriod timePeriod) {
        String assignment = String.format("%s: Teacher=%s, Classroom=%s, Time=%s",
                unit.toString(), teacher.getId(), classroom.getId(), timePeriod.getId());
        assignments.add(assignment);
    }

    public void printSchedule() {
        if (assignments.isEmpty()) {
            System.out.println("No assignments in the schedule.");
            return;
        }
        System.out.println("--- Generated Schedule ---");
        assignments.forEach(System.out::println);
        System.out.println("--------------------------");
    }
}