package com.hortifacil.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AppSplashController {

    private static final double MIN_WIDTH = 800;
    private static final double MIN_HEIGHT = 600;

    private static void aplicarEstilo(Scene scene) {
        URL cssUrl = AppSplashController.class.getResource("/css/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
    }
    private static void configurarStage(Stage stage, Scene scene, String titulo, double largura, double altura) {
        stage.setScene(scene);
        if (titulo != null) {
            stage.setTitle(titulo);
        }
        stage.setResizable(true);
        stage.setMinWidth(largura);
        stage.setMinHeight(altura);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    public static void trocarCena(ActionEvent event, String caminhoFXML, String titulo, double largura, double altura) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        trocarCena(stage, caminhoFXML, titulo, largura, altura);
    }

    public static void trocarCena(ActionEvent event, String caminhoFXML, String titulo) {
        trocarCena(event, caminhoFXML, titulo, MIN_WIDTH, MIN_HEIGHT);
    }

    public static void trocarCena(Stage stage, String caminhoFXML, String titulo, double largura, double altura) {
        try {
            FXMLLoader loader = new FXMLLoader(AppSplashController.class.getResource(caminhoFXML));
            Parent root = loader.load();

            Scene scene = new Scene(root, largura, altura);
            aplicarEstilo(scene);

            configurarStage(stage, scene, titulo, largura, altura);
        } catch (IOException e) {
            System.err.println("Erro ao carregar FXML: " + caminhoFXML);
            e.printStackTrace();
        }
    }

    public static void trocarCena(Stage stage, String caminhoFXML, String titulo) {
        trocarCena(stage, caminhoFXML, titulo, MIN_WIDTH, MIN_HEIGHT);
    }

    public static <T> void trocarCenaComDados(ActionEvent event, String caminhoFXML, String titulo, Consumer<T> configurador, double largura, double altura) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        trocarCenaComDados(stage, caminhoFXML, titulo, configurador, largura, altura);
    }

    public static <T> void trocarCenaComDados(ActionEvent event, String caminhoFXML, String titulo, Consumer<T> configurador) {
        trocarCenaComDados(event, caminhoFXML, titulo, configurador, MIN_WIDTH, MIN_HEIGHT);
    }

    public static <T> void trocarCenaComDados(Stage stage, String caminhoFXML, String titulo, Consumer<T> configurador, double largura, double altura) {
        try {
            FXMLLoader loader = new FXMLLoader(AppSplashController.class.getResource(caminhoFXML));
            Parent root = loader.load();

            if (configurador != null) {
                T controller = loader.getController();
                configurador.accept(controller);
            }

            Scene scene = new Scene(root, largura, altura);
            aplicarEstilo(scene);

            configurarStage(stage, scene, titulo, largura, altura);
        } catch (IOException e) {
            System.err.println("Erro ao carregar FXML: " + caminhoFXML);
            e.printStackTrace();
        }
    }

    public static <T> void trocarCenaComDados(Stage stage, String caminhoFXML, String titulo, Consumer<T> configurador) {
        trocarCenaComDados(stage, caminhoFXML, titulo, configurador, MIN_WIDTH, MIN_HEIGHT);
    }

    public static <T, D> void trocarCenaComDados(Stage stage, String caminhoFXML, String titulo, D dados,
                                                 BiConsumer<T, D> configurador, double largura, double altura) {
        try {
            FXMLLoader loader = new FXMLLoader(AppSplashController.class.getResource(caminhoFXML));
            Parent root = loader.load();

            if (configurador != null) {
                T controller = loader.getController();
                configurador.accept(controller, dados);
            }

            Scene scene = new Scene(root, largura, altura);
            aplicarEstilo(scene);

            configurarStage(stage, scene, titulo, largura, altura);
        } catch (IOException e) {
            System.err.println("Erro ao carregar FXML: " + caminhoFXML);
            e.printStackTrace();
        }
    }
    public static <T, D> void trocarCenaComDados(Stage stage, String caminhoFXML, String titulo, D dados,
                                                 BiConsumer<T, D> configurador) {
        trocarCenaComDados(stage, caminhoFXML, titulo, dados, configurador, MIN_WIDTH, MIN_HEIGHT);
    }

    public static <T> T trocarCenaComController(Stage stage, String caminhoFXML, String titulo, Consumer<T> configurador) {
        try {
            FXMLLoader loader = new FXMLLoader(AppSplashController.class.getResource(caminhoFXML));
            Parent root = loader.load();

            T controller = loader.getController();
            if (configurador != null) {
                configurador.accept(controller);
            }

            Scene scene = new Scene(root, MIN_WIDTH, MIN_HEIGHT);
            aplicarEstilo(scene);

            configurarStage(stage, scene, titulo, MIN_WIDTH, MIN_HEIGHT);

            return controller;
        } catch (IOException e) {
            System.err.println("Erro ao carregar FXML: " + caminhoFXML);
            e.printStackTrace();
            return null;
        }
    }
}