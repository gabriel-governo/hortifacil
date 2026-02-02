package com.hortifacil.service;

import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.model.Produto;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProdutoService {

    private ProdutoDAO produtoDAO;

    public ProdutoService(ProdutoDAO produtoDAO) {
        this.produtoDAO = produtoDAO;
    }

    // Cadastra um produto e retorna o ID
    public int cadastrarProduto(Produto produto) throws SQLException {
    // Use o método "salvar" do DAO
    return produtoDAO.salvar(produto);
    }

    // Lista todos os produtos
    public List<Produto> listarTodos() throws SQLException {
        return produtoDAO.listarTodos();
    }

    // Atualiza um produto existente
    public void atualizarProduto(Produto produto) throws SQLException {
        produtoDAO.atualizar(produto);
    }

    // Remove um produto pelo ID
    public void removerProduto(int id) throws SQLException {
        produtoDAO.remover(id);
    }

    // Busca por nome
    public Optional<Produto> buscarPorNome(String nome) throws SQLException {
        return Optional.ofNullable(produtoDAO.buscarPorNome(nome));
    }

    public int calcularMediaEstrelas(int idProduto) {
    try {
        List<Integer> estrelasList = produtoDAO.listarEstrelasPorProduto(idProduto); // DAO retorna todas as avaliações
        if (estrelasList.isEmpty()) return 0;

        double media = estrelasList.stream().mapToInt(Integer::intValue).average().orElse(0);
        return (int) Math.round(media); // arredonda para inteiro
    } catch (SQLException e) {
        e.printStackTrace();
        return 0;
    }
}

}
