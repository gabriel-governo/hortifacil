package com.hortifacil.controller;

import com.hortifacil.dao.*;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.Produto;
import com.hortifacil.model.ProdutoEstoque;
import com.hortifacil.service.ClienteService;
import com.hortifacil.service.EstoqueService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.SQLException;
import java.util.List;

public class ClienteCarrinhoController {

    @FXML private ListView<CarrinhoProduto> lvCarrinho;
    @FXML private Button btnFinalizar;
    @FXML private Button btnRemoverSelecionado;
    @FXML private Label lblStatus;
    @FXML private Label lblTotal;
    @FXML private Button btnVoltar;
    @FXML private Button btnVerProdutos;
    @FXML private Button btnVerFoodToSave;
    @FXML private VBox vboxCarrinho;
    
    private ObservableList<CarrinhoProduto> carrinho = FXCollections.observableArrayList();
    private String cpf, nomeUsuario, endereco;
    private int clienteId;
    private String login;

    private EstoqueService estoqueService;

    private CarrinhoProdutoDAO carrinhoProdutoDAO;

    @FXML
    public void initialize() {
        // Fundo verde clarinho atrás dos cards
        vboxCarrinho.setStyle("-fx-background-color: #e3f2d8; -fx-padding: 15;");

        try {
            var conn = DatabaseConnection.getConnection();
            var produtoEstoqueDAO = new ProdutoEstoqueDAOImpl(conn, null); // null porque ainda não existe ProdutoDAO
            var produtoDAO = new ProdutoDAOImpl(conn);
            estoqueService = EstoqueService.getInstance(produtoDAO, produtoEstoqueDAO);
            carrinhoProdutoDAO = new CarrinhoProdutoDAOImpl(conn);
            btnVerProdutos.setOnAction(e -> verProdutos());
            btnVerFoodToSave.setOnAction(e -> verFoodToSave());
            btnFinalizar.setOnAction(e -> irParaPagamento());
            btnVoltar.setOnAction(e -> verProdutos());

        } catch (SQLException e) {
            e.printStackTrace();
            lblStatus.setText("Erro ao conectar ao banco: " + e.getMessage());
        }

        atualizarCarrinho();
    }

    private void atualizarCarrinho() {
        vboxCarrinho.getChildren().clear();
        for (CarrinhoProduto item : carrinho) {
            if (item != null && item.getProduto() != null) {
                vboxCarrinho.getChildren().add(criarCardProduto(item));
            }
        }
        atualizarTotal();
    }

    private void atualizarTotal() {
        double total = 0;
        for (CarrinhoProduto item : carrinho) {
            double valorUnitario;
            if (item.isFoodToSave() && item.getProduto() != null) {
                try {
                    valorUnitario = estoqueService.calcularPrecoFoodToSave(item.getProduto());
                } catch (SQLException e) {
                    e.printStackTrace();
                    valorUnitario = item.getPrecoUnitario(); // fallback caso haja erro
                }
            } else {
                valorUnitario = item.getPrecoUnitario();
            }
            total += valorUnitario * item.getQuantidade();
        }
        lblTotal.setText("Total: R$ " + String.format("%.2f", total));
    }

