package br.com.lojagames.produto_service.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.lojagames.produto_service.dto.ProdutoRequest;
import br.com.lojagames.produto_service.entity.Plataforma;
import br.com.lojagames.produto_service.entity.Produto;
import br.com.lojagames.produto_service.messaging.EstoqueResultadoEvent;
import br.com.lojagames.produto_service.messaging.PedidoCriadoEvent;
import br.com.lojagames.produto_service.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository repository;

    // ===== Usados pelo controller (HTTP) =====

    public List<Produto> listar(Plataforma plataforma) {
        if (plataforma == null) {
            return repository.findAll();                      // sem filtro: todos
        }
        return repository.findByPlataforma(plataforma);       // com filtro
    }

    public Produto buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
    }

    public Produto criar(ProdutoRequest req) {
        return repository.save(new Produto(
                req.nome(), req.plataforma(), req.genero(), req.preco(), req.estoque()));
    }

    @Transactional
    public Produto atualizar(Long id, ProdutoRequest req) {
        Produto produto = buscar(id);   // se não existir, já dá 404
        produto.setNome(req.nome());
        produto.setPlataforma(req.plataforma());
        produto.setGenero(req.genero());
        produto.setPreco(req.preco());
        produto.setEstoque(req.estoque());
        return produto;   // o @Transactional salva as mudanças sozinho
    }

    @Transactional
    public void remover(Long id) {
        Produto produto = buscar(id);   // se não existir, já dá 404
        repository.delete(produto);
    }

    // ===== Usado pelo listener (RabbitMQ) =====

    @Transactional
    public EstoqueResultadoEvent reservarEstoque(PedidoCriadoEvent evento) {
        Produto produto = repository.findById(evento.produtoId()).orElse(null);

        if (produto == null) {
            return new EstoqueResultadoEvent(evento.pedidoId(), false, "Produto não encontrado", null);
        }
        if (produto.getEstoque() < evento.quantidade()) {
            return new EstoqueResultadoEvent(evento.pedidoId(), false,
                    "Estoque insuficiente (disponível: " + produto.getEstoque() + ")", null);
        }

        produto.setEstoque(produto.getEstoque() - evento.quantidade());

        // NOVO: preço x quantidade (BigDecimal não usa *, usa multiply)
        BigDecimal valorTotal = produto.getPreco().multiply(BigDecimal.valueOf(evento.quantidade()));

        return new EstoqueResultadoEvent(evento.pedidoId(), true, "Estoque reservado", valorTotal);
    }
}