package com.hortifacil.controller;

import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.ProdutoEstoque;
import com.hortifacil.model.Produto;
import com.hortifacil.service.EstoqueService;
import com.hortifacil.service.ProdutoService;
import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.dao.ProdutoDAOImpl;
import com.hortifacil.dao.ProdutoEstoqueDAO;
import com.hortifacil.dao.ProdutoEstoqueDAOImpl;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public class AdminAdicionarEstoqueController {

    @FXML private TableView<ProdutoEstoque> tvEstoque;
    @FXML private TableColumn<ProdutoEstoque, String> colProduto;
    @FXML private TableColumn<ProdutoEstoque, LocalDate> colDataColheita;
    @FXML private TableColumn<ProdutoEstoque, LocalDate> colDataValidade;
    @FXML private TableColumn<ProdutoEstoque, Integer> colQuantidade;
    @FXML private TableColumn<ProdutoEstoque, Integer> colLote;

    @FXML private ComboBox<Produto> cbProduto;
    @FXML private TextField txtQuantidade;
    @FXML private Button btnAdicionar;
    @FXML private Button btnVoltarHome;
    @FXML private Label lblInfo;

    private EstoqueService estoqueService;
    private ProdutoService produtoService;

    @FXML
    public void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();

            ProdutoEstoqueDAO produtoEstoqueDAO = new ProdutoEstoqueDAOImpl(conn, null);

            ProdutoDAO produtoDAO = new ProdutoDAOImpl(conn);

            produtoService = new ProdutoService(produtoDAO);
            estoqueService = EstoqueService.getInstance(produtoDAO, produtoEstoqueDAO);

            cbProduto.getItems().addAll(produtoService.listarTodos());

            colProduto.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProduto().getNome()));
            colQuantidade.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getQuantidade()).asObject());
            colDataColheita.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDataColhido()));
            colDataValidade.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getDataValidade()));
            colLote.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getLote()).asObject());

            atualizarListaEstoque();

            btnAdicionar.setOnAction(e -> adicionarProduto());
            btnVoltarHome.setOnAction(e -> voltarHome());

        } catch (Exception e) {
            e.printStackTrace(); 
            System.err.println("Erro ao iniciar controlador: " + e.getMessage());
        }
    }

    private void adicionarProduto() {
        try {
            Produto produtoSelecionado = cbProduto.getValue();
            if (produtoSelecionado == null) {
                lblInfo.setText("Selecione um produto.");
                return;
            }

            int quantidade = Integer.parseInt(txtQuantidade.getText());
            if (quantidade <= 0) {
                lblInfo.setText("Informe uma quantidade válida.");
                return;
            }

            LocalDate dataColheita = LocalDate.now();
            LocalDate dataValidade = dataColheita.plusDays(produtoSelecionado.getDiasParaVencer());

            int proximoLote = estoqueService.getProximoLote(produtoSelecionado.getId());

            ProdutoEstoque estoque = new ProdutoEstoque(produtoSelecionado, quantidade, dataColheita, dataValidade, proximoLote);
            estoqueService.adicionarProduto(estoque);

            atualizarListaEstoque();

            lblInfo.setText("Produto adicionado com sucesso!");
            cbProduto.setValue(null);
            txtQuantidade.clear();

        } catch (NumberFormatException e) {
            lblInfo.setText("Quantidade inválida.");
        } catch (Exception e) {
            e.printStackTrace();
            lblInfo.setText("Erro ao adicionar produto: " + e.getMessage());
        }
    }

    private void atualizarListaEstoque() {
    try {
        tvEstoque.getItems().clear();
        
        var lista = estoqueService.listarEstoque();
        
        lista.sort((e1, e2) -> e2.getId() - e1.getId());
        
        tvEstoque.getItems().addAll(lista);
        
    } catch (SQLException e) {
        e.printStackTrace();
        lblInfo.setText("Erro ao listar estoque: " + e.getMessage());
    }
}

@FXML
private void voltarHome() {
    Stage stage = (Stage) btnVoltarHome.getScene().getWindow();
    AppSplashController.trocarCena(stage, "/view/AdminHomeProdutoView.fxml", "home");
}

}