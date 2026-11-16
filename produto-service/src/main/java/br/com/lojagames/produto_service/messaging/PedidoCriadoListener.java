package br.com.lojagames.produto_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.lojagames.produto_service.config.RabbitConfig;
import br.com.lojagames.produto_service.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoCriadoListener {

    private final ProdutoRepository produtoRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitConfig.FILA_PEDIDO_CRIADO)
    @Transactional
    public void aoReceberPedido(PedidoCriadoEvent evento) {
        log.info("Pedido recebido: {}", evento);

        EstoqueResultadoEvent resultado = reservarEstoque(evento);

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE, RabbitConfig.RK_ESTOQUE_RESULTADO, resultado);
        log.info("Resultado publicado: {}", resultado);
    }

    private EstoqueResultadoEvent reservarEstoque(PedidoCriadoEvent evento) {
        var produto = produtoRepository.findById(evento.produtoId()).orElse(null);

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
