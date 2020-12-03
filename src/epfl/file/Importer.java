package epfl.file;

import epfl.dataset.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.time.Year;
import java.util.*;

import static epfl.dataset.EPFLYear.defaultYear;

public final class Importer {

    public static Set<Student> extractYear(final File file) throws IOException {
        try {

            Workbook workbook =   WorkbookFactory.create(file);
            Sheet datatypeSheet = workbook.getSheetAt(0);

            Map<Integer, Student> res = new HashMap<>();
            EPFLYear year = defaultYear();

            for (Row currentRow : datatypeSheet) {
                if (currentRow.getCell(0).getStringCellValue().contains("ét.)")) {
                    String unit = currentRow.getCell(0).getStringCellValue().substring(0, currentRow.getCell(0).getStringCellValue().indexOf("2") - 1);
                    String[] arr = (currentRow.getCell(0).getStringCellValue().substring(currentRow.getCell(0).getStringCellValue().indexOf("2"))).split(",");

                    String y = arr[0].substring(0, arr[0].lastIndexOf("-")).strip();
                    year = new EPFLYear(unit, Integer.parseInt(y) ,arr[1]);
                }
                else {
                    if (!year.equals(defaultYear()) && !currentRow.getCell(0).getStringCellValue().equals("Civilité")) {
                        if (currentRow.getCell(0).getStringCellValue().equals("Madame") ||
                                currentRow.getCell(0).getStringCellValue().equals("Monsieur")) {

                            Gender gender = Gender.Male;
                            int sciper = 0;
                            List<String> nationality;
                            Map<EPFLYear, EPFLCaract> caracteristics = new HashMap<>(1);
                            String orientationBA = null;
                            String orientationMA = null;
                            String specializationMA = null;
                            String option = null;
                            String minor = null;
                            String typeExchange = null;
                            String exchangeDest = null;
                            boolean present = false;
                            if (currentRow.getCell(0).getStringCellValue().equals("Madame"))
                                gender = Gender.Female;
                            orientationBA = currentRow.getCell(2).getStringCellValue();
                            orientationMA = currentRow.getCell(3).getStringCellValue();
                            specializationMA = currentRow.getCell(4).getStringCellValue();
                            option = currentRow.getCell(5).getStringCellValue();
                            minor = currentRow.getCell(6).getStringCellValue();
                            if(currentRow.getCell(7).getStringCellValue().equals("Présent"))
                                present = true;
                            typeExchange = currentRow.getCell(8).getStringCellValue();
                            exchangeDest = currentRow.getCell(9).getStringCellValue();
                            if(currentRow.getCell(10).getCellType() == CellType.STRING)
                                continue;
                            sciper = (int)currentRow.getCell(10).getNumericCellValue();
                            nationality = List.of(currentRow.getCell(12).getStringCellValue().split("[,;]"));

                            caracteristics.put(year,
                                        new EPFLCaract(
                                                Optional.ofNullable(orientationBA),
                                                Optional.ofNullable(orientationMA),
                                                Optional.ofNullable(specializationMA),
                                                Optional.ofNullable(option),
                                                Optional.ofNullable(minor),
                                                Optional.of(new Exchange(exchangeDest, typeExchange)),
                                                present));

                            Student et = new Student(gender, sciper, nationality, caracteristics);

                            if(res.containsKey(sciper))
                                res.get(sciper).caracteristics().putAll(caracteristics);
                            else
                                res.put(sciper, et);
                            }
                        }
                    }
                }
            workbook.close();
            return Set.copyOf(res.values());

        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new IOException("Problem while reading file");
    }

        public static List<Set<Student>> extractAllYear(final File directory) throws IOException {
            final String[] SUFFIX = {"xlsx"};  // use the suffix to filter
            Collection<File> files = FileUtils.listFiles(directory, SUFFIX, false);
            List<Set<Student>> res = new  ArrayList<>(files.size());

            files.stream().parallel().forEach(f -> {
                try {
                    res.add(extractYear(f));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Data " + f.getName() +" imported");
            });

            return res;
        }
}
