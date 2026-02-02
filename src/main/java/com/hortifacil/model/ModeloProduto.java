package com.hortifacil.model;

public class ModeloProduto {

    private int idModeloProduto;
    private int idModelo;
    private int idProduto;
    private int quantidade;

    private Produto produto; // referência opcional

    // Getters e Setters
    public int getIdModeloProduto() { return idModeloProduto; }
    public void setIdModeloProduto(int idModeloProduto) { this.idModeloProduto = idModeloProduto; }

    public int getIdModelo() { return idModelo; }
    public void setIdModelo(int idModelo) { this.idModelo = idModelo; }

    public int getIdProduto() { return idProduto; }
    public void setIdProduto(int idProduto) { this.idProduto = idProduto; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
}

