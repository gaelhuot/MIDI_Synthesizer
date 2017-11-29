package MAO;

import com.sun.javafx.scene.control.skin.LabeledText;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import sun.security.ssl.Debug;

import javax.sound.midi.*;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.sun.org.apache.xalan.internal.lib.ExsltDatetime.time;

public class Controller implements Initializable {

    private int maxKey = 0;
    private int minKey = 1000;

    private int notes = 0;
    private int played = 0;

    private Button[] keysBtn;

    private File file;

    @FXML
    private TextField midiFilePath;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private ListView<String> listMidiDevices;

    @FXML
    private ListView<String> listInstrument;
    HashMap<String, Integer> instruments = new HashMap<>();

    @FXML
    private GridPane pianoKeys;


    /*
     * MIDI VARIABLES
     */
    private Sequence sequence;
    private Sequencer sequencer;

    private Synthesizer synthesizer;
    private MidiChannel midiChannel;
    private MyReceiver receiver;

    private MidiDevice inputDevice;
    private MidiDevice outputDevice;


    /*
     * KEYBOARD FUNCTION
     */

    private void resetKeyboard()
    {
        notes = 0;
        played = 0;
        maxKey = 0;
        minKey = 1000;
        keysBtn = null;
        pianoKeys.getChildren().clear();
    }


    void defaultGrid()
    {
        minKey = 48;
        maxKey = 72;
        ajustGrids();
    }

    private void ajustGrids()
    {
        boolean[] isBlackKey = {true, false, true, false, true, true, false, true, false, true, false, true};

        int maxDo = (maxKey/12 +1) * 12;
        int minDo = (minKey/12) * 12;

        int octaves = (maxDo - minDo) / 12;
        int keysCount = octaves * 12;

        double maxWidth     = 190;
        double maxHeight    = 766 / keysCount;


        // Touches du piano
        keysBtn = new Button[keysCount];

        for ( int i = 0; i < keysCount; i++ )
        {
            keysBtn[i] = new Button();
            String color = ( isBlackKey[i%12] ) ? "#ecf0f1" : "#000";
            keysBtn[i].setStyle("-fx-background-color: " + color + "; -fx-border-color: #000;");
            keysBtn[i].setMinWidth(maxWidth);
            keysBtn[i].setMinHeight(maxHeight);

            pianoKeys.add(keysBtn[i], 0, i);
        }

    }

    @FXML
    private void note_played(int note_id, boolean start)
    {
        int index = note_id - minKey;

        boolean[] isBlackKey = {true, false, true, false, true, true, false, true, false, true, false, true};


        if ( start )
            keysBtn[index].setStyle("-fx-background-color: red; -fx-border-color: #000");
        else
        {
            String color = ( isBlackKey[index%12] ) ? "#ecf0f1" : "#000";
            keysBtn[index].setStyle("-fx-background-color: " + color + "; -fx-border-color: #000");
        }
    }

    @FXML
    public void UserNote_played(int note_id, boolean start)
    {
        int index = note_id - minKey;

        boolean[] isBlackKey = {true, false, true, false, true, true, false, true, false, true, false, true};


        try{
            if ( start )
                keysBtn[index].setStyle("-fx-background-color: red; -fx-border-color: #000");
            else
            {
                String color = ( isBlackKey[index%12] ) ? "#ecf0f1" : "#000";
                keysBtn[index].setStyle("-fx-background-color: " + color + "; -fx-border-color: #000");
            }
        }catch (ArrayIndexOutOfBoundsException e)
        {
            Debug.println(e.getMessage(), "err");
        }

    }

    /*
     * END KEYBOARD FUNCTION
     */

    /*
     * READ MIDI FILE FUNCTION
     */

    private void getMidiFileInfo() throws InvalidMidiDataException, IOException {
        int NOTE_ON = 0x90;
        int NOTE_OFF = 0x80;

        Sequence sequence = MidiSystem.getSequence(file);

        int trackNumber = 0;
        Track[] tracks = sequence.getTracks();
        for ( int u = 0; u < tracks.length; u++ )
            System.out.println("Track " + u + " : " + tracks[u].size());

        Track track = sequence.getTracks()[1];
        System.out.println("Track " + trackNumber + ": size = " + track.size());
        System.out.println();
        for (int i=0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            MidiMessage message = event.getMessage();
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;
                if (sm.getCommand() == NOTE_ON) {
                    int key = sm.getData1();
                    maxKey = (key > maxKey) ? key : maxKey;
                    minKey = (key < minKey) ? key : minKey;
                    notes++;
                }
            }
        }

