package com.hortifacil.controller;

import com.hortifacil.dao.PedidoDAO;
import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.Pedido;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.sql.Connection;
import java.util.List;

public class ClientePedidoListarController {

    @FXML
    private VBox pedidosContainer;

    @FXML
    private Button btnVoltar;

    private PedidoDAO pedidoDAO;

    private int clienteId;
    private String cpf;
    private String nomeUsuario;
    private String endereco;
    private String login;
    private int usuarioId;

   public void setDadosUsuario(int usuarioId, String cpf, String nome, String login, int clienteId, String endereco) {
    this.usuarioId = usuarioId;
    this.cpf = cpf;
    this.nomeUsuario = nome;
    this.login = login;
    this.clienteId = clienteId;
    this.endereco = endereco;

    carregarPedidos();
}

public void setConnection(Connection connection) {
    this.pedidoDAO = new PedidoDAO(connection); // instanciando direto a classe existente
    if (clienteId != 0) {
        carregarPedidos();
    }
}

public void setDadosUsuario(String cpf, String nome, String login, int clienteId, String endereco) {
    this.cpf = cpf;
    this.nomeUsuario = nome;
    this.login = login;
    this.clienteId = clienteId;
    this.endereco = endereco;

    if (pedidoDAO != null) { // Se a conexão já foi injetada
        carregarPedidos();
    }
}

@FXML
private void initialize() {
    this.pedidoDAO = new PedidoDAO(); // cria sempre que abre a tela
}

public void carregarPedidos() {
    if (pedidoDAO == null) {
        mostrarAlerta("Erro", "Conexão com o banco não inicializada.");
        return;
    }

    List<Pedido> pedidos;
    try {
        pedidos = pedidoDAO.listarPedidosPorCliente(clienteId);
    } catch (Exception e) {
        e.printStackTrace();
        mostrarAlerta("Erro", "Não foi possível carregar os pedidos: " + e.getMessage());
        return;
    }

    pedidosContainer.getChildren().clear();

    if (pedidos.isEmpty()) {
        Label lblVazio = new Label("Nenhum pedido encontrado.");
        lblVazio.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
        pedidosContainer.getChildren().add(lblVazio);
        return;
    }

    pedidos.sort((p1, p2) -> p2.getDataPedido().compareTo(p1.getDataPedido()));

    for (Pedido pedido : pedidos) {
        VBox card = criarCardPedido(pedido);
        pedidosContainer.getChildren().add(card);
    }
}

private void mostrarAlerta(String titulo, String mensagem) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(titulo);
    alert.setHeaderText(null);
    alert.setContentText(mensagem);
    alert.showAndWait();
}

    private VBox criarCardPedido(Pedido pedido) {
        VBox card = new VBox(10);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-padding: 10; " +
            "-fx-border-color: #ccc; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8;"
        );
        card.setPadding(new javafx.geometry.Insets(12));

        Label lblNumero = new Label("Pedido #" + pedido.getIdPedido());
        lblNumero.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label lblData = new Label("Data: " + pedido.getDataPedido().toString());

        // Calcula total somando os itens do pedido
        double total = 0;
        List<CarrinhoProduto> itens = pedidoDAO.listarItensPorPedido(pedido.getIdPedido());
        for (CarrinhoProduto item : itens) {
            total += item.getQuantidade() * item.getPrecoUnitario();
        }
        Label lblTotal = new Label(String.format("Total: R$ %.2f", total));

        Label lblStatus = new Label("Status: " + pedido.getStatus());
        switch (pedido.getStatus()) {
            case "CANCELADO":
                lblStatus.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                break;
            case "FINALIZADO":
                lblStatus.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                break;
            default:
                lblStatus.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                break;
        }

        Button btnDetalhes = new Button("Detalhes");
        btnDetalhes.getStyleClass().add("button-green");
        btnDetalhes.setOnAction(event -> mostrarDetalhesPedido(pedido));

        HBox bottom = new HBox(10, btnDetalhes);
        bottom.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(lblNumero, lblData, lblTotal, lblStatus, bottom);
        return card;
    }

private void mostrarDetalhesPedido(Pedido pedido) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ClientePedidoDetalheView.fxml"));
        Parent root = loader.load();

        ClientePedidoDetalheController controller = loader.getController();
        
        controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco);
        controller.setPedido(pedido);

        Stage stage = (Stage) pedidosContainer.getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.setTitle("Pedido #" + pedido.getIdPedido());

    } catch (Exception e) {
        e.printStackTrace();
    }
}

@FXML
private void handleVoltar() {
    Stage stage = (Stage) btnVoltar.getScene().getWindow();
    AppSplashController.<ClienteHomeController>trocarCenaComDados(
        stage,
        "/view/ClienteHomeView.fxml",
        "Home Cliente",
        controller -> controller.setDadosUsuario(
            usuarioId,    // int
            cpf,          // String
            nomeUsuario,  // String
            login,        // String
            clienteId,    // int
            endereco      // String
        )
    );
}

}
