package com.hortifacil.dao;

import com.hortifacil.model.Cliente;
import com.hortifacil.model.Usuario;
import com.hortifacil.util.Enums;
import com.hortifacil.model.Endereco;

import java.sql.*;

import org.mindrot.jbcrypt.BCrypt; // biblioteca para hash de senha

public class ClienteDAO {

    private Connection conn;

    public ClienteDAO(Connection conn) {
        this.conn = conn;
    }

    // Insere só o usuário e retorna o ID gerado
    public int inserirUsuario(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (Login, senha, tipo, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getLogin());
            String senhaHash = BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt());
            stmt.setString(2, senhaHash);
            stmt.setString(3, usuario.getTipo().name());
            stmt.setString(4, usuario.getStatus().name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao inserir usuário, nenhuma linha afetada.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Falha ao obter o ID do usuário inserido.");
                }
            }
        }
    }

    public int inserirCliente(Cliente cliente) throws SQLException {
    String sql = "INSERT INTO cliente (id_usuario, cpf, nome, email, telefone) VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setInt(1, cliente.getId()); // id_usuario
        stmt.setString(2, cliente.getCpf());
        stmt.setString(3, cliente.getNome());
        stmt.setString(4, cliente.getEmail());
        stmt.setString(5, cliente.getTelefone());

        int rows = stmt.executeUpdate();
        if (rows == 0) return -1;

        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1); // retorna id_cliente
            }
        }
        return -1;
    }
}

    public Cliente buscarPorCpf(String cpf) throws SQLException {
        String sql = "SELECT u.id_usuario, u.Login, u.senha, u.tipo, u.status, " +
                     "c.cpf, c.nome, c.email, c.telefone " +
                     "FROM usuario u JOIN cliente c ON u.id_usuario = c.id_usuario " +
                     "WHERE c.cpf = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return construirClienteCompleto(rs);
                }
            }
        }
        return null;

    }

public int inserirClienteCompleto(Cliente cliente) throws SQLException {
    String sqlUsuario = "INSERT INTO usuario (Login, senha, tipo, status) VALUES (?, ?, ?, ?)";
    String sqlCliente = "INSERT INTO cliente (id_usuario, nome, cpf, email, telefone) VALUES (?, ?, ?, ?, ?)";

    try {
        conn.setAutoCommit(false);

        // Inserir na tabela usuario
        try (PreparedStatement stmtUser = conn.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
            stmtUser.setString(1, cliente.getLogin());
            stmtUser.setString(2, BCrypt.hashpw(cliente.getSenha(), BCrypt.gensalt()));
            stmtUser.setString(3, cliente.getTipo().name());
            stmtUser.setString(4, cliente.getStatus().name());

            int rows = stmtUser.executeUpdate();
            if (rows == 0) {
                conn.rollback();
                return -1;
            }

            try (ResultSet rs = stmtUser.getGeneratedKeys()) {
                if (rs.next()) {
                    cliente.setIdUsuario(rs.getInt(1)); // ← armazena o ID do usuário separadamente
                } else {
                    conn.rollback();
                    return -1;
                }
            }
        }

        // Inserir na tabela cliente com o id_usuario obtido acima
        try (PreparedStatement stmtCliente = conn.prepareStatement(sqlCliente, Statement.RETURN_GENERATED_KEYS)) {
            stmtCliente.setInt(1, cliente.getIdUsuario());
            stmtCliente.setString(2, cliente.getNome());
            stmtCliente.setString(3, cliente.getCpf());
            stmtCliente.setString(4, cliente.getEmail());
            stmtCliente.setString(5, cliente.getTelefone());

            int rows = stmtCliente.executeUpdate();
            if (rows == 0) {
                conn.rollback();
                return -1;
            }

            try (ResultSet rs = stmtCliente.getGeneratedKeys()) {
                if (rs.next()) {
                    int idCliente = rs.getInt(1);
                    cliente.setIdCliente(idCliente); // ← armazena o ID do cliente corretamente
                    conn.commit();
                    return idCliente;
                }
            }
        }

        conn.rollback();
        return -1;

    } catch (SQLException e) {
        conn.rollback();
        throw e;
    } finally {
        conn.setAutoCommit(true);
    }
}

 private Cliente construirClienteCompleto(ResultSet rs) throws SQLException {
    Cliente cliente = new Cliente();
    cliente.setId(rs.getInt("id_usuario"));
    cliente.setLogin(rs.getString("Login"));
    cliente.setSenha(rs.getString("senha"));
    cliente.setTipo(Enums.TipoUsuario.valueOf(rs.getString("tipo").toUpperCase()));
    cliente.setStatus(Enums.StatusUsuario.valueOf(rs.getString("status").toUpperCase()));

    cliente.setCpf(rs.getString("cpf"));
    cliente.setNome(rs.getString("nome"));
    cliente.setEmail(rs.getString("email"));
    cliente.setTelefone(rs.getString("telefone"));

    return cliente;
}

