package business;// generate csp solution for a schedule creator, knowing the varaibles are in Subject, Classroom, StudentGroup, Teacher, and TimePeriod java classes

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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
                    scheduledUnits.add(new ScheduledUnit(sg, subj, unitIdCounter++));
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
        }

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



    }

    private Schedule solveModel(){
        Solution solution = model.getSolver().findSolution();
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
        return schedule;




    }


    public Schedule createSchedule() {

        defineVariables();
        addConstraints();
        return solveModel();



    }


}