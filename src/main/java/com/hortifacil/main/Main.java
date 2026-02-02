package com.hortifacil.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.net.URL;

public class Main extends Application {

    private static final String CSS_PATH = "/css/style.css";

    @Override
    public void start(Stage stage) throws Exception {
        URL splashUrl = getClass().getResource("/view/AppSplashView.fxml");
        System.out.println("URL SplashView carregada: " + splashUrl);

        if (splashUrl == null) {
            throw new RuntimeException("SplashView.fxml não encontrado.");
        }

        Parent root = FXMLLoader.load(splashUrl);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(CSS_PATH).toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Teste Splash");
        stage.show();

        // Após 3 segundos, abre a tela de login
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> {
            try {
                openLogin(stage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        delay.play();
    }

    private void openLogin(Stage stage) throws Exception {
    URL loginUrl = getClass().getResource("/view/LoginView.fxml");
    System.out.println("URL LoginView carregada: " + loginUrl);

    if (loginUrl == null) {
        throw new RuntimeException("LoginView.fxml não encontrado.");
    }

    Parent root = FXMLLoader.load(loginUrl);
    Scene scene = new Scene(root);
    scene.getStylesheets().add(getClass().getResource(CSS_PATH).toExternalForm());

    stage.setScene(scene);
    stage.setTitle("HortiFácil - Login");

    stage.centerOnScreen(); // <-- garante que centraliza
    stage.show();
}


    public static void main(String[] args) {
        launch(args);
    }
}
