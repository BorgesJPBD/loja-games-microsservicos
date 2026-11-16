package br.com.lojagames.pedido_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PedidoRequest(
        @NotNull Long produtoId,
        @NotNull @Min(1) Integer quantidade) {
}
