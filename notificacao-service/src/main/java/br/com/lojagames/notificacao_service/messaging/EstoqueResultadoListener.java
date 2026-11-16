package br.com.lojagames.notificacao_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.com.lojagames.notificacao_service.config.RabbitConfig;
import br.com.lojagames.notificacao_service.entity.Notificacao;
import br.com.lojagames.notificacao_service.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstoqueResultadoListener {

    private final NotificacaoRepository repository;

    @RabbitListener(queues = RabbitConfig.FILA_ESTOQUE_RESULTADO)
    public void aoReceberResultado(EstoqueResultadoEvent evento) {
        log.info("Resultado recebido para notificar: {}", evento);

        String mensagem = evento.sucesso()
                ? "Pedido #" + evento.pedidoId() + " confirmado! Obrigado pela compra."
                : "Pedido #" + evento.pedidoId() + " recusado: " + evento.motivo();

        repository.save(new Notificacao(evento.pedidoId(), mensagem));
    }
}
