package com.hortifacil.dao;

import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.Pedido;
import com.hortifacil.model.Produto;
import com.hortifacil.model.UnidadeMedida;
import com.hortifacil.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PedidoDAO {

    
    private Connection conn;

    public PedidoDAO() {}

    public PedidoDAO(Connection conn) {
        this.conn = conn;
    }

    // Método auxiliar para obter conexão segura
    private Connection getConnection() throws SQLException {
        return (conn != null) ? conn : DatabaseConnection.getConnection();
    }

    /*** SALVAR PEDIDO ***/
    public int salvarPedido(Pedido pedido) {
        String sql = "INSERT INTO pedido (id_cliente, data_pedido, total, status, ativo) VALUES (?, ?, ?, ?, ?)";
        if (conn != null) { // conexão injetada
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                return executarSalvarPedido(stmt, pedido);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else { // conexão local temporária
            try (Connection c = getConnection();
                 PreparedStatement stmt = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                return executarSalvarPedido(stmt, pedido);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    private int executarSalvarPedido(PreparedStatement stmt, Pedido pedido) throws SQLException {
        stmt.setInt(1, pedido.getIdCliente());
        stmt.setDate(2, Date.valueOf(pedido.getDataPedido()));
        stmt.setDouble(3, pedido.getTotal());
        stmt.setString(4, pedido.getStatus());
        stmt.setBoolean(5, pedido.isAtivo());
        stmt.executeUpdate();

        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    /*** SALVAR ITENS DO PEDIDO ***/
    public void salvarItensPedido(int idPedido, List<CarrinhoProduto> itensCarrinho) {
        String sql = "INSERT INTO pedido_produto (id_pedido, id_produto, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";
        if (conn != null) {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                executarSalvarItens(stmt, idPedido, itensCarrinho);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try (Connection c = getConnection();
                 PreparedStatement stmt = c.prepareStatement(sql)) {
                executarSalvarItens(stmt, idPedido, itensCarrinho);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void executarSalvarItens(PreparedStatement stmt, int idPedido, List<CarrinhoProduto> itensCarrinho) throws SQLException {
        for (CarrinhoProduto item : itensCarrinho) {
            stmt.setInt(1, idPedido);
            stmt.setInt(2, item.getProduto().getId());
            stmt.setInt(3, item.getQuantidade());
            stmt.setDouble(4, item.getPrecoUnitario());
            stmt.addBatch();
        }
        stmt.executeBatch();
    }

        /*** LISTAR PEDIDOS POR CLIENTE ***/
       public List<Pedido> listarPedidosPorCliente(int clienteId) {
            List<Pedido> pedidos = new ArrayList<>();
            String sql = "SELECT * FROM pedido WHERE id_cliente = ? ORDER BY data_pedido DESC, id_pedido DESC";

            try (Connection c = (conn != null) ? conn : getConnection();
                PreparedStatement stmt = c.prepareStatement(sql)) {

                stmt.setInt(1, clienteId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Pedido pedido = new Pedido();
                        pedido.setIdPedido(rs.getInt("id_pedido"));
                        pedido.setIdCliente(rs.getInt("id_cliente"));
                        pedido.setStatus(rs.getString("status"));

                        // Usando Timestamp para capturar data + hora
                        Timestamp ts = rs.getTimestamp("data_pedido");
                        if (ts != null) {
                            pedido.setDataPedido(rs.getTimestamp("data_pedido").toLocalDateTime().toLocalDate());
                        }

                        pedidos.add(pedido);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Garantia extra: ordena do mais recente para o mais antigo
            pedidos.sort(Comparator.comparing(Pedido::getDataPedido).reversed());

            return pedidos;
        }


    /*** LISTAR TODOS PEDIDOS ***/
    public List<Pedido> listarTodosPedidos() {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT * FROM pedido ORDER BY data_pedido DESC, id_pedido DESC";

        try (Connection c = (conn != null) ? conn : getConnection();
            PreparedStatement stmt = c.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Pedido pedido = new Pedido();
                pedido.setIdPedido(rs.getInt("id_pedido"));
                pedido.setIdCliente(rs.getInt("id_cliente"));
                pedido.setDataPedido(rs.getDate("data_pedido").toLocalDate());
                pedido.setTotal(rs.getDouble("total"));
                pedido.setStatus(rs.getString("status"));
                pedido.setAtivo(rs.getBoolean("ativo"));

                pedido.setItens(listarItensPorPedido(pedido.getIdPedido()));

                pedidos.add(pedido);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pedidos;
    }

    /*** LISTAR ITENS POR PEDIDO ***/
    public List<CarrinhoProduto> listarItensPorPedido(int idPedido) {
        List<CarrinhoProduto> itens = new ArrayList<>();
        String sql = "SELECT p.id_produto, p.nome, pp.quantidade, pp.preco_unitario " +
                     "FROM pedido_produto pp " +
                     "JOIN produto p ON pp.id_produto = p.id_produto " +
                     "WHERE pp.id_pedido = ?";

        try (Connection c = (conn != null) ? conn : getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {

            stmt.setInt(1, idPedido);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Produto produto = new Produto(
                        rs.getInt("id_produto"),
                        rs.getString("nome"),
                        rs.getDouble("preco_unitario"),
                        "", "", null
                    );
                    CarrinhoProduto item = new CarrinhoProduto();
                    item.setProduto(produto);
                    item.setQuantidade(rs.getInt("quantidade"));
                    item.setPrecoUnitario(rs.getDouble("preco_unitario"));
                    itens.add(item);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return itens;
    }

    /*** ATUALIZAR STATUS ***/
    public boolean atualizarStatus(int idPedido, String status) {
        String sql = "UPDATE pedido SET status = ? WHERE id_pedido = ?";
        try (Connection c = (conn != null) ? conn : getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, idPedido);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*** BUSCAR ITENS DETALHADOS DO PEDIDO ***/
    public List<CarrinhoProduto> buscarItensPedido(int idPedido) {
        List<CarrinhoProduto> itens = new ArrayList<>();
        String sql = """
            SELECT cp.id_produto, cp.quantidade, cp.preco_unitario,
                   p.nome, p.descricao, p.imagem_path, p.preco_unitario,
                   u.id_unidade, u.nome as nome_unidade
            FROM pedido_produto cp
            JOIN produto p ON cp.id_produto = p.id_produto
            JOIN unidade_medida u ON p.id_unidade = u.id_unidade
            WHERE cp.id_pedido = ?
        """;

        try (Connection c = (conn != null) ? conn : getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {

            stmt.setInt(1, idPedido);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
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
                        unidade
                    );

                    CarrinhoProduto item = new CarrinhoProduto();
                    item.setProduto(produto);
                    item.setQuantidade((int) rs.getDouble("quantidade"));
                    item.setPrecoUnitario(rs.getDouble("preco_unitario"));

                    itens.add(item);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return itens;
    }

    /*** LISTAR PEDIDOS POR ASSINATURA ***/
public List<Pedido> listarPorAssinatura(int assinaturaId) {
    List<Pedido> pedidos = new ArrayList<>();
    String sql = "SELECT * FROM pedido WHERE id_assinatura = ? ORDER BY data_pedido DESC";

    try (Connection c = (conn != null) ? conn : getConnection();
         PreparedStatement stmt = c.prepareStatement(sql)) {

        stmt.setInt(1, assinaturaId);

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Pedido pedido = new Pedido();
                pedido.setIdPedido(rs.getInt("id_pedido"));
                pedido.setIdCliente(rs.getInt("id_cliente"));
                pedido.setDataPedido(rs.getDate("data_pedido").toLocalDate());
                pedido.setTotal(rs.getDouble("total"));
                pedido.setStatus(rs.getString("status"));
                pedido.setAtivo(rs.getBoolean("ativo"));

                // opcional: carregar itens
                pedido.setItens(listarItensPorPedido(pedido.getIdPedido()));

                pedidos.add(pedido);
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return pedidos;
}

/*** CRIAR PEDIDO A PARTIR DE UMA LISTA DE ITENS ***/
public int criarPedidoCarrinho(List<CarrinhoProduto> itens, int idCliente, String metodoPagamento) {
    Pedido pedido = new Pedido();
    pedido.setIdCliente(idCliente);
    pedido.setDataPedido(LocalDate.now());
    
    // ✅ Aqui deve ser EM_ANDAMENTO para pedidos do carrinho
    pedido.setStatus("EM_ANDAMENTO");
    
    double total = itens.stream()
            .mapToDouble(item -> item.getPrecoUnitario() * item.getQuantidade())
            .sum();
    pedido.setTotal(total);
    pedido.setAtivo(true);

    int pedidoId = salvarPedido(pedido);
    if (pedidoId > 0) {
        salvarItensPedido(pedidoId, itens);
    }
    return pedidoId;
}

}