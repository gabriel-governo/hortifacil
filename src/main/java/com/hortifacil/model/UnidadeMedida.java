package com.hortifacil.model;

public class UnidadeMedida {
    private int id;
    private String nome;

    public UnidadeMedida(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public UnidadeMedida() {
        // Construtor vazio, útil para frameworks e DAOs
    }

    public int getId() { 
        return id; 
    }

    public void setId(int id) { 
        this.id = id; 
    }

    public String getNome() { 
        return nome; 
    }

    public void setNome(String nome) { 
        this.nome = nome; 
    }

    @Override
    public String toString() {
        return nome;
    }
}

