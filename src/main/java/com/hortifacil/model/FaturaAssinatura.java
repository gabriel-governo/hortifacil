package com.hortifacil.model;

import java.time.LocalDate;

public class FaturaAssinatura {

    private int id; // id da fatura
    private int idAssinatura;
    private LocalDate dataEmissao;
    private LocalDate dataVencimento;
    private double valor;
    private Status status;
    private String formaPagamento;
    private int idCartao;

    public enum Status {
        PENDENTE,
        PAGA,
        ATRASADA
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Para compatibilidade com DAO
    public int getIdFatura() {
        return id;
    }

    public void setIdFatura(int idFatura) {
        this.id = idFatura;
    }

    public int getIdAssinatura() {
        return idAssinatura;
    }

    public void setIdAssinatura(int idAssinatura) {
        this.idAssinatura = idAssinatura;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDate dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }
    
    public int getIdCartao() {
        return idCartao;
    }

    public void setIdCartao(int idCartao) {
        this.idCartao = idCartao;
    }

}
