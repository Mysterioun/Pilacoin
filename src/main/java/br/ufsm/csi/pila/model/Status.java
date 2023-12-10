package br.ufsm.csi.pila.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Status {
    private boolean mineraPila;
    private boolean mineraBloco;
    private boolean validaPila;
    private boolean validaBloco;
}
