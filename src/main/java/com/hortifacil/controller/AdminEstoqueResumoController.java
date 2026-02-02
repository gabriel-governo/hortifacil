package com.hortifacil.controller;

import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.dao.ProdutoDAOImpl;
import com.hortifacil.dao.ProdutoEstoqueDAO;
import com.hortifacil.dao.ProdutoEstoqueDAOImpl;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.ProdutoEstoque;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class AdminEstoqueResumoController {

    @FXML
    private TableView<ProdutoEstoque> tvResumoEstoque;

    @FXML
    private TableColumn<ProdutoEstoque, String> colProduto;

    @FXML
    private TableColumn<ProdutoEstoque, Integer> colQuantidade;

    @FXML
    private TableColumn<ProdutoEstoque, String> colValidade;

    @FXML
    private TableColumn<ProdutoEstoque, Double> colPreco;

    @FXML
    private TableColumn<ProdutoEstoque, String> colDesconto;

    @FXML
    private TableColumn<ProdutoEstoque, Void> colDetalhes;

    private ProdutoEstoqueDAO produtoEstoqueDAO;

    @FXML
    public void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ProdutoDAO produtoDAO = new ProdutoDAOImpl(conn);
            produtoEstoqueDAO = new ProdutoEstoqueDAOImpl(conn, produtoDAO);

            // Nome do produto
            colProduto.setCellValueFactory(cell ->
                    new SimpleStringProperty(cell.getValue().getProduto().getNome()));

            // Quantidade total em estoque (agrupado)
            colQuantidade.setCellValueFactory(cell ->
                    new SimpleIntegerProperty(cell.getValue().getQuantidade()).asObject());

            // Validade mínima entre os lotes
            colValidade.setCellValueFactory(cell -> {
                LocalDate validade = cell.getValue().getDataValidade();
                long diasParaVencer = ChronoUnit.DAYS.between(LocalDate.now(), validade);
                return new SimpleStringProperty(diasParaVencer + " dias / " + validade);
            });

            // Preço unitário
            colPreco.setCellValueFactory(cell ->
                    new SimpleDoubleProperty(cell.getValue().getProduto().getPreco()).asObject());

            // Desconto ativo
            colDesconto.setCellValueFactory(cell -> {
                double descontoDiario = cell.getValue().getProduto().getDescontoDiario();
                int descontoInicio = cell.getValue().getProduto().getDescontoInicio();
                String tipoDesconto = "-";
                if (descontoDiario > 0) tipoDesconto = "Diário";
                else if (descontoInicio > 0) tipoDesconto = "Início";
                return new SimpleStringProperty(tipoDesconto);
            });

            // Botão Detalhes
            colDetalhes.setCellFactory(param -> new TableCell<>() {
                private final Button btn = new Button("Detalhes");

                {
                    btn.setOnAction(event -> {
                        ProdutoEstoque produto = getTableView().getItems().get(getIndex());
                        abrirDetalhes(produto);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btn);
                }
            });

            atualizarResumoAgrupado();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void atualizarResumoAgrupado() {
        try {
            // Lista todos os lotes
            List<ProdutoEstoque> lotes = produtoEstoqueDAO.listarTodos();

            // Agrupa por produto
            Map<Integer, List<ProdutoEstoque>> agrupados = lotes.stream()
                    .collect(Collectors.groupingBy(lote -> lote.getProduto().getId()));

            // Cria lista final de resumo
            ObservableList<ProdutoEstoque> resumo = FXCollections.observableArrayList();
            for (List<ProdutoEstoque> listaLotes : agrupados.values()) {
                ProdutoEstoque primeiro = listaLotes.get(0); // pega referência do produto
                int quantidadeTotal = listaLotes.stream().mapToInt(ProdutoEstoque::getQuantidade).sum();
                LocalDate validadeMinima = listaLotes.stream()
                        .map(ProdutoEstoque::getDataValidade)
                        .min(LocalDate::compareTo)
                        .orElse(LocalDate.now());

                ProdutoEstoque resumoProduto = new ProdutoEstoque(
                        primeiro.getId(), // id do primeiro lote (ou 0)
                        primeiro.getProduto(),
                        quantidadeTotal,
                        LocalDate.now(),   // dataColheita não importa no resumo
                        validadeMinima,
                        0                  // lote = 0 no resumo
                );
                resumo.add(resumoProduto);
            }

            tvResumoEstoque.setItems(resumo);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void abrirDetalhes(ProdutoEstoque produto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminEstoqueDetalhesView.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));

            AdminEstoqueDetalhesController controller = loader.getController();

            // Busca todos os lotes desse produto
            List<ProdutoEstoque> lotes = produtoEstoqueDAO.listarLotesPorProduto(produto.getProduto().getId());
            controller.setLotes(lotes);

            stage.setTitle("Detalhes do Estoque - " + produto.getProduto().getNome());
            stage.show();

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void voltar() {
        Stage stage = (Stage) tvResumoEstoque.getScene().getWindow();
        AppSplashController.trocarCena(stage, "/view/AdminHomeProdutoView.fxml", "Home produto Admin");
    }
}
