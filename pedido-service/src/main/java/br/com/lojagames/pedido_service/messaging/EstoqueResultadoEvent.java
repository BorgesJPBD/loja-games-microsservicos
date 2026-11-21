package br.com.lojagames.pedido_service.messaging;

import java.math.BigDecimal;

// Evento recebido do produto-service com o resultado da reserva de estoque.
// valorTotal só vem preenchido quando deu certo.
public record EstoqueResultadoEvent(Long pedidoId, boolean sucesso, String motivo, BigDecimal valorTotal) {
}