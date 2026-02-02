package com.hortifacil.controller;

import java.text.DecimalFormat;
import com.hortifacil.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;

import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.dao.ProdutoDAOImpl;
import com.hortifacil.model.Produto;
import com.hortifacil.model.ProdutoEstoque;
import com.hortifacil.service.ProdutoService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class ClienteFoodToSaveCardController {
    @FXML private Label nomeLabel;
    @FXML private Label precoOriginalLabel;
    @FXML private Label precoDescontoLabel;
    @FXML private Label porcentagemLabel;
    @FXML private Label quantidadeLabel;
    @FXML private TextField quantidadeField;
    @FXML private Button adicionarBtn;
    @FXML private Label mensagemLabel;
    @FXML private ImageView imagemView;
    @FXML private Label avaliacaoLabel;

    private ProdutoEstoque produtoEstoque;
    private int quantidadeDisponivel;
    private ProdutoCardListener listener;

    private String cpf;
    private String nomeUsuario;
    private int clienteId;
    private String endereco;
    private String login;

    private final DecimalFormat df = new DecimalFormat("#0.00");


    public void setDadosUsuario(String cpf, String nome, String login, int clienteId, String endereco) {
    this.cpf = cpf;
    this.nomeUsuario = nome;
    this.login = login;
    this.clienteId = clienteId;
    this.endereco = endereco;
    }

    public interface ProdutoCardListener {
        void onAdicionarAoCarrinho(ProdutoEstoque produtoEstoque, int quantidade);
    }

    public void setListener(ProdutoCardListener listener) { this.listener = listener; }

    public void setProdutoEstoque(ProdutoEstoque produtoEstoque) {
        this.produtoEstoque = produtoEstoque;
        this.quantidadeDisponivel = produtoEstoque.getQuantidade();
        atualizarVisual();
    }

private void atualizarVisual() {
    Produto produto = produtoEstoque.getProduto();
    int desconto = produtoEstoque.getPercentualDesconto();
    
    // Nome e quantidade
    nomeLabel.setText(produto.getNome());
    quantidadeLabel.setText(quantidadeDisponivel + " " + produto.getUnidade().getNome() + " disponíveis");

    // Formatar preços
    precoOriginalLabel.setText("R$ " + df.format(produto.getPreco()));
    if (desconto > 0) {
        precoDescontoLabel.setText("R$ " + df.format(produtoEstoque.getPrecoComDesconto()));
        porcentagemLabel.setText("-" + desconto + "%");
        precoDescontoLabel.setVisible(true);
        porcentagemLabel.setVisible(true);
    } else {
        precoDescontoLabel.setVisible(false);
        porcentagemLabel.setVisible(false);
    }

    try {
        Connection connection = DatabaseConnection.getConnection();
        
        // Se não precisar do ProdutoDAO dentro do ProdutoEstoqueDAOImpl, pode passar null
        ProdutoDAO produtoDAO = new ProdutoDAOImpl(connection);
        
        ProdutoService produtoService = new ProdutoService(produtoDAO);

        int estrelas = produtoService.calcularMediaEstrelas(produto.getId()); // 0 a 5

        // Criar estrelas visuais
        StringBuilder estrelasStr = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            estrelasStr.append(i <= estrelas ? "★" : "☆");
        }
        avaliacaoLabel.setText(estrelasStr.toString());

            } catch (SQLException e) {
                e.printStackTrace();
                avaliacaoLabel.setText("⭐☆☆☆☆"); // fallback caso dê erro
            }

    // Imagem
    try {
        var recurso = getClass().getResource(produto.getCaminhoImagem());
        if (recurso != null) imagemView.setImage(new Image(recurso.toExternalForm()));
        else imagemView.setImage(new Image(getClass().getResource("/imagens/placeholder.png").toExternalForm()));
    } catch (Exception e) {
        imagemView.setImage(new Image(getClass().getResource("/imagens/placeholder.png").toExternalForm()));
    }
}


@FXML
private void handleAdicionarAoCarrinho() {
    int quantidade = 1;
    try { quantidade = Integer.parseInt(quantidadeField.getText()); } catch (Exception ignored) {}

    if (listener != null) {
        listener.onAdicionarAoCarrinho(produtoEstoque, quantidade);
        mensagemLabel.setStyle("-fx-text-fill: green;");
        mensagemLabel.setText("Produto adicionado ao carrinho!");
    } else {
        mensagemLabel.setStyle("-fx-text-fill: red;");
        mensagemLabel.setText("Erro ao adicionar ao carrinho.");
    }
}

@FXML
public void abrirDetalhesProduto() {
    Stage stage = (Stage) imagemView.getScene().getWindow();
    AppSplashController.<ClienteFoodToSaveDetalheController>trocarCenaComDados(
    stage,
    "/view/ClienteFoodToSaveDetalheView.fxml",
    "Detalhes Food To Save",
    controller -> {
        // Passar dados do usuário primeiro
        controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco);

        // Depois passar o produto
        controller.setProdutoEstoque(produtoEstoque);
    }
);
}

}
