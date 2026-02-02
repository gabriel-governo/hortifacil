package com.hortifacil.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Produto {
    private int id;
    private String nome;
    private double preco;
    private String caminhoImagem;
    private String descricao;
    private UnidadeMedida unidade;
    private int diasParaVencer;      // total de dias de validade após a colheita
    private int descontoInicio;      // quantos dias antes do vencimento começa o desconto
    private double descontoDiario;  
    private double avaliacao; // 0.0 a 5.0

// Construtor completo
public Produto(int id, String nome, double preco, String caminhoImagem, String descricao, UnidadeMedida unidade,
               int diasParaVencer, int descontoInicio, double descontoDiario) {
    this.id = id;
    this.nome = nome;
    this.preco = preco;
    this.caminhoImagem = caminhoImagem;
    this.descricao = descricao;
    this.unidade = unidade;
    this.diasParaVencer = diasParaVencer;
    this.descontoInicio = descontoInicio;
    this.descontoDiario = descontoDiario;
}

// Construtor “reduzido” já usando valores padrão
public Produto(int id, String nome, double preco, String caminhoImagem, String descricao, UnidadeMedida unidade) {
    this(id, nome, preco, caminhoImagem, descricao, unidade, 0, 0, 0.0);
}

// Construtor que só seta diasParaVencer (mantendo compatibilidade)
public Produto(int id, String nome, double preco, String caminhoImagem, String descricao, UnidadeMedida unidade, int diasParaVencer) {
    this(id, nome, preco, caminhoImagem, descricao, unidade, diasParaVencer, 0, 0.0);
}

    public Produto() {}

    @Override
    public String toString() {
        return nome;
    }

    // =================== GETTERS E SETTERS ===================
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    public String getCaminhoImagem() { return caminhoImagem; }
    public void setCaminhoImagem(String caminhoImagem) { this.caminhoImagem = caminhoImagem; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public UnidadeMedida getUnidade() { return unidade; }
    public String getNomeUnidade() { return unidade != null ? unidade.getNome() : ""; }
    public void setUnidade(UnidadeMedida unidade) { this.unidade = unidade; }

    // =================== GETTERS E SETTERS NOVOS CAMPOS ===================
    public int getDiasParaVencer() { return diasParaVencer; }
    public void setDiasParaVencer(int diasParaVencer) { this.diasParaVencer = diasParaVencer; }

    public int getDescontoInicio() { return descontoInicio; }
    public void setDescontoInicio(int descontoInicio) { this.descontoInicio = descontoInicio; }

    public double getDescontoDiario() { return descontoDiario; }
    public void setDescontoDiario(double descontoDiario) { this.descontoDiario = descontoDiario; }

    public double getAvaliacao() { return avaliacao; }
    public void setAvaliacao(double avaliacao) { this.avaliacao = avaliacao; } // % de desconto aplicado por dia

    public double calcularPrecoComDesconto(ProdutoEstoque estoque) {
    LocalDate hoje = LocalDate.now();
    LocalDate dataValidade = estoque.getDataValidade();

    // calcula quantos dias faltam para vencer
    long diasParaVencer = ChronoUnit.DAYS.between(hoje, dataValidade);

    // se ainda não chegou no período de desconto, preço é normal
    if (diasParaVencer > descontoInicio) {
        return preco;
    }

    // caso esteja no período de desconto
    long diasDeDesconto = descontoInicio - diasParaVencer; // quantos dias já estamos no desconto
    double descontoTotal = diasDeDesconto * descontoDiario;

    // garante que não passa de 100% do preço
    if (descontoTotal > preco) descontoTotal = preco;

    double precoFinal = preco - descontoTotal;
    return precoFinal;
}
}
