package business;// generate csp solution for a schedule creator, knowing the varaibles are in Subject, Classroom, StudentGroup, Teacher, and TimePeriod java classes

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;


import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

/**
 * ScheduleSolver se encarga de modelar y resolver el problema de horarios usando Choco Solver.
 * Utiliza las entidades Teacher, Classroom, StudentGroup, Subject y TimePeriod.
 */

public class ScheduleSolver {

    // Listas de entidades del problema
    private List<Teacher> teachers;
    private List<Classroom> classrooms;
    private List<StudentGroup> studentGroups;
    private List<TimePeriod> timePeriods;
    private List<ScheduledUnit> scheduledUnits; // Unidades a programar (grupo + asignatura + id único)
    private Model model; // Modelo de Choco Solver

    // Variables de tamaño
    private int numUnits;
    private int numTeachers;
    private int numClassrooms;
    private int numTimePeriods;

    // Variables de decisión para cada unidad (grupo+asignatura)
    private IntVar[] unitTeacherVars;
    private IntVar[] unitClassroomVars;
    private IntVar[] unitTimePeriodVars;
    private List<BoolVar> teacherPreferencesPenalty; // Penalizaciones por preferencias de profesores
    private List<BoolVar> teacherUnPreferencesPenalty; // Penalizaciones por no preferencias de profesores

    private IntVar totalScore;

    /**
     * Constructor principal. Inicializa listas y modelo.
     */
    public ScheduleSolver(List<Teacher> teachers, List<Classroom> classrooms, List<StudentGroup> studentGroups, List<TimePeriod> timePeriods) {
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

        this.teacherPreferencesPenalty = new ArrayList<>();
        this.teacherUnPreferencesPenalty = new ArrayList<>();

    }

