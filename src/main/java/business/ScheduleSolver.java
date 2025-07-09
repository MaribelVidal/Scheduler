package business;// generate csp solution for a schedule creator, knowing the varaibles are in Subject, Classroom, StudentGroup, Teacher, and TimePeriod java classes

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
//import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;


public class ScheduleSolver {

    private List<Teacher> teachers;
    private List<Classroom> classrooms;
    private List<StudentGroup> studentGroups;
    private List<TimePeriod> timePeriods;

    private Model model;

    private List<ScheduledUnit> scheduledUnits;

    private int numUnits;
    private int numTeachers;
    private int numClassrooms;
    private int numTimePeriods;

    private IntVar[] unitTeacherVars;
    private IntVar[] unitClassroomVars;
    private IntVar[] unitTimePeriodVars;

    //Constructor
    public ScheduleSolver( List<Teacher> teachers, List<Classroom> classrooms, List<StudentGroup> studentGroups, List<TimePeriod> timePeriods)  {
        this.teachers = teachers;
        this.classrooms = classrooms;
        this.studentGroups = studentGroups;
        this.timePeriods = timePeriods;
        this.model = new Model("SchoolScheduler");
        createScheduleUnits();

        this.numUnits = scheduledUnits.size();
        this.numTeachers = teachers.size();
        this.numClassrooms = classrooms.size();
        this.numTimePeriods = timePeriods.size();


    }

    // Method to create schedule units based on student groups and subjects
    public void createScheduleUnits(){
        // A ScheduledUnit is a specific StudentGroup needing a specific Subject.
        List<ScheduledUnit> scheduledUnits = new ArrayList<>();
        int unitIdCounter = 0;
        for (StudentGroup sg : studentGroups) {
            if (sg.getRequiredSubjects() != null) {
                for (Subject subj : sg.getRequiredSubjects()) {
                    for (int i = 0; i < subj.getWeeklyAssignedHours(); i++) {
                        // Create a ScheduledUnit for each subject lesson required by the student group
                        // Each unit is uniquely identified by a combination of student group and subject
                        // The unitIdCounter ensures unique IDs for each ScheduledUnit
                        System.out.println("Creating unit for StudentGroup: " + sg.getId() + ", Subject: " + subj.getId() + ", Unit ID: " + unitIdCounter);
                    scheduledUnits.add(new ScheduledUnit(sg, subj, unitIdCounter++));
                    }
                }
            }
        }

        if (scheduledUnits.isEmpty()) {
            System.out.println("No units to schedule (e.g., no student groups or no required subjects defined).");

        }
        if (teachers.isEmpty() || classrooms.isEmpty() || timePeriods.isEmpty()) {
            System.err.println("Error: Teachers, classrooms, or time periods list is empty. Cannot create a schedule.");

        }
        this.scheduledUnits = scheduledUnits;



    }

    public void defineVariables(){
        // 2. Define solver variables for each ScheduledUnit

        this.unitTeacherVars = model.intVarArray("unit_teacher", numUnits, 0, numTeachers - 1);
        this.unitClassroomVars = model.intVarArray("unit_classroom", numUnits, 0, numClassrooms - 1);
        this.unitTimePeriodVars = model.intVarArray("unit_timeperiod", numUnits, 0, numTimePeriods - 1);

    }


