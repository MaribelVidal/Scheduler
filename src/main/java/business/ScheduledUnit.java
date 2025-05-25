package business;

public class ScheduledUnit {
    StudentGroup studentGroup;
    Subject subject;
    int uniqueId; // A unique identifier for this specific unit instance

    public ScheduledUnit(StudentGroup studentGroup, Subject subject, int uniqueId) {
        this.studentGroup = studentGroup;
        this.subject = subject;
        this.uniqueId = uniqueId;
    }

    @Override
    public String toString() {
        return "Unit-" + uniqueId + "[" + studentGroup.getId() + " - " + subject.getId() + "]";
    }
}

