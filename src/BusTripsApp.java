import model.Arrival;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

public class BusTripsApp {

    public static void main(String[] args) throws Exception {

        if (args.length < 3) {
            System.out.println("Uporaba:");
            System.out.println("  java gtfs.BusTripsApp <stopId> <N> <absolute|relative>");
            System.out.println("Primer:");
            System.out.println("  java gtfs.BusTripsApp 12345 3 relative");
            return;
        }

        int stopId = Integer.parseInt(args[0]);
        int limit = Integer.parseInt(args[1]);
        boolean relative = args[2].equalsIgnoreCase("relative");

        ArrivalService service = new ArrivalService();
        StopService stopService = new StopService(Path.of("data/stops.txt"));
        List<Arrival> arrivals = service.getArrivals(
                Path.of("data/stop_times.txt"),
                Path.of("data/trips.txt"),
                Path.of("data/routes.txt"),
                String.valueOf(stopId),
                limit
        );

        // Poišči objekt Stop za izbrani stopId
        model.Stop stopObj = stopService.getAllStops().stream()
                .filter(s -> s.stopId().equals(String.valueOf(stopId)))
                .findFirst()
                .orElse(null);
        if (stopObj == null) {
            System.out.println("Stop: Unknown stop (" + stopId + ")");
        } else {
            System.out.println("Stop: " + stopObj.name() + " (" + stopObj.stopId() + ")");
        }

        if (arrivals.isEmpty()) {
            System.out.println("Ni prihodov v naslednjih 2 urah za stop_id=" + stopId);
            return;
        }

        LocalTime now = LocalTime.now();
  
        java.util.Map<String, java.util.List<Arrival>> byRoute = new java.util.LinkedHashMap<>();
        for (Arrival a : arrivals) {
            byRoute.computeIfAbsent(a.routeName(), k -> new java.util.ArrayList<>()).add(a);
        }
        for (var entry : byRoute.entrySet()) {
            System.out.println(entry.getKey() + ":");
            for (Arrival a : entry.getValue()) {
                String timeStr;
                if (relative) {
                    long min = Duration.between(now, a.arrivalTime()).toMinutes();
                    timeStr = "  in " + min + " min";
                } else {
                    timeStr = "  at " + a.arrivalTime();
                }
                System.out.println(timeStr);
            }
        }
    }
}
