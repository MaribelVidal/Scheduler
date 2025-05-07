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

      numberOfLessons = numberOfStudentesGroup * numberOfTimePeriods;
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
         for (int i = 0; i < numberOfLessons; i++) {
              // Definimos las variables para cada lección
              IntVar teacher = model.intVar("teacher" + i, 0, numberOfTeachers - 1);
              IntVar studentGroup = model.intVar("studentGroup" + i, 0, numberOfStudentesGroup - 1);
              IntVar subject = model.intVar("subject" + i, 0, numberOfSubjects - 1);
              IntVar classroom = model.intVar("classroom" + i, 0, numberOfClassrooms - 1);
              IntVar timePeriod = model.intVar("timePeriod" + i, 0, numberOfTimePeriods - 1);

              lessons.add(new Lesson(teacher, studentGroup, subject, classroom, timePeriod));
         }

   }

   private void defineHardConstraints(){

       sameTeacherDifferentClassroom();

   }

   private void defineSoftConstraints(){

   }

   private void solve(){
       // Resolvemos el problema
       Solver solver = model.getSolver();

       // Imprimimos la solución
       if (solver.solve()) {
           System.out.println("Solution found!");
           transform();
       } else {
           System.out.println("No solution found.");
       }

   }

   // Métodos para definir Hard Constraints
   //no puede haber 1 profesor en el mismo periodo de tiempo en 2 Aulas (Classrooms)

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

   }

   //El traductor sólo es para debug

    public void printMatrix (){
       String[][] schedule = new String[numberOfTeachers][numberOfTimePeriods];

       for (Lesson l:lessons){

               int teacherIndex = l.getTeacher().getValue();
               int studentGroupIndex = l.getStudentGroup().getValue();
               int subjectIndex = l.getSubject().getValue();
               int classroomIndex = l.getClassroom().getValue();
               int timePeriodIndex = l.getTimePeriod().getValue();

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
           int teacherId = lesson.getTeacher().getValue();
           int timePeriodId = lesson.getTimePeriod().getValue();
           int subjectId = lesson.getSubject().getValue();
           int studentGroupId = lesson.getStudentGroup().getValue();
           int classroomId = lesson.getClassroom().getValue();

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
           solvedSchedulesTeacher.get(teacherId).add(lessonDetails);
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
            int teacherId = lesson.getTeacher().getValue();
            int timePeriodId = lesson.getTimePeriod().getValue();

            // Creamos la lista de detalles
            List<Integer> details = new ArrayList<>();
            details.add(lesson.getSubject().getValue());
            details.add(lesson.getStudentGroup().getValue());
            details.add(lesson.getClassroom().getValue());

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
