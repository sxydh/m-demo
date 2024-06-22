package cn.net.bhe.javafxqsdemo.ui_control;

import cn.net.bhe.mutil.StrUtils;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;

public class ChoiceApp extends Application {

    public static void launch(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Asset Class:");
        ChoiceBox<Pair<String, String>> choiceBox = new ChoiceBox<>();
        choiceBox.setPrefWidth(200);
        initChoice(choiceBox);
        Button saveButton = new Button("Save");

        HBox hbox = new HBox(
                label,
                choiceBox,
                saveButton);
        hbox.setSpacing(10.0);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(40));

        Scene scene = new Scene(hbox);

        saveButton.setOnAction(evt -> System.out.println("saving " + choiceBox.getValue()));

        primaryStage.setTitle("ChoicesApp");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initChoice(ChoiceBox<Pair<String, String>> choiceBox) {
        List<Pair<String, String>> items = new ArrayList<>();
        items.add(new Pair<>("Equipment", "20000"));
        items.add(new Pair<>("Furniture", "21000"));
        items.add(new Pair<>("Investment", "22000"));

        choiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pair<String, String> pair) {
                return pair.getKey();
            }

            @Override
            public Pair<String, String> fromString(String string) {
                return null;
            }
        });

        Pair<String, String> EMPTY_PAIR = new Pair<>(StrUtils.EMPTY, StrUtils.EMPTY);
        choiceBox.getItems().add(EMPTY_PAIR);
        choiceBox.getItems().addAll(items);
        choiceBox.setValue(EMPTY_PAIR);
    }

}
