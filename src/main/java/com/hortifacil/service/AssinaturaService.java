package com.hortifacil.service;


import com.hortifacil.dao.ProdutoDAOImpl;
import com.hortifacil.dao.AssinaturaDAO;
import com.hortifacil.dao.AssinaturaDAOImpl;
import com.hortifacil.dao.PedidoAssinaturaDAO;
import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.Assinatura;
import com.hortifacil.model.AssinaturaModelo;
import com.hortifacil.model.AssinaturaProduto;
import com.hortifacil.model.PedidoAssinatura;
import com.hortifacil.model.ProdutoEstoque;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;
import com.hortifacil.dao.AssinaturaModeloDAO;
import com.hortifacil.dao.AssinaturaModeloDAOImpl;
import com.hortifacil.dao.AssinaturaProdutoDAOImpl;

public class AssinaturaService {

    private final AssinaturaDAO assinaturaDAO;
    private final PedidoAssinaturaDAO pedidoAssinaturaDAO;
    private final AssinaturaModeloDAO modeloDAO;
    private final AssinaturaProdutoDAOImpl assinaturaProdutoDAO;
    private final ProdutoDAO produtoDAO;

    public AssinaturaService() {
        this.assinaturaDAO = new AssinaturaDAOImpl();
        this.pedidoAssinaturaDAO = new PedidoAssinaturaDAO();
        this.modeloDAO = new AssinaturaModeloDAOImpl();
        this.assinaturaProdutoDAO = new AssinaturaProdutoDAOImpl();
        this.produtoDAO = new ProdutoDAOImpl();
    }


    // Criar assinatura para um cliente a partir de um modelo
    public boolean criarAssinaturaParaCliente(int clienteId, AssinaturaModelo modelo) {
        Assinatura assinatura = new Assinatura();
        assinatura.setIdCliente(clienteId);
        assinatura.setIdModelo(modelo.getIdModelo());
        assinatura.setModelo(modelo);
        assinatura.setDataInicio(LocalDate.now());
        assinatura.setStatus(Assinatura.Status.ATIVA);

        return assinaturaDAO.criarAssinatura(assinatura);
    }

    // Listar assinaturas ativas
    public List<Assinatura> listarAssinaturasAtivas() throws SQLException {
        return assinaturaDAO.listarAtivas();
    }

    // Cancelar assinatura
    public boolean cancelarAssinatura(int idAssinatura) {
        return assinaturaDAO.cancelarAssinatura(idAssinatura);
    }

    // Calcular a próxima data de entrega com base na data de início e frequência do modelo
    public LocalDate calcularProximaEntrega(Assinatura assinatura) {
        int frequenciaDias = assinatura.getModelo().getFrequencia(); // frequência do modelo
        LocalDate data = assinatura.getDataInicio();
        LocalDate hoje = LocalDate.now();

        while (!data.isAfter(hoje)) {
            data = data.plusDays(frequenciaDias);
        }
        return data;
    }

