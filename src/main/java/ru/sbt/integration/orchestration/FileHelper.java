package ru.sbt.integration.orchestration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by SBT-Vitchinkin-AV on 14.09.2017.
 */
public class FileHelper {

    /**
     * Получить все файлы из папки и вложенных папок, удовлетворяющие переданному предикату
     * @throws IOException
     */
    public static List<String> getFilePaths(String folderPath, Predicate<? super Path> filter) throws IOException {
        return Files.walk(Paths.get(folderPath))
                .filter(Objects::nonNull)
                .filter(Files::isRegularFile)
                .filter(filter)
                .map(Path::toString)
                .collect(Collectors.toList());
    }

    /**
     * Получить все файлы из папки и вложенных папок
     * @throws IOException
     */
    public static List<String> getFilePaths(String folderPath) throws IOException {
        return Files.walk(Paths.get(folderPath))
                .filter(Objects::nonNull)
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .collect(Collectors.toList());
    }

    /**
     * Получить файл из папки и удовлетворяющий переданному предикату
     * @throws IOException
     */
    public static Path getPath(String folderPath, Predicate<? super Path> filter) throws IOException {
        return Files.walk(Paths.get(folderPath))
                .filter(Objects::nonNull)
                .filter(Files::isRegularFile)
                .filter(filter)
                .findFirst()
                .orElseThrow(() -> new IOException("Файл не найден"));
    }
}
