package com.hortifacil.dao;

import com.hortifacil.model.Cartao;
import java.sql.SQLException;
import java.util.List;

public interface CartaoDAO {
    
    List<Cartao> buscarPorCliente(int clienteId) throws SQLException;

    void inserir(Cartao cartao) throws SQLException;

    boolean remover(int idCartao) throws SQLException;

}
