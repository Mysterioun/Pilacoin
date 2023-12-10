package br.ufsm.csi.pila.repository;

import br.ufsm.csi.pila.model.Pilacoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PilacoinRepository extends JpaRepository<Pilacoin, String> {
    List<Pilacoin> findByStatus(String status);

}
