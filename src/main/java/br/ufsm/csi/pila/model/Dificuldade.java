package br.ufsm.csi.pila.model;

import lombok.Data;

import java.util.Date;

@Data
public class Dificuldade {
    String dificuldade;
    Date inicio;
    Date validadeFinal;
}