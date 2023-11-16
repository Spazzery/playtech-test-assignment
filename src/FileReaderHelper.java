import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileReaderHelper<T> {
    public static List<String> readAllLines(String path) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to open file: " + path);
        }
        return lines;
    }

//    public static List<T> readAllLinesIntoObjects(String path, Class clazz) {
//        List<String> lines;
//        try {
//            lines = Files.readAllLines(Paths.get(path));
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to open file: " + path);
//        }
//
//        if (clazz == Player.class) {
//
//        }
//        else if (clazz == Match.class) {
//
//        }
//        else
//    }
}
