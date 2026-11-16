package br.com.lojagames.pedido_service.config;

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

    public static final String RK_PEDIDO_CRIADO = "pedido.criado";
    public static final String RK_ESTOQUE_RESULTADO = "estoque.resultado";

    // Fila que ESTE serviço consome
    public static final String FILA_ESTOQUE_RESULTADO = "pedido.estoque-resultado";

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
