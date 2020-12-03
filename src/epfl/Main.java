package epfl;

import epfl.dataset.Dataset;
import epfl.dataset.EPFLYear;
import epfl.dataset.Student;
import epfl.file.Importer;

import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    static final Map<String, ToDoubleFunction<Student>> variables = Map.of(
            " time to complete BA ", s -> s.semesterUntil("Bachelor semestre 6"),
            " time to complete MA ", Student::semestersToCompleteMA,
            " time at EPFL ", Student::timeAtEPFL
    );

    static final Map<String, Predicate<Student>> sets = Map.of(
            "", (s -> true),
            " being a man ", (Student::isAMan),
            " being a woman ", (s -> !s.isAMan()),
            " succeeding BA ", (Student::validatedBA),
            " succeeding MA ", (Student::validatedMA),
            " begin MA ", (Student::beganMA),
            " doing an exchange " , (Student::didAnExchange),
            " taking a minor ", (Student::tookAMinor)
    );

    static final Map<String, Function<String, Predicate<Student>>> ParametricSets = Map.of(
            " doing an exchange at ", str -> s -> s.didAnExchangeAt(str),
            " completing semester ", str -> s -> s.completedSemester(str),
            " being of nationality ", str -> s -> s.nationality().contains(str),
            " getting to semester ", str -> s -> s.completedSemester(str),
            " being in section ", str -> s -> s.hasBeenInSection(str),
            " being here in ", str -> s -> s.yearN(Integer.parseInt(str))

    );


    static final Map<String, Function<String, ToDoubleFunction<Student>>> ParametricVars = Map.of(
            " time to complete ", str -> s -> s.nSemesterToComplete(str),
            " time to complete all semesters until ", str -> s -> s.semesterUntil(str)
    );

    static final Map<String, Function<Dataset, Function<Predicate<Student>, Double>>> statsSet = Map.of(
            " probability of", d -> d::probabilityOf,
            " probability not of", d -> p -> d.probabilityOf(p.negate())
            );
    static final Map<String, Function<Dataset, BiFunction<Predicate<Student>, Predicate<Student>, Double>>> stats2Set = Map.of(
            " conditional probability of ", d           -> d::conditionalProbabilityOf,
            " probability of and", d -> (s1, s2)       -> d.probabilityOf(s1.and(s2)),
            " probability of or", d -> (s1, s2)        -> d.probabilityOf(s1.or(s2)),
            " probability of and not", d -> (s1, s2)   -> d.probabilityOf(s1.and(s2.negate())),
            " probability of or not", d -> (s1, s2)    -> d.probabilityOf(s1.or(s2.negate()))
    );

    static final Map<String, Function<Dataset, BiFunction<ToDoubleFunction<Student>, Predicate<Student>, Double>>> statsVar = Map.of(
            " mean of", d -> d::mean,
            " variance of", d -> d::variance
            );

    static final Map<String, Function<Dataset, Function<ToDoubleFunction<Student>, BiFunction<ToDoubleFunction<Student>, Predicate<Student>, Double>>>> stats2vars = Map.of(
            " covariance of", d -> v -> (v1, s) -> d.covariance(v, v1, s),
            " correlation of", d -> v -> (v1, s) -> d.correlation(v, v1, s)
    );

    public static void main(String[] args) throws IOException
    {
	// write your code here
        File dir =new  File(args[0]);
        if (dir.isDirectory())
        {
            Dataset data = new Dataset(Importer.extractAllYear(dir));

            Set<String> nationalities = data.getData().stream().flatMap(s -> s.nationality().stream())
                    .map(s -> s.strip().toLowerCase()).collect(Collectors.toUnmodifiableSet());
            Set<String> units = data.getData().stream().flatMap(s -> s.caracteristics().keySet().stream().map(EPFLYear::unit))
                    .map(s -> {
                        String str = s.strip();
                        int a = str.indexOf("(");
                        return str.substring(0, a == -1 ? str.length() : a);
                    }).collect(Collectors.toSet());
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("outM.txt")));

            bw.write("Nombres d'étudiants"+ "\n");
            bw.write("Probabilité d'être un homme : " + data.probabilityOf(Student::isAMan)+ "\n");
            bw.write("Probabilité d'être une femme : " + data.probabilityOf(s -> !s.isAMan())+ "\n");

            bw.write("Nationalités"+ "\n");
            for (String nat :
                    nationalities) {
                bw.write("Probabilité d'être " +  nat +" : " + data.probabilityOf(s -> s.nationality().contains(nat))+ "\n");
            }
            bw.write("Sections"+ "\n");

            for(String un :
                    units)
            {
                bw.write("Probabilité d'être en " +  un +" : " + data.probabilityOf(s ->s.caracteristics().keySet().stream().map(EPFLYear::unit).anyMatch(u -> u.equals(un)))+ "\n");
                bw.write("Probabilité d'être en " +  un +" et d'etre un homme : " + data.probabilityOf(s -> s.caracteristics().keySet().stream().map(EPFLYear::unit).anyMatch(u -> u.equals(un)) && s.isAMan())+ "\n");
                bw.write("Probabilité d'être en " +  un +" et d'etre une femme : " + data.probabilityOf(s -> s.caracteristics().keySet().stream().map(EPFLYear::unit).anyMatch(u -> u.equals(un))&& !s.isAMan())+ "\n");

                bw.write("Probabilité d'être en " +  un +" sachant qu'on est un homme : " + data.conditionalProbabilityOf(s -> s.caracteristics().keySet().stream().map(EPFLYear::unit).anyMatch(u -> u.equals(un)) , Student::isAMan)+ "\n");
                bw.write("Probabilité d'être en " +  un +" sachant qu'on est une femme : " + data.conditionalProbabilityOf(s -> s.caracteristics().keySet().stream().map(EPFLYear::unit).anyMatch(u -> u.equals(un)), s -> !s.isAMan())+ "\n");


                bw.write("Probabilité d'être un homme sachant qu'on est en " + un +": " + data.conditionalProbabilityOf(Student::isAMan, s -> s.caracteristics().keySet().stream().map(EPFLYear::unit).anyMatch(u -> u.equals(un)))+ "\n");
                bw.write("Probabilité d'être une femme sachant qu'on est en " + un +": "  + data.conditionalProbabilityOf(s -> !s.isAMan(), s -> s.caracteristics().keySet().stream().map(EPFLYear::unit).anyMatch(u -> u.equals(un)))+ "\n");


            }


            bw.write("Taux de passages généraux"+ "\n");

            bw.write("Probabilité de passer son BA sachant qu'on est un homme : " + data.conditionalProbabilityOf(Student::validatedBA, Student::isAMan)+ "\n");
            bw.write("Probabilité de passer son BA sachant qu'on est une femme : " + data.conditionalProbabilityOf(Student::validatedBA, s -> !s.isAMan())+ "\n");

            bw.write("Taux de passages et nationalité"+ "\n");

            for (String nat : nationalities) {
                bw.write("Probabilité de passer son BA sachant qu'on est " +  nat +" : " +
                        data.conditionalProbabilityOf(Student::validatedBA, s -> s.nationality().contains(nat))+ "\n");
                bw.write("Correlation entre le nombre de semestres passés à valider son BA et être " + nat +" : "+
                        data.correlation(
                                s -> s.semesterUntil("bachelor semestre 6"),
                                s -> s.nationality().contains(nat) ? 1 : 0)+ "\n");
            }
            bw.write("Taux de passages et section"+ "\n");
            for(String un : units)
            {
                bw.write("Probabilité de passer son BA sachant qu'on est en " +  un +" : "
                        + data.conditionalProbabilityOf(Student::validatedBA,
                        s -> s.caracteristics().keySet().stream().map(EPFLYear::unit).anyMatch(u -> u.equals(un)) && s.isAMan())+ "\n");
            }

            bw.flush();

            IntStream.range(2007, 2021).parallel().forEach(i -> {
                Predicate<Student> isInYear = s -> s.yearN(i);
                try {
                    bw.write("Nombres d'étudiants"+  "\n");
                    bw.write("Probabilité d'être un homme en " + i + " : " + data.probabilityOf(isInYear.and(Student::isAMan))+ "\n");
                    bw.write("Probabilité d'être une femme en " + i + " : " + data.probabilityOf(isInYear.and(s -> !s.isAMan()))+ "\n");
                    bw.write("\n");
                    bw.write("Nationalités"+ "\n");
                    for (String nat :
                            nationalities) {
                        bw.write("Probabilité d'être " +  nat +" en" + i + " : " + data.probabilityOf(isInYear.and(s -> s.nationality().contains(nat)))+ "\n");
                    }
                    bw.write( "\n");
                    bw.write("Sections"+ "\n");

                    for(String un :
                            units)
                    {
                        bw.write("Probabilité d'être en " +  un + " et en " + i + ": " + data.probabilityOf(isInYear.and(s ->s.caracteristics().keySet().stream().map(EPFLYear::unit).anyMatch(u -> u.equals(un))))+ "\n");
                        bw.write("Probabilité d'être en " +  un + " et en " + i + " et d'etre un homme : " + data.probabilityOf(isInYear.and(s -> s.caracteristics().keySet().stream().map(EPFLYear::unit).anyMatch(u -> u.equals(un)) && s.isAMan()))+ "\n");
                        bw.write("Probabilité d'être en " +  un + " et en " + i +" et d'etre une femme : " + data.probabilityOf(isInYear.and(s -> s.caracteristics().keySet().stream().map(EPFLYear::unit).anyMatch(u -> u.equals(un))&& !s.isAMan()))+ "\n");

                        bw.write("Probabilité d'être en " +  un + " sachant qu'on est en " + i + " et  est un homme : " + data.conditionalProbabilityOf((s -> s.caracteristics().keySet().stream().map(EPFLYear::unit).anyMatch(u -> u.equals(un))) , isInYear.and(Student::isAMan))+ "\n");
                        bw.write("Probabilité d'être en " +  un + " sachant qu'on est en " + i + " et une femme : " + data.conditionalProbabilityOf((s -> s.caracteristics().keySet().stream().map(EPFLYear::unit).anyMatch(u -> u.equals(un))), isInYear.and(s -> !s.isAMan()))+ "\n");


                        bw.write("Probabilité d'être un homme sachant qu'on est en " + i + "et  en " + un +" : " + data.conditionalProbabilityOf((Student::isAMan), isInYear.and(s -> s.caracteristics().keySet().stream().map(EPFLYear::unit).anyMatch(u -> u.equals(un))))+ "\n");
                        bw.write("Probabilité d'être une femme sachant qu'on est en " + i + " et en " + un +" : "  + data.conditionalProbabilityOf(isInYear.and(s -> !s.isAMan()), isInYear.and(s -> s.caracteristics().keySet().stream().map(EPFLYear::unit).anyMatch(u -> u.equals(un))))+ "\n");


                    }

                    bw.write( "\n");
                    bw.write("Taux de passages généraux"+ "\n");

                    bw.write("Probabilité de passer son BA sachant qu'on est un homme et en " + i +" : "  + data.conditionalProbabilityOf(Student::validatedBA, isInYear.and(Student::isAMan))+ "\n");
                    bw.write("Probabilité de passer son BA sachant qu'on est une femme et en " + i +" : " + data.conditionalProbabilityOf(Student::validatedBA, isInYear.and(s -> !s.isAMan()))+ "\n");

                    bw.write( "\n");
                    bw.write("Taux de passages et nationalité"+ "\n");

                    for (String nat : nationalities) {
                        bw.write("Probabilité de passer son BA sachant qu'on est " +  nat +" et en "+ i +" : " +
                                data.conditionalProbabilityOf(Student::validatedBA, isInYear.and(s -> s.nationality().contains(nat)))+ "\n");
                        bw.write("Correlation entre le nombre de semestres passés à valider son BA et être " + nat +" et etre en "+ i + " : "+
                                data.correlation(
                                        s -> s.semesterUntil("bachelor semestre 6"),
                                        (s -> s.nationality().contains(nat) ? 1 : 0))+ "\n");
                    }

                    bw.write( "\n");
                    bw.write("Taux de passages et section"+ "\n");
                    for(String un : units)
                    {
                        bw.write("Probabilité de passer son BA sachant qu'on est en " +  un +" et en "+ i + ": "
                                + data.conditionalProbabilityOf(Student::validatedBA,
                                isInYear.and(s -> s.caracteristics().keySet().stream().map(EPFLYear::unit).anyMatch(u -> u.equals(un)) && s.isAMan()))+ "\n");
                    }
                    bw.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            bw.close();

        }
    }
}
