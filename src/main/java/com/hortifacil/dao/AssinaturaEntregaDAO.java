package com.hortifacil.dao;

import com.hortifacil.model.AssinaturaEntrega;
import com.hortifacil.model.AssinaturaEntrega.Status;
import com.hortifacil.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssinaturaEntregaDAO {

    public void adicionar(AssinaturaEntrega entrega) throws SQLException {
        String sql = "INSERT INTO assinatura_entrega (idAssinatura, dataPrevista, dataRealizada, status, observacoes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, entrega.getIdAssinatura());
            stmt.setDate(2, Date.valueOf(entrega.getDataPrevista()));
            stmt.setDate(3, entrega.getDataRealizada() != null ? Date.valueOf(entrega.getDataRealizada()) : null);
            stmt.setString(4, entrega.getStatus().name());
            stmt.setString(5, entrega.getObservacoes());
            stmt.executeUpdate();
        }
    }

    public List<AssinaturaEntrega> listarPorAssinatura(int idAssinatura) throws SQLException {
        List<AssinaturaEntrega> entregas = new ArrayList<>();
        String sql = "SELECT * FROM assinatura_entrega WHERE idAssinatura = ? ORDER BY dataPrevista ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAssinatura);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                AssinaturaEntrega e = new AssinaturaEntrega();
                e.setIdEntrega(rs.getInt("idEntrega"));
                e.setIdAssinatura(rs.getInt("idAssinatura"));
                e.setDataPrevista(rs.getDate("dataPrevista").toLocalDate());
                e.setDataRealizada(rs.getDate("dataRealizada") != null ? rs.getDate("dataRealizada").toLocalDate() : null);
                e.setStatus(Status.valueOf(rs.getString("status")));
                e.setObservacoes(rs.getString("observacoes"));
                entregas.add(e);
            }
        }
        return entregas;
    }

    public void atualizarStatus(int idEntrega, Status novoStatus) throws SQLException {
        String sql = "UPDATE assinatura_entrega SET status = ? WHERE idEntrega = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novoStatus.name());
            stmt.setInt(2, idEntrega);
            stmt.executeUpdate();
        }
    }
}
