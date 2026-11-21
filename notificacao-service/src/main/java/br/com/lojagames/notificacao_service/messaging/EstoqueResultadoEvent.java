package br.com.lojagames.notificacao_service.messaging;

import java.math.BigDecimal;

// Evento recebido do produto-service. valorTotal só vem preenchido quando deu certo.
public record EstoqueResultadoEvent(Long pedidoId, boolean sucesso, String motivo, BigDecimal valorTotal) {
}