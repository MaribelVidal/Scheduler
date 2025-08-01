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


    // SOFT CONSTRAINTS

    /**
     * Añade restricciones blandas (preferencias de profesores por periodos, asignaturas, grupos, etc).
     * Penaliza las asignaciones que no cumplen preferencias.
     */

    /*
    public void addSoftConstraints(){


        // A - Preferencias de periodos de los profesores

        //Gestiona la penalización por asignar unidades a periodos preferidos por los profesores.
        //La penalización se suma si la asignación no coincide con los periodos preferidos
        //Se minimiza en la función objetivo.

        // Paso 1: Definir la variable que acumulará la penalización total por preferencias de periodo de los profesores.
        // El rango es de 0 a numUnits, ya que como máximo todas las unidades pueden estar en periodos diferentes a los preferidos.

        List<Condition> teacherPreferences = new ArrayList<>();
        List<Condition> teacherUnPreferences = new ArrayList<>();

        IntVar totalPreferencePenalty = model.intVar("total_preference_penalty", 0, numUnits);

        // Paso 2: Comprobar que existen profesores, periodos y unidades antes de aplicar la restricción.
        if (numTeachers > 0 && numTimePeriods > 0 && numUnits > 0) {
            // Paso 3: Crear la estructura Tuples para almacenar los pares (profesor, periodo preferido).
            Tuples preferredTeacherTimePeriodsPairs = new Tuples(true);
            for (int tIdx = 0; tIdx < numTeachers; tIdx++) {
                Teacher teacher = teachers.get(tIdx);

                teacherPreferences.addAll(teacher.getPreferredConditions());
                teacherUnPreferences.addAll(teacher.getUnPreferredConditions());

                List<TimePeriod> preferredTimePeriods = teacher.getPreferredTimePeriods();
                // Paso 4: Para cada profesor, recorrer sus periodos preferidos y añadir la tupla (profesor, periodo) si corresponde.
                if (!preferredTimePeriods.isEmpty()) {
                    for (int tpIdx = 0; tpIdx < numTimePeriods; tpIdx++) {
                        System.out.println("Checking teacher " + teacher.getId() + " for preferred time period " + timePeriods.get(tpIdx).getId());
                        TimePeriod timePeriod = timePeriods.get(tpIdx);
                        if (preferredTimePeriods.contains(timePeriod)) {
                            System.out.println("Teacher " + teacher.getId() + " prefers time period " + timePeriod.getId());
                            preferredTeacherTimePeriodsPairs.add(tIdx, tpIdx);
                            System.out.println(preferredTeacherTimePeriodsPairs);
                        }

                    }
                }

            }

            // Paso 5: Para cada unidad a programar, crear una variable booleana que indica si la asignación cumple la preferencia.
            for (int i = 0; i < numUnits; i++) {
                BoolVar prefersTimePeriod = model.boolVar("prefers_time_period_" + i);
                teacherPreferencesPenalty.add(prefersTimePeriod);

                // Paso 6: Si existen tuplas de preferencia, usar una restricción condicional:
                // Si la asignación (profesor, periodo) está en las tuplas preferidas, la penalización es 0.
                // Si no, la penalización es 1.
                if (preferredTeacherTimePeriodsPairs.nbTuples() != 0) {
                    model.ifThenElse(
                            model.table(new IntVar[]{unitTeacherVars[i], unitTimePeriodVars[i]}, preferredTeacherTimePeriodsPairs),
                            model.arithm(prefersTimePeriod, "=", 0),
                            model.arithm(prefersTimePeriod, "=", 1)
                    );
                } else {
                    // Paso 7: Si no hay preferencias definidas, la penalización es 0 por defecto.
                    model.arithm(prefersTimePeriod, "=", 0).post();
                }
            }

            // Paso 8: Sumar todas las penalizaciones individuales en la variable totalPreferencePenalty.
            model.sum(teacherPreferencesPenalty.toArray(new BoolVar[0]), "=", totalPreferencePenalty).post();

            // Paso 9: Definir la función objetivo del modelo para minimizar la penalización total por preferencias de periodo.
            model.setObjective(Model.MINIMIZE, totalPreferencePenalty);// Set the objective to maximize the total preference penalty
        } else {
            // Paso 10: Si faltan datos, mostrar error y no aplicar la restricción.
            System.err.println("Error: No teachers, time periods, or units defined. Cannot add soft constraints.");

        }

        // B - Rechazo (no deseo) de periodos de los profesores

        if (numTeachers > 0 && numTimePeriods > 0 && numUnits > 0) {
            // Use the predefined pairs of allowed teacher-time period combinations
            Tuples unPreferredTeacherTimePeriodsPairs = new Tuples(true);
            for (int tIdx = 0; tIdx < numTeachers; tIdx++) {
                Teacher teacher = teachers.get(tIdx);
                List<TimePeriod> unPreferredTimePeriods = teacher.getUnPreferredTimePeriods();
                if (!unPreferredTimePeriods.isEmpty()) {
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



            for (int i = 0; i < numUnits; i++) {
                BoolVar unPrefersTimePeriod = model.boolVar("doesnot_prefers_time_period_" + i);
                teacherUnPreferencesPenalty.add(unPrefersTimePeriod);
                if (unPreferredTeacherTimePeriodsPairs.nbTuples() != 0) {
                    // If the teacher prefers the time period for this unit, set the penalty to 0
                    model.ifThenElse(
                            model.table(new IntVar[]{unitTeacherVars[i], unitTimePeriodVars[i]}, unPreferredTeacherTimePeriodsPairs),
                            model.arithm(unPrefersTimePeriod, "=", 2), model.arithm(unPrefersTimePeriod, "=", 0)
                    );
                } else {
                    // If no preferences are defined, we assume no penalty
                    model.arithm(unPrefersTimePeriod, "=", 0).post();
                }
            }
            IntVar totalUnPreferencePenalty = model.intVar("total_unpreference_penalty", 0, numUnits);
            model.sum(teacherUnPreferencesPenalty.toArray(new BoolVar[0]), "=", totalUnPreferencePenalty).post();

            //IntVar totalPenalty = model.intVar("total_penalty", 0, numUnits * 2);
            //model.arithm(totalPreferencePenalty, "+", totalUnPreferencePenalty, "=", totalPenalty).post();
            model.setObjective(Model.MINIMIZE, totalUnPreferencePenalty);
        } else {
            System.err.println("Error: No teachers, time periods, or units defined. Cannot add soft constraints.");

        }

        // Preferencias de profesores por asignaturas

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


        // Preferencias de profesores por grupos de alumnos

        if (numTeachers > 0 && numUnits > 0) {
            // Paso 1: Crear un mapeo de subjectId (String) a índice (int)
            List<StudentGroup> allStudentGroups = studentGroups;
            Map<String, Integer> studentGroupIdToIndex = new HashMap<>();
            for (int idx = 0; idx < allStudentGroups.size(); idx++) {
                studentGroupIdToIndex.put(allStudentGroups.get(idx).getId(), idx);
            }

            Tuples preferredTeacherStudentGroupPairs = new Tuples(true);
            // Construir los pares (profesor, grupo preferido)
            for (int tIdx = 0; tIdx < numTeachers; tIdx++) {
                Teacher teacher = teachers.get(tIdx);

                for (StudentGroup sg : teacher.getPreferredStudentGroups()) {
                    Integer studentGroupIdx = studentGroupIdToIndex.get(sg.getId());
                    if (studentGroupIdx != null) {
                        preferredTeacherStudentGroupPairs.add(tIdx, studentGroupIdx);
                    }
                }
            }

            List<BoolVar> teacherStudentGroupPreferencePenalty = new ArrayList<>();

            for (int i = 0; i < numUnits; i++) {
                ScheduledUnit unit = scheduledUnits.get(i);
                int studentGroupIdx = studentGroupIdToIndex.get(unit.getStudentGroup().getId());
                BoolVar prefersStudentGroup = model.boolVar("prefers_studentGroup_" + i);
                teacherStudentGroupPreferencePenalty.add(prefersStudentGroup);

                if (preferredTeacherStudentGroupPairs.nbTuples() != 0) {
                    model.ifThenElse(
                            model.table(new IntVar[]{unitTeacherVars[i], model.intVar(studentGroupIdx)}, preferredTeacherStudentGroupPairs),
                            model.arithm(prefersStudentGroup, "=", 0),
                            model.arithm(prefersStudentGroup, "=", 1)
                    );
                } else {
                    model.arithm(prefersStudentGroup, "=", 0).post();
                }
            }

            IntVar totalStudentGroupPreferencePenalty = model.intVar("total_studentGroup_preference_penalty", 0, numUnits);
            model.sum(teacherStudentGroupPreferencePenalty.toArray(new BoolVar[0]), "=", totalStudentGroupPreferencePenalty).post();
            // Puedes añadir totalSubjectPreferencePenalty a la función objetivo si lo deseas
        } else {
            System.err.println("Error: No teachers or units defined. Cannot add studentGroup preference soft constraints.");
        }
    //Todo: NO preferencias por grupo

        // Penalización de profesores por grupos de alumnos

        if (numTeachers > 0 && numUnits > 0) {
            // Paso 1: Crear un mapeo de subjectId (String) a índice (int)
            List<StudentGroup> allStudentGroups = studentGroups;
            Map<String, Integer> studentGroupIdToIndex = new HashMap<>();
            for (int idx = 0; idx < allStudentGroups.size(); idx++) {
                studentGroupIdToIndex.put(allStudentGroups.get(idx).getId(), idx);
            }

            Tuples unPreferredTeacherStudentGroupPairs = new Tuples(true);
            // Construir los pares (profesor, grupo no)
            for (int tIdx = 0; tIdx < numTeachers; tIdx++) {
                Teacher teacher = teachers.get(tIdx);
                List<StudentGroup> unPreferredStudentGroups = teacher.getUnPreferredStudentGroups();
                if (!unPreferredStudentGroups.isEmpty()) {
                    for (StudentGroup sg : unPreferredStudentGroups) {
                        Integer studentGroupIdx = studentGroupIdToIndex.get(sg.getId());
                        if (studentGroupIdx != null) {
                            unPreferredTeacherStudentGroupPairs.add(tIdx, studentGroupIdx);
                        }
                    }
                }

            }

            List<BoolVar> teacherStudentGroupUnPreferencePenalty = new ArrayList<>();

            for (int i = 0; i < numUnits; i++) {
                ScheduledUnit unit = scheduledUnits.get(i);
                int studentGroupIdx = studentGroupIdToIndex.get(unit.getStudentGroup().getId());
                BoolVar unPrefersStudentGroup = model.boolVar("unprefers_studentGroup_" + i);
                teacherStudentGroupUnPreferencePenalty.add(unPrefersStudentGroup);

                if (unPreferredTeacherStudentGroupPairs.nbTuples() != 0) {
                    model.ifThenElse(
                            model.table(new IntVar[]{unitTeacherVars[i], model.intVar(studentGroupIdx)}, unPreferredTeacherStudentGroupPairs),
                            model.arithm(unPrefersStudentGroup, "=", 1),
                            model.arithm(unPrefersStudentGroup, "=", 0)
                    );
                } else {
                    model.arithm(unPrefersStudentGroup, "=", 0).post();
                }
            }

            IntVar totalStudentGroupUnPreferencePenalty = model.intVar("total_studentGroup_unPreference_penalty", 0, numUnits);
            model.sum(teacherStudentGroupUnPreferencePenalty.toArray(new BoolVar[0]), "=", totalStudentGroupUnPreferencePenalty).post();
            // Puedes añadir totalSubjectPreferencePenalty a la función objetivo si lo deseas
        } else {
            System.err.println("Error: No teachers or units defined. Cannot add studentGroup preference soft constraints.");
        }
    }





*/

    public void addSoftConstraints() {
        // Obtener todas las condiciones de preferencia/no preferencia
        List<Condition> teacherPreferences = new ArrayList<>();
        List<Condition> teacherUnPreferences = new ArrayList<>();

        for (int tIdx = 0; tIdx < numTeachers; tIdx++) {
            Teacher teacher = teachers.get(tIdx);
            teacherPreferences.addAll(teacher.getPreferredConditions());
            teacherUnPreferences.addAll(teacher.getUnPreferredConditions());
        }

        // Variables para almacenar las penalizaciones
        List<IntVar> tpPreferencesPenalty = new ArrayList<>();
        List<IntVar> tpUnPreferencesPenalty = new ArrayList<>();

        // 1. Procesar las preferencias positivas (preferredConditions)
        for (Condition condition : teacherPreferences) {
            // Encontrar el índice del profesor
            int teacherIdx = -1;
            for (int i = 0; i < teachers.size(); i++) {
                if (teachers.get(i).getId().equals(condition.getTeacher().getId())) {
                    teacherIdx = i;
                    break;
                }
            }

            if (teacherIdx == -1) continue;

            // Procesar según el tipo de condición
            if (condition.getType() == "TimePeriod") {
                // Encontrar el índice del periodo
                int periodIdx = -1;
                for (int i = 0; i < timePeriods.size(); i++) {
                    if (timePeriods.get(i).getId().equals(condition.getTimePeriod().getId())) {
                        periodIdx = i;
                        break;
                    }
                }

                if (periodIdx == -1) continue;

                // Para cada unidad, añadir una penalización si no se cumple la preferencia
                for (int unitIdx = 0; unitIdx < numUnits; unitIdx++) {
                    IntVar penaltyVar = model.intVar("penalty_pref_tp_" + teacherIdx + "_" + periodIdx + "_" + unitIdx, 0, condition.getWeight());

                    // Si la unidad está asignada a este profesor y NO está en el periodo preferido
                    model.ifThenElse(
                            model.and(
                                    model.arithm(unitTeacherVars[unitIdx], "=", teacherIdx),
                                    model.arithm(unitTimePeriodVars[unitIdx], "!=", periodIdx)
                            ),
                            model.arithm(penaltyVar, "=", condition.getWeight()),
                            model.arithm(penaltyVar, "=", 0)
                    );

                    tpPreferencesPenalty.add(penaltyVar);
                }
            }
            // Añadir procesamiento similar para asignaturas y grupos preferidos
        }

        // 2. Procesar las preferencias negativas (unPreferredConditions)
        for (Condition condition : teacherUnPreferences) {
            // Encontrar el índice del profesor
            int teacherIdx = -1;
            for (int i = 0; i < teachers.size(); i++) {
                if (teachers.get(i).getId().equals(condition.getTeacher().getId())) {
                    teacherIdx = i;
                    break;
                }
            }

            if (teacherIdx == -1) continue;

            // Procesar según el tipo de condición
            if (condition.getType() == "TimePeriod") {

                // Encontrar el índice del periodo
                int periodIdx = -1;
                for (int i = 0; i < timePeriods.size(); i++) {
                    if (timePeriods.get(i).getId().equals(condition.getTimePeriod().getId())) {
                        periodIdx = i;
                        break;
                    }
                }

                if (periodIdx == -1) continue;

                // Para cada unidad, añadir una penalización si se asigna al periodo no preferido
                for (int unitIdx = 0; unitIdx < numUnits; unitIdx++) {
                    IntVar penaltyVar = model.intVar("penalty_unpref_tp_" + teacherIdx + "_" + periodIdx + "_" + unitIdx, 0, condition.getWeight());

                    // Si la unidad está asignada a este profesor Y está en el periodo NO preferido
                    model.ifThenElse(
                            model.and(
                                    model.arithm(unitTeacherVars[unitIdx], "=", teacherIdx),
                                    model.arithm(unitTimePeriodVars[unitIdx], "=", periodIdx)
                            ),
                            model.arithm(penaltyVar, "=", condition.getWeight()),
                            model.arithm(penaltyVar, "=", 0)
                    );

                    tpUnPreferencesPenalty.add(penaltyVar);
                }

            }
            // Añadir procesamiento similar para asignaturas y grupos no preferidos
        }

        IntVar totalTpPreferencesPenalty = model.intVar("totalTpPreferencesPenalty", 0, numUnits); // Asumiendo un peso máximo de 100
        model.sum(tpPreferencesPenalty.toArray(new IntVar[0]), "=", totalTpPreferencesPenalty).post();
        model.setObjective(Model.MINIMIZE, totalTpPreferencesPenalty);

        IntVar totalTpUnPreferencesPenalty = model.intVar("totalTpUnPreferencesPenalty", 0, numUnits); // Asumiendo un peso máximo de 100
        model.sum(tpUnPreferencesPenalty.toArray(new IntVar[0]), "=", totalTpUnPreferencesPenalty).post();
        model.setObjective(Model.MINIMIZE, totalTpUnPreferencesPenalty);

        // Crear variable para la suma total de penalizaciones
        //IntVar totalPenalty = model.intVar("total_penalty", 0, weightedPenaltyVars.size() * 100); // Asumiendo un peso máximo de 100

        // Sumar todas las penalizaciones
        //model.sum(weightedPenaltyVars.toArray(new IntVar[0]), "=", totalPenalty).post();


        // Establecer la función objetivo
        //model.setObjective(Model.MINIMIZE, totalPenalty);
    }










    private List<Schedule> solveModel(){

        List<Solution> solutions = new ArrayList<>();
        for (int i = 0; i < 10; i++) { // Limiting to 10 solutions for performance
            Solution solution = model.getSolver().findSolution();
            if (solution != null) {
                solutions.add(solution);
            } else {
                break; // No more solutions found
            }
        }
        List<Schedule> schedules = new ArrayList<>();
        for (Solution solution : solutions) {


            Schedule schedule = new Schedule(); // Your schedule representation

            if (solution != null) {
                System.out.println("Solution found!");
                for (int i = 0; i < numUnits; i++) {
                    int teacherIdx = solution.getIntVal(unitTeacherVars[i]);
                    int classroomIdx = solution.getIntVal(unitClassroomVars[i]);
                    int timePeriodIdx = solution.getIntVal(unitTimePeriodVars[i]);

                    //int periodPenalty = solution.getIntVal(teacherPreferencesPenalty.get(i));
                    //int unPreferredPenalty = solution.getIntVal(teacherUnPreferencesPenalty.get(i));

                    schedule.addAssignment(
                            scheduledUnits.get(i),
                            teachers.get(teacherIdx),
                            classrooms.get(classroomIdx),
                            timePeriods.get(timePeriodIdx)
                    );
                    /*System.out.println("Unidad " + i + ": Profesor " + teachers.get(teacherIdx).getName() +
                            ", Periodo " + timePeriods.get(timePeriodIdx).getId() +
                            ", Penalización preferencia periodo: " + periodPenalty +
                            ", Penalización NO preferido: " + unPreferredPenalty);*/

                    //int penalty = calculatePenalty(solution);
                    //System.out.println("Total penalty for this solution: " + penalty);

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
{}
    private int calculatePenalty(Solution solution) {
        int totalPenalty = 0;
        for (int i = 0; i < numUnits; i++) {
            totalPenalty += solution.getIntVal(teacherPreferencesPenalty.get(i));
            totalPenalty += solution.getIntVal(teacherUnPreferencesPenalty.get(i));
        }
        return totalPenalty;
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