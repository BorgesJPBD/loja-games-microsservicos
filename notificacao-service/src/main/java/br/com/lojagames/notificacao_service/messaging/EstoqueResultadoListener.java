package br.com.lojagames.notificacao_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.com.lojagames.notificacao_service.config.RabbitConfig;
import br.com.lojagames.notificacao_service.service.NotificacaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstoqueResultadoListener {

    private final NotificacaoService service;

    @RabbitListener(queues = RabbitConfig.FILA_ESTOQUE_RESULTADO)   // consome da fila da notificação
    public void aoReceberResultado(EstoqueResultadoEvent evento) {
        log.info("Resultado recebido para notificar: {}", evento);
        service.registrar(evento);   // a regra (montar a mensagem) fica no service
    }
}