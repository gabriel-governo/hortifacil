package com.hortifacil.model;

import javafx.beans.property.*;

public class PedidoAssinaturaItem {

    private int idItem;
    private int idProduto;
    private int idPedido;
    private String unidade;


    private ProdutoEstoque produtoEstoque;
    private IntegerProperty quantidade = new SimpleIntegerProperty();

    // ================= Construtores =================
    public PedidoAssinaturaItem() {
        // Construtor vazio para uso pelo DAO
    }

    public PedidoAssinaturaItem(String nomeProduto, int quantidade, double precoUnitario) {
        this.quantidade.set(quantidade);

        Produto produto = new Produto();
        produto.setNome(nomeProduto);
        produto.setPreco(precoUnitario); // 👈 aqui usamos o preço direto no Produto

        ProdutoEstoque pe = new ProdutoEstoque();
        pe.setProduto(produto);

        this.produtoEstoque = pe;
    }


    public PedidoAssinaturaItem(ProdutoEstoque produtoEstoque, int quantidade) {
        this.produtoEstoque = produtoEstoque;
        this.quantidade.set(quantidade);

        if (produtoEstoque != null && produtoEstoque.getProduto() != null) {
            this.idProduto = produtoEstoque.getProduto().getId();

            if (produtoEstoque.getProduto().getUnidade() != null) {
                this.unidade = produtoEstoque.getProduto().getUnidade().getNome();
            }
        }
    }

    // ================= Propriedades para TableView =================
    public StringProperty nomeProdutoProperty() {
        String nome = (produtoEstoque != null && produtoEstoque.getProduto() != null)
                ? produtoEstoque.getProduto().getNome()
                : "Produto";
        return new SimpleStringProperty(nome);
    }

    public IntegerProperty quantidadeProperty() {
        return quantidade;
    }

    public DoubleProperty precoUnitarioProperty() {
        double preco = (produtoEstoque != null) ? produtoEstoque.getPrecoComDesconto() : 0.0;
        return new SimpleDoubleProperty(preco);
    }

    public DoubleProperty subtotalProperty() {
        double subtotal = (produtoEstoque != null)
                ? produtoEstoque.getPrecoComDesconto() * quantidade.get()
                : 0.0;
        return new SimpleDoubleProperty(subtotal);
    }

    public StringProperty unidadeProperty() {
        String unidadeStr = (unidade != null) ? unidade : "";
        return new SimpleStringProperty(unidadeStr);
    }

    // ================= Getters e Setters =================
    public int getIdItem() {
        return idItem;
    }

    public void setIdItem(int idItem) {
        this.idItem = idItem;
    }

    public int getIdProduto() {
        if (produtoEstoque != null && produtoEstoque.getProduto() != null) {
            return produtoEstoque.getProduto().getId();
        }
        return idProduto;
    }

    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;

        // Garante que produtoEstoque e Produto existam
        if (produtoEstoque == null) produtoEstoque = new ProdutoEstoque();
        if (produtoEstoque.getProduto() == null) produtoEstoque.setProduto(new Produto());

        produtoEstoque.getProduto().setId(idProduto);
    }

    public String getUnidade() {
        if (unidade != null) {
            return unidade;
        }
        if (produtoEstoque != null &&
            produtoEstoque.getProduto() != null &&
            produtoEstoque.getProduto().getUnidade() != null) {
            return produtoEstoque.getProduto().getUnidade().getNome();
        }
        return "";
    }

    public void setUnidade(String unidadeNome) {
        this.unidade = unidadeNome; // salva localmente também

        // Evita NullPointerException garantindo que os objetos existam
        if (produtoEstoque == null) produtoEstoque = new ProdutoEstoque();
        if (produtoEstoque.getProduto() == null) produtoEstoque.setProduto(new Produto());
        if (produtoEstoque.getProduto().getUnidade() == null)
            produtoEstoque.getProduto().setUnidade(new UnidadeMedida(0, unidadeNome));
        else
            produtoEstoque.getProduto().getUnidade().setNome(unidadeNome);
    }

    public int getQuantidade() {
        return quantidade.get();
    }

    public void setQuantidade(int quantidade) {
        this.quantidade.set(quantidade);
    }

    public ProdutoEstoque getProdutoEstoque() {
        return produtoEstoque;
    }

    public void setProdutoEstoque(ProdutoEstoque produtoEstoque) {
        this.produtoEstoque = produtoEstoque;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }
}
