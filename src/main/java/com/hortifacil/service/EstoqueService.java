package com.hortifacil.service;

import com.hortifacil.dao.ProdutoDAO;
import com.hortifacil.dao.ProdutoEstoqueDAO;
import com.hortifacil.model.CarrinhoProduto;
import com.hortifacil.model.Produto;
import com.hortifacil.model.ProdutoEstoque;
import com.hortifacil.model.ProdutoEstoqueResumo;
import com.hortifacil.model.ProdutoQuantidadeTotal;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class EstoqueService {

    private static EstoqueService instance;

    private final ProdutoEstoqueDAO produtoEstoqueDAO;
    private final ProdutoDAO produtoDAO;

    private EstoqueService(ProdutoDAO produtoDAO, ProdutoEstoqueDAO produtoEstoqueDAO) {
        this.produtoDAO = produtoDAO;
        this.produtoEstoqueDAO = produtoEstoqueDAO;
    }

    public static EstoqueService getInstance(ProdutoDAO produtoDAO, ProdutoEstoqueDAO produtoEstoqueDAO) {
        if (instance == null) {
            instance = new EstoqueService(produtoDAO, produtoEstoqueDAO);
        }
        return instance;
    }

    public void adicionarProduto(ProdutoEstoque novoProduto) throws SQLException {
        ProdutoEstoque loteExistente = produtoEstoqueDAO.buscarPorProdutoData(
            novoProduto.getProduto().getId(),
            novoProduto.getDataColhido(),
            novoProduto.getDataValidade()
        );

        if (loteExistente != null) {
            int novaQuantidade = loteExistente.getQuantidade() + novoProduto.getQuantidade();
            loteExistente.setQuantidade(novaQuantidade);
            produtoEstoqueDAO.atualizarQuantidadeLote(loteExistente.getId(), novaQuantidade);
        } else {
            produtoEstoqueDAO.salvar(novoProduto);
        }
    }

    public List<ProdutoEstoque> listarEstoque() throws SQLException {
        return produtoEstoqueDAO.listarTodos();
    }

    public List<ProdutoEstoque> listarProdutosComQuantidadeTotal() throws SQLException {
        List<ProdutoEstoque> todosLotes = produtoEstoqueDAO.listarTodos();
        Map<Integer, ProdutoEstoque> agrupado = new HashMap<>();

        for (ProdutoEstoque lote : todosLotes) {
            int produtoId = lote.getProduto().getId();

            if (agrupado.containsKey(produtoId)) {
                ProdutoEstoque existente = agrupado.get(produtoId);
                existente.setQuantidade(existente.getQuantidade() + lote.getQuantidade());
            } else {
                agrupado.put(produtoId, new ProdutoEstoque(
                    lote.getProduto(),
                    lote.getQuantidade()
                ));
            }
        }

        return new ArrayList<>(agrupado.values());
    }

    public int buscarProdutoIdPorNome(String nomeProduto) throws SQLException {
        Produto produto = produtoDAO.buscarPorNome(nomeProduto);
        return (produto != null) ? produto.getId() : -1;
    }

    public ProdutoEstoque buscarProdutoEstoquePorNome(String nomeProduto) throws SQLException {
        int produtoId = buscarProdutoIdPorNome(nomeProduto);
        if (produtoId == -1) return null;

        List<ProdutoEstoque> lotes = produtoEstoqueDAO.listarLotesPorProduto(produtoId);
        if (lotes != null && !lotes.isEmpty()) {
            lotes.sort(Comparator.comparing(ProdutoEstoque::getDataValidade));
            return lotes.get(0);
        }
        return null;
    }

 public boolean verificarEstoque(String nomeProduto, int quantidadeSolicitada) throws SQLException {
        int quantidadeDisponivel = produtoEstoqueDAO.getQuantidadeEstoquePorNome(nomeProduto);
        return quantidadeDisponivel >= quantidadeSolicitada;
    }

    public boolean removerProdutosDoEstoque(List<CarrinhoProduto> carrinho) throws SQLException {
        boolean sucesso = true;
        for (CarrinhoProduto item : carrinho) {
            int quantidadeRemover = item.getQuantidade();
            List<ProdutoEstoque> lotes = produtoEstoqueDAO.listarLotesPorProduto(item.getProduto().getId());

            for (ProdutoEstoque lote : lotes) {
                if (quantidadeRemover <= 0) break;

                int qtdLote = lote.getQuantidade();
                if (qtdLote > quantidadeRemover) {
                    int novaQtd = qtdLote - quantidadeRemover;
                    sucesso &= produtoEstoqueDAO.atualizarQuantidadeLote(lote.getId(), novaQtd);
                    quantidadeRemover = 0;
                } else {
                    sucesso &= produtoEstoqueDAO.removerLote(lote.getId());
                    quantidadeRemover -= qtdLote;
                }
            }

            if (quantidadeRemover > 0) {
                sucesso = false;
                break;
            }
        }
        return sucesso;
    }

public List<ProdutoQuantidadeTotal> listarProdutosComQuantidadeTotalAgrupada() throws SQLException {
    List<ProdutoEstoque> todosEstoques = produtoEstoqueDAO.listarTodos();
    Map<Integer, ProdutoQuantidadeTotal> map = new HashMap<>();

    for (ProdutoEstoque pe : todosEstoques) {
        int produtoId = pe.getProduto().getId();
        ProdutoQuantidadeTotal pqt = map.get(produtoId);

        if (pqt == null) {
            pqt = new ProdutoQuantidadeTotal(pe.getProduto(), 0);
        }

        int novaQuantidade = (int) (pqt.getQuantidadeTotal() + pe.getQuantidade());
        map.put(produtoId, new ProdutoQuantidadeTotal(pe.getProduto(), novaQuantidade));
    }

    return new ArrayList<>(map.values());
}

public Produto buscarProdutoPorNomeQuantidade(String nomeProduto, int quantidadeNecessaria) throws SQLException {
    Produto produto = produtoDAO.buscarPorNome(nomeProduto);
    if (produto == null) return null;

    int quantidadeDisponivel = produtoEstoqueDAO.getQuantidadeEstoquePorNome(nomeProduto);

    if (quantidadeDisponivel >= quantidadeNecessaria) {
        return produto;
    }

    return null;
}

public Produto buscarProdutoPorNome(String nomeProduto) throws SQLException {
    return produtoDAO.buscarPorNome(nomeProduto);
}

public boolean removerProdutosDoEstoqueFIFO(List<CarrinhoProduto> itens) throws SQLException {
    int qtdRestante;
    List<ProdutoEstoque> lotes;

    for (CarrinhoProduto item : itens) {
        qtdRestante = item.getQuantidade();
        // Buscar lotes do produto ordenados pelo FIFO
        lotes = produtoEstoqueDAO.buscarLotesPorProdutoOrdenados(item.getProduto().getId());

        for (ProdutoEstoque lote : lotes) {
            int qtdLote = lote.getQuantidade();

            if (qtdLote >= qtdRestante) {
                produtoEstoqueDAO.atualizarQuantidadeLote(lote.getId(), qtdLote - qtdRestante);
                qtdRestante = 0;
                break;
            } else {
                produtoEstoqueDAO.removerLote(lote.getId());
                qtdRestante -= qtdLote;
            }
        }

        if (qtdRestante > 0) {
            throw new SQLException("Estoque insuficiente para o produto: " + item.getProduto().getNome());
        }
    }

    return true;
}


public int getProximoLote(int idProduto) throws SQLException {
    int ultimoLote = produtoEstoqueDAO.buscarUltimoLote(idProduto);
    return ultimoLote + 1;
}

public List<ProdutoEstoqueResumo> listarProdutosDisponiveis() throws SQLException {
    List<ProdutoEstoque> todosLotes = produtoEstoqueDAO.listarTodos();
    Map<Integer, ProdutoEstoqueResumo> agrupado = new HashMap<>();

    for (ProdutoEstoque lote : todosLotes) {
        // Ignora vencidos
        if (lote.getDataValidade() != null && lote.getDataValidade().isBefore(LocalDate.now())) {
            continue;
        }

        // Ignora lotes com quantidade zerada ou negativa
        if (lote.getQuantidade() <= 0) {
            continue;
        }

        int produtoId = lote.getProduto().getId();

        ProdutoEstoqueResumo resumoLote = new ProdutoEstoqueResumo(
            lote.getProduto(),
            lote.getQuantidade(),
            lote.getDataColhido(),
            lote.getDataValidade(),
            lote.getLote() // objeto do lote
        );

        if (agrupado.containsKey(produtoId)) {
            ProdutoEstoqueResumo existente = agrupado.get(produtoId);
            // acumula quantidades
            agrupado.put(produtoId, new ProdutoEstoqueResumo(
                existente.getProduto(),
                existente.getQuantidadeTotal() + resumoLote.getQuantidadeTotal(),
                existente.getDataColhido(),
                existente.getDataValidade(),
                existente.getLote()
            ));
        } else {
            agrupado.put(produtoId, resumoLote);
        }
    }

    return new ArrayList<>(agrupado.values());
}

public List<ProdutoEstoqueResumo> listarProdutosNormais() throws SQLException {
    return listarProdutosDisponiveis(); 
}

public List<ProdutoEstoqueResumo> listarProdutosFoodToSave() throws SQLException {
    List<ProdutoEstoque> todosLotes = produtoEstoqueDAO.listarTodos();
    Map<Integer, ProdutoEstoqueResumo> agrupado = new HashMap<>();

    LocalDate hoje = LocalDate.now();
    System.out.println("[FoodToSave] Hoje = " + hoje);

    for (ProdutoEstoque lote : todosLotes) {
        Produto produto = lote.getProduto();

        if (produto == null) {
            System.out.println("[FoodToSave][SKIP] Lote sem produto associado (id lote: " + lote.getId() + ")");
            continue;
        }

        Integer diasParaVencer = (produto.getDiasParaVencer() != 0) ? produto.getDiasParaVencer() : null;
        Integer descontoInicio  = (produto.getDescontoInicio()  != 0) ? produto.getDescontoInicio()  : null;

        if (diasParaVencer == null || descontoInicio == null) {
            System.out.printf("[FoodToSave][SKIP] Produto '%s' sem dias_para_vencer (%s) ou desconto_inicio (%s)%n",
                    produto.getNome(), diasParaVencer, descontoInicio);
            continue;
        }

        // Corrige casos inválidos
        if (descontoInicio < 0) descontoInicio = 0;
        if (diasParaVencer < 0) diasParaVencer = 0;
        if (descontoInicio > diasParaVencer) {
            System.out.printf("[FoodToSave][WARN] Produto '%s': desconto_inicio (%d) > dias_para_vencer (%d). Ajustando para igual.%n",
                    produto.getNome(), descontoInicio, diasParaVencer);
            descontoInicio = diasParaVencer;
        }

        LocalDate dataValidade = lote.getDataValidade();
        if (dataValidade == null) {
            if (lote.getDataColhido() == null) {
                System.out.printf("[FoodToSave][SKIP] Produto '%s' sem dataValidade e sem dataColhido%n", produto.getNome());
                continue;
            }
            dataValidade = lote.getDataColhido().plusDays(diasParaVencer);
        }

        int janela = Math.max(0, diasParaVencer - descontoInicio);
        LocalDate dataInicioDesconto = dataValidade.minusDays(janela);

        boolean dentroJanela = ( !hoje.isBefore(dataInicioDesconto) && !hoje.isAfter(dataValidade) );

        System.out.printf(
            "[FoodToSave] '%s' | Colhido=%s | Val=%s | InícioDesc=%s | Qtd=%d | Dentro? %s%n",
            produto.getNome(),
            String.valueOf(lote.getDataColhido()),
            dataValidade,
            dataInicioDesconto,
            lote.getQuantidade(),
            dentroJanela
        );

        if (!dentroJanela) {
            if (hoje.isBefore(dataInicioDesconto)) {
                System.out.println("   -> [SKIP] Ainda não entrou no período de desconto.");
            } else if (hoje.isAfter(dataValidade)) {
                System.out.println("   -> [SKIP] Já venceu.");
            }
            continue;
        }

        if (lote.getQuantidade() <= 0) {
            System.out.println("   -> [SKIP] Quantidade do lote <= 0.");
            continue;
        }

       int produtoId = produto.getId();
        agrupado.merge(produtoId,
            new ProdutoEstoqueResumo(
                produto,
                lote.getQuantidade(),
                lote.getDataColhido(),
                dataValidade,
                lote.getLote() // <-- passa o objeto ProdutoEstoque
            ),
            (existente, novo) -> new ProdutoEstoqueResumo(
                existente.getProduto(),
                existente.getQuantidadeTotal() + novo.getQuantidadeTotal(),
                existente.getDataColhido(),
                existente.getDataValidade(),
                existente.getLote() // mantém o objeto ProdutoEstoque existente ou faça ajuste se precisar
            )
        );

    }

    List<ProdutoEstoqueResumo> resultado = new ArrayList<>(agrupado.values());
    System.out.println("[FoodToSave] Total de produtos no período: " + resultado.size());
    for (ProdutoEstoqueResumo r : resultado) {
        System.out.printf("   -> %s | QtdTotal=%d%n", r.getProduto().getNome(), r.getQuantidadeTotal());
    }
    return resultado;
}

public double calcularPrecoComDesconto(ProdutoEstoque lote) {
    Produto produto = lote.getProduto();
    if (produto == null || lote.getDataValidade() == null || produto.getDiasParaVencer() <= 0) {
        return produto.getPreco();
    }

    LocalDate hoje = LocalDate.now();
    LocalDate dataValidade = lote.getDataValidade();
    int diasParaVencer = produto.getDiasParaVencer();
    int descontoInicio = produto.getDescontoInicio();

    if (descontoInicio < 0) descontoInicio = 0;
    if (descontoInicio > diasParaVencer) descontoInicio = diasParaVencer;

    LocalDate dataInicioDesconto = dataValidade.minusDays(diasParaVencer - descontoInicio);

    if (!hoje.isBefore(dataInicioDesconto) && !hoje.isAfter(dataValidade)) {
        int diasNoDesconto = (int) (dataValidade.toEpochDay() - hoje.toEpochDay());
        double percentualDesconto = ((double)(descontoInicio - diasNoDesconto) / descontoInicio) * 100;
        percentualDesconto = Math.max(0, percentualDesconto);
        return produto.getPreco() * (1 - percentualDesconto / 100);
    } else {
        return produto.getPreco();
    }
}

public boolean isFoodToSave(ProdutoEstoque lote) {
    if (lote == null || lote.getProduto() == null) return false;

    Produto produto = lote.getProduto();
    LocalDate hoje = LocalDate.now();

    Integer diasParaVencer = produto.getDiasParaVencer() != 0 ? produto.getDiasParaVencer() : null;
    Integer descontoInicio = produto.getDescontoInicio() != 0 ? produto.getDescontoInicio() : null;

    if (diasParaVencer == null || descontoInicio == null) return false;

    LocalDate dataValidade = lote.getDataValidade();
    if (dataValidade == null && lote.getDataColhido() != null) {
        dataValidade = lote.getDataColhido().plusDays(diasParaVencer);
    } else if (dataValidade == null) {
        return false;
    }

    int janela = Math.max(0, diasParaVencer - descontoInicio);
    LocalDate inicioDesconto = dataValidade.minusDays(janela);

    return !hoje.isBefore(inicioDesconto) && !hoje.isAfter(dataValidade);
}

public ProdutoEstoque buscarLoteParaFoodToSave(int produtoId) throws SQLException {
    List<ProdutoEstoqueResumo> foodToSave = listarProdutosFoodToSave();
    for (ProdutoEstoqueResumo resumo : foodToSave) {
        if (resumo.getProduto().getId() == produtoId) {
            return produtoEstoqueDAO.buscarPorId(resumo.getLote());
        }
    }
    return null;
}

public double calcularPrecoFoodToSave(Produto produto) throws SQLException {
    ProdutoEstoque lote = buscarLoteParaFoodToSave(produto.getId());
    if (lote != null) {
        return calcularPrecoComDesconto(lote);
    } else {
        return produto.getPreco();
    }
}

public void adicionarQuantidade(int produtoId, int quantidade) throws SQLException {
    List<ProdutoEstoque> lotes = produtoEstoqueDAO.listarLotesPorProduto(produtoId);
    if (!lotes.isEmpty()) {
        ProdutoEstoque lote = lotes.get(0); // pega o primeiro lote
        produtoEstoqueDAO.atualizarQuantidadeLote(lote.getId(), lote.getQuantidade() + quantidade);
    }
}

public int buscarQuantidade(int produtoId) throws SQLException {
    List<ProdutoEstoque> lotes = produtoEstoqueDAO.listarLotesPorProduto(produtoId);
    int total = 0;
    for (ProdutoEstoque lote : lotes) {
        total += lote.getQuantidade();
    }
    return total;
}

public void atualizarQuantidade(int produtoId, int novaQuantidade) throws SQLException {
    List<ProdutoEstoque> lotes = produtoEstoqueDAO.listarLotesPorProduto(produtoId);
    if (!lotes.isEmpty()) {
        ProdutoEstoque lote = lotes.get(0); // pega o primeiro lote
        produtoEstoqueDAO.atualizarQuantidadeLote(lote.getId(), novaQuantidade);
    }
}

public List<ProdutoEstoqueResumo> listarProdutosBaixoEstoqueService(int limite) throws SQLException {
    List<ProdutoEstoqueResumo> todos = listarProdutosNormais();
    List<ProdutoEstoqueResumo> baixoEstoque = new ArrayList<>();

    for (ProdutoEstoqueResumo pe : todos) {
        if (pe.getQuantidadeTotal() <= limite) {
            baixoEstoque.add(pe);
        }
    }

    return baixoEstoque;
}

public List<ProdutoEstoqueResumo> listarProdutosProximosVencerService(int dias) throws SQLException {
    List<ProdutoEstoqueResumo> todos = listarProdutosNormais();
    List<ProdutoEstoqueResumo> proximosVencer = new ArrayList<>();
    LocalDate hoje = LocalDate.now();

    for (ProdutoEstoqueResumo pe : todos) {
        if (pe.getDataValidade() != null && !pe.getDataValidade().isBefore(hoje)
            && !pe.getDataValidade().isAfter(hoje.plusDays(dias))) {
            proximosVencer.add(pe);
        }
    }

    return proximosVencer;
}

public void retirarDoEstoque(int produtoId, int quantidade) throws SQLException {
    List<ProdutoEstoque> lotes = produtoEstoqueDAO.buscarLotesPorProdutoOrdenados(produtoId);
    int qtdRestante = quantidade;

    for (ProdutoEstoque lote : lotes) {
        int qtdLote = lote.getQuantidade();

        if (qtdLote >= qtdRestante) {
            produtoEstoqueDAO.atualizarQuantidadeLote(lote.getId(), qtdLote - qtdRestante);
            qtdRestante = 0;
            break;
        } else {
            produtoEstoqueDAO.removerLote(lote.getId());
            qtdRestante -= qtdLote;
        }
    }

    if (qtdRestante > 0) {
        throw new SQLException("Estoque insuficiente para o produto ID " + produtoId);
    }
}


}
