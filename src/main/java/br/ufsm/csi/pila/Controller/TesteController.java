package br.ufsm.csi.pila.Controller;

import br.ufsm.csi.pila.model.*;
import br.ufsm.csi.pila.repository.PilacoinRepository;
import br.ufsm.csi.pila.repository.UsuarioRepository;
import br.ufsm.csi.pila.service.MinerarBlocoService;
import br.ufsm.csi.pila.service.MinerarPilaService;
import br.ufsm.csi.pila.service.ValidarBlocoService;
import br.ufsm.csi.pila.service.ValidarPilaService;
import br.ufsm.csi.pila.utils.Chaves;
import br.ufsm.csi.pila.utils.PilaUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/teste")
@CrossOrigin
public class TesteController {
    private final PilacoinRepository pilacoinRepository;
    private final UsuarioRepository usuarioRepository;
    private final RabbitTemplate rabbitTemplate;

    public TesteController(PilacoinRepository pilacoinRepository, UsuarioRepository usuarioRepository, RabbitTemplate rabbitTemplate) {
        this.pilacoinRepository = pilacoinRepository;
        this.usuarioRepository = usuarioRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/minerarPila")
    public boolean mineraPila(){
        System.out.println("Alterando mineracao de pila");
        return MinerarPilaService.mudarStatusMineracao();
    }

    @GetMapping("/minerarBloco")
    public boolean mineraBloco(){
        System.out.println("Alterando mineracao de bloco");
        return MinerarBlocoService.mudarStatusBloco();
    }


    @GetMapping("/validarPila")
    public boolean validaPila(){
        System.out.println("Alterando validacao de pila");
        return ValidarPilaService.mudarStatusValidarPila();
    }

    @GetMapping("/validarBloco")
    public boolean validaBloco(){
        System.out.println("Alterando validacao de bloco");
        return ValidarBlocoService.mudarStatusValidarBloco();
    }


    @GetMapping("/status")
    public Status getStatus(){
        Status status = Status.builder().mineraBloco(MinerarBlocoService.isMinerarBloco())
                .mineraPila(MinerarPilaService.isMinerarPila()).validaPila(ValidarPilaService.isValidarPila())
                .validaBloco(ValidarBlocoService.isValidarBloco()).build();
        System.out.println(status);
        return status;
    }


    @GetMapping("/usuarios")
    public List<Usuario> getUsuarios(){
        System.out.println(usuarioRepository.findAll());
        return usuarioRepository.findAll();
    }


    @GetMapping("/pilas")
    public List<Pilacoin> getPilas(){
        return pilacoinRepository.findAll();
    }


    @GetMapping("/query/{type}")
    public void query(@PathVariable String type) throws JsonProcessingException {
        QueryEnvia query = QueryEnvia.builder().idQuery(1).tipoQuery(type).nomeUsuario("christian_katarine").build();
        ObjectMapper objectMapper = new ObjectMapper();
        rabbitTemplate.convertAndSend("query", objectMapper.writeValueAsString(query));
    }


    @PostMapping("/transferir/{qntd}")
    public void tranferirPila(@RequestBody Usuario user, @PathVariable int qntd) throws IOException, ClassNotFoundException {
        List<Pilacoin> pilas = pilacoinRepository.findByStatus("VALIDO");
        if (pilas.size() < qntd){
            throw new RuntimeException();
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            for (int i = 0; i < qntd; i++){
                TransferirPila transferir = TransferirPila.builder().noncePila(pilas.get(i).getNonce())
                        .chaveUsuarioOrigem(Chaves.carregarChavePublicaDeArquivo("publicKey.der").getEncoded()).nomeUsuarioOrigem("christian_katarine")
                        .chaveUsuarioDestino(user.getChavePublica()).nomeUsuarioDestino(user.getNome())
                        .dataTransacao(new Date()).build();
                transferir.setAssinatura(PilaUtils.geraAssinatura(transferir));
                rabbitTemplate.convertAndSend("transferir-pila", objectMapper.writeValueAsString(transferir));
                System.out.println("Pila enviado pro usuario " + user.getNome());
                System.out.println(transferir);
                pilacoinRepository.delete(pilas.get(i));
            }
        }
    }


}