package com.hortifacil.dao;

import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.AssinaturaModelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssinaturaModeloDAOImpl implements AssinaturaModeloDAO {

    @Override
    public List<AssinaturaModelo> listarTodos() {
        List<AssinaturaModelo> modelos = new ArrayList<>();
        String sql = "SELECT * FROM assinatura_modelo WHERE ativo = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                AssinaturaModelo modelo = new AssinaturaModelo();
                modelo.setIdModelo(rs.getInt("id_modelo"));
                modelo.setNome(rs.getString("nome"));
                modelo.setDescricao(rs.getString("descricao"));
                modelo.setValor(rs.getDouble("valor"));
                modelo.setFrequencia(rs.getInt("frequencia"));
                modelo.setAtivo(rs.getBoolean("ativo"));
                modelo.setQuantidadeProdutos(rs.getInt("quantidade_produtos")); // novo campo
                modelos.add(modelo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return modelos;
    }

    @Override
    public boolean criar(AssinaturaModelo modelo) {
        String sql = "INSERT INTO assinatura_modelo (nome, descricao, valor, frequencia, ativo, quantidade_produtos) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, modelo.getNome());
            stmt.setString(2, modelo.getDescricao());
            stmt.setDouble(3, modelo.getValor());
            stmt.setInt(4, modelo.getFrequencia());
            stmt.setBoolean(5, modelo.isAtivo());
            stmt.setInt(6, modelo.getQuantidadeProdutos()); // novo campo

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean atualizar(AssinaturaModelo modelo) {
        String sql = "UPDATE assinatura_modelo SET nome=?, descricao=?, valor=?, frequencia=?, ativo=?, quantidade_produtos=? WHERE id_modelo=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, modelo.getNome());
            stmt.setString(2, modelo.getDescricao());
            stmt.setDouble(3, modelo.getValor());
            stmt.setInt(4, modelo.getFrequencia());
            stmt.setBoolean(5, modelo.isAtivo());
            stmt.setInt(6, modelo.getQuantidadeProdutos()); // novo campo
            stmt.setInt(7, modelo.getIdModelo());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean excluir(int idModelo) {
        String sql = "UPDATE assinatura_modelo SET ativo = 0 WHERE id_modelo=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idModelo);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public AssinaturaModelo buscarPorId(int idModelo) {
        String sql = "SELECT * FROM assinatura_modelo WHERE id_modelo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idModelo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                AssinaturaModelo modelo = new AssinaturaModelo();
                modelo.setIdModelo(rs.getInt("id_modelo"));
                modelo.setNome(rs.getString("nome"));
                modelo.setDescricao(rs.getString("descricao"));
                modelo.setValor(rs.getDouble("valor"));
                modelo.setFrequencia(rs.getInt("frequencia"));
                modelo.setAtivo(rs.getBoolean("ativo"));
                modelo.setQuantidadeProdutos(rs.getInt("quantidade_produtos")); // novo campo
                return modelo;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}