    // Gerar pedido da assinatura
    public boolean gerarPedidoAssinatura(Assinatura assinatura, List<ProdutoEstoque> produtos) throws SQLException {
    try {
        PedidoAssinatura pedido = new PedidoAssinatura();
        pedido.setIdAssinatura(assinatura.getIdAssinatura());
        pedido.setDataEntrega(calcularProximaEntrega(assinatura));
        pedido.setStatus(PedidoAssinatura.Status.PENDENTE);

        double valorTotal = 0;
        for (ProdutoEstoque p : produtos) {
            valorTotal += p.getProduto().getPreco() * p.getQuantidade();
        }
        pedido.setValorTotal(valorTotal);

        // Salvar pedido no banco
        int linhas = pedidoAssinaturaDAO.criarPedido(pedido);
        boolean sucesso = linhas > 0;

        if (sucesso) {
            pedidoAssinaturaDAO.atualizarValorTotal(pedido.getId(), valorTotal);

            // 🔽 Aqui chamamos o DAO da tabela assinatura_produto
            AssinaturaProdutoDAOImpl assinaturaProdutoDAO = new AssinaturaProdutoDAOImpl();

            for (ProdutoEstoque p : produtos) {
                AssinaturaProduto ap = new AssinaturaProduto();
                ap.setIdAssinatura(assinatura.getIdAssinatura());
                ap.setIdProduto(p.getProduto().getId());
                ap.setQuantidade((int) p.getQuantidade());
                assinaturaProdutoDAO.criarAssinaturaProduto(ap);
            }
        }

        return sucesso;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

    public boolean criarPedido(PedidoAssinatura pedido) throws SQLException {
        String sql = "INSERT INTO pedido_assinatura (id_assinatura, data_entrega, status, valor_total) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pedido.getIdAssinatura());
            stmt.setDate(2, java.sql.Date.valueOf(pedido.getDataEntrega()));
            stmt.setString(3, pedido.getStatus().name());
            stmt.setDouble(4, pedido.getValorTotal());

            int linhas = stmt.executeUpdate();
            return linhas > 0;
        }
    }

    // Listar assinaturas de um cliente
    public List<Assinatura> listarAssinaturasPorCliente(int clienteId) {
        return assinaturaDAO.listarAssinaturasPorCliente(clienteId);
    }

    // Alterar horário de entrega
   public boolean alterarHorarioEntrega(int idAssinatura, String novoHorario) {
    try {
        return assinaturaDAO.alterarHorarioEntrega(idAssinatura, novoHorario);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
    }

    public boolean alterarPagamento(int idAssinatura, String novaFormaPagamento) {
        try {
            return assinaturaDAO.alterarPagamento(idAssinatura, novaFormaPagamento);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Salvar/Atualizar um modelo existente
    public boolean salvarModelo(AssinaturaModelo modelo) {
        return modeloDAO.atualizar(modelo);
    }

    // Criar um novo modelo
    public boolean criarModelo(AssinaturaModelo modelo) {
        return modeloDAO.criar(modelo);
    }

    // Listar todos os modelos ativos
    public List<AssinaturaModelo> listarModelos() {
        return modeloDAO.listarTodos();
    }

    // Excluir um modelo
    public boolean excluirModelo(int idModelo) {
        return modeloDAO.excluir(idModelo);
    }

    // Buscar modelo por ID
    public AssinaturaModelo buscarModeloPorId(int idModelo) {
        return modeloDAO.buscarPorId(idModelo);
    }

    public int contarUsuariosPorAssinatura(int idModelo) {
        return assinaturaDAO.contarClientesPorModelo(idModelo);
    }

    public boolean atualizarStatusPedido(PedidoAssinatura pedido) throws SQLException {
        return pedidoAssinaturaDAO.atualizarStatus(pedido.getId(), pedido.getStatus());
    }

    public List<Assinatura> listarTodasAssinaturas() throws SQLException {
        return assinaturaDAO.listarTodos();
    }

    public void verificarEGerarPedidosAutomaticamente() {
    try {
        List<Assinatura> assinaturas = assinaturaDAO.listarAtivas();
        LocalDate hoje = LocalDate.now();

        for (Assinatura assinatura : assinaturas) {
            // Define próxima geração se for a primeira vez
            if (assinatura.getProximaGeracao() == null) {
                assinatura.setProximaGeracao(calcularProximaEntrega(assinatura));
                assinaturaDAO.atualizarDatasGeracao(
                    assinatura.getIdAssinatura(),
                    null,
                    assinatura.getProximaGeracao()
                );
                continue;
            }

            // Se é dia de gerar novo pedido
            if (!hoje.isBefore(assinatura.getProximaGeracao())) {
                List<AssinaturaProduto> produtosAssinatura =
                        assinaturaProdutoDAO.listarPorAssinatura(assinatura.getIdAssinatura());

                List<ProdutoEstoque> produtosEstoque = new ArrayList<>();
                for (AssinaturaProduto ap : produtosAssinatura) {
                    ProdutoEstoque p = new ProdutoEstoque();
                    p.setProduto(produtoDAO.buscarPorId(ap.getIdProduto()));
                    p.setQuantidade(ap.getQuantidade());
                    produtosEstoque.add(p);
                }

                // Gera pedido e registra a geração
                gerarPedidoAssinatura(assinatura, produtosEstoque);

                assinatura.setDataUltimaGeracao(hoje);
                assinatura.setProximaGeracao(calcularProximaEntrega(assinatura));

                assinaturaDAO.atualizarDatasGeracao(
                    assinatura.getIdAssinatura(),
                    assinatura.getDataUltimaGeracao(),
                    assinatura.getProximaGeracao()
                );

                System.out.println("✅ Pedido automático gerado para assinatura " + assinatura.getIdAssinatura());
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

public void gerarPedidosAutomaticos() {
   List<Assinatura> assinaturas = assinaturaDAO.listarAtivas();
    LocalDate hoje = LocalDate.now();

    for (Assinatura a : assinaturas) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            if (a.getProximaGeracao() != null && !hoje.isBefore(a.getProximaGeracao())) {

                // 1️⃣ Gerar o pedido
                String sqlPedido = """
                    INSERT INTO pedido (id_cliente, data_pedido, status, valor_total, tipo)
                    VALUES (?, CURDATE(), 'PENDENTE', 0, 'ASSINATURA')
                """;
                PreparedStatement stmtPedido = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS);
                stmtPedido.setInt(1, a.getIdCliente());
                stmtPedido.executeUpdate();

                ResultSet rsPedido = stmtPedido.getGeneratedKeys();
                int idPedido = 0;
                if (rsPedido.next()) idPedido = rsPedido.getInt(1);

                // 2️⃣ Inserir produtos da assinatura
                String sqlItens = """
                    INSERT INTO pedido_produto (id_pedido, id_produto, quantidade, unidade)
                    SELECT ?, id_produto, quantidade, unidade
                    FROM assinatura_produto WHERE id_assinatura = ?
                """;
                PreparedStatement stmtItens = conn.prepareStatement(sqlItens);
                stmtItens.setInt(1, idPedido);
                stmtItens.setInt(2, a.getIdAssinatura());
                stmtItens.executeUpdate();

                // 3️⃣ Atualizar próximas datas
                LocalDate proxima = a.getProximaGeracao().plusDays(a.getModelo().getFrequencia());
                assinaturaDAO.atualizarDatasGeracao(a.getIdAssinatura(), hoje, proxima);

                conn.commit();
                System.out.println("✅ Pedido gerado automaticamente para assinatura #" + a.getIdAssinatura());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

}