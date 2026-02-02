package com.hortifacil.model;

public class CarrinhoProduto {
    private int id;
    private int clienteId;
    private Produto produto;
    private int quantidade;
    private double precoUnitario;
    private boolean isFoodToSave;
    private AssinaturaModelo assinatura;


    public boolean isFoodToSave() {
        return isFoodToSave;
    }

    public boolean isAssinatura() {
        return assinatura != null;
    }
    
    public CarrinhoProduto() {
    }

public CarrinhoProduto(int id, int clienteId, Produto produto, int quantidade, double precoUnitario, boolean isFoodToSave) {
    this.id = id;
    this.clienteId = clienteId;
    this.produto = produto;
    this.quantidade = quantidade;
    this.precoUnitario = precoUnitario;
    this.isFoodToSave = isFoodToSave;
}

public CarrinhoProduto(int clienteId, Produto produto, int quantidade, double precoUnitario, boolean isFoodToSave) {
    this.clienteId = clienteId;
    this.produto = produto;
    this.quantidade = quantidade;
    this.precoUnitario = precoUnitario;
    this.isFoodToSave = isFoodToSave;
}

public CarrinhoProduto(int id, int clienteId, Produto produto, int quantidade, double precoUnitario) {
    this.id = id;
    this.clienteId = clienteId;
    this.produto = produto;
    this.quantidade = quantidade;
    this.precoUnitario = precoUnitario;
}

public CarrinhoProduto(int clienteId, Produto produto, int quantidade, double precoUnitario) {
        this(0, clienteId, produto, quantidade, precoUnitario);
    }

public CarrinhoProduto(int clienteId, AssinaturaModelo assinatura, int quantidade) {
    this.clienteId = clienteId;
    this.assinatura = assinatura;
    this.quantidade = quantidade;
    this.precoUnitario = assinatura.getValor(); // pega o valor da assinatura
}


public String getNomeExibicao() {
    if (assinatura != null) {
        return "Assinatura: " + assinatura.getNome();
    } else if (produto != null) {
        return produto.getNome();
    } else {
        return "Item";
    }
}

    // Getters e Setters
    public int getId() { return id; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) {this.clienteId = clienteId;}
    public Produto getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }
    public double getPrecoUnitario() { return precoUnitario; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public void setPrecoUnitario(double precoUnitario) { this.precoUnitario = precoUnitario; }
    public double getTotal() {return quantidade * precoUnitario; }
    public void setFoodToSave(boolean isFoodToSave) { this.isFoodToSave = isFoodToSave; }
    public AssinaturaModelo getAssinatura() { return assinatura; }
    public void setAssinatura(AssinaturaModelo assinatura) { this.assinatura = assinatura; }

}
