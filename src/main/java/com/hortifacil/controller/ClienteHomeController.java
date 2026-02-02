package com.hortifacil.controller;

import java.sql.Connection;

import com.hortifacil.dao.AssinaturaDAO;
import com.hortifacil.dao.AssinaturaDAOImpl;
import com.hortifacil.model.Assinatura;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ClienteHomeController {

    @FXML
    private ImageView settingsIcon;

    @FXML
    private Label welcomeLabel;

    // Cards
    @FXML private VBox cardVerProdutos;
    @FXML private VBox cardAssinaturas;
    @FXML private VBox cardMeusPedidos;
    @FXML private VBox cardFoodToSave;

    // Dados do usuário
    private int usuarioId;
    private String login;
    private String cpf;
    private String nomeUsuario;
    private int clienteId;
    private String endereco;
    
    private Connection connection; // conexão com o banco

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setDadosUsuario(int usuarioId, String cpf, String nome, String login, int clienteId, String endereco) {
        this.usuarioId = usuarioId;
        this.cpf = cpf;
        this.nomeUsuario = nome;
        this.login = login;
        this.clienteId = clienteId;
        this.endereco = endereco;

        if (welcomeLabel != null && nomeUsuario != null) {
            welcomeLabel.setText("Bem-vindo, " + nomeUsuario + "!");
        }
    }

    @FXML
    private void abrirSettings() {
        Stage stage = (Stage) settingsIcon.getScene().getWindow();
        AppSplashController.trocarCenaComController(
            stage,
            "/view/ClienteSettingsView.fxml",
            "Configurações",
            (ClienteSettingsController controller) -> {
                controller.setDadosUsuario(usuarioId, cpf, nomeUsuario, login, clienteId, endereco);
            }
        );
    }

    // Método genérico para animar clique do card
    private void animarClique(VBox card, Runnable acao) {
        ScaleTransition st = new ScaleTransition(Duration.millis(100), card);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(0.95);
        st.setToY(0.95);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.setOnFinished(e -> acao.run());
        st.play();
    }

    // Cards clicáveis
    @FXML
    private void handleVerProdutosClick(MouseEvent event) {
        animarClique(cardVerProdutos, this::handleVerProdutos);
    }

    @FXML
    private void handleAssinaturasClick(MouseEvent event) {
        animarClique(cardAssinaturas, this::handleAssinaturas);
    }

    @FXML
    private void handleMeusPedidosClick(MouseEvent event) {
        animarClique(cardMeusPedidos, this::handleVerPedidos);
    }

    @FXML
    private void handleFoodToSaveClick(MouseEvent event) {
        animarClique(cardFoodToSave, this::handleFoodToSave);
    }

    // Ações reais
    private void handleVerProdutos() {
        if (cpf == null || nomeUsuario == null) {
            System.err.println("Dados do usuário não foram inicializados corretamente.");
            return;
        }

        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        AppSplashController.<ClienteProdutoListarController>trocarCenaComDados(
            stage,
            "/view/ClienteProdutoListarView.fxml",
            "Produtos Disponíveis",
            controller -> controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco)
        );
    }
    
private void handleAssinaturas() {
    AssinaturaDAO assinaturaDAO = new AssinaturaDAOImpl();
    Assinatura assinatura = assinaturaDAO.buscarAtivaPorCliente(clienteId);

    Stage stage = (Stage) cardAssinaturas.getScene().getWindow();

    if (assinatura == null) {
        // Não tem assinatura → abre a tela de nova assinatura
        AppSplashController.trocarCenaComController(
            stage,
            "/view/ClienteAssinaturaNovaView.fxml",
            "Nova Assinatura",
            (ClienteAssinaturaNovaController controller) -> {
                controller.setClienteIdParaPlanos(clienteId, cpf, nomeUsuario, login, endereco);
            }
        );
    } else {
        // Já tem assinatura → abre a tela de assinatura ativa
       AppSplashController.trocarCenaComController(
            stage,
            "/view/ClienteAssinaturaView.fxml",
            "Minha Assinatura",
            (ClienteAssinaturaController controller) -> {
                controller.setClienteId(clienteId);
                controller.setDadosCliente(clienteId, usuarioId, cpf, nomeUsuario, login, endereco);
            }
        );
    }
}

    private void handleVerPedidos() {
        Stage stage = (Stage) cardMeusPedidos.getScene().getWindow();
        AppSplashController.trocarCenaComController(
            stage,
            "/view/ClientePedidoListarView.fxml",
            "Meus Pedidos",
            (ClientePedidoListarController controller) -> {
                controller.setConnection(connection); // injeta a conexão
                controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco); // define usuário
            }
        );
    }

    private void handleFoodToSave() {
        Stage stage = (Stage) cardFoodToSave.getScene().getWindow();
        AppSplashController.<ClienteFoodToSaveController>trocarCenaComDados(
            stage,
            "/view/ClienteFoodToSaveView.fxml",
            "Save to Food",
            controller -> controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco)
        );
    }

    @FXML
    private void handleSair(MouseEvent event) {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        AppSplashController.trocarCena(stage, "/view/LoginView.fxml", "Login");
    }
}
