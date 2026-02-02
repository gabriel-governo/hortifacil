package com.hortifacil.controller;

import com.hortifacil.dao.RelatorioDAO;
import com.hortifacil.dao.RelatorioDAOImpl;
import com.hortifacil.model.ProdutoEstoque;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AdminRelatorioController {

    private RelatorioDAO relatorioDAO;

    @FXML private Label lblUsuarios;
    @FXML private Label lblAssinaturas;
    @FXML private Label lblLucro;
    @FXML private Button btnVoltar;

    // Tabela de produtos vencidos
    @FXML private TableView<ProdutoEstoque> tabelaProdutosVencidos;
    @FXML private TableColumn<ProdutoEstoque, Integer> colId;
    @FXML private TableColumn<ProdutoEstoque, String> colProduto;
    @FXML private TableColumn<ProdutoEstoque, Integer> colQuantidade;
    @FXML private TableColumn<ProdutoEstoque, String> colValidade;
    @FXML private TableColumn<ProdutoEstoque, String> colLote;
    @FXML private TableColumn<ProdutoEstoque, Void> colDescartar;

    // Tabela de produtos baixo estoque
    @FXML private TableView<ProdutoEstoque> tabelaBaixoEstoque;
    @FXML private TableColumn<ProdutoEstoque, Integer> colIdEstoque;
    @FXML private TableColumn<ProdutoEstoque, String> colProdutoEstoque;
    @FXML private TableColumn<ProdutoEstoque, Integer> colQuantidadeEstoque;
    @FXML private TableColumn<ProdutoEstoque, String> colValidadeEstoque;
    @FXML private TableColumn<ProdutoEstoque, String> colLoteEstoque;

    // Tabela de produtos próximos ao vencimento
    @FXML private TableView<ProdutoEstoque> tabelaProximosVencer;
    @FXML private TableColumn<ProdutoEstoque, Integer> colIdProximo;
    @FXML private TableColumn<ProdutoEstoque, String> colProdutoProximo;
    @FXML private TableColumn<ProdutoEstoque, Integer> colQuantidadeProximo;
    @FXML private TableColumn<ProdutoEstoque, String> colValidadeProximo;
    @FXML private TableColumn<ProdutoEstoque, String> colLoteProximo;

    // Gráfico de produtos mais vendidos
    @FXML private BarChart<String, Number> graficoMaisVendidos;

    // Gráfico de pedidos por status
    @FXML private BarChart<String, Number> graficoPedidosPorStatus;

    // Construtor padrão obrigatório pelo FXMLLoader
    public AdminRelatorioController() {}

    // Setter para injetar a conexão
    public void setConnection(Connection connection) {
        this.relatorioDAO = new RelatorioDAOImpl(connection);
        carregarResumo();
        carregarProdutosVencidos();
        carregarProdutosBaixoEstoque();
        carregarProdutosProximosVencimento();
        carregarProdutosMaisVendidos();
        carregarPedidosPorStatus();
    }

    @FXML
    public void atualizarRelatorio(ActionEvent event) {
        carregarResumo();
        carregarProdutosVencidos();
        carregarProdutosBaixoEstoque();
        carregarProdutosProximosVencimento();
        carregarProdutosMaisVendidos();
        carregarPedidosPorStatus();
    }

    private void carregarResumo() {
        if (relatorioDAO == null) return;
        try {
            lblUsuarios.setText(String.valueOf(relatorioDAO.contarUsuarios()));
            lblAssinaturas.setText(String.valueOf(relatorioDAO.contarAssinaturasAtivas()));
            double lucro = relatorioDAO.calcularLucroPorPeriodo(LocalDate.now().minusMonths(1), LocalDate.now());
            lblLucro.setText("R$ " + String.format("%.2f", lucro));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void carregarProdutosVencidos() {
        try {
            List<ProdutoEstoque> lista = relatorioDAO.listarProdutosVencidos();
            ObservableList<ProdutoEstoque> obs = FXCollections.observableArrayList(lista);

            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colProduto.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduto().getNome()));
            colQuantidade.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getQuantidade()));
            colValidade.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDataValidade().toString()));
            colLote.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getLote())));

            colDescartar.setCellFactory(param -> new TableCell<>() {
                private final Button btn = new Button("Descartar");
                {
                    btn.setOnAction(event -> {
                        ProdutoEstoque produto = getTableView().getItems().get(getIndex());
                        descartarProduto(produto);
                    });
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btn);
                }
            });

            tabelaProdutosVencidos.setItems(obs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void carregarProdutosBaixoEstoque() {
        try {
            List<ProdutoEstoque> lista = relatorioDAO.listarProdutosBaixoEstoque();
            ObservableList<ProdutoEstoque> obs = FXCollections.observableArrayList(lista);

            colIdEstoque.setCellValueFactory(new PropertyValueFactory<>("id"));
            colProdutoEstoque.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduto().getNome()));
            colQuantidadeEstoque.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getQuantidade()));
            colValidadeEstoque.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDataValidade().toString()));
            colLoteEstoque.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getLote())));

            tabelaBaixoEstoque.setItems(obs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void carregarProdutosProximosVencimento() {
        try {
            List<ProdutoEstoque> lista = relatorioDAO.listarProdutosProximosVencimento();
            ObservableList<ProdutoEstoque> obs = FXCollections.observableArrayList(lista);

            colIdProximo.setCellValueFactory(new PropertyValueFactory<>("id"));
            colProdutoProximo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduto().getNome()));
            colQuantidadeProximo.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getQuantidade()));
            colValidadeProximo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDataValidade().toString()));
            colLoteProximo.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getLote())));

            tabelaProximosVencer.setItems(obs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void carregarProdutosMaisVendidos() {
        if (relatorioDAO == null) return;
        try {
            Map<String, Integer> maisVendidos = relatorioDAO.produtosMaisVendidos();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Mais vendidos");

            for (Map.Entry<String, Integer> entry : maisVendidos.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            graficoMaisVendidos.getData().clear();
            graficoMaisVendidos.getData().add(series);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void carregarPedidosPorStatus() {
        try {
            Map<String, Integer> statusMap = relatorioDAO.pedidosPorStatus();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Pedidos por Status");

            for (Map.Entry<String, Integer> entry : statusMap.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            graficoPedidosPorStatus.getData().clear();
            graficoPedidosPorStatus.getData().add(series);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void descartarProduto(ProdutoEstoque produto) {
        if (produto == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText(null);
            alert.setContentText("Selecione um produto para descartar.");
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Descarte");
        confirm.setHeaderText("Deseja realmente descartar este lote?");
        confirm.setContentText("Lote: " + produto.getLote() +
                            "\nProduto: " + produto.getProduto().getNome());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                relatorioDAO.descartarProduto(produto.getId());
                tabelaProdutosVencidos.getItems().remove(produto);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Produto descartado");
                alert.setHeaderText(null);
                alert.setContentText("O lote " + produto.getLote() + " do produto "
                                    + produto.getProduto().getNome() + " foi descartado com sucesso.");
                alert.showAndWait();
            } catch (SQLException e) {
                e.printStackTrace();
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Erro");
                error.setHeaderText("Erro ao descartar produto");
                error.setContentText(e.getMessage());
                error.showAndWait();
            }
        }
    }

    @FXML
    private void voltarHomeAdmin() {
        Stage stage = (Stage) btnVoltar.getScene().getWindow();
        AppSplashController.trocarCena(stage, "/view/AdminHomeView.fxml", "Painel Administrativo");
    }
}
