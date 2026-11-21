package br.com.lojagames.produto_service.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.lojagames.produto_service.dto.ProdutoRequest;
import br.com.lojagames.produto_service.entity.Produto;
import br.com.lojagames.produto_service.messaging.EstoqueResultadoEvent;
import br.com.lojagames.produto_service.messaging.PedidoCriadoEvent;
import br.com.lojagames.produto_service.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository repository;


    public List<Produto> listar() {
        return repository.findAll();
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
    public EstoqueResultadoEvent reservarEstoque(PedidoCriadoEvent evento) {
        Produto produto = repository.findById(evento.produtoId()).orElse(null);

        if (produto == null) {
            return new EstoqueResultadoEvent(evento.pedidoId(), false, "Produto não encontrado");
        }
        if (produto.getEstoque() < evento.quantidade()) {
            return new EstoqueResultadoEvent(evento.pedidoId(), false,
                    "Estoque insuficiente (disponível: " + produto.getEstoque() + ")");
        }

        produto.setEstoque(produto.getEstoque() - evento.quantidade());
        return new EstoqueResultadoEvent(evento.pedidoId(), true, "Estoque reservado");
    }
}