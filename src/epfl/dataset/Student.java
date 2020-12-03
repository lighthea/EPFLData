package epfl.dataset;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record Student(Gender gender,
                      int sciper,
                      List<String> nationality,
                      Map<EPFLYear, EPFLCaract> caracteristics) {

    public EPFLYear entryYear()
    {
        return Collections.min(caracteristics.keySet());
    }

    public EPFLYear exitYear()
    {
        return Collections.max(caracteristics.keySet());
    }

    public int timeAtEPFL()
    {
        return exitYear().firstYear() - entryYear().firstYear();
    }

    public boolean validatedBA()
    {
        return  completedSemester("bachelor semestre 6");
    }

    public boolean beganMA()
    {
        return caracteristics.keySet().stream().map(EPFLYear::semester).anyMatch(u ->  u.toLowerCase().contains("master"));
    }

    public boolean validatedMA()
    {
        return completedSemester("master semestre 4");
    }

    public boolean didAnExchange()
    {
        return caracteristics.values().stream().anyMatch(c -> c.typeExchange().isPresent());
    }

    public boolean didAnExchangeAt(String university)
    {
        return caracteristics.values()
                .stream()
                .anyMatch(c -> c.typeExchange().isPresent() && c.typeExchange().get().destination().toLowerCase().contains(university.toLowerCase()));
    }

    public boolean tookAMinor()
    {
        return caracteristics.values().stream().anyMatch(c -> c.minor().isPresent());
    }

    public boolean tookASpe()
    {
        return caracteristics.values().stream().anyMatch(c -> c.specializationMA().isPresent());
    }

    public boolean completedSemester(String semester)
    {
        return caracteristics.keySet().stream().map(EPFLYear::semester).anyMatch(s -> s.toLowerCase().contains(semester.toLowerCase()));
    }
    public long nSemesterToComplete(String semester)
    {
        if(completedSemester(semester))
            return caracteristics.keySet().stream().map(EPFLYear::semester).filter(s -> s.toLowerCase().contains(semester.toLowerCase())).count();
        return -1;
    }

    public long semesterUntil(String semester)
    {
        if(completedSemester(semester)){
            EPFLYear year =  caracteristics.keySet().stream().filter(y -> y.semester().toLowerCase().contains(semester.toLowerCase())).findAny().orElseThrow();
            return caracteristics.keySet().stream().filter(y -> y.compareTo(year) < 1).mapToLong(y -> nSemesterToComplete(y.semester())).sum();
        }
        return -1;

    }
    public boolean isAMan()
    {
        return gender.equals(Gender.Male);
    }

     public boolean hasBeenInSection(String section)
    {
        return caracteristics().keySet().stream().map(EPFLYear::unit).anyMatch(se -> se.contains(section));
    }

     public boolean yearN(int year)
    {
        return caracteristics().keySet().stream().map(EPFLYear::firstYear).anyMatch(se -> se == year || se == year -1);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Student)
            return ((Student) obj).sciper == sciper;
        else
            return false;
    }

    public double semestersToCompleteMA() {
        long a = semesterUntil("bachelor semestre 6");
        if (a == -1)
            return semesterUntil("master semestre 4");
        return semesterUntil("master semestre 4") - a;
    }

    public double yearUntil(String semester)
    {
        return semesterUntil(semester) / 2.;
    }

    public double nYearToComplete(String semester)
    {
        return nSemesterToComplete(semester) / 2.;
    }
}
