package com.hortifacil.model;

public class Avaliacao {
    private int id;
    private int clienteId;
    private int produtoId;
    private int estrelas; // 0 a 5
    private String comentario;

    public Avaliacao(int id, int clienteId, int produtoId, int estrelas, String comentario) {
        this.id = id;
        this.clienteId = clienteId;
        this.produtoId = produtoId;
        this.estrelas = estrelas;
        this.comentario = comentario;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public int getProdutoId() { return produtoId; }
    public void setProdutoId(int produtoId) { this.produtoId = produtoId; }
    public int getEstrelas() { return estrelas; }
    public void setEstrelas(int estrelas) { this.estrelas = estrelas; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}
