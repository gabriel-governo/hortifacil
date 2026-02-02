package com.hortifacil.dao;

import com.hortifacil.model.Produto;
import com.hortifacil.model.ProdutoEstoque;
import com.hortifacil.model.UnidadeMedida;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProdutoEstoqueDAOImpl implements ProdutoEstoqueDAO {

    private final Connection connection;


    public ProdutoEstoqueDAOImpl(Connection connection, ProdutoDAO produtoDAO) {
        this.connection = connection; 
    }

    @Override
    public int buscarUltimoLote(int idProduto) throws SQLException {
        String sql = "SELECT MAX(lote) as ultimo FROM produto_estoque WHERE id_produto = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idProduto);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int ultimo = rs.getInt("ultimo");
                if (rs.wasNull()) { // se MAX(lote) foi NULL
                    return 0;
                }
                return ultimo;
            }
        }
        return 0; // nenhum resultado
    }

@Override
public int adicionarLote(ProdutoEstoque lote) {
    String sql = "INSERT INTO produto_estoque (id_produto, quantidade, data_colheita, data_validade, lote) VALUES (?, ?, ?, ?, ?)";
    String sqlUpdateProduto = "UPDATE produto SET ultimo_lote = ? WHERE id_produto = ?";

    try {
        connection.setAutoCommit(false);

        int ultimoLote = buscarUltimoLote(lote.getProduto().getId());
        int novoLote = ultimoLote + 1;

        LocalDate hoje = LocalDate.now();

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, lote.getProduto().getId());
            stmt.setInt(2, lote.getQuantidade());
            stmt.setDate(3, Date.valueOf(lote.getDataColhido() != null ? lote.getDataColhido() : hoje));
            stmt.setDate(4, Date.valueOf(lote.getDataValidade() != null ? lote.getDataValidade() : hoje.plusDays(lote.getProduto().getDiasParaVencer())));
            stmt.setInt(5, novoLote);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Falha ao adicionar lote de estoque, nenhuma linha afetada.");
            }
        }

        try (PreparedStatement stmtUpdate = connection.prepareStatement(sqlUpdateProduto)) {
            stmtUpdate.setInt(1, novoLote);
            stmtUpdate.setInt(2, lote.getProduto().getId());
            stmtUpdate.executeUpdate();
        }

        connection.commit();
        return novoLote; 

    } catch (SQLException e) {
        e.printStackTrace();
        try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        return -1;
    } finally {
        try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
    }
}

  @Override
public List<ProdutoEstoque> listarLotesPorProduto(int produtoId) {
    List<ProdutoEstoque> lotes = new ArrayList<>();
    String sql = """
       SELECT pe.id, pe.quantidade, pe.data_colheita, pe.data_validade, pe.lote,
       p.id_produto, p.nome, p.preco_unitario, p.imagem_path, p.descricao,
       p.dias_para_vencer, p.desconto_inicio, p.desconto_diario,
       u.id_unidade, u.nome AS nome_unidade
        FROM produto_estoque pe
        JOIN produto p ON pe.id_produto = p.id_produto
        JOIN unidade_medida u ON p.id_unidade = u.id_unidade
        WHERE pe.id_produto = ?
        ORDER BY pe.data_colheita ASC
    """;

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, produtoId);
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                // Cria o produto usando o método auxiliar
                Produto produto = mapearProduto(rs);

                // Datas do estoque, usando default se NULL
                LocalDate dataColheita = (rs.getDate("data_colheita") != null)
                        ? rs.getDate("data_colheita").toLocalDate()
                        : LocalDate.now();
                LocalDate dataValidade = (rs.getDate("data_validade") != null)
                        ? rs.getDate("data_validade").toLocalDate()
                        : dataColheita.plusDays(produto.getDiasParaVencer());

                ProdutoEstoque lote = new ProdutoEstoque(
                rs.getInt("id"),
                produto,
                rs.getInt("quantidade"),
                dataColheita,
                dataValidade,
                rs.getInt("lote")   // <- pega o lote do banco
            );

                lotes.add(lote);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return lotes;
}

   public boolean atualizarQuantidadeLote(int loteId, int novaQuantidade) throws SQLException {
    String sql = "UPDATE produto_estoque SET quantidade = ? WHERE id = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, novaQuantidade);
        stmt.setInt(2, loteId);
        return stmt.executeUpdate() > 0;
    }
}


    @Override
    public boolean removerLote(int idLote) {
        String sql = "DELETE FROM produto_estoque WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idLote);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean salvar(ProdutoEstoque produtoEstoque) {
        return adicionarLote(produtoEstoque) != -1;
    }

  @Override
