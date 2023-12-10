package br.ufsm.csi.pila.service;

import br.ufsm.csi.pila.model.Pilacoin;
import br.ufsm.csi.pila.model.PilacoinValido;
import br.ufsm.csi.pila.utils.Chaves;
import br.ufsm.csi.pila.utils.PilaUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ValidarPilaService {
    private final RabbitTemplate rabbitTemplate;

    private List<String> ignoreList = new ArrayList<>();

    @Getter
    private static boolean validarPila = true;


    public ValidarPilaService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @SneakyThrows
    @RabbitListener(queues = "pila-minerado")
    public void validaPila(String pilaStr){

        if (!validarPila){
            System.out.println("Ignorando validação de pilas");
            rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
            return;
        }


        if (ignoreList.contains(pilaStr)) {
            rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
            System.out.println("---- JA VALIDEI ESSA PILA ----");
            return;
        }

        ignoreList.add(pilaStr);


        ObjectMapper objectMapper = new ObjectMapper();
        Pilacoin pila;
        try {
            pila = objectMapper.readValue(pilaStr, Pilacoin.class);
        } catch (JsonProcessingException e) {
            System.out.println("Pila formato invalido");
            return;
        }
        if (pila.getNomeCriador().equals("christian_katarine")){
            rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
            return;
        }
        System.out.println("\n\n--- Validando pila do(a): "+pila.getNomeCriador());
        BigInteger hash;
        try {
            hash = PilaUtils.geraHash(pilaStr);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            return;
        }

        while(DificuldadeService.dificuldadeAtual == null){}//garatnir q n vai tentar comparar antes de receber a dificuldade

        if (hash.compareTo(DificuldadeService.dificuldadeAtual) < 0){
            System.out.println("=== Valido! ===");
            PilacoinValido valido = PilacoinValido.builder().assinaturaPilaCoin(PilaUtils.geraAssinatura(pila))
                    .chavePublicaValidador(Chaves.carregarChavePublicaDeArquivo("publicKey.der").getEncoded()).nomeValidador("christian_katarine")
                    .pilaCoinJson(pila).build();


            try {
                this.rabbitTemplate.convertAndSend("pila-validado", objectMapper.writeValueAsString(valido));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        this.rabbitTemplate.convertAndSend("pila-minerado", pilaStr);
    }


    public static boolean mudarStatusValidarPila(){
        validarPila = !validarPila;
        return validarPila;
    }

}
