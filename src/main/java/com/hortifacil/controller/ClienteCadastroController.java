package com.hortifacil.controller;

import com.hortifacil.dao.ClienteDAO;
import com.hortifacil.dao.EnderecoDAO;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.Cliente;
import com.hortifacil.model.Endereco;

import com.hortifacil.util.Enums;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.application.Platform;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.sql.Connection;

public class ClienteCadastroController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmaSenhaField;
    @FXML private TextField nomeField;
    @FXML private TextField emailField;
    @FXML private TextField ruaField;
    @FXML private TextField numeroField;
    @FXML private TextField bairroField;
    @FXML private TextField complementoField;
    @FXML private TextField cpfField;
    @FXML private TextField telefoneField;

    @FXML private Label messageLabel;
    @FXML private Button cadastrarButton;

    @FXML
public void initialize() {
    messageLabel.setText("");
    cadastrarButton.setDisable(true);

    ChangeListener<String> listener = (obs, oldVal, newVal) -> validarFormulario();

    loginField.textProperty().addListener(listener);
    passwordField.textProperty().addListener(listener);
    confirmaSenhaField.textProperty().addListener(listener);
    nomeField.textProperty().addListener(listener);
    emailField.textProperty().addListener(listener);
    cpfField.textProperty().addListener(listener);
    ruaField.textProperty().addListener(listener);
    numeroField.textProperty().addListener(listener);
    bairroField.textProperty().addListener(listener);
    telefoneField.textProperty().addListener(listener);

    aplicarMascaraTelefone();

    confirmaSenhaField.textProperty().addListener((obs, oldV, newV) -> {
        if (!newV.equals(passwordField.getText())) {
            confirmaSenhaField.setStyle("-fx-border-color: red;");
        } else {
            confirmaSenhaField.setStyle(null);
        }
    });

        confirmaSenhaField.textProperty().addListener((obs, oldV, newV) -> {
            if (!newV.equals(passwordField.getText())) {
                confirmaSenhaField.setStyle("-fx-border-color: red;");
            } else {
                confirmaSenhaField.setStyle(null);
            }
        });

        passwordField.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() < 6) {
                passwordField.setStyle("-fx-border-color: red;");
                setMensagemErro("Senha muito curta (mín 6 caracteres).");
            } else {
                passwordField.setStyle(null);
                setMensagemErro("");
            }
        });

   cpfField.textProperty().addListener((obs, oldVal, newVal) -> {
    String digits = newVal.replaceAll("[^\\d]", "");
    if (digits.length() > 11) digits = digits.substring(0, 11);

    StringBuilder formatted = new StringBuilder();

    int len = digits.length();
    for (int i = 0; i < len; i++) {
        formatted.append(digits.charAt(i));
        if (i == 2 || i == 5) {
            if (i != len - 1) formatted.append('.');
        } else if (i == 8) {
            if (i != len - 1) formatted.append('-');
        }
    }

    String result = formatted.toString();
    if (!newVal.equals(result)) {
        Platform.runLater(() -> {
            cpfField.setText(result);
            cpfField.positionCaret(result.length());
        });
    }
});

        telefoneField.textProperty().addListener((obs, oldVal, newVal) -> {
    String digits = newVal.replaceAll("[^\\d]", "");

    if (digits.length() > 11)
        digits = digits.substring(0, 11);

    StringBuilder formatado = new StringBuilder();

    if (digits.length() >= 1) formatado.append("(");
    if (digits.length() >= 1) formatado.append(digits.substring(0, Math.min(2, digits.length())));
    if (digits.length() >= 3) formatado.append(") ");
    if (digits.length() >= 3) formatado.append(digits.substring(2, Math.min(7, digits.length())));
    if (digits.length() >= 8) formatado.append("-");
    if (digits.length() >= 8) formatado.append(digits.substring(7));

    String resultado = formatado.toString();

    if (!newVal.equals(resultado)) {
        Platform.runLater(() -> telefoneField.setText(resultado));
    }
});
    }

