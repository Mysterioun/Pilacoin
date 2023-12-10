package br.ufsm.csi.pila.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
@Entity
@Table(name = "pilacoin")
public class Pilacoin {
    @Transient
    private long id;
    @Id
    @Column(name = "nonce", unique = true)
    private String nonce;
    @Transient
    private String nomeCriador;
    @Transient
    private byte[] chaveCriador;
    @Transient
    private Date dataCriacao;
    @Transient
    private List<Transacao> transacoes;
    @Column(name = "status")
    private String status;
}
