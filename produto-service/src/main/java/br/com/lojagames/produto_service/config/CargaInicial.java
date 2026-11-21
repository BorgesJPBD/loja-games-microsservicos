package br.com.lojagames.produto_service.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import br.com.lojagames.produto_service.entity.Plataforma;
import br.com.lojagames.produto_service.entity.Produto;
import br.com.lojagames.produto_service.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CargaInicial implements CommandLineRunner {

    private final ProdutoRepository repository;

    // Roda automaticamente uma vez, logo depois que o serviço liga
    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;   // o banco já tem produtos: não cadastra de novo
        }

        repository.saveAll(List.of(
                new Produto("Elden Ring", Plataforma.PC, "RPG", new BigDecimal("229.90"), 10),
                new Produto("Hollow Knight", Plataforma.PC, "Metroidvania", new BigDecimal("46.99"), 20),
                new Produto("God of War Ragnarok", Plataforma.PLAYSTATION_5, "Ação", new BigDecimal("249.90"), 8),
                new Produto("Gran Turismo 7", Plataforma.PLAYSTATION_5, "Corrida", new BigDecimal("299.90"), 5),
                new Produto("Forza Horizon 5", Plataforma.XBOX_SERIES, "Corrida", new BigDecimal("199.90"), 6),
                new Produto("Halo Infinite", Plataforma.XBOX_SERIES, "Tiro", new BigDecimal("149.90"), 4),
                new Produto("Zelda: Tears of the Kingdom", Plataforma.NINTENDO_SWITCH, "Aventura", new BigDecimal("349.90"), 7),
                new Produto("Mario Kart 8 Deluxe", Plataforma.NINTENDO_SWITCH, "Corrida", new BigDecimal("299.90"), 1)));

        log.info("Carga inicial: {} produtos cadastrados", repository.count());
    }
}