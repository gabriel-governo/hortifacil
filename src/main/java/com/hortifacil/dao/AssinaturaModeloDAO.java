package com.hortifacil.dao;

import com.hortifacil.model.AssinaturaModelo;
import java.util.List;

public interface AssinaturaModeloDAO {
    List<AssinaturaModelo> listarTodos();
    boolean criar(AssinaturaModelo modelo);
    boolean atualizar(AssinaturaModelo modelo);
    boolean excluir(int idModelo);
    AssinaturaModelo buscarPorId(int idModelo);
}
