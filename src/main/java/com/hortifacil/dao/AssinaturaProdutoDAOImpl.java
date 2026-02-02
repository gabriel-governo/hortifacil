package com.hortifacil.dao;

import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.AssinaturaProduto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssinaturaProdutoDAOImpl implements AssinaturaProdutoDAO {

    @Override
    public boolean criarAssinaturaProduto(AssinaturaProduto ap) {
        String sql = "INSERT INTO assinatura_produto (id_assinatura, id_produto, quantidade) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ap.getIdAssinatura());
            stmt.setInt(2, ap.getIdProduto());
            stmt.setInt(3, ap.getQuantidade());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<AssinaturaProduto> listarPorAssinatura(int idAssinatura) {
        List<AssinaturaProduto> lista = new ArrayList<>();
        String sql = "SELECT * FROM assinatura_produto WHERE id_assinatura = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idAssinatura);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                AssinaturaProduto ap = new AssinaturaProduto();
                ap.setId(rs.getInt("id"));
                ap.setIdAssinatura(rs.getInt("id_assinatura"));
                ap.setIdProduto(rs.getInt("id_produto"));
                ap.setQuantidade(rs.getInt("quantidade"));
                lista.add(ap);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    @Override
    public void removerPorAssinatura(int idAssinatura) {
        String sql = "DELETE FROM assinatura_produto WHERE id_assinatura = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idAssinatura);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
