package org.ssc;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Scanner;

public class WakeManager {

    private LocalTime arrival;
    private Integer drive;
    private Integer preparation;
    private LocalTime wakeUp;
    private WakeDatabaseConnector dbConnector;

    private void start() {
        dbConnector = new WakeDatabaseConnector();

        String output = "Herzlich Willkommen zur WakeApp Terminal App\n"
                + "--------------------------------------------\n"
                + "Möchten Sie [1] gespeicherte Daten laden oder [2] selbst Daten eingeben?";

        System.out.println(output);
        Scanner reader = new Scanner(System.in);
        int choice = reader.nextInt();

        while (!(choice == 1 || choice == 2)) {
            System.out.println("Bitte wählen Sie eine der zwei Möglichkeiten: 1 oder 2");
            choice = reader.nextInt();
        }

        if (choice == 1 && this.retrieveDataFromMemory()) {
            System.out.println("Daten wurden geladen.");
        } else if (choice == 1 && !this.retrieveDataFromMemory()) {
            System.out.println("Leider konnten keine Daten geladen werden und müssen manuell eingegeben werden.");
            this.retrieveDataFromTerminal();
        } else {
            this.retrieveDataFromTerminal();
        }

        this.calculate();

        System.out.println("Möchten Sie eine weitere Weckzeit berechnen?\n" +
                "[1] Ja [2] Nein, Programm beenden"
        );

        int end_choice = reader.nextInt();

        while(!(end_choice == 1 | end_choice == 2)) {
            System.out.println("Für die Berechnung einer neuen Weckzeit wählen Sie die " +
                    "[1], für das Beenden des Programmes [2]."
            );

            end_choice = reader.nextInt();
        }

         if(end_choice == 1) {
             this.retrieveDataFromTerminal();
         }

         System.out.println("Bis zum nächsten Mal!");
         System.exit(0);
    }

    private boolean retrieveDataFromMemory() {
        /*
        ArrayList<WakeTime> wakeTimeList = dbConnector.getWaketimes();

        if (wakeTimeList.isEmpty()) {
            return false;
        }

        for (WakeTime wakeTime: wakeTimeList) {
            // TODO load wake times
        }*/

        return false;
    }

    private boolean checkInput(String input, Integer arrival_h, Integer arrival_min) {
        boolean match = false;

        if (input.matches("[0-2]{0,1}[0-9]:[0-6]{0,1}[0-9]")) {
            match = true;
        }

        if (0 <= arrival_h && arrival_h < 24 && 0 <= arrival_min && arrival_min < 60) {
            match = true;
        } else {
            match = false;
        }

        return match;
    }

    private void retrieveDataFromTerminal() {
        Scanner reader = new Scanner(System.in);

        System.out.println("Geben Sie die Ankunftszeit/Zielzeit ein (hh:mm):");
        String arrival_input = reader.next();

        Integer arrival_h = Integer.parseInt(arrival_input.substring(0, arrival_input.indexOf(':')));
        Integer arrival_min = Integer.parseInt(arrival_input.substring(arrival_input.indexOf(':') + 1, arrival_input.length()));

        while (!checkInput(arrival_input, arrival_h, arrival_min)) {
            System.out.println("Die eingegebene Zeit entspricht keiner legitimen Uhrzeit. Bitte geben Sie eine Uhrzeit im 24h Format an (hh:mm)");

            arrival_input = reader.next();
            arrival_h = Integer.parseInt(arrival_input.substring(0, arrival_input.indexOf(':')));
            arrival_min = Integer.parseInt(arrival_input.substring(arrival_input.indexOf(':') + 1, arrival_input.length()));
        }

        this.arrival = LocalTime.of(arrival_h, arrival_min);

        System.out.println("Wie lange brauchen Sie für Ihren normalen Fahrtweg? (in Minuten):");
        this.drive = reader.nextInt();

        System.out.println("Wie lange brauchen Sie, um sich morgens fertigzumachen? (in Minuten):");
        this.preparation = reader.nextInt();
    }

    private void calculate() {
        Integer arrival_in_min = this.arrival.getMinute() + this.arrival.getHour() * 60;
        Integer wakeUpTime = arrival_in_min - this.drive - this.preparation;
        this.wakeUp = LocalTime.of(wakeUpTime/60, wakeUpTime % 60);

        System.out.println("Ihr Wecker wird um: " + this.wakeUp + " Uhr gestellt");
    }


    public static void main(String[] args) {
        WakeManager wm = new WakeManager();
        wm.start();

        // DB Test
//        WakeDatabaseConnector db = new WakeDatabaseConnector();
//        db.test();
    }
}