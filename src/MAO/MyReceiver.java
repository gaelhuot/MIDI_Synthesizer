package MAO;

import javax.sound.midi.*;

class MyReceiver implements Receiver {

    Receiver rcvr;
    MidiChannel channel;
    Controller ctr;

    public boolean sendReceiver = false;

    public MyReceiver(MidiChannel channel, Controller ctr) throws MidiUnavailableException {

        this.ctr = ctr;

        this.channel = channel;
        try {
            this.rcvr = MidiSystem.getReceiver();
        } catch (MidiUnavailableException mue) {
            mue.printStackTrace();
        }
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
        byte[] b = message.getMessage();

        boolean velocity_on = false;

        int note_id = b[1];
        if ( b[2] != 0 ) {
            channel.noteOn(note_id, (velocity_on) ? b[2] : 100);
            ctr.UserNote_played(note_id, true);
        }
        else {
            channel.noteOff(note_id);
            ctr.UserNote_played(note_id, false);
        }
        if ( sendReceiver )
        rcvr.send(message, timeStamp);
    }

    void changeInstrument(int id)
    {
        System.out.println("Change instrument");
        channel.programChange(id);
    }

    @Override
    public void close() {
        rcvr.close();
    }
}