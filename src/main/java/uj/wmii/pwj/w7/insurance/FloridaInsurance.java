package uj.wmii.pwj.w7.insurance;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

// Hardcoded values because compiler dislikes passing them for some reason.
// They are predetermined in exercise anyway.

public class FloridaInsurance {

    public static void main(String[] args) {
        try {
            List<InsuranceEntry> data = loadData();

            generateCountFile(data);
            generateTiv2012File(data);
            generateMostValuableFile(data);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<InsuranceEntry> loadData() throws IOException {
        try (ZipFile zipFile = new ZipFile("FL_insurance.csv.zip")) {
            ZipEntry entry = zipFile.getEntry("FL_insurance.csv");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8))) {

                return reader.lines()
                        .skip(1)
                        .map(line -> line.split(","))
                        .map(InsuranceEntry::fromCsvArray)
                        .collect(Collectors.toList());
            }
        }
    }

    private static void generateCountFile(List<InsuranceEntry> data) throws IOException {
        long uniqueCounties = data.stream()
                .map(InsuranceEntry::county)
                .distinct()
                .count();

        writeToFile("count.txt", String.valueOf(uniqueCounties));
    }

    private static void generateTiv2012File(List<InsuranceEntry> data) throws IOException {
        double totalTiv2012 = data.stream()
                .mapToDouble(InsuranceEntry::tiv2012)
                .sum();

        String formattedValue = String.format(Locale.US, "%.2f", totalTiv2012);
        writeToFile("tiv2012.txt", formattedValue);
    }

    private static void generateMostValuableFile(List<InsuranceEntry> data) throws IOException {
        List<String> topCounties = data.stream()
                .collect(Collectors.groupingBy(
                        InsuranceEntry::county,
                        Collectors.summingDouble(e -> e.tiv2012() - e.tiv2011())
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .map(entry -> String.format(Locale.US, "%s,%.2f", entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        writeToFile(topCounties);
    }

    private static void writeToFile(String filename, String content) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(filename),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(content);
        }
    }

    private static void writeToFile(List<String> lines) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of("most_valuable.txt"),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("country,value");
            writer.newLine();
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public record InsuranceEntry(String county, double tiv2011, double tiv2012) {
        public static InsuranceEntry fromCsvArray(String[] parts) {
            String county = parts[2];
            double tiv2011 = Double.parseDouble(parts[7]);
            double tiv2012 = Double.parseDouble(parts[8]);
            return new InsuranceEntry(county, tiv2011, tiv2012);
        }
    }
}