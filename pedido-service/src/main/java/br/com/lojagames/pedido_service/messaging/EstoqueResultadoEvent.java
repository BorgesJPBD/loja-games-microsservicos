package br.com.lojagames.pedido_service.messaging;

// Evento recebido do produto-service com o resultado da reserva de estoque
public record EstoqueResultadoEvent(Long pedidoId, boolean sucesso, String motivo) {
}
