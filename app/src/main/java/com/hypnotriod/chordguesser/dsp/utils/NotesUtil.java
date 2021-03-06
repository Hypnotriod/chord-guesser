package com.hypnotriod.chordguesser.dsp.utils;

public class NotesUtil {
    public static final String[] NOTES = new String[]{"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    public static final double A4_HZ = 440;

    public static double getNoteIndexFractional(double frequency) {
        return (Math.log(frequency / A4_HZ) / Math.log(2)) * 12 + 4 * 12 + 9;
    }

    public static String getNoteCentsByFrequency(double frequency) {
        double noteIndex = getNoteIndexFractional(frequency);
        int cents = (int) Math.round((noteIndex - (int) (noteIndex + 0.5)) * 200);
        return cents > 0 ? "+" + cents : "" + cents;
    }

    public static String getNoteByFrequency(double frequency) {
        int noteIndex = (int) Math.round(getNoteIndexFractional(frequency));
        int octaveNumber = 0;
        if (noteIndex < 0) {
            int octavesNum = noteIndex / -12 + 1;
            octaveNumber -= octavesNum;
            noteIndex = (octavesNum * 12 + noteIndex) % 12;
        } else if (noteIndex >= 12) {
            octaveNumber += noteIndex / 12;
            noteIndex %= 12;
        }
        return NOTES[noteIndex] + octaveNumber;
    }
}
