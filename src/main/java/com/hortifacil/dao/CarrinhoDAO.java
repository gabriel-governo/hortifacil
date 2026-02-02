package com.hortifacil.dao;

import com.hortifacil.database.DatabaseConnection;
import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarrinhoDAO {

    // Adiciona um item no carrinho (ou atualiza quantidade se já existir)
    public boolean adicionarOuAtualizarItem(int idCliente, CarrinhoProduto item, boolean somarQuantidade) {
        String sqlCheck = "SELECT quantidade FROM carrinho_produto WHERE id_cliente = ? AND id_produto = ?";
        String sqlInsert = "INSERT INTO carrinho_produto (id_cliente, id_produto, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";
        String sqlUpdate = "UPDATE carrinho_produto SET quantidade = ?, preco_unitario = ? WHERE id_cliente = ? AND id_produto = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmtCheck = conn.prepareStatement(sqlCheck)) {

            stmtCheck.setInt(1, idCliente);
            stmtCheck.setInt(2, item.getProduto().getId());

            ResultSet rs = stmtCheck.executeQuery();

            if (rs.next()) {
                // Item existe, atualizar quantidade
                int quantidadeAtual = rs.getInt("quantidade");
                int novaQuantidade = somarQuantidade
                        ? quantidadeAtual + item.getQuantidade() // soma ao existente
                        : item.getQuantidade();                  // substitui

                try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate)) {
                    stmtUpdate.setInt(1, novaQuantidade);
                    stmtUpdate.setDouble(2, item.getPrecoUnitario());
                    stmtUpdate.setInt(3, idCliente);
                    stmtUpdate.setInt(4, item.getProduto().getId());
                    stmtUpdate.executeUpdate();
                }

            } else {
                // Item não existe, inserir novo
                try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                    stmtInsert.setInt(1, idCliente);
                    stmtInsert.setInt(2, item.getProduto().getId());
                    stmtInsert.setInt(3, item.getQuantidade());
                    stmtInsert.setDouble(4, item.getPrecoUnitario());
                    stmtInsert.executeUpdate();
                }
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Remove um item do carrinho
    public boolean removerItem(int idCliente, int idProduto) {
        String sql = "DELETE FROM carrinho_produto WHERE id_cliente = ? AND id_produto = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCliente);
            stmt.setInt(2, idProduto);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Lista todos os itens do carrinho de um cliente
    public List<CarrinhoProduto> listarItensCarrinho(int idCliente) {
        List<CarrinhoProduto> itens = new ArrayList<>();

       String sql = "SELECT cp.id_produto, cp.quantidade, cp.preco_unitario as preco_carrinho, " +
             "p.nome, p.descricao, p.preco_unitario as preco_produto, p.caminho_imagem " +
             "FROM carrinho_produto cp " +
             "JOIN produto p ON cp.id_produto = p.id " +
             "WHERE cp.id_cliente = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCliente);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Cria o objeto Produto
                Produto produto = new Produto(
                rs.getInt("id_produto"),
                rs.getString("nome"),
               rs.getDouble("preco_produto"),
                rs.getString("caminho_imagem"),
                rs.getString("descricao"),
                null
            );

            CarrinhoProduto item = new CarrinhoProduto(
                idCliente,
                produto,
                rs.getInt("quantidade"),
                rs.getDouble("preco_unitario")
            );

                itens.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return itens;
    }

    // Limpa o carrinho do cliente (deleta todos os itens)
    public boolean limparCarrinho(int idCliente) {
        String sql = "DELETE FROM carrinho_produto WHERE id_cliente = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCliente);
            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
}
