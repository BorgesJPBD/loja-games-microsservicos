package br.com.lojagames.pedido_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.com.lojagames.pedido_service.config.RabbitConfig;
import br.com.lojagames.pedido_service.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstoqueResultadoListener {

    private final PedidoService service;

    @RabbitListener(queues = RabbitConfig.FILA_ESTOQUE_RESULTADO)   // consome da fila do pedido
    public void aoReceberResultado(EstoqueResultadoEvent evento) {
        log.info("Resultado de estoque recebido: {}", evento);
        service.aplicarResultado(evento);   // a regra fica no service
    }
}