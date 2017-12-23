package melordi;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Melordi extends Application {

    public static void main(String[] args) {
        Application.launch(Melordi.class, args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Melordi");
        Group root = new Group();
        Scene scene = new Scene(root, 500, 500, Color.WHITE);

        Metronome mon_metronome = new Metronome();
        root.getChildren().add(mon_metronome);

        Instru mon_instru = new Instru();

        Clavier mon_clavier = new Clavier(mon_instru);
        root.getChildren().add(mon_clavier);

        ChangeInstru mon_changeinstru = new ChangeInstru(mon_instru);
        root.getChildren().add(mon_changeinstru);

        Son mon_son = new Son(mon_clavier);
        mon_instru.volume.bind(mon_son.slider.valueProperty());//on lie les deux paramètres
        root.getChildren().add(mon_son);

        primaryStage.setScene(scene);
        primaryStage.show();
        mon_clavier.requestFocus();
    }



}
