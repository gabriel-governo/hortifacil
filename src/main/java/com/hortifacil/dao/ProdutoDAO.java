package com.hortifacil.dao;

import com.hortifacil.model.Produto;

import java.sql.SQLException;
import java.util.List;

public interface ProdutoDAO {
    Produto buscarPorId(int id);
    Produto buscarPorNome(String nome);
    List<Produto> listarTodos();
    boolean atualizar(Produto produto);
    boolean remover(int id);
    int salvar(Produto produto);
    public List<Integer> listarEstrelasPorProduto(int idProduto) throws SQLException;
    int buscarQuantidade(int idProduto);
    boolean atualizarQuantidade(int idProduto, int novaQtd);
    public List<Produto> buscarProdutosDisponiveis() throws SQLException;
}
