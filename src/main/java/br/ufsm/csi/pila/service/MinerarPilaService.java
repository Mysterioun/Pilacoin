package br.ufsm.csi.pila.service;

import br.ufsm.csi.pila.model.Pilacoin;
import br.ufsm.csi.pila.repository.PilacoinRepository;
import br.ufsm.csi.pila.utils.Chaves;
import br.ufsm.csi.pila.utils.PilaUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Service
public class MinerarPilaService {
    private final PilacoinRepository pilacoinRepository;
    private final RabbitTemplate rabbitTemplate;

    @Getter
    private static volatile boolean minerarPila = true;

    public MinerarPilaService(RabbitTemplate rabbitTemplate, PilacoinRepository pilacoinRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.pilacoinRepository = pilacoinRepository;
    }




    @PostConstruct
    public void mineraPila(){
        new Thread(()->{
            Pilacoin pilacoin = null;
            try {
                pilacoin = Pilacoin.builder().chaveCriador(Chaves.carregarChavePublicaDeArquivo("publicKey.der").getEncoded()).
                        nomeCriador("christian_katarine").dataCriacao(new Date()).build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            ObjectMapper om = new ObjectMapper();
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            int tentativa = 0;
            while (true){

                while (!minerarPila) {
                    Thread.onSpinWait();
                }

                tentativa++;
                pilacoin.setNonce(PilaUtils.geraNonce());
                BigInteger hash;
                try {
                    hash = new BigInteger(md.digest(om.writeValueAsString(pilacoin).getBytes(StandardCharsets.UTF_8))).abs();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                while(DificuldadeService.dificuldadeAtual == null){}//garatnir q n vai tentar comparar antes de receber a dificuldade


                if (hash.compareTo(DificuldadeService.dificuldadeAtual) < 0){
                    System.out.println("\n\n---PILA Minerado em: "+tentativa+" tentativas ---");
                    pilacoin.setStatus("AG_VALIDACAO");
                    System.out.println(pilacoin);
                    try {
                        rabbitTemplate.convertAndSend("pila-minerado", om.writeValueAsString(pilacoin));
                      //  pilacoinRepository.save(pilacoin);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }

    public static boolean mudarStatusMineracao(){
        minerarPila = !minerarPila;
        return minerarPila;
    }

}
