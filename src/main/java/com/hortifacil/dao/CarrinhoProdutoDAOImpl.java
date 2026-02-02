package com.hortifacil.dao;

import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.Produto;
import com.hortifacil.model.UnidadeMedida;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarrinhoProdutoDAOImpl implements CarrinhoProdutoDAO {

    private final Connection connection;

    public CarrinhoProdutoDAOImpl(Connection connection) {
        this.connection = connection;
    }

    public int obterClienteIdPorUsuarioId(int usuarioId) throws SQLException {
        String sql = "SELECT id FROM cliente WHERE id_usuario = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("Cliente não encontrado para usuário: " + usuarioId);
            }
        }
    }

    public boolean clienteExiste(int clienteId) throws SQLException {
        String sql = "SELECT 1 FROM cliente WHERE id_cliente = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clienteId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    @Override
    public void adicionarAoCarrinho(CarrinhoProduto item) throws SQLException {
        int idCarrinho = obterOuCriarCarrinhoAberto(item.getClienteId());

        String verificaProduto = "SELECT quantidade FROM carrinho_produto WHERE id_carrinho = ? AND id_produto = ? AND is_food_to_save = ?";
        try (PreparedStatement stmt = connection.prepareStatement(verificaProduto)) {
            stmt.setInt(1, idCarrinho);
            stmt.setInt(2, item.getProduto().getId());
            stmt.setBoolean(3, item.isFoodToSave());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int quantidadeAtual = rs.getInt("quantidade");
                int novaQuantidade = quantidadeAtual + item.getQuantidade();

                String atualizaQuantidade = "UPDATE carrinho_produto SET quantidade = ? WHERE id_carrinho = ? AND id_produto = ? AND is_food_to_save = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(atualizaQuantidade)) {
                    updateStmt.setInt(1, novaQuantidade);
                    updateStmt.setInt(2, idCarrinho);
                    updateStmt.setInt(3, item.getProduto().getId());
                    updateStmt.setBoolean(4, item.isFoodToSave());
                    updateStmt.executeUpdate();
                }
            } else {
                String insereProduto = "INSERT INTO carrinho_produto (id_carrinho, id_produto, quantidade, preco_unitario, is_food_to_save) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = connection.prepareStatement(insereProduto)) {
                    insertStmt.setInt(1, idCarrinho);
                    insertStmt.setInt(2, item.getProduto().getId());
                    insertStmt.setInt(3, item.getQuantidade());
                    insertStmt.setDouble(4, item.getPrecoUnitario());
                    insertStmt.setBoolean(5, item.isFoodToSave());
                    insertStmt.executeUpdate();
                }
            }
        }
    }

    public int obterOuCriarCarrinhoAberto(int clienteId) throws SQLException {
        if (!clienteExiste(clienteId)) {
            throw new SQLException("Cliente não encontrado: " + clienteId);
        }

        String buscaCarrinho = "SELECT id_carrinho FROM carrinho WHERE id_cliente = ? AND status = 'ABERTO'";
        try (PreparedStatement stmt = connection.prepareStatement(buscaCarrinho)) {
            stmt.setInt(1, clienteId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_carrinho");
            }
        }

        String insereCarrinho = "INSERT INTO carrinho (id_cliente, status) VALUES (?, 'ABERTO')";
        try (PreparedStatement stmtInsere = connection.prepareStatement(insereCarrinho, Statement.RETURN_GENERATED_KEYS)) {
            stmtInsere.setInt(1, clienteId);
            stmtInsere.executeUpdate();
            ResultSet generatedKeys = stmtInsere.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Falha ao obter ID do novo carrinho.");
            }
        }
    }

    @Override
    public boolean limparCarrinhoDoCliente(int clienteId) {
        try {
            String buscaCarrinho = "SELECT id_carrinho FROM carrinho WHERE id_cliente = ? AND status = 'ABERTO'";
            int idCarrinho;
            try (PreparedStatement stmtBusca = connection.prepareStatement(buscaCarrinho)) {
                stmtBusca.setInt(1, clienteId);
                ResultSet rs = stmtBusca.executeQuery();
                if (rs.next()) {
                    idCarrinho = rs.getInt("id_carrinho");
                } else {
                    throw new SQLException("Carrinho aberto não encontrado para cliente: " + clienteId);
                }
            }

            String sql = "DELETE FROM carrinho_produto WHERE id_carrinho = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, idCarrinho);
                return stmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<CarrinhoProduto> listarPorCliente(int clienteId) {
        List<CarrinhoProduto> itens = new ArrayList<>();
        int idCarrinho;
        try {
            idCarrinho = obterOuCriarCarrinhoAberto(clienteId);
        } catch (SQLException e) {
            e.printStackTrace();
            return itens;
        }

        String sqlProdutos = """
            SELECT cp.id_produto, cp.quantidade, cp.preco_unitario, cp.is_food_to_save,
                   p.nome, p.preco_unitario AS produto_preco, p.imagem_path, p.descricao,
                   u.id_unidade, u.nome AS nome_unidade
            FROM carrinho_produto cp
            JOIN produto p ON cp.id_produto = p.id_produto
            JOIN unidade_medida u ON p.id_unidade = u.id_unidade
            WHERE cp.id_carrinho = ?
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sqlProdutos)) {
            stmt.setInt(1, idCarrinho);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Produto produto = new Produto(
                    rs.getInt("id_produto"),
                    rs.getString("nome"),
                    rs.getDouble("produto_preco"),
                    rs.getString("imagem_path"),
                    rs.getString("descricao"),
                    new UnidadeMedida(rs.getInt("id_unidade"), rs.getString("nome_unidade"))
                );
                CarrinhoProduto item = new CarrinhoProduto(
                    clienteId,
                    produto,
                    rs.getInt("quantidade"),
                    rs.getDouble("preco_unitario"),
                    rs.getBoolean("is_food_to_save")
                );
                itens.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return itens;
    }

    @Override
    public boolean atualizarQuantidade(int clienteId, int produtoId, int quantidade) {
        return atualizarQuantidade(clienteId, produtoId, quantidade, false);
    }

    @Override
    public boolean atualizarQuantidade(int clienteId, int produtoId, int quantidade, boolean isFoodToSave) {
        try {
            int idCarrinho = obterCarrinhoAbertoPorCliente(clienteId);
            String sql = "UPDATE carrinho_produto SET quantidade = ? WHERE id_carrinho = ? AND id_produto = ? AND is_food_to_save = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, quantidade);
                stmt.setInt(2, idCarrinho);
                stmt.setInt(3, produtoId);
                stmt.setBoolean(4, isFoodToSave);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int obterCarrinhoAbertoPorCliente(int clienteId) throws SQLException {
        String sql = "SELECT id_carrinho FROM carrinho WHERE id_cliente = ? AND status = 'ABERTO'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clienteId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_carrinho");
            } else {
                throw new SQLException("Carrinho aberto não encontrado para cliente: " + clienteId);
            }
        }
    }

    @Override
    public List<CarrinhoProduto> listarPorPedido(int pedidoId) throws SQLException {
        String sql = """
            SELECT pi.quantidade, pi.preco_unitario, p.id_produto, p.nome, p.id_unidade, u.nome AS unidade_nome
            FROM pedido_produto pi
            JOIN produto p ON pi.id_produto = p.id_produto
            JOIN unidade_medida u ON p.id_unidade = u.id_unidade
            WHERE pi.id_pedido = ?
        """;

        List<CarrinhoProduto> itens = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, pedidoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Produto produto = new Produto();
                    produto.setId(rs.getInt("id_produto"));
                    produto.setNome(rs.getString("nome"));
                    produto.setUnidade(new UnidadeMedida(rs.getInt("id_unidade"), rs.getString("unidade_nome")));

                    CarrinhoProduto item = new CarrinhoProduto();
                    item.setProduto(produto);
                    item.setQuantidade(rs.getInt("quantidade"));
                    item.setPrecoUnitario(rs.getDouble("preco_unitario"));

                    itens.add(item);
                }
            }
        }

        return itens;
    }

    @Override
    public boolean removerItem(int clienteId, int produtoId) {
        return removerItem(clienteId, produtoId, false);
    }

    public boolean removerItem(int clienteId, int produtoId, boolean isFoodToSave) {
        try {
            int idCarrinho = obterCarrinhoAbertoPorCliente(clienteId);
            String sql = "DELETE FROM carrinho_produto WHERE id_carrinho = ? AND id_produto = ? AND is_food_to_save = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, idCarrinho);
                stmt.setInt(2, produtoId);
                stmt.setBoolean(3, isFoodToSave);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
