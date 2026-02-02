package com.hortifacil.controller;

import com.hortifacil.model.Cliente;
import com.hortifacil.model.Usuario;
import com.hortifacil.service.ClienteService;
import com.hortifacil.service.UsuarioService;
import com.hortifacil.util.Enums;
import com.hortifacil.util.Enums.ResultadoLogin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private CheckBox showPasswordCheck;
    @FXML private Button loginButton;
    @FXML private Label messageLabel;
    @FXML private Button btnCadastro;

    @FXML
    public void initialize() {
        // Mostrar/ocultar senha
        passwordVisibleField.managedProperty().bind(showPasswordCheck.selectedProperty());
        passwordVisibleField.visibleProperty().bind(showPasswordCheck.selectedProperty());
        passwordField.managedProperty().bind(showPasswordCheck.selectedProperty().not());
        passwordField.visibleProperty().bind(showPasswordCheck.selectedProperty().not());
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());

        // Validação de entrada
        loginField.textProperty().addListener((obs, oldVal, newVal) -> validateInputs());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateInputs());
        passwordVisibleField.textProperty().addListener((obs, oldVal, newVal) -> validateInputs());

        // Tecla ENTER
        loginField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && !loginButton.isDisabled()) handleLogin();
        });
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && !loginButton.isDisabled()) handleLogin();
        });
        passwordVisibleField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && !loginButton.isDisabled()) handleLogin();
        });
    }

    private void validateInputs() {
        String senha = showPasswordCheck.isSelected() ? passwordVisibleField.getText() : passwordField.getText();
        boolean disable = loginField.getText().trim().isEmpty() || senha.trim().isEmpty();
        loginButton.setDisable(disable);
        if (!disable) clearMessage();
    }

    @FXML
public void handleLogin() {
    String login = loginField.getText().trim();
    String senha = showPasswordCheck.isSelected()
        ? passwordVisibleField.getText().trim()
        : passwordField.getText().trim();
    

    if (login.isEmpty() || senha.isEmpty()) {
        setMessageErro("Preencha usuário e senha.");
        return;
    }

    try {
        UsuarioService usuarioService = new UsuarioService();
        ResultadoLogin resultado = usuarioService.autenticar(login, senha);
        

        switch (resultado) {
            case SUCESSO -> {
                Usuario usuario = usuarioService.getUsuarioPorLogin(login);
                Stage stage = (Stage) loginButton.getScene().getWindow();

                if (usuario.getTipo() == Enums.TipoUsuario.ADMIN) {
                    AppSplashController.<AdminHomeController>trocarCenaComDados(
                        stage,
                        "/view/AdminHomeView.fxml",
                        "Admin",
                        controller -> {
                        
                        }
                    );

                } else if (usuario.getTipo() == Enums.TipoUsuario.CLIENTE) {
                    try {
                        Cliente cliente = usuarioService.getClientePorUsuarioId(usuario.getId());
                        String endereco = ClienteService.getInstance().buscarEnderecoPorId(cliente.getId());

                        int usuarioId = usuario.getId();
                        String cpf = cliente.getCpf();
                        String nomeUsuario = cliente.getNome();
                        String loginUsuario = usuario.getLogin();
                        int clienteId = cliente.getId();
                        String enderecoCliente = endereco;

                        AppSplashController.<ClienteHomeController>trocarCenaComDados(
                            stage,
                            "/view/ClienteHomeView.fxml",
                            "Cliente",
                            controller -> controller.setDadosUsuario(
                                usuarioId,
                                cpf,
                                nomeUsuario,
                                loginUsuario,
                                clienteId,
                                enderecoCliente
                            )
                        );

                    } catch (SQLException e) {
                        e.printStackTrace();
                        setMessageErro("Cliente não encontrado para este usuário.");
                    }
                }

                clearMessage();
            }

            case USUARIO_NAO_ENCONTRADO -> setMessageErro("Usuário não encontrado.");
            case SENHA_INVALIDA -> setMessageErro("Senha incorreta.");
            case USUARIO_INATIVO -> setMessageErro("Usuário inativo. Contate o administrador.");
            default -> setMessageErro("Erro desconhecido. Tente novamente.");
        }

    } catch (SQLException e) {
        e.printStackTrace();
        setMessageErro("Erro ao conectar com o banco de dados.");
    }
}

    @FXML
    private void exibirMensagemSenha(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Senha?");
        alert.setHeaderText(null);
        alert.setContentText("Eu também não sei sua senha!");
        alert.showAndWait();
}

    @FXML
    public void abrirCadastro() {
        try {
            URL url = getClass().getResource("/view/ClienteCadastroView.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Criar Conta");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setWidth(800);
            stage.setHeight(600);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.setMaxWidth(800);
            stage.setMaxHeight(600);

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erro", "Não foi possível abrir a tela de cadastro.", e.getMessage());
        }
    }

    private void setMessageErro(String mensagem) {
        messageLabel.setText(mensagem);
        messageLabel.setStyle("-fx-text-fill: red;");
    }

    private void clearMessage() {
        messageLabel.setText("");
    }

    private void showAlert(String titulo, String cabecalho, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(conteudo);
        alert.showAndWait();

}

}
