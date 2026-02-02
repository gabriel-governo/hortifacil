package com.hortifacil.controller;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

public class AdminHomeAssinaturaController {

    @FXML private Button btnCadastrarAssinatura;
    @FXML private Button btnListarPedidos;
    @FXML private Button btnVoltar;

    @FXML
    private void abrirCadastroAssinatura(ActionEvent event) {
        Stage stage = (Stage) btnCadastrarAssinatura.getScene().getWindow();
        AppSplashController.trocarCena(stage, "/view/AdminAssinaturaView.fxml", "Cadastrar Assinatura");
    }

    @FXML
    private void abrirListarPedidos(ActionEvent event) {
        Stage stage = (Stage) btnListarPedidos.getScene().getWindow();
        AppSplashController.trocarCena(stage, "/view/AdminPedidoAssinaturaListarView.fxml", "Pedidos de Assinaturas");
    }

    @FXML
    private void voltarHome(ActionEvent event) {
        Stage stage = (Stage) btnVoltar.getScene().getWindow();
        AppSplashController.trocarCena(stage, "/view/AdminHomeView.fxml", "Painel Administrativo");
    }
}
