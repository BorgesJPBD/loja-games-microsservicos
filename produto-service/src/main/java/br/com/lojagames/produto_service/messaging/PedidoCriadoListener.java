package br.com.lojagames.produto_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import br.com.lojagames.produto_service.config.RabbitConfig;
import br.com.lojagames.produto_service.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoCriadoListener {

    private final ProdutoService produtoService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitConfig.FILA_PEDIDO_CRIADO)   // 1) recebe da fila
    public void aoReceberPedido(PedidoCriadoEvent evento) {
        log.info("Pedido recebido: {}", evento);

        // 2) pede ao service; quando ele termina, o estoque JÁ está salvo no banco
        EstoqueResultadoEvent resultado = produtoService.reservarEstoque(evento);

        // 3) só então avisa os outros serviços
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE, RabbitConfig.RK_ESTOQUE_RESULTADO, resultado);
        log.info("Resultado publicado: {}", resultado);
    }
}