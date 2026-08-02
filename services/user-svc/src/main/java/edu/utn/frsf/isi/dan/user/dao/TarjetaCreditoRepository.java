package edu.utn.frsf.isi.dan.user.dao;

import edu.utn.frsf.isi.dan.user.model.TarjetaCredito;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarjetaCreditoRepository extends JpaRepository<TarjetaCredito, Integer> {

  Optional<TarjetaCredito> findByHuespedIdAndEsPrincipalTrue(Integer huespedId);

  List<TarjetaCredito> findByHuespedId(Integer huespedId);
}
