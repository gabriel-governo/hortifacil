package com.hortifacil.controller;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import java.sql.Connection;
import java.sql.SQLException;

import com.hortifacil.database.DatabaseConnection;

public class AdminHomeController {

    @FXML private Button btnProdutos;
    @FXML private Button btnPedidos;
    @FXML private Button btnRelatorios;
    @FXML private Button btnAssinaturas;
    @FXML private Button btnSair;


    // ==== MÉTODOS ====

    @FXML
    private void abrirTelaProdutos() {
        Stage stage = (Stage) btnProdutos.getScene().getWindow();
        AppSplashController.trocarCena(stage, "/view/AdminHomeProdutoView.fxml", "Produtos");
    }

    @FXML
    private void abrirTelaPedidos() {
        Stage stage = (Stage) btnPedidos.getScene().getWindow();
        AppSplashController.<AdminPedidoListarController>trocarCenaComDados(
            stage,
            "/view/AdminPedidoListarView.fxml",
            "Lista de Pedidos",
            controller -> controller.carregarPedidos()
        );
    }

    @FXML
    private void abrirRelatorios() {
        Stage stage = (Stage) btnRelatorios.getScene().getWindow();
        try {
            AppSplashController.trocarCenaComController(
                stage,
                "/view/AdminRelatorioView.fxml",
                "Relatórios",
                (AdminRelatorioController controller) -> {
                    try {
                        Connection conn = DatabaseConnection.getConnection();
                        controller.setConnection(conn);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        // pode exibir um Alert pro usuário se quiser
                    }
                }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirTelaAssinaturas() {
        Stage stage = (Stage) btnAssinaturas.getScene().getWindow();
        AppSplashController.trocarCena(
            stage,
            "/view/AdminHomeAssinaturaView.fxml", 
            "Assinaturas"
        );
    }

    @FXML
    private void voltarLogin() {
        Stage stage = (Stage) btnSair.getScene().getWindow();
        AppSplashController.trocarCena(stage, "/view/LoginView.fxml", "Login");
    }
}
