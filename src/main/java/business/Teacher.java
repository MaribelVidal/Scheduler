package business;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Representa un profesor con sus preferencias, restricciones y datos personales.
 */
public class Teacher extends Entity {

    private String email;
    private String phone;
    private String department;
    private List<String> role;

    private final List<Subject> possibleSubjects;        // FIX: final + always mutable
    private int hoursWork;

    private final List<Condition> preferredConditions;   // FIX: final + mutable
    private final List<Condition> unPreferredConditions;

    private int achievedConditions;
    private int weightedConditions;

    private final List<TimePeriod> unavailableTimePeriods;   // FIX: final + mutable
    private final List<TimePeriod> preferredTimePeriods;
    private final List<TimePeriod> unPreferredTimePeriods;

    private final List<Subject> preferredSubjects;
    private final List<StudentGroup> preferredStudentGroups;
    private final List<StudentGroup> unPreferredStudentGroups;

    private double percentageAchievedConditions;
    private double percentageWeightedConditions;

    private List<Schedule> schedules = new ArrayList<>();

    public Teacher(String id, String name, String abbreviation) {
        super(id, name, abbreviation);
        this.possibleSubjects = new ArrayList<>();
        this.hoursWork = 25;

        this.unavailableTimePeriods = new ArrayList<>();
        this.preferredTimePeriods = new ArrayList<>();
        this.unPreferredTimePeriods = new ArrayList<>();
        this.preferredSubjects = new ArrayList<>();
        this.preferredStudentGroups = new ArrayList<>();
        this.unPreferredStudentGroups = new ArrayList<>();
        this.preferredConditions = new ArrayList<>();
        this.unPreferredConditions = new ArrayList<>();
        this.percentageAchievedConditions = 0.0;
        this.percentageWeightedConditions = 0.0;
    }

    // ---------------- Schedules ----------------
    public List<Schedule> getSchedules() {
        if (schedules == null) schedules = new ArrayList<>();
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) { this.schedules = schedules; }
    public void addSchedule(Schedule schedule) {
        if (schedules == null) schedules = new ArrayList<>();
        schedules.add(schedule);
    }
    public Schedule getScheduleById(String Id){
        if (schedules == null) return null;
        for (Schedule schedule : schedules) if (schedule.getId().equals(Id)) return schedule;
        return null;
    }

    // ---------------- Basics ----------------
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public List<String> getRole() { return role; }
    public void setRole(List<String> role) { this.role = role; }
    public int getHoursWork() { return hoursWork; }
    public void setHoursWork(int hoursWork) { this.hoursWork = hoursWork; }

    // ---------------- Possible subjects (ALWAYS MUTABLE) ----------------
    public List<Subject> getPossibleSubjects() { return possibleSubjects; }

    /** Replaces the internal list with a mutable copy. */
    public void setPossibleSubjects(List<Subject> list) {     // FIX: defensive copy
        this.possibleSubjects.clear();
        if (list != null) this.possibleSubjects.addAll(list);
    }
    public void addPossibleSubject(Subject subject){          // helper
        if (subject != null && !possibleSubjects.contains(subject)) possibleSubjects.add(subject);
    }
    public boolean removePossibleSubjectById(String subjectId){ // NEW
        return possibleSubjects.removeIf(s -> s.getId().equals(subjectId));
    }

    // ---------------- Availability ----------------
    public List<TimePeriod> getUnavailableTimePeriods() { return unavailableTimePeriods; }
    public void setUnavailableTimePeriods(List<TimePeriod> l) { // FIX: defensive copy
        unavailableTimePeriods.clear();
        if (l != null) unavailableTimePeriods.addAll(l);
    }
    public void addUnavailableTimePeriod(TimePeriod tp) { if (tp != null) unavailableTimePeriods.add(tp); }
    public boolean removeUnavailableTimePeriodById(String tpId) { // NEW
        return unavailableTimePeriods.removeIf(tp -> tp.getId().equals(tpId));
    }
    public boolean isAvailable(TimePeriod timePeriod) { return !unavailableTimePeriods.contains(timePeriod); }

    // ---------------- Preferred/Unpreferred TimePeriods ----------------
    public List<TimePeriod> getPreferredTimePeriods() { return preferredTimePeriods; }
    public void setPreferredTimePeriods(List<TimePeriod> list, int weight) {
        preferredTimePeriods.clear();
        if (list != null) preferredTimePeriods.addAll(list);
        // rebuild conditions for time periods of this type
        preferredConditions.removeIf(c -> c.getEntity() instanceof TimePeriod);
        if (list != null) for (TimePeriod tp : list) preferredConditions.add(new Condition(this, weight, tp));
    }
    public void addPreferredTimePeriod(TimePeriod tp, int weight) {
        if (tp == null) return;
        if (!preferredTimePeriods.contains(tp)) preferredTimePeriods.add(tp);
        preferredConditions.add(new Condition(this, weight, tp));
    }
    public boolean removePreferredTimePeriodById(String tpId) { // NEW (keeps conditions in sync)
        boolean removed = preferredTimePeriods.removeIf(tp -> tp.getId().equals(tpId));
        if (removed) preferredConditions.removeIf(c -> c.getEntity() instanceof TimePeriod
                && ((TimePeriod)c.getEntity()).getId().equals(tpId));
        return removed;
    }

