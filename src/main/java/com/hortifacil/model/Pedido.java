package com.hortifacil.model;

import java.time.LocalDate;
import java.util.List;

public class Pedido {

    private int idPedido;
    private int idCliente;
    private LocalDate dataPedido;
    private double total;
    private String status; // "EM_ANDAMENTO", "CANCELADO", "FINALIZADO"
    private boolean ativo;
    private String metodoPagamento;
    private double valorTotal; // ou calcule dinamicamente a partir dos itens do pedido

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
}

    private List<CarrinhoProduto> itens;

    public Pedido() {}

    public Pedido(int idCliente, LocalDate dataPedido, double total, String status, boolean ativo) {
        this.idCliente = idCliente;
        this.dataPedido = dataPedido;
        this.total = total;
        this.status = status;
        this.ativo = ativo;
    }

    public Pedido(int idPedido, int idCliente, LocalDate dataPedido, double total, String status, boolean ativo, List<CarrinhoProduto> itens) {
        this.idPedido = idPedido;
        this.idCliente = idCliente;
        this.dataPedido = dataPedido;
        this.total = total;
        this.status = status;
        this.ativo = ativo;
        this.itens = itens;
    }

    // getters e setters

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public LocalDate getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(LocalDate dataPedido) {
        this.dataPedido = dataPedido;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public List<CarrinhoProduto> getItens() {
        return itens;
    }

    public void setItens(List<CarrinhoProduto> itens) {
        this.itens = itens;
    }

    public String getMetodoPagamento() {
    return metodoPagamento;
    }

    public void setMetodoPagamento(String metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }


    @Override
    public String toString() {
        return "Pedido{" +
                "idPedido=" + idPedido +
                ", idCliente=" + idCliente +
                ", dataPedido=" + dataPedido +
                ", total=" + total +
                ", status='" + status + '\'' +
                ", ativo=" + ativo +
                ", itens=" + (itens != null ? itens.size() + " itens" : "nenhum item") +
                '}';
    }
}