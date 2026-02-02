package com.hortifacil.controller;

import com.hortifacil.model.AssinaturaModelo;
import com.hortifacil.service.AssinaturaService;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.util.List;

public class AdminAssinaturaController {

    private final AssinaturaService assinaturaService = new AssinaturaService();

    @FXML
    private ComboBox<AssinaturaModelo> comboPlanos;

    @FXML
    private Label lblUsuarios;

    @FXML
    private Label lblQuantidadeProdutos;

    @FXML
    private VBox cardPlano;
    @FXML
    private Label lblNome;
    @FXML
    private Label lblDescricao;
    @FXML
    private Label lblValor;
    @FXML
    private Label lblFrequencia;

    @FXML
    public void initialize() {
        carregarPlanos();
    }

    private void carregarPlanos() {
        List<AssinaturaModelo> modelos = assinaturaService.listarModelos();
        comboPlanos.getItems().setAll(modelos);
    }

    @FXML
    private void abrirNovaAssinatura() {
        Stage stage = (Stage) comboPlanos.getScene().getWindow();
        AppSplashController.trocarCenaComController(
                stage,
                "/view/AdminAssinaturaNovaView.fxml",
                "Nova Assinatura",
                (AdminAssinaturaNovaController controller) -> controller.setNovoModelo()
        );
    }

    @FXML
    private void abrirEditarAssinatura() {
        AssinaturaModelo modelo = comboPlanos.getValue();
        if (modelo == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Selecione um plano para editar!");
            alert.showAndWait();
            return;
        }

        Stage stage = (Stage) comboPlanos.getScene().getWindow();
        AppSplashController.trocarCenaComController(
                stage,
                "/view/AdminAssinaturaNovaView.fxml",
                "Editar Assinatura",
                (AdminAssinaturaNovaController controller) -> controller.setModeloParaEditar(modelo)
        );
    }
    
    @FXML
private void mostrarDetalhesPlano() {
    AssinaturaModelo modelo = comboPlanos.getValue();
    if (modelo != null) {
        lblNome.setText("Plano: " + modelo.getNome());
        lblDescricao.setText("Descrição: " + modelo.getDescricao());
        lblValor.setText("Valor: R$ " + modelo.getValor());
        lblFrequencia.setText("Frequência: " + modelo.getFrequencia() + " semanas");
        lblQuantidadeProdutos.setText("Quantidade de produtos: " + modelo.getQuantidadeProdutos());

        // Aqui buscamos no service quantos usuários estão com essa assinatura
        int qtdUsuarios = assinaturaService.contarUsuariosPorAssinatura(modelo.getIdModelo());
        lblUsuarios.setText("Usuários com este plano: " + qtdUsuarios);

        cardPlano.setVisible(true);
    } else {
        cardPlano.setVisible(false);
    }
}

    @FXML
    private void excluirAssinatura() {
        AssinaturaModelo modelo = comboPlanos.getValue();
        if (modelo == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Selecione um plano para excluir!");
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Excluir Assinatura");
        confirm.setContentText("Tem certeza que deseja excluir o plano \"" + modelo.getNome() + "\"?");
        confirm.showAndWait().ifPresent(res -> {
            if (res == javafx.scene.control.ButtonType.OK) {
                assinaturaService.excluirModelo(modelo.getIdModelo());
                carregarPlanos();
                cardPlano.setVisible(false);
            }
        });
    }

    @FXML
    private void voltarAssinatura() {
        Stage stage = (Stage) comboPlanos.getScene().getWindow();
        AppSplashController.trocarCena(stage, "/view/AdminHomeAssinaturaView.fxml", "Home");
    }
}
