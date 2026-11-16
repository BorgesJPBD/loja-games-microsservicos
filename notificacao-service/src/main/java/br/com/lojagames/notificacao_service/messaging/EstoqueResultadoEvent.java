package br.com.lojagames.notificacao_service.messaging;

public record EstoqueResultadoEvent(Long pedidoId, boolean sucesso, String motivo) {
}
