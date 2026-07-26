package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.dao.TarifaRepository;
import edu.utn.frsf.isi.dan.gestion.model.Tarifa;
import edu.utn.frsf.isi.dan.shared.HabitacionEvent;
import edu.utn.frsf.isi.dan.shared.TarifaDTO;
import edu.utn.frsf.isi.dan.shared.TipoEvento;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TarifaService {

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private EventPublisherService eventPublisherService;

    /**
     * Crea una tarifa nueva para un tipo de habitacion, cerrando automaticamente la que
     * estaba vigente para no dejar solapamientos:
     * - Continua (sin fechaFin): arranca en fechaInicio (o hoy si no se especifica) y no tiene fin.
     * - Promocional (con fechaFin): ademas crea la tarifa de continuacion que retoma el precio
     *   anterior a partir del dia siguiente a que termina la promo.
     */
    @Transactional
    public Tarifa crear(Tarifa tarifaRequest) {
        if (tarifaRequest.getTipoHabitacion() == null || tarifaRequest.getTipoHabitacion().getId() == null) {
            throw new IllegalArgumentException("El tipo de habitacion es requerido");
        }
        if (tarifaRequest.getPrecioNoche() == null || tarifaRequest.getPrecioNoche() <= 0) {
            throw new IllegalArgumentException("El precio por noche debe ser mayor a cero");
        }

        Integer tipoHabitacionId = tarifaRequest.getTipoHabitacion().getId();
        LocalDate fechaInicio = tarifaRequest.getFechaInicio() != null ? tarifaRequest.getFechaInicio() : LocalDate.now();
        LocalDate fechaFin = tarifaRequest.getFechaFin();
        boolean esPromocional = fechaFin != null;

        if (esPromocional && !fechaFin.isAfter(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin de una tarifa promocional debe ser posterior a la fecha de inicio");
        }

        // Se busca vigente a la fecha de inicio de la nueva tarifa (no al dia anterior): si hubiera
        // otra tarifa que arranca ese mismo dia, "fechaFin = fechaInicio - 1" quedaria invertido
        // (fechaFin antes que fechaInicio). En ese caso la anterior queda completamente reemplazada.
        Optional<Tarifa> vigenteAnterior = obtenerVigente(tipoHabitacionId, fechaInicio);
        vigenteAnterior.ifPresent(vigente -> {
            if (vigente.getFechaInicio().isBefore(fechaInicio)) {
                vigente.setFechaFin(fechaInicio.minusDays(1));
                tarifaRepository.save(vigente);
            } else {
                tarifaRepository.deleteById(vigente.getId());
            }
        });

        Tarifa nueva = Tarifa.builder()
                .fechaInicio(fechaInicio)
                .fechaFin(esPromocional ? fechaFin : null)
                .tipoHabitacion(tarifaRequest.getTipoHabitacion())
                .precioNoche(tarifaRequest.getPrecioNoche())
                .build();
        Tarifa guardada = tarifaRepository.save(nueva);

        if (esPromocional) {
            Double precioContinuacion = vigenteAnterior.map(Tarifa::getPrecioNoche).orElse(tarifaRequest.getPrecioNoche());
            Tarifa continuacion = Tarifa.builder()
                    .fechaInicio(fechaFin.plusDays(1))
                    .fechaFin(null)
                    .tipoHabitacion(tarifaRequest.getTipoHabitacion())
                    .precioNoche(precioContinuacion)
                    .build();
            tarifaRepository.save(continuacion);
        }

        publicarPrecioVigente(tipoHabitacionId);
        return guardada;
    }

    /**
     * Solo se puede borrar si queda al menos otra tarifa para ese tipo de habitacion (no puede
     * quedar un tipo sin tarifa). Si la que se borra es la vigente, la tarifa anterior (la de
     * fechaInicio mas reciente entre las que ya terminaron) vuelve a quedar vigente (fechaFin = null).
     */
    @Transactional
    public void eliminar(Integer id) {
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarifa no encontrada con ID: " + id));

        Integer tipoHabitacionId = tarifa.getTipoHabitacion().getId();
        List<Tarifa> otras = tarifaRepository.findByTipoHabitacionIdOrderByFechaInicioDesc(tipoHabitacionId).stream()
                .filter(t -> !t.getId().equals(id))
                .collect(Collectors.toList());

        if (otras.isEmpty()) {
            throw new IllegalArgumentException("No se puede eliminar la unica tarifa de un tipo de habitacion");
        }

        boolean esVigente = esVigente(tarifa, LocalDate.now());
        tarifaRepository.deleteById(id);

        if (esVigente) {
            otras.stream()
                    .filter(t -> t.getFechaInicio().isBefore(tarifa.getFechaInicio()))
                    .max(Comparator.comparing(Tarifa::getFechaInicio))
                    .ifPresent(anterior -> {
                        anterior.setFechaFin(null);
                        tarifaRepository.save(anterior);
                    });
        }

        publicarPrecioVigente(tipoHabitacionId);
    }

    public Optional<Tarifa> obtenerVigente(Integer tipoHabitacionId, LocalDate fecha) {
        List<Tarifa> vigentes = tarifaRepository.findVigentes(tipoHabitacionId, fecha);
        return vigentes.isEmpty() ? Optional.empty() : Optional.of(vigentes.get(0));
    }

    public Double obtenerPrecioVigente(Integer tipoHabitacionId) {
        return obtenerVigente(tipoHabitacionId, LocalDate.now())
                .map(Tarifa::getPrecioNoche)
                .orElse(0.0);
    }

    private boolean esVigente(Tarifa tarifa, LocalDate fecha) {
        boolean yaEmpezo = !tarifa.getFechaInicio().isAfter(fecha);
        boolean noTermino = tarifa.getFechaFin() == null || !tarifa.getFechaFin().isBefore(fecha);
        return yaEmpezo && noTermino;
    }

    private void publicarPrecioVigente(Integer tipoHabitacionId) {
        TarifaDTO dto = TarifaDTO.builder()
                .tipoHabitacionId(tipoHabitacionId)
                .nuevoPrecio(obtenerPrecioVigente(tipoHabitacionId))
                .build();
        HabitacionEvent msgEvent = HabitacionEvent.builder()
                .tipoEvento(TipoEvento.ACTUALIZAR_PRECIO)
                .tarifa(dto)
                .build();
        eventPublisherService.publicar(msgEvent);
    }

    public Optional<Tarifa> findById(Integer id) {
        return tarifaRepository.findById(id);
    }

    public List<Tarifa> findAll() {
        return tarifaRepository.findAll();
    }
}
