package com.hortifacil.dao;

import com.hortifacil.model.FaturaAssinatura;
import com.hortifacil.model.FaturaAssinatura.Status;
import com.hortifacil.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FaturaAssinaturaDAO {

    public void criar(FaturaAssinatura fatura) throws SQLException {
        String sql = """
            INSERT INTO fatura_assinatura 
            (id_assinatura, id_cartao, data_emissao, data_vencimento, valor, status, forma_pagamento)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, fatura.getIdAssinatura());
            if (fatura.getIdCartao() > 0) {
                stmt.setInt(2, fatura.getIdCartao());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setDate(3, Date.valueOf(fatura.getDataEmissao()));
            stmt.setDate(4, Date.valueOf(fatura.getDataVencimento()));
            stmt.setDouble(5, fatura.getValor());
            stmt.setString(6, fatura.getStatus().name().toLowerCase()); // "pendente", "pago", "atrasado"
            stmt.setString(7, fatura.getFormaPagamento());
            stmt.executeUpdate();
        }
    }

    public List<FaturaAssinatura> listarPorAssinatura(int idAssinatura) throws SQLException {
        List<FaturaAssinatura> faturas = new ArrayList<>();

        String sql = "SELECT * FROM fatura_assinatura WHERE id_assinatura = ? ORDER BY data_vencimento DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idAssinatura);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FaturaAssinatura f = new FaturaAssinatura();
                f.setIdFatura(rs.getInt("id"));
                f.setIdAssinatura(rs.getInt("id_assinatura"));
                f.setValor(rs.getDouble("valor"));
                f.setFormaPagamento(rs.getString("forma_pagamento"));
                f.setDataEmissao(rs.getDate("data_emissao").toLocalDate());
                f.setDataVencimento(rs.getDate("data_vencimento").toLocalDate());
                f.setStatus(Status.valueOf(rs.getString("status").toUpperCase()));

                int idCartao = rs.getInt("id_cartao");
                if (!rs.wasNull()) {
                    f.setIdCartao(idCartao);
                }

                faturas.add(f);
            }
        }
        return faturas;
    }

    public void atualizarStatus(int idFatura, Status novoStatus) throws SQLException {
        String sql = "UPDATE fatura_assinatura SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novoStatus.name().toLowerCase());
            stmt.setInt(2, idFatura);
            stmt.executeUpdate();
        }
    }

    public FaturaAssinatura buscarUltimaFatura(int idAssinatura) throws SQLException {
        String sql = """
            SELECT * FROM fatura_assinatura 
            WHERE id_assinatura = ? 
            ORDER BY data_emissao DESC 
            LIMIT 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idAssinatura);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                FaturaAssinatura f = new FaturaAssinatura();
                f.setIdFatura(rs.getInt("id"));
                f.setIdAssinatura(rs.getInt("id_assinatura"));
                f.setValor(rs.getDouble("valor"));
                f.setFormaPagamento(rs.getString("forma_pagamento"));
                f.setDataEmissao(rs.getDate("data_emissao").toLocalDate());
                f.setDataVencimento(rs.getDate("data_vencimento").toLocalDate());
                f.setStatus(Status.valueOf(rs.getString("status").toUpperCase()));

                int idCartao = rs.getInt("id_cartao");
                if (!rs.wasNull()) {
                    f.setIdCartao(idCartao);
                }

                return f;
            }
        }
        return null;
    }
}
