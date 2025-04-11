package business;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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


    private List<Teacher> teacherCollection;
    private List<StudentGroup> groupOfStudentsCollection;
    private List<Subject> subjectCollection;
    private List<Classroom> classroomCollection;
    private List<TimePeriod> timePeriodCollection;

    private int numberOfLessons; //Lesson es el conjunto de las 5 variables principales
    private Map<Integer, List<List<Integer>>> solvedSchedules;

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

      lessons = new ArrayList<>();



   }


   private void defineVariables(){

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
           int teacher = l.getTeacher().getValue();
           int studentGroup = l.getStudentGroup().getValue();
           int subject = l.getSubject().getValue();
           int classroom = l.getClassroom().getValue();
           int timePeriod = l.getTimePeriod().getValue();

           String info = String.format("t%d - sg%d - s%d - c%d - tp%d" , teacher, studentGroup, subject, classroom, timePeriod);

           schedule[teacher][timePeriod] = info;
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
   private void transform(){}

    // Constructor para inicializar los horarios de los profesores, grupos de estudiantes, estudiantes y aulas

    public Map<Integer, List<List<Integer>>> getSolvedSchedules() {
        return solvedSchedules;
    }

    public List<List<Integer>> getSchedules(int id) {
        return solvedSchedules.get(id);
    }


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
