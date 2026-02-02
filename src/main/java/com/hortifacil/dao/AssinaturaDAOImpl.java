package com.hortifacil.dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.hortifacil.model.Assinatura;
import com.hortifacil.model.AssinaturaModelo;
import com.hortifacil.database.DatabaseConnection;

public class AssinaturaDAOImpl implements AssinaturaDAO {

    private final AssinaturaModeloDAO modeloDAO;

    public AssinaturaDAOImpl() {
        this.modeloDAO = new AssinaturaModeloDAOImpl();
    }

    public AssinaturaDAOImpl(Connection conn) {
        this.modeloDAO = new AssinaturaModeloDAOImpl();
    }

 @Override
public boolean criarAssinatura(Assinatura assinatura) {
    String sql = "INSERT INTO assinatura (id_cliente, id_modelo, data_inicio, status, hora_entrega) VALUES (?, ?, ?, ?, ?)";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        stmt.setInt(1, assinatura.getIdCliente());
        stmt.setInt(2, assinatura.getIdModelo());
        stmt.setDate(3, java.sql.Date.valueOf(assinatura.getDataInicio()));
        stmt.setString(4, assinatura.getStatus().name());
        stmt.setString(5, assinatura.getHorarioEntrega());

        int rows = stmt.executeUpdate();

        // Recuperar o ID gerado automaticamente (AUTO_INCREMENT)
        try (ResultSet rs = stmt.getGeneratedKeys()) {
            if (rs.next()) {
                assinatura.setIdAssinatura(rs.getInt(1));
            }
        }

        return rows > 0;

    } catch (SQLException e) {
        System.err.println("Erro ao criar assinatura: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

    // Mapear ResultSet -> Assinatura
   private Assinatura mapResultSetToAssinatura(ResultSet rs) throws SQLException {
    Assinatura a = new Assinatura();
    a.setIdAssinatura(rs.getInt("id"));
    a.setIdCliente(rs.getInt("id_cliente"));
    a.setIdModelo(rs.getInt("id_modelo"));

    Date dataInicio = rs.getDate("data_inicio");
    if (dataInicio != null) a.setDataInicio(dataInicio.toLocalDate());

    Date dataFim = rs.getDate("data_fim");
    if (dataFim != null) a.setDataFim(dataFim.toLocalDate());

    a.setStatus(Assinatura.Status.valueOf(rs.getString("status")));
    a.setHorarioEntrega(rs.getString("hora_entrega"));

    // Tentar preencher modelo a partir do ResultSet (se houver colunas)
    AssinaturaModelo modelo = null;
    try {
        modelo = new AssinaturaModelo();
        modelo.setIdModelo(rs.getInt("id_modelo"));
        modelo.setNome(rs.getString("modelo_nome"));
        modelo.setDescricao(rs.getString("modelo_desc"));
        modelo.setValor(rs.getDouble("modelo_valor"));
        modelo.setFrequencia(rs.getInt("modelo_frequencia"));
    } catch (SQLException e) {
        // Colunas não existem, buscar pelo DAO
        modelo = modeloDAO.buscarPorId(a.getIdModelo());
    }

    a.setModelo(modelo); // garante que o modelo nunca será null
    return a;
}

    // Cancelar assinatura
    @Override
    public boolean cancelarAssinatura(int idAssinatura) {
        String sql = "UPDATE assinatura SET status = 'CANCELADA', data_fim = CURDATE() WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idAssinatura);
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Listar todos os modelos disponíveis
    @Override
    public List<AssinaturaModelo> listarModelos() {
        List<AssinaturaModelo> modelos = new ArrayList<>();
        String sql = "SELECT * FROM assinatura_modelo WHERE ativo = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                AssinaturaModelo m = new AssinaturaModelo();
                m.setIdModelo(rs.getInt("id_modelo"));
                m.setNome(rs.getString("nome"));
                m.setDescricao(rs.getString("descricao"));
                m.setValor(rs.getDouble("valor"));
                m.setFrequencia(rs.getInt("frequencia"));
                modelos.add(m);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return modelos;
    }

    // Alterar horário de entrega
    @Override
    public boolean alterarHorarioEntrega(int idAssinatura, String horarioEntrega) {
        String sql = "UPDATE assinatura SET hora_entrega = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, horarioEntrega);
            stmt.setInt(2, idAssinatura);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Alterar pagamento (se precisar)
    @Override
    public boolean alterarPagamento(int idAssinatura, String formaPagamento) {
        String sql = "UPDATE fatura_assinatura SET forma_pagamento = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, formaPagamento);
            stmt.setInt(2, idAssinatura);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
public List<Assinatura> listarAssinaturasPorCliente(int clienteId) {
    List<Assinatura> assinaturas = new ArrayList<>();
    String sql = "SELECT a.*, m.nome AS modelo_nome, m.descricao AS modelo_desc, m.valor AS modelo_valor, m.frequencia AS modelo_frequencia " +
                 "FROM assinatura a JOIN assinatura_modelo m ON a.id_modelo = m.id_modelo WHERE a.id_cliente = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, clienteId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            assinaturas.add(mapResultSetToAssinatura(rs));
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return assinaturas;
}

@Override
public int contarUsuariosPorAssinatura(int idAssinatura) {
    String sql = "SELECT COUNT(*) FROM assinatura WHERE id_modelo = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, idAssinatura);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0;
}

@Override
public List<Assinatura> listarAtivas() {
    List<Assinatura> assinaturas = new ArrayList<>();
    String sql = "SELECT * FROM assinatura WHERE status = 'ATIVA'"; // sem JOIN

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            Assinatura a = mapResultSetToAssinatura(rs); // agora vai preencher o modelo via DAO se precisar
            assinaturas.add(a);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return assinaturas;
}

public LocalDate calcularProximaEntrega(Assinatura a) {
    AssinaturaModelo modelo = modeloDAO.buscarPorId(a.getIdModelo());
    LocalDate inicio = a.getDataInicio();
    int freq = modelo.getFrequencia();
    LocalDate hoje = LocalDate.now();

    while (!inicio.isAfter(hoje)) {
        inicio = inicio.plusDays(freq);
    }
    return inicio; // próxima entrega futura
}

@Override
public boolean vincularClienteAPlano(int clienteId, AssinaturaModelo modelo, String horarioEntrega) {
    Assinatura assinatura = new Assinatura();
    assinatura.setIdCliente(clienteId);
    assinatura.setIdModelo(modelo.getIdModelo()); // referência ao modelo do admin
    assinatura.setDataInicio(LocalDate.now());
    assinatura.setStatus(Assinatura.Status.ATIVA);
    assinatura.setHorarioEntrega(horarioEntrega);

    boolean criado = criarAssinatura(assinatura);

    if (criado) {
        // Preenche o modelo no objeto para evitar NullPointerException
        assinatura.setModelo(modelo);
    }

    return criado;
}

@Override
public int contarClientesPorModelo(int idModelo) {
    String sql = "SELECT COUNT(*) AS total FROM assinatura WHERE id_modelo = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
         
        stmt.setInt(1, idModelo);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("total");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0;
}

@Override
public Assinatura buscarPorId(int idAssinatura) {
    String sql = """
        SELECT a.*, 
               m.nome AS modelo_nome, 
               m.descricao AS modelo_desc, 
               m.valor AS modelo_valor, 
               m.frequencia AS modelo_frequencia
        FROM assinatura a
        JOIN assinatura_modelo m ON a.id_modelo = m.id_modelo
        WHERE a.id = ?
        """;

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, idAssinatura);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return mapResultSetToAssinatura(rs);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return null;
}

@Override
public boolean cancelarAssinaturasAtivasPorCliente(int idCliente) {
    String sql = "UPDATE assinatura SET status = 'CANCELADA', data_fim = CURDATE() WHERE id_cliente = ? AND status = 'ATIVA'";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, idCliente);
        int linhas = stmt.executeUpdate();
        return linhas > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

@Override
public void desativarAssinaturasAtivas(int idCliente) {
    String sql = "UPDATE assinatura SET status = 'CANCELADA' WHERE id_cliente = ? AND status = 'ATIVA'";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idCliente);
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

@Override
public boolean ativarAssinatura(int idCliente, int idAssinatura) {
    String sql = "UPDATE assinatura SET status = 'ATIVA' WHERE id_cliente = ? AND id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, idCliente);
        stmt.setInt(2, idAssinatura);
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

@Override
public void atualizarStatusEPeriodo(Assinatura assinatura) throws SQLException {
    String sql = "UPDATE assinatura SET status = ?, data_inicio = ?, data_fim = ? WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, assinatura.getStatus() != null ? assinatura.getStatus().name() : "ATIVA");
        stmt.setDate(2, java.sql.Date.valueOf(assinatura.getDataInicio()));
        stmt.setDate(3, java.sql.Date.valueOf(assinatura.getDataFim()));
        stmt.setInt(4, assinatura.getIdAssinatura());
        stmt.executeUpdate();
    }
}

@Override
public Assinatura buscarAtivaPorCliente(int clienteId) {
    String sql = "SELECT a.*, m.nome AS modelo_nome, m.descricao AS modelo_desc, m.valor AS modelo_valor, m.frequencia AS modelo_frequencia " +
                 "FROM assinatura a " +
                 "JOIN assinatura_modelo m ON a.id_modelo = m.id_modelo " +
                 "WHERE a.id_cliente = ? AND a.status = 'ATIVA' LIMIT 1";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, clienteId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return mapResultSetToAssinatura(rs); // garante que o modelo está preenchido
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return null;
}

@Override
public boolean atualizarDatasGeracao(int idAssinatura, LocalDate dataUltimaGeracao, LocalDate proximaGeracao) throws SQLException {
    String sql = "UPDATE assinatura SET data_ultima_geracao = ?, proxima_geracao = ? WHERE id = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        if (dataUltimaGeracao != null)
            stmt.setDate(1, java.sql.Date.valueOf(dataUltimaGeracao));
        else
            stmt.setNull(1, java.sql.Types.DATE);

        if (proximaGeracao != null)
            stmt.setDate(2, java.sql.Date.valueOf(proximaGeracao));
        else
            stmt.setNull(2, java.sql.Types.DATE);

        stmt.setInt(3, idAssinatura);
        return stmt.executeUpdate() > 0;
    }
}

@Override
public List<Assinatura> listarTodos() throws SQLException {
    List<Assinatura> lista = new ArrayList<>();

    String sql = "SELECT * FROM assinatura";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            Assinatura a = new Assinatura();
            a.setIdAssinatura(rs.getInt("id"));
            a.setIdCliente(rs.getInt("id_cliente"));
            a.setIdModelo(rs.getInt("id_modelo"));
            a.setPlano(rs.getString("plano"));
            a.setDataInicio(rs.getDate("data_inicio").toLocalDate());
            a.setDataFim(rs.getDate("data_fim") != null ? rs.getDate("data_fim").toLocalDate() : null);
            a.setStatus(Assinatura.Status.valueOf(rs.getString("status")));
            a.setHorarioEntrega((rs.getTime("hora_entrega") != null) ? rs.getTime("hora_entrega").toLocalTime().toString() : null);
            lista.add(a);
        }
    }

    return lista;
}

}
