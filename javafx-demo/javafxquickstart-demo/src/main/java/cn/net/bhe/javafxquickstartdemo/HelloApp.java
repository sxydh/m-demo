package cn.net.bhe.javafxquickstartdemo;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class HelloApp extends Application {

    public static void launch(String[] args) {
        Application.launch(args);
    }

    private Parent buildRoot() {
        return new StackPane(new Text("Hello World"));
    }

    @Override
    public void start(Stage stage) {
        stage.setScene(new Scene(buildRoot(), 300, 300));
        stage.show();
    }


}