package br.com.lojagames.produto_service.dto;

import java.math.BigDecimal;

import br.com.lojagames.produto_service.entity.Plataforma;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProdutoRequest(
        @NotBlank String nome,
        @NotNull Plataforma plataforma,
        String genero,
        @NotNull @DecimalMin("0.0") BigDecimal preco,
        @NotNull @Min(0) Integer estoque) {
}
