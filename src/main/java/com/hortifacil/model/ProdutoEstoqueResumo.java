package com.hortifacil.model;

import java.time.LocalDate;

public class ProdutoEstoqueResumo {
    private Produto produto;
    private int quantidadeTotal;
    private LocalDate dataColhido;
    private LocalDate dataValidade;
    private int lote; // apenas int

    public ProdutoEstoqueResumo(Produto produto, int quantidadeTotal,
                                LocalDate dataColhido, LocalDate dataValidade, int lote) {
        this.produto = produto;
        this.quantidadeTotal = quantidadeTotal;
        this.dataColhido = dataColhido;
        this.dataValidade = dataValidade;
        this.lote = lote;
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidadeTotal() {
        return quantidadeTotal;
    }

    public LocalDate getDataColhido() {
        return dataColhido;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public int getLote() { 
        return lote; 
    }
}
