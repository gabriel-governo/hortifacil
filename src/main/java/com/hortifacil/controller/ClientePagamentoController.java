package com.hortifacil.controller;

import com.hortifacil.dao.AssinaturaDAO;
import com.hortifacil.dao.AssinaturaDAOImpl;
import com.hortifacil.dao.CarrinhoProdutoDAO;
import com.hortifacil.dao.CarrinhoProdutoDAOImpl;
import com.hortifacil.dao.CartaoDAO;
import com.hortifacil.dao.CartaoDAOImpl;
import com.hortifacil.dao.PedidoAssinaturaDAO;
import com.hortifacil.dao.PedidoDAO;
import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.dao.ProdutoDAOImpl;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.Assinatura;
import com.hortifacil.model.AssinaturaModelo;
import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.Cartao;
import com.hortifacil.model.PedidoAssinatura;
import com.hortifacil.model.Produto;
import com.hortifacil.model.ProdutoEstoque;
import com.hortifacil.service.PedidoAssinaturaService;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;

public class ClientePagamentoController {

    @FXML private Label lblEndereco;
    @FXML private Label lblValorTotal;
    @FXML private Label lblStatus;
    @FXML private ComboBox<Cartao> comboCartoesCadastrados;

    @FXML private RadioButton rbNovoCartao;
    @FXML private VBox containerDinheiro, containerPix;

    @FXML private TextField txtNomeTitular, txtNumeroCartao, txtValidade, txtCvv;
    @FXML private TextField txtTrocoPara;
    @FXML private TextArea txtCodigoPix;
    @FXML private ImageView qrCodeView;
    @FXML private Button btnGerarPix, btnConfirmar, btnVoltar;
    @FXML private CheckBox cbPrecisaTroco;
    @FXML private CheckBox cbNovoCartao;

    @FXML private VBox containerCartaoExistente;
    @FXML private RadioButton rbCartaoNaEntrega;

    @FXML private RadioButton rbPix, rbCartao, rbDinheiro;
    @FXML private VBox containerCartao, vboxCartoesCadastrados, containerNovoCartao;

    @FXML private RadioButton rbCadastrarNovoCartao;

    private ToggleGroup grupoPagamento = new ToggleGroup();
    private ToggleGroup grupoCartoes = new ToggleGroup(); // Para os cartões existentes + novo

    private String origemTela;

    private String cpf;
    private String nomeUsuario;
    private int clienteId;
    private List<CarrinhoProduto> carrinho;
    private String endereco;
    private String login;
    private AssinaturaModelo assinaturaSelecionada;
    private CartaoDAO cartaoDAO;
    private List<Cartao> cartoesCadastrados = new ArrayList<>();
    private Connection conn;

    private CarrinhoProdutoDAO carrinhoProdutoDAO;

