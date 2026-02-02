package com.hortifacil.controller;

import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.dao.ProdutoDAOImpl;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.Produto;
import com.hortifacil.model.ProdutoEstoque;
import com.hortifacil.service.ProdutoService;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.text.DecimalFormat;
import javafx.util.Duration;

public class ClienteProdutoCardController {

    private String cpf;
    private String nomeUsuario;
    private int clienteId;
    private String endereco;
     private String login;

    @FXML private Label nomeLabel;
    @FXML private Label precoLabel;
    @FXML private ImageView imagemView;
    @FXML private Button adicionarBtn;
    @FXML private Label mensagemLabel;
    @FXML private TextField quantidadeField;
    @FXML private Label quantidadeLabel;
    @FXML private Label mediaEstrelasLabel;


    private int quantidadeDisponivel;
    private Produto produto;
    private ProdutoEstoque produtoEstoque;
    private ProdutoCardListener listener;

    private final DecimalFormat df = new DecimalFormat("#,##0.00");

public static interface ProdutoCardListener {
    void onAdicionarAoCarrinho(ProdutoEstoque produtoEstoque, int quantidade);
}

    public void setListener(ProdutoCardListener listener) {
        this.listener = listener;
    }

    @FXML
    public void initialize() {
        System.out.println("🧠 mensagemLabel carregado? " + (mensagemLabel != null));
    }

    public void setDadosUsuario(String cpf, String nome, String login, int clienteId, String endereco) {
        this.cpf = cpf;
        this.nomeUsuario = nome;
        this.login = login;
        this.clienteId = clienteId;
        this.endereco = endereco;
    }


    public void setProdutoEstoque(ProdutoEstoque produtoEstoque) {
        this.produtoEstoque = produtoEstoque;
        this.produto = produtoEstoque.getProduto();
        this.quantidadeDisponivel = produtoEstoque.getQuantidade();
        atualizarVisual();
    }

private void atualizarVisual() {
    if (produto == null) return;

    nomeLabel.setText(produto.getNome());
    quantidadeLabel.setText(quantidadeDisponivel + " " + produto.getUnidade() + " disponíveis");
    precoLabel.setText("R$ " + df.format(produto.getPreco()));

    // Imagem
    try {
        var recurso = getClass().getResource(produto.getCaminhoImagem());
        if (recurso != null) imagemView.setImage(new Image(recurso.toExternalForm()));
        else imagemView.setImage(new Image(getClass().getResource("/imagens/placeholder.png").toExternalForm()));
    } catch (Exception e) {
        imagemView.setImage(new Image(getClass().getResource("/imagens/placeholder.png").toExternalForm()));
    }

    // Média de estrelas via banco
    try (var conn = DatabaseConnection.getConnection()) {
        ProdutoDAO produtoDAO = new ProdutoDAOImpl(conn);
        int estrelas = new ProdutoService(produtoDAO).calcularMediaEstrelas(produto.getId());

        StringBuilder estrelasStr = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            estrelasStr.append(i <= estrelas ? "★" : "☆");
        }
        mediaEstrelasLabel.setText(estrelasStr.toString());

    } catch (SQLException e) {
        e.printStackTrace();
        mediaEstrelasLabel.setText("★☆☆☆☆"); // fallback
    }
}

@FXML
private void handleAdicionarAoCarrinho() {
    if (produtoEstoque == null) {
        exibirMensagem("❌ Produto sem estoque disponível.", "crimson");
        return;
    }

    int quantidade = 1;
    try {
        quantidade = Integer.parseInt(quantidadeField.getText());
        if (quantidade <= 0) throw new NumberFormatException();
    } catch (NumberFormatException e) {
        exibirMensagem("⚠ Quantidade inválida. Usando 1.", "orange");
        quantidade = 1;
    }

    if (quantidade > quantidadeDisponivel) {
        exibirMensagem("❌ Só temos " + quantidadeDisponivel + " " + produto.getUnidade() + " disponíveis.", "crimson");
        return;
    }

    if (listener != null) {
        try {
            listener.onAdicionarAoCarrinho(produtoEstoque, quantidade);

            // Atualiza estoque local
            quantidadeDisponivel -= quantidade;
            if (quantidadeDisponivel < 0) quantidadeDisponivel = 0;
            quantidadeLabel.setText(quantidadeDisponivel + " " + produto.getUnidade() + " disponíveis");

            exibirMensagem("✅ " + produto.getNome() + " adicionado ao carrinho!", "limegreen");

        } catch (Exception e) {
            e.printStackTrace();
            exibirMensagem("❌ Erro ao adicionar ao carrinho.", "crimson");
        }
    } else {
        exibirMensagem("⚠️ Ação indisponível. Tente novamente.", "crimson");
    }
}

    @FXML
    public void abrirDetalhesProduto() {
        Stage stage = (Stage) imagemView.getScene().getWindow();

        AppSplashController.<ClienteProdutoDetalheController>trocarCenaComDados(
            stage,
            "/view/ClienteProdutoDetalheView.fxml",
            "Detalhes do Produto",
            controller -> {
                controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco);
                controller.setProduto(produto, produtoEstoque);
            }
        );
    }

public void exibirMensagem(String texto, String cor) {
        // Garante que a atualização ocorra na Thread JavaFX
        Platform.runLater(() -> {
            mensagemLabel.setText(texto);
            mensagemLabel.setStyle("-fx-text-fill: " + cor + "; -fx-font-weight: bold;");
            mensagemLabel.setOpacity(1);

            // Faz a mensagem desaparecer suavemente
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> {
                FadeTransition fade = new FadeTransition(Duration.seconds(1), mensagemLabel);
                fade.setFromValue(1);
                fade.setToValue(0);
                fade.setOnFinished(ev -> mensagemLabel.setText(""));
                fade.play();
            });
            pause.play();
        });
    }
}
