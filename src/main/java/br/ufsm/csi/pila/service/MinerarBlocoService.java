package br.ufsm.csi.pila.service;

import br.ufsm.csi.pila.model.Bloco;
import br.ufsm.csi.pila.utils.Chaves;
import br.ufsm.csi.pila.utils.PilaUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class MinerarBlocoService {
    private final RabbitTemplate rabbitTemplate;


    @Getter
    private static boolean minerarBloco = true;

    public MinerarBlocoService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }




    @SneakyThrows
    @RabbitListener(queues = "descobre-bloco")
    public void mineraBloco(String blocoJson) {

        if (!minerarBloco){
            //System.out.println("Ignorando blocos!");
            rabbitTemplate.convertAndSend("descobre-bloco", blocoJson);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Bloco bloco = objectMapper.readValue(blocoJson, Bloco.class);
        bloco.setChaveUsuarioMinerador(Chaves.carregarChavePublicaDeArquivo("publicKey.der").getEncoded());
        bloco.setNomeUsuarioMinerador("christian_katarine");
        BigInteger hash;
        while (true){
            bloco.setNonce(PilaUtils.geraNonce());
            hash = PilaUtils.geraHash(bloco);
            while(DificuldadeService.dificuldadeAtual == null){}//garatnir q n vai tentar comparar antes de receber a dificuldade

            if(hash.compareTo(DificuldadeService.dificuldadeAtual) < 0){
                System.out.println("\n\n--- Minerou BLOCO: --- \n" + bloco);
                rabbitTemplate.convertAndSend("bloco-minerado", objectMapper.writeValueAsString(bloco));
                return;
            }
        }
    }

    public static boolean mudarStatusBloco(){
        minerarBloco = !minerarBloco;
        return minerarBloco;
    }
}
