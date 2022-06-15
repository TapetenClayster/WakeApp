package org.ssc;

import org.ssc.model.Location;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WakeManager {

    private LocalTime arrival;
    private int preparation;
    private List<Location> startLocations;
    private Location chosenStartLocation;
    private List<Location> destinationLocations;
    private Location chosenDestinationLocation;
    private int transportType;
    private Duration travelDuration;
    private LocalTime wakeUp;
    private WakeDatabaseConnector dbConnector;

    private void start() {
        dbConnector = new WakeDatabaseConnector();

        String output = "Herzlich Willkommen zur WakeApp Terminal App\n"
                + "--------------------------------------------\n"
                + "Ihre gespeicherten Werte werden nun geladen, falls vorhanden.";

        System.out.println(output);

        if (this.retrieveDataFromMemory()) {
            System.out.println("Daten wurden geladen.");
        } else {
            this.retrieveDateFromTerminal();
        }

        this.calculate();


    }

    boolean retrieveDataFromMemory() {
        ArrayList<WakeTime> wakeTimeList = dbConnector.getWaketimes();

        if (wakeTimeList.isEmpty()) {
            return false;
        }

        for (WakeTime wakeTime : wakeTimeList) {
            // TODO load wake times
        }

        return true;
    }

    private void retrieveDateFromTerminal() {
        System.out.println("Es konnten keine Standardwerte eingelesen werden."
                + "Alle Werte werden auf 0 gesetzt");

        Scanner reader = new Scanner(System.in);

        System.out.println("Geben Sie die Ankunftszeit/Zielzeit ein (hh:mm)");
        String arrival_input = reader.next();

        while (!arrival_input.matches("[0-2]{0,1}[0-9]:[0-6]{0,1}[0-9]")) {
            System.out.println("Die Eingabe entspricht nicht dem angegebenen Format, bitte wiederholen Sie Ihre Eingabe: (hh:mm)");
            arrival_input = reader.next();
        }

        int arrival_h = Integer.parseInt(arrival_input.substring(0, arrival_input.indexOf(':')));
        int arrival_min = Integer.parseInt(arrival_input.substring(arrival_input.indexOf(':') + 1, arrival_input.length()));

        //TODO: More error handling for possible false input?

        this.arrival = LocalTime.of(arrival_h, arrival_min);

        System.out.println("Wie lange brauchen Sie, um sich morgens fertigzumachen? (in Minuten)");
        this.preparation = reader.nextInt();
        reader.nextLine();

        System.out.println("Von wo fahren Sie los? (Adresse)");
        String startLocationInput = reader.nextLine();
        this.startLocations = WakeNavigation.searchLocationRequest(startLocationInput);

        assert this.startLocations != null;
        this.chosenStartLocation = chooseLocation(this.startLocations, reader);

        System.out.println("Wo arbeiten Sie? (Adresse)");
        String destinationLocationInput = reader.nextLine();
        this.destinationLocations = WakeNavigation.searchLocationRequest(destinationLocationInput);

        assert this.destinationLocations != null;
        this.chosenDestinationLocation = chooseLocation(this.destinationLocations, reader);

        System.out.println("Womit kommen Sie zur Arbeit?");
        while (true) {
            System.out.println(
                    "(1) Auto\n" +
                            "(2) Fahrrad\n" +
                            "(3) ÖPNV\n" +
                            "(4) Zu Fuß\n" +
                            "(5) Rollstuhl\n"
            );

            int userVehicleChoice = reader.nextInt();

            if (userVehicleChoice < 1 || userVehicleChoice > 5) {
                System.out.println("Die Auswahl muss zwische 1 und 5 liegen.");
                continue;
            }

            this.transportType = userVehicleChoice;
            break;
        }

        System.out.println("Berechne den Kürzesten Weg zum Ziel... bitter warten...");
        this.travelDuration = WakeNavigation.navigationRequest(this.chosenStartLocation, this.chosenDestinationLocation, this.transportType);

        assert this.travelDuration != null;
        System.out.printf("\nDie Anfahrtszeit Beträgt ca. %s", LocalTime.MIDNIGHT.plus(this.travelDuration).format(DateTimeFormatter.ofPattern("HH:mm:ss")));

    }

    private Location chooseLocation(List<Location> toChooseLocations, Scanner reader) {
        System.out.println("Welche dieser gefundenen Adressen ist richtig?");
        System.out.println("(Wenn Ihre Adresse nicht dabei ist, starten Sie neu und versuchen Sie etwas spezifischer zu sein)");

        while (true) {
            for (int i = 0; i < toChooseLocations.size(); i++) {
                System.out.printf("\n(%d)\n", i + 1);
                System.out.println(toChooseLocations.get(i).toString());
            }

            int userAddressChoice;
            try {
                userAddressChoice = Integer.parseInt(reader.next());
                reader.nextLine();
            } catch (Exception e) {
                System.out.println("Invalide Zahleneingabe");
                continue;
            }

            if (userAddressChoice < 1 || userAddressChoice > toChooseLocations.size()) {
                System.out.printf("\nDie Auswahl muss zwische 1 und %s liegen.", toChooseLocations.size());
                continue;
            }

            return toChooseLocations.get(userAddressChoice - 1);
        }
    }

    private void calculate() {
        this.wakeUp = arrival.minusMinutes(
                        Integer.toUnsignedLong(this.preparation))
                .minus(this.travelDuration)
                .minusMinutes(5); //5 Minuten buffer

        System.out.printf("\nDie Weckzeit beträgt: %s Uhr", this.wakeUp.format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    public static void main(String[] args) {
        WakeManager wm = new WakeManager();
        wm.start();

        // DB Test
//        WakeDatabaseConnector db = new WakeDatabaseConnector();
//        db.test();
    }
}