package cn.net.bhe.javafxqsdemo;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class TransformApp extends Application {

    public static void launch(String[] args) {
        Application.launch(args);
    }

    private Parent buildRoot() {
        Rectangle box = new Rectangle(100, 50, Color.BLUE);
        transform(box);
        return new Pane(box);
    }

    private void transform(Rectangle box) {
        box.setTranslateX(100);
        box.setTranslateY(200);

        box.setScaleX(1.5);
        box.setScaleY(1.5);

        box.setRotate(30);
    }

    @Override
    public void start(Stage stage) {
        stage.setScene(new Scene(buildRoot(), 300, 300, Color.GRAY));
        stage.show();
    }

}
