package business;

public class ScheduledUnit {
    StudentGroup studentGroup;
    Subject subject;
    int uniqueId; // A unique identifier for this specific unit instance

    public ScheduledUnit(StudentGroup studentGroup, Subject subject, int uniqueId) {
        this.studentGroup = studentGroup;
        this.subject = subject;        this.uniqueId = uniqueId;
    }

    public StudentGroup getStudentGroup() {
        return studentGroup;
    }

    public void setStudentGroup(StudentGroup studentGroup) {
        this.studentGroup = studentGroup;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public String toString() {
        return "Unit-" + uniqueId + "[" + studentGroup.getId() + " - " + subject.getId() + "]";
    }
}

