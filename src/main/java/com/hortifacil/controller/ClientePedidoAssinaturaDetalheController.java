package com.hortifacil.controller;

import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.PedidoAssinatura;
import com.hortifacil.model.PedidoAssinaturaItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;

public class ClientePedidoAssinaturaDetalheController {

    @FXML private Label lblTituloPedido;
    @FXML private Label lblDataPedido;
    @FXML private Label lblStatus;
    @FXML private Label lblValorTotal;
    @FXML private Button btnVoltar;
    @FXML private VBox produtosContainer;

    private String cpf;
    private String nomeUsuario;
    private String login;
    private int clienteId;
    private String endereco;
    private PedidoAssinatura pedido;
    private final ObservableList<PedidoAssinaturaItem> listaItens = FXCollections.observableArrayList();


    public void setPedidoAssinatura(PedidoAssinatura pedido) {
        this.pedido = pedido;
        carregarDados();
    }

    public void setDadosUsuario(String cpf, String nomeUsuario, String login, int clienteId, String endereco, String usuarioId) {
        this.cpf = cpf;
        this.nomeUsuario = nomeUsuario;
        this.login = login;
        this.clienteId = clienteId;
        this.endereco = endereco;
    }

    private void carregarDados() {
        if (pedido == null) return;

        lblTituloPedido.setText("Pedido #" + pedido.getId());
        lblStatus.setText("Status: " + pedido.getStatus().toString());
        lblDataPedido.setText("Data: " + 
    (pedido.getDataEntrega() != null ? pedido.getDataEntrega().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-")
);

        carregarItens();
    }

    private void carregarItens() {
    produtosContainer.getChildren().clear();
    listaItens.clear();

    try (Connection conn = DatabaseConnection.getConnection()) {
        String sql = """
            SELECT 
                ap.id,
                ap.id_assinatura,
                p.id_produto,
                p.nome AS nome_produto,
                ap.quantidade,
                um.nome AS unidade
            FROM assinatura_produto ap
            JOIN produto p ON ap.id_produto = p.id_produto
            LEFT JOIN unidade_medida um ON p.id_unidade = um.id_unidade
            WHERE ap.id_assinatura = ?;
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedido.getIdAssinatura());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PedidoAssinaturaItem item = new PedidoAssinaturaItem();
                item.setIdPedido(pedido.getId());
                item.setIdProduto(rs.getInt("id_produto"));
                item.setUnidade(rs.getString("unidade"));
                item.setQuantidade(rs.getInt("quantidade"));
                listaItens.add(item);

                VBox card = new VBox(8);
                card.setStyle("""
                    -fx-background-color: white;
                    -fx-padding: 10;
                    -fx-border-color: #ccc;
                    -fx-border-radius: 8;
                    -fx-background-radius: 8;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3,0,0,1);
                """);

                Label lblNome = new Label("Produto: " + rs.getString("nome_produto"));
                lblNome.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                String unidade = rs.getString("unidade");
                if (unidade == null) unidade = "-";

                Label lblUnidade = new Label("Unidade: " + unidade);
                Label lblQuantidade = new Label("Quantidade: " + rs.getInt("quantidade"));

                card.getChildren().addAll(lblNome, lblUnidade, lblQuantidade);
                produtosContainer.getChildren().add(card);
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

@FXML
private void voltarParaAssinatura() {
    Stage stage = (Stage) btnVoltar.getScene().getWindow();
    AppSplashController.<ClienteAssinaturaController>trocarCenaComController(
        stage,
        "/view/ClienteAssinaturaView.fxml",
        "Minha Assinatura",
        (ClienteAssinaturaController controller) -> {
            controller.setDadosCliente(clienteId, clienteId, cpf, nomeUsuario, login, endereco);
        }
    );
}
}
