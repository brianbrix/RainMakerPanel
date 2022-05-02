package com.company.panel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;



public class RainMakerPanel extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(RainMakerPanel.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
        stage.setTitle("RAINMAKER");
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
//       runMini();
        launch();
    }



}