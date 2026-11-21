package br.com.lojagames.pedido_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.lojagames.pedido_service.config.RabbitConfig;
import br.com.lojagames.pedido_service.entity.StatusPedido;
import br.com.lojagames.pedido_service.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstoqueResultadoListener {

    private final PedidoRepository repository;

    @RabbitListener(queues = RabbitConfig.FILA_ESTOQUE_RESULTADO)
    @Transactional
    public void aoReceberResultado(EstoqueResultadoEvent evento) {
        log.info("Resultado de estoque recebido: {}", evento);

        repository.findById(evento.pedidoId()).ifPresent(pedido -> {
            if (evento.sucesso()) {
                pedido.setStatus(StatusPedido.CONFIRMADO);
                pedido.setValorTotal(evento.valorTotal());   // NOVO: guarda o valor calculado pelo produto
            } else {
                pedido.setStatus(StatusPedido.RECUSADO);
                pedido.setMotivo(evento.motivo());
            }
        });
    }
}