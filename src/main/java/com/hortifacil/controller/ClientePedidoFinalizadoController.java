package com.hortifacil.controller;

import com.hortifacil.model.Assinatura;
import com.hortifacil.model.CarrinhoProduto;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.stage.Stage;
import java.util.List;

public class ClientePedidoFinalizadoController {

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private VBox dadosPedidoContainer;

    @FXML
    private Label lblNumeroPedido;

    @FXML
    private Label lblDataPedido;

    @FXML
    private VBox produtosContainer; 

    @FXML
    private Label lblTotalPedido;

    @FXML
    private Label lblPlano;

    private int pedidoId;
    private List<CarrinhoProduto> itensPedido;
    private String dataPedido;
    private double totalPedido;
    private String cpf;
    private String nomeUsuario;
    private int clienteId;
    private String metodoPagamento;
    private String troco;
    private String endereco;
    private String login;
    private Assinatura assinaturaAtiva;

    public void setAssinaturaAtiva(Assinatura assinatura) {
        this.assinaturaAtiva = assinatura;

        lblPlano.setText("Plano: " + assinatura.getPlano());
    }

    // Define os dados do usuário
    public void setDadosUsuario(String cpf, String nome, String login, int clienteId, String endereco) {
        this.cpf = cpf;
        this.nomeUsuario = nome;
        this.login = login;
        this.clienteId = clienteId;
        this.endereco = endereco;
    }


    // Define os dados do pedido, incluindo endereço
    public void setDadosPedido(int pedidoId, String dataPedido, List<CarrinhoProduto> itensPedido,
                               double totalPedido, String metodoPagamento, String troco, String endereco) {
        this.pedidoId = pedidoId;
        this.itensPedido = itensPedido;
        this.dataPedido = dataPedido;
        this.totalPedido = totalPedido;
        this.metodoPagamento = metodoPagamento;
        this.troco = troco;
        this.endereco = endereco;

        mostrarLoadingComDelay();
    }

    private void mostrarLoadingComDelay() {
        progressIndicator.setVisible(true);
        dadosPedidoContainer.setVisible(false);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> mostrarDadosPedido());
        pause.play();
    }

private void mostrarDadosPedido() {
    System.out.println("Itens do pedido: " + (itensPedido == null ? "nulo" : itensPedido.size()));
    progressIndicator.setVisible(false);
    dadosPedidoContainer.setVisible(true);

    lblNumeroPedido.setText("Número do Pedido: " + pedidoId);
    lblDataPedido.setText("Data do Pedido: " + dataPedido);

    // Limpa antes de recriar
    produtosContainer.getChildren().clear();

    if (assinaturaAtiva != null) {
        // Exibe apenas a assinatura
        VBox card = new VBox();
        card.setSpacing(5);
        card.setStyle("-fx-padding: 10; -fx-background-color: #f4f4f4; -fx-background-radius: 10; -fx-border-color: #ccc; -fx-border-radius: 10;");

        Label plano = new Label("Assinatura Ativa: " + assinaturaAtiva.getPlano());
        Label modelo = new Label("Modelo: " + assinaturaAtiva.getModelo().getNome());
        Label valor = new Label(String.format("Valor: R$ %.2f", assinaturaAtiva.getModelo().getValor()));

        card.getChildren().addAll(plano, modelo, valor);
        produtosContainer.getChildren().add(card);
    } else if (itensPedido != null) {
        for (CarrinhoProduto item : itensPedido) {
            VBox card = new VBox();
            card.setSpacing(5);
            card.setStyle("-fx-padding: 10; -fx-background-color: #f4f4f4; -fx-background-radius: 10; -fx-border-color: #ccc; -fx-border-radius: 10;");

            Label nomeProduto = new Label("Produto: " + item.getProduto().getNome());
            Label quantidade = new Label("Quantidade: " + item.getQuantidade());
            Label precoUnitario = new Label(String.format("Preço Unitário: R$ %.2f", item.getPrecoUnitario()));
            Label subtotal = new Label(String.format("Subtotal: R$ %.2f", item.getPrecoUnitario() * item.getQuantidade()));

            card.getChildren().addAll(nomeProduto, quantidade, precoUnitario, subtotal);
            produtosContainer.getChildren().add(card);
        }
    }

    lblTotalPedido.setText(String.format("Total: R$ %.2f", totalPedido));

    // Endereço
    if (endereco != null && !endereco.isEmpty()) {
        Label lblEnderecoPedido = new Label("Endereço: " + endereco);
        lblEnderecoPedido.setStyle("-fx-font-weight: bold;");
        dadosPedidoContainer.getChildren().add(0, lblEnderecoPedido);
    }

    // Pagamento
    if (metodoPagamento != null && !metodoPagamento.isEmpty()) {
        Label lblPagamento = new Label("Pagamento: " + metodoPagamento);
        dadosPedidoContainer.getChildren().add(lblPagamento);

        if (metodoPagamento.equals("Dinheiro") && troco != null && !troco.isEmpty()) {
            Label lblTroco = new Label("Troco para: R$ " + troco);
            dadosPedidoContainer.getChildren().add(lblTroco);
        }
    }
}

@FXML
private void voltarALoja() {
    Stage stage = (Stage) dadosPedidoContainer.getScene().getWindow();
    AppSplashController.trocarCenaComController(stage, "/view/ClienteProdutoListarView.fxml", "Ver Produtos", 
        (ClienteProdutoListarController controller) -> {
            controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco);
        }
    );
}

@FXML
private void verMeusPedidos() {
    Stage stage = (Stage) dadosPedidoContainer.getScene().getWindow();
    AppSplashController.trocarCenaComController(stage, "/view/ClientePedidoListarView.fxml", "Meus Pedidos", 
        (ClientePedidoListarController controller) -> {
            controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco);
            controller.carregarPedidos();  // agora os dados devem chegar corretamente
        }
    );
}

}