    public void addConstraints(){

        // creamos lista para: cada profesor los horarios en los que puede trabajar
        Tuples allowTeacherTimePeriodsPairs = new Tuples(true);
        if (numTeachers > 0 && numTimePeriods > 0) {
            // Allow all pairs of teachers and time periods
            for (int tIdx = 0; tIdx < numTeachers; tIdx++) {
                Teacher teacher = teachers.get(tIdx);
                for (int tpIdx = 0; tpIdx < numTimePeriods; tpIdx++) {
                    TimePeriod timePeriod = timePeriods.get(tpIdx);
                    if (teacher.isAvailable(timePeriod)) {
                        // If the teacher is available at this time period, allow this pair
                        allowTeacherTimePeriodsPairs.add(tIdx, tpIdx);
                    }

                }
            }
        }



        // 3. Add constraints
        for (int i = 0; i < numUnits; i++) {
            ScheduledUnit currentUnit = scheduledUnits.get(i);

            // Constraint: Teacher must be qualified to teach the subject of the unit.
            List<Integer> qualifiedTeacherIndices = new ArrayList<>();
            for (int tIdx = 0; tIdx < numTeachers; tIdx++) {
                if (teachers.get(tIdx).canTeach(currentUnit.subject)) {
                    qualifiedTeacherIndices.add(tIdx);
                }
            }

            if (qualifiedTeacherIndices.isEmpty()) {
                System.err.println("Error: No teacher is qualified to teach " + currentUnit.subject +
                        " for " + currentUnit.studentGroup + ". Schedule might be infeasible.");
                // Make this unit unschedulable for teachers, effectively making the model infeasible if this unit must be scheduled.
                model.member(unitTeacherVars[i], new int[]{}).post();
            } else {
                model.member(unitTeacherVars[i], qualifiedTeacherIndices.stream().mapToInt(Integer::intValue).toArray()).post();
            }

            // (Optional) Add other constraints like classroom capacity, equipment, etc.
            // Example: model.member(unitClassroomVars[i], suitableClassroomIndices).post();

            // Constraints for non-overlapping resources
            for (int j = i + 1; j < numUnits; j++) {
                ScheduledUnit otherUnit = scheduledUnits.get(j);

                // If two different units are scheduled at the same time period:
                // C1: They must not use the same teacher.
                // C2: They must not use the same classroom.
                model.ifThen(
                        model.arithm(unitTimePeriodVars[i], "=", unitTimePeriodVars[j]),
                        model.and(
                                model.arithm(unitTeacherVars[i], "!=", unitTeacherVars[j]),
                                model.arithm(unitClassroomVars[i], "!=", unitClassroomVars[j])
                        )
                );

                // C3: If two units involve the same student group, they must not be at the same time period.
                // (This implicitly means a student group cannot take two subjects simultaneously).
                if (currentUnit.studentGroup.getId().equals(otherUnit.studentGroup.getId())) {
                    model.arithm(unitTimePeriodVars[i], "!=", unitTimePeriodVars[j]).post();
                }
            }

            // Constraint: Subject must be done in AssignedClassroom if it has.
           Subject subject = currentUnit.getSubject();
           Classroom assignedClassroom = subject.getAssignedClassroom();
            if (assignedClassroom != null) {
                int index = -1;
                for (int cIdx = 0; cIdx < numClassrooms; cIdx++) {
                    if (classrooms.get(cIdx).getId().equals(assignedClassroom.getId())) {
                        index = cIdx;
                        break;
                    }
                }
                if (index != -1) {
                    // If the subject has an assigned classroom, enforce that this unit must use that classroom.
                    model.arithm(unitClassroomVars[i], "=", index).post();
                } else {
                    System.err.println("Error: Assigned classroom for subject " + subject.getId() +
                            " not found in the list of classrooms. This unit may not be scheduled correctly.");
                    model.member(unitClassroomVars[i], new int[]{}).post();
                }
            }

            // Constraint: Si Classroom tiene asignaturas asignadas, solo esas asignaturas la pueden usar.

            if (numClassrooms > 0) {
                List<Integer> allowedClassroomIndices = new ArrayList<>();
                for (int cIdx = 0; cIdx < numClassrooms; cIdx++) {
                    Classroom classroom = classrooms.get(cIdx);
                    List<Subject> assignedSubjects = classroom.getAssignedSubjects();

                    // Si el aula no tiene asignaturas asignadas o contiene la asignatura de la unidad actual
                    if (assignedSubjects == null || assignedSubjects.isEmpty() ||
                            assignedSubjects.stream().anyMatch(subj ->
                                    subj.getId().equals(currentUnit.getSubject().getId()))) {
                        allowedClassroomIndices.add(cIdx);
                    }
                }

                if (!allowedClassroomIndices.isEmpty()) {
                    model.member(unitClassroomVars[i],
                            allowedClassroomIndices.stream().mapToInt(Integer::intValue).toArray()).post();
                } else {
                    System.err.println("Error: No classrooms available for subject " +
                            currentUnit.getSubject().getId() + ". This unit may not be scheduled correctly.");
                    model.member(unitClassroomVars[i], new int[]{}).post();
                }
            }








            // Constraint: Classroom must have enough capacity for the student group.
            if (numClassrooms > 0 && currentUnit.getStudentGroup() != null) {
                int studentGroupSize = currentUnit.getStudentGroup().getNumberOfStudents();
                int[] allClassroomCapacities = classrooms.stream()
                        .mapToInt(Classroom::getCapacity)
                        .toArray();
                int maxCapacity = Arrays.stream(allClassroomCapacities).max().orElse(0);
                IntVar classroomCapacityVar = model.intVar("classroom_capacity_" + i, 0, maxCapacity);
                model.element(classroomCapacityVar, allClassroomCapacities, unitClassroomVars[i]).post();
                model.arithm(classroomCapacityVar, ">=", studentGroupSize).post();
            }

            // Constraint: Time period must be allowed for the teacher.
            if (numTeachers > 0 && numTimePeriods > 0) {
                // Use the predefined pairs of allowed teacher-time period combinations
                model.table(new IntVar[]{unitTeacherVars[i], unitTimePeriodVars[i]},
                        allowTeacherTimePeriodsPairs).post();
            } else {
                System.err.println("Error: No teachers or time periods defined. Cannot enforce time period constraints.");
            }




        }
/*
        // AllDifferent constraint for (teacher, timeperiod) pairs to ensure a teacher is not in two places at once.
        // This is more robust than the pairwise check ifThen for teacher clashes.
        IntVar[] teacherTimePairs = new IntVar[numUnits];
        for(int i=0; i<numUnits; i++){
            // Create a combined variable: teacher_idx * numTimePeriods + timeperiod_idx
            // This ensures each pair (teacher, timeperiod) is unique if these vars are all different.
            // Max value for timeperiod_idx is numTimePeriods - 1.
            teacherTimePairs[i] = model.intVar("tt_pair_" + i, 0, numTeachers * numTimePeriods -1);
            model.scalar(new IntVar[]{unitTeacherVars[i], unitTimePeriodVars[i]}, new int[]{numTimePeriods, 1}, "=", teacherTimePairs[i]).post();
        }
        model.allDifferent(teacherTimePairs, "AC").post();

        // AllDifferent constraint for (classroom, timeperiod) pairs.
        IntVar[] classroomTimePairs = new IntVar[numUnits];
        for(int i=0; i<numUnits; i++){
            classroomTimePairs[i] = model.intVar("ct_pair_" + i, 0, numClassrooms * numTimePeriods -1);
            model.scalar(new IntVar[]{unitClassroomVars[i], unitTimePeriodVars[i]}, new int[]{numTimePeriods, 1}, "=", classroomTimePairs[i]).post();
        }
        model.allDifferent(classroomTimePairs, "AC").post();

        // AllDifferent constraint for (studentgroup, timeperiod) pairs.
        // We need to map student group objects to indices first.
        List<String> studentGroupIds = studentGroups.stream().map(sg -> sg.getId()).distinct().collect(Collectors.toList());
        IntVar[] studentGroupTimePairs = new IntVar[numUnits];
        for(int i=0; i<numUnits; i++){
            int sgIdx = studentGroupIds.indexOf(scheduledUnits.get(i).studentGroup.getId());
            // This variable represents the student group index for the unit.
            IntVar unitStudentGroupIdxVar = model.intVar(sgIdx);
            studentGroupTimePairs[i] = model.intVar("sgt_pair_" + i, 0, studentGroups.size() * numTimePeriods -1);
            model.scalar(new IntVar[]{unitStudentGroupIdxVar, unitTimePeriodVars[i]}, new int[]{numTimePeriods, 1}, "=", studentGroupTimePairs[i]).post();
        }
        model.allDifferent(studentGroupTimePairs, "AC").post();

*/
        // Constraint: No teacher can teach more hours than what they are allowed to.
        // Assumes Teacher.getMaxHours() exists.
        // Assumes Subject.getDurationInHours() exists and returns the duration of one ScheduledUnit for that subject.
        // If all units are 1 hour, then getDurationInHours() should return 1.
        if (numTeachers > 0 && numUnits > 0) {
            for (int tIdx = 0; tIdx < numTeachers; tIdx++) {
                Teacher teacher = teachers.get(tIdx);
                IntVar[] unitsTaughtByTeacher = new IntVar[numUnits];
                int[] unitDurations = new int[numUnits];

                for (int uIdx = 0; uIdx < numUnits; uIdx++) {
                    // unitTaughtByTeacher[uIdx] will be 1 if teacher tIdx teaches unit uIdx, 0 otherwise.
                    unitsTaughtByTeacher[uIdx] = model.boolVar("teaches_unit_" + uIdx + "_by_teacher_" + tIdx);
                    model.arithm(unitTeacherVars[uIdx], "=", tIdx).reifyWith((BoolVar) unitsTaughtByTeacher[uIdx]);
                    unitDurations[uIdx] = scheduledUnits.get(uIdx).getSubject().getDuration(); // Assumed method
                }
                // Sum of (unitsTaughtByTeacher[u] * unitDurations[u]) <= teacher.getMaxHours()
                model.scalar(unitsTaughtByTeacher, unitDurations, "<=", teacher.getHoursWork()).post();
            }
        }







    }

