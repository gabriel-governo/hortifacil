package com.hortifacil.controller;

import com.hortifacil.model.Pedido;
import com.hortifacil.service.PedidoService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class AdminPedidoListarController {

    @FXML
    private VBox pedidosContainer;

    @FXML
    private Button btnVoltarHome;

    private PedidoService pedidoService;

    @FXML
    public void initialize() {
        pedidoService = PedidoService.getInstance();
        carregarPedidos();
    }

    public void carregarPedidos() {
        pedidosContainer.getChildren().clear();

        List<Pedido> pedidos = pedidoService.listarTodosPedidos();
        if (pedidos.isEmpty()) {
            Label lblVazio = new Label("Nenhum pedido encontrado.");
            lblVazio.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
            pedidosContainer.getChildren().add(lblVazio);
            return;
        }

        pedidos.sort(Comparator.comparing(Pedido::getDataPedido).reversed());

        for (Pedido pedido : pedidos) {
            double total = pedidoService.calcularTotalDoPedido(pedido.getIdPedido());
            pedido.setTotal(total);
            VBox card = criarCardPedido(pedido);
            pedidosContainer.getChildren().add(card);
        }
    }

    private VBox criarCardPedido(Pedido pedido) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(12));
        card.setStyle(
                "-fx-background-color: white; " +
                "-fx-padding: 8; " +
                "-fx-border-color: #ccc; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8;"
        );

        Label lblNumero = new Label("Pedido #" + pedido.getIdPedido());
        lblNumero.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label lblData = new Label("Data: " + pedido.getDataPedido());
        Label lblTotal = new Label(String.format("Total: R$ %.2f", pedido.getTotal()));

        Label lblStatus = new Label("Status: " + pedido.getStatus());
        switch (pedido.getStatus().toUpperCase()) {
            case "CANCELADO" -> lblStatus.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            case "FINALIZADO" -> lblStatus.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            default -> lblStatus.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
        }

        Button btnDetalhes = new Button("Detalhes");
        btnDetalhes.getStyleClass().add("button-green");
        btnDetalhes.setOnAction(e -> mostrarDetalhesPedido(pedido));

        Button btnFinalizar = new Button("Finalizar");
        btnFinalizar.getStyleClass().add("button-primary");
        btnFinalizar.setDisable(pedido.getStatus().equalsIgnoreCase("FINALIZADO"));
        btnFinalizar.setOnAction(e -> finalizarPedido(pedido));

        HBox botoes = new HBox(10, btnDetalhes, btnFinalizar);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(lblNumero, lblData, lblTotal, lblStatus, botoes);
        return card;
    }

    private void mostrarDetalhesPedido(Pedido pedido) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminPedidoDetalheView.fxml"));
            Parent root = loader.load();

            AdminPedidoDetalheController controller = loader.getController();
            controller.setPedido(pedido);

            Stage stage = (Stage) pedidosContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Detalhes do Pedido #" + pedido.getIdPedido());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void finalizarPedido(Pedido pedido) {
        pedido.setStatus("FINALIZADO");
        boolean sucesso = pedidoService.atualizarStatusPedido(pedido);
        if (sucesso) {
            carregarPedidos();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Não foi possível finalizar o pedido.");
            alert.showAndWait();
        }
    }

    @FXML
    private void btnVoltarHome() {
        Stage stage = (Stage) btnVoltarHome.getScene().getWindow();
        AppSplashController.trocarCena(stage, "/view/AdminHomeView.fxml", "Home Admin");
    }
}
