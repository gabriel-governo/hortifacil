package com.hortifacil.model;

import java.time.LocalDateTime;

public class CarrinhoAssinatura {

    private int idCarrinhoAssinatura; // PK da tabela carrinho_assinatura
    private int idCarrinho;           // FK para carrinho
    private int idAssinatura;         // FK para assinatura
    private double preco;             // preço da assinatura no carrinho
    private LocalDateTime dataAdicao; // data e hora que foi adicionada ao carrinho

    // Getters e Setters
    public int getIdCarrinhoAssinatura() {
        return idCarrinhoAssinatura;
    }

    public void setIdCarrinhoAssinatura(int idCarrinhoAssinatura) {
        this.idCarrinhoAssinatura = idCarrinhoAssinatura;
    }

    public int getIdCarrinho() {
        return idCarrinho;
    }

    public void setIdCarrinho(int idCarrinho) {
        this.idCarrinho = idCarrinho;
    }

    public int getIdAssinatura() {
        return idAssinatura;
    }

    public void setIdAssinatura(int idAssinatura) {
        this.idAssinatura = idAssinatura;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public LocalDateTime getDataAdicao() {
        return dataAdicao;
    }

    public void setDataAdicao(LocalDateTime dataAdicao) {
        this.dataAdicao = dataAdicao;
    }
}
