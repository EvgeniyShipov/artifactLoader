package ru.sbt.integration.orchestration.resourcereader;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResourceReader {
    public List<String> readResource(String resourceName) throws IOException, URISyntaxException {
        return readResource(resourceName, Charset.defaultCharset());
    }

    /**
     * Метод получения списка строк из файла ресурсов
     *
     * @param resourcePath относительный путь до ресурса(тоесть файл repositories1.txt в корне будет "repositories1.txt")
     * @param charset      Кодировка файла ресурсов
     */
    public List<String> readResource(String resourcePath, Charset charset) throws IOException, URISyntaxException {
        URI uri = ResourceReader.class.getClassLoader().getResource(cutResourcePath(resourcePath)).toURI();
        return Scheme.getScheme(uri.getScheme()).getLinesFromResource(uri, resourcePath, charset);
    }

    private String cutResourcePath(String resourcePath) {
        if (resourcePath.startsWith("./")) {
            return resourcePath.substring(2);
        }
        if (resourcePath.startsWith(".") || resourcePath.startsWith("/")) {
            return resourcePath.substring(1);
        }
        return resourcePath;
    }

    private enum Scheme {
        WAR {
            @Override
            List<String> getLinesFromResource(URI uri, String resourcePath, Charset charset) {
                List<String> lines = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                ResourceReader.class.getClassLoader().getResourceAsStream(resourcePath), charset))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        lines.add(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return lines;
            }
        },
        JAR {
            @Override
            List<String> getLinesFromResource(URI uri, String resourcePath, Charset charset) throws IOException {
                //synchronized (ResourceReader.class) {
                FileSystem f;
                try {
                    f = FileSystems.getFileSystem(uri);
                } catch (FileSystemNotFoundException e) {
                    f = null;
                }
                try (FileSystem fileSystem = f == null ?
                        FileSystems.newFileSystem(uri, Collections.emptyMap()) :
                        f) {
                    return Files.readAllLines(fileSystem.getPath(resourcePath), charset);
                }
                //}
            }
        },
        FILE {
            @Override
            List<String> getLinesFromResource(URI uri, String resourcePath, Charset charset) throws IOException {
                return Files.readAllLines(Paths.get(uri), charset);
            }
        };
        private static final String WAR_SCHEME = "vfs";

        abstract List<String> getLinesFromResource(URI uri, String resourcePath, Charset charset) throws IOException, URISyntaxException;

        static ResourceReader.Scheme getScheme(String scheme) {
            if (JAR.name().equalsIgnoreCase(scheme)) return JAR;
            if (WAR_SCHEME.equalsIgnoreCase(scheme) || WAR.name().equalsIgnoreCase(scheme)) return WAR;
            return FILE;
        }
    }
}
