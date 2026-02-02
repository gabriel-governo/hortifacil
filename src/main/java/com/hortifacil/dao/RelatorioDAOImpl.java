package com.hortifacil.dao;

import com.hortifacil.model.Produto;
import com.hortifacil.model.ProdutoEstoque;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.sql.Date;

public class RelatorioDAOImpl implements RelatorioDAO {
    private final Connection connection;

    public RelatorioDAOImpl(Connection connection) {
        this.connection = connection;
    }

@Override
public List<ProdutoEstoque> listarProdutosVencidos() throws SQLException {
    List<ProdutoEstoque> lista = new ArrayList<>();
    String sql = """
        SELECT pe.id, p.nome AS nomeProduto, pe.quantidade, pe.data_validade, pe.lote
        FROM produto_estoque pe
        JOIN produto p ON p.id_produto = pe.id_produto
        WHERE pe.data_validade < CURDATE()
          AND pe.quantidade > 0
    """;

    try (PreparedStatement stmt = connection.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            ProdutoEstoque pe = new ProdutoEstoque();
            pe.setId(rs.getInt("id"));
            pe.setQuantidade(rs.getInt("quantidade"));
            pe.setDataValidade(rs.getDate("data_validade").toLocalDate());
            pe.setLote(rs.getInt("lote"));

            Produto produto = new Produto();
            produto.setNome(rs.getString("nomeProduto"));
            pe.setProduto(produto);

            lista.add(pe);

            System.out.println("DAO - Produto Vencido: " + pe.getProduto().getNome()
                + ", Quantidade: " + pe.getQuantidade()
                + ", Validade: " + pe.getDataValidade()
                + ", Lote: " + pe.getLote());
        }
    }
    return lista;
}

    @Override
    public Map<String, Integer> produtosMaisVendidos() throws SQLException {
        String sql = """
            SELECT pr.nome, SUM(i.quantidade) as total
            FROM pedido_produto i
            JOIN produto pr ON pr.id_produto = i.id_produto
            GROUP BY pr.nome
            ORDER BY total DESC
        """;
        Map<String, Integer> map = new LinkedHashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("nome"), rs.getInt("total"));
            }
        }
        return map;
    }

    @Override
    public double calcularLucroPorPeriodo(LocalDate inicio, LocalDate fim) throws SQLException {
        String sql = "SELECT SUM(i.quantidade * i.preco_unitario) as lucro " +
                     "FROM pedido p " +
                     "JOIN pedido_produto i ON p.id_pedido = i.id_pedido " +
                     "WHERE p.status = 'FINALIZADO' " +
                     "AND DATE(p.data_pedido) BETWEEN ? AND ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(inicio));
            stmt.setDate(2, Date.valueOf(fim));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("lucro");
            }
        }
        return 0.0;
    }

    @Override
    public int contarUsuarios() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM usuario";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt("total");
        }
        return 0;
    }

    @Override
    public int contarAssinaturasAtivas() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM assinatura WHERE status = 'ATIVA'";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt("total");
        }
        return 0;
    }
    
    @Override
public List<ProdutoEstoque> listarProdutosBaixoEstoque() throws SQLException {
    String sql = """
        SELECT pe.id, pe.id_produto, pe.quantidade, pe.data_colheita, pe.data_validade, pe.lote,
               p.nome AS nomeProduto
        FROM produto_estoque pe
        INNER JOIN produto p ON pe.id_produto = p.id_produto
        WHERE pe.quantidade < 10
          AND pe.data_validade >= CURDATE()
        ORDER BY pe.quantidade ASC
    """;

    List<ProdutoEstoque> lista = new ArrayList<>();
    try (PreparedStatement stmt = connection.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            ProdutoEstoque pe = new ProdutoEstoque();
            pe.setId(rs.getInt("id"));
            pe.setIdProduto(rs.getInt("id_produto"));
            pe.setQuantidade(rs.getInt("quantidade"));
            pe.setDataColhido(rs.getDate("data_colheita").toLocalDate());
            pe.setDataValidade(rs.getDate("data_validade").toLocalDate());
            pe.setLote(rs.getInt("lote"));

            // Cria o objeto Produto
            Produto produto = new Produto();
            produto.setId(rs.getInt("id_produto"));
            produto.setNome(rs.getString("nomeProduto"));
            pe.setProduto(produto);

            lista.add(pe);
        }
    }
    return lista;
}

