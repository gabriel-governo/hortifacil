package com.hortifacil.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.hortifacil.dao.CartaoDAO;
import com.hortifacil.dao.CartaoDAOImpl;
import com.hortifacil.dao.EnderecoDAO;
import com.hortifacil.dao.UsuarioDAO;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.Cartao;
import com.hortifacil.model.Endereco;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;

public class ClienteSettingsController {

    // Boxes expansíveis
    @FXML private VBox loginBox;
    @FXML private VBox enderecoBox;
    @FXML private VBox senhaBox;

    // Campos Login
    @FXML private TextField txtNovoLogin;

    // Campos Endereço
    @FXML private TextField txtNovoEndereco;
    @FXML private TextField txtBairro;
    @FXML private TextField txtNumero;
    @FXML private TextField txtComplemento;

    // Campos Senha
    @FXML private PasswordField txtSenhaAtual;
    @FXML private PasswordField txtSenhaNova;
    @FXML private PasswordField txtSenhaConfirmar;

    // Botão Voltar
    @FXML private Button btnVoltar;

    // Dados do usuário
    private int usuarioId;
    private int clienteId;
    private String cpf;
    private String nomeUsuario;
    private String endereco;
    private String login;

    // cartões
    @FXML private VBox cartoesBox;
    @FXML private ListView<Cartao> listaCartoes;

    public void setDadosUsuario(int usuarioId, String cpf, String nomeUsuario, String login, int clienteId, String endereco) {
        this.usuarioId = usuarioId;
        this.cpf = cpf;
        this.nomeUsuario = nomeUsuario;
        this.login = login;
        this.clienteId = clienteId;
        this.endereco = endereco;
    }

    @FXML
    public void initialize() {
        carregarEnderecoAtual(); // Preenche os campos de endereço ao abrir a tela
    }

    // Toggle boxes
    @FXML private void toggleLogin() {
        boolean visivel = !loginBox.isVisible();
        loginBox.setVisible(visivel);
        loginBox.setManaged(visivel);
    }

    @FXML
    private void toggleEndereco() {
        boolean visivel = !enderecoBox.isVisible();
        enderecoBox.setVisible(visivel);
        enderecoBox.setManaged(visivel);

        if (visivel) { // só carrega quando a aba é aberta
            carregarEnderecoAtual();
        }
    }

    @FXML private void toggleSenha() {
        boolean visivel = !senhaBox.isVisible();
        senhaBox.setVisible(visivel);
        senhaBox.setManaged(visivel);
    }

