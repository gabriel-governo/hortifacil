package com.hortifacil.dao;

import com.hortifacil.model.AssinaturaProduto;
import java.util.List;

public interface AssinaturaProdutoDAO {

    // Insere um produto em uma assinatura
    boolean criarAssinaturaProduto(AssinaturaProduto ap);

    // Lista todos os produtos de uma assinatura
    List<AssinaturaProduto> listarPorAssinatura(int idAssinatura);

    // Remove todos os produtos de uma assinatura
    void removerPorAssinatura(int idAssinatura);
}
