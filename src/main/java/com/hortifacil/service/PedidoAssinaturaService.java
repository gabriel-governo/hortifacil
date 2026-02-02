package com.hortifacil.service;

import com.hortifacil.dao.PedidoAssinaturaDAO;
import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.AssinaturaModelo;
import com.hortifacil.model.PedidoAssinatura;
import com.hortifacil.model.ProdutoEstoque;
import com.hortifacil.model.Produto;
import com.hortifacil.model.UnidadeMedida;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PedidoAssinaturaService {

    private static PedidoAssinaturaService instance;

    private final PedidoAssinaturaDAO pedidoAssinaturaDAO;

    private PedidoAssinaturaService(ProdutoDAO produtoDAO) {
        this.pedidoAssinaturaDAO = new PedidoAssinaturaDAO();
    }

    public static PedidoAssinaturaService getInstance(ProdutoDAO dao) {
        if (instance == null) {
            instance = new PedidoAssinaturaService(dao);
        }
        return instance;
    }

    // Cria um pedido de assinatura com os produtos (assinatura_produto)
    public void criarPedidoAssinatura(int idAssinatura, int idCliente, double valorTotal, List<ProdutoEstoque> produtos) throws SQLException {
        PedidoAssinatura pedido = new PedidoAssinatura();
        pedido.setIdAssinatura(idAssinatura);
        pedido.setIdCliente(idCliente);
        pedido.setValorTotal(valorTotal);
        pedido.setDataCriacao(LocalDateTime.now());
        pedido.setDataEntrega(LocalDate.now().plusDays(7));
        pedido.setStatus(PedidoAssinatura.Status.PENDENTE);

        int idPedidoGerado = pedidoAssinaturaDAO.criarPedido(pedido);

        // Salva os produtos no "assinatura_produto"
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO assinatura_produto (id_pedido_assinatura, id_produto, quantidade) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (ProdutoEstoque pe : produtos) {
                    stmt.setInt(1, idPedidoGerado);
                    stmt.setInt(2, pe.getProduto().getId());
                    stmt.setInt(3, 1); // quantidade pode ser parametrizada se quiser
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }
    }

    // Lista pedidos de um cliente específico
    public List<PedidoAssinatura> listarPedidosPorCliente(int idCliente) throws SQLException {
        return pedidoAssinaturaDAO.listarPorCliente(idCliente);
    }

    // Lista todos os pedidos
    public List<PedidoAssinatura> listarTodosPedidos() throws SQLException {
        return pedidoAssinaturaDAO.listarTodos();
    }

    // Atualiza status de um pedido
    public void atualizarStatus(int idPedido, PedidoAssinatura.Status novoStatus) throws SQLException {
        pedidoAssinaturaDAO.atualizarStatus(idPedido, novoStatus);
    }

    public boolean atualizarStatusPedido(PedidoAssinatura pedido) throws SQLException {
        return pedidoAssinaturaDAO.atualizarStatus(pedido.getId(), pedido.getStatus());
    }

    // Busca produtos disponíveis no estoque
    public List<ProdutoEstoque> buscarProdutosDisponiveis() throws SQLException {
        List<ProdutoEstoque> lista = new ArrayList<>();
        String sql = "SELECT pe.id AS id_estoque, pe.id_produto, pe.quantidade, pe.data_colheita, pe.data_validade, " +
                     "p.nome, p.preco_unitario, p.descricao, p.id_unidade, u.nome AS unidade_nome " +
                     "FROM produto_estoque pe " +
                     "JOIN produto p ON pe.id_produto = p.id_produto " +
                     "JOIN unidade_medida u ON p.id_unidade = u.id_unidade " +
                     "WHERE pe.quantidade > 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Produto p = new Produto();
                p.setId(rs.getInt("id_produto"));
                p.setNome(rs.getString("nome"));
                p.setPreco(rs.getDouble("preco_unitario"));
                p.setDescricao(rs.getString("descricao"));
                p.setUnidade(new UnidadeMedida(rs.getInt("id_unidade"), rs.getString("unidade_nome")));

                ProdutoEstoque pe = new ProdutoEstoque();
                pe.setId(rs.getInt("id_estoque"));
                pe.setProduto(p);
                pe.setQuantidade(rs.getInt("quantidade"));
                pe.setDataColhido(rs.getDate("data_colheita").toLocalDate());
                pe.setDataValidade(rs.getDate("data_validade").toLocalDate());

                lista.add(pe);
            }
        }
        return lista;
    }

    // Gera produtos para assinatura com base no modelo
    public List<ProdutoEstoque> gerarProdutosDaAssinatura(AssinaturaModelo modelo) throws SQLException {
        List<ProdutoEstoque> produtosDisponiveis = buscarProdutosDisponiveis();
        List<ProdutoEstoque> selecionados = new ArrayList<>();

        if (produtosDisponiveis.isEmpty()) {
            throw new SQLException("Nenhum produto disponível para gerar itens da assinatura.");
        }

        int quantidadeItens = modelo.getQuantidadeProdutos();
        Random random = new Random();

        for (int i = 0; i < quantidadeItens && !produtosDisponiveis.isEmpty(); i++) {
            int index = random.nextInt(produtosDisponiveis.size());
            ProdutoEstoque lote = produtosDisponiveis.get(index);
            selecionados.add(lote);

            // Reduz quantidade local para evitar repetir lotes
            lote.setQuantidade(lote.getQuantidade() - 1);
            if (lote.getQuantidade() <= 0) {
                produtosDisponiveis.remove(index);
            }
        }

        return selecionados;
    }
}