    // SOFT CONSTRAINTS

    // Teacher prefer certain TimePeriods.


    public void addSoftConstraints(){

        // Teacher prefer certain TimePeriods.
        IntVar totalPreferencePenalty = model.intVar("total_preference_penalty", 0, numUnits);

        if (numTeachers > 0 && numTimePeriods > 0 && numUnits > 0) {
            // Use the predefined pairs of allowed teacher-time period combinations
            Tuples preferredTeacherTimePeriodsPairs = new Tuples(true);
            for (int tIdx = 0; tIdx < numTeachers; tIdx++) {
                Teacher teacher = teachers.get(tIdx);
                List<TimePeriod> preferredTimePeriods = teacher.getPreferredTimePeriods();
                if (!preferredTimePeriods.isEmpty()) {
                    for (int tpIdx = 0; tpIdx < numTimePeriods; tpIdx++) {
                        System.out.println("Checking teacher " + teacher.getId() + " for preferred time period " + timePeriods.get(tpIdx).getId());
                        TimePeriod timePeriod = timePeriods.get(tpIdx);
                        if (preferredTimePeriods.contains(timePeriod)) {
                            System.out.println("Teacher " + teacher.getId() + " prefers time period " + timePeriod.getId());
                            // If the teacher prefers this time period, allow this pair
                            preferredTeacherTimePeriodsPairs.add(tIdx, tpIdx);
                            System.out.println(preferredTeacherTimePeriodsPairs);
                        }

                    }
                }

            }

            List<BoolVar> teacherPreferencesPenalty = new ArrayList<>();


            for (int i = 0; i < numUnits; i++) {
                BoolVar prefersTimePeriod = model.boolVar("prefers_time_period_" + i);
                teacherPreferencesPenalty.add(prefersTimePeriod);

                if (preferredTeacherTimePeriodsPairs.nbTuples() != 0) {
                    // If the teacher prefers the time period for this unit, set the penalty to 0
                    model.ifThenElse(
                            model.table(new IntVar[]{unitTeacherVars[i], unitTimePeriodVars[i]}, preferredTeacherTimePeriodsPairs),
                            model.arithm(prefersTimePeriod, "=", 0), model.arithm(prefersTimePeriod, "=", 1)
                    );
                } else {
                    // If no preferences are defined, we assume no penalty
                    model.arithm(prefersTimePeriod, "=", 0).post();
                }
            }

            model.sum(teacherPreferencesPenalty.toArray(new BoolVar[0]), "=", totalPreferencePenalty).post();

        } else {
            System.err.println("Error: No teachers, time periods, or units defined. Cannot add soft constraints.");

        }

        // Teacher dislikes certain TimePeriods.

        if (numTeachers > 0 && numTimePeriods > 0 && numUnits > 0) {
            // Use the predefined pairs of allowed teacher-time period combinations
            Tuples unPreferredTeacherTimePeriodsPairs = new Tuples(true);
            for (int tIdx = 0; tIdx < numTeachers; tIdx++) {
                Teacher teacher = teachers.get(tIdx);
                List<TimePeriod> unPreferredTimePeriods = teacher.getUnPreferredTimePeriods();
                if (unPreferredTimePeriods != null) {
                    for (int tpIdx = 0; tpIdx < numTimePeriods; tpIdx++) {
                        TimePeriod timePeriod = timePeriods.get(tpIdx);
                        if (unPreferredTimePeriods.contains(timePeriod)) {
                            System.out.println("Teacher " + teacher.getId() + " does not prefer time period " + timePeriod.getId());
                            // If the teacher prefers this time period, allow this pair
                            unPreferredTeacherTimePeriodsPairs.add(tIdx, tpIdx);
                        }

                    }
                }

            }

            List<BoolVar> teacherUnPreferencesPenalty = new ArrayList<>();

            for (int i = 0; i < numUnits; i++) {
                BoolVar unPrefersTimePeriod = model.boolVar("doesnot_prefers_time_period_" + i);
                teacherUnPreferencesPenalty.add(unPrefersTimePeriod);
                if (unPreferredTeacherTimePeriodsPairs.nbTuples() != 0) {
                    // If the teacher prefers the time period for this unit, set the penalty to 0
                    model.ifThenElse(
                            model.table(new IntVar[]{unitTeacherVars[i], unitTimePeriodVars[i]}, unPreferredTeacherTimePeriodsPairs),
                            model.arithm(unPrefersTimePeriod, "=", 1), model.arithm(unPrefersTimePeriod, "=", 0)
                    );
                } else {
                    // If no preferences are defined, we assume no penalty
                    model.arithm(unPrefersTimePeriod, "=", 0).post();
                }
            }
            IntVar totalUnPreferencePenalty = model.intVar("total_unpreference_penalty", 0, numUnits);
            model.sum(teacherUnPreferencesPenalty.toArray(new BoolVar[0]), "=", totalUnPreferencePenalty).post();

            IntVar totalPenalty = model.intVar("total_penalty", 0, numUnits * 2);
            model.arithm(totalPreferencePenalty, "+", totalUnPreferencePenalty, "=", totalPenalty).post();
            model.setObjective(Model.MINIMIZE, totalPenalty);
        } else {
            System.err.println("Error: No teachers, time periods, or units defined. Cannot add soft constraints.");

        }

        // Preferencia de profesores por ciertas asignaturas

        if (numTeachers > 0 && numUnits > 0) {
            // Paso 1: Crear un mapeo de subjectId (String) a índice (int)
            List<Subject> allSubjects = teachers.stream()
                    .flatMap(t -> t.getPossibleSubjects().stream())
                    .distinct()
                    .collect(Collectors.toList());
            Map<String, Integer> subjectIdToIndex = new HashMap<>();
            for (int idx = 0; idx < allSubjects.size(); idx++) {
                subjectIdToIndex.put(allSubjects.get(idx).getId(), idx);
            }

            Tuples preferredTeacherSubjectPairs = new Tuples(true);
            // Construir los pares (profesor, asignatura preferida)
            for (int tIdx = 0; tIdx < numTeachers; tIdx++) {
                Teacher teacher = teachers.get(tIdx);
                List<Subject> preferredSubjects = teacher.getPreferredSubjects();
                for (Subject subj : teacher.getPreferredSubjects()) {
                    Integer subjIdx = subjectIdToIndex.get(subj.getId());
                    if (subjIdx != null) {
                        preferredTeacherSubjectPairs.add(tIdx, subjIdx);
                    }
                }
            }

            List<BoolVar> teacherSubjectPreferencePenalty = new ArrayList<>();

            for (int i = 0; i < numUnits; i++) {
                ScheduledUnit unit = scheduledUnits.get(i);
                int subjectIdx = subjectIdToIndex.get(unit.getSubject().getId());                BoolVar prefersSubject = model.boolVar("prefers_subject_" + i);
                teacherSubjectPreferencePenalty.add(prefersSubject);

                if (preferredTeacherSubjectPairs.nbTuples() != 0) {
                    model.ifThenElse(
                            model.table(new IntVar[]{unitTeacherVars[i], model.intVar(subjectIdx)}, preferredTeacherSubjectPairs),
                            model.arithm(prefersSubject, "=", 0),
                            model.arithm(prefersSubject, "=", 1)
                    );
                } else {
                    model.arithm(prefersSubject, "=", 0).post();
                }
            }

            IntVar totalSubjectPreferencePenalty = model.intVar("total_subject_preference_penalty", 0, numUnits);
            model.sum(teacherSubjectPreferencePenalty.toArray(new BoolVar[0]), "=", totalSubjectPreferencePenalty).post();
            // Puedes añadir totalSubjectPreferencePenalty a la función objetivo si lo deseas
        } else {
            System.err.println("Error: No teachers or units defined. Cannot add subject preference soft constraints.");
        }


    }




