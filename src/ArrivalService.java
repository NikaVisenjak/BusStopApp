import model.Arrival;
import model.Route;
import model.Trip;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrivalService {

    private static final Duration WINDOW = Duration.ofHours(2);


    public List<Arrival> getArrivals(
            Path stopTimes,
            Path trips,
            Path routes,
            String stopId,
            int maxPerRoute
    ) throws IOException {
        LocalTime now = LocalTime.now();
        LocalTime limit = now.plus(WINDOW);

        List<Trip> tripList = loadTrips(trips);
        List<Route> routeList = loadRoutes(routes);

        Map<String, String> tripToRoute = new HashMap<>();
        for (Trip t : tripList) {
            tripToRoute.put(t.tripId(), t.routeId());
        }

        Map<String, String> routeNames = new HashMap<>();
        for (Route r : routeList) {
            routeNames.put(r.routeId(), r.shortName());
        }

        Map<String, List<Arrival>> arrivalsPerRoute = new HashMap<>();

        GtfsLoader.readCsv(stopTimes).forEach(row -> {
            // stop_times.txt: trip_id,arrival_time,departure_time,stop_id,...
            if (row.length <= 3) return;
            if (!row[3].equals(stopId)) return;

            LocalTime arrival;
            try {
                arrival = LocalTime.parse(row[1]);
            } catch (Exception e) {
                return; // cas v cudnem formatu -> preskoci vrstico
            }

            if (arrival.isBefore(now) || arrival.isAfter(limit)) return;

            String tripId = row[0];
            String routeId = tripToRoute.get(tripId);
            if (routeId == null) return;

            String routeName = routeNames.get(routeId);
            if (routeName == null) return;

            arrivalsPerRoute
                    .computeIfAbsent(routeName, r -> new ArrayList<>())
                    .add(new Arrival(routeName, arrival));
        });

        // sortiraj prihode po casu in vzemi maxPerRoute za vsako linijo
        List<Arrival> result = new ArrayList<>();
        for (List<Arrival> list : arrivalsPerRoute.values()) {
            list.sort(Comparator.comparing(Arrival::arrivalTime));
            for (int i = 0; i < Math.min(maxPerRoute, list.size()); i++) {
                result.add(list.get(i));
            }
        }

        // koncni output se enkrat sortiramo globalno (da je lep izpis)
        result.sort(Comparator.comparing(Arrival::arrivalTime));
        return result;
    }

    private List<Trip> loadTrips(Path trips) throws IOException {
        List<Trip> tripList = new ArrayList<>();
        // trips.txt header: route_id,service_id,trip_id,...
        GtfsLoader.readCsv(trips).forEach(row -> {
            if (row.length <= 2) return;
            String routeId = row[0];
            String tripId = row[2];
            tripList.add(new Trip(tripId, routeId));
        });
        return tripList;
    }

    private List<Route> loadRoutes(Path routes) throws IOException {
        List<Route> routeList = new ArrayList<>();
        // routes.txt header: route_id,agency_id,route_short_name,...
        GtfsLoader.readCsv(routes).forEach(row -> {
            if (row.length <= 2) return;
            String routeId = row[0];
            String shortName = row[2];
            routeList.add(new Route(routeId, shortName));
        });
        return routeList;
    }
}