public List<ProdutoEstoque> listarTodos() {
    List<ProdutoEstoque> lotes = new ArrayList<>();
    String sql = """
        SELECT pe.id, pe.quantidade, pe.data_colheita, pe.data_validade, pe.lote,
        p.id_produto, p.nome, p.preco_unitario, p.imagem_path, p.descricao,
        p.dias_para_vencer, p.desconto_inicio, p.desconto_diario,
        u.id_unidade, u.nome AS nome_unidade
        FROM produto_estoque pe
        JOIN produto p ON pe.id_produto = p.id_produto
        JOIN unidade_medida u ON p.id_unidade = u.id_unidade
        ORDER BY pe.data_validade ASC
    """;

    try (PreparedStatement stmt = connection.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            // Usa o método auxiliar para criar o Produto completo
            Produto produto = mapearProduto(rs);

            // Datas do estoque, usando default se NULL
            LocalDate dataColheita = (rs.getDate("data_colheita") != null)
                    ? rs.getDate("data_colheita").toLocalDate()
                    : LocalDate.now();
            LocalDate dataValidade = (rs.getDate("data_validade") != null)
                    ? rs.getDate("data_validade").toLocalDate()
                    : dataColheita.plusDays(produto.getDiasParaVencer());

            ProdutoEstoque lote = new ProdutoEstoque(
                rs.getInt("id"),
                produto,
                rs.getInt("quantidade"),
                dataColheita,
                dataValidade,
                rs.getInt("lote")
            );


            lotes.add(lote);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return lotes;
}


   @Override
public List<ProdutoEstoque> buscarPorNomeProduto(String nomeProduto) throws SQLException {
    List<ProdutoEstoque> lotes = new ArrayList<>();
    String sql = """
            SELECT pe.id, pe.quantidade, pe.data_colheita, pe.data_validade, pe.lote,
                    p.id_produto, p.nome, p.preco_unitario, p.imagem_path, p.descricao,
                    p.dias_para_vencer, p.desconto_inicio, p.desconto_diario,
                    u.id_unidade, u.nome AS nome_unidade
            FROM produto_estoque pe
            JOIN produto p ON pe.id_produto = p.id_produto
            JOIN unidade_medida u ON p.id_unidade = u.id_unidade
            WHERE p.nome LIKE ?
            ORDER BY pe.data_validade ASC
            """;

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, "%" + nomeProduto + "%");
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Produto produto = mapearProduto(rs);

                LocalDate dataColheita = (rs.getDate("data_colheita") != null)
                        ? rs.getDate("data_colheita").toLocalDate()
                        : LocalDate.now();
                LocalDate dataValidade = (rs.getDate("data_validade") != null)
                        ? rs.getDate("data_validade").toLocalDate()
                        : dataColheita.plusDays(produto.getDiasParaVencer());
                ProdutoEstoque lote = new ProdutoEstoque(
                    rs.getInt("id"),
                    produto,
                    rs.getInt("quantidade"),
                    dataColheita,
                    dataValidade,
                    rs.getInt("lote") // <- agora o lote vem do banco
                );

                lotes.add(lote);
            }
        }
    }

    return lotes;
}


@Override
public ProdutoEstoque buscarPorProdutoData(int produtoId, LocalDate dataColhido, LocalDate dataValidade) throws SQLException {
    String sql = """
        SELECT pe.id, pe.quantidade, pe.data_colheita, pe.data_validade, pe.lote,
               p.id_produto, p.nome, p.preco_unitario, p.imagem_path, p.descricao,
               p.dias_para_vencer, p.desconto_inicio, p.desconto_diario,
               u.id_unidade, u.nome AS nome_unidade
        FROM produto_estoque pe
        JOIN produto p ON pe.id_produto = p.id_produto
        JOIN unidade_medida u ON p.id_unidade = u.id_unidade
        WHERE pe.id_produto = ? AND pe.data_colheita = ? AND pe.data_validade = ?
    """;

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, produtoId);
        stmt.setDate(2, java.sql.Date.valueOf(dataColhido));
        stmt.setDate(3, java.sql.Date.valueOf(dataValidade));

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                Produto produto = mapearProduto(rs);

                LocalDate dataColheitaBanco = rs.getDate("data_colheita").toLocalDate();
                LocalDate dataValidadeBanco = rs.getDate("data_validade").toLocalDate();

                return new ProdutoEstoque(
                    rs.getInt("id"),
                    produto,
                    rs.getInt("quantidade"),
                    dataColheitaBanco,
                    dataValidadeBanco,
                    rs.getInt("lote")
                );
            }
        }
    }

    return null;
}



