package br.com.lojagames.notificacao_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.lojagames.notificacao_service.entity.Notificacao;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByPedidoId(Long pedidoId);
}
