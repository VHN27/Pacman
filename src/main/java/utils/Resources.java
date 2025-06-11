package utils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import javafx.scene.image.Image;
import javafx.scene.media.Media;

public final class Resources {
    /** {@code Map}contenant les paths aux fonts. */
    private static Map<String, String> ttf = new HashMap<>();
    /** {@code Map}contenant les objets{@code Media}. */
    private static Map<String, Media> mp4 = new HashMap<>();
    /** {@code Map}contenant les paths aux wav. */
    private static Map<String, String> wav = new HashMap<>();
    /** {@code Map}contenant les objets{@code Image}. */
    private static Map<String, Image> png = new HashMap<>();
    /** {@code Map}contenant les paths aux maps. */
    private static Map<String, String> json = new HashMap<>();
    /** {@code List}contenant toutes les{@code Map}. */
    private static List<Map<String, String>> all = new ArrayList<>();

    private Resources() {
    }

    static {
        all.add(ttf);
        all.add(wav);
        all.add(json);
        try {
            URI fontsFolder = Resources.class.getClassLoader().getResource("fonts").toURI();
            Map<String, String> env;
            FileSystem fs;
            if ("jar".equals(fontsFolder.getScheme())) {
                env = new HashMap<>();
                env.put("create", "true");

                URI jarFileUri = URI.create(fontsFolder.toString().split("!")[0]);
                fs = FileSystems.newFileSystem(jarFileUri, env);

                Files.list(
                    fs.getPath("/fonts")
                ).forEach(Resources::sort);
                Files.list(
                    fs.getPath("/mp4")
                ).forEach(Resources::sort);
                Files.list(
                    fs.getPath("/png")
                ).forEach(Resources::sort);
                Files.list(
                    fs.getPath("/wav")
                ).forEach(Resources::sort);
                Files.list(
                    fs.getPath("/maps")
                ).forEach(Resources::sort);
                Files.list(
                    fs.getPath("/maps/mazeGenPieces")
                ).forEach(Resources::sort);
            } else {
                Files.list(Path.of("src/main/resources/fonts")).forEach(Resources::sort);
                Files.list(Path.of("src/main/resources/mp4")).forEach(Resources::sort);
                Files.list(Path.of("src/main/resources/png")).forEach(Resources::sort);
                Files.list(Path.of("src/main/resources/wav")).forEach(Resources::sort);
                Files.list(Path.of("src/main/resources/maps")).forEach(Resources::sort);
                Files.list(Path.of(
                    "src/main/resources/maps/mazeGenPieces"
                )).forEach(Resources::sort);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sort(final Path file) {
        switch (FilenameUtils.getExtension(file.toString())) {
            case "ttf":
                ttf.put(
                    FilenameUtils.removeExtension(
                        file.getFileName().toString()
                    ),
                    file.toUri().toString()
                );
                break;
            case "mp4":
                mp4.put(
                    FilenameUtils.removeExtension(
                        file.getFileName().toString()
                    ),
                    new Media(file.toUri().toString())
                );
                break;
            case "png":
                png.put(
                    FilenameUtils.removeExtension(
                        file.getFileName().toString()
                    ),
                    new Image(file.toUri().toString())
                );
                break;
            case "wav":
                wav.put(
                    FilenameUtils.removeExtension(
                        file.getFileName().toString()
                    ),
                    file.toUri().toString()
                );
                break;
            case "json":
                try {
                    json.put(
                        FilenameUtils.removeExtension(
                            file.getFileName().toString()
                        ),
                        readFileToString(file)
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
    }

    /**
     * Retourne le path du fichier ou le contenu des json.
     * @param name Nom du fichier
     * @return {@code String}path
     */
    public static String getPathOrContent(final String name) {
        for (Map<String, String> map : all) {
            if (map.containsKey(name)) {
                return map.get(name);
            }
        }
        return "";
    }

    /**
     * Retourne le contenu en{@code String}d'un fichier.
     * @param file {@code Path}
     * @return {@code String}
     */
    public static String readFileToString(final Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        StringBuilder content = new StringBuilder();
        for (String string : lines) {
            content.append(string);
        }
        return content.toString();
    }

    /**
     * Retourne l'objet{@code Image}correspondant au nom.
     * @param name Nom de l'image
     * @return {@code Image}
     */
    public static Image getImage(final String name) {
        return png.get(name);
    }

    /**
     * Retourne l'objet{@code Media}correspondant au nom.
     * @param name Nom de l'image
     * @return {@code Media}
     */
    public static Media getMedia(final String name) {
        return mp4.get(name);
    }
}
