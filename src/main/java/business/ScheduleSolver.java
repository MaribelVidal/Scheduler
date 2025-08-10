package business;// generate csp solution for a schedule creator, knowing the varaibles are in Subject, Classroom, StudentGroup, Teacher, and TimePeriod java classes

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;


import java.util.*;
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

    private List<Schedule> solveModel() {
        List<Schedule> schedules = new ArrayList<>();
        Map<Teacher, Schedule> teacherSchedules = new HashMap<>();
        Map<Classroom, Schedule> classroomSchedules = new HashMap<>();
        Map<StudentGroup, Schedule> studentGroupSchedules = new HashMap<>();

        // 1. Find the optimal score
        IntVar scoreVar = totalScore;
        Solution optimalSolution = model.getSolver().findOptimalSolution(scoreVar, Model.MAXIMIZE);

        if (optimalSolution == null) {
            System.out.println("No optimal solution found.");
            return schedules;
        }

        int bestScore = optimalSolution.getIntVal(scoreVar);
        System.out.println("Best soft score found: " + bestScore);

        // 2. Reset solver and add randomness for diversity
        Solver solver = model.getSolver();
        solver.reset();

        IntVar[] allVars = Stream.concat(
                Stream.concat(Stream.of(unitTeacherVars), Stream.of(unitClassroomVars)),
                Stream.of(unitTimePeriodVars)
        ).toArray(IntVar[]::new);

        solver.setSearch(Search.randomSearch(allVars, System.currentTimeMillis()));

        int maxSolutions = 10;
        int collected = 0;

        while (solver.solve()) {
            int currentScore = scoreVar.getValue();
            if (currentScore < bestScore - maxSolutions) break;

            //Crear un nuevo Schedule para cada profesor
            teacherSchedules.clear(); // Limpiar horarios anteriores

            // Inicializar un horario para cada profesor
            for (Teacher teacher : teachers) {
                teacherSchedules.put(teacher, new Schedule());
            }

            // Crear un horario para cada aula
            classroomSchedules.clear(); // Limpiar horarios anteriores
            for (Classroom classroom : classrooms) {
                classroomSchedules.put(classroom, new Schedule());
            }

            // Crear un horario para cada grupo de estudiantes
            studentGroupSchedules.clear(); // Limpiar horarios anteriores
            for (StudentGroup studentGroup : studentGroups) {
                studentGroupSchedules.put(studentGroup, new Schedule());
            }



            // Construct the schedule
            Schedule schedule = new Schedule();
            for (int i = 0; i < numUnits; i++) {
                int teacherIdx = unitTeacherVars[i].getValue();
                int classroomIdx = unitClassroomVars[i].getValue();
                int timePeriodIdx = unitTimePeriodVars[i].getValue();

                Teacher teacher = teachers.get(teacherIdx);
                Classroom classroom = classrooms.get(classroomIdx);
                TimePeriod timePeriod = timePeriods.get(timePeriodIdx);
                ScheduledUnit unit = scheduledUnits.get(i);

                schedule.addAssignment(
                        scheduledUnits.get(i),
                        teachers.get(teacherIdx),
                        classrooms.get(classroomIdx),
                        timePeriods.get(timePeriodIdx)
                );

                // Añadir al horario individual del profesor
                teacherSchedules.get(teacher).addAssignment(unit, teacher, classroom, timePeriod);

                // Añadir al horario individual del aula
                classroomSchedules.get(classroom).addAssignment(unit, teacher, classroom, timePeriod);

                // Añadir al horario individual del grupo de estudiantes
                studentGroupSchedules.get(unit.getStudentGroup()).addAssignment(unit, teacher, classroom, timePeriod);
            }





            schedules.add(schedule);

            // Guardar los horarios individuales
            for (Teacher teacher : teachers) {
                Schedule teacherSchedule = teacherSchedules.get(teacher);
                // Solo guardar si tiene asignaciones

                    // Asignar el horario al profesor
                    teacher.addSchedule(teacherSchedule);

            }

            for (Classroom classroom : classrooms) {
                Schedule classroomSchedule = classroomSchedules.get(classroom);
                // Solo guardar si tiene asignaciones

                    // Asignar el horario al aula
                    classroom.addSchedule(classroomSchedule);

            }

            for (StudentGroup studentGroup : studentGroups) {
                Schedule studentGroupSchedule = studentGroupSchedules.get(studentGroup);
                // Solo guardar si tiene asignaciones

                    // Asignar el horario al grupo de estudiantes
                    studentGroup.addSchedule(studentGroupSchedule);

            }




            System.out.println("Solution " + schedules.size() + ": soft score = " + currentScore);
            collected++;
            if (collected >= maxSolutions) break;


            // Exclude this solution from future results
            List<BoolVar> diffs = new ArrayList<>();
            for (int i = 0; i < numUnits; i++) {
                diffs.add(model.arithm(unitTeacherVars[i], "!=", unitTeacherVars[i].getValue()).reify());
                diffs.add(model.arithm(unitClassroomVars[i], "!=", unitClassroomVars[i].getValue()).reify());
                diffs.add(model.arithm(unitTimePeriodVars[i], "!=", unitTimePeriodVars[i].getValue()).reify());
            }
            model.or(diffs.toArray(new BoolVar[0])).post();
        }

        return schedules;
    }







    public List<Schedule> createSchedule() {

        defineVariables();
        addConstraints();
        addSoftConstraints();
        System.out.println("Model created with " + numUnits + " units, " + numTeachers + " teachers, " +
                numClassrooms + " classrooms, and " + numTimePeriods + " time periods.");
        return solveModel();



    }


}