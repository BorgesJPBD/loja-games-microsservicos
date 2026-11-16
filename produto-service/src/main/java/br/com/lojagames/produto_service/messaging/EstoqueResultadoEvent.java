package br.com.lojagames.produto_service.messaging;

// Evento publicado: resultado da tentativa de reservar o estoque
public record EstoqueResultadoEvent(Long pedidoId, boolean sucesso, String motivo) {
}
