package model;

import java.time.LocalTime;

public record Arrival(String routeName, LocalTime arrivalTime) {}