package com.hortifacil.controller;

import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.dao.ProdutoDAOImpl;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.PedidoAssinatura;
import com.hortifacil.service.PedidoAssinaturaService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AdminPedidoAssinaturaListarController {

    @FXML private VBox pedidosContainer; // Container onde os cards serão adicionados
    @FXML private Button btnVoltarHome;

    private PedidoAssinaturaService pedidoService;

    @FXML
    public void initialize() {
        try {
            // Cria a conexão e o DAO
            Connection conn = DatabaseConnection.getConnection();
            ProdutoDAO produtoDAO = new ProdutoDAOImpl(conn);

            // Instancia o service passando o DAO
            pedidoService = PedidoAssinaturaService.getInstance(produtoDAO);

            carregarPedidos();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void carregarPedidos() {
        try {
            pedidosContainer.getChildren().clear();
            List<PedidoAssinatura> pedidos = pedidoService.listarTodosPedidos();

            for (PedidoAssinatura pedido : pedidos) {
                VBox card = criarCardPedido(pedido);
                pedidosContainer.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox criarCardPedido(PedidoAssinatura pedido) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(12));
        card.setStyle(
                "-fx-background-color: white; " +
                "-fx-padding: 8; " +
                "-fx-border-color: #ccc; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8;"
        );

        Label lblNumero = new Label("Pedido Assinatura #" + pedido.getId());
        lblNumero.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label lblData = new Label("Data de Entrega: " + pedido.getDataEntrega());
        Label lblStatus = new Label("Status: " + pedido.getStatus());

        switch (pedido.getStatus().toString().toUpperCase()) {
            case "ENTREGUE" -> lblStatus.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            case "CANCELADO" -> lblStatus.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            default -> lblStatus.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
        }

        Button btnDetalhes = new Button("Detalhes");
        btnDetalhes.getStyleClass().add("button-green");
        btnDetalhes.setOnAction(e -> mostrarDetalhesPedido(pedido));

        Button btnFinalizar = new Button("Finalizar");
        btnFinalizar.getStyleClass().add("button-primary");
        btnFinalizar.setDisable(pedido.getStatus() == PedidoAssinatura.Status.ENTREGUE);
        btnFinalizar.setOnAction(e -> {
            pedido.setStatus(PedidoAssinatura.Status.ENTREGUE);
            try {
                boolean sucesso = pedidoService.atualizarStatusPedido(pedido);
                if (sucesso) {
                    lblStatus.setText("Status: " + pedido.getStatus());
                    lblStatus.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    btnFinalizar.setDisable(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        HBox botoes = new HBox(10, btnDetalhes, btnFinalizar);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(lblNumero, lblData, lblStatus, botoes);
        return card;
    }

    private void mostrarDetalhesPedido(PedidoAssinatura pedido) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminPedidoAssinaturaDetalheView.fxml"));
            Parent root = loader.load();

            AdminPedidoAssinaturaDetalheController controller = loader.getController();
            controller.setPedidoAssinatura(pedido.getId()); // <--- usar o método correto

            Stage stage = (Stage) pedidosContainer.getScene().getWindow();
            stage.setTitle("Detalhes do Pedido de Assinatura #" + pedido.getId());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnVoltarHome() {
        Stage stage = (Stage) btnVoltarHome.getScene().getWindow();
        AppSplashController.trocarCena(stage, "/view/AdminHomeAssinaturaView.fxml", "Home Assinaturas");
    }
}