        maxKey = 120;
        minKey = 30;
    }

    private void addNotesToTrack(
            Track track,
            Track trk) throws InvalidMidiDataException {
        for (int ii = 0; ii < track.size(); ii++) {
            MidiEvent me = track.get(ii);
            MidiMessage mm = me.getMessage();
            if (mm instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) mm;
                int command = sm.getCommand();
                int com = -1;
                if (command == ShortMessage.NOTE_ON) {
                    com = 1;
                } else if (command == ShortMessage.NOTE_OFF) {
                    com = 2;
                }
                if (com > 0) {
                    byte[] b = sm.getMessage();
                    int l = (b == null ? 0 : b.length);
                    MetaMessage metaMessage = new MetaMessage(com, b, l);
                    MidiEvent me2 = new MidiEvent(metaMessage, me.getTick());
                    trk.add(me2);
                }
            }
        }
    }

    private void playMidi() throws InvalidMidiDataException, IOException, MidiUnavailableException {
        sequence = MidiSystem.getSequence(file);
        sequencer = MidiSystem.getSequencer();

        sequencer.open();

        receiver.sendReceiver = true;


        MetaEventListener mel = new MetaEventListener() {

            @Override
            public void meta(MetaMessage meta) {
                final int type = meta.getType();
                if ( meta.getData()[0] == -128 ||meta.getData()[2] == 0 )
                {
                    //System.out.println("Note off : " + meta.getData()[1]);
                    note_played(meta.getData()[1], false);
                }
                else if ( meta.getData()[0] == -112 )
                {
                    played++;
                    //System.out.println("Note on : " + meta.getData()[1] + "," + meta.getData()[2]);
                    note_played(meta.getData()[1], true);
                    float percent = (float) ( (float)played / (float)notes );

                    System.out.println("Notes : " + notes + " , played : " + played + " Percent:  " + percent);
                    progressBar.setProgress(percent);
                }
            }
        };


        sequencer.addMetaEventListener(mel);

        Track[] tracks = sequence.getTracks();
        Track trk = sequence.createTrack();
        for (Track track : tracks) {
            addNotesToTrack(track, trk);
        }

        sequencer.setSequence(sequence);
        sequencer.start();
    }

    /*
     * END READ MIDI FILE FUNCTION
     */

    /*
     * Event listener
     */

    // Button - Jouer un fichier midi
    public void playMidiFile(MouseEvent mouseEvent) {
        String path = midiFilePath.getText();
        File f = new File(path);

        if ( ! f.exists() )
            return;

        file = f;

        resetKeyboard();

        try {
            getMidiFileInfo();
        } catch (InvalidMidiDataException | IOException e) {
            e.printStackTrace();
        }
        ajustGrids();

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    playMidi();
                } catch (InvalidMidiDataException | IOException | MidiUnavailableException e) {
                    e.printStackTrace();
                }
            }
        });



    }

    // Button - Stoper la lecture du fichier midi
    public void stopMidiFile(MouseEvent mouseEvent) {
        if ( sequencer.isOpen() || sequencer.isRunning() )
        {
            sequencer.stop();
            resetKeyboard();
            minKey = 48;
            maxKey = 72;
            ajustGrids();

        }
    }

    // Button - Jouer avec un clavier
    public void playWithKeyboard(MouseEvent mouseEvent) throws MidiUnavailableException {
        int index = listMidiDevices.getSelectionModel().getSelectedIndex();

        MidiDevice.Info[] midiDevices = MidiSystem.getMidiDeviceInfo();
        inputDevice = MidiSystem.getMidiDevice(midiDevices[index]);

        receiver.sendReceiver = false;

        listInstrument.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 ) {
                    String name = listInstrument.getSelectionModel().getSelectedItem();
                    receiver.changeInstrument(instruments.get(name));
                }
            }
        });

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run()  {
                try {
                    MidiSystem.getTransmitter().setReceiver(receiver);
                } catch (MidiUnavailableException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    void initSeq() throws MidiUnavailableException {
        // TODO : Implémenter la fonctionnalité déjà codée

        synthesizer = MidiSystem.getSynthesizer();
        synthesizer.open();

        midiChannel = synthesizer.getChannels()[0];
        receiver = new MyReceiver(midiChannel, this);
    }


    public void loadMidiDevices(MouseEvent mouseEvent) throws MidiUnavailableException {
        listMidiDevices.getItems().clear();
        MidiDevice.Info[] midiDevices = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info midiDevice : midiDevices)
        {
            String name = MidiSystem.getMidiDevice(midiDevice).getDeviceInfo().getName();
            listMidiDevices.getItems().add(listMidiDevices.getItems().size(), name);
            if (Objects.equals(name, "Real Time Sequencer"))
                outputDevice = MidiSystem.getMidiDevice(midiDevice);
        }
    }

    // Button - Close
    public void closeApplication(MouseEvent mouseEvent) {
        if ( sequencer != null && sequencer.isOpen() && sequencer.isRunning() )
        {
            sequencer.stop();
            sequencer.close();
            resetKeyboard();
        }
        System.exit(0);
    }


    /*
     * END Event Listener
     */

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        minKey = 48;
        maxKey = 72;
        ajustGrids();


        try {
            initSeq();
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }


        instruments.put("Acoustic Piano", 1);
        instruments.put("Electric Piano", 3);
        instruments.put("Trompette", 56);
        instruments.put("Violon", 40);
        instruments.put("Acoustic Guitar", 26);
        instruments.put("Electric Guitar", 27);
        instruments.put("Bass", 37);
        instruments.put("Drum", 115);

        for(Map.Entry<String, Integer> entry : instruments.entrySet()) {
            String key = entry.getKey();
            listInstrument.getItems().add(key);
        }


    }


}
