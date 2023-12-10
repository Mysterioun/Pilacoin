package br.ufsm.csi.pila.service;

import br.ufsm.csi.pila.model.Dificuldade;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class DificuldadeService {

    public static BigInteger dificuldadeAtual;
    private BigInteger novaDificuldade;
    private boolean verificaDif = true;


    // Pega a dificuldade do Sor
    @SneakyThrows
    @RabbitListener(queues = "dificuldade")
    public void getDificuldade(@Payload String difSor) {
        ObjectMapper objectMapper = new ObjectMapper();
        Dificuldade dif = objectMapper.readValue(difSor, Dificuldade.class);
        dificuldadeAtual = new BigInteger(dif.getDificuldade(), 16);
        //Testa se a dificuldade pega mudou

        if(!dificuldadeAtual.equals(this.novaDificuldade) && !verificaDif) {
            System.out.println("--- Nova Dificuldade = " + dificuldadeAtual);
        }
        // Printa a dificuldade pega

        if(verificaDif) {
            System.out.println(" --- Dificuldade Do Sor = " + dificuldadeAtual);
            verificaDif = false;
        }
        novaDificuldade = dificuldadeAtual;

    }

}
