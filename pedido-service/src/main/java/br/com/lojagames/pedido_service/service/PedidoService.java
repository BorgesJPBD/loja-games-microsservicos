package br.com.lojagames.pedido_service.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import br.com.lojagames.pedido_service.config.RabbitConfig;
import br.com.lojagames.pedido_service.dto.PedidoRequest;
import br.com.lojagames.pedido_service.entity.Pedido;
import br.com.lojagames.pedido_service.messaging.PedidoCriadoEvent;
import br.com.lojagames.pedido_service.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository repository;
    private final RabbitTemplate rabbitTemplate;

    // Sem @Transactional de propósito: o save() confirma no banco ANTES de publicar,
    // assim o pedido já existe quando o resultado voltar.
    public Pedido criar(PedidoRequest req) {
        Pedido pedido = repository.save(new Pedido(req.produtoId(), req.quantidade()));

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.RK_PEDIDO_CRIADO,
                new PedidoCriadoEvent(pedido.getId(), pedido.getProdutoId(), pedido.getQuantidade()));

        return pedido; // volta com status PENDENTE; muda quando o resultado chegar
    }
}
