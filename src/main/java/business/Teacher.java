package business;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un profesor con sus preferencias, restricciones y datos personales.
 */
public class Teacher extends Entity {

    private String email; // Correo electrónico del profesor
    private String phone; // Teléfono de contacto
    private String department; // Departamento al que pertenece el profesor
    //private boolean tutorial;
    private List<String> role; // Roles del profesor (tutor, jefe de estudios, etc.)
    private List<Subject> possibleSubjects; // Asignaturas que puede impartir según sus habilitaciones
    //private TimePeriod timePeriod;
    private int hoursWork; // Máximo número de horas de trabajo semanales
    //private int assignedHoursWork; // Horas de docencia asignadas
    private List<Condition> preferredConditions;
    private List<Condition> unPreferredConditions;

    private int achievedConditions;
    private int weightedConditions; // Condiciones ponderadas cumplidas

    // Lista de lecciones asignadas al profesor

    //PREFERENCIAS Y RESTRICCIONES

    // Horario: Lista de periodos de tiempo en los que el profesor NO está disponible
    private List<TimePeriod> unavailableTimePeriods; // Horarios no disponibles

    // Horario: Lista de periodos de tiempo preferidos por el profesor
    private List<TimePeriod> preferredTimePeriods; // Horarios preferidos

    // Horario: Lista de periodos de tiempo NO deseables por el profesor
    private List<TimePeriod> unPreferredTimePeriods; // Horarios no deseados

    // Asignaturas: Lista de asignaturas preferidas por el profesor
    private List<Subject> preferredSubjects; // Asignaturas preferidas
    //Todo Controlar ls asignaturas preferidas estén incluidas en la lista de asignaturas posibles

    // Grupos de alumnos: Lista de grupos de grupos de alumnos preferidos por el profesor
    private List<StudentGroup> preferredStudentGroups; // Grupos de alumnos preferidos

    // Grupos de alumnos: Lista de grupos de grupos de alumnos No deseables por el profesor
    private List<StudentGroup> unPreferredStudentGroups; // Grupos de alumnos no deseado
    //Todo soft Constraint asociada a los grupos de estudiantes no deseados.
    //Todo Controlar las Soft y Hard Constraints asociadas a horas de docencia asignadas, no a las horas máximas

    /**
     * Constructor principal de Teacher.
     * @param id Identificador único
     * @param name Nombre completo
     * @param abbreviation Abreviatura
     */
    public Teacher(String id, String name, String abbreviation) {
        super(id, name, abbreviation);
        this.possibleSubjects= new ArrayList<>();
        this.hoursWork= 25;
        //Todo: Inicializar roles con un rol por defecto, por ejemplo "Profesor"
        //Todo: Inicializar horas de trabajo máximas por cada profesor
        //Todo: Inicializar las horas de docencia asignadas a cada profesor.
        this.unavailableTimePeriods= new ArrayList<>();
        this.preferredTimePeriods= new ArrayList<>();
        this.unPreferredTimePeriods= new ArrayList<>();
        this.preferredSubjects = new ArrayList<>();
        this.preferredStudentGroups = new ArrayList<>();
        this.unPreferredStudentGroups = new ArrayList<>();
        this.preferredConditions = new ArrayList<>();
        this.unPreferredConditions = new ArrayList<>();
    }

