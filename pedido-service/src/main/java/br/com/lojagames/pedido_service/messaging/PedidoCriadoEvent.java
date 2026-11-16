package br.com.lojagames.pedido_service.messaging;

// Evento publicado quando um pedido é criado
public record PedidoCriadoEvent(Long pedidoId, Long produtoId, Integer quantidade) {
}
