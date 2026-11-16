package br.com.lojagames.pedido_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.lojagames.pedido_service.entity.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
