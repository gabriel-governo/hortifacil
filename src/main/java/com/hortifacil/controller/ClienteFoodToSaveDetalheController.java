package com.hortifacil.controller;

import com.hortifacil.dao.CarrinhoProdutoDAOImpl;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.ProdutoEstoque;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class ClienteFoodToSaveDetalheController {

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
    @FXML private VBox avaliacoesContainer;
    @FXML private Label mediaEstrelasLabel;


    private ProdutoEstoque produtoEstoque;
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

    public void setProdutoEstoque(ProdutoEstoque produtoEstoque) {
        this.produtoEstoque = produtoEstoque;
        quantidadeDisponivel = produtoEstoque.getQuantidade();

        nomeLabel.setText(produtoEstoque.getProduto().getNome());

        int estrelasMedia = calcularMediaEstrelas(produtoEstoque.getProduto().getId());
        String estrelasStr = "★".repeat(estrelasMedia) + "☆".repeat(5 - estrelasMedia);
        mediaEstrelasLabel.setText(estrelasStr);

        descricaoLabel.setText(produtoEstoque.getProduto().getDescricao());
        quantidadeLabel.setText("Disponível: " + quantidadeDisponivel + " " + produtoEstoque.getProduto().getUnidade());
        dataColheitaLabel.setText("Colheita: " + produtoEstoque.getDataColhido());
        dataValidadeLabel.setText("Validade: " + produtoEstoque.getDataValidade());

        carregarImagemProduto();
        atualizarPreco();
        carregarAvaliacoes();

        System.out.println("Desconto calculado: " + produtoEstoque.getDesconto() +
                   ", Preço com desconto: " + produtoEstoque.getPrecoComDesconto());

    }

    // =================== IMAGEM ===================
    private void carregarImagemProduto() {
        try {
            File arquivoImagem = new File(produtoEstoque.getProduto().getCaminhoImagem());
            if (arquivoImagem.exists()) {
                imagemView.setImage(new Image(arquivoImagem.toURI().toString()));
            } else {
                var recurso = getClass().getResource(produtoEstoque.getProduto().getCaminhoImagem());
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
    double precoOriginal = produtoEstoque.getProduto().getPreco();
    double precoComDesconto = produtoEstoque.getPrecoComDesconto();
    int percentualDesconto = produtoEstoque.getPercentualDesconto();

    precoOriginalLabel.setText("R$ " + df.format(precoOriginal));
    precoDescontoLabel.setText("R$ " + df.format(precoComDesconto));
    porcentagemLabel.setText("-" + percentualDesconto + "%");

    // Estilo visual
    if (percentualDesconto > 0) {
        precoOriginalLabel.setStyle("-fx-text-fill: red; -fx-strikethrough: true; -fx-font-size: 16px;");
        precoDescontoLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 20px;");
        porcentagemLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 16px;");
        precoDescontoLabel.setVisible(true);
        porcentagemLabel.setVisible(true);
    } else {
        precoOriginalLabel.setStyle("-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 20px;");
        precoDescontoLabel.setVisible(false);
        porcentagemLabel.setVisible(false);
    }
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
            mensagemLabel.setText("❌ Só temos " + quantidadeDisponivel + " " + produtoEstoque.getProduto().getUnidade() + " disponíveis.");
            return;
        }

        double precoUnitario = produtoEstoque.getPrecoComDesconto();

        try (var conn = DatabaseConnection.getConnection()) {
            var carrinhoDAO = new CarrinhoProdutoDAOImpl(conn);
            int idCarrinho = carrinhoDAO.obterOuCriarCarrinhoAberto(clienteId);

            CarrinhoProduto item = new CarrinhoProduto(
                    idCarrinho,
                    clienteId,
                    produtoEstoque.getProduto(),
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
        AppSplashController.<ClienteFoodToSaveController>trocarCenaComDados(
                stage,
                "/view/ClienteFoodToSaveView.fxml",
                "Food To Save",
                controller -> controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco)
        );
    }

    @FXML
public void initialize() {
    estrelasComboBox.getItems().addAll(0,1,2,3,4,5);
}

private void carregarAvaliacoes() {
    avaliacoesContainer.getChildren().clear();
    try (var conn = DatabaseConnection.getConnection()) {
        String sql = "SELECT c.nome, a.estrelas, a.comentario " +
                     "FROM avaliacao a JOIN cliente c ON a.id_cliente = c.id_cliente " +
                     "WHERE a.id_produto = ?";

        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, produtoEstoque.getProduto().getId());
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

@FXML
private void enviarAvaliacao() {
    int estrelas = estrelasComboBox.getValue() != null ? estrelasComboBox.getValue() : 0;
    String comentario = comentarioField.getText().trim();

    if (produtoEstoque == null || comentario.isEmpty()) return;

    try (var conn = DatabaseConnection.getConnection()) {
        String sql = "INSERT INTO avaliacao (id_cliente, id_produto, estrelas, comentario) VALUES (?, ?, ?, ?)";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clienteId);
            stmt.setInt(2, produtoEstoque.getProduto().getId());
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

private int calcularMediaEstrelas(int idProduto) {
    int media = 0;
    try (var conn = DatabaseConnection.getConnection()) {
        String sql = "SELECT AVG(estrelas) AS media FROM avaliacao WHERE id_produto = ?";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProduto);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    media = (int) Math.round(rs.getDouble("media")); // arredonda para inteiro
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return media;
}

}
