package com.hortifacil.controller;

import com.hortifacil.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class AdminPedidoAssinaturaDetalheController {

    @FXML private Label lblTituloPedido;
    @FXML private Label lblNomeCliente;
    @FXML private Label lblEnderecoCliente;
    @FXML private Label lblStatus;
    @FXML private Label lblEntregaAtual;
    @FXML private VBox produtosContainer;
    @FXML private Button btnVoltar;
    @FXML private Button btnAtualizarStatus;

    private int idPedidoAssinatura;
    private int entregaAtual;

    @FXML
    public void initialize() {
        // Inicializações se necessárias
    }

    public void setPedidoAssinatura(int id) {
        this.idPedidoAssinatura = id;
        lblTituloPedido.setText("Assinatura #" + id);
        carregarDados();
    }

    private void carregarDados() {
    produtosContainer.getChildren().clear();

    int idAssinatura = 0; // ✅ declarar aqui
    String plano = "Mensal";
    LocalDate dataInicio = LocalDate.now();

    try (Connection conn = DatabaseConnection.getConnection()) {

        System.out.println("idPedidoAssinatura: " + idPedidoAssinatura);

        // 1️⃣ Buscar dados do pedido, assinatura e cliente
        String sqlInfo = """
            SELECT a.id AS id_assinatura, c.nome AS nome_cliente,
                   CONCAT(e.rua, ', ', e.numero, ' - ', e.bairro) AS endereco,
                   pa.status, a.plano, a.data_inicio
            FROM pedido_assinatura pa
            JOIN assinatura a ON pa.id_assinatura = a.id
            JOIN cliente c ON a.id_cliente = c.id_cliente
            LEFT JOIN endereco e ON e.id_cliente = c.id_cliente
            WHERE pa.id = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sqlInfo)) {
            stmt.setInt(1, idPedidoAssinatura);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                idAssinatura = rs.getInt("id_assinatura");
                lblNomeCliente.setText("Cliente: " + rs.getString("nome_cliente"));
                lblEnderecoCliente.setText("Endereço: " + rs.getString("endereco"));
                lblStatus.setText("Status: " + rs.getString("status"));
                plano = rs.getString("plano");
                dataInicio = rs.getDate("data_inicio").toLocalDate();

                entregaAtual = calcularEntregaAtual(dataInicio, plano);
            }
        }

        System.out.println("idAssinatura: " + idAssinatura);

        // 2️⃣ Buscar produtos da assinatura usando id_assinatura
        String sqlProdutos = """
            SELECT p.nome, SUM(ap.quantidade) AS quantidade, ap.unidade
            FROM assinatura_produto ap
            JOIN produto p ON ap.id_produto = p.id_produto
            WHERE ap.id_assinatura = ?
            GROUP BY p.nome, ap.unidade;
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sqlProdutos)) {
            stmt.setInt(1, idAssinatura);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                VBox card = new VBox(8);
                card.setStyle("-fx-background-color: white; " +
                              "-fx-padding: 10; " +
                              "-fx-border-color: #ccc; " +
                              "-fx-border-radius: 8; " +
                              "-fx-background-radius: 8; " +
                              "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3,0,0,1);");

                Label lblNome = new Label(rs.getString("nome"));
                lblNome.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                Label lblQtd = new Label("Quantidade: " + rs.getInt("quantidade") +
                (rs.getString("unidade") != null ? " " + rs.getString("unidade") : ""));

                card.getChildren().addAll(lblNome, lblQtd);
                produtosContainer.getChildren().add(card);
            }
        }

        // 3️⃣ Calcular total de entregas baseado no plano
        int totalEntregas = switch (plano.toLowerCase()) {
            case "semanal" -> 4;
            case "quinzenal" -> 2;
            case "mensal" -> 1;
            default -> 1;
        };
        lblEntregaAtual.setText("Entrega: " + entregaAtual + " / " + totalEntregas);

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    private int calcularEntregaAtual(LocalDate dataInicio, String plano) {
        LocalDate hoje = LocalDate.now();
        if (dataInicio.isAfter(hoje)) return 1; // assinatura futura
        long dias = ChronoUnit.DAYS.between(dataInicio, hoje);

        int entrega;
        switch (plano.toLowerCase()) {
            case "semanal" -> entrega = (int) (dias / 7) + 1;
            case "quinzenal" -> entrega = (int) (dias / 15) + 1;
            case "mensal" -> entrega = 1;
            default -> entrega = 1;
        }

        int limite = switch (plano.toLowerCase()) {
            case "semanal" -> 4;
            case "quinzenal" -> 2;
            case "mensal" -> 1;
            default -> 1;
        };

        return Math.min(entrega, limite);
    }

    @FXML
    private void handleAtualizarStatus() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE pedido_assinatura SET status = 'ENTREGUE' WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idPedidoAssinatura);
                stmt.executeUpdate();
                lblStatus.setText("Status: ENTREGUE");
                btnAtualizarStatus.setDisable(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVoltar() {
        Stage stage = (Stage) btnVoltar.getScene().getWindow();
        AppSplashController.trocarCena(stage, "/view/AdminPedidoAssinaturaListarView.fxml", "Pedidos de Assinatura");
    }
}
