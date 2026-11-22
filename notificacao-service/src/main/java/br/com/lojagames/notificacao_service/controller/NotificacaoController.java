package br.com.lojagames.notificacao_service.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.lojagames.notificacao_service.entity.Notificacao;
import br.com.lojagames.notificacao_service.service.NotificacaoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService service;   

    @GetMapping   // /notificacoes, ?pedidoId=1 ou ?lida=false
    public List<Notificacao> listar(@RequestParam(required = false) Long pedidoId,
                                    @RequestParam(required = false) Boolean lida) {
        return service.listar(pedidoId, lida);
    }

    @PatchMapping("/{id}/lida")   // : marca uma notificação como lida
    public Notificacao marcarComoLida(@PathVariable Long id) {
        return service.marcarComoLida(id);
    }
}