    /**
     * Crea las unidades a programar (ScheduledUnit) a partir de los grupos y sus asignaturas requeridas.
     */
    public void createScheduleUnits() {
        // A ScheduledUnit is a specific StudentGroup needing a specific Subject.
        List<ScheduledUnit> scheduledUnits = new ArrayList<>();
        int unitIdCounter = 0;
        for (StudentGroup sg : studentGroups) {
            if (sg.getRequiredSubjects() != null) {
                for (Subject subj : sg.getRequiredSubjects()) {
                    for (int i = 0; i < subj.getWeeklyAssignedHours(); i++) {
                        // Una unidad por cada hora semanal requerida
                        //System.out.println("Creating unit for StudentGroup: " + sg.getId() + ", Subject: " + subj.getId() + ", Unit ID: " + unitIdCounter);
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

    /**
     * Define las variables de decisión del modelo para cada unidad.
     */
    public void defineVariables() {

        this.unitTeacherVars = model.intVarArray("unit_teacher", numUnits, 0, numTeachers - 1);
        this.unitClassroomVars = model.intVarArray("unit_classroom", numUnits, 0, numClassrooms - 1);
        this.unitTimePeriodVars = model.intVarArray("unit_timeperiod", numUnits, 0, numTimePeriods - 1);

    }

    /**
     * Añade las restricciones duras al modelo (asignación válida de profesores, aulas, periodos, etc).
     */
    public void addConstraints() {

        // 1 - Restricciones de asignación de profesores y periodos de tiempo posibles para el profesor

        // Crear una estructura Tuples para almacenar las combinaciones válidas de (profesor, periodo)
        // Esta estructura se usará en la restricción de Choco Solver para permitir solo asignaciones válidas.
        Tuples allowTeacherTimePeriodsPairs = new Tuples(true);

        // Verificar que existen profesores y periodos antes de construir las tuplas
        if (numTeachers > 0 && numTimePeriods > 0) {

            // Recorrer todos los profesores
            for (int tIdx = 0; tIdx < numTeachers; tIdx++) {
                Teacher teacher = teachers.get(tIdx);

                // Recorrer todos los periodos de tiempo
                for (int tpIdx = 0; tpIdx < numTimePeriods; tpIdx++) {
                    TimePeriod timePeriod = timePeriods.get(tpIdx);

                    // Si el profesor está disponible en ese periodo, añadir la tupla (profesor, periodo)
                    // Esto significa que esa combinación está permitida en el modelo.
                    if (teacher.isAvailable(timePeriod)) {
                        allowTeacherTimePeriodsPairs.add(tIdx, tpIdx);
                    }
                }
            }
        }

        // Restricciones para cada unidad
        for (int i = 0; i < numUnits; i++) {

            // El periodo debe estar permitido para el profesor
            if (numTeachers > 0 && numTimePeriods > 0) {
                // Usa la tabla de tuplas permitidas (profesor, periodo) para asegurar que solo se asignen combinaciones válidas.
                model.table(new IntVar[]{unitTeacherVars[i], unitTimePeriodVars[i]},
                        allowTeacherTimePeriodsPairs).post();
            } else {
                // Si no hay profesores o periodos definidos, muestra un error y no aplica la restricción.
                System.err.println("Error: No teachers or time periods defined. Cannot enforce time period constraints.");
            }


            // 2 - Control de las habilitaciones de los profesores

            // Solo profesores cualificados pueden impartir la asignatura

            // Obtiene la unidad actual a programar (grupo y asignatura)
            ScheduledUnit currentUnit = scheduledUnits.get(i);

            // Lista para almacenar los índices de profesores cualificados para la asignatura de la unidad actual
            List<Integer> qualifiedTeacherIndices = new ArrayList<>();
            for (int tIdx = 0; tIdx < numTeachers; tIdx++) {
                // Verifica si el profesor puede impartir la asignatura
                if (teachers.get(tIdx).canTeach(currentUnit.subject)) {
                    qualifiedTeacherIndices.add(tIdx);
                }
            }

            // Si ningún profesor es cualificado, muestra error y hace la unidad no asignable (modelo infactible)
            if (qualifiedTeacherIndices.isEmpty()) {
                System.err.println("Error: No teacher is qualified to teach " + currentUnit.subject +
                        " for " + currentUnit.studentGroup + ". Schedule might be infeasible.");
                // Restringe la variable para que no pueda tomar ningún valor (ningún profesor puede ser asignado)// Make this unit unschedulable for teachers, effectively making the model infeasible if this unit must be scheduled.
                model.member(unitTeacherVars[i], new int[]{}).post();
            } else {
                // Restringe la variable para que solo pueda tomar los valores de profesores cualificados
                model.member(unitTeacherVars[i], qualifiedTeacherIndices.stream().mapToInt(Integer::intValue).toArray()).post();
            }


            // 3 - Solapamiento de recursos y restricciones de aula

            // Recorre todas las unidades posteriores para evitar solapamientos de recursos
            for (int j = i + 1; j < numUnits; j++) {
                ScheduledUnit otherUnit = scheduledUnits.get(j);

                // Si dos unidades se programan en el mismo periodo:
                // C1: No pueden tener el mismo profesor
                // C2: No pueden tener el mismo aula
                model.ifThen(
                        model.arithm(unitTimePeriodVars[i], "=", unitTimePeriodVars[j]),
                        model.and(
                                model.arithm(unitTeacherVars[i], "!=", unitTeacherVars[j]),
                                model.arithm(unitClassroomVars[i], "!=", unitClassroomVars[j])
                        )
                );

                // C3: Si dos unidades tienen el mismo grupo de alumnos, no pueden estar en el mismo periodo
                if (currentUnit.studentGroup.getId().equals(otherUnit.studentGroup.getId())) {
                    model.arithm(unitTimePeriodVars[i], "!=", unitTimePeriodVars[j]).post();
                }
            }

            // Si la asignatura tiene aula asignada, fuerza su uso
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


            // Solo se pueden usar aulas permitidas para la asignatura
            if (numClassrooms > 0) {
                List<Integer> allowedClassroomIndices = new ArrayList<>();
                for (int cIdx = 0; cIdx < numClassrooms; cIdx++) {
                    Classroom classroom = classrooms.get(cIdx);
                    List<Subject> assignedSubjects = classroom.getAssignedSubjects();

                    // Permite el aula si no tiene asignaturas asignadas o incluye la actual
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

            // El aula debe tener capacidad suficiente para el grupo
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


        }

        // 4 - Restricciones de horas de trabajo de los profesores

        // Restricción: ningún profesor puede superar su máximo de horas semanales

        if (numTeachers > 0 && numUnits > 0) {
            for (int tIdx = 0; tIdx < numTeachers; tIdx++) {
                Teacher teacher = teachers.get(tIdx);

                // Array de variables booleanas: indica si el profesor tIdx imparte cada unidad (1 = sí, 0 = no)
                IntVar[] unitsTaughtByTeacher = new IntVar[numUnits];

                // Array con la duración de cada unidad (en horas)
                int[] unitDurations = new int[numUnits];

                for (int uIdx = 0; uIdx < numUnits; uIdx++) {
                    // Crea una variable booleana para la unidad uIdx y el profesor tIdx
                    unitsTaughtByTeacher[uIdx] = model.boolVar("teaches_unit_" + uIdx + "_by_teacher_" + tIdx);

                    // Relaciona la variable booleana con la asignación real del profesor a la unidad
                    model.arithm(unitTeacherVars[uIdx], "=", tIdx).reifyWith((BoolVar) unitsTaughtByTeacher[uIdx]);

                    // Obtiene la duración de la unidad (en horas)
                    unitDurations[uIdx] = scheduledUnits.get(uIdx).getSubject().getDuration(); // Assumed method
                }

                // Suma ponderada: total de horas impartidas por el profesor en la semana
                // La suma de (unidades impartidas * duración unidad) debe ser menor o igual a su máximo de horas
                // Sum of (unitsTaughtByTeacher[u] * unitDurations[u]) <= teacher.getMaxHours()
                model.scalar(unitsTaughtByTeacher, unitDurations, "<=", teacher.getHoursWork()).post();
            }
        }


        // 5 - Restricciones de horas de asignatura por día y consecutividad
        for (StudentGroup sg : studentGroups) {
            for (Subject subj : sg.getRequiredSubjects()) {
                // Obtener índices de unidades para esta combinación grupo-asignatura
                List<Integer> unitIndices = new ArrayList<>();
                for (int i = 0; i < numUnits; i++) {
                    ScheduledUnit unit = scheduledUnits.get(i);
                    if (unit.getStudentGroup().equals(sg) && unit.getSubject().equals(subj)) {
                        unitIndices.add(i);
                    }
                }

                if (unitIndices.isEmpty()) continue;

                // Agrupar periodos por día
                Map<String, List<Integer>> tpByDay = new HashMap<>();
                for (int tp = 0; tp < numTimePeriods; tp++) {
                    String day = timePeriods.get(tp).getWeekday();
                    tpByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(tp);
                }

                // Restricciones por día
                int maxHoursPerDay = subj.getMaxDailyHours() > 0 ? subj.getMaxDailyHours() : unitIndices.size();

                for (String day : tpByDay.keySet()) {
                    List<Integer> dayPeriods = tpByDay.get(day);

                    // Para cada unidad, ¿está asignada a este día?
                    List<BoolVar> unitsInThisDay = new ArrayList<>();
                    for (int uIdx : unitIndices) {
                        BoolVar isInDay = model.boolVar("unit_" + uIdx + "_in_day_" + day);

                        // Array de BoolVars: unidad está en algún periodo de este día
                        BoolVar[] inAnyPeriod = new BoolVar[dayPeriods.size()];
                        for (int i = 0; i < dayPeriods.size(); i++) {
                            int tp = dayPeriods.get(i);
                            inAnyPeriod[i] = model.boolVar("unit_" + uIdx + "_tp_" + tp);
                            model.arithm(unitTimePeriodVars[uIdx], "=", tp).reifyWith(inAnyPeriod[i]);
                        }

                        // La unidad está en este día si está en algún periodo del día
                        model.max(isInDay, inAnyPeriod).post();
                        unitsInThisDay.add(isInDay);
                    }

                    // Limitar unidades por día
                    model.sum(unitsInThisDay.toArray(new BoolVar[0]), "<=", maxHoursPerDay).post();

                    // Restricción de consecutividad
                    if (dayPeriods.size() >= 2) {
                        // Para cada par de unidades asignadas a este día
                        for (int i = 0; i < unitIndices.size(); i++) {
                            for (int j = i + 1; j < unitIndices.size(); j++) {
                                int uIdxA = unitIndices.get(i);
                                int uIdxB = unitIndices.get(j);

                                // Si ambas unidades están en este día
                                BoolVar bothInDay = model.boolVar("both_in_day_" + uIdxA + "_" + uIdxB);
                                model.and(unitsInThisDay.get(i), unitsInThisDay.get(j)).reifyWith(bothInDay);

                                // Si ambas están en el día, el periodo B debe ser el periodo A + 1
                                model.ifThen(
                                        bothInDay,
                                        model.or(
                                                model.arithm(unitTimePeriodVars[uIdxB], "=", model.intOffsetView(unitTimePeriodVars[uIdxA], 1)),
                                                model.arithm(unitTimePeriodVars[uIdxA], "=", model.intOffsetView(unitTimePeriodVars[uIdxB], 1))
                                        )
                                );
                            }
                        }
                    }
                }
            }
        }

        // 6 - Restricción de mismo profesor para todas las unidades de la misma asignatura y grupo
        Map<String, List<Integer>> unitsByGroupSubject = new HashMap<>();

// Agrupar unidades por combinación grupo-asignatura
        for (int i = 0; i < numUnits; i++) {
            ScheduledUnit unit = scheduledUnits.get(i);
            String key = unit.getStudentGroup().getId() + "-" + unit.getSubject().getId();
            unitsByGroupSubject.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
        }

// Para cada grupo de unidades del mismo grupo-asignatura, forzar el mismo profesor
        for (List<Integer> relatedUnits : unitsByGroupSubject.values()) {
            if (relatedUnits.size() > 1) {
                for (int i = 1; i < relatedUnits.size(); i++) {
                    // La unidad actual debe tener el mismo profesor que la primera unidad del grupo
                    int firstUnit = relatedUnits.get(0);
                    int currentUnit = relatedUnits.get(i);
                    model.arithm(unitTeacherVars[firstUnit], "=", unitTeacherVars[currentUnit]).post();
                }
            }
        }
    }


    // Añade las restricciones soft basadas en las preferencias de los profesores
    public void addSoftConstraints() {
        if (numTeachers == 0 || numTimePeriods == 0 || numUnits == 0) {
            System.err.println("Error: No teachers, time periods, or units defined. Cannot add soft constraints.");
            return;
        }

        List<IntVar> softScores = new ArrayList<>();

        for (int tIdx = 0; tIdx < numTeachers; tIdx++) {
            Teacher teacher = teachers.get(tIdx);

            List<Condition> preferredConditions = teacher.getPreferredConditions();
            List<Condition> unPreferredConditions = teacher.getUnPreferredConditions();

            // === Preferred Time Periods ===
            if (preferredConditions != null) {
                for (Condition condition : preferredConditions) {
                    TimePeriod period = condition.getTimePeriod();
                    int weight = condition.getWeight();
                    int periodIdx = timePeriods.indexOf(period);

                    if (period != null && weight > 0 && periodIdx != -1) {
                        BoolVar[] matches = new BoolVar[numUnits];
                        for (int uIdx = 0; uIdx < numUnits; uIdx++) {
                            matches[uIdx] = model.and(
                                    model.arithm(unitTeacherVars[uIdx], "=", tIdx),
                                    model.arithm(unitTimePeriodVars[uIdx], "=", periodIdx)
                            ).reify();
                        }
                        BoolVar fulfilled = model.boolVar("fulfilled_pref_time_t" + tIdx + "_p" + periodIdx);
                        model.max(fulfilled, matches).post();

                        softScores.add(model.intScaleView(fulfilled, weight));
                    }
                }
            }

            // === Unpreferred Time Periods ===
            if (unPreferredConditions != null) {
                for (Condition condition : unPreferredConditions) {
                    TimePeriod period = condition.getTimePeriod();
                    int weight = condition.getWeight();
                    int periodIdx = timePeriods.indexOf(period);

                    if (period != null && weight > 0 && periodIdx != -1) {
                        BoolVar[] violations = new BoolVar[numUnits];
                        for (int uIdx = 0; uIdx < numUnits; uIdx++) {
                            violations[uIdx] = model.and(
                                    model.arithm(unitTeacherVars[uIdx], "=", tIdx),
                                    model.arithm(unitTimePeriodVars[uIdx], "=", periodIdx)
                            ).reify();
                        }
                        BoolVar violated = model.boolVar("violated_unpref_time_t" + tIdx + "_p" + periodIdx);
                        model.max(violated, violations).post();

                        softScores.add(model.intScaleView(violated, -weight));
                    }
                }
            }

            // === Preferred Subjects ===
            if (preferredConditions != null) {
                for (Condition condition : preferredConditions) {
                    Subject subject = condition.getSubject();
                    int weight = condition.getWeight();
                    if (subject != null && weight > 0) {
                        BoolVar[] matches = new BoolVar[numUnits];
                        for (int uIdx = 0; uIdx < numUnits; uIdx++) {
                            if (!scheduledUnits.get(uIdx).getSubject().equals(subject)) {
                                matches[uIdx] = model.boolVar(false);
                            } else {
                                matches[uIdx] = model.arithm(unitTeacherVars[uIdx], "=", tIdx).reify();
                            }
                        }
                        BoolVar fulfilled = model.boolVar("fulfilled_pref_subj_t" + tIdx + "_s" + subject.getId());
                        model.max(fulfilled, matches).post();

                        softScores.add(model.intScaleView(fulfilled, weight));
                    }
                }
            }

            // === Unpreferred Subjects ===
            if (unPreferredConditions != null) {
                for (Condition condition : unPreferredConditions) {
                    Subject subject = condition.getSubject();
                    int weight = condition.getWeight();
                    if (subject != null && weight > 0) {
                        BoolVar[] violations = new BoolVar[numUnits];
                        for (int uIdx = 0; uIdx < numUnits; uIdx++) {
                            if (!scheduledUnits.get(uIdx).getSubject().equals(subject)) {
                                violations[uIdx] = model.boolVar(false);
                            } else {
                                violations[uIdx] = model.arithm(unitTeacherVars[uIdx], "=", tIdx).reify();
                            }
                        }
                        BoolVar violated = model.boolVar("violated_unpref_subj_t" + tIdx + "_s" + subject.getId());
                        model.max(violated, violations).post();

                        softScores.add(model.intScaleView(violated, -weight));
                    }
                }
            }

            // === Preferred Student Groups ===
            if (preferredConditions != null) {
                for (Condition condition : preferredConditions) {
                    StudentGroup group = condition.getStudentGroup();
                    int weight = condition.getWeight();
                    if (group != null && weight > 0) {
                        BoolVar[] matches = new BoolVar[numUnits];
                        for (int uIdx = 0; uIdx < numUnits; uIdx++) {
                            if (!scheduledUnits.get(uIdx).getStudentGroup().equals(group)) {
                                matches[uIdx] = model.boolVar(false);
                            } else {
                                matches[uIdx] = model.arithm(unitTeacherVars[uIdx], "=", tIdx).reify();
                            }
                        }
                        BoolVar fulfilled = model.boolVar("fulfilled_pref_group_t" + tIdx + "_g" + group.getId());
                        model.max(fulfilled, matches).post();

                        softScores.add(model.intScaleView(fulfilled, weight));
                    }
                }
            }

            // === Unpreferred Student Groups ===
            if (unPreferredConditions != null) {
                for (Condition condition : unPreferredConditions) {
                    StudentGroup group = condition.getStudentGroup();
                    int weight = condition.getWeight();
                    if (group != null && weight > 0) {
                        BoolVar[] violations = new BoolVar[numUnits];
                        for (int uIdx = 0; uIdx < numUnits; uIdx++) {
                            if (!scheduledUnits.get(uIdx).getStudentGroup().equals(group)) {
                                violations[uIdx] = model.boolVar(false);
                            } else {
                                violations[uIdx] = model.arithm(unitTeacherVars[uIdx], "=", tIdx).reify();
                            }
                        }
                        BoolVar violated = model.boolVar("violated_unpref_group_t" + tIdx + "_g" + group.getId());
                        model.max(violated, violations).post();

                        softScores.add(model.intScaleView(violated, -weight));
                    }
                }
            }
        }

        if (!softScores.isEmpty()) {
            int min = softScores.stream().mapToInt(IntVar::getLB).sum();
            int max = softScores.stream().mapToInt(IntVar::getUB).sum();
            this.totalScore = model.intVar("soft_score", min, max);
            model.sum(softScores.toArray(new IntVar[0]), "=", totalScore).post();
            model.setObjective(Model.MAXIMIZE, totalScore);
        } else {
            System.out.println("No soft constraints to optimize.");
        }
    }

    private boolean quickFeasibilityCheck() {
        // groups need ≤ available slots
        int slots = timePeriods.size();
        for (StudentGroup g : studentGroups) {
            int need = g.getRequiredSubjects().stream()
                    .mapToInt(Subject::getWeeklyAssignedHours).sum();
            if (need > slots) {
                System.out.printf("Infeasible: %s needs %d > %d slots%n", g.getName(), need, slots);
                return false;
            }
        }
        // teacher capacity rough bound (sum of hoursWork ≥ sum of group demand)
        int totalDemand = studentGroups.stream()
                .flatMap(g -> g.getRequiredSubjects().stream())
                .mapToInt(Subject::getWeeklyAssignedHours).sum();
        int totalTeacherCap = teachers.stream()
                .mapToInt(Teacher::getHoursWork).sum();
        if (totalTeacherCap < totalDemand) {
            System.out.printf("Likely infeasible: teacher capacity %d < demand %d%n", totalTeacherCap, totalDemand);
            // still continue if you want; or return false to fail fast
        }
        return true;
    }



    private List<Schedule> solveModel() {
        final int  MAX_SOLUTIONS   = 10;     // how many to return
        final int  KEEP_FACTOR     = 3;      // keep up to 3x, then trim to top 10
        final int  TOLERANCE       = 10;      // accept solutions within (bestSeen - TOLERANCE)
        final int  MAX_RESTARTS    = 6;      // random restarts
        final int  FAIL_LIMIT      = 50_000; // per-restart fail limit
        final long TIME_BUDGET_MS  = 10_000;  // total time budget

        long deadline = System.currentTimeMillis() + TIME_BUDGET_MS;

        List<Schedule> result = new ArrayList<>();
        List<Candidate> pool = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        // Make sure objective exists and grab it
        IntVar scoreVar = ensureObjectiveVar();

        // Solver setup and baseline feasible
        Solver solver = model.getSolver();
        solver.reset();
        solver.limitTime(Math.max(1000, TIME_BUDGET_MS / 3) + "ms");

        Solution feasible = solver.findSolution();
        if (feasible == null) {
            System.out.println("No feasible solution under current hard constraints.");
            return result;
        }

        // Snapshot baseline
        int bestSeen = feasible.getIntVal(scoreVar);
        Solution snap = new Solution(model); snap.record();
        addCandidateIfUnique(pool, seen, snap, scoreVar, Integer.MAX_VALUE); // force-accept baseline

        // Variables to randomize
        IntVar[] allVars = java.util.stream.Stream.concat(
                java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(unitTeacherVars),
                        java.util.stream.Stream.of(unitClassroomVars)
                ),
                java.util.stream.Stream.of(unitTimePeriodVars)
        ).toArray(IntVar[]::new);

        // Randomized restarts within a global time budget
        int poolCap = MAX_SOLUTIONS * KEEP_FACTOR;
        for (int r = 0; r < MAX_RESTARTS && System.currentTimeMillis() < deadline; r++) {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) break;

            solver.reset();
            solver.limitFail(FAIL_LIMIT);
            solver.limitTime(Math.max(500, remaining) + "ms");

            long seed = System.nanoTime() ^ java.util.concurrent.ThreadLocalRandom.current().nextLong();
            solver.setSearch(org.chocosolver.solver.search.strategy.Search.randomSearch(allVars, seed));

            while (solver.solve()) {
                if (System.currentTimeMillis() >= deadline) break;

                int s = scoreVar.getValue();
                if (s > bestSeen) bestSeen = s;

                // snapshot promising and unique solutions
                if (s >= bestSeen - TOLERANCE) {
                    Solution sh = new Solution(model);
                    sh.record();
                    addCandidateIfUnique(pool, seen, sh, scoreVar, TOLERANCE);
                    if (pool.size() >= poolCap) break; // enough for this run
                }
            }
        }

        // If somehow nothing in pool (shouldn't happen), keep baseline
        if (pool.isEmpty()) addCandidateIfUnique(pool, seen, feasible, scoreVar, Integer.MAX_VALUE);

        // Sort by score desc and trim to MAX_SOLUTIONS
        pool.sort((a, b) -> Integer.compare(b.score, a.score));
        if (pool.size() > MAX_SOLUTIONS) pool = pool.subList(0, MAX_SOLUTIONS);

        // Build schedules from snapshots (NO restore)
        int idx = 1;
        for (Candidate c : pool) {
            Schedule sch = buildScheduleFromSolution(c.sol);
            String id = java.util.UUID.randomUUID().toString();
            sch.setId(id);
            sch.setName("Solución " + (idx++) + " (score=" + c.score + ")");

            // If your BusinessController.generateSchedules(false) already attaches schedules
            // after solveModel() returns, do NOT attach here. Otherwise, uncomment:
            // attachScheduleToOwners(sch);

            result.add(sch);
        }

        System.out.println("Selected " + result.size() + " solutions; bestSeen=" + bestSeen);
        return result;
    }

    // --- Small helper holder ---
    private static final class Candidate {
        final int score; final Solution sol; final String sig;
        Candidate(int score, Solution sol, String sig) { this.score = score; this.sol = sol; this.sig = sig; }
    }

    // Add if unique (by signature), using the given tolerance only for informational filtering upstream.
    private void addCandidateIfUnique(List<Candidate> pool, Set<String> seen, Solution sol, IntVar scoreVar, int tol) {
        String sig = buildSignatureIgnoringClassrooms(sol);
        if (seen.add(sig)) {
            pool.add(new Candidate(sol.getIntVal(scoreVar), sol, sig));
        }
    }


    // Build a Schedule using values from a Solution snapshot (no variable restoration)
    private Schedule buildScheduleFromSolution(Solution sol) {
        Schedule schedule = new Schedule();
        for (int i = 0; i < numUnits; i++) {
            int teacherIdx    = sol.getIntVal(unitTeacherVars[i]);
            int classroomIdx  = sol.getIntVal(unitClassroomVars[i]);
            int timePeriodIdx = sol.getIntVal(unitTimePeriodVars[i]);

            Teacher    teacher    = teachers.get(teacherIdx);
            Classroom  classroom  = classrooms.get(classroomIdx);
            TimePeriod timePeriod = timePeriods.get(timePeriodIdx);
            ScheduledUnit unit    = scheduledUnits.get(i);

            schedule.addAssignment(unit, teacher, classroom, timePeriod);
        }
        return schedule;
    }



    private Schedule buildScheduleFromCurrentAssign() {
        Schedule schedule = new Schedule();
        for (int i = 0; i < numUnits; i++) {
            int teacherIdx    = unitTeacherVars[i].getValue();
            int classroomIdx  = unitClassroomVars[i].getValue();
            int timePeriodIdx = unitTimePeriodVars[i].getValue();

            Teacher    teacher    = teachers.get(teacherIdx);
            Classroom  classroom  = classrooms.get(classroomIdx);
            TimePeriod timePeriod = timePeriods.get(timePeriodIdx);
            ScheduledUnit unit    = scheduledUnits.get(i);

            schedule.addAssignment(unit, teacher, classroom, timePeriod);
        }
        return schedule;
    }

    private void attachScheduleToOwners(Schedule s) {
        if (s == null || s.getLessons() == null) return;
        java.util.Set<Teacher> ts = new java.util.HashSet<>();
        java.util.Set<StudentGroup> gs = new java.util.HashSet<>();
        java.util.Set<Classroom> cs = new java.util.HashSet<>();
        for (Lesson l : s.getLessons()) {
            if (l.getTeacher() != null) ts.add(l.getTeacher());
            if (l.getStudentGroup() != null) gs.add(l.getStudentGroup());
            if (l.getClassroom() != null) cs.add(l.getClassroom());
        }
        for (Teacher t : ts) {
            if (t.getSchedules() == null) t.setSchedules(new ArrayList<>());
            if (t.getSchedules().stream().noneMatch(sc -> sc.getId().equals(s.getId()))) t.getSchedules().add(s);
        }
        for (StudentGroup g : gs) {
            if (g.getSchedules() == null) g.setSchedules(new ArrayList<>());
            if (g.getSchedules().stream().noneMatch(sc -> sc.getId().equals(s.getId()))) g.getSchedules().add(s);
        }
        for (Classroom c : cs) {
            if (c.getSchedules() == null) c.setSchedules(new ArrayList<>());
            if (c.getSchedules().stream().noneMatch(sc -> sc.getId().equals(s.getId()))) c.getSchedules().add(s);
        }
    }


    /**
    private List<Schedule> solveModel() {
        if (!quickFeasibilityCheck()) {
            System.out.println("Quick feasibility check failed. Aborting solve.");
            return new ArrayList<>();
        }
        final int MAX_SOLUTIONS   = 10;   // N best you want to return
        final int POOL_LIMIT      = 30;  // how many unique candidates to keep before stopping
        final int MAX_RESTARTS    = 5;    // how many random restarts
        final int BEAM_TOLERANCE  = 10;  // allow exploring solutions within (bestScore - tolerance)

        // Collect candidates here (do NOT attach to entities yet)
        class Candidate {
            final int score;
            final Schedule schedule; // global schedule
            final Map<Teacher, Schedule> tMap;
            final Map<Classroom, Schedule> cMap;
            final Map<StudentGroup, Schedule> gMap;
            final String signature;  // uniqueness signature (ignoring classrooms)

            Candidate(int score, Schedule schedule,
                      Map<Teacher, Schedule> tMap,
                      Map<Classroom, Schedule> cMap,
                      Map<StudentGroup, Schedule> gMap,
                      String signature) {
                this.score = score;
                this.schedule = schedule;
                this.tMap = tMap;
                this.cMap = cMap;
                this.gMap = gMap;
                this.signature = signature;
            }
        }

        List<Candidate> pool = new ArrayList<>();
        Set<String> seen = new HashSet<>(); // for uniqueness (by signature)

        // 1) Find best soft score once
        IntVar scoreVar = ensureObjectiveVar();
        Solution optimalSolution = model.getSolver().findOptimalSolution(scoreVar, Model.MAXIMIZE);
        if (optimalSolution == null) {
            System.out.println("No optimal solution found.");
            return new ArrayList<>();
        }
        int bestScore = optimalSolution.getIntVal(scoreVar);
        System.out.println("Best soft score found: " + bestScore);

        // Prepare arrays for random search
        IntVar[] allVars = Stream.concat(
                Stream.concat(Stream.of(unitTeacherVars), Stream.of(unitClassroomVars)),
                Stream.of(unitTimePeriodVars)
        ).toArray(IntVar[]::new);

        int collected = 0;

        // 2) Multi-restart randomized search to diversify
        for (int restart = 0; restart < MAX_RESTARTS && pool.size() < POOL_LIMIT; restart++) {
            Solver solver = model.getSolver();
            solver.reset();
            long seed = System.nanoTime() ^ ThreadLocalRandom.current().nextLong();
            solver.setSearch(Search.randomSearch(allVars, seed));

            while (solver.solve()) {
                int currentScore = scoreVar.getValue();
                // Prune far-below-best solutions to save time
                if (currentScore < bestScore - BEAM_TOLERANCE) break;

                // Build per-entity schedules for this solution (local, not attached)
                Map<Teacher, Schedule> teacherSchedules = new HashMap<>();
                for (Teacher t : teachers) teacherSchedules.put(t, new Schedule());

                Map<Classroom, Schedule> classroomSchedules = new HashMap<>();
                for (Classroom c : classrooms) classroomSchedules.put(c, new Schedule());

                Map<StudentGroup, Schedule> studentGroupSchedules = new HashMap<>();
                for (StudentGroup g : studentGroups) studentGroupSchedules.put(g, new Schedule());

                Schedule schedule = new Schedule();

                for (int i = 0; i < numUnits; i++) {
                    int teacherIdx    = unitTeacherVars[i].getValue();
                    int classroomIdx  = unitClassroomVars[i].getValue();
                    int timePeriodIdx = unitTimePeriodVars[i].getValue();

                    Teacher teacher       = teachers.get(teacherIdx);
                    Classroom classroom   = classrooms.get(classroomIdx);
                    TimePeriod timePeriod = timePeriods.get(timePeriodIdx);
                    ScheduledUnit unit    = scheduledUnits.get(i);

                    // global schedule
                    schedule.addAssignment(unit, teacher, classroom, timePeriod);

                    // per-entity
                    teacherSchedules.get(teacher).addAssignment(unit, teacher, classroom, timePeriod);
                    classroomSchedules.get(classroom).addAssignment(unit, teacher, classroom, timePeriod);
                    studentGroupSchedules.get(unit.getStudentGroup()).addAssignment(unit, teacher, classroom, timePeriod);
                }

                // Uniqueness signature that ignores classroom => increases diversity
                String sig = buildSignatureIgnoringClassrooms();

                if (seen.add(sig)) {
                    pool.add(new Candidate(currentScore, schedule, teacherSchedules, classroomSchedules, studentGroupSchedules, sig));
                    collected++;
                    System.out.println("Collected candidate #" + collected + " with score " + currentScore);
                    if (pool.size() >= POOL_LIMIT) break; // enough unique candidates
                }
            }
        }

        // 3) Sort by soft score desc and pick the best N
        pool.sort((a,b) -> Integer.compare(b.score, a.score));
        List<Candidate> top = pool.size() > MAX_SOLUTIONS ? pool.subList(0, MAX_SOLUTIONS) : pool;

        // 4) Now attach selected solutions to entities and return the schedules
        List<Schedule> result = new ArrayList<>(top.size());
        int solNum = 1;

        // Optional: clear existing schedules in entities before attaching (depends on your lifecycle)
        //for (Teacher t : teachers) t.setSchedules(new ArrayList<>());
        //for (Classroom c : classrooms) c.setSchedules(new ArrayList<>());
        //for (StudentGroup g : studentGroups) g.setSchedules(new ArrayList<>());

        for (Candidate cand : top) {
            // Attach per-entity schedules
            String id   = java.util.UUID.randomUUID().toString();
            String name = "Solución " + solNum++ + " (score=" + cand.score + ")";

            cand.schedule.setId(id);
            cand.schedule.setName(name);

            for (var e : cand.tMap.entrySet()) {
                e.getValue().setId(id);
                e.getValue().setName(name);
            }
            for (var e : cand.cMap.entrySet()) {
                e.getValue().setId(id);
                e.getValue().setName(name);
            }
            for (var e : cand.gMap.entrySet()) {
                e.getValue().setId(id);
                e.getValue().setName(name);
            }

            // Attach per-entity schedules to entities
            for (var e : cand.tMap.entrySet()) e.getKey().addSchedule(e.getValue());
            for (var e : cand.cMap.entrySet()) e.getKey().addSchedule(e.getValue());
            for (var e : cand.gMap.entrySet()) e.getKey().addSchedule(e.getValue());

            result.add(cand.schedule);
        }

        // Debug
        System.out.println("Selected " + result.size() + " best unique schedules by soft score.");
        for (int i = 0; i < result.size(); i++) {
            System.out.println("Solution " + (i+1) + " score=" + top.get(i).score);
        }
        return result;
    }
**/
    private IntVar ensureObjectiveVar() {
        if (totalScore != null) return totalScore;

        // If you have soft BoolVars + weights, build a weighted sum here.
        // Fallback: a fixed 0 var still lets findOptimalSolution run.
        totalScore = model.intVar("totalScore", 0);
        return totalScore;
    }

    /** Build a uniqueness signature from the current solution that IGNORES classrooms,
     *  so two solutions that only change a classroom are considered the same (encourages diversity).
     */
    // Signature that ignores classrooms to promote diversity by teacher/timePeriod per unit & group
    private String buildSignatureIgnoringClassrooms(Solution sol) {
        StringBuilder sb = new StringBuilder(numUnits * 8);
        for (int i = 0; i < numUnits; i++) {
            int tIdx  = sol.getIntVal(unitTeacherVars[i]);
            int tpIdx = sol.getIntVal(unitTimePeriodVars[i]);

            // Tie the unit to its student group to avoid permutations of the same plan
            StudentGroup g = scheduledUnits.get(i).getStudentGroup();
            int gIdx = (g == null) ? -1 : studentGroups.indexOf(g);

            sb.append('[').append(gIdx).append(':').append(tIdx).append('@').append(tpIdx).append(']');
        }
        return sb.toString();
    }









    public List<Schedule> createSchedule() throws ContradictionException {

        defineVariables();
        addConstraints();
        addSoftConstraints();
        System.out.println("Model created with " + numUnits + " units, " + numTeachers + " teachers, " +
                numClassrooms + " classrooms, and " + numTimePeriods + " time periods.");
        return solveModel();



    }


}