    private List<Schedule> solveModel(){
        while (model.getSolver().solve()) {
            // This loop will find all solutions, but we only need one.
            // You can remove this loop if you only want the first solution.
        }
        List<Solution> solutions = model.getSolver().findAllSolutions();
        List<Schedule> schedules = new ArrayList<>();
        for (Solution solution : solutions) {


            Schedule schedule = new Schedule(); // Your schedule representation

            if (solution != null) {
                System.out.println("Solution found!");
                for (int i = 0; i < numUnits; i++) {
                    int teacherIdx = solution.getIntVal(unitTeacherVars[i]);
                    int classroomIdx = solution.getIntVal(unitClassroomVars[i]);
                    int timePeriodIdx = solution.getIntVal(unitTimePeriodVars[i]);

                    schedule.addAssignment(
                            scheduledUnits.get(i),
                            teachers.get(teacherIdx),
                            classrooms.get(classroomIdx),
                            timePeriods.get(timePeriodIdx)
                    );
                }
            } else {
                System.out.println("No solution found.");
                // For debugging, you can print solver statistics or the model:
                // System.out.println(model.getSolver().getMeasures());
                // System.out.println(model);
                return null;
            }

            //return schedule;
            schedules.add(schedule);

        }
        return schedules;



    }


    public List<Schedule> createSchedule() {

        defineVariables();
        addConstraints();
        addSoftConstraints();
        return solveModel();



    }


}