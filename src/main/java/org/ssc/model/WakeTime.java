package org.ssc.model;

import java.time.LocalTime;

public class WakeTime {

    private LocalTime arrival;
    private int drive;
    private int preparation;
    private TransportType transType;
    private Location startLocation;
    private Location endLocation;

    public enum TransportType {
        ON_FOOT,
        CYCLE,
        OVPN,
        CAR
    }

    public WakeTime(LocalTime arrival, int drive, int preparation, TransportType transType, Location startLocation, Location endLocation) {
        this.arrival = arrival;
        this.drive = drive;
        this.preparation = preparation;
        this.transType = transType;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    public LocalTime getArrival() {
        return arrival;
    }

    public void setArrival(LocalTime arrival) {
        this.arrival = arrival;
    }

    public int getDrive() {
        return drive;
    }

    public void setDrive(int drive) {
        this.drive = drive;
    }

    public int getPreparation() {
        return preparation;
    }

    public void setPreparation(int preparation) {
        this.preparation = preparation;
    }

    public TransportType getTransType() {
        return transType;
    }

    public void setTransType(TransportType transType) {
        this.transType = transType;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }
}
