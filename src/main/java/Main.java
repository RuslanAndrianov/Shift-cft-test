import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    private static String outputPath = "";
    private static String prefix = "";
    private static boolean append = false;
    private static boolean shortStats = false;
    private static boolean fullStats = false;

    private static boolean canCreateOutputDir = true;

    private static final List<String> inputFiles = new ArrayList<>();
    private static final List<Long> integers = new ArrayList<>();
    private static final List<Double> floats = new ArrayList<>();
    private static final List<String> strings = new ArrayList<>();

    public static void main(String[] args) {
        parseArguments(args);
        processFiles();
        writeOutput();
        printStatistics();
    }

    private static void parseArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-o":
                    outputPath = args[++i];
                    break;
                case "-p":
                    prefix = args[++i];
                    break;
                case "-a":
                    append = true;
                    break;
                case "-s":
                    shortStats = true;
                    break;
                case "-f":
                    fullStats = true;
                    break;
                default:
                    inputFiles.add(args[i]);
                    break;
            }
        }
    }

    private static void processFiles() {
        for (String fileName : inputFiles) {
            try {
                processFile(fileName);
            } catch (IOException e) {
                System.err.println("Ошибка при обработке файла " + fileName);
                System.err.println();

                canCreateOutputDir = false;
            }
        }
    }

    private static void processFile(String fileName) throws IOException {
        String integerRegexp = "-?\\d+";
        String floatRegexp = "-?\\d*\\.\\d+(?:[eE][-+]?\\d+)?";

        List<String> lines = Files.readAllLines(Paths.get(fileName));
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            try {
                if (line.matches(integerRegexp)) {
                    integers.add(Long.parseLong(line));
                } else if (line.matches(floatRegexp)) {
                    floats.add(Double.parseDouble(line));
                } else {
                    strings.add(line);
                }
            } catch (NumberFormatException e) {
                System.err.println("Ошибка при парсинге строки " + line);
            }
        }
    }

    private static void writeOutput() {
        if (canCreateOutputDir) {
            createOutputDirectory();
        }

        String intFileName = "integers.txt";
        String floatFileName = "floats.txt";
        String strFileName = "strings.txt";

        if (!integers.isEmpty()) {
            writeDataToFile(integers, intFileName);
        }
        if (!floats.isEmpty()) {
            writeDataToFile(floats, floatFileName);
        }
        if (!strings.isEmpty()) {
            writeDataToFile(strings, strFileName);
        }
    }

    private static void createOutputDirectory() {
        String rootPath = "./";
        outputPath = rootPath + outputPath;
        Path path = Path.of(outputPath);

        if (Files.notExists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                System.err.println("Не удалось создать папку для вывода результатов! " + e.getMessage());
                System.err.println("Файлы будут созданы в корневой папке проекта!");
                System.err.println();

                outputPath = rootPath;
            }
        }
    }

    private static <T> void writeDataToFile(List<T> data, String fileName) {
        String fullPath = outputPath + File.separator + prefix + fileName;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullPath, append))) {
            for (T item : data) {
                writer.write(item.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Ошибка записи в файл " + fullPath);
            System.err.println();
        }
    }

    private static void printStatistics() {
        if (shortStats) {
            System.out.println("Краткая статистика:");
            System.out.println();
            System.out.println("Количество целых чисел: " + integers.size());
            System.out.println("Количество вещественных чисел: " + floats.size());
            System.out.println("Количество строк: " + strings.size());
            System.out.println();
        }

        if (fullStats) {
            System.out.println("Полная статистика:");
            System.out.println();
            printIntegerStats();
            printFloatStats();
            printStringStats();
        }
    }

    private static void printIntegerStats() {
        if (integers.isEmpty()) {
            return;
        }

        long min = Collections.min(integers);
        long max = Collections.max(integers);
        double sum = integers.stream().mapToLong(Long::longValue).sum();
        double average = sum / integers.size();

        System.out.println("* Целые числа:");
        System.out.println("Количество: " + integers.size());
        System.out.println("Минимум: " + min);
        System.out.println("Максимум: " + max);
        System.out.println("Сумма: " + sum);
        System.out.println("Среднее: " + average);
        System.out.println();
    }

    private static void printFloatStats() {
        if (floats.isEmpty()) {
            return;
        }

        double min = Collections.min(floats);
        double max = Collections.max(floats);
        double sum = floats.stream().mapToDouble(Double::doubleValue).sum();
        double average = sum / floats.size();

        System.out.println("* Вещественные числа:");
        System.out.println("Количество: " + floats.size());
        System.out.println("Минимум: " + min);
        System.out.println("Максимум: " + max);
        System.out.println("Сумма: " + sum);
        System.out.println("Среднее: " + average);
        System.out.println();
    }

    private static void printStringStats() {
        if (strings.isEmpty()) {
            return;
        }

        int minLength = strings.stream().mapToInt(String::length).min().orElse(0);
        int maxLength = strings.stream().mapToInt(String::length).max().orElse(0);

        System.out.println("* Строки:");
        System.out.println("Количество: " + strings.size());
        System.out.println("Длина самой короткой строки: " + minLength);
        System.out.println("Длина самой длинной строки: " + maxLength);
        System.out.println();
    }
}
