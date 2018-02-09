package sample;

import org.jfugue.player.Player;
import org.jfugue.realtime.RealtimePlayer;
import org.jfugue.theory.Note;

import javax.sound.midi.*;

class MyReceiver implements Receiver {

    Receiver rcvr;
    Player player;
    RealtimePlayer realtimePlayer;

    Controller controller;

    public MyReceiver(Controller controller) throws MidiUnavailableException {
        player = new Player();
        realtimePlayer = new RealtimePlayer();

        realtimePlayer.changeInstrument(1);
        this.controller = controller;

        try {
            this.rcvr = MidiSystem.getReceiver();
        } catch (MidiUnavailableException mue) {
            mue.printStackTrace();
        }
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
        byte[] b = message.getMessage();

        int note_id = b[1];
        String notes[] = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

        String jfugue_note = notes[note_id%12] + (note_id/12);

        Note note = new Note(jfugue_note);
        if ( b[2] != 0 ) {
            realtimePlayer.startNote(note);
            System.out.println(timeStamp + " - Start " + note);
            this.controller.note_played(note_id, true);
        }
        else {
            realtimePlayer.stopNote(note);
            System.out.println(timeStamp + " - Stop " + note);
            this.controller.note_played(note_id, false);
        }
        rcvr.send(message, timeStamp);
    }

    @Override
    public void close() {
        rcvr.close();
    }
}