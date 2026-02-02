package com.hortifacil.dao;

import com.hortifacil.model.Produto;
import com.hortifacil.model.UnidadeMedida;
import com.hortifacil.database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAOImpl implements ProdutoDAO {

    private final Connection connection;

    public ProdutoDAOImpl(Connection connection) {
        this.connection = connection;
    }

   @Override
public int salvar(Produto produto) {
    String sql = "INSERT INTO produto (nome, preco_unitario, imagem_path, descricao, id_unidade, dias_para_vencer, desconto_inicio, desconto_diario) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, produto.getNome());
        stmt.setDouble(2, produto.getPreco());
        stmt.setString(3, produto.getCaminhoImagem());
        stmt.setString(4, produto.getDescricao());
        stmt.setInt(5, produto.getUnidade().getId());
        stmt.setInt(6, produto.getDiasParaVencer());
        stmt.setInt(7, produto.getDescontoInicio());
        stmt.setDouble(8, produto.getDescontoDiario());

        int rows = stmt.executeUpdate();
        if (rows == 0) return -1;

        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) return rs.getInt(1);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return -1;
}

   @Override
public Produto buscarPorId(int id) {
    String sql = "SELECT p.*, u.id_unidade AS id_unidade, u.nome AS unidade_nome " +
                 "FROM produto p JOIN unidade_medida u ON p.id_unidade = u.id_unidade " +
                 "WHERE p.id_produto = ?";

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, id);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                UnidadeMedida unidade = new UnidadeMedida(
                    rs.getInt("id_unidade"),
                    rs.getString("unidade_nome")
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

                // Agora sim, setamos os descontos
                produto.setDescontoInicio(rs.getInt("desconto_inicio"));
                produto.setDescontoDiario(rs.getDouble("desconto_diario"));

                return produto;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

@Override
public Produto buscarPorNome(String nome) {
    String sql = "SELECT p.*, u.id_unidade AS id_unidade, u.nome AS unidade_nome " +
                 "FROM produto p JOIN unidade_medida u ON p.id_unidade = u.id_unidade " +
                 "WHERE p.nome = ?";

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, nome);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                UnidadeMedida unidade = new UnidadeMedida(
                    rs.getInt("id_unidade"),
                    rs.getString("unidade_nome")
                );

                Produto produto = new Produto(
                    rs.getInt("id_produto"),
                    rs.getString("nome"),
                    rs.getDouble("preco_unitario"),
                    rs.getString("imagem_path"),
                    rs.getString("descricao"),
                    unidade,
                    rs.getInt("dias_para_vencer")  // <-- ADICIONADO
                );
                produto.setDescontoInicio(rs.getInt("desconto_inicio")); // <-- ADICIONADO
                produto.setDescontoDiario(rs.getDouble("desconto_diario")); // opcional

                return produto;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

@Override
public List<Produto> listarTodos() {
    List<Produto> produtos = new ArrayList<>();
    String sql = "SELECT p.*, u.id_unidade AS id_unidade, u.nome AS unidade_nome " +
                 "FROM produto p JOIN unidade_medida u ON p.id_unidade = u.id_unidade";

    try (PreparedStatement stmt = connection.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        
        while (rs.next()) {
            UnidadeMedida unidade = new UnidadeMedida(
                rs.getInt("id_unidade"),
                rs.getString("unidade_nome")
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

            produtos.add(produto);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return produtos;
}

@Override
public boolean atualizar(Produto produto) {
    String sql = "UPDATE produto SET nome = ?, preco_unitario = ?, imagem_path = ?, descricao = ?, id_unidade = ?, " +
                 "dias_para_vencer = ?, desconto_inicio = ?, desconto_diario = ? WHERE id_produto = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, produto.getNome());
        stmt.setDouble(2, produto.getPreco());
        stmt.setString(3, produto.getCaminhoImagem());
        stmt.setString(4, produto.getDescricao());
        stmt.setInt(5, produto.getUnidade().getId());
        stmt.setInt(6, produto.getDiasParaVencer());
        stmt.setInt(7, produto.getDescontoInicio());
        stmt.setDouble(8, produto.getDescontoDiario());
        stmt.setInt(9, produto.getId());
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}


@Override
public boolean remover(int id) {
    String sql = "DELETE FROM produto WHERE id_produto = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, id);
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

@Override
public List<Integer> listarEstrelasPorProduto(int idProduto) throws SQLException {
    List<Integer> estrelas = new ArrayList<>();
    String sql = "SELECT estrelas FROM avaliacao WHERE id_produto = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idProduto);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            estrelas.add(rs.getInt("estrelas"));
        }
    }
    return estrelas;
}

public ProdutoDAOImpl() {
    try {
        this.connection = DatabaseConnection.getConnection();
    } catch (SQLException e) {
        e.printStackTrace();
        throw new RuntimeException("Erro ao conectar ao banco", e);
    }
    
}


@Override
public int buscarQuantidade(int idProduto) {
    String sql = "SELECT quantidade FROM produto WHERE id_produto = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, idProduto);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("quantidade");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0;
}

@Override
public boolean atualizarQuantidade(int idProduto, int novaQtd) {
    String sql = "UPDATE produto SET quantidade = ? WHERE id_produto = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, novaQtd);
        stmt.setInt(2, idProduto);
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

@Override
public List<Produto> buscarProdutosDisponiveis() throws SQLException {
    String sql = "SELECT * FROM produto WHERE quantidade > 0"; // ou condição de estoque > 0
    List<Produto> produtos = new ArrayList<>();

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            Produto p = new Produto();
            p.setId(rs.getInt("id_produto"));
            p.setNome(rs.getString("nome"));
            p.setPreco(rs.getDouble("preco"));
            p.setUnidade(new UnidadeMedida(rs.getInt("id_unidade"), rs.getString("unidade")));
            // ... outros campos
            produtos.add(p);
        }
    }
    return produtos;
}

}
