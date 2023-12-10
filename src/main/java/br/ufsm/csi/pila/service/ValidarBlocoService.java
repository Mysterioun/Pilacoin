package br.ufsm.csi.pila.service;

import br.ufsm.csi.pila.model.Bloco;
import br.ufsm.csi.pila.model.BlocoValido;
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
public class ValidarBlocoService {

    private final RabbitTemplate rabbitTemplate;

    private List<String> ignoreList = new ArrayList<>();


    @Getter
    private static boolean validarBloco = true;


    public ValidarBlocoService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;

    }


    @SneakyThrows
    @RabbitListener(queues = "bloco-minerado")
    public void validaBloco(String blocoStr){

        if (!validarBloco){
            System.out.println("Ignorando validação de blocos");
            rabbitTemplate.convertAndSend("bloco-minerado", blocoStr);
            return;
        }


        if (ignoreList.contains(blocoStr)) {
            rabbitTemplate.convertAndSend("bloco-minerado", blocoStr);
            System.out.println("---- JA VALIDEI ESSE BLOCO ----");
            return;
        }
        ignoreList.add(blocoStr);

        ObjectMapper objectMapper = new ObjectMapper();
        Bloco bloco;
        try {
            bloco = objectMapper.readValue(blocoStr, Bloco.class);
        } catch (JsonProcessingException e) {
            System.out.println("bloco formato invalido");
            return;
        }
        if(bloco.getNomeUsuarioMinerador().equals("christian_katarine")){
            rabbitTemplate.convertAndSend("bloco-minerado", blocoStr);
            return;
        }
        System.out.println("\n\n--- Validando bloco minerado pelo(a): "+bloco.getNomeUsuarioMinerador());
        BigInteger hash;
        try {
            hash = PilaUtils.geraHash(blocoStr);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        while(DificuldadeService.dificuldadeAtual == null){}//garatnir q n vai tentar comparar antes de receber a dificuldade

        if (hash.compareTo(DificuldadeService.dificuldadeAtual) < 0){
            System.out.println(" === Valido! ===");
            BlocoValido valido = BlocoValido.builder().assinaturaBloco(PilaUtils.geraAssinatura(bloco))
                    .bloco(bloco).chavePublicaValidador(Chaves.carregarChavePublicaDeArquivo("publicKey.der").getEncoded())
                    .nomeValidador("christian_katarine").build();

            try {
                this.rabbitTemplate.convertAndSend("bloco-validado", objectMapper.writeValueAsString(valido));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        this.rabbitTemplate.convertAndSend("bloco-minerado", blocoStr);
    }

    public static boolean mudarStatusValidarBloco(){
        validarBloco = !validarBloco;
        return validarBloco;
    }

}

