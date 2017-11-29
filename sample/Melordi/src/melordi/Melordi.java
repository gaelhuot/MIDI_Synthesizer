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

        Instru mon_instru = new Instru();

        Clavier mon_clavier = new Clavier(mon_instru);//on créé un objet clavier
        root.getChildren().add(mon_clavier);//on l'ajoute à notre groupe root

        ChangeInstru mon_changeinstru = new ChangeInstru(mon_instru);//on créé notre nouvel objet
        root.getChildren().add(mon_changeinstru);



        primaryStage.setScene(scene);
        primaryStage.show();

        mon_clavier.requestFocus();

    }


}
