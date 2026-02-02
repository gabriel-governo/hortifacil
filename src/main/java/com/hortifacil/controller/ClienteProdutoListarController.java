package com.hortifacil.controller;

import com.hortifacil.dao.CarrinhoProdutoDAOImpl;
import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.dao.ProdutoDAOImpl;
import com.hortifacil.dao.ProdutoEstoqueDAO;
import com.hortifacil.dao.ProdutoEstoqueDAOImpl;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.Produto;
import com.hortifacil.model.ProdutoEstoque;
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

public class ClienteProdutoListarController {

    @FXML private FlowPane produtosContainer;

    private String cpf;
    private String nomeUsuario;
    private int clienteId;
    private String endereco;
    private String login;
    private int usuarioId;

    private ProdutoDAO produtoDAO;
    private EstoqueService estoqueService;

    @FXML
    private void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ProdutoEstoqueDAO produtoEstoqueDAO = new ProdutoEstoqueDAOImpl(conn, null);
            this.produtoDAO = new ProdutoDAOImpl(conn);
            this.estoqueService = EstoqueService.getInstance(produtoDAO, produtoEstoqueDAO);
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
        carregarProdutosDoEstoque();
    }

private void carregarProdutosDoEstoque() {
    produtosContainer.getChildren().clear();

    try {
        // Filtra apenas produtos com estoque
        List<Produto> produtos = produtoDAO.listarTodos().stream()
                .filter(p -> {
                    try {
                        return estoqueService.buscarQuantidade(p.getId()) > 0;
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .toList();

        for (Produto produto : produtos) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ClienteProdutoCardView.fxml"));
            VBox card = loader.load();

            ClienteProdutoCardController controller = loader.getController();
            controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco);

            int quantidadeDisponivel = estoqueService.buscarQuantidade(produto.getId());
            controller.setProdutoEstoque(new ProdutoEstoque(produto, quantidadeDisponivel));

            // Listener ajustado com exibição de mensagem
            controller.setListener((produtoEstoque, quantidade) -> {
                boolean sucesso = adicionarAoCarrinho(produtoEstoque.getProduto(), quantidade, clienteId);

                if (sucesso) {
                    controller.exibirMensagem("✅ Produto adicionado ao carrinho!", "limegreen");
                } else {
                    controller.exibirMensagem("❌ Erro ao adicionar ao carrinho.", "crimson");
                }
            });

            produtosContainer.getChildren().add(card);
        }

    } catch (IOException | SQLException e) {
        e.printStackTrace();
        System.err.println("Erro ao carregar produtos do estoque: " + e.getMessage());
    }
}

   private boolean adicionarAoCarrinho(Produto produto, int quantidade, int clienteId) {
    try (Connection conn = DatabaseConnection.getConnection()) {
        var carrinhoProdutoDAO = new CarrinhoProdutoDAOImpl(conn);
        int idCarrinho = carrinhoProdutoDAO.obterOuCriarCarrinhoAberto(clienteId);

        CarrinhoProduto item = new CarrinhoProduto(idCarrinho, clienteId, produto, quantidade, produto.getPreco());
        carrinhoProdutoDAO.adicionarAoCarrinho(item);

        if (!estoqueService.removerProdutosDoEstoqueFIFO(List.of(item))) {
            System.err.println("Estoque insuficiente para o produto: " + produto.getNome());
            return false;
        }

        System.out.println(produto.getNome() + " adicionado ao carrinho! Qtd: " + quantidade);
        carregarProdutosDoEstoque();
        return true;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
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
                        this.usuarioId,
                        this.cpf,
                        this.nomeUsuario,
                        this.login,
                        this.clienteId,
                        this.endereco
                )
        );
    }

    @FXML
    private void handleIrFoodToSave() {
        Stage stage = (Stage) produtosContainer.getScene().getWindow();
        AppSplashController.<ClienteFoodToSaveController>trocarCenaComDados(
                stage,
                "/view/ClienteFoodToSaveView.fxml",
                "Food To Save",
                controller -> controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco)
        );
    }
}