    public ClientePagamentoController() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            carrinhoProdutoDAO = new CarrinhoProdutoDAOImpl(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setDadosUsuario(String cpf, String nome, String login, int clienteId, String endereco) {
        this.cpf = cpf;
        this.nomeUsuario = nome;
        this.login = login;
        this.clienteId = clienteId;
        this.endereco = endereco;

        try {
            Connection conn = DatabaseConnection.getConnection();
            this.cartaoDAO = new CartaoDAOImpl(conn);
            carregarCartoesCadastrados();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

   public void setDadosUsuarioParaAssinatura(int clienteId, String cpf, String nome, String login, String endereco) {
        this.clienteId = clienteId;
        this.cpf = cpf;
        this.nomeUsuario = nome;
        this.login = login;
        this.endereco = endereco;

        try {
            if (this.cartaoDAO == null) {
                Connection conn = DatabaseConnection.getConnection();
                this.cartaoDAO = new CartaoDAOImpl(conn);
            }
            carregarCartoesCadastrados();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void setAssinatura(AssinaturaModelo assinatura) {
        this.assinaturaSelecionada = assinatura;

        if (assinatura != null) {
            lblValorTotal.setText("Valor total: R$ " + String.format("%.2f", assinatura.getValor()));

            rbPix.setDisable(true);
            rbDinheiro.setDisable(true);

            if (rbCartaoNaEntrega != null) {
                rbCartaoNaEntrega.setVisible(false);
                rbCartaoNaEntrega.setManaged(false);
            }

            containerCartao.setVisible(true);
            containerCartao.setManaged(true);

            carregarCartoesCadastrados();

            boolean temCartoesAtivos = grupoCartoes.getToggles().stream()
                    .anyMatch(toggle -> ((RadioButton) toggle).getUserData() instanceof Cartao);

            if (temCartoesAtivos) {
                grupoCartoes.getToggles().stream()
                        .filter(toggle -> ((RadioButton) toggle).getUserData() instanceof Cartao)
                        .findFirst()
                        .ifPresent(toggle -> ((RadioButton) toggle).setSelected(true));
            }

            containerNovoCartao.setVisible(false);
            containerNovoCartao.setManaged(false);

            if (carrinho == null || carrinho.isEmpty()) {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    ProdutoDAO produtoDAO = new ProdutoDAOImpl(conn);
                    PedidoAssinaturaService service = PedidoAssinaturaService.getInstance(produtoDAO);

                    // Agora gera lista de ProdutoEstoque
                    carrinho = service.gerarProdutosDaAssinatura(assinatura).stream()
                            .map(pe -> {
                                Produto p = pe.getProduto();
                                CarrinhoProduto cp = new CarrinhoProduto();
                                cp.setProduto(p);
                                cp.setQuantidade(1); // ou se o modelo tiver quantidade, use pe.getQuantidade()
                                cp.setPrecoUnitario(p.getPreco());
                                return cp;
                            })
                            .collect(Collectors.toList());

                } catch (SQLException e) {
                    e.printStackTrace();
                    setStatus("Erro ao gerar itens da assinatura.", false);
                }
            }

            btnConfirmar.setDisable(false);

        } else {
            rbPix.setDisable(false);
            rbDinheiro.setDisable(false);
            containerCartao.setVisible(false);
            containerCartao.setManaged(false);
        }
    }

    public void setCarrinho(List<CarrinhoProduto> carrinho) {
        this.carrinho = new ArrayList<>(carrinho);
    }

    public void setOrigemTela(String origem) {
        this.origemTela = origem;
    }

   @FXML
    public void initialize() {
        try {
            conn = DatabaseConnection.getConnection();
            cartaoDAO = new CartaoDAOImpl(conn);
            carrinhoProdutoDAO = new CarrinhoProdutoDAOImpl(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        rbPix.setToggleGroup(grupoPagamento);
        rbCartao.setToggleGroup(grupoPagamento);
        rbDinheiro.setToggleGroup(grupoPagamento);

        containerPix.setVisible(false);
        containerPix.setManaged(false);
        containerDinheiro.setVisible(false);
        containerDinheiro.setManaged(false);
        containerCartao.setVisible(false);
        containerCartao.setManaged(false);
        containerNovoCartao.setVisible(false);
        containerNovoCartao.setManaged(false);

        ToggleGroup grupoCartoes = new ToggleGroup();
        rbCartaoNaEntrega.setToggleGroup(grupoCartoes);
        rbCadastrarNovoCartao.setToggleGroup(grupoCartoes);

        rbCadastrarNovoCartao.setOnAction(e -> {
            boolean mostrar = rbCadastrarNovoCartao.isSelected();
            containerNovoCartao.setVisible(mostrar);
            containerNovoCartao.setManaged(mostrar);
        });

        rbCartaoNaEntrega.setOnAction(e -> {
            if (rbCartaoNaEntrega.isSelected()) {
                containerNovoCartao.setVisible(false);
                containerNovoCartao.setManaged(false);
            }
        });

        grupoPagamento.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boolean pagamentoCartao = newVal == rbCartao;
            boolean pagamentoPix = newVal == rbPix;
            boolean pagamentoDinheiro = newVal == rbDinheiro;

            containerPix.setVisible(pagamentoPix);
            containerPix.setManaged(pagamentoPix);

            containerDinheiro.setVisible(pagamentoDinheiro);
            containerDinheiro.setManaged(pagamentoDinheiro);

            containerCartao.setVisible(pagamentoCartao);
            containerCartao.setManaged(pagamentoCartao);

            if (pagamentoCartao) {

                boolean pedidosNormais = assinaturaSelecionada == null;
                rbCartaoNaEntrega.setVisible(pedidosNormais);
                rbCartaoNaEntrega.setManaged(pedidosNormais);
            }
        });
    }

    @FXML
private void atualizarPagamentoContainers() {
    if (assinaturaSelecionada != null) {
        rbPix.setDisable(true);
        rbDinheiro.setDisable(true);

        rbNovoCartao.setVisible(true);
        rbCartaoNaEntrega.setVisible(false);

        containerCartao.setVisible(true);
        containerPix.setVisible(false);
        containerDinheiro.setVisible(false);
    } else {
        containerPix.setVisible(rbPix.isSelected());
        containerDinheiro.setVisible(rbDinheiro.isSelected());
        containerCartao.setVisible(rbNovoCartao.isSelected() || rbCartaoNaEntrega.isSelected());

        rbCartaoNaEntrega.setVisible(true);
        rbNovoCartao.setVisible(true);
    }
    atualizarCartaoContainers();
}

@FXML
public void atualizarCartaoContainers(ActionEvent event) {
    atualizarCartaoContainers();
}

private void atualizarCartaoContainers() {
    boolean assinatura = assinaturaSelecionada != null;

    boolean novoCartaoSelecionado = rbNovoCartao.isSelected();
    boolean cartaoNaEntregaSelecionado = rbCartaoNaEntrega.isSelected();

    containerCartao.setVisible(novoCartaoSelecionado || cartaoNaEntregaSelecionado);

    if (!containerCartao.isVisible()) {
        containerNovoCartao.setVisible(false);
        containerNovoCartao.setManaged(false);
        rbCartaoNaEntrega.setVisible(false);
        rbCartaoNaEntrega.setManaged(false);
        return;
    }

    rbCartaoNaEntrega.setVisible(!assinatura);
    rbCartaoNaEntrega.setManaged(!assinatura);

    containerNovoCartao.setVisible(novoCartaoSelecionado || (!assinatura));
    containerNovoCartao.setManaged(novoCartaoSelecionado || (!assinatura));
}

   public void setResumoPedido(List<CarrinhoProduto> carrinho, double total, String endereco) {
        this.carrinho = new ArrayList<>(carrinho);
        this.endereco = endereco;
        lblEndereco.setText("Endereço: " + endereco);
        lblValorTotal.setText(String.format("R$ %.2f", total));
    }

    public void voltarTelaAnterior() {
        Stage stage = (Stage) btnVoltar.getScene().getWindow();

        if ("assinatura".equals(origemTela)) {
            AppSplashController.trocarCenaComController(
                stage,
                "/view/ClienteAssinaturaNovaView.fxml",
                "Assinaturas",
                (ClienteAssinaturaNovaController controller) -> {

                    controller.setClienteIdParaPlanos(clienteId, cpf, nomeUsuario, login, endereco);
                    

                    if (assinaturaSelecionada != null) {
                        controller.setAssinaturaAtual(assinaturaSelecionada);
                    }
                }
            );

        } else {
            AppSplashController.trocarCenaComController(
                stage,
                "/view/ClienteCarrinhoView.fxml",
                "Carrinho",
                (ClienteCarrinhoController controller) -> {
                    controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco);
                    controller.setCarrinho(carrinho);
                }
            );
        }
    }

    @FXML
    private void gerarPix(ActionEvent event) {
        String codigoPix = "00020126580014BR.GOV.BCB.PIX0114"
                + UUID.randomUUID().toString().substring(0, 8)
                + "5204000053039865802BR5920HortiFacil Comercio6009SAO PAULO62070503***6304";

        txtCodigoPix.setText(codigoPix);

        QRCodeWriter qrWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrWriter.encode(codigoPix, BarcodeFormat.QR_CODE, 200, 200);
            BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < 200; x++) {
                for (int y = 0; y < 200; y++) {
                    bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            Image qrImage = SwingFXUtils.toFXImage(bufferedImage, null);
            qrCodeView.setImage(qrImage);
            qrCodeView.setVisible(true);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        btnConfirmar.setDisable(false);
        new Alert(Alert.AlertType.INFORMATION, "Código PIX criado com sucesso!\n" + codigoPix).showAndWait();
    }

    @FXML
private void confirmarPagamento() {
    String metodoFinal = null;

    // Verificar se há assinatura selecionada
    boolean ehAssinatura = assinaturaSelecionada != null;

    // Seleção do método de pagamento
    if (ehAssinatura) {
        // Pagamento para assinatura
        boolean novoCartao = rbCadastrarNovoCartao.isSelected();
        Cartao cartaoSelecionado = grupoCartoes.getToggles().stream()
                .filter(toggle -> ((RadioButton) toggle).getUserData() instanceof Cartao && ((RadioButton) toggle).isSelected())
                .map(toggle -> (Cartao) toggle.getUserData())
                .findFirst().orElse(null);

        if (novoCartao) {
            if (!validarCartao()) return;
            cadastrarCartao();
            metodoFinal = "Cartão (novo cadastrado)";
        } else if (cartaoSelecionado != null) {
            metodoFinal = "Cartão cadastrado (" + cartaoSelecionado.getUltimos4() + ")";
        } else {
            setStatus("Selecione um cartão cadastrado ou cadastre um novo.", false);
            return;
        }

    } else {
        // Pagamento para pedido do carrinho
        if (rbPix.isSelected()) {
            metodoFinal = "Pix";
        } else if (rbDinheiro.isSelected()) {
            metodoFinal = "Dinheiro";
        } else if (rbCartao.isSelected()) {
            if (rbCartaoNaEntrega.isSelected()) {
                metodoFinal = "Cartão (na entrega)";
            } else if (rbCadastrarNovoCartao.isSelected()) {
                if (!validarCartao()) return;
                cadastrarCartao();
                metodoFinal = "Cartão (novo cadastrado)";
            } else {
                Cartao cartaoSelecionado = grupoCartoes.getToggles().stream()
                        .filter(toggle -> ((RadioButton) toggle).getUserData() instanceof Cartao && ((RadioButton) toggle).isSelected())
                        .map(toggle -> (Cartao) toggle.getUserData())
                        .findFirst().orElse(null);

                if (cartaoSelecionado != null) {
                    metodoFinal = "Cartão cadastrado (" + cartaoSelecionado.getUltimos4() + ")";
                } else {
                    setStatus("Selecione uma opção de pagamento com cartão.", false);
                    return;
                }
            }
        } else {
            setStatus("Selecione um método de pagamento.", false);
            return;
        }
    }

    processarPedido(metodoFinal, ehAssinatura);

}

    private boolean validarCartao() {
        if (txtNomeTitular.getText().isEmpty() ||
            txtNumeroCartao.getText().isEmpty() ||
            txtValidade.getText().isEmpty() ||
            txtCvv.getText().isEmpty()) {
            setStatus("Preencha todos os campos do cartão.", false);
            return false;
        }
        return true;
    }

private void cadastrarCartao() {
    try (Connection conn = DatabaseConnection.getConnection()) {
        Cartao cartao = new Cartao();
        cartao.setIdCliente(clienteId);
        cartao.setNomeTitular(txtNomeTitular.getText());
        cartao.setValidade(txtValidade.getText());

        String numeroCriptografado = criptografar(txtNumeroCartao.getText());
        String cvvCriptografado = criptografar(txtCvv.getText());

        cartao.setNumeroCriptografado(numeroCriptografado);
        cartao.setCvvCriptografado(cvvCriptografado);

        String ultimos4 = txtNumeroCartao.getText().length() >= 4
                ? txtNumeroCartao.getText().substring(txtNumeroCartao.getText().length() - 4)
                : txtNumeroCartao.getText();
        cartao.setUltimosDigitos(ultimos4);

        cartao.setBandeira(detectarBandeira(txtNumeroCartao.getText()));

        cartaoDAO.inserir(cartao);

        cartoesCadastrados.add(cartao);

    } catch (SQLException e) {
        e.printStackTrace();
        setStatus("Erro ao cadastrar cartão.", false);
    }
}

private String criptografar(String valor) {
    return java.util.Base64.getEncoder().encodeToString(valor.getBytes());
}


private void processarPedido(String metodoFinal, boolean ehAssinatura) {
    try (Connection conn = DatabaseConnection.getConnection()) {

        if (ehAssinatura) {
            // -------------------- PEDIDO DE ASSINATURA --------------------
            if (assinaturaSelecionada == null) {
                setStatus("Erro: nenhuma assinatura selecionada.", false);
                return;
            }

            // 1️⃣ Criar assinatura ativa
            Assinatura assinaturaAtiva = new Assinatura();
            assinaturaAtiva.setIdCliente(clienteId);
            assinaturaAtiva.setIdModelo(assinaturaSelecionada.getIdModelo());
            assinaturaAtiva.setModelo(assinaturaSelecionada);
            assinaturaAtiva.setDataInicio(LocalDate.now());
            assinaturaAtiva.setStatus(Assinatura.Status.ATIVA);
            assinaturaAtiva.setPlano("Mensal");

            AssinaturaDAO assinaturaDAO = new AssinaturaDAOImpl(conn);
            boolean criada = assinaturaDAO.criarAssinatura(assinaturaAtiva);
            if (!criada || assinaturaAtiva.getIdAssinatura() <= 0) {
                setStatus("Erro ao criar assinatura.", false);
                return;
            }

            // 2️⃣ Criar pedido da assinatura
            PedidoAssinatura pedido = new PedidoAssinatura();
            pedido.setIdAssinatura(assinaturaAtiva.getIdAssinatura());
            pedido.setDataEntrega(LocalDate.now());
            pedido.setStatus(PedidoAssinatura.Status.PENDENTE);
            pedido.setValorTotal(assinaturaSelecionada.getValor());

            PedidoAssinaturaDAO pedidoDAO = new PedidoAssinaturaDAO();
            int pedidoId = pedidoDAO.criarPedido(pedido);
            if (pedidoId <= 0) {
                setStatus("Erro ao criar pedido da assinatura.", false);
                return;
            }

            // 3️⃣ Gerar itens da assinatura
            List<CarrinhoProduto> itensDaAssinatura = new ArrayList<>();
            try {
                ProdutoDAO produtoDAO = new ProdutoDAOImpl(conn);
                PedidoAssinaturaService service = PedidoAssinaturaService.getInstance(produtoDAO);

                for (ProdutoEstoque pe : service.gerarProdutosDaAssinatura(assinaturaSelecionada)) {
                    Produto p = pe.getProduto();
                    if (p == null || p.getPreco() <= 0) continue;

                    CarrinhoProduto cp = new CarrinhoProduto();
                    cp.setProduto(p);
                    cp.setQuantidade(1); // ou quantidade definida pelo modelo
                    cp.setPrecoUnitario(p.getPreco());
                    itensDaAssinatura.add(cp);

                    // 3️⃣a️⃣ Inserir produto na tabela pedido_assinatura_produto
                    String sqlInsert = "INSERT INTO assinatura_produto "
                    + "(id_assinatura, id_produto, quantidade) "
                    + "VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                        stmt.setInt(1, assinaturaAtiva.getIdAssinatura());
                        stmt.setInt(2, p.getId());
                        stmt.setInt(3, cp.getQuantidade());
                        stmt.executeUpdate();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                setStatus("Erro ao gerar itens da assinatura: " + e.getMessage(), false);
                return;
            }

            // 4️⃣ Abrir tela finalizado
            abrirTelaPedidoFinalizado(pedidoId, assinaturaSelecionada.getValor(), metodoFinal,
                                      assinaturaAtiva, new ArrayList<>(itensDaAssinatura));

            // 5️⃣ Limpar carrinho no banco e na memória
            CarrinhoProdutoDAOImpl carrinhoDAO = new CarrinhoProdutoDAOImpl(conn);
            carrinhoDAO.limparCarrinhoDoCliente(clienteId);
            if (carrinho != null) carrinho.clear();

            setStatus("Assinatura ativada e pedido criado com sucesso!", true);

        } else {
            // -------------------- PEDIDO DO CARRINHO --------------------
            if (carrinho == null || carrinho.isEmpty()) {
                setStatus("Carrinho vazio.", false);
                return;
            }

            double total = carrinho.stream()
                .mapToDouble(item -> item.getPrecoUnitario() * item.getQuantidade())
                .sum();

            PedidoDAO pedidoDAO = new PedidoDAO(conn);
            int pedidoId = pedidoDAO.criarPedidoCarrinho(carrinho, clienteId, metodoFinal);
            if (pedidoId <= 0) {
                setStatus("Erro ao criar pedido do carrinho.", false);
                return;
            }

            abrirTelaPedidoFinalizado(pedidoId, total, metodoFinal, null, new ArrayList<>(carrinho));

            // Limpar carrinho no banco e na memória
            CarrinhoProdutoDAOImpl carrinhoDAO = new CarrinhoProdutoDAOImpl(conn);
            carrinhoDAO.limparCarrinhoDoCliente(clienteId);
            carrinho.clear();

            setStatus("Pedido do carrinho criado com sucesso!", true);
        }

    } catch (SQLException e) {
        e.printStackTrace();
        setStatus("Erro ao processar pedido: " + e.getMessage(), false);
    }
}

private void abrirTelaPedidoFinalizado(int pedidoId, double total, String metodoPagamento,
                                       Assinatura assinatura, List<CarrinhoProduto> itensCarrinho) {
    Stage stage = (Stage) btnConfirmar.getScene().getWindow();
    AppSplashController.trocarCenaComController(stage, "/view/ClientePedidoFinalizadoView.fxml", "Pedido Finalizado",
        (ClientePedidoFinalizadoController controller) -> {

            // Dados do usuário
            controller.setDadosUsuario(cpf, nomeUsuario, login, clienteId, endereco);

            // Dados do pedido
            controller.setDadosPedido(
                pedidoId,
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                itensCarrinho,    // Passa os produtos do carrinho, se houver
                total,
                metodoPagamento,
                "",               // Troco
                endereco
            );

            // Assinatura ativa
            if (assinatura != null) {
                controller.setAssinaturaAtiva(assinatura);
            }
        });
}

    private void setStatus(String mensagem, boolean sucesso) {
        lblStatus.setText(mensagem);
        lblStatus.setStyle(sucesso ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
    }

    private String detectarBandeira(String numeroCartao) {
        if (numeroCartao.startsWith("4")) return "Visa";
        if (numeroCartao.startsWith("5")) return "MasterCard";
        if (numeroCartao.startsWith("6")) return "Elo"; 
        return "Desconhecida";
    }

    private void carregarCartoesCadastrados() {
        if (vboxCartoesCadastrados == null) return;
        vboxCartoesCadastrados.getChildren().clear();

        try {
            List<Cartao> cartoes = cartaoDAO.buscarPorCliente(clienteId).stream()
                    .filter(Cartao::isAtivo)
                    .toList();

            if (grupoCartoes == null)
                grupoCartoes = new ToggleGroup();

            grupoCartoes.getToggles().clear();

            for (Cartao c : cartoes) {
                RadioButton rb = new RadioButton(c.getBandeira() + " (**** " + c.getUltimos4() + ")");
                rb.setUserData(c);
                rb.setToggleGroup(grupoCartoes);

                rb.setOnAction(e -> {
                    containerNovoCartao.setVisible(false);
                    containerNovoCartao.setManaged(false);
                });

                vboxCartoesCadastrados.getChildren().add(rb);
            }

            if (rbCartaoNaEntrega != null) {
                rbCartaoNaEntrega.setToggleGroup(grupoCartoes);
                rbCartaoNaEntrega.setVisible(assinaturaSelecionada == null);
                rbCartaoNaEntrega.setManaged(assinaturaSelecionada == null);
                rbCartaoNaEntrega.setOnAction(e -> {
                    containerNovoCartao.setVisible(false);
                    containerNovoCartao.setManaged(false);
                });
                vboxCartoesCadastrados.getChildren().add(rbCartaoNaEntrega);
            }
            rbCadastrarNovoCartao.setToggleGroup(grupoCartoes);
            rbCadastrarNovoCartao.setOnAction(e -> {
                boolean mostrar = rbCadastrarNovoCartao.isSelected();
                containerNovoCartao.setVisible(mostrar);
                containerNovoCartao.setManaged(mostrar);
            });
            vboxCartoesCadastrados.getChildren().add(rbCadastrarNovoCartao);

            grupoCartoes.getToggles().stream()
                    .filter(toggle -> toggle.getUserData() instanceof Cartao)
                    .findFirst()
                    .ifPresent(toggle -> ((RadioButton) toggle).setSelected(true));

        } catch (SQLException e) {
            e.printStackTrace();
            lblStatus.setText("Erro ao carregar cartões cadastrados.");
        }
    }

    @FXML
    private void mostrarContainerNovoCartao() {
            containerNovoCartao.setVisible(true);
            containerNovoCartao.setManaged(true);
            containerCartaoExistente.setVisible(false);
            containerCartaoExistente.setManaged(false);
    }

}
