package com.hortifacil.dao;

import com.hortifacil.model.CarrinhoProduto;
import java.sql.SQLException;
import java.util.List;

public interface CarrinhoProdutoDAO {

    List<CarrinhoProduto> listarPorCliente(int clienteId) throws SQLException;

    boolean limparCarrinhoDoCliente(int clienteId) throws SQLException;

    boolean atualizarQuantidade(int clienteId, int produtoId, int quantidade) throws SQLException;

    boolean atualizarQuantidade(int clienteId, int produtoId, int quantidade, boolean isFoodToSave) throws SQLException;

    boolean removerItem(int clienteId, int produtoId) throws SQLException;

    List<CarrinhoProduto> listarPorPedido(int pedidoId) throws SQLException;

    void adicionarAoCarrinho(CarrinhoProduto item) throws SQLException;

    boolean removerItem(int clienteId, int produtoId, boolean isFoodToSave);

}
