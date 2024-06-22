package cn.net.bhe.javafxquickstartdemo;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static javafx.geometry.Pos.CENTER;

public class StartVsShownJavaFXApp extends Application {

    private final DoubleProperty startX = new SimpleDoubleProperty();
    private final DoubleProperty startY = new SimpleDoubleProperty();
    private final DoubleProperty shownX = new SimpleDoubleProperty();
    private final DoubleProperty shownY = new SimpleDoubleProperty();

    public static void launch(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        Label startLabel = new Label("Start Dimensions");
        TextField startTF = new TextField();
        startTF.textProperty().bind(Bindings.format("(%.1f, %.1f)", startX, startY));

        Label shownLabel = new Label("Shown Dimensions");
        TextField shownTF = new TextField();
        shownTF.textProperty().bind(Bindings.format("(%.1f, %.1f)", shownX, shownY));

        GridPane gp = new GridPane();
        gp.add(startLabel, 0, 0);
        gp.add(startTF, 1, 0);
        gp.add(shownLabel, 0, 1);
        gp.add(shownTF, 1, 1);
        gp.setHgap(10);
        gp.setVgap(10);

        HBox hbox = new HBox(gp);
        hbox.setAlignment(CENTER);

        VBox vbox = new VBox(hbox);
        vbox.setAlignment(CENTER);

        Scene scene = new Scene(vbox, 480, 320);

        stage.setScene(scene);

        startX.set(stage.getWidth());
        startY.set(stage.getHeight());

        stage.setOnShown((evt) -> {
            shownX.set(stage.getWidth());
            shownY.set(stage.getHeight());
        });

        stage.setTitle("Start Vs. Shown");
        stage.show();
    }

}
