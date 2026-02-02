package com.hortifacil.controller;

import com.hortifacil.dao.ProdutoEstoqueDAO;
import com.hortifacil.dao.ProdutoEstoqueDAOImpl;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.ProdutoEstoque;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AdminEstoqueDetalhesController {

    @FXML
    private TableView<ProdutoEstoque> tvDetalhesLotes;

    @FXML
    private TableColumn<ProdutoEstoque, Integer> colLote;

    @FXML
    private TableColumn<ProdutoEstoque, Integer> colQuantidade;

    @FXML
    private TableColumn<ProdutoEstoque, String> colDataColheita;

    @FXML
    private TableColumn<ProdutoEstoque, String> colDataValidade;

    @FXML
    private Label lblTituloProduto;

    private ProdutoEstoque produto;

    private ProdutoEstoqueDAO produtoEstoqueDAO;

    public void setProduto(ProdutoEstoque produto) {
        this.produto = produto;
        lblTituloProduto.setText("Detalhes do Produto: " + produto.getProduto().getNome());
        carregarDetalhes();
    }

    @FXML
    public void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            produtoEstoqueDAO = new ProdutoEstoqueDAOImpl(conn, null); // ProdutoDAO não é necessário aqui

            colLote.setCellValueFactory(cell ->
                    new SimpleIntegerProperty(cell.getValue().getLote()).asObject());

            colQuantidade.setCellValueFactory(cell ->
                    new SimpleIntegerProperty(cell.getValue().getQuantidade()).asObject());

            colDataColheita.setCellValueFactory(cell ->
                    new SimpleStringProperty(cell.getValue().getDataColhido().toString()));

            colDataValidade.setCellValueFactory(cell ->
                    new SimpleStringProperty(cell.getValue().getDataValidade().toString()));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void carregarDetalhes() {
        try {
            List<ProdutoEstoque> lotes = produtoEstoqueDAO.listarLotesPorProduto(produto.getProduto().getId());
            tvDetalhesLotes.getItems().setAll(lotes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

public void setLotes(List<ProdutoEstoque> lotes) {
    tvDetalhesLotes.getItems().setAll(lotes);
}


    @FXML
    private void fechar() {
        Stage stage = (Stage) tvDetalhesLotes.getScene().getWindow();
        stage.close();
    }
}
