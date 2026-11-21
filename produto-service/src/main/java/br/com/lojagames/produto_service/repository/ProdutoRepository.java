package br.com.lojagames.produto_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.lojagames.produto_service.entity.Plataforma;
import br.com.lojagames.produto_service.entity.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // O Spring monta o SQL pelo nome: SELECT * FROM produtos WHERE plataforma = ?
    List<Produto> findByPlataforma(Plataforma plataforma);
}