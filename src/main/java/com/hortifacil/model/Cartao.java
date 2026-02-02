package com.hortifacil.model;

public class Cartao {
    private int id;
    private int idCliente;
    private String nomeTitular;
    private String numero;       // número original (usado só antes da criptografia)
    private String numeroCriptografado;   // número criptografado (BCrypt)
    private String validade;     // MM/AA
    private String cvv;          // usado só antes da criptografia
    private String cvvCriptografado;      // criptografado (BCrypt)
    private String bandeira;
    private String ultimosDigitos;
    private boolean ativo = true;

    // --- Getters e Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNomeTitular() {
        return nomeTitular;
    }

    public void setNomeTitular(String nomeTitular) {
        this.nomeTitular = nomeTitular;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getNumeroCriptografado() { 
        return numeroCriptografado; 
    }

    public void setNumeroCriptografado(String numeroCriptografado) {
        this.numeroCriptografado = numeroCriptografado; 
    }

    public String getValidade() {
        return validade;
    }

    public void setValidade(String validade) {
        this.validade = validade;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getCvvCriptografado() { 
        return cvvCriptografado; 
    }
    
    public void setCvvCriptografado(String cvvCriptografado) {
        this.cvvCriptografado = cvvCriptografado; 
    }

    public String getBandeira() {
        return bandeira;
    }

    public void setBandeira(String bandeira) {
        this.bandeira = bandeira;
    }

    public String getUltimosDigitos() {
        return ultimosDigitos;
    }

    public void setUltimosDigitos(String ultimosDigitos) {
        this.ultimosDigitos = ultimosDigitos;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    @Override
    public String toString() {
        return "****" + ultimosDigitos + " - " + bandeira;
    }

    public String getUltimos4() {
        if (numero != null && numero.length() >= 4) {
            return numero.substring(numero.length() - 4);
        }
        return "";
    }

}