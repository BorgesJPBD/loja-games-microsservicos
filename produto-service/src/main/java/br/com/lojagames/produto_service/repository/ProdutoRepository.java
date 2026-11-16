package br.com.lojagames.produto_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.lojagames.produto_service.entity.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
