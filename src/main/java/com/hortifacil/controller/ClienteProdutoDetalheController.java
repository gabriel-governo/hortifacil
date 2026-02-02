package com.hortifacil.controller;

import com.hortifacil.model.Produto;
import com.hortifacil.model.ProdutoEstoque;
import com.hortifacil.service.ProdutoService;
import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.dao.CarrinhoProdutoDAOImpl;
import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.dao.ProdutoDAOImpl;
import com.hortifacil.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.File;
import java.sql.*;
import java.text.DecimalFormat;

public class ClienteProdutoDetalheController {

    @FXML private ImageView imagemView;
    @FXML private Label nomeLabel;
    @FXML private Label descricaoLabel;
    @FXML private Label quantidadeLabel;
    @FXML private Label precoOriginalLabel;
    @FXML private Label precoDescontoLabel;
    @FXML private Label porcentagemLabel;
    @FXML private TextField quantidadeField;
    @FXML private Label mensagemLabel;
    @FXML private Label dataColheitaLabel;
    @FXML private Label dataValidadeLabel;
    @FXML private ComboBox<Integer> estrelasComboBox;
    @FXML private TextField comentarioField;
    @FXML private Label mediaEstrelasLabel;


    @FXML private VBox avaliacoesContainer;

    private ProdutoEstoque produtoEstoque;
    private Produto produto;
    private int quantidadeDisponivel;

    private String cpf;
    private String nomeUsuario;
    private int clienteId;
    private String endereco;
    private String login;

    private final DecimalFormat df = new DecimalFormat("#0.00");

    // =================== SETTERS ===================
   public void setDadosUsuario(String cpf, String nome, String login, int clienteId, String endereco) {
        this.cpf = cpf;
        this.nomeUsuario = nome;
        this.login = login;
        this.clienteId = clienteId;
        this.endereco = endereco;
    }


