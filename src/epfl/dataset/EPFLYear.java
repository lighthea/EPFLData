package epfl.dataset;

import java.time.Year;

public record EPFLYear(String unit,
                       int firstYear,
                       String semester) implements Comparable<EPFLYear> {

    public static EPFLYear defaultYear() { return new EPFLYear("NA", 1901, "0");}
    @Override
    public int compareTo(EPFLYear o) {
            return Integer.compareUnsigned(firstYear , o.firstYear);
    }
}
