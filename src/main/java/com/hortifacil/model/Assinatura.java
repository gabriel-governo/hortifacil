package com.hortifacil.model;

import java.time.LocalDate;

public class Assinatura {
    private int idAssinatura;
    private int idCliente;
    private int idModelo;             // referência ao modelo de assinatura do admin
    private AssinaturaModelo modelo;  // objeto modelo para acessar tipo, valor, frequencia, etc.
    private LocalDate dataInicio;
    private LocalDate dataFim;        // se for cancelada
    private Status status;            // Enum
    private String formaPagamento;    // PIX, Dinheiro, Cartão
    private String HorarioEntrega;    // Ex: "12:00"
    private String plano;             // EX: "mensal"
    private int idCartao; 
    private LocalDate dataUltimaGeracao;
    private LocalDate proximaGeracao;



    public enum Status {
        ATIVA,
        CANCELADA,
        PAUSADA
    }

    // ---- Getters e Setters ----
    public int getIdAssinatura() {
        return idAssinatura;
    }

    public void setIdAssinatura(int idAssinatura) {
        this.idAssinatura = idAssinatura;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public int getIdModelo() {
        return idModelo;
    }

    public void setIdModelo(int idModelo) {
        this.idModelo = idModelo;
    }

    public AssinaturaModelo getModelo() {
        return modelo;
    }

    public void setModelo(AssinaturaModelo modelo) {
        this.modelo = modelo;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
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

    public String getHorarioEntrega() {
        return HorarioEntrega;
    }

    public void setHorarioEntrega(String HorarioEntrega) {
        this.HorarioEntrega = HorarioEntrega;
    } 

    public String getPlano(){
    return plano;
    }
    
    public void setPlano(String plano) {
        this.plano = plano;
    }

    public int getIdCartao() {
    return idCartao;
    }

    public void setIdCartao(int idCartao) {
        this.idCartao = idCartao;
    }

    public LocalDate getDataUltimaGeracao() { 
        return dataUltimaGeracao; 
    }

    public void setDataUltimaGeracao(LocalDate dataUltimaGeracao) {
         this.dataUltimaGeracao = dataUltimaGeracao; 
    }

    public LocalDate getProximaGeracao() { 
        return proximaGeracao; 
    }

    public void setProximaGeracao(LocalDate proximaGeracao) {
         this.proximaGeracao = proximaGeracao; 
    }
  
}