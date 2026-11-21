package br.com.lojagames.produto_service.messaging;

import java.math.BigDecimal;

// Evento publicado: resultado da tentativa de reservar o estoque.
// valorTotal = preço x quantidade (só vem preenchido quando deu certo; senão, null)
public record EstoqueResultadoEvent(Long pedidoId, boolean sucesso, String motivo, BigDecimal valorTotal) {
}