    // Atualizar login
    @FXML
    private void confirmarLogin() {
        String novoLogin = txtNovoLogin.getText();

        if (novoLogin == null || novoLogin.trim().isEmpty()) {
            mostrarAlerta("Erro", "O login não pode estar vazio.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            UsuarioDAO usuarioDAO = new UsuarioDAO(conn);
            if (usuarioDAO.loginExiste(novoLogin)) {
                mostrarAlerta("Erro", "Esse login já está em uso!");
                return;
            }

            boolean atualizado = usuarioDAO.atualizarLogin(usuarioId, novoLogin);
            if (atualizado) {
                mostrarAlerta("Sucesso", "Login atualizado com sucesso!");
            } else {
                mostrarAlerta("Erro", "Não foi possível atualizar o login.");
            }

        } catch (SQLException e) {
            mostrarAlerta("Erro de banco", "Ocorreu um erro ao acessar o banco: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Carregar endereço atual do banco
    public void carregarEnderecoAtual() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            EnderecoDAO enderecoDAO = new EnderecoDAO(conn);
            Endereco endereco = enderecoDAO.buscarPorCliente(clienteId);
            if (endereco != null) {
                txtNovoEndereco.setText(endereco.getRua());
                txtNumero.setText(endereco.getNumero());
                txtComplemento.setText(endereco.getComplemento());
                txtBairro.setText(endereco.getBairro());
            }

        } catch (SQLException e) {
            mostrarAlerta("Erro de banco", "Falha ao carregar endereço: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Atualizar endereço
    @FXML
    private void confirmarEndereco() {
        String rua = txtNovoEndereco.getText().trim();
        String numero = txtNumero.getText().trim();
        String complemento = txtComplemento.getText().trim();
        String bairroNovo = txtBairro.getText().trim();

        if (rua.isBlank() || numero.isBlank() || bairroNovo.isBlank()) {
            mostrarAlerta("Erro", "Preencha todos os campos obrigatórios!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            EnderecoDAO enderecoDAO = new EnderecoDAO(conn);
            if (enderecoDAO.atualizarEndereco(clienteId, rua, numero, complemento, bairroNovo)) {
                mostrarAlerta("Sucesso", "Endereço atualizado!");
            } else {
                mostrarAlerta("Erro", "Não foi possível atualizar o endereço.");
            }

        } catch (SQLException e) {
            mostrarAlerta("Erro de banco", "Falha ao atualizar endereço: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Atualizar senha
    @FXML
    private void confirmarSenha() {
        String senhaAtual = txtSenhaAtual.getText();
        String senhaNova = txtSenhaNova.getText();
        String senhaConf = txtSenhaConfirmar.getText();

        if (senhaNova == null || senhaNova.trim().isEmpty()) {
            mostrarAlerta("Erro", "Digite uma nova senha!");
            return;
        }

        if (senhaNova.length() < 6) {
            mostrarAlerta("Erro", "A nova senha deve ter pelo menos 6 caracteres!");
            return;
        }

        if (!senhaNova.equals(senhaConf)) {
            mostrarAlerta("Erro", "As senhas novas não coincidem!");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            UsuarioDAO usuarioDAO = new UsuarioDAO(conn);
            if (!usuarioDAO.verificarSenha(usuarioId, senhaAtual)) {
                mostrarAlerta("Erro", "Senha atual incorreta!");
                return;
            }

            if (usuarioDAO.atualizarSenha(usuarioId, senhaNova)) {
                mostrarAlerta("Sucesso", "Senha alterada com sucesso!");
                txtSenhaAtual.clear();
                txtSenhaNova.clear();
                txtSenhaConfirmar.clear();
            } else {
                mostrarAlerta("Erro", "Não foi possível alterar a senha.");
            }

        } catch (SQLException e) {
            mostrarAlerta("Erro de banco", "Falha ao atualizar senha: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Alerta genérico
    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    // Voltar para home
    @FXML
    private void voltarHome() {
        Stage stage = (Stage) btnVoltar.getScene().getWindow();
        AppSplashController.trocarCenaComController(
            stage,
            "/view/ClienteHomeView.fxml",
            "Home Cliente",
            (ClienteHomeController controller) -> 
                controller.setDadosUsuario(
                    usuarioId,
                    cpf,
                    nomeUsuario,
                    login,
                    clienteId,
                    endereco
                )
        );
    }

    @FXML private void toggleCartoes() {
    boolean visivel = !cartoesBox.isVisible();
    cartoesBox.setVisible(visivel);
    cartoesBox.setManaged(visivel);
    if (visivel) {
        carregarCartoes();
    }
}

// Carrega cartões do cliente
private void carregarCartoes() {
    try (Connection conn = DatabaseConnection.getConnection()) {
        CartaoDAO cartaoDAO = new CartaoDAOImpl(conn);
        List<Cartao> cartoes = cartaoDAO.buscarPorCliente(clienteId);
        listaCartoes.getItems().setAll(cartoes);
    } catch (SQLException e) {
        mostrarAlerta("Erro de banco", "Falha ao carregar cartões: " + e.getMessage());
        e.printStackTrace();
    }
}

// Remover cartão selecionado
@FXML
private void removerCartaoSelecionado() {
    Cartao selecionado = listaCartoes.getSelectionModel().getSelectedItem();
    if (selecionado == null) {
        mostrarAlerta("Atenção", "Selecione um cartão para remover.");
        return;
    }

    try (Connection conn = DatabaseConnection.getConnection()) {
        CartaoDAO cartaoDAO = new CartaoDAOImpl(conn);
        boolean removido = cartaoDAO.remover(selecionado.getId());
        if (removido) {
            mostrarAlerta("Sucesso", "Cartão removido com sucesso!");
            carregarCartoes();
        } else {
            mostrarAlerta("Erro", "Não foi possível remover o cartão.");
        }
    } catch (SQLException e) {
        mostrarAlerta("Erro de banco", "Falha ao remover cartão: " + e.getMessage());
        e.printStackTrace();
    }
}

}