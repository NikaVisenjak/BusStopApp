import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class GtfsLoader {

    public static Stream<String[]> readCsv(Path path) throws IOException {
        return Files.lines(path)
                .skip(1)
                .map(line -> line.split(","));
    }
}