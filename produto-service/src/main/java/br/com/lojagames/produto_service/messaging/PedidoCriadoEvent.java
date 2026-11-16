package br.com.lojagames.produto_service.messaging;

// Evento recebido: um pedido foi criado e precisa reservar estoque
public record PedidoCriadoEvent(Long pedidoId, Long produtoId, Integer quantidade) {
}