private void aplicarMascaraTelefone() {
    telefoneField.textProperty().addListener((obs, oldVal, newVal) -> {
        String digits = newVal.replaceAll("[^\\d]", "");

        if (digits.length() > 11)
            digits = digits.substring(0, 11);

        StringBuilder formatado = new StringBuilder();

        if (digits.length() >= 1) formatado.append("(");
        if (digits.length() >= 1) formatado.append(digits.substring(0, Math.min(2, digits.length())));
        if (digits.length() >= 3) formatado.append(") ");
        if (digits.length() >= 3) formatado.append(digits.substring(2, Math.min(7, digits.length())));
        if (digits.length() >= 8) formatado.append("-");
        if (digits.length() >= 8) formatado.append(digits.substring(7));

        if (!newVal.equals(formatado.toString())) {
            telefoneField.setText(formatado.toString());
        }
    });
}

    private void validarFormulario() {

        setMensagemErro("");

        boolean todosPreenchidos = !loginField.getText().trim().isEmpty()
                && !passwordField.getText().trim().isEmpty()
                && !confirmaSenhaField.getText().trim().isEmpty()
                && !nomeField.getText().trim().isEmpty()
                && !emailField.getText().trim().isEmpty()
                && !cpfField.getText().trim().isEmpty()
                && !ruaField.getText().trim().isEmpty()
                && !numeroField.getText().trim().isEmpty()
                && !bairroField.getText().trim().isEmpty();

        boolean senhasConferem = passwordField.getText().equals(confirmaSenhaField.getText());

        String cpfNumerico = cpfField.getText().replaceAll("[^\\d]", "");
        boolean cpfValido = cpfNumerico.length() == 11;


        cadastrarButton.setDisable(!(todosPreenchidos && senhasConferem && cpfValido));

        if (!cpfValido && !cpfField.getText().isEmpty()) {
            setMensagemErro("CPF deve conter 11 dígitos numéricos.");
            cpfField.setStyle("-fx-border-color: red;");
        } else {
            cpfField.setStyle(null);
        }

        if (!senhasConferem && !confirmaSenhaField.getText().isEmpty()) {
            setMensagemErro("As senhas não conferem.");
            confirmaSenhaField.setStyle("-fx-border-color: red;");
        } else if (senhasConferem) {
            confirmaSenhaField.setStyle(null);
        }

        String email = emailField.getText().trim();
        boolean emailValido = email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

        if (!emailValido && !email.isEmpty()) {
            setMensagemErro("E-mail inválido.");
            emailField.setStyle("-fx-border-color: red;");
            cadastrarButton.setDisable(true);
        } else {
            emailField.setStyle(null);
        }

    }

@FXML
private void handleCadastrar() {
    cadastrarButton.setDisable(true);
    try (Connection conn = DatabaseConnection.getConnection()) {
        Cliente cliente = new Cliente();
        cliente.setLogin(loginField.getText());
        cliente.setSenha(passwordField.getText());
        cliente.setTipo(Enums.TipoUsuario.CLIENTE);
        cliente.setStatus(Enums.StatusUsuario.ATIVO);

        cliente.setNome(nomeField.getText());
        cliente.setCpf(cpfField.getText().replaceAll("[^\\d]", ""));
        cliente.setEmail(emailField.getText());
        cliente.setTelefone(telefoneField.getText());

        ClienteDAO clienteDAO = new ClienteDAO(conn);
        int idCliente = clienteDAO.inserirClienteCompleto(cliente);

        if (idCliente > 0) {
            Endereco endereco = new Endereco();
            endereco.setRua(ruaField.getText());
            endereco.setNumero(numeroField.getText());
            endereco.setBairro(bairroField.getText());
            endereco.setComplemento(complementoField.getText());
            endereco.setClienteId(idCliente);

            EnderecoDAO enderecoDAO = new EnderecoDAO(conn);
            if (enderecoDAO.inserir(endereco)) {
                messageLabel.setText("Cadastro realizado com sucesso!");

                PauseTransition delay = new PauseTransition(Duration.seconds(3));
                delay.setOnFinished(event -> fecharJanela());
                delay.play();

            } else {
                messageLabel.setText("Cliente cadastrado, mas erro ao salvar o endereço.");
            }
        } else {
            messageLabel.setText("Erro ao cadastrar cliente.");
        }
    } catch (Exception e) {
        e.printStackTrace();
        messageLabel.setText("Erro no sistema: " + e.getMessage());
    } finally {
        cadastrarButton.setDisable(false);
    }
}

    private void setMensagemErro(String mensagem) {
    messageLabel.setText(mensagem);
    if (mensagem == null || mensagem.isEmpty()) {
        messageLabel.setStyle("");
    } else {
        messageLabel.setStyle("-fx-text-fill: red;");
    }
}

    private void fecharJanela() {
    Stage stage = (Stage) cadastrarButton.getScene().getWindow();
    stage.close();
}

    @FXML
private void handleCancelar() {
    fecharJanela();
    }

}
