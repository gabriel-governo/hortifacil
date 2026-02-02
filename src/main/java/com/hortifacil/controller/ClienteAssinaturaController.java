package com.hortifacil.controller;

import java.sql.SQLException;
import java.util.List;

import com.hortifacil.dao.AssinaturaDAO;
import com.hortifacil.dao.AssinaturaDAOImpl;
import com.hortifacil.model.Assinatura;
import com.hortifacil.model.AssinaturaModelo;
import com.hortifacil.model.PedidoAssinatura;
import com.hortifacil.service.AssinaturaService;
import com.hortifacil.dao.PedidoAssinaturaDAO;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClienteAssinaturaController {

    @FXML
    private VBox pedidosBox;

    @FXML
    private Button btnAlterar, btnCancelar, btnAgendarEntrega, btnVoltar;

    private int clienteId;
    private int usuarioId;
    private String cpf;
    private String nomeUsuario;
    private String login;
    private String endereco;
    private String nome;

    @FXML private Label lblNomeAssinatura;
    @FXML private Label lblFrequencia;
    @FXML private Label lblHorarioEntrega;
    @FXML private Label lblPlaceholderPedidos;

    private final PedidoAssinaturaDAO pedidoAssinaturaDAO = new PedidoAssinaturaDAO();
    private final AssinaturaService assinaturaService = new AssinaturaService();
    private AssinaturaDAO assinaturaDAO = new AssinaturaDAOImpl();
    private Assinatura assinaturaAtiva;

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
        carregarAssinaturaAtiva();
    }

    public void setDadosCliente(int clienteId, int usuarioId, String cpf, String nomeUsuario, String login, String endereco) {
        this.clienteId = clienteId;
        this.usuarioId = usuarioId;
        this.cpf = cpf;
        this.nomeUsuario = nomeUsuario;
        this.login = login;
        this.endereco = endereco;
        carregarAssinaturaAtiva();
    }

    private void carregarAssinaturaAtiva() {
        pedidosBox.getChildren().clear();

        assinaturaAtiva = assinaturaDAO.buscarAtivaPorCliente(clienteId);

        if (assinaturaAtiva == null) {
            lblNomeAssinatura.setText("Plano: Sem assinatura");
            lblFrequencia.setText("Frequência: -");
            lblHorarioEntrega.setText("Horário entrega: -");

            Label vazio = new Label("Você não possui nenhuma assinatura ativa.");
            vazio.setStyle("-fx-font-size: 16px; -fx-text-fill: #555;");
            pedidosBox.getChildren().add(vazio);
            desativarBotoes();
            return;
        }

        // Atualiza cabeçalho (labels do topo)
        AssinaturaModelo modelo = assinaturaAtiva.getModelo();
        lblNomeAssinatura.setText("Plano: " + modelo.getNome());
        lblFrequencia.setText("Frequência: " + frequenciaNumeroParaTexto(modelo.getFrequencia()));
        lblHorarioEntrega.setText("Horário entrega: " +
                (assinaturaAtiva.getHorarioEntrega() != null ? assinaturaAtiva.getHorarioEntrega() : "Não definido"));

        // Carrega pedidos no scroll
        try {
            List<PedidoAssinatura> pedidos = pedidoAssinaturaDAO.listarPorAssinatura(assinaturaAtiva.getIdAssinatura());

            if (pedidos.isEmpty()) {
                Label nenhumPedido = new Label("Nenhum pedido foi gerado por esta assinatura ainda.");
                nenhumPedido.setStyle("-fx-text-fill: #777;");
                pedidosBox.getChildren().add(nenhumPedido);
            } else {
                pedidos.sort((p1, p2) -> Integer.compare(p2.getId(), p1.getId())); // ordem decrescente
                for (PedidoAssinatura pedido : pedidos) {
                    pedidosBox.getChildren().add(criarCardPedido(pedido));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Não foi possível carregar os pedidos da assinatura.", Alert.AlertType.ERROR);
        }

        ativarBotoes();
    }

    @FXML
    private void cancelarAssinatura() {
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION);
        confirmar.setTitle("Cancelar Assinatura");
        confirmar.setHeaderText("Deseja realmente cancelar sua assinatura?");
        confirmar.setContentText("Essa ação não poderá ser desfeita.");

        confirmar.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                boolean sucesso = assinaturaDAO.cancelarAssinatura(assinaturaAtiva.getIdAssinatura());
                if (sucesso) {
                    mostrarAlerta("Sucesso", "Assinatura cancelada com sucesso!", Alert.AlertType.INFORMATION);
                    carregarAssinaturaAtiva();
                } else {
                    mostrarAlerta("Erro", "Erro ao cancelar a assinatura.", Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void agendarEntrega() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Não especificado",
            "Não especificado", "08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00", "18:00");

        dialog.setTitle("Agendar Entrega");
        dialog.setHeaderText("Escolha o horário de entrega");
        dialog.setContentText("Horário:");

        dialog.showAndWait().ifPresent(novoHorario -> {
            String horarioFinal = novoHorario.equals("Não especificado") ? null : novoHorario;
            boolean sucesso = assinaturaService.alterarHorarioEntrega(assinaturaAtiva.getIdAssinatura(), horarioFinal);
            if (sucesso) {
                assinaturaAtiva.setHorarioEntrega(horarioFinal);
                carregarAssinaturaAtiva();
            } else {
                mostrarAlerta("Erro", "Falha ao alterar o horário de entrega!", Alert.AlertType.ERROR);
            }
        });
    }

   @FXML
    private void abrirTelaAlterarPlano() {
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION);
        confirmar.setTitle("Alterar Plano");
        confirmar.setHeaderText("Tem certeza que deseja alterar seu plano?");
        confirmar.setContentText("A alteração substituirá o plano atual.");

        confirmar.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                try {
                    Assinatura assinaturaAtual = assinaturaDAO.buscarAtivaPorCliente(clienteId);
                    Stage stage = (Stage) pedidosBox.getScene().getWindow();
                    AppSplashController.trocarCenaComController(
                        stage,
                        "/view/ClienteAssinaturaNovaView.fxml",
                        "Alterar Plano",
                        (ClienteAssinaturaNovaController controller) -> {
                            controller.setClienteIdParaPlanos(clienteId, cpf, nomeUsuario, login, endereco);
                            if (assinaturaAtual != null) {
                                controller.setAssinaturaAtual(assinaturaAtual.getModelo());
                            }
                        }
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarAlerta("Erro", "Falha ao abrir tela de alteração de plano.", Alert.AlertType.ERROR);
                }
            }
        });
    }
    
    private VBox criarCardPedido(PedidoAssinatura pedido) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("""
            -fx-background-color: white;
            -fx-border-color: #ccc;
            -fx-border-radius: 10;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5,0,0,1);
        """);

        Label lblNumero = new Label("Pedido #" + pedido.getId());
        lblNumero.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Label lblData = new Label("Data: " + pedido.getDataEntrega());
        Label lblStatus = new Label("Status: " + pedido.getStatus());
        lblStatus.setStyle(switch (pedido.getStatus().name().toLowerCase()) {
            case "finalizado" -> "-fx-text-fill: green; -fx-font-weight: bold;";
            case "pendente" -> "-fx-text-fill: orange; -fx-font-weight: bold;";
            case "cancelado" -> "-fx-text-fill: red; -fx-font-weight: bold;";
            default -> "-fx-text-fill: #555;";
        });

        Button btnDetalhes = new Button("Detalhes");
        btnDetalhes.getStyleClass().add("button-green");
        btnDetalhes.setOnAction(e -> abrirDetalhesPedido(pedido));

        HBox bottom = new HBox(btnDetalhes);
        bottom.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(lblNumero, lblData, lblStatus, bottom);
        return card;
    }

    private void abrirDetalhesPedido(PedidoAssinatura pedido) {
        Stage stage = (Stage) pedidosBox.getScene().getWindow();
        AppSplashController.trocarCenaComController(
            stage,
            "/view/ClientePedidoAssinaturaDetalheView.fxml",
            "Detalhes do Pedido da Assinatura",
            (ClientePedidoAssinaturaDetalheController controller) -> {
                controller.setPedidoAssinatura(pedido);
                // 🔹 Passa os dados do cliente junto
                controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco, String.valueOf(usuarioId));
            }
        );
    }

    private void ativarBotoes() {
        btnAlterar.setDisable(false);
        btnCancelar.setDisable(false);
        btnAgendarEntrega.setDisable(false);
    }

    private void desativarBotoes() {
        btnAlterar.setDisable(true);
        btnCancelar.setDisable(true);
        btnAgendarEntrega.setDisable(true);
    }

    @FXML
    private void voltarHome() {
        Stage stage = (Stage) btnVoltar.getScene().getWindow();
        AppSplashController.trocarCenaComController(
            stage,
            "/view/ClienteHomeView.fxml",
            "Home",
            (ClienteHomeController controller) -> controller.setDadosUsuario(usuarioId, cpf, nomeUsuario, login, clienteId, endereco)
        );
    }

    // Converte número de semanas em texto amigável
    private String frequenciaNumeroParaTexto(int frequencia) {
        return switch (frequencia) {
            case 1 -> "Semanal";
            case 2 -> "Quinzenal";
            case 4 -> "Mensal";
            default -> "Regular";
        };
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
    
        @Override
        public String toString() {
            return nome; // ou o atributo que quiser exibir no ComboBox
        }

}