public int getQuantidadeEstoquePorNome(String nomeProduto) throws SQLException {
    String sql = """
        SELECT SUM(pe.quantidade)
        FROM produto_estoque pe
        JOIN produto p ON pe.id_produto = p.id_produto
        WHERE p.nome = ?
          AND pe.quantidade > 0
          AND pe.data_validade >= CURDATE()
    """;

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, nomeProduto);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        }
    }
}

@Override
public List<ProdutoEstoque> buscarLotesPorProdutoOrdenados(int idProduto) throws SQLException {
    String sql = """
        SELECT pe.id, pe.quantidade, pe.data_colheita, pe.data_validade,
               p.id_produto, p.nome, p.preco_unitario, p.imagem_path, p.descricao,
               p.dias_para_vencer, p.desconto_inicio, p.desconto_diario,
               u.id_unidade, u.nome AS nome_unidade
        FROM produto_estoque pe
        JOIN produto p ON pe.id_produto = p.id_produto
        JOIN unidade_medida u ON p.id_unidade = u.id_unidade
        WHERE pe.id_produto = ?
        ORDER BY pe.data_colheita ASC
    """;

    List<ProdutoEstoque> lotes = new ArrayList<>();
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, idProduto);
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Produto produto = mapearProduto(rs);

                LocalDate dataColheita = (rs.getDate("data_colheita") != null)
                        ? rs.getDate("data_colheita").toLocalDate()
                        : LocalDate.now();
                LocalDate dataValidade = (rs.getDate("data_validade") != null)
                        ? rs.getDate("data_validade").toLocalDate()
                        : dataColheita.plusDays(produto.getDiasParaVencer());

                ProdutoEstoque lote = new ProdutoEstoque(
                    rs.getInt("id"),
                    produto,
                    rs.getInt("quantidade"),
                    dataColheita,
                    dataValidade
                );

                lotes.add(lote);
            }
        }
    }

    return lotes;
}

public int somarQuantidadePorProduto(int idProduto) throws SQLException {
    int total = 0;
    String sql = """
        SELECT SUM(quantidade)
        FROM produto_estoque
        WHERE id_produto = ?
          AND quantidade > 0
          AND data_validade >= CURDATE()
    """;

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, idProduto);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                total = rs.getInt(1);
            }
        }
    }

    return total;
}

private Produto mapearProduto(ResultSet rs) throws SQLException {
    UnidadeMedida unidade = new UnidadeMedida(
        rs.getInt("id_unidade"),
        rs.getString("nome_unidade")
    );
    Produto produto = new Produto(
        rs.getInt("id_produto"),
        rs.getString("nome"),
        rs.getDouble("preco_unitario"),
        rs.getString("imagem_path"),
        rs.getString("descricao"),
        unidade,
        rs.getInt("dias_para_vencer")
    );
    produto.setDescontoInicio(rs.getInt("desconto_inicio"));
    produto.setDescontoDiario(rs.getDouble("desconto_diario"));
    return produto;
}

@Override
public ProdutoEstoque buscarPorId(int idLote) throws SQLException {
    String sql = "SELECT pe.id, pe.quantidade, pe.data_colheita, pe.data_validade, pe.id_produto, " +
                 "p.nome, p.preco_unitario, p.imagem_path, p.descricao, p.dias_para_vencer, " +
                 "p.desconto_inicio, p.desconto_diario, u.id_unidade, u.nome AS nome_unidade " +
                 "FROM produto_estoque pe " +
                 "JOIN produto p ON pe.id_produto = p.id_produto " +
                 "JOIN unidade_medida u ON p.id_unidade = u.id_unidade " +
                 "WHERE pe.id = ?";

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, idLote);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                Produto produto = mapearProduto(rs); // seu método de mapear Produto
                LocalDate dataColheita = rs.getDate("data_colheita").toLocalDate();
                LocalDate dataValidade = rs.getDate("data_validade") != null ?
                                         rs.getDate("data_validade").toLocalDate() :
                                         dataColheita.plusDays(produto.getDiasParaVencer());
                return new ProdutoEstoque(
                        rs.getInt("id"),
                        produto,
                        rs.getInt("quantidade"),
                        dataColheita,
                        dataValidade,
                        0 // ou lote se você tiver
                );
            }
        }
    }
    return null;
}

@Override
public int buscarQuantidade(int idProduto) throws SQLException {
    String sql = "SELECT SUM(quantidade) AS total FROM produto_estoque WHERE id_produto = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, idProduto);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("total"); // retorna a soma das quantidades em todos os lotes
        }
    }
    return 0;
}

@Override
public boolean atualizarQuantidade(int idLote, int novaQuantidade) throws SQLException {
    String sql = "UPDATE produto_estoque SET quantidade = ? WHERE id = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, novaQuantidade);
        stmt.setInt(2, idLote); // agora atua no lote certo
        return stmt.executeUpdate() > 0;
    }
}

}