    private HBox criarCardProduto(CarrinhoProduto item) {
        HBox card = new HBox(15);
        card.setStyle(
            "-fx-padding: 15;" +
            "-fx-background-color: white;" +      
            "-fx-border-color: #ccc;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 6, 0, 2, 2);"  // sombra suave
        );

        // --- Imagem ---
        ImageView img = new ImageView();
        try {
            if (item.getProduto() != null && item.getProduto().getCaminhoImagem() != null && !item.getProduto().getCaminhoImagem().isEmpty()) {
                img.setImage(new Image(item.getProduto().getCaminhoImagem()));
            } else {
                img.setImage(new Image(getClass().getResource("/imagens/placeholder.png").toExternalForm()));
            }
        } catch (Exception e) {
            img.setImage(new Image(getClass().getResource("/imagens/placeholder.png").toExternalForm()));
        }
        img.setFitWidth(80);
        img.setFitHeight(80);
        img.setPreserveRatio(true);

        // --- Nome ---
        Label nome = new Label(item.getProduto() != null ? item.getProduto().getNome() : "Assinatura");
        nome.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // --- Preços ---
        Label precoOriginalLabel = new Label();
        Label precoComDescontoLabel = new Label();

        // --- Quantidade e subtotal ---
        Label qtd = new Label("Qtde: " + item.getQuantidade());
        Label subtotal = new Label();

        // --- Função para atualizar subtotal e preços ---
        Runnable atualizarSubtotal = () -> {
        double precoAtual = item.getPrecoUnitario(); // valor padrão

        if (item.isFoodToSave() && item.getProduto() != null) {
            try {
                ProdutoEstoque lote = estoqueService.buscarLoteParaFoodToSave(item.getProduto().getId());
                if (lote != null) {
                    precoAtual = estoqueService.calcularPrecoComDesconto(lote);

                    precoOriginalLabel.setText(String.format("De: R$ %.2f", item.getPrecoUnitario()));
                    precoOriginalLabel.setStyle("-fx-strikethrough: true; -fx-text-fill: gray; -fx-font-size: 12px;");

                    precoComDescontoLabel.setText(String.format("Por: R$ %.2f [Food to Save]", precoAtual));
                    precoComDescontoLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 12px;");
                } else {
                    precoOriginalLabel.setText("");
                    precoComDescontoLabel.setText(String.format("R$ %.2f", precoAtual));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                precoOriginalLabel.setText("");
                precoComDescontoLabel.setText(String.format("R$ %.2f", precoAtual));
            }
        } else {
            precoOriginalLabel.setText("");
            precoComDescontoLabel.setText(String.format("R$ %.2f", precoAtual));
        }

        subtotal.setText(String.format("Subtotal: R$ %.2f", item.getQuantidade() * precoAtual));
        atualizarTotal();
    };

        atualizarSubtotal.run(); // inicializa

        // --- Botões ---
        Button btnMais = new Button("+");
        Button btnMenos = new Button("-");
        Button btnRemover = new Button("Remover");

        btnMais.setOnAction(e -> {
            item.setQuantidade(item.getQuantidade() + 1);
            qtd.setText("Qtde: " + item.getQuantidade());
            atualizarSubtotal.run();
        });

        btnMenos.setOnAction(e -> {
            if (item.getQuantidade() > 1) {
                item.setQuantidade(item.getQuantidade() - 1);
                qtd.setText("Qtde: " + item.getQuantidade());
                atualizarSubtotal.run();
            }
        });
        
    btnRemover.setOnAction(e -> {
        try {
            // Devolve ao estoque antes de remover
            if (item.getProduto() != null) {
                int qtdAtual = estoqueService.buscarQuantidade(item.getProduto().getId());
                int novaQtd = qtdAtual + item.getQuantidade();
                estoqueService.atualizarQuantidade(item.getProduto().getId(), novaQtd);
            }

            // Remove do carrinho local e persistente
            carrinho.remove(item);
            carrinhoProdutoDAO.removerItem(clienteId, item.getProduto() != null ? item.getProduto().getId() : 0, item.isFoodToSave());
            atualizarCarrinho();
        } catch (SQLException ex) {
            ex.printStackTrace();
            lblStatus.setText("Erro ao remover item do carrinho.");
        }
    });

        HBox botoes = new HBox(5, btnMenos, btnMais, btnRemover);

        VBox info;
        if (!precoOriginalLabel.getText().isEmpty()) {
            info = new VBox(5, nome, precoOriginalLabel, precoComDescontoLabel, qtd, subtotal, botoes);
        } else {
            info = new VBox(5, nome, precoComDescontoLabel, qtd, subtotal, botoes);
        }

        card.getChildren().addAll(img, info);
        return card;
    }

        public void setDadosUsuario(String cpf, String nome, String login, int clienteId, String endereco) {
            this.cpf = cpf;
            this.nomeUsuario = nome;
            this.login = login;
            this.clienteId = clienteId;
            this.endereco = endereco;

        try {
            List<CarrinhoProduto> itensPersistidos = carrinhoProdutoDAO.listarPorCliente(clienteId);
            carrinho.setAll(itensPersistidos);
            atualizarCarrinho();
        } catch (SQLException e) {
            e.printStackTrace();
            lblStatus.setText("Erro ao carregar itens do carrinho.");
        }
    }

    public void adicionarAoCarrinho(String nomeProduto, int quantidade, boolean forcarNormal) {
        if (quantidade <= 0) return;

        try {
            Produto produto = estoqueService.buscarProdutoPorNome(nomeProduto);
            if (produto == null) {
                lblStatus.setText("Produto não encontrado no estoque.");
                return;
            }

            boolean isFoodToSave = false;
            double precoUnitario = produto.getPreco(); // preço normal por padrão

            if (!forcarNormal) {
                ProdutoEstoque lote = estoqueService.buscarLoteParaFoodToSave(produto.getId());
                if (lote != null) {
                    isFoodToSave = true;
                    precoUnitario = estoqueService.calcularPrecoComDesconto(lote); // preço com desconto
                }
            }

            // verifica se já existe no carrinho com mesmo produto e tipo (normal/foodToSave separados)
            for (CarrinhoProduto item : carrinho) {
                if (item.getProduto().getId() == produto.getId() && item.isFoodToSave() == isFoodToSave) {
                    int novaQtd = item.getQuantidade() + quantidade;
                    if (!estoqueService.verificarEstoque(nomeProduto, novaQtd)) {
                        lblStatus.setText("Quantidade total ultrapassa o estoque disponível.");
                        return;
                    }
                    item.setQuantidade(novaQtd);
                    lvCarrinho.refresh();
                    atualizarTotal();
                    carrinhoProdutoDAO.atualizarQuantidade(clienteId, item.getProduto().getId(), novaQtd, isFoodToSave);
                    lblStatus.setText("Quantidade atualizada no carrinho.");
                    return;
                }
            }

            // produto novo no carrinho
            if (!estoqueService.verificarEstoque(nomeProduto, quantidade)) {
                lblStatus.setText("Quantidade solicitada ultrapassa o estoque disponível.");
                return;
            }

            CarrinhoProduto novoItem = new CarrinhoProduto(clienteId, produto, quantidade, precoUnitario, isFoodToSave);

            carrinhoProdutoDAO.adicionarAoCarrinho(novoItem);
            carrinho.setAll(carrinhoProdutoDAO.listarPorCliente(clienteId));
            atualizarCarrinho();
            lblStatus.setText("Produto adicionado ao carrinho.");

        } catch (SQLException e) {
            e.printStackTrace();
            lblStatus.setText("Erro ao acessar o banco.");
        }
    }

    private double calcularTotal() {
        return carrinho.stream().mapToDouble(i -> i.getPrecoUnitario() * i.getQuantidade()).sum();
    }

    private void irParaPagamento() {
        if (carrinho.isEmpty()) {
            lblStatus.setText("Carrinho vazio.");
            return;
        }

        // Buscar endereço via serviço
        this.endereco = ClienteService.getInstance().buscarEnderecoPorId(clienteId);

        Stage stage = (Stage) btnFinalizar.getScene().getWindow();
        AppSplashController.trocarCenaComController(stage, "/view/ClientePagamentoView.fxml", "Pagamento", (ClientePagamentoController controller) -> {
            controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco);
            controller.setCarrinho(carrinho);
            controller.setResumoPedido(carrinho, calcularTotal(), endereco); // envia o endereço
        });
    }

