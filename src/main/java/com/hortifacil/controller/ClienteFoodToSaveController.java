package com.hortifacil.controller;

import com.hortifacil.dao.CarrinhoProdutoDAOImpl;
import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.dao.ProdutoDAOImpl;
import com.hortifacil.dao.ProdutoEstoqueDAO;
import com.hortifacil.dao.ProdutoEstoqueDAOImpl;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.ProdutoEstoque;
import com.hortifacil.model.ProdutoEstoqueResumo;
import com.hortifacil.service.EstoqueService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ClienteFoodToSaveController {

    @FXML
    private FlowPane produtosContainer;

    private EstoqueService estoqueService;


    private String cpf;
    private String nomeUsuario;
    private int clienteId;
    private String endereco;
    private String login;
    private int usuarioId;

    @FXML
    private void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            ProdutoEstoqueDAO produtoEstoqueDAO = new ProdutoEstoqueDAOImpl(conn, null);
            ProdutoDAO produtoDAO = new ProdutoDAOImpl(conn);

            estoqueService = EstoqueService.getInstance(produtoDAO, produtoEstoqueDAO);

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erro ao conectar ao banco: " + e.getMessage());
        }
    }

   public void setDadosUsuario(String cpf, String nome, String login, int clienteId, String endereco) {
    this.cpf = cpf;
    this.nomeUsuario = nome;
    this.login = login;
    this.clienteId = clienteId;
    this.endereco = endereco;
        carregarProdutosFoodToSave();
    }

    private void carregarProdutosFoodToSave() {
    produtosContainer.getChildren().clear();

    try {
        List<ProdutoEstoqueResumo> produtos = estoqueService.listarProdutosFoodToSave();

        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto próximo do vencimento.");
            return;
        }

        for (ProdutoEstoqueResumo resumo : produtos) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ClienteFoodToSaveCardView.fxml"));
            VBox card = loader.load();

            ClienteFoodToSaveCardController controller = loader.getController();

            controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco);

            ProdutoEstoque pe = new ProdutoEstoque(
            resumo.getProduto(),
            resumo.getQuantidadeTotal(),
            resumo.getDataColhido(),
            resumo.getDataValidade(),
            (int) resumo.getLote()  
        );


            controller.setProdutoEstoque(pe);

            controller.setListener((produtoEstoque, quantidade) ->
                adicionarAoCarrinho(produtoEstoque, quantidade, clienteId)
            );

            produtosContainer.getChildren().add(card);

            // Log detalhado para debugging
            System.out.println("[FoodToSave] Produto: '" + resumo.getProduto().getNome() + "' | "
                + "Colhido=" + resumo.getDataColhido()
                + " | Val=" + resumo.getDataValidade()
                + " | Lote=" + resumo.getLote()
                + " | Qtd=" + resumo.getQuantidadeTotal()
                + " | DescontoAplicado=" + pe.getDesconto()
                + " | PrecoComDesconto=" + pe.getPrecoComDesconto()
            );
        }

    } catch (SQLException | IOException e) {
        e.printStackTrace();
        System.err.println("Erro ao carregar produtos Food to Save: " + e.getMessage());
    }
}

  private void adicionarAoCarrinho(ProdutoEstoque produtoEstoque, int quantidade, int clienteId) {
    try (Connection conn = DatabaseConnection.getConnection()) {
        CarrinhoProdutoDAOImpl carrinhoDAO = new CarrinhoProdutoDAOImpl(conn);
        int idCarrinho = carrinhoDAO.obterOuCriarCarrinhoAberto(clienteId);

        double precoUnitario = produtoEstoque.getPrecoComDesconto();

        CarrinhoProduto item = new CarrinhoProduto(
            idCarrinho,
            clienteId,
            produtoEstoque.getProduto(),
            quantidade,
            precoUnitario
        );

        // Adiciona o item ao carrinho
        carrinhoDAO.adicionarAoCarrinho(item);
        System.out.println("✅ Produto adicionado ao carrinho com desconto aplicado.");

        // Agora dá baixa no estoque (usando EstoqueService Singleton)
        try {
            ProdutoDAO produtoDAO = new ProdutoDAOImpl(conn);
            ProdutoEstoqueDAO produtoEstoqueDAO = new ProdutoEstoqueDAOImpl(conn, null);
            EstoqueService estoqueService = EstoqueService.getInstance(produtoDAO, produtoEstoqueDAO);

            estoqueService.retirarDoEstoque(produtoEstoque.getProduto().getId(), quantidade);
            System.out.println("📦 Estoque atualizado (baixa realizada com sucesso).");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("⚠ Erro ao atualizar estoque no FoodToSave: " + e.getMessage());
        }

    } catch (SQLException e) {
        e.printStackTrace();
        System.err.println("❌ Erro ao adicionar ao carrinho: " + e.getMessage());
    }
}

    @FXML
    private void handleVerCarrinho() {
        Stage stage = (Stage) produtosContainer.getScene().getWindow();
        AppSplashController.<ClienteCarrinhoController>trocarCenaComDados(
                stage,
                "/view/ClienteCarrinhoView.fxml",
                "Carrinho",
                controller -> controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco)

        );
    }

@FXML
private void handleVoltar() {
    Stage stage = (Stage) produtosContainer.getScene().getWindow();
    AppSplashController.<ClienteHomeController>trocarCenaComDados(
        stage,
        "/view/ClienteHomeView.fxml",
        "Home Cliente",
        controller -> controller.setDadosUsuario(
            this.usuarioId,   // int
            this.cpf,         // String
            this.nomeUsuario, // String
            this.login,       // String
            this.clienteId,   // int
            this.endereco     // String
        )
    );
}

@FXML
private void handleVerProdutos() {
    Stage stage = (Stage) produtosContainer.getScene().getWindow();
    AppSplashController.<ClienteProdutoListarController>trocarCenaComDados(  
            stage,
            "/view/ClienteProdutoListarView.fxml",
            "Ver Produtos",
            controller -> controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco)
    );
}

}