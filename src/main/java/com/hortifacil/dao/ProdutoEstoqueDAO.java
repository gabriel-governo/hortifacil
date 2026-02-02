package com.hortifacil.dao;

import com.hortifacil.model.ProdutoEstoque;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface ProdutoEstoqueDAO {

    int adicionarLote(ProdutoEstoque lote) throws SQLException;

    boolean salvar(ProdutoEstoque produtoEstoque) throws SQLException;

    boolean atualizarQuantidadeLote(int idLote, int novaQuantidade) throws SQLException;

    boolean removerLote(int idLote) throws SQLException;

    List<ProdutoEstoque> listarTodos() throws SQLException;

    List<ProdutoEstoque> listarLotesPorProduto(int produtoId) throws SQLException;

    List<ProdutoEstoque> buscarPorNomeProduto(String nomeProduto) throws SQLException;

    ProdutoEstoque buscarPorProdutoData(int produtoId, LocalDate dataColhido, LocalDate dataValidade) throws SQLException;

    int getQuantidadeEstoquePorNome(String nomeProduto) throws SQLException;

    List<ProdutoEstoque> buscarLotesPorProdutoOrdenados(int idProduto) throws SQLException;

    int somarQuantidadePorProduto(int idProduto) throws SQLException;

    int buscarUltimoLote(int idProduto) throws SQLException;

    ProdutoEstoque buscarPorId(int idLote) throws SQLException;

    int buscarQuantidade(int idProduto) throws SQLException;

    boolean atualizarQuantidade(int idProduto, int novaQuantidade) throws SQLException;

}
