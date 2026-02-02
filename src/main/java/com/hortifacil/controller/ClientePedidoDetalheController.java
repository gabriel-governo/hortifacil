package com.hortifacil.controller;

import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.ItemPedido;
import com.hortifacil.model.Pedido;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ClientePedidoDetalheController {

    @FXML private Label lblTituloPedido; // <--- novo
    @FXML private Label lblNomeCliente;
    @FXML private Label lblEnderecoCliente;
    @FXML private Label lblTotal;
    @FXML private Label lblPagamento;
    @FXML private Button btnVoltar;
    @FXML private Label lblTroco;
    @FXML private VBox produtosContainer;

    private String cpf;
    private String nomeUsuario;
    private int clienteId;
    private String login;
    private String endereco;

    private final ObservableList<ItemPedido> listaItens = FXCollections.observableArrayList();
    private int idPedido;

    public void setPedido(Pedido pedido) {
        this.idPedido = pedido.getIdPedido();

        // Atualiza o título do pedido
        lblTituloPedido.setText("Pedido #" + pedido.getIdPedido());

        // Carrega os dados do pedido (cliente, itens, total, pagamento)
        carregarDados();
    }

    public void setDadosUsuario(String cpf, String nomeUsuario, String login, int clienteId, String endereco) {
        this.cpf = cpf;
        this.nomeUsuario = nomeUsuario;
        this.login = login;
        this.clienteId = clienteId;
        this.endereco = endereco;
    }

    @FXML
    public void initialize() {
        // initialize vazio
    }

    private void carregarDados() {
        double total = 0;
        try (Connection conn = DatabaseConnection.getConnection()) {

            // Informações do cliente
            String sqlInfo = """
                SELECT c.nome AS nome_cliente, 
                       CONCAT(e.rua, ', ', e.numero, ' - ', e.bairro) AS endereco
                FROM pedido p
                JOIN cliente c ON p.id_cliente = c.id_cliente
                LEFT JOIN endereco e ON e.id_cliente = c.id_cliente
                WHERE p.id_pedido = ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sqlInfo)) {
                stmt.setInt(1, idPedido);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    lblNomeCliente.setText("Cliente: " + rs.getString("nome_cliente"));
                    lblEnderecoCliente.setText("Endereço: " + rs.getString("endereco"));
                }
            }

            // Itens do pedido
            String sqlItens = """
                SELECT pr.nome, pp.quantidade, pp.preco_unitario
                FROM pedido_produto pp
                JOIN produto pr ON pr.id_produto = pp.id_produto
                WHERE pp.id_pedido = ?
            """;

            listaItens.clear();
            try (PreparedStatement stmt = conn.prepareStatement(sqlItens)) {
                stmt.setInt(1, idPedido);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String nome = rs.getString("nome");
                    int qtd = rs.getInt("quantidade");
                    double preco = rs.getDouble("preco_unitario");
                    listaItens.add(new ItemPedido(nome, qtd, preco));
                    total += preco * qtd;
                }
            }

            lblTotal.setText(String.format("R$ %.2f", total));

                        // Método de pagamento
                        String sqlPagamento = "SELECT metodo_pagamento FROM pedido WHERE id_pedido = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sqlPagamento)) {
                stmt.setInt(1, idPedido);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    lblPagamento.setText(rs.getString("metodo_pagamento"));
                    lblTroco.setVisible(false); // deixa invisível se não houver troco
                }

            }

            // Carregar itens como cards
            carregarItensComoCards();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void carregarItensComoCards() {
        produtosContainer.getChildren().clear();
        for (ItemPedido item : listaItens) {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color:white; " +
                        "-fx-padding: 10; " +
                        "-fx-border-color: #ccc; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3,0,0,1);"); // sombra leve

            Label lblNome = new Label(item.getNomeProduto());
            lblNome.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Label lblQtd = new Label("Quantidade: " + item.getQuantidade());
            Label lblPreco = new Label(String.format("Preço unitário: R$ %.2f", item.getPrecoUnitario()));
            Label lblSubtotal = new Label(String.format("Subtotal: R$ %.2f", item.getQuantidade() * item.getPrecoUnitario()));

            card.getChildren().addAll(lblNome, lblQtd, lblPreco, lblSubtotal);
            produtosContainer.getChildren().add(card);
        }
    }

    @FXML
    private void voltarParaMeusPedidos() {
        Stage stage = (Stage) btnVoltar.getScene().getWindow();
        AppSplashController.<ClientePedidoListarController>trocarCenaComController(
                stage,
                "/view/ClientePedidoListarView.fxml",
                "Meus Pedidos",
                (ClientePedidoListarController controller) -> {
                    controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco);
                }
        );
    }
}
