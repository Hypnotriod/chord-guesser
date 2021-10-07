package com.hypnotriod.utils;

public class NotesUtil {
    public static final String[] NOTES = new String[]{"A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#"};

    public static final double A4_HZ = 440;

    private static double getNoteIndexRaw(double frequency) {
        return (Math.log(frequency / A4_HZ) / Math.log(2)) * 12;
    }

    public static String getNoteCentsByFrequency(double frequency) {
        double noteIndex = getNoteIndexRaw(frequency);
        int cents = (int) Math.round((noteIndex - (int) noteIndex) * 100);
        return cents > 0 ? "+" + cents : "" + cents;
    }

    public static String getNoteByFrequency(double frequency) {
        int noteIndex = (int) Math.round(getNoteIndexRaw(frequency));
        int octaveNumber = 4;
        while (noteIndex < 0) {
            noteIndex += 12;
            octaveNumber--;
        }
        while (noteIndex >= 12) {
            noteIndex -= 12;
            octaveNumber++;
        }
        return NOTES[noteIndex] + octaveNumber;
    }
}
