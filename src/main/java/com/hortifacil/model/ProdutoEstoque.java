package com.hortifacil.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ProdutoEstoque {
    private int id;
    private Produto produto;
    private int quantidade;
    private LocalDate dataColhido;
    private LocalDate dataValidade;
    private int diasParaVencer;       // Total de dias de validade
    private int diaInicioDesconto;    // Dias antes do vencimento que começa o desconto
    private int lote;
    private int idProduto;

// Construtor principal
public ProdutoEstoque(int id, Produto produto, int quantidade, LocalDate dataColhido, LocalDate dataValidade, int lote) {
    this.id = id;
    this.produto = produto;
    this.quantidade = quantidade;
    this.dataColhido = (dataColhido != null) ? dataColhido : LocalDate.now();
    this.dataValidade = (dataValidade != null) ? dataValidade : calcularValidadeDefault();
    this.lote = lote;
    inicializarDiasEDesconto();
}

// Construtor sem id (quando ainda não foi salvo no banco)
public ProdutoEstoque(Produto produto, int quantidade, LocalDate dataColhido, LocalDate dataValidade, int lote) {
    this(0, produto, quantidade, dataColhido, dataValidade, lote);
}

// Construtor rápido (somente produto e quantidade)
public ProdutoEstoque(Produto produto, int quantidade) {
    this(0, produto, quantidade, LocalDate.now(), null, 0);
}

// Construtor com 5 parâmetros, para facilitar DAO
public ProdutoEstoque(int id, Produto produto, int quantidade, LocalDate dataColhido, LocalDate dataValidade) {
    this(id, produto, quantidade, dataColhido, dataValidade, 0); // lote = 0 por padrão
}


    public ProdutoEstoque() {}

    // =================== MÉTODOS AUXILIARES ===================
    private void inicializarDiasEDesconto() {
    if (produto != null) {
        this.diasParaVencer = produto.getDiasParaVencer();
        this.diaInicioDesconto = produto.getDescontoInicio();
        // garantir que desconto diário não seja 0
        if (produto.getDescontoDiario() <= 0) {
            produto.setDescontoDiario(0.1); // ou outro valor padrão para Food To Save
        }
    } else {
        this.diasParaVencer = 7; // padrão
        this.diaInicioDesconto = 2; // exemplo
    }
}


    private LocalDate calcularValidadeDefault() {
        if (produto != null && produto.getDiasParaVencer() > 0) {
            return dataColhido.plusDays(produto.getDiasParaVencer());
        } else {
            return dataColhido.plusDays(7);
        }
    }

    // =================== GETTERS E SETTERS ===================
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { 
        this.produto = produto; 
        inicializarDiasEDesconto();
    }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public LocalDate getDataColhido() { return dataColhido; }
    public void setDataColhido(LocalDate dataColhido) { this.dataColhido = dataColhido; }

    public LocalDate getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; }

    public int getDiasParaVencer() { return diasParaVencer; }
    public int getDiaInicioDesconto() { return diaInicioDesconto; }

    public int getLote() { return lote; }
    public void setLote(int lote) { this.lote = lote; }

    public int getIdProduto() { return idProduto; }
    public void setIdProduto(int idProduto) {this.idProduto = idProduto; }

   // =================== CÁLCULOS DE DESCONTO ===================
    public double getDesconto() {
        if (produto == null || dataColhido == null || dataValidade == null) return 0.0;

        long diasPassados = ChronoUnit.DAYS.between(dataColhido, LocalDate.now());

        // Aplica desconto apenas a partir do dia de início do desconto
        if (diasPassados >= diaInicioDesconto) {
            long diasComDesconto = diasPassados - diaInicioDesconto + 1; // inclui o dia atual
            double descontoCalculado = diasComDesconto * produto.getDescontoDiario();
            return Math.min(descontoCalculado, 1.0); // máximo 100%
        }

        return 0.0; // ainda não começou o desconto
    }

    public double getPrecoComDesconto() {
        if (produto == null) return 0.0;
        double desconto = getDesconto();
        return produto.getPreco() * (1 - desconto);
    }


    public int getPercentualDesconto() {
        return (int) Math.round(getDesconto() * 100);
    }

    @Override
    public String toString() {
        String nomeProduto = (produto != null) ? produto.getNome() : "Produto";
        String unidade = (produto != null && produto.getUnidade() != null) ? produto.getUnidade().getNome() : "";
        String preco = (produto != null) ? String.format("R$%.2f", produto.getPreco()) : "R$0.00";
        String colhido = (dataColhido != null) ? dataColhido.toString() : "N/A";
        String validade = (dataValidade != null) ? dataValidade.toString() : "N/A";

        return nomeProduto + " - Qtde: " + quantidade + " " + unidade +
               " - Colhido: " + colhido +
               " - Vence: " + validade +
               " - Preço: " + preco +
               " - Desconto: " + getPercentualDesconto() + "%";
    }
    
}