package com.hortifacil.dao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import com.hortifacil.model.Assinatura;
import com.hortifacil.model.AssinaturaModelo;

public interface AssinaturaDAO {
    
    // Cria assinatura e retorna true se deu certo
    boolean criarAssinatura(Assinatura assinatura);

    // Busca assinatura ativa do cliente
    Assinatura buscarAtivaPorCliente(int clienteId);

    // Lista todas as assinaturas (ativas ou não) de um cliente
    List<Assinatura> listarAssinaturasPorCliente(int clienteId);

    // Cancela assinatura
    boolean cancelarAssinatura(int idAssinatura);

    List<AssinaturaModelo> listarModelos();
    
   public boolean vincularClienteAPlano(int clienteId, AssinaturaModelo modelo, String horarioEntrega);
    
    boolean alterarPagamento(int idAssinatura, String formaPagamento) throws SQLException;

    boolean alterarHorarioEntrega(int idAssinatura, String horarioEntrega) throws SQLException;

    int contarUsuariosPorAssinatura(int idAssinatura);   

    List<Assinatura> listarAtivas();

    int contarClientesPorModelo(int idModelo);

    Assinatura buscarPorId(int idAssinatura);

    public boolean cancelarAssinaturasAtivasPorCliente(int idCliente);

    boolean ativarAssinatura(int idCliente, int id);

    void desativarAssinaturasAtivas(int idCliente);

    public void atualizarStatusEPeriodo(Assinatura assinatura) throws SQLException;
    
    boolean atualizarDatasGeracao(int idAssinatura, LocalDate dataUltimaGeracao, LocalDate proximaGeracao) throws SQLException;

    List<Assinatura> listarTodos() throws SQLException;

}
