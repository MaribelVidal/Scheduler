package business;

import java.util.ArrayList;
import java.util.List;

public class Schedule {
    private List<String> assignments; // Example: storing assignments as strings
    private int achievedConditions; // número de condiciones cumplidas
    private int weightedConditions;// condiciones ponderadas cumplidas

    private List<Lesson> lessons; // Lista de lecciones asignadas

    public Schedule() {
        this.assignments = new ArrayList<>();
        this.achievedConditions = 0; //
        this.weightedConditions = 0;
        this.lessons= new ArrayList<>();//
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

        for (Teacher teacher : teachers) {

            List<Condition> preferredConditions = teacher.getPreferredConditions();
            List<Condition> unPreferredConditions = teacher.getUnPreferredConditions();

            int teacherAchievedConditions = unPreferredConditions.size();
            int teacherWeightedConditions = 0;
            for (Condition condition : unPreferredConditions) {
                teacherWeightedConditions += condition.getWeight();// Reiniciar el contador de condiciones cumplidas
            }
            for (Lesson lesson : lessons) {
                for (Condition condition : preferredConditions) {
                    if (condition.getTeacher().equals(lesson.getTeacher()) &&
                            (condition.getSubject().equals(lesson.getSubject()) ||
                                    condition.getTimePeriod().equals(lesson.getTimePeriod()) ||
                                    condition.getStudentGroup().equals(lesson.getStudentGroup()))) {
                        teacherAchievedConditions++;
                        teacherWeightedConditions += condition.getWeight();
                    }
                }
                for (Condition condition : unPreferredConditions) {
                    if (condition.getTeacher().equals(lesson.getTeacher()) &&
                            (condition.getSubject().equals(lesson.getSubject()) ||
                                    condition.getTimePeriod().equals(lesson.getTimePeriod()) ||
                                    condition.getStudentGroup().equals(lesson.getStudentGroup()))) {
                        teacherAchievedConditions--;
                        teacherWeightedConditions -= condition.getWeight();// Aquí podrías manejar las condiciones no preferidas, por ejemplo, restando puntos
                        // achievedConditions--; // Si se desea penalizar por condiciones no preferidas
                    }
                }


                // Ejemplo simple, se puede ajustar según la lógica real
            }

            achievedConditions = this.achievedConditions + teacherAchievedConditions;
            weightedConditions = this.weightedConditions + teacherWeightedConditions;
            teacher.setAchievedConditions(teacherAchievedConditions);
            teacher.setWeightedConditions(teacherWeightedConditions);
            System.out.println("Teacher: " + teacher.getId() +
                    ", Achieved Conditions: " + teacherAchievedConditions +
                    ", Weighted Conditions: " + teacherWeightedConditions);




        }
        System.out.println("Total Achieved Conditions: " + achievedConditions);
        System.out.println("Total Weighted Conditions: " + weightedConditions);
    }
}