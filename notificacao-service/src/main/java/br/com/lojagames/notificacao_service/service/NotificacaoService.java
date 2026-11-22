package br.com.lojagames.notificacao_service.service;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.lojagames.notificacao_service.entity.Notificacao;
import br.com.lojagames.notificacao_service.messaging.EstoqueResultadoEvent;
import br.com.lojagames.notificacao_service.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository repository;

    // ===== Usados pelo controller (HTTP) =====

    // GET /notificacoes, /notificacoes?pedidoId=1 ou /notificacoes?lida=false
    public List<Notificacao> listar(Long pedidoId, Boolean lida) {
        if (pedidoId != null) {
            return repository.findByPedidoId(pedidoId);
        }
        if (lida != null) {
            return repository.findByLida(lida);
        }
        return repository.findAll();
    }

    @Transactional
    public Notificacao marcarComoLida(Long id) {
        Notificacao notificacao = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada"));
        notificacao.setLida(true);
        return notificacao;   // o @Transactional salva a mudança sozinho
    }

    // ===== Usado pelo listener (RabbitMQ) =====

    public void registrar(EstoqueResultadoEvent evento) {
        String mensagem;
        if (evento.sucesso()) {
            String total = String.format(Locale.of("pt", "BR"), "R$ %.2f", evento.valorTotal());
            mensagem = "Pedido #" + evento.pedidoId() + " confirmado! Total: " + total + ". Obrigado pela compra.";
        } else {
            mensagem = "Pedido #" + evento.pedidoId() + " recusado: " + evento.motivo();
        }
        repository.save(new Notificacao(evento.pedidoId(), mensagem));
    }
}