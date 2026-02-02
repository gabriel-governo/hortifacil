package com.hortifacil.dao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.hortifacil.model.ProdutoEstoque;

public interface RelatorioDAO {
    // Produtos
    List<ProdutoEstoque> listarProdutosVencidos() throws SQLException;
    Map<String, Integer> produtosMaisVendidos() throws SQLException;
    void descartarProduto(int idProdutoEstoque) throws SQLException;
    public List<ProdutoEstoque> listarProdutosBaixoEstoque() throws SQLException;
    public List<ProdutoEstoque> listarProdutosProximosVencimento() throws SQLException;

    // Financeiro
    double calcularLucroPorPeriodo(LocalDate inicio, LocalDate fim) throws SQLException;
    double calcularTicketMedio(LocalDate inicio, LocalDate fim) throws SQLException;

    // Usuários e assinaturas
    int contarUsuarios() throws SQLException;
    int contarAssinaturasAtivas() throws SQLException;
    int contarAssinaturasInativas() throws SQLException;

    // Pedidos
    int contarPedidosPorPeriodo(LocalDate inicio, LocalDate fim) throws SQLException;
    Map<String, Integer> pedidosPorStatus() throws SQLException;

    // Clientes
    Map<String, Integer> clientesMaisAtivos(int limite) throws SQLException;
}

