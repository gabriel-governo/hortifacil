package com.hortifacil.service;

import com.hortifacil.dao.PedidoDAO;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.Pedido;
import com.hortifacil.model.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

public class PedidoService {

    private static PedidoService instance;
    private PedidoDAO pedidoDAO;

    private PedidoService() {
        pedidoDAO = new PedidoDAO();
    }

    public static PedidoService getInstance() {
        if (instance == null) {
            instance = new PedidoService();
        }
        return instance;
    }

    public boolean criarPedido(int clienteId, List<CarrinhoProduto> itensCarrinho) {
        double total = 0.0;

        for (CarrinhoProduto item : itensCarrinho) {
            total += item.getQuantidade() * item.getPrecoUnitario();
        }

        Pedido pedido = new Pedido(
            clienteId,
            LocalDate.now(),
            total,
            "EM_ANDAMENTO",
            true          
        );

        int idPedido = pedidoDAO.salvarPedido(pedido);
        if (idPedido == -1) {
            return false;
        }

        pedidoDAO.salvarItensPedido(idPedido, itensCarrinho);
        return true;
    }

    public List<Pedido> listarPedidosPorCliente(int clienteId) {
        return pedidoDAO.listarPedidosPorCliente(clienteId);
    }

    public boolean finalizarPedido(int clienteId) {
    Connection conn = null;
    try {
        conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);

        // Buscar carrinho aberto
        int idCarrinho;
        String sqlCarrinho = "SELECT id_carrinho FROM carrinho WHERE id_cliente = ? AND status = 'ABERTO'";
        try (PreparedStatement psCarrinho = conn.prepareStatement(sqlCarrinho)) {
            psCarrinho.setInt(1, clienteId);
            try (ResultSet rs = psCarrinho.executeQuery()) {
                if (rs.next()) {
                    idCarrinho = rs.getInt("id_carrinho");
                } else {
                    System.out.println("Nenhum carrinho aberto encontrado para o cliente.");
                    conn.rollback();
                    return false;
                }
            }
        }

        // Buscar itens do carrinho
        List<CarrinhoProduto> itens = new ArrayList<>();
        String sqlItens = "SELECT cp.id_produto, cp.quantidade, cp.preco_unitario, p.nome " +
                          "FROM carrinho_produto cp JOIN produto p ON cp.id_produto = p.id " +
                          "WHERE cp.id_carrinho = ?";
        try (PreparedStatement psItens = conn.prepareStatement(sqlItens)) {
            psItens.setInt(1, idCarrinho);
            try (ResultSet rs = psItens.executeQuery()) {
                while (rs.next()) {
                    int idProduto = rs.getInt("id_produto");
                    int quantidade = rs.getInt("quantidade");
                    double precoUnitario = rs.getDouble("preco_unitario");
                    String nome = rs.getString("nome");

                    Produto produto = new Produto(idProduto, nome, precoUnitario, null, null, null);
                    CarrinhoProduto cp = new CarrinhoProduto();
                    cp.setProduto(produto);
                    cp.setQuantidade(quantidade);
                    cp.setPrecoUnitario(precoUnitario);
                    itens.add(cp);
                }
            }
        }

        if (itens.isEmpty()) {
            System.out.println("Carrinho está vazio.");
            conn.rollback();
            return false;
        }

        // Verificar estoque e deduzir
        String sqlEstoqueSelect = "SELECT quantidade FROM estoque WHERE id_produto = ? FOR UPDATE";
        String sqlEstoqueUpdate = "UPDATE estoque SET quantidade = quantidade - ? WHERE id_produto = ?";

        try (PreparedStatement psEstoqueSelect = conn.prepareStatement(sqlEstoqueSelect);
             PreparedStatement psEstoqueUpdate = conn.prepareStatement(sqlEstoqueUpdate)) {

            for (CarrinhoProduto item : itens) {
                int idProduto = item.getProduto().getId();
                int qtdPedido = item.getQuantidade();

                psEstoqueSelect.setInt(1, idProduto);
                try (ResultSet rsEstoque = psEstoqueSelect.executeQuery()) {
                    if (rsEstoque.next()) {
                        double qtdEstoque = rsEstoque.getDouble("quantidade");
                        if (qtdEstoque < qtdPedido) {
                            System.out.println("Estoque insuficiente para o produto: " + item.getProduto().getNome());
                            conn.rollback();
                            return false;
                        }
                    } else {
                        System.out.println("Produto não encontrado no estoque: " + item.getProduto().getNome());
                        conn.rollback();
                        return false;
                    }
                }

                psEstoqueUpdate.setDouble(1, qtdPedido);
                psEstoqueUpdate.setInt(2, idProduto);
                psEstoqueUpdate.executeUpdate();
            }
        }

        // Atualizar status do carrinho para FECHADO
        String sqlAtualizaCarrinho = "UPDATE carrinho SET status = 'FECHADO', data_fechamento = CURRENT_DATE WHERE id_carrinho = ?";
        try (PreparedStatement psAtualiza = conn.prepareStatement(sqlAtualizaCarrinho)) {
            psAtualiza.setInt(1, idCarrinho);
            psAtualiza.executeUpdate();
        }

        // Criar pedido e salvar itens com DAO usando mesma conexão
        double total = itens.stream().mapToDouble(i -> i.getQuantidade() * i.getPrecoUnitario()).sum();
        Pedido pedido = new Pedido(clienteId, LocalDate.now(), total, "FINALIZADO", true);

        int idPedido = pedidoDAO.salvarPedido(pedido);
        pedidoDAO.salvarItensPedido(idPedido, itens);
 
        if (idPedido == -1) {
            System.out.println("Falha ao salvar pedido.");
            conn.rollback();
            return false;
        }

        pedidoDAO.salvarItensPedido(idPedido, itens);

        conn.commit();
        return true;

    } catch (SQLException e) {
        e.printStackTrace();
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    } finally {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

public boolean atualizarStatusPedido(Pedido pedido) {
    return pedidoDAO.atualizarStatus(pedido.getIdPedido(), pedido.getStatus());
}

public List<Pedido> listarTodosPedidos() {
    return pedidoDAO.listarTodosPedidos();
}

public List<CarrinhoProduto> buscarItensPedido(int idPedido) {
    return pedidoDAO.buscarItensPedido(idPedido); // método que você precisa criar no DAO
}

public int criarPedidoRetornaId(Connection connection, int clienteId, List<CarrinhoProduto> itens) throws SQLException {
    // Calcula o total do pedido
    double total = itens.stream()
                        .mapToDouble(i -> i.getQuantidade() * i.getPrecoUnitario())
                        .sum();

    // Adiciona o total na query de inserção
    String sqlPedido = "INSERT INTO pedido (id_cliente, data_pedido, status, total) VALUES (?, NOW(), 'EM_ANDAMENTO', ?)";
    String sqlItem = "INSERT INTO pedido_produto (id_pedido, id_produto, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";

    connection.setAutoCommit(false);
    int pedidoId = -1;

    // Inserção do pedido
    try (PreparedStatement stmtPedido = connection.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
        stmtPedido.setInt(1, clienteId);
        stmtPedido.setDouble(2, total); // seta o total
        int affectedRows = stmtPedido.executeUpdate();

        if (affectedRows == 0) 
            throw new SQLException("Falha ao criar pedido, nenhuma linha afetada.");

        try (ResultSet generatedKeys = stmtPedido.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                pedidoId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Falha ao criar pedido, nenhum ID retornado.");
            }
        }
    }

    // Inserção dos itens do pedido
    try (PreparedStatement stmtItem = connection.prepareStatement(sqlItem)) {
        for (CarrinhoProduto item : itens) {
            stmtItem.setInt(1, pedidoId);
            stmtItem.setInt(2, item.getProduto().getId());
            stmtItem.setInt(3, item.getQuantidade());
            stmtItem.setDouble(4, item.getPrecoUnitario());
            stmtItem.addBatch();
        }
        stmtItem.executeBatch();
    }

    connection.commit();
    connection.setAutoCommit(true);
    return pedidoId;
}

public double calcularTotalDoPedido(int idPedido) {
    double total = 0;
    try (Connection conn = DatabaseConnection.getConnection()) {
        String sql = "SELECT quantidade, preco_unitario FROM pedido_produto WHERE id_pedido = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPedido);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                total += rs.getInt("quantidade") * rs.getDouble("preco_unitario");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return total;
}

public void associarCartaoAoPedido(int pedidoId, int cartaoId) throws SQLException {
    String sql = "UPDATE pedido SET id_cartao = ? WHERE id_pedido = ?"; // ou insere em pedido_cartao, se existir
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, cartaoId);
        stmt.setInt(2, pedidoId);
        stmt.executeUpdate();
    }
}

}