    // Métodos getter y setter para los datos personales y preferencias

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }

    public List<String> getRole() {
        return role;
    }
    public void setRole(List<String> role) {
        this.role = role;
    }

    public List<Subject> getPossibleSubjects() {
        return possibleSubjects;
    }
    public void setPossibleSubjects(List<Subject> possibleSubjects) {
        this.possibleSubjects = possibleSubjects;
    }
    /**
     * Añade una asignatura a la lista de posibles asignaturas que puede impartir el profesor.
     */
    public void addPossibleSubject(Subject subject){
        possibleSubjects.add(subject);
    }

    public int getHoursWork() {
        return hoursWork;
    }
    public void setHoursWork(int hoursWork) {
        this.hoursWork = hoursWork;
    }



    public List<TimePeriod> getUnavailableTimePeriods() {
        return unavailableTimePeriods;
    }
    public void setUnavailableTimePeriods(List<TimePeriod> unavailableTimePeriods) {
        this.unavailableTimePeriods = unavailableTimePeriods;
    }
    /**
     * Añade un periodo a la lista de periodos no disponibles.
     */
    public void addUnavailableTimePeriods(TimePeriod timePeriod) {
        unavailableTimePeriods.add(timePeriod);
    }

    /**
     * Comprueba si el profesor está disponible en un periodo de tiempo dado.
     * @param timePeriod Periodo de tiempo a comprobar
     * @return true si el profesor está disponible, false si no lo está
     */
    public boolean isAvailable(TimePeriod timePeriod) {
        return !unavailableTimePeriods.contains(timePeriod);
    }

    /**
     * Comprueba si el profesor puede impartir una asignatura.
     * @param subject Asignatura a comprobar
     * @return true si el profesor puede impartir la asignatura, false si no puede
     */
    public boolean canTeach(Subject subject) {    return possibleSubjects.contains(subject);}


    public List<TimePeriod> getPreferredTimePeriods() {
        return preferredTimePeriods;
    }

    public void setPreferredTimePeriods(List<TimePeriod> preferredTimePeriods, int weight) {
        this.preferredTimePeriods = preferredTimePeriods;
        //preferredConditions = new ArrayList<>();
        for (TimePeriod timePeriod : preferredTimePeriods) {

            preferredConditions.add (new Condition(this, weight, timePeriod))  ;

        }

    }
    /**
     * Añade un periodo a la lista de periodos preferidos.
     * @param timePeriod Periodo de tiempo preferido
     */
    public void addPreferredTimePeriod(TimePeriod timePeriod, int weight) {

        preferredTimePeriods.add(timePeriod);
        preferredConditions.add (new Condition(this, weight, timePeriod));
    }

    /**
     * Indica si el profesor prefiere un periodo dado.
     */
    public boolean prefersTimePeriod(TimePeriod timePeriod) {
        return preferredTimePeriods.contains(timePeriod);
    }

    public List<TimePeriod> getUnPreferredTimePeriods() {
        return unPreferredTimePeriods;
    }

    public void setUnPreferredTimePeriods(List<TimePeriod> unPreferredTimePeriods, int weight) {
        this.unPreferredTimePeriods = unPreferredTimePeriods;

        for (TimePeriod timePeriod : unPreferredTimePeriods) {

            unPreferredConditions.add (new Condition(this, weight, timePeriod))  ;

        }
    }


    public void addUnPreferredTimePeriod(TimePeriod timePeriod, int weight) {
        unPreferredTimePeriods.add(timePeriod);

        unPreferredConditions.add (new Condition(this, weight, timePeriod));

    }
    /**
     * Indica si al profesor le disgusta un periodo dado.
     */



    public boolean dislikesTimePeriod(TimePeriod timePeriod) {
        return unPreferredTimePeriods.contains(timePeriod);
    }

    public List<Subject> getPreferredSubjects() {
        return preferredSubjects;
    }
    public void setPreferredSubjects(List<Subject> preferredSubjects, int weight) {
        this.preferredSubjects = preferredSubjects;

        for (Subject subject : preferredSubjects) {

            preferredConditions.add (new Condition(this, weight, subject))  ;

        }
        
    }


    public void addPreferredSubject(Subject subject, int weight) {
        preferredSubjects.add(subject);
        preferredConditions.add (new Condition(this, weight, subject));
    }

    public List<StudentGroup> getPreferredStudentGroups() {
        return preferredStudentGroups;
    }


    public void setPreferredStudentGroups(List<StudentGroup> preferredStudentGroups, int weight) {
        this.preferredStudentGroups = preferredStudentGroups;
        for (StudentGroup studentGroup : preferredStudentGroups) {

            preferredConditions.add (new Condition(this, weight, studentGroup))  ;

        }
    }

    public void addPreferredStudentGroup(StudentGroup studentGroup, int weight) {
        preferredStudentGroups.add(studentGroup);
        preferredConditions.add (new Condition(this, weight, studentGroup));
    }




    public List<StudentGroup> getUnPreferredStudentGroups() {
        return unPreferredStudentGroups;
    }

    public void setUnPreferredStudentGroups(List<StudentGroup> unPreferredStudentGroups, int weight) {
        this.unPreferredStudentGroups = unPreferredStudentGroups;
        for (StudentGroup studentGroup : unPreferredStudentGroups) {

            unPreferredConditions.add (new Condition(this, weight, studentGroup))  ;

        }
    }

    public void addUnPreferredStudentGroup(StudentGroup studentGroup, int weight) {
        unPreferredStudentGroups.add(studentGroup);
        unPreferredConditions.add (new Condition(this, weight, studentGroup));
    }

    public List<Condition> getPreferredConditions() {
        return preferredConditions;
    }

    public List<Condition> getUnPreferredConditions() {
        return unPreferredConditions;
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
}
