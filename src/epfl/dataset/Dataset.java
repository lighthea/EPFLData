package epfl.dataset;

import org.apache.commons.math3.FieldElement;
import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.apache.commons.math3.linear.MatrixUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.sqrt;

public class Dataset {

    public Set<Student> getData() {
        return data;
    }

    private final Set<Student> data;

    public Dataset(final Set<Student> data)
    {
        this.data = Set.copyOf(data);
    }

    public Dataset(final List<Set<Student>> dataPerYear)
    {
        Map<Integer, Student> students = new HashMap<>();
        for(var year : dataPerYear)
            for (var student : year)
                if (!students.containsKey(student.sciper()))
                    students.put(student.sciper(), student);
                else students.get(student.sciper()).caracteristics().putAll(student.caracteristics());

        this.data = Set.copyOf(students.values());
    }

    public double probabilityOf(Predicate<Student> set)
    {
        return data.stream().filter(set).count()/(double)data.size();
    }

    public double conditionalProbabilityOf(Predicate<Student> set, Predicate<Student> knowing)
    {
        return probabilityOf(set.and(knowing))/probabilityOf(knowing);
    }

    public double mean (ToDoubleFunction<Student> randomV)
    {
        return data.parallelStream().mapToDouble(randomV).filter(d -> d >= 0).average().orElse(0);
    }

    public double variance (ToDoubleFunction<Student> randomV)
    {
        double E = mean(randomV);
        return mean(v -> Math.pow(randomV.applyAsDouble(v) - E, 2));
    }

    public double covariance (ToDoubleFunction<Student> randomV, ToDoubleFunction<Student> randomV2)
    {
        double E = mean(randomV);
        double E1 = mean(randomV2);
        return  mean(v ->(randomV.applyAsDouble(v) - E) * (randomV2.applyAsDouble(v) - E1));
    }

    public double correlation (ToDoubleFunction<Student> randomV, ToDoubleFunction<Student> randomV2)
    {
        return  covariance(randomV, randomV2)/(sqrt(variance(randomV) * variance(randomV2)));
    }

    public List<List<Double>> covarianceMatrix (List<ToDoubleFunction<Student> > randomVi)
    {
       return randomVi
               .stream()
               .map(toDoubleFunction -> (randomVi
                        .stream()
                        .map(studentToDoubleFunction -> covariance(toDoubleFunction, studentToDoubleFunction))
                        .collect(Collectors.toList())))
               .collect(Collectors.toList());
    }

    public double mean (ToDoubleFunction<Student> randomV, Predicate<Student> set)
    {
        return data.stream().filter(set).mapToDouble(randomV).filter(d -> d >= 0).average().orElse(0);
    }

    public double variance (ToDoubleFunction<Student> randomV, Predicate<Student> set)
    {
        double E = mean(randomV, set);
        return mean(v -> Math.pow(randomV.applyAsDouble(v) - E, 2), set);
    }

    public double covariance (ToDoubleFunction<Student> randomV, ToDoubleFunction<Student> randomV2, Predicate<Student> set)
    {
        double E = mean(randomV, set);
        double E1 = mean(randomV2, set);
        return  mean(v ->(randomV.applyAsDouble(v) - E) * (randomV2.applyAsDouble(v) - E1), set);
    }

    public double correlation (ToDoubleFunction<Student> randomV, ToDoubleFunction<Student> randomV2, Predicate<Student> set)
    {

        return covariance(randomV, randomV2, set)/((variance(randomV, set) * variance(randomV2, set)));
    }

    public List<List<Double>> covarianceMatrix (List<ToDoubleFunction<Student> > randomVi, Predicate<Student> set)
    {
        return randomVi
                .stream()
                .map(toDoubleFunction -> (randomVi
                        .stream()
                        .map(studentToDoubleFunction -> covariance(toDoubleFunction, studentToDoubleFunction, set))
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }



}
