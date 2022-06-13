package org.ssc;

import java.time.LocalTime;
import java.util.Scanner;

public class WakeManager {

    private LocalTime arrival;
    private Integer drive;
    private Integer preparation;
    private LocalTime wakeUp;

    private void start() {
        String output = "Herzlich Willkommen zur WakeApp Terminal App"
                + "--------------------------------------------"
                + "Ihre gespeicherten Werte werden nun geladen, falls vorhanden.";

        System.out.println(output);

        if (this.retrieveDataFromMemory()) {
            System.out.println("Daten wurden geladen.");
        } else {
            this.retrieveDateFromTerminal();
        }

        this.calculate();
    }

    Boolean retrieveDataFromMemory() {
        return false;
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

        Integer arrival_h = Integer.parseInt(arrival_input.substring(0, arrival_input.indexOf(':')));
        Integer arrival_min = Integer.parseInt(arrival_input.substring(arrival_input.indexOf(':') + 1, arrival_input.length()));

        //TODO: More error handling for possible false input?

        this.arrival = LocalTime.of(arrival_h, arrival_min);

        System.out.println("Wie lange brauchen Sie für Ihren normalen Fahrtweg? (in Minuten");
        this.drive = reader.nextInt();

        System.out.println("Wie lange brauchen Sie, um sich morgens fertigzumachen? (in Minuten)");
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
    }
}