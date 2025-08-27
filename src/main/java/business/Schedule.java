package business;

import java.util.*;

public class Schedule {
    private String id;
    private String name;
    private List<String> assignments; // Example: storing assignments as strings
    private int achievedConditions; // número de condiciones cumplidas
    private int weightedConditions;// condiciones ponderadas cumplidas

    private List<Lesson> lessons; // Lista de lecciones asignadas

    public Schedule() {
        this.assignments = new ArrayList<>();
        this.achievedConditions = 0; //
        this.weightedConditions = 0;
        this.lessons= new ArrayList<>();//
        this.name = "Horario Generado"; // Default name
    }

    public int getAchievedConditions() {
        return achievedConditions;
    }

    public void setAchievedConditions(int achievedConditions) {
        this.achievedConditions = achievedConditions;
    }

    public int getWeightedConditions() {
        return weightedConditions;
    }

    public void setWeightedConditions(int weightedConditions) {
        this.weightedConditions = weightedConditions;
    }

    public void addAssignment(ScheduledUnit unit, Teacher teacher, Classroom classroom, TimePeriod timePeriod) {
        String assignment = String.format("%s: Teacher=%s, Classroom=%s, Time=%s",
                unit.toString(), teacher.getId(), classroom.getId(), timePeriod.getId());
        assignments.add(assignment);
        lessons.add(new Lesson(teacher, unit.getStudentGroup(), classroom, timePeriod, unit.getSubject()));
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

    public void calculateConditions(List<Teacher> teachers) {
        int totalAchieved = 0;
        int totalWeighted = 0;

        for (Teacher teacher : teachers) {
            List<Condition> preferredConditions = teacher.getPreferredConditions();
            List<Condition> unPreferredConditions = teacher.getUnPreferredConditions();

            int achieved = 0;
            int weighted = 0;

            // --- Preferred conditions: reward if fulfilled ---
            for (Condition condition : preferredConditions) {
                boolean matched = false;
                for (Lesson lesson : lessons) {
                    if (lesson.getTeacher().equals(teacher) &&
                            condition.getTimePeriod() != null &&
                            condition.getTimePeriod().equals(lesson.getTimePeriod())) {
                        matched = true;
                        break;
                    }
                }
                if (matched) {
                    achieved += 1;
                    weighted += condition.getWeight();
                }
            }

            // --- Unpreferred conditions: reward if avoided ---
            for (Condition condition : unPreferredConditions) {
                boolean violated = false;
                for (Lesson lesson : lessons) {
                    if (lesson.getTeacher().equals(teacher) &&
                            condition.getTimePeriod() != null &&
                            condition.getTimePeriod().equals(lesson.getTimePeriod())) {
                        violated = true;
                        break;
                    }
                }
                if (!violated) {
                    achieved += 1;
                    weighted += condition.getWeight();
                }
            }

            // --- Track totals ---
            teacher.setAchievedConditions(achieved);
            teacher.setWeightedConditions(weighted);

            totalAchieved += achieved;
            totalWeighted += weighted;

            System.out.println("Teacher: " + teacher.getId() +
                    ", Achieved Conditions: " + achieved +
                    ", Achieved Weighted Conditions: " + achieved +
                    ", Total Conditions Defined: " + weighted);
        }

        System.out.println("Total Achieved Weighted Conditions: " + totalAchieved);
        System.out.println("Total Weighted Conditions: " + totalWeighted);
    }

    public List<Lesson> getLessons() {
        return lessons;
    }


    public Lesson getLessonAt(TimePeriod period, String columnName) {
        for (Lesson lesson : lessons) {
            if (lesson.getTimePeriod().equals(period)) {
                // match column depending on type
                if (lesson.getTeacher().getId().equals(columnName) ||
                        lesson.getStudentGroup().getId().equals(columnName) ||
                        lesson.getClassroom().getId().equals(columnName)) {
                    return lesson;
                }
            }
        }
        return null;
    }

     public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }

    public String getId() {
        return id;
    }

    public void setId(String scheduleId) {
        this.id = scheduleId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addLesson(Lesson l) {
        if (this.lessons == null) {
            this.lessons = new ArrayList<>();
        }
        this.lessons.add(l);
    }
}