    public List<TimePeriod> getUnPreferredTimePeriods() { return unPreferredTimePeriods; }
    public void setUnPreferredTimePeriods(List<TimePeriod> list, int weight) {
        unPreferredTimePeriods.clear();
        if (list != null) unPreferredTimePeriods.addAll(list);
        unPreferredConditions.removeIf(c -> c.getEntity() instanceof TimePeriod);
        if (list != null) for (TimePeriod tp : list) unPreferredConditions.add(new Condition(this, weight, tp));
    }
    public void addUnPreferredTimePeriod(TimePeriod tp, int weight) {
        if (tp == null) return;
        if (!unPreferredTimePeriods.contains(tp)) unPreferredTimePeriods.add(tp);
        unPreferredConditions.add(new Condition(this, weight, tp));
    }
    public boolean removeUnPreferredTimePeriodById(String tpId) { // NEW
        boolean removed = unPreferredTimePeriods.removeIf(tp -> tp.getId().equals(tpId));
        if (removed) unPreferredConditions.removeIf(c -> c.getEntity() instanceof TimePeriod
                && ((TimePeriod)c.getEntity()).getId().equals(tpId));
        return removed;
    }

    // ---------------- Preferred/Unpreferred Subjects ----------------
    public List<Subject> getPreferredSubjects() { return preferredSubjects; }
    public void setPreferredSubjects(List<Subject> list, int weight) {
        preferredSubjects.clear();
        if (list != null) preferredSubjects.addAll(list);
        preferredConditions.removeIf(c -> c.getEntity() instanceof Subject);
        if (list != null) for (Subject s : list) preferredConditions.add(new Condition(this, weight, s));
    }
    public void addPreferredSubject(Subject subject, int weight) {
        if (subject == null) return;
        if (!preferredSubjects.contains(subject)) preferredSubjects.add(subject);
        preferredConditions.add(new Condition(this, weight, subject));
    }
    public boolean removePreferredSubjectById(String subjectId) { // NEW
        boolean removed = preferredSubjects.removeIf(s -> s.getId().equals(subjectId));
        if (removed) preferredConditions.removeIf(c -> c.getEntity() instanceof Subject
                && ((Subject)c.getEntity()).getId().equals(subjectId));
        return removed;
    }

    // ---------------- Preferred/Unpreferred StudentGroups ----------------
    public List<StudentGroup> getPreferredStudentGroups() { return preferredStudentGroups; }
    public void setPreferredStudentGroups(List<StudentGroup> list, int weight) {
        preferredStudentGroups.clear();
        if (list != null) preferredStudentGroups.addAll(list);
        preferredConditions.removeIf(c -> c.getEntity() instanceof StudentGroup);
        if (list != null) for (StudentGroup g : list) preferredConditions.add(new Condition(this, weight, g));
    }
    public void addPreferredStudentGroup(StudentGroup g, int weight) {
        if (g == null) return;
        if (!preferredStudentGroups.contains(g)) preferredStudentGroups.add(g);
        preferredConditions.add(new Condition(this, weight, g));
    }
    public boolean removePreferredStudentGroupById(String groupId) { // NEW
        boolean removed = preferredStudentGroups.removeIf(g -> g.getId().equals(groupId));
        if (removed) preferredConditions.removeIf(c -> c.getEntity() instanceof StudentGroup
                && ((StudentGroup)c.getEntity()).getId().equals(groupId));
        return removed;
    }

    public List<StudentGroup> getUnPreferredStudentGroups() { return unPreferredStudentGroups; }
    public void setUnPreferredStudentGroups(List<StudentGroup> list, int weight) {
        unPreferredStudentGroups.clear();
        if (list != null) unPreferredStudentGroups.addAll(list);
        unPreferredConditions.removeIf(c -> c.getEntity() instanceof StudentGroup);
        if (list != null) for (StudentGroup g : list) unPreferredConditions.add(new Condition(this, weight, g));
    }
    public void addUnPreferredStudentGroup(StudentGroup g, int weight) {
        if (g == null) return;
        if (!unPreferredStudentGroups.contains(g)) unPreferredStudentGroups.add(g);
        unPreferredConditions.add(new Condition(this, weight, g));
    }
    public boolean removeUnPreferredStudentGroupById(String groupId) { // NEW
        boolean removed = unPreferredStudentGroups.removeIf(g -> g.getId().equals(groupId));
        if (removed) unPreferredConditions.removeIf(c -> c.getEntity() instanceof StudentGroup
                && ((StudentGroup)c.getEntity()).getId().equals(groupId));
        return removed;
    }

