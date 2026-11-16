package br.com.lojagames.produto_service.config;

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

    // Exchange compartilhada por todos os serviços (mesmo nome nos 3 projetos)
    public static final String EXCHANGE = "loja.eventos";

    // Routing keys dos eventos
    public static final String RK_PEDIDO_CRIADO = "pedido.criado";
    public static final String RK_ESTOQUE_RESULTADO = "estoque.resultado";

    // Fila que ESTE serviço consome
    public static final String FILA_PEDIDO_CRIADO = "produto.pedido-criado";

    @Bean
    public TopicExchange lojaEventosExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue filaPedidoCriado() {
        return new Queue(FILA_PEDIDO_CRIADO, true);
    }

    @Bean
    public Binding bindingPedidoCriado(Queue filaPedidoCriado, TopicExchange lojaEventosExchange) {
        return BindingBuilder.bind(filaPedidoCriado).to(lojaEventosExchange).with(RK_PEDIDO_CRIADO);
    }

    // Serializa/deserializa os eventos como JSON
    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
