package cn.net.bhe.javafxquickstartdemo.ui_control;

import cn.net.bhe.mutil.StrUtils;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class ComboBoxApp extends Application {

    private final Pair<String, String> EMPTY_PAIR = new Pair<>(StrUtils.EMPTY, StrUtils.EMPTY);

    public static void launch(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        Label label = new Label("Account:");
        ComboBox<Pair<String, String>> comboBox = new ComboBox<>();
        comboBox.setPrefWidth(200);
        Button saveButton = new Button("Save");

        HBox hbox = new HBox(
                label,
                comboBox,
                saveButton);
        hbox.setSpacing(10.0d);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(40));

        Scene scene = new Scene(hbox);

        initCombo(comboBox);

        saveButton.setOnAction((evt) -> {
            if (comboBox.getValue().equals(EMPTY_PAIR)) {
                System.out.println("no save needed; no item selected");
            } else {
                System.out.println("saving " + comboBox.getValue());
            }
        });

        stage.setTitle(ComboBoxApp.class.getSimpleName());
        stage.setScene(scene);
        stage.show();
    }

    private void initCombo(ComboBox<Pair<String, String>> comboBox) {
        List<Pair<String, String>> items = new ArrayList<>();
        items.add(new Pair<>("Auto Expense", "60000"));
        items.add(new Pair<>("Interest Expense", "61000"));
        items.add(new Pair<>("Office Expense", "62000"));
        items.add(new Pair<>("Salaries Expense", "63000"));

        comboBox.getItems().add(EMPTY_PAIR);
        comboBox.getItems().addAll(items);
        comboBox.setValue(EMPTY_PAIR);

        Callback<ListView<Pair<String, String>>, ListCell<Pair<String, String>>> factory = lv ->
                new ListCell<>() {
                    @Override
                    protected void updateItem(Pair<String, String> item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(StrUtils.EMPTY);
                        } else {
                            setText(item.getKey());
                        }
                    }
                };

        comboBox.setCellFactory(factory);
        comboBox.setButtonCell(factory.call(null));
    }

}