    // ---------------- Conditions & percentages ----------------
    public List<Condition> getPreferredConditions() { return preferredConditions; }
    public List<Condition> getUnPreferredConditions() { return unPreferredConditions; }
    public int getAchievedConditions() { return achievedConditions; }
    public int getWeightedConditions() { return weightedConditions; }

    public void setPreferredConditions(List<Condition> list) { // FIX: defensive copy
        preferredConditions.clear();
        if (list != null) preferredConditions.addAll(list);
    }
    public void setUnPreferredConditions(List<Condition> list) {
        unPreferredConditions.clear();
        if (list != null) unPreferredConditions.addAll(list);
    }

    public void setAchievedConditions(int achievedConditions) {
        this.achievedConditions = achievedConditions;
        int total = preferredConditions.size() + unPreferredConditions.size();
        this.percentageAchievedConditions = total == 0 ? 0.0 : (double) achievedConditions / total * 100.0;
    }

    public void setWeightedConditions(int weightedConditions) {
        this.weightedConditions = weightedConditions;
        double totalW = 0.0;
        for (Condition c : preferredConditions) totalW += c.getWeight();
        for (Condition c : unPreferredConditions) totalW += c.getWeight();
        this.percentageWeightedConditions = totalW <= 0 ? 0.0 : (double) weightedConditions / totalW * 100.0;
    }
    public double getPercentageAchievedConditions() { return percentageAchievedConditions; }
    public double getPercentageWeightedConditions() { return percentageWeightedConditions; }

    // -------- maps/updates for GUI --------
    public Map<String, Integer> getPreferredSubjectWeights() {
        return preferredConditions.stream()
                .filter(c -> c.getEntity() instanceof Subject)
                .collect(Collectors.toMap(c -> ((Subject)c.getEntity()).getId(), Condition::getWeight, (a,b)->b));
    }
    public Map<String, Integer> getPreferredStudentGroupWeights() {
        return preferredConditions.stream()
                .filter(c -> c.getEntity() instanceof StudentGroup)
                .collect(Collectors.toMap(c -> ((StudentGroup)c.getEntity()).getId(), Condition::getWeight, (a,b)->b));
    }
    public Map<String, Integer> getUnPreferredStudentGroupWeights() {
        return unPreferredConditions.stream()
                .filter(c -> c.getEntity() instanceof StudentGroup)
                .collect(Collectors.toMap(c -> ((StudentGroup)c.getEntity()).getId(), Condition::getWeight, (a,b)->b));
    }
    public Map<String, Integer> getPreferredTimePeriodWeights() {
        return preferredConditions.stream()
                .filter(c -> c.getEntity() instanceof TimePeriod)
                .collect(Collectors.toMap(c -> ((TimePeriod)c.getEntity()).getId(), Condition::getWeight, (a,b)->b));
    }
    public Map<String, Integer> getUnPreferredTimePeriodWeights() {
        return unPreferredConditions.stream()
                .filter(c -> c.getEntity() instanceof TimePeriod)
                .collect(Collectors.toMap(c -> ((TimePeriod)c.getEntity()).getId(), Condition::getWeight, (a,b)->b));
    }

    public void updatePreferredSubjectWeight(String id, int w) {
        for (Condition c : preferredConditions)
            if (c.getEntity() instanceof Subject s && s.getId().equals(id)) { c.setWeight(w); return; }
    }
    public void updatePreferredStudentGroupWeight(String id, int w) {
        for (Condition c : preferredConditions)
            if (c.getEntity() instanceof StudentGroup g && g.getId().equals(id)) { c.setWeight(w); return; }
    }
    public void updateUnPreferredStudentGroupWeight(String id, int w) {
        for (Condition c : unPreferredConditions)
            if (c.getEntity() instanceof StudentGroup g && g.getId().equals(id)) { c.setWeight(w); return; }
    }
    public void updatePreferredTimePeriodWeight(String id, int w) {
        for (Condition c : preferredConditions)
            if (c.getEntity() instanceof TimePeriod tp && tp.getId().equals(id)) { c.setWeight(w); return; }
    }
    public void updateUnPreferredTimePeriodWeight(String id, int w) {
        for (Condition c : unPreferredConditions)
            if (c.getEntity() instanceof TimePeriod tp && tp.getId().equals(id)) { c.setWeight(w); return; }
    }

    public boolean canTeach(Subject subject) {
        return possibleSubjects.contains(subject);
    }
}