public Cliente buscarPorIdCliente(int idCliente) {
    String sql = "SELECT nome, endereco FROM cliente WHERE id_cliente = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idCliente);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            Cliente cliente = new Cliente();
            cliente.setNome(rs.getString("nome"));
            return cliente;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
    
}

    public String buscarNomePorId(int idCliente) {
    String sql = "SELECT nome FROM cliente WHERE id_cliente = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idCliente);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getString("nome");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return "Desconhecido";
}


    public String buscarEnderecoPorId(int idCliente) {
    String sql = "SELECT rua, numero, bairro, complemento FROM endereco WHERE id_cliente = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idCliente);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return String.format(
                "%s, %s - %s %s",
                rs.getString("rua"),
                rs.getString("numero"),
                rs.getString("bairro"),
                rs.getString("complemento") != null ? "(" + rs.getString("complemento") + ")" : ""
            );
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return "Endereço não cadastrado";
}

public int obterIdClientePorIdUsuario(int idUsuario) throws SQLException {
    String sql = "SELECT id_cliente FROM cliente WHERE id_usuario = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idUsuario);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id_cliente");
            } else {
                throw new SQLException("Cliente não encontrado para id_usuario: " + idUsuario);
            }
        }
    }
}

public void atualizarCliente(Cliente cliente, Endereco endereco) throws SQLException {
    // Atualiza dados do cliente
    String sqlCliente = "UPDATE cliente SET nome = ?, email = ?, telefone = ? WHERE id_cliente = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sqlCliente)) {
        stmt.setString(1, cliente.getNome());
        stmt.setString(2, cliente.getEmail());
        stmt.setString(3, cliente.getTelefone());
        stmt.setInt(4, cliente.getIdCliente());
        stmt.executeUpdate();
    }

    // Atualiza dados do endereço
    String sqlEndereco = "UPDATE endereco SET rua = ?, numero = ?, bairro = ?, complemento = ? WHERE id_cliente = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sqlEndereco)) {
        stmt.setString(1, endereco.getRua());
        stmt.setString(2, endereco.getNumero());
        stmt.setString(3, endereco.getBairro());
        stmt.setString(4, endereco.getComplemento());
        stmt.setInt(5, cliente.getIdCliente());
        stmt.executeUpdate();
    }
}

public Endereco buscarEnderecoObjeto(int idCliente) {
    String sql = "SELECT rua, numero, bairro, complemento FROM endereco WHERE id_cliente = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idCliente);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            Endereco endereco = new Endereco();
            endereco.setRua(rs.getString("rua"));
            endereco.setNumero(rs.getString("numero"));
            endereco.setBairro(rs.getString("bairro"));
            endereco.setComplemento(rs.getString("complemento"));
            endereco.setClienteId(idCliente);
            return endereco;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

}
