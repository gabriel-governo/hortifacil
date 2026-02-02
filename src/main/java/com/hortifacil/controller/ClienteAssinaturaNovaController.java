package com.hortifacil.controller;

import java.util.Collections;
import java.util.List;

import com.hortifacil.model.AssinaturaModelo;
import com.hortifacil.dao.AssinaturaModeloDAO;
import com.hortifacil.dao.AssinaturaModeloDAOImpl;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ClienteAssinaturaNovaController {

    @FXML
    private FlowPane containerAssinaturas;

    private final AssinaturaModeloDAO modeloDAO = new AssinaturaModeloDAOImpl();

    private int clienteId;
    private int usuarioId;
    private String cpf;
    private String nomeUsuario;
    private String login;
    private String endereco;

    private AssinaturaModelo assinaturaAtual;

    // Método chamado ao entrar na tela, passa dados do usuário
    public void setClienteIdParaPlanos(int clienteId, String cpf, String nomeUsuario, String login, String endereco) {
        this.clienteId = clienteId;
        this.cpf = cpf;
        this.nomeUsuario = nomeUsuario;
        this.login = login;
        this.endereco = endereco;
        carregarAssinaturasDisponiveis();
    }

    // Define a assinatura atual para destacar na tela
    public void setAssinaturaAtual(AssinaturaModelo assinatura) {
        this.assinaturaAtual = assinatura;
        carregarAssinaturasDisponiveis();
        destacarPlanoAtual();
    }

    @FXML
    public void initialize() {
        // Nenhuma inicialização específica necessária aqui
    }

    // Destaca a assinatura que o usuário já possui
    private void destacarPlanoAtual() {
        if (assinaturaAtual == null || containerAssinaturas.getChildren().isEmpty()) return;

        for (var node : containerAssinaturas.getChildren()) {
            if (node instanceof VBox card) {
                Label nomeLabel = (Label) card.lookup("#nomeLabel");
                if (nomeLabel != null &&
                    nomeLabel.getText().equalsIgnoreCase(assinaturaAtual.getNome())) {
                    card.setStyle(card.getStyle() + "-fx-border-color: #4caf50; -fx-border-width: 2;");
                }
            }
        }
    }

    // Carrega todos os planos disponíveis
    private void carregarAssinaturasDisponiveis() {
        containerAssinaturas.getChildren().clear();

        List<AssinaturaModelo> planos = modeloDAO.listarTodos();
        if (planos == null) planos = Collections.emptyList();

        if (planos.isEmpty()) {
            Label vazio = new Label("Nenhuma assinatura disponível no momento.");
            containerAssinaturas.getChildren().add(vazio);
            return;
        }

        for (AssinaturaModelo modelo : planos) {
            VBox card = criarCardPlano(modelo);
            containerAssinaturas.getChildren().add(card);
        }
    }

    // Cria o card visual de cada assinatura
    private VBox criarCardPlano(AssinaturaModelo modelo) {
        Label nome = new Label(modelo.getNome());
        nome.setId("nomeLabel");
        nome.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label descricao = new Label(modelo.getDescricao());
        descricao.setWrapText(true);
        descricao.setMaxWidth(260);
        descricao.setStyle("-fx-font-size: 14px;");

        Label valor = new Label("R$ " + String.format("%.2f", modelo.getValor()));
        valor.setStyle("-fx-font-weight: bold; -fx-text-fill: green; -fx-font-size: 16px;");

        Label quantidade = new Label("Produtos: " + modelo.getQuantidadeProdutos());
        Label frequencia = new Label("Frequência: " + frequenciaNumeroParaTexto(modelo.getFrequencia()));

        VBox card = new VBox(8, nome, descricao, valor, quantidade, frequencia);
        card.setStyle(
            "-fx-border-color: #ccc; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12; " +
            "-fx-padding: 20; " +
            "-fx-background-color: #fff;"
        );

        card.setPrefWidth(280);
        card.setPrefHeight(420);

        ScaleTransition st = new ScaleTransition(Duration.millis(100), card);
        card.setOnMousePressed(e -> {
            st.setFromX(1.0);
            st.setFromY(1.0);
            st.setToX(0.95);
            st.setToY(0.95);
            st.setCycleCount(1);
            st.playFromStart();
        });
        card.setOnMouseReleased(e -> {
            st.setFromX(0.95);
            st.setFromY(0.95);
            st.setToX(1.0);
            st.setToY(1.0);
            st.setCycleCount(1);
            st.playFromStart();
            irParaPagamentoAssinatura(modelo);
        });

        return card;
    }

    // Redireciona direto para pagamento da assinatura
    private void irParaPagamentoAssinatura(AssinaturaModelo modelo) {
        if (modelo == null) {
            new Alert(Alert.AlertType.WARNING, "Nenhum plano selecionado!").showAndWait();
            return;
        }

        try {
            Stage stage = (Stage) containerAssinaturas.getScene().getWindow();
            AppSplashController.trocarCenaComController(
                stage,
                "/view/ClientePagamentoView.fxml",
                "Pagamento",
                (ClientePagamentoController controller) -> {
                    controller.setAssinatura(modelo);
                    controller.setDadosUsuarioParaAssinatura(clienteId, cpf, nomeUsuario, login, endereco);
                    controller.setOrigemTela("assinatura");
                }
            );

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Erro ao redirecionar para pagamento: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void voltarAssinatura() {
        Stage stage = (Stage) containerAssinaturas.getScene().getWindow();
        AppSplashController.trocarCenaComController(
            stage,
            "/view/ClienteHomeView.fxml",
            "Home",
            (ClienteHomeController controller) -> {
                controller.setDadosUsuario(usuarioId, cpf, nomeUsuario, login, clienteId, endereco);
            }
        );
    }

    // Converte a frequência numérica para texto
    private String frequenciaNumeroParaTexto(int frequencia) {
        return switch (frequencia) {
            case 1 -> "Semanal";
            case 2 -> "Quinzenal";
            case 4 -> "Mensal";
            default -> frequencia + " dias";
        };
    }
}