@Override
public List<ProdutoEstoque> listarProdutosProximosVencimento() throws SQLException {
    LocalDate hoje = LocalDate.now();
    LocalDate limite = hoje.plusDays(4);

    String sql = """
        SELECT pe.id, pe.id_produto, pe.quantidade, pe.data_colheita, pe.data_validade, pe.lote,
               p.nome AS nomeProduto
        FROM produto_estoque pe
        JOIN produto p ON pe.id_produto = p.id_produto
        WHERE pe.quantidade > 0
          AND pe.data_validade BETWEEN ? AND ?
        ORDER BY pe.data_validade ASC
    """;

    List<ProdutoEstoque> lista = new ArrayList<>();
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setDate(1, Date.valueOf(hoje));
        stmt.setDate(2, Date.valueOf(limite));

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ProdutoEstoque pe = new ProdutoEstoque();
                pe.setId(rs.getInt("id"));
                pe.setIdProduto(rs.getInt("id_produto"));
                pe.setQuantidade(rs.getInt("quantidade"));
                pe.setDataColhido(rs.getDate("data_colheita").toLocalDate());
                pe.setDataValidade(rs.getDate("data_validade").toLocalDate());
                pe.setLote(rs.getInt("lote"));

                // Cria o objeto Produto
                Produto produto = new Produto();
                produto.setId(rs.getInt("id_produto"));
                produto.setNome(rs.getString("nomeProduto"));
                pe.setProduto(produto);

                lista.add(pe);
            }
        }
    }
    return lista;
}

    @Override
    public double calcularTicketMedio(LocalDate inicio, LocalDate fim) throws SQLException {
        String sql = "SELECT AVG(total) as ticket_medio " +
                    "FROM (SELECT SUM(i.quantidade * i.preco_unitario) as total " +
                    "      FROM pedido p " +
                    "      JOIN pedido_produto i ON p.id_pedido = i.id_pedido " +
                    "      WHERE p.status = 'FINALIZADO' " +
                    "      AND DATE(p.data_pedido) BETWEEN ? AND ? " +
                    "      GROUP BY p.id_pedido) as sub";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(inicio));
            stmt.setDate(2, Date.valueOf(fim));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("ticket_medio");
            }
        }
        return 0.0;
    }

    @Override
    public int contarPedidosPorPeriodo(LocalDate inicio, LocalDate fim) throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM pedido " +
                    "WHERE DATE(data_pedido) BETWEEN ? AND ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(inicio));
            stmt.setDate(2, Date.valueOf(fim));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("total");
        }
        return 0;
    }

    @Override
    public Map<String, Integer> pedidosPorStatus() throws SQLException {
        String sql = "SELECT status, COUNT(*) as total FROM pedido GROUP BY status";
        Map<String, Integer> map = new LinkedHashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("status"), rs.getInt("total"));
            }
        }
        return map;
    }

    @Override
    public int contarAssinaturasInativas() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM assinatura WHERE status = 'INATIVA'";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt("total");
        }
        return 0;
    }

    @Override
    public Map<String, Integer> clientesMaisAtivos(int limite) throws SQLException {
        String sql = "SELECT u.login, COUNT(p.id_pedido) as total " +
                    "FROM pedido p " +
                    "JOIN usuario u ON u.id_usuario = p.id_cliente " +
                    "WHERE p.status = 'FINALIZADO' " +
                    "GROUP BY u.login " +
                    "ORDER BY total DESC " +
                    "LIMIT ?";
        Map<String, Integer> map = new LinkedHashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limite);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("login"), rs.getInt("total"));
            }
        }
        return map;
    }

    public void descartarProduto(int idProdutoEstoque) throws SQLException {
        String sql = "DELETE FROM produto_estoque WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idProdutoEstoque);
            stmt.executeUpdate();
        }
    }

}
