package com.hortifacil.dao;

import com.hortifacil.model.Endereco;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EnderecoDAO {

    private Connection conn;

    public EnderecoDAO(Connection conn) {
        this.conn = conn;
    }

    public EnderecoDAO() {
        try {
            this.conn = com.hortifacil.database.DatabaseConnection.getConnection();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao conectar com o banco", e);
        }
    }   

    public boolean inserir(Endereco endereco) {
        String sql = "INSERT INTO endereco (rua, numero, bairro, complemento, id_cliente) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = this.conn.prepareStatement(sql)) {
            stmt.setString(1, endereco.getRua());
            stmt.setString(2, endereco.getNumero());
            stmt.setString(3, endereco.getBairro());
            stmt.setString(4, endereco.getComplemento());
            stmt.setInt(5, endereco.getClienteId());

            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir endereço: " + e.getMessage(), e);
        }
    }

    public boolean atualizarEndereco(int clienteId, String rua, String numero, String complemento, String bairro) {
        String sql = "UPDATE endereco SET rua = ?, numero = ?, complemento = ?, bairro = ? WHERE id_cliente = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rua);
            stmt.setString(2, numero);
            stmt.setString(3, complemento);
            stmt.setString(4, bairro);
            stmt.setInt(5, clienteId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

public Endereco buscarPorCliente(int clienteId) throws SQLException {
        String sql = "SELECT * FROM endereco WHERE id_cliente = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clienteId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Endereco endereco = new Endereco();
                endereco.setRua(rs.getString("rua"));
                endereco.setNumero(rs.getString("numero"));
                endereco.setComplemento(rs.getString("complemento"));
                endereco.setBairro(rs.getString("bairro"));
                return endereco;
            }
        }
        return null; // caso não exista
    }

}
