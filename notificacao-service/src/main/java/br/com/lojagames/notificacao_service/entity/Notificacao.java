package br.com.lojagames.notificacao_service.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notificacoes")
@Getter
@Setter
@NoArgsConstructor
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long pedidoId;

    @Column(nullable = false)
    private String mensagem;

    @Column(nullable = false)
    private LocalDateTime criadaEm = LocalDateTime.now();

    // : o cliente já leu este aviso? Toda notificação nasce como não lida.
    @Column(nullable = false)
    private boolean lida = false;

    public Notificacao(Long pedidoId, String mensagem) {
        this.pedidoId = pedidoId;
        this.mensagem = mensagem;
    }
}