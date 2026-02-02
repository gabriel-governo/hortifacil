package com.hortifacil.controller;

import com.hortifacil.model.AssinaturaModelo;
import com.hortifacil.service.AssinaturaService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AdminAssinaturaNovaController {

    @FXML
    private TextField txtNome;

    @FXML
    private TextArea txtDescricao;

    @FXML
    private TextField txtValor;

    @FXML
    private ComboBox<String> comboFrequencia;

    @FXML
    private TextField txtQuantidadeProdutos;

    @FXML
    private Button btnExcluir;

    private final AssinaturaService assinaturaService = new AssinaturaService();
    private AssinaturaModelo modeloParaEditar;

    // Chamado para edição
    public void setModeloParaEditar(AssinaturaModelo modelo) {
        this.modeloParaEditar = modelo;
        txtNome.setText(modelo.getNome());
        txtDescricao.setText(modelo.getDescricao());
        txtValor.setText(String.valueOf(modelo.getValor()));
        txtQuantidadeProdutos.setText(String.valueOf(modelo.getQuantidadeProdutos()));
        comboFrequencia.getItems().setAll("Semanal", "Quinzenal", "Mensal");
        comboFrequencia.getSelectionModel().select(frequenciaNumeroParaTexto(modelo.getFrequencia()));
        btnExcluir.setVisible(true);
    }

    // Chamado para criar novo
    public void setNovoModelo() {
        this.modeloParaEditar = null;
        txtNome.clear();
        txtValor.clear();
        txtDescricao.setText(getDescricaoPadrao());
        txtQuantidadeProdutos.setText("5"); // valor padrão

        comboFrequencia.getItems().clear();
        comboFrequencia.getItems().addAll("Semanal", "Quinzenal", "Mensal");
        comboFrequencia.getSelectionModel().select("Semanal");

        btnExcluir.setVisible(false);
    }

    @FXML
    private void salvarModelo() {
        try {
            String nome = txtNome.getText();
            double valor = Double.parseDouble(txtValor.getText());
            int quantidadeProdutos = Integer.parseInt(txtQuantidadeProdutos.getText());
            String frequenciaTexto = comboFrequencia.getSelectionModel().getSelectedItem();
            int frequencia = frequenciaTextoParaNumero(frequenciaTexto);
            String descricao;

            if (modeloParaEditar != null) {
                descricao = txtDescricao.getText();
                modeloParaEditar.setNome(nome);
                modeloParaEditar.setDescricao(descricao);
                modeloParaEditar.setValor(valor);
                modeloParaEditar.setFrequencia(frequencia);
                modeloParaEditar.setQuantidadeProdutos(quantidadeProdutos);
                assinaturaService.salvarModelo(modeloParaEditar);
            } else {
                descricao = gerarDescricaoPadrao(nome, valor, frequenciaTexto);
                AssinaturaModelo novo = new AssinaturaModelo();
                novo.setNome(nome);
                novo.setDescricao(descricao);
                novo.setValor(valor);
                novo.setFrequencia(frequencia);
                novo.setQuantidadeProdutos(quantidadeProdutos);
                novo.setAtivo(true);
                assinaturaService.criarModelo(novo);
            }

            mostrarAlerta("Sucesso", "Plano salvo com sucesso!", Alert.AlertType.INFORMATION);
            voltarAdmin();

        } catch (NumberFormatException e) {
            mostrarAlerta("Erro", "Valor ou quantidade inválida!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void excluirModelo() {
        if (modeloParaEditar == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Excluir plano");
        confirm.setHeaderText("Confirma a exclusão?");
        confirm.setContentText("O plano \"" + modeloParaEditar.getNome() + "\" será desativado.");

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                assinaturaService.excluirModelo(modeloParaEditar.getIdModelo());
                mostrarAlerta("Excluído", "Plano excluído com sucesso!", Alert.AlertType.INFORMATION);
                voltarAdmin();
            }
        });
    }

    @FXML
    private void voltarAdmin() {
        Stage stage = (Stage) txtNome.getScene().getWindow();
        AppSplashController.trocarCenaComController(
            stage,
            "/view/AdminAssinaturaView.fxml",
            "Planos de Assinatura",
            (AdminAssinaturaController controller) -> controller.initialize()
        );
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
    
    private String getDescricaoPadrao() {
        return "* Receba sempre produtos fresquinhos colhidos especialmente para você!\n" +
               "* Inclui diversas variedades de hortifruti selecionados!\n" +
               "* Surpresas sazonais e orgânicas a cada entrega!\n" +
               "* Mais economia e praticidade para o seu dia a dia!\n" +
               "* Escolha o plano que combina com você!";
    }

    private String gerarDescricaoPadrao(String nomePlano, double valor, String frequenciaTexto) {
        String frequenciaFrase = switch (frequenciaTexto) {
            case "Semanal"   -> "toda semana";
            case "Quinzenal" -> "a cada 15 dias";
            case "Mensal"    -> "todo mês";
            default -> "regularmente";
        };

        return "* Receba " + frequenciaFrase + " uma caixa de produtos fresquinhos colhidos especialmente para você!\n" +
               "* Variedade de frutas, verduras e legumes selecionados!\n" +
               "* Produtos orgânicos e surpresas especiais " + frequenciaFrase + "!\n" +
               "* Economize até R$" + String.format("%.2f", valor * 0.5) +
               " em relação à compra avulsa!\n" +
               "* Tudo isso por apenas R$" + String.format("%.2f", valor) +
               " " + frequenciaTexto.toLowerCase() + "!";
    }

    private String frequenciaNumeroParaTexto(int frequencia) {
        return switch (frequencia) {
            case 1 -> "Semanal";
            case 2 -> "Quinzenal";
            case 4 -> "Mensal";
            default -> "Semanal";
        };
    }

    private int frequenciaTextoParaNumero(String frequenciaTexto) {
        return switch (frequenciaTexto) {
            case "Semanal" -> 1;
            case "Quinzenal" -> 2;
            case "Mensal" -> 4;
            default -> 1;
        };
    }
}
