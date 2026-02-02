package com.hortifacil.dao;

import com.hortifacil.model.ModeloProduto;
import com.hortifacil.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ModeloProdutoDAO {

    public void adicionar(ModeloProduto mp) throws SQLException {
        String sql = "INSERT INTO modelo_produto (idModelo, idProduto, quantidade) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, mp.getIdModelo());
            stmt.setInt(2, mp.getIdProduto());
            stmt.setInt(3, mp.getQuantidade());
            stmt.executeUpdate();
        }
    }

    public List<ModeloProduto> listarPorModelo(int idModelo) throws SQLException {
        List<ModeloProduto> lista = new ArrayList<>();
        String sql = "SELECT * FROM modelo_produto WHERE idModelo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idModelo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ModeloProduto mp = new ModeloProduto();
                mp.setIdModeloProduto(rs.getInt("idModeloProduto"));
                mp.setIdModelo(rs.getInt("idModelo"));
                mp.setIdProduto(rs.getInt("idProduto"));
                mp.setQuantidade(rs.getInt("quantidade"));
                lista.add(mp);
            }
        }
        return lista;
    }
}
