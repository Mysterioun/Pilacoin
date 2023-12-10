package br.ufsm.csi.pila.service;

import br.ufsm.csi.pila.model.*;
import br.ufsm.csi.pila.repository.PilacoinRepository;
import br.ufsm.csi.pila.repository.UsuarioRepository;
import br.ufsm.csi.pila.utils.Chaves;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class RequisicaoService {


    private final ObjectReader objectReader = new ObjectMapper().reader();

    public final UsuarioRepository usuarioRepository;
    public final PilacoinRepository pilacoinRepository;

    public RequisicaoService(UsuarioRepository usuarioRepository, PilacoinRepository pilacoinRepository) {
        this.usuarioRepository = usuarioRepository;
        this.pilacoinRepository = pilacoinRepository;
    }

    @RabbitListener(queues = "christian_katarine")
    public void msgs(@Payload String msg){
        System.out.println(msg);
    }

    @SneakyThrows
    @RabbitListener(queues = "report")
    public void getReport(@Payload String report) {
        List<Report> reports = List.of(this.objectReader.readValue(report, Report[].class));
        Optional<Report> myReport = reports.stream()
                .filter(r  -> r.getNomeUsuario() != null && r.getNomeUsuario().equals("christian_katarine"))
                .findFirst();
        System.out.println(myReport);
    }

    @SneakyThrows
    @RabbitListener(queues = "christian_katarine-query")
    public void recebeQuery(String queryStr){
        //System.out.println(queryStr);
        ObjectMapper objectMapper = new ObjectMapper();
        QueryRecebe query = objectMapper.readValue(queryStr, QueryRecebe.class);
        if (query.getPilasResult() != null){
            for (Pilacoin pila: query.getPilasResult()){
                if (Arrays.equals(pila.getChaveCriador(), Chaves.carregarChavePublicaDeArquivo("publicKey.der").getEncoded()) && (pila.getTransacoes() == null || pila.getTransacoes().size() <= 1)){
                    pilacoinRepository.save(pila);
                } else if (pila.getTransacoes() != null && !pila.getTransacoes().isEmpty()){
                    Transacao transacao = pila.getTransacoes().get(pila.getTransacoes().size() - 1);
                    if (Arrays.equals(transacao.getChaveUsuarioDestino(),Chaves.carregarChavePublicaDeArquivo("publicKey.der").getEncoded())){
                        //pilacoinRepository.save(pila);
                    } else if (Arrays.equals(transacao.getChaveUsuarioOrigem(), Chaves.carregarChavePublicaDeArquivo("publicKey.der").getEncoded())){
                        pilacoinRepository.delete(pila);
                    }
                }
            }
        } else if (query.getUsuariosResult() != null) {
            usuarioRepository.saveAll(query.getUsuariosResult());
        } else if (query.getBlocosResult() != null){
            for (Bloco bloco: query.getBlocosResult()){
                for (Transacao transacao: bloco.getTransacoes()){
                    if (transacao.getChaveUsuarioDestino() != null && Arrays.equals(transacao.getChaveUsuarioDestino(), Chaves.carregarChavePublicaDeArquivo("publicKey.der").getEncoded())){
                        pilacoinRepository.save(Pilacoin.builder().nonce(transacao.getNoncePila()).status("VALIDO").build());
                    } else if (transacao.getChaveUsuarioOrigem() != null && Arrays.equals(transacao.getChaveUsuarioOrigem(), Chaves.carregarChavePublicaDeArquivo("publicKey.der").getEncoded())){
                        pilacoinRepository.delete(Pilacoin.builder().nonce(transacao.getNoncePila()).status("VALIDO").build());
                    }
                }
            }
        }
    }

    @RabbitListener(queues = "clients-errors")
    public void errors(@Payload String error){
        System.out.println("Error: "+error);
    }
}
