package com.hortifacil.dao;

import com.hortifacil.model.Cartao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartaoDAOImpl implements CartaoDAO {

    private Connection conn;

    public CartaoDAOImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
public List<Cartao> buscarPorCliente(int clienteId) throws SQLException {
    List<Cartao> cartoes = new ArrayList<>();
    String sql = "SELECT * FROM cartao WHERE id_cliente = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, clienteId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Cartao c = new Cartao();
            c.setId(rs.getInt("id"));
            c.setIdCliente(rs.getInt("id_cliente"));
            c.setNomeTitular(rs.getString("nome_titular"));
            c.setNumeroCriptografado(rs.getString("numero_criptografado"));
            c.setValidade(rs.getString("validade"));
            c.setCvvCriptografado(rs.getString("cvv_criptografado"));
            c.setBandeira(rs.getString("bandeira"));
            c.setUltimosDigitos(rs.getString("ultimos_digitos"));
            c.setAtivo(rs.getBoolean("ativo")); // 🔑 popula o campo ativo
            cartoes.add(c);
        }
    }
    return cartoes;
}

    @Override
    public void inserir(Cartao cartao) throws SQLException {
        String sql = "INSERT INTO cartao (id_cliente, nome_titular, numero_criptografado, validade, cvv_criptografado, bandeira, ultimos_digitos) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, cartao.getIdCliente());
            stmt.setString(2, cartao.getNomeTitular());
            stmt.setString(3, cartao.getNumeroCriptografado());
            stmt.setString(4, cartao.getValidade());
            stmt.setString(5, cartao.getCvvCriptografado());
            stmt.setString(6, cartao.getBandeira());
            stmt.setString(7, cartao.getUltimosDigitos());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                cartao.setId(keys.getInt(1));
            }
        }
    }

@Override
public boolean remover(int id) throws SQLException {
    String sql = "UPDATE cartao SET numero_criptografado = 'REMOVIDO', "
               + "cvv_criptografado = 'REMOVIDO', "
               + "nome_titular = 'REMOVIDO', "
               + "ultimos_digitos = 'XXXX', "
               + "bandeira = 'REMOVIDO', "
               + "ativo = 0 "
               + "WHERE id = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, id);
        return stmt.executeUpdate() > 0;
    }
}

}