        public void setCarrinho(List<CarrinhoProduto> itens) {
        this.carrinho.setAll(itens);
        atualizarTotal();
    } 

    @FXML
    private void verProdutos() {
        Stage stage = (Stage) btnVerProdutos.getScene().getWindow();
        AppSplashController.trocarCenaComController(stage, "/view/ClienteProdutoListarView.fxml", "Ver Produtos", (ClienteProdutoListarController controller) -> {
        controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco);
        });
    }

    @FXML
    private void verFoodToSave() {
        Stage stage = (Stage) btnVerFoodToSave.getScene().getWindow();
        AppSplashController.trocarCenaComController(stage, "/view/ClienteFoodToSaveView.fxml", "Food to Save", (ClienteFoodToSaveController controller) -> {
            controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco);
        });
    }

    public void carregarItensCarrinho() {
    vboxCarrinho.getChildren().clear();

    try {
        // 1. Busca todos os itens persistidos no banco
        List<CarrinhoProduto> itensPersistidos = carrinhoProdutoDAO.listarPorCliente(clienteId);
        carrinho.setAll(itensPersistidos); // atualiza o carrinho local
        for (CarrinhoProduto item : itensPersistidos) {
                vboxCarrinho.getChildren().add(criarCardProduto(item));
            }

    } catch (SQLException e) {
        e.printStackTrace();
        lblStatus.setText("Erro ao carregar itens do carrinho.");
    }

    atualizarTotal();
}

}
