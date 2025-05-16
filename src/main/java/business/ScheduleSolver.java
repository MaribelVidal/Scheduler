package business;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ScheduleSolver {

    /*private List<Schedule> teacherSchedules;
    private List<Schedule> studentGroupSchedules;
    private List<Schedule> studentSchedules;
    private List<Schedule> classroomSchedules;*/

    private Model model;

    private IntVar[][] schedule;

    private int numberOfTeachers;
    private int numberOfStudentesGroup;
    private int numberOfSubjects;
    private int numberOfClassrooms;
    private int numberOfTimePeriods;

    private Map<Integer, Teacher> indexToTeacherMap;
    private Map<Integer, StudentGroup> indexToStudentGroupMap;
    private Map<Integer, Subject> indexToSubjectMap;
    private Map<Integer, Classroom> indexToClassroomMap;
    private Map<Integer, TimePeriod> indexToTimePeriodMap;

    private List<Teacher> teacherCollection;
    private List<StudentGroup> groupOfStudentsCollection;
    private List<Subject> subjectCollection;
    private List<Classroom> classroomCollection;
    private List<TimePeriod> timePeriodCollection;

    private int numberOfLessons; //Lesson es el conjunto de las 5 variables principales
    private Map<Integer, List<List<Integer>>> solvedSchedulesTeacher; //Key=teacherId, valor=List<Lesson> donde cada Lesson es una lista de sus componentes.
    private Map<Integer, List<Integer>> solvedSchedulesSubject; //Key=subjectId, valor=List<LessonIds>
    private Map<Integer, List<Integer>> solvedSchedulesStudentGroup; //Key=studentGroupId, valor=List<LessonIds>
    private Map<Integer, List<Integer>> solvedSchedulesClassroom; //Key=classroomId, valor=List<LessonIds>
    private Map<Integer, List<List<Integer>>> solvedSchedulesTimePeriod; //Key=timePeriodId, valor=List<LessonIds>
    private List<Lesson> lessons;

    private List<IntVar> teacherVars;
    private List<IntVar> studentGroupVars;
    private List<IntVar> subjectVars;
    private List<IntVar> classroomVars;
    private List<IntVar> timePeriodVars;

   public ScheduleSolver(List<Teacher> teacherCollection, List<StudentGroup> groupOfStudentCollection,
                         List<Subject> subjectCollection, List<Classroom> classroomCollection, List<TimePeriod> timePeriodCollection){

       this.teacherCollection = teacherCollection;
       this.groupOfStudentsCollection = groupOfStudentCollection;
       this.subjectCollection = subjectCollection;
       this.classroomCollection = classroomCollection;
       this.timePeriodCollection = timePeriodCollection;

       // Inicializar modelo
         model = new Model("School Scheduling Problem");

         initializeVariables();

         defineVariables();
         defineHardConstraints();
         defineSoftConstraints();

         solve();


   }

   private void initializeVariables(){
      numberOfTeachers = teacherCollection.size();
      numberOfStudentesGroup = groupOfStudentsCollection.size();
      numberOfSubjects = subjectCollection.size();
      numberOfClassrooms = classroomCollection.size();
      numberOfTimePeriods = timePeriodCollection.size();

      //numberOfLessons = numberOfStudentesGroup * numberOfTimePeriods;
       numberOfLessons =3;
       //todo revisar
       System.out.println("numberOfLessons: " + numberOfLessons);
      lessons = new ArrayList<>();
      solvedSchedulesTeacher = new HashMap<>();

       // Crear mapeos de índice a entidad
       indexToTeacherMap = new HashMap<>();
       for (int i = 0; i < teacherCollection.size(); i++) {
           indexToTeacherMap.put(i, teacherCollection.get(i));
       }

       // Crear mapeos similares para los otros tipos de entidades
       indexToStudentGroupMap = new HashMap<>();
       for (int i = 0; i < groupOfStudentsCollection.size(); i++) {
           indexToStudentGroupMap.put(i, groupOfStudentsCollection.get(i));
       }

       // Y así sucesivamente para las otras colecciones
       indexToSubjectMap = new HashMap<>();
       for (int i = 0; i < subjectCollection.size(); i++) {
           indexToSubjectMap.put(i, subjectCollection.get(i));
       }
       indexToClassroomMap = new HashMap<>();
       for (int i = 0; i < classroomCollection.size(); i++) {
           indexToClassroomMap.put(i, classroomCollection.get(i));
       }
       indexToTimePeriodMap = new HashMap<>();
       for (int i = 0; i < timePeriodCollection.size(); i++) {
           indexToTimePeriodMap.put(i, timePeriodCollection.get(i));
       }

   }


   private void defineVariables(){
         // Definimos las variables de decisión

           teacherVars = new ArrayList<>();
           studentGroupVars = new ArrayList<>();
           subjectVars = new ArrayList<>();
           classroomVars = new ArrayList<>();
           timePeriodVars = new ArrayList<>();

           for (int i = 0; i < numberOfLessons; i++) {
               IntVar teacher = model.intVar("teacher" + i, 0, numberOfTeachers - 1);
               IntVar studentGroup = model.intVar("studentGroup" + i, 0, numberOfStudentesGroup - 1);
               IntVar subject = model.intVar("subject" + i, 0, numberOfSubjects - 1);
               IntVar classroom = model.intVar("classroom" + i, 0, numberOfClassrooms - 1);
               IntVar timePeriod = model.intVar("timePeriod" + i, 0, numberOfTimePeriods - 1);

               teacherVars.add(teacher);
               studentGroupVars.add(studentGroup);
               subjectVars.add(subject);
               classroomVars.add(classroom);
               timePeriodVars.add(timePeriod);
           }

   }

   private void defineHardConstraints(){

     //  sameTeacherDifferentClassroom();
       subjectHoursAndStudentGroups();
       teacherOnlyOncePerTimePeriod();
       subjectNotInSameTimePeriod();

   }

   private void defineSoftConstraints(){

   }

   private void solve(){
       // Resolvemos el problema
       Solver solver = model.getSolver();

       // Imprimimos la solución
       if (solver.solve()) {
           System.out.println("Solution found!");
           lessons.clear(); // Asegúrate de limpiar la lista antes de llenarla
           for (int i = 0; i < numberOfLessons; i++) {
               // Crear una nueva lección con los valores resueltos
               Lesson lesson = new Lesson(
                       teacherVars.get(i).getValue(),
                       studentGroupVars.get(i).getValue(),
                       subjectVars.get(i).getValue(),
                       classroomVars.get(i).getValue(),
                       timePeriodVars.get(i).getValue()
               );
               lessons.add(lesson); // Añadir la lección a la lista
           }

           transform();
       } else {
           System.out.println("No solution found.");
       }

   }

   // Métodos para definir Hard Constraints
   //no puede haber 1 profesor en el mismo periodo de tiempo en 2 Aulas (Classrooms)

    /*
   private void sameTeacherDifferentClassroom(){

       for (int i = 0; i < numberOfLessons; i++) {
           for (int j = i+1 ; j < numberOfLessons; j++) {

               /*
               BoolVar sameTeacher = model.boolVar("sameTeacher" + i + "_" + j);
               model.arithm(teacherCollection[i] , "=" , teacherCollection[j]).reifyWith(sameTeacher);
               BoolVar sameTimePeriod = model.boolVar("sameTimePeriod" + i + "_" + j);
               model.arithm(timePeriodCollection[i] , "=" , timePeriodCollection[j]).reifyWith(sameTimePeriod);
               BoolVar sameClassroom = model.boolVar("sameClassroom" + i + "_" + j);
               model.arithm(classroomCollection[i] , "=" , classroomCollection[j]).reifyWith(sameClassroom);

               model.ifThen(model.and(sameTeacher , sameTimePeriod) , model.arithm(sameClassroom, "<=", 1)); */
/*
               Lesson lesson1 = lessons.get(i);
               Lesson lesson2 = lessons.get(j);
               model.ifThen (
                       model.and(
                               model.arithm(lesson1.getTeacher(), "=" , lesson2.getTeacher()),
                               model.arithm(lesson1.getTimePeriod(), "=" , lesson2.getTimePeriod())
                               ),
                       model.arithm(lesson1.getClassroom(), "=", lesson2.getClassroom()));

           }


       }

   }*/

    //Cada studentgroup ha de hacer un número de horas de cada asignatura que le corresponda
    private void subjectHoursAndStudentGroups(){
        for (int i = 0; i < numberOfStudentesGroup; i++) {
            for (int j = 0; j < numberOfSubjects; j++) {

                Subject subject = indexToSubjectMap.get(j);
                int hours = subject.getWeeklyAssignedHours(); // Cambia esto por la lógica para obtener las horas de la asignatura
                //int hours = 0; // Cambia esto por la lógica para obtener las horas de la asignatura
                List<BoolVar> isSubjectAssigned = new ArrayList<>();

                for (int k = 0; k < numberOfLessons; k++) {
                    // Verificar si la lección pertenece al grupo de estudiantes y asignatura actuales
                    BoolVar isAssigned = model.boolVar("studentGroup" + i + "_subject" + j + "_lesson" + k);
                    model.ifThen(
                            model.and(
                                    model.arithm(studentGroupVars.get(k), "=", i),
                                    model.arithm(subjectVars.get(k), "=", j)
                            ),
                            model.arithm(isAssigned, "=", 1)
                    );
                    isSubjectAssigned.add(isAssigned);
                }


                model.sum(isSubjectAssigned.toArray(new BoolVar[0]), "=", hours).post();
            }

        }

    }
       //cada profesor sólo puede dar clase una sólo vez por TimePeriod
       private void teacherOnlyOncePerTimePeriod() {
           for (int t = 0; t < numberOfTimePeriods; t++) {
               for (int teacher = 0; teacher < numberOfTeachers; teacher++) {
                   // Lista para almacenar las lecciones asignadas al mismo profesor y período
                   List<BoolVar> lessonsInSamePeriod = new ArrayList<>();

                   for (int i = 0; i < numberOfLessons; i++) {
                       // Crear una variable booleana que indica si esta lección está asignada
                       // a este profesor en este período
                       BoolVar isAssigned = model.and(
                               model.arithm(teacherVars.get(i), "=", teacher),
                               model.arithm(timePeriodVars.get(i), "=", t)
                       ).reify();

                       lessonsInSamePeriod.add(isAssigned);
                   }

                   // Restricción: la suma debe ser ≤ 1 (máximo una lección por profesor y período)
                   model.sum(lessonsInSamePeriod.toArray(new BoolVar[0]), "<=", 1).post();
               }
           }
       }


    private void subjectNotInSameTimePeriod() {
        for (int subject = 0; subject < numberOfSubjects; subject++) {
            for (int group = 0; group < numberOfStudentesGroup; group++) {
                for (int t = 0; t < numberOfTimePeriods; t++) {
                    // Lista para almacenar las lecciones de esta asignatura y grupo en este período
                    List<BoolVar> lessonsInSamePeriod = new ArrayList<>();

                    for (int lesson = 0; lesson < numberOfLessons; lesson++) {
                        BoolVar isAssigned = model.and(
                                model.arithm(subjectVars.get(lesson), "=", subject),
                                model.arithm(studentGroupVars.get(lesson), "=", group),
                                model.arithm(timePeriodVars.get(lesson), "=", t)
                        ).reify();
                        lessonsInSamePeriod.add(isAssigned);
                    }

                    // No más de 1 lección de la misma asignatura para el mismo grupo en un período
                    model.sum(lessonsInSamePeriod.toArray(new BoolVar[0]), "<=", 1).post();
                }
            }
        }
    }

   //El traductor sólo es para debug

    public void printMatrix (){
       String[][] schedule = new String[numberOfTeachers][numberOfTimePeriods];

       for (Lesson l:lessons){

               int teacherIndex = l.getTeacher();
               int studentGroupIndex = l.getStudentGroup();
               int subjectIndex = l.getSubject();
               int classroomIndex = l.getClassroom();
               int timePeriodIndex = l.getTimePeriod();

               Teacher teacher = indexToTeacherMap.get(teacherIndex);
               StudentGroup studentGroup = indexToStudentGroupMap.get(studentGroupIndex);
               Subject subject = indexToSubjectMap.get(subjectIndex);
               Classroom classroom = indexToClassroomMap.get(classroomIndex);
               TimePeriod timePeriod = indexToTimePeriodMap.get(timePeriodIndex);

               String info = String.format("%s - %s - %s - %s - %s",
                       teacher.getId(),
                       studentGroup.getId(),
                       subject.getId(),
                       classroom.getId(),
                       timePeriod.getId());

               schedule[teacherIndex][timePeriodIndex] = info;
       }
        for (int i = 0; i < numberOfTeachers; i++) {

            System.out.println("teacher " + i);

            for (int j = 0; j < numberOfTimePeriods; j++) {

                System.out.println(schedule[i][j]);

            }

            System.out.println();
        }

    }



   // convierte la solución encontrada del csp al formato solvedSchedule
   // Convierte la solución encontrada del CSP al formato solvedSchedule
   private void transform() {
       // Inicializamos el mapa para almacenar los horarios resueltos
       solvedSchedulesTeacher = new HashMap<>();

       // Para cada lección que se ha resuelto en el solver
       for (Lesson lesson : lessons) {
           int teacherId = lesson.getTeacher();
           int timePeriodId = lesson.getTimePeriod();
           int subjectId = lesson.getSubject();
           int studentGroupId = lesson.getStudentGroup();
           int classroomId = lesson.getClassroom();

           // Creamos una lista para almacenar los detalles de la lección
           List<Integer> lessonDetails = new ArrayList<>();
           lessonDetails.add(subjectId);
           lessonDetails.add(studentGroupId);
           lessonDetails.add(classroomId);
           lessonDetails.add(timePeriodId);

           // Si el profesor no tiene un horario en el mapa, creamos uno nuevo
           if (!solvedSchedulesTeacher.containsKey(teacherId)) {
               // Inicializamos una lista para cada profesor
               solvedSchedulesTeacher.put(teacherId, new ArrayList<>());
           }

           // Añadimos los detalles de la lección a la lista del profesor
           solvedSchedulesTeacher
                   .computeIfAbsent(teacherId, k -> new ArrayList<>())
                   .add(lessonDetails);
       }

       System.out.println("Transformación completada. Número de profesores con horario: " + solvedSchedulesTeacher.size());

       // Si necesitas una matriz real [profesor][período]
       // puedes crearla así después de llenar el mapa
       createScheduleMatrix();
   }

    // Método para crear una matriz explícita de profesores x períodos
    private void createScheduleMatrix() {
        // Usaremos Object[][] para almacenar listas de detalles de lecciones
        Object[][] matrix = new Object[numberOfTeachers][numberOfTimePeriods];

        // Inicializamos la matriz con listas vacías
        for (int i = 0; i < numberOfTeachers; i++) {
            for (int j = 0; j < numberOfTimePeriods; j++) {
                matrix[i][j] = new ArrayList<List<Integer>>();
            }
        }

        // Llenamos la matriz con las lecciones organizadas
        for (Lesson lesson : lessons) {
            int teacherId = lesson.getTeacher();
            int timePeriodId = lesson.getTimePeriod();

            // Creamos la lista de detalles
            List<Integer> details = new ArrayList<>();
            details.add(lesson.getSubject());
            details.add(lesson.getStudentGroup());
            details.add(lesson.getClassroom());

            // Añadimos a la posición correspondiente en la matriz
            ((List<List<Integer>>) matrix[teacherId][timePeriodId]).add(details);
        }

        // Ahora matrix[i][j] contiene todas las lecciones del profesor i en el período j
        // Puedes acceder o guardar esta matriz según necesites
        schedule = new IntVar[numberOfTeachers][numberOfTimePeriods];
        // Nota: IntVar no es el tipo adecuado para schedule si quieres guardar lecciones resueltas
        // Deberías cambiar el tipo según lo que necesites almacenar
    }
    // Constructor para inicializar los horarios de los profesores, grupos de estudiantes, estudiantes y aulas

   /* public Map<Integer, List<Integer>> getSolvedSchedules() {
        return solvedSchedules;
    }

    public List<Integer> getSchedules(int id) {
        return solvedSchedules.get(id);
    } */


/*
    public Solver (){
        this.teacherSchedules = EntityManager.getTeacherSchedules();
        this.studentGroupSchedules = EntityManager.getStudentGroupSchedules();
        this.studentSchedules = EntityManager.getStudentSchedules();
        this.classroomSchedules = EntityManager.getClassroomSchedules();
    }



    // Método para sacar el horario (de momento prints). Para funciones de debug

    public void ScheduleShower (int id, String type) {
        String name = EntityManager.getEntity(id).getName();


        System.out.println("Horario del " + type + " con nombre " + name + " id " + id);

        int[][] schedule = EntityManager.getEntity(id).getSchedule().getSchedule();
        for (int i  = 0; i < schedule.length ; i++) {
            System.out.println(i +" ");
            for (int j = 0; j < schedule[i].length; j++) {
                System.out.println(j);

            }
            System.out.println("\n");
        }


    }
*/

}
