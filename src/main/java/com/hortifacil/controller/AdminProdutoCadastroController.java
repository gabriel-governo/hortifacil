package com.hortifacil.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.dao.ProdutoDAOImpl;
import com.hortifacil.dao.UnidadeMedidaDAO;
import com.hortifacil.dao.UnidadeMedidaDAOImpl;
import com.hortifacil.model.Produto;
import com.hortifacil.model.UnidadeMedida;
import com.hortifacil.service.ProdutoService;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AdminProdutoCadastroController {

    @FXML private TextField nomeField;
    @FXML private TextField precoField;
    @FXML private TextField imagemField;
    @FXML private TextArea descricaoArea;
    @FXML private ComboBox<UnidadeMedida> unidadeComboBox;
    @FXML private TextField diasParaVencerField;
    @FXML private TextField descontoInicioField;
    @FXML private TextField descontoDiarioField;
    @FXML private Button salvarBtn;
    @FXML private Button alterarProdutosBtn;
    @FXML private Label mensagemLabel;

    private ProdutoService produtoService;
    private UnidadeMedidaDAO unidadeMedidaDAO;

    @FXML
    private void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            unidadeMedidaDAO = new UnidadeMedidaDAOImpl(conn);

            ProdutoDAO produtoDAO = new ProdutoDAOImpl(conn); 
            produtoService = new ProdutoService(produtoDAO);


            // Carregar unidades
            List<UnidadeMedida> unidades = unidadeMedidaDAO.listarTodas();
            unidadeComboBox.getItems().addAll(unidades);
            if (!unidades.isEmpty()) unidadeComboBox.getSelectionModel().selectFirst();

            salvarBtn.setOnAction(e -> salvarProduto());

            // === MÁSCARA DE MOEDA BR ===
            aplicarMascaraMoeda(precoField);

        } catch (SQLException e) {
            e.printStackTrace();
            setMensagem("Erro de conexão: " + e.getMessage(), "red");
        }
    }

    private void aplicarMascaraMoeda(TextField textField) {
    Locale localeBR = Locale.of("pt", "BR");
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(localeBR);

    // Valor inicial
    textField.setText("R$ 0,00");

    // Listener seguro
    textField.textProperty().addListener((obs, oldValue, newValue) -> {
        if (newValue == null || newValue.isEmpty()) {
            Platform.runLater(() -> textField.setText("R$ 0,00"));
            return;
        }

        // Mantém apenas números
        String digits = newValue.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            Platform.runLater(() -> textField.setText("R$ 0,00"));
            return;
        }

        try {
            double value = Double.parseDouble(digits) / 100.0;
            String formatted = currencyFormat.format(value);

            // Atualiza apenas se for diferente
            if (!textField.getText().equals(formatted)) {
                Platform.runLater(() -> {
                    textField.setText(formatted);
                    textField.positionCaret(formatted.length());
                });
            }
        } catch (NumberFormatException e) {
            Platform.runLater(() -> textField.setText("R$ 0,00"));
        }
    });
}

    private void salvarProduto() {
        try {
            Produto p = criarProdutoDoFormulario();
            int id = produtoService.cadastrarProduto(p);
            setMensagem("Produto cadastrado com sucesso! ID: " + id, "green");
            limparCampos();
        } catch (Exception e) {
            e.printStackTrace();
            setMensagem("Erro ao salvar produto: " + e.getMessage(), "red");
        }
    }

private double parsePreco(String precoStr) {
    try {
        if (precoStr == null || precoStr.isBlank()) {
            return 0.0;
        }

        System.out.println("DEBUG precoStr original: [" + precoStr + "]");

        // Remove "R$", espaços, e converte vírgula em ponto
        String clean = precoStr.replace("R$", "")
                               .replace("\u00A0", "") // remove espaço não-quebrável
                               .replace(" ", "")
                               .replace(".", "")
                               .replace(",", ".")
                               .trim();

        System.out.println("DEBUG precoStr limpo: [" + clean + "]");

        return Double.parseDouble(clean);
    } catch (NumberFormatException e) {
        System.err.println("Erro ao converter preço: " + precoStr);
        e.printStackTrace();
        return 0.0;
    }
}


private Produto criarProdutoDoFormulario() {
    String nome = nomeField.getText().trim();

    // Usando método de conversão com máscara BRL
    double preco = parsePreco(precoField.getText());

    String imagem = imagemField.getText().trim();
    String descricao = descricaoArea.getText().trim();
    UnidadeMedida unidade = unidadeComboBox.getSelectionModel().getSelectedItem();
    int diasParaVencer = Integer.parseInt(diasParaVencerField.getText().trim());
    int descontoInicio = Integer.parseInt(descontoInicioField.getText().trim());
    double descontoDiario = Double.parseDouble(descontoDiarioField.getText().trim());

    Produto p = new Produto();
    p.setNome(nome);
    p.setPreco(preco);
    p.setCaminhoImagem(imagem);
    p.setDescricao(descricao);
    p.setUnidade(unidade);
    p.setDiasParaVencer(diasParaVencer);
    p.setDescontoInicio(descontoInicio);
    p.setDescontoDiario(descontoDiario);
    return p;
}

    private void limparCampos() {
        nomeField.clear();
        precoField.clear();
        imagemField.clear();
        descricaoArea.clear();
        unidadeComboBox.getSelectionModel().selectFirst();
        diasParaVencerField.clear();
        descontoInicioField.clear();
        descontoDiarioField.clear();
    }

    private void setMensagem(String texto, String cor) {
        mensagemLabel.setText(texto);
        mensagemLabel.setStyle("-fx-text-fill:" + cor);
    }

    @FXML
    private void handleVoltar(ActionEvent event) {
        AppSplashController.trocarCena(event, "/view/AdminHomeProdutoView.fxml", "Home Admin");
    }

    @FXML
    private void abrirTelaAlterarProdutos(ActionEvent event) {
        AppSplashController.trocarCena(event, "/view/AdminProdutoEditarView.fxml", "Alterar Produtos");
    }
}
