package com.hortifacil.controller;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

public class AdminHomeProdutoController {

    @FXML private Button btnAdicionarEstoque;
    @FXML private Button btnEstoqueAtual;
    @FXML private Button btnCadastroProdutos;
    @FXML private Button btnVoltar;

    // ==== MÉTODOS ====

    @FXML
    private void abrirAdicionarEstoque(ActionEvent event) {
        Stage stage = (Stage) btnAdicionarEstoque.getScene().getWindow();
        AppSplashController.trocarCena(stage, "/view/AdminAdicionarEstoqueView.fxml", "Controle de Estoque");
    }

    @FXML
    private void abrirEstoqueAtual() {
        Stage stage = (Stage) btnEstoqueAtual.getScene().getWindow();
        AppSplashController.trocarCena(stage, "/view/AdminEstoqueResumoView.fxml", "Estoque Atual");
    }

    @FXML
    private void abrirCadastroProdutos(ActionEvent event) {
        Stage stage = (Stage) btnCadastroProdutos.getScene().getWindow();
        AppSplashController.trocarCena(stage, "/view/AdminProdutoCadastroView.fxml", "Cadastro de Produtos");
    }

    @FXML
    private void voltarHome() {
        Stage stage = (Stage) btnVoltar.getScene().getWindow();
        AppSplashController.trocarCena(stage, "/view/AdminHomeView.fxml", "Painel Administrativo");
    }
}
