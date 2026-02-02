package com.hortifacil.dao;

import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.PedidoAssinatura;
import com.hortifacil.model.ProdutoEstoque;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PedidoAssinaturaDAO {

    // Criar pedido de assinatura
public int criarPedido(PedidoAssinatura pedido) throws SQLException {
    try (Connection conn = DatabaseConnection.getConnection()) {
        String sqlPedido = "INSERT INTO pedido_assinatura (id_assinatura, data_entrega, status, valor_total) VALUES (?, ?, ?, ?)";
        int idPedido;

        try (PreparedStatement stmt = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, pedido.getIdAssinatura());
            stmt.setDate(2, Date.valueOf(pedido.getDataEntrega()));
            stmt.setString(3, pedido.getStatus().name());
            stmt.setDouble(4, pedido.getValorTotal());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                idPedido = rs.getInt(1);
            } else {
                return -1;
            }
        }

        // ✅ Não precisamos inserir itens, pois eles já estão em assinatura_produto
        return idPedido;
    } catch (SQLException e) {
        e.printStackTrace();
        throw e;
    }
}

    public int criar(PedidoAssinatura pedido) throws SQLException {
        String sql = "INSERT INTO pedido_assinatura (id_assinatura, data_entrega, status, valor_total) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, pedido.getIdAssinatura());
            stmt.setDate(2, Date.valueOf(pedido.getDataEntrega()));
            stmt.setString(3, pedido.getStatus().name());
            stmt.setDouble(4, pedido.getValorTotal());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public List<PedidoAssinatura> listarPorCliente(int idCliente) throws SQLException {
        String sql = """
            SELECT pa.*
            FROM pedido_assinatura pa
            INNER JOIN assinatura a ON pa.id_assinatura = a.id
            WHERE a.id_cliente = ?
            ORDER BY pa.data_entrega DESC
        """;
        List<PedidoAssinatura> pedidos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCliente);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pedidos.add(mapResultSetToPedido(rs));
            }
        }
        return pedidos;
    }

    public boolean atualizarStatus(int idPedido, String novoStatus) throws SQLException {
        String sql = "UPDATE pedido_assinatura SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novoStatus);
            stmt.setInt(2, idPedido);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean atualizarStatus(int idPedido, PedidoAssinatura.Status status) throws SQLException {
        return atualizarStatus(idPedido, status.name());
    }

    // Listar todos os pedidos de uma assinatura
    public List<PedidoAssinatura> listarPorAssinatura(int idAssinatura) throws SQLException {
        List<PedidoAssinatura> pedidos = new ArrayList<>();
        String sql = "SELECT * FROM pedido_assinatura WHERE id_assinatura = ? ORDER BY data_entrega DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAssinatura);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PedidoAssinatura pedido = mapResultSetToPedido(rs);
                pedidos.add(pedido);
            }
        }
        return pedidos;
    }

    // Buscar pedido por ID
    public PedidoAssinatura buscarPorId(int idPedido) throws SQLException {
        String sql = "SELECT * FROM pedido_assinatura WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPedido);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPedido(rs);
            }
        }
        return null;
    }

    // Excluir pedido (opcional)
    public boolean excluir(int idPedido) throws SQLException {
        String sql = "DELETE FROM pedido_assinatura WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPedido);
            return stmt.executeUpdate() > 0;
        }
    }

    // Mapeamento auxiliar
    private PedidoAssinatura mapResultSetToPedido(ResultSet rs) throws SQLException {
        PedidoAssinatura p = new PedidoAssinatura();
        p.setId(rs.getInt("id"));
        p.setIdAssinatura(rs.getInt("id_assinatura"));
        p.setDataEntrega(rs.getDate("data_entrega").toLocalDate());
        p.setStatus(PedidoAssinatura.Status.valueOf(rs.getString("status").toUpperCase()));
        p.setValorTotal(rs.getDouble("valor_total"));
        return p;
    }

    public void atualizarValorTotal(int idPedido, double valorTotal) throws SQLException {
        String sql = "UPDATE pedido_assinatura SET valor_total = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, valorTotal);
            stmt.setInt(2, idPedido);
            stmt.executeUpdate();
        }
    }

       public List<PedidoAssinatura> listarTodos() throws SQLException {
        List<PedidoAssinatura> pedidos = new ArrayList<>();
        String sql = "SELECT * FROM pedido_assinatura ORDER BY data_entrega DESC, id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PedidoAssinatura pedido = new PedidoAssinatura();
                pedido.setId(rs.getInt("id"));
                pedido.setIdAssinatura(rs.getInt("id_assinatura"));
                pedido.setDataEntrega(rs.getDate("data_entrega").toLocalDate());
                pedido.setStatus(PedidoAssinatura.Status.valueOf(rs.getString("status").toUpperCase()));
                pedido.setValorTotal(rs.getDouble("valor_total"));

                pedidos.add(pedido);
            }
        }

        return pedidos;
    }
    
    public void salvarProdutosDaAssinatura(int idAssinatura, List<ProdutoEstoque> produtos) throws SQLException {
    String sql = "INSERT INTO assinatura_produto (id_assinatura, id_produto, quantidade, unidade) VALUES (?, ?, ?, ?)";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        for (ProdutoEstoque p : produtos) {
            stmt.setInt(1, idAssinatura);
            stmt.setInt(2, p.getProduto().getId());
            stmt.setDouble(3, p.getQuantidade());
            stmt.setString(4, p.getProduto().getUnidade().getNome());
            stmt.addBatch();
        }
        stmt.executeBatch();
    }
}

}
