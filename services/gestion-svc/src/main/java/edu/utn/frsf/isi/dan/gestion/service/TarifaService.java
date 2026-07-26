package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.dao.TarifaRepository;
import edu.utn.frsf.isi.dan.gestion.model.Tarifa;
import edu.utn.frsf.isi.dan.shared.HabitacionEvent;
import edu.utn.frsf.isi.dan.shared.TarifaDTO;
import edu.utn.frsf.isi.dan.shared.TipoEvento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TarifaService {
    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private EventPublisherService eventPublisherService;

    public Tarifa save(Tarifa tarifa) {
        Tarifa nuevaTarifa = tarifaRepository.save(tarifa);
        enviarActualizarPrecioJms(nuevaTarifa);
        return nuevaTarifa;
    }

    private void enviarActualizarPrecioJms(Tarifa tarifa) {
        TarifaDTO dto = TarifaDTO.builder()
                .tipoHabitacionId(tarifa.getTipoHabitacion().getId())
                .nuevoPrecio(tarifa.getPrecioNoche())
                .build();
        HabitacionEvent msgEvent = HabitacionEvent.builder()
                .tipoEvento(TipoEvento.ACTUALIZAR_PRECIO)
                .tarifa(dto)
                .build();
        eventPublisherService.publicar(msgEvent);
    }

    public void deleteById(Integer id) {
        tarifaRepository.deleteById(id);
    }

    public Optional<Tarifa> findById(Integer id) {
        return tarifaRepository.findById(id);
    }

    public List<Tarifa> findAll() {
        return tarifaRepository.findAll();
    }
}
