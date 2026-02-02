package com.hortifacil.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PedidoAssinatura {

    private int id; // id do pedido
    private int idAssinatura; // id da assinatura
    private int idCliente; // id do cliente
    private LocalDateTime dataCriacao; // data e hora da criação do pedido
    private LocalDate dataEntrega; // data prevista de entrega
    private Status status;
    private double valorTotal;
    private List<PedidoAssinaturaItem> itens;
    private int frequenciaTotal; // total de entregas da assinatura
    private int entregaAtual;     // qual entrega está ocorrendo agora


    public enum Status {
        PENDENTE,
        ENTREGUE,
        ENVIADO,
        CANCELADO
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdPedido() {   // para compatibilidade com DAO
        return id;
    }

    public void setIdPedido(int idPedido) {  // para compatibilidade com DAO
        this.id = idPedido;
    }

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

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDate getDataEntrega() {
        return dataEntrega;
    }

    public void setDataEntrega(LocalDate dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<PedidoAssinaturaItem> getItens() {
        return itens;
    }

    public void setItens(List<PedidoAssinaturaItem> itens) {
        this.itens = itens;
    }

    public int getFrequenciaTotal() { return frequenciaTotal; }
    public void setFrequenciaTotal(int frequenciaTotal) { this.frequenciaTotal = frequenciaTotal; }

    public int getEntregaAtual() { return entregaAtual; }
    public void setEntregaAtual(int entregaAtual) { this.entregaAtual = entregaAtual; }

}
