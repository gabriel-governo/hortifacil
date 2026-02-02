package com.hortifacil.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.dao.ProdutoDAOImpl;
import com.hortifacil.dao.UnidadeMedidaDAO;
import com.hortifacil.dao.UnidadeMedidaDAOImpl;
import com.hortifacil.model.Produto;
import com.hortifacil.model.UnidadeMedida;
import com.hortifacil.service.ProdutoService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AdminProdutoEditarController {

    @FXML private ComboBox<Produto> produtosComboBox; // Agora ComboBox
    @FXML private TextField nomeField;
    @FXML private TextField precoField;
    @FXML private TextField imagemField;
    @FXML private TextArea descricaoArea;
    @FXML private ComboBox<UnidadeMedida> unidadeComboBox;
    @FXML private TextField diasParaVencerField;
    @FXML private TextField descontoInicioField;
    @FXML private TextField descontoDiarioField;
    @FXML private Button alterarBtn;
    @FXML private Button excluirBtn;
    @FXML private Button voltarBtn;
    @FXML private Label mensagemLabel;

    private ProdutoService produtoService;
    private UnidadeMedidaDAO unidadeMedidaDAO;
    private ObservableList<Produto> produtosList = FXCollections.observableArrayList();
    private Produto produtoSelecionado = null;

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

            // Carregar produtos existentes
            carregarProdutos();

            // Listener de seleção do ComboBox
            produtosComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) carregarProdutoParaEdicao(newSel);
            });

            alterarBtn.setOnAction(e -> alterarProduto());
            excluirBtn.setOnAction(e -> excluirProduto());

            // === MÁSCARA DE MOEDA ===
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

    private void carregarProdutos() {
        try {
            produtosList.clear();
            produtosList.addAll(produtoService.listarTodos());
            produtosComboBox.setItems(produtosList);
            if (!produtosList.isEmpty()) produtosComboBox.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            e.printStackTrace();
            setMensagem("Erro ao carregar produtos: " + e.getMessage(), "red");
        }
    }

    private void carregarProdutoParaEdicao(Produto p) {
        produtoSelecionado = p;
        nomeField.setText(p.getNome());

        var currencyFormat = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.of("pt", "BR"));
        precoField.setText(currencyFormat.format(p.getPreco()));

        imagemField.setText(p.getCaminhoImagem());
        descricaoArea.setText(p.getDescricao());
        unidadeComboBox.getSelectionModel().select(p.getUnidade());
        diasParaVencerField.setText(String.valueOf(p.getDiasParaVencer()));
        descontoInicioField.setText(String.valueOf(p.getDescontoInicio()));
        descontoDiarioField.setText(String.valueOf(p.getDescontoDiario()));
    }

    private void alterarProduto() {
        if (produtoSelecionado == null) {
            setMensagem("Selecione um produto para alterar.", "red");
            return;
        }
        try {
            Produto p = criarProdutoDoFormulario();
            p.setId(produtoSelecionado.getId());
            produtoService.atualizarProduto(p);
            setMensagem("Produto alterado com sucesso!", "green");
            limparCampos();
            carregarProdutos();
        } catch (Exception e) {
            e.printStackTrace();
            setMensagem("Erro ao alterar produto: " + e.getMessage(), "red");
        }
    }

    private void excluirProduto() {
        if (produtoSelecionado == null) {
            setMensagem("Selecione um produto para excluir.", "red");
            return;
        }

        // Exibe um alerta de confirmação antes da exclusão
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação de Exclusão");
        alert.setHeaderText("Excluir produto: " + produtoSelecionado.getNome());
        alert.setContentText("Tem certeza que deseja excluir este produto?\n"
                + "Essa ação também removerá os registros relacionados no estoque.");

        Optional<ButtonType> resultado = alert.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                produtoService.removerProduto(produtoSelecionado.getId());
                setMensagem("Produto removido com sucesso!", "green");
                limparCampos();
                carregarProdutos();
            } catch (Exception e) {
                e.printStackTrace();
                setMensagem("Erro ao excluir produto: " + e.getMessage(), "red");
            }
        } else {
            setMensagem("Exclusão cancelada.", "gray");
        }
    }

    private Produto criarProdutoDoFormulario() {
        String nome = nomeField.getText().trim();
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

    private double parsePreco(String precoStr) {
    try {
        if (precoStr == null || precoStr.isBlank()) return 0.0;

        String clean = precoStr.replace("R$", "")
                               .replace("\u00A0", "")
                               .replace(" ", "")
                               .replace(".", "")
                               .replace(",", ".")
                               .trim();

        return Double.parseDouble(clean);
    } catch (NumberFormatException e) {
        System.err.println("Erro ao converter preço: " + precoStr);
        return 0.0;
    }
}

    private void limparCampos() {
        produtoSelecionado = null;
        nomeField.clear();
        precoField.clear();
        imagemField.clear();
        descricaoArea.clear();
        unidadeComboBox.getSelectionModel().selectFirst();
        diasParaVencerField.clear();
        descontoInicioField.clear();
        descontoDiarioField.clear();
        produtosComboBox.getSelectionModel().clearSelection();
    }

    private void setMensagem(String texto, String cor) {
        mensagemLabel.setText(texto);
        mensagemLabel.setStyle("-fx-text-fill:" + cor);
    }

    @FXML
    private void handleVoltar(ActionEvent event) {
        AppSplashController.trocarCena(event, "/view/AdminProdutoCadastroView.fxml", "Cadastro de Produtos");
    }
}
