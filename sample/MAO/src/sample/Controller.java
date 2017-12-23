package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import java.net.URL;
import java.util.ResourceBundle;

import static java.lang.System.exit;

public class Controller implements Initializable {

    public  MidiDevice      input;
    public  MidiDevice      output;

    @FXML
    private GridPane        keyboard;

    @FXML
    public void note_played(int note_id, boolean start)
    {
        System.out.println(note_id + " - " + start);

        Button key = (Button) keyboard.getScene().lookup("#key" + note_id);
        boolean is_white_key[] = {true, false, true, false, true, true, false, true, false, true, false, true};

        if ( start )
        {
            System.out.println("Start");
            key.setStyle("-fx-background-color: red; -fx-border-color: #000");
        }
        else
        {
            System.out.println("Stop");
            key.setStyle("-fx-background-color: " + (is_white_key[note_id%12]  ? "#fff" : "#000" ) + "; -fx-border-color: #000");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        boolean defined = true;
        // true : Default / false : affiche les périphériques midi

        MidiDevice.Info[] midiDevices = MidiSystem.getMidiDeviceInfo();
        if ( defined )
        {
            try{
                // Ici tu rentre ton input et output en fonction de ce qu'il te faut
                input   = MidiSystem.getMidiDevice(midiDevices[1]);
                output  = MidiSystem.getMidiDevice(midiDevices[3]);

                Receiver rcvr;
                try {
                    output.open();
                    rcvr = new MyReceiver(this);
                    MidiSystem.getTransmitter().setReceiver(rcvr);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    exit(0);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            for ( int i = 0; i < midiDevices.length; i++ )
                System.out.println(midiDevices[i].getName());

            exit(0);
        }

    }


}