    public void setProduto(Produto produto, ProdutoEstoque produtoEstoque) {
        this.produto = produto;
        this.produtoEstoque = produtoEstoque;

        nomeLabel.setText(produto.getNome());
        descricaoLabel.setText(produto.getDescricao());

        if (produtoEstoque != null) {
            quantidadeDisponivel = produtoEstoque.getQuantidade();
            quantidadeLabel.setText("Disponível: " + quantidadeDisponivel + " " + produto.getUnidade());
            dataColheitaLabel.setText("Colheita: " + produtoEstoque.getDataColhido());
            dataValidadeLabel.setText("Validade: " + produtoEstoque.getDataValidade());
        }

        carregarImagemProduto();
        atualizarPreco();
        carregarAvaliacoes();

        // --- Exibir estrelas ---
       try (var conn = DatabaseConnection.getConnection()) {
           ProdutoDAO produtoDAO = new ProdutoDAOImpl(conn);

            ProdutoService produtoService = new ProdutoService(produtoDAO);
            int estrelas = produtoService.calcularMediaEstrelas(produto.getId()); // 0 a 5

            StringBuilder estrelasStr = new StringBuilder();
            for (int i = 1; i <= 5; i++) {
                estrelasStr.append(i <= estrelas ? "★" : "☆");
            }
            mediaEstrelasLabel.setText(estrelasStr.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            mediaEstrelasLabel.setText("★☆☆☆☆"); // fallback caso dê erro
        }

    }

    // =================== IMAGEM ===================
    private void carregarImagemProduto() {
        try {
            File arquivoImagem = new File(produto.getCaminhoImagem());
            if (arquivoImagem.exists()) {
                imagemView.setImage(new Image(arquivoImagem.toURI().toString()));
            } else {
                var recurso = getClass().getResource(produto.getCaminhoImagem());
                if (recurso != null) {
                    imagemView.setImage(new Image(recurso.toExternalForm()));
                } else {
                    imagemView.setImage(new Image(getClass().getResource("/imagens/placeholder.png").toExternalForm()));
                }
            }
        } catch (Exception e) {
            imagemView.setImage(new Image(getClass().getResource("/imagens/placeholder.png").toExternalForm()));
        }

        imagemView.setFitWidth(200);
        imagemView.setFitHeight(200);
        imagemView.setPreserveRatio(true);
        imagemView.setSmooth(true);
        imagemView.setCache(true);
        imagemView.setClip(new Rectangle(200, 200));
    }

    // =================== PREÇO / DESCONTO ===================
    private void atualizarPreco() {
        double precoOriginal = produto.getPreco();
        double precoComDesconto = produtoEstoque.getPrecoComDesconto();
        int percentualDesconto = produtoEstoque.getPercentualDesconto();

        if (percentualDesconto > 0) {
            precoOriginalLabel.setText("R$ " + df.format(precoOriginal));
            precoDescontoLabel.setText("R$ " + df.format(precoComDesconto));
            porcentagemLabel.setText("-" + percentualDesconto + "%");

            precoOriginalLabel.setVisible(true);
            precoDescontoLabel.setVisible(true);
            porcentagemLabel.setVisible(true);
        } else {
            precoOriginalLabel.setText("R$ " + df.format(precoOriginal));
            precoOriginalLabel.setVisible(true);

            precoDescontoLabel.setVisible(false);
            porcentagemLabel.setVisible(false);
        }
    }

    @FXML
    public void initialize() {
        estrelasComboBox.getItems().addAll(0,1,2,3,4,5);
    }

    // =================== AÇÕES ===================
    @FXML
    private void handleAdicionarAoCarrinho() {
        if (produtoEstoque == null) {
            mensagemLabel.setText("❌ Produto sem estoque disponível.");
            return;
        }

        int quantidade = 1;
        try {
            quantidade = Integer.parseInt(quantidadeField.getText());
            if (quantidade <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mensagemLabel.setText("Quantidade inválida! Usando 1.");
            quantidade = 1;
        }

        if (quantidade > quantidadeDisponivel) {
            mensagemLabel.setText("❌ Só temos " + quantidadeDisponivel + " " + produto.getUnidade() + " disponíveis.");
            return;
        }

        double precoUnitario = produtoEstoque.getPrecoComDesconto();

        try (var conn = DatabaseConnection.getConnection()) {
            var carrinhoDAO = new CarrinhoProdutoDAOImpl(conn);
            int idCarrinho = carrinhoDAO.obterOuCriarCarrinhoAberto(clienteId);

            CarrinhoProduto item = new CarrinhoProduto(
                    idCarrinho,
                    clienteId,
                    produto,
                    quantidade,
                    precoUnitario
            );

            carrinhoDAO.adicionarAoCarrinho(item);
            mensagemLabel.setText("✅ Adicionado ao carrinho!");
        } catch (SQLException e) {
            e.printStackTrace();
            mensagemLabel.setText("❌ Erro ao adicionar ao carrinho!");
        }
    }

    @FXML
    private void handleIrAoCarrinho() {
        Stage stage = (Stage) imagemView.getScene().getWindow();
        AppSplashController.<ClienteCarrinhoController>trocarCenaComDados(
                stage,
                "/view/ClienteCarrinhoView.fxml",
                "Carrinho",
                controller -> controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco)
        );
    }

    @FXML
    private void handleVoltar() {
        Stage stage = (Stage) imagemView.getScene().getWindow();
        AppSplashController.<ClienteProdutoListarController>trocarCenaComDados(
                stage,
                "/view/ClienteProdutoListarView.fxml",
                "Produtos",
                controller -> controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco)

        );
    }

    @FXML
    private void enviarAvaliacao() {
        int estrelas = estrelasComboBox.getValue() != null ? estrelasComboBox.getValue() : 0;
        String comentario = comentarioField.getText().trim();

        if (produto == null || comentario.isEmpty()) return;

        try (var conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO avaliacao (id_cliente, id_produto, estrelas, comentario) VALUES (?, ?, ?, ?)";
            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, clienteId);
                stmt.setInt(2, produto.getId());
                stmt.setInt(3, estrelas);
                stmt.setString(4, comentario);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        comentarioField.clear();
        estrelasComboBox.getSelectionModel().clearSelection();
        carregarAvaliacoes();
    }

    private void carregarAvaliacoes() {
        avaliacoesContainer.getChildren().clear();

        try (var conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT c.nome, a.estrelas, a.comentario " +
                         "FROM avaliacao a JOIN cliente c ON a.id_cliente = c.id_cliente " +
                         "WHERE a.id_produto = ?";

            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, produto.getId());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        VBox card = criarCardAvaliacao(
                            rs.getString("nome"),
                            rs.getInt("estrelas"),
                            rs.getString("comentario")
                        );
                        avaliacoesContainer.getChildren().add(card);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox criarCardAvaliacao(String cliente, int estrelas, String comentario) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: #fff; -fx-padding: 10; " +
                      "-fx-border-color: #ccc; -fx-border-radius: 8; -fx-background-radius: 8;");

        Label lblCliente = new Label(cliente);
        lblCliente.setStyle("-fx-font-weight: bold;");

        Label lblEstrelas = new Label("⭐".repeat(estrelas));
        Label lblComentario = new Label(comentario);
        lblComentario.setWrapText(true);

        card.getChildren().addAll(lblCliente, lblEstrelas, lblComentario);
        return card;
    }
}
