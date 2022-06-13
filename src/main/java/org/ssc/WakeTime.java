package org.ssc;

import java.time.LocalTime;

public class WakeTime {

    private LocalTime arrival;
    private int drive;
    private int preparation;
    private TransportType transType;

    enum TransportType {
        ON_FOOT,
        CYCLE,
        OVPN,
        CAR
    }

    public WakeTime(LocalTime arrival, int drive, int preparation, TransportType transType) {
        this.arrival = arrival;
        this.drive = drive;
        this.preparation = preparation;
        this.transType = transType;
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
}
