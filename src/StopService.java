import model.Stop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StopService {
	private final List<Stop> stops;

	public StopService(Path stopsFile) throws IOException {
		stops = new ArrayList<>();
		try (var lines = Files.lines(stopsFile)) {
			lines.skip(1).forEach(line -> {
				String[] row = line.split(",", -1);
				if (row.length > 2) {
					stops.add(new Stop(row[0], row[2]));
				}
			});
		}
	}

	public String getStopName(String stopId) {
		return stops.stream()
				.filter(s -> s.stopId().equals(stopId))
				.map(Stop::name)
				.findFirst()
				.orElse("Unknown stop");
	}

	public List<Stop> getAllStops() {
		return new ArrayList<>(stops);
	}
}
