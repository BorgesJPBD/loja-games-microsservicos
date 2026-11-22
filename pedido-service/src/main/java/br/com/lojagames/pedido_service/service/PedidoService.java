package br.com.lojagames.pedido_service.service;

import java.util.List;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.lojagames.pedido_service.config.RabbitConfig;
import br.com.lojagames.pedido_service.dto.PedidoRequest;
import br.com.lojagames.pedido_service.entity.Pedido;
import br.com.lojagames.pedido_service.entity.StatusPedido;
import br.com.lojagames.pedido_service.messaging.EstoqueResultadoEvent;
import br.com.lojagames.pedido_service.messaging.PedidoCriadoEvent;
import br.com.lojagames.pedido_service.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository repository;
    private final RabbitTemplate rabbitTemplate;

    // ===== Usados pelo controller (HTTP) =====

    public List<Pedido> listar() {
        return repository.findAll();
    }

    public Pedido buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));
    }

    // Sem @Transactional de propósito: o save() confirma no banco ANTES de publicar,
    // assim o pedido já existe quando o resultado voltar.
    public Pedido criar(PedidoRequest req) {
        Pedido pedido = repository.save(new Pedido(req.produtoId(), req.quantidade()));

        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE,
                    RabbitConfig.RK_PEDIDO_CRIADO,
                    new PedidoCriadoEvent(pedido.getId(), pedido.getProdutoId(), pedido.getQuantidade()));
        } catch (AmqpException e) {
            // : RabbitMQ fora do ar. O pedido não pode ficar PENDENTE para sempre.
            log.error("Não foi possível publicar o pedido {}: {}", pedido.getId(), e.getMessage());
            pedido.setStatus(StatusPedido.RECUSADO);
            pedido.setMotivo("Serviço de mensagens indisponível. Tente novamente.");
            repository.save(pedido);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Serviço de mensagens indisponível. Tente novamente.");
        }

        return pedido;   // volta com status PENDENTE; muda quando o resultado chegar
    }

    // ===== Usado pelo listener (RabbitMQ) =====

    @Transactional
    public void aplicarResultado(EstoqueResultadoEvent evento) {
        Pedido pedido = repository.findById(evento.pedidoId()).orElse(null);

        if (pedido == null) {
            log.warn("Pedido {} não existe; resultado ignorado", evento.pedidoId());
            return;
        }
        // : só um pedido PENDENTE pode mudar. Se a mesma resposta chegar de novo, é ignorada.
        if (pedido.getStatus() != StatusPedido.PENDENTE) {
            log.warn("Pedido {} já está {}; resultado repetido ignorado", pedido.getId(), pedido.getStatus());
            return;
        }

        if (evento.sucesso()) {
            pedido.setStatus(StatusPedido.CONFIRMADO);
            pedido.setValorTotal(evento.valorTotal());
        } else {
            pedido.setStatus(StatusPedido.RECUSADO);
            pedido.setMotivo(evento.motivo());
        }
    }
}