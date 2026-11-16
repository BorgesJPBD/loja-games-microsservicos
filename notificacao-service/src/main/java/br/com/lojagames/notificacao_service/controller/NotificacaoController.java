package br.com.lojagames.notificacao_service.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.lojagames.notificacao_service.entity.Notificacao;
import br.com.lojagames.notificacao_service.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoRepository repository;

    @GetMapping
    public List<Notificacao> listar(@RequestParam(required = false) Long pedidoId) {
        return pedidoId == null ? repository.findAll() : repository.findByPedidoId(pedidoId);
    }
}
