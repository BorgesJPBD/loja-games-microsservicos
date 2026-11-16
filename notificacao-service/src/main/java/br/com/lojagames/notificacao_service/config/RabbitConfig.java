package br.com.lojagames.notificacao_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "loja.eventos";

    public static final String RK_ESTOQUE_RESULTADO = "estoque.resultado";

    // Fila própria: o RabbitMQ entrega uma CÓPIA do mesmo evento para o pedido-service e para este
    public static final String FILA_ESTOQUE_RESULTADO = "notificacao.estoque-resultado";

    @Bean
    public TopicExchange lojaEventosExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue filaEstoqueResultado() {
        return new Queue(FILA_ESTOQUE_RESULTADO, true);
    }

    @Bean
    public Binding bindingEstoqueResultado(Queue filaEstoqueResultado, TopicExchange lojaEventosExchange) {
        return BindingBuilder.bind(filaEstoqueResultado).to(lojaEventosExchange).with(RK_ESTOQUE_RESULTADO);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
