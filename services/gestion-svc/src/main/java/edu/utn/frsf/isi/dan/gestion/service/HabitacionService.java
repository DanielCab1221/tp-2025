package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.dao.HabitacionRepository;
import edu.utn.frsf.isi.dan.gestion.dao.HotelRepository;
import edu.utn.frsf.isi.dan.gestion.model.Habitacion;
import edu.utn.frsf.isi.dan.gestion.model.Hotel;
import edu.utn.frsf.isi.dan.gestion.model.Tarifa;
import edu.utn.frsf.isi.dan.shared.HabitacionDTO;
import edu.utn.frsf.isi.dan.shared.HabitacionEvent;
import edu.utn.frsf.isi.dan.shared.HotelDTO;
import edu.utn.frsf.isi.dan.shared.TipoEvento;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Log4j2
public class HabitacionService {

    @Autowired
    private HabitacionRepository habitacionRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private TarifaService tarifaService;

    @Autowired
    private EventPublisherService eventPublisherService;

    public Habitacion save(Habitacion habitacion) {
        log.info("Habltaicon {} ", habitacion);
        boolean isNew = Objects.isNull(habitacion.getId());
        Habitacion newHabitacion = habitacionRepository.save(habitacion);
        enviarHabitacionJms(newHabitacion, isNew);
        return newHabitacion;
    }

    public void deleteById(Integer id) {
        enviarHabitacionJms(id);
        habitacionRepository.deleteById(id);
    }

    public Optional<Habitacion> findById(Integer id) {
        return habitacionRepository.findById(id);
    }

    public List<Habitacion> findAll() {
        return habitacionRepository.findAll();
    }

    public void enviarHabitacionJms(Habitacion habitacion, boolean isNew) {
        Double precioVigente = tarifaService.obtenerPrecioVigente(habitacion.getTipoHabitacion().getId());
        HotelDTO hotelDto = mapearHotel(habitacion.getHotel());

        HabitacionDTO dto = HabitacionDTO.builder()
                .habitacionId(habitacion.getId().longValue())
                .numero(habitacion.getNumero())
                .tipoHabitacionId(habitacion.getTipoHabitacion().getId())
                .tipoHabitacion(habitacion.getTipoHabitacion().getDescripcion())
                .capacidad(habitacion.getTipoHabitacion().getCapacidad())
                .precioNoche(precioVigente)
                .disponible(habitacion.getDisponible())
                .hotel(hotelDto)
                .build();
        HabitacionEvent msgEvent = HabitacionEvent.builder()
                .tipoEvento(isNew ? TipoEvento.CREAR : TipoEvento.ACTUALIZAR_DATOS)
                .habitacion(dto)
                .build();
        eventPublisherService.publicar(msgEvent);
    }

    public void enviarHabitacionJms(Integer id) {
        HabitacionDTO dto = HabitacionDTO.builder()
                .habitacionId(id.longValue()).build();
        HabitacionEvent msgEvent = HabitacionEvent.builder()
                .tipoEvento(TipoEvento.ELIMINAR)
                .habitacion(dto)
                .build();
        eventPublisherService.publicar(msgEvent);
    }

    public Optional<Tarifa> obtenerTarifaVigente(Habitacion habitacion) {
        return tarifaService.obtenerVigente(habitacion.getTipoHabitacion().getId(), LocalDate.now());
    }

    private HotelDTO mapearHotel(Hotel hotelReferenciado) {
        if (hotelReferenciado == null || hotelReferenciado.getId() == null) {
            return null;
        }
        Optional<Hotel> hotel = hotelRepository.findById(hotelReferenciado.getId());
        return hotel.map(h -> HotelDTO.builder()
                .id(h.getId())
                .nombre(h.getNombre())
                .cuit(h.getCuit())
                .domicilio(h.getDomicilio())
                .latitud(h.getLatitud())
                .longitud(h.getLongitud())
                .telefono(h.getTelefono())
                .correoContacto(h.getCorreoContacto())
                .categoria(h.getCategoria())
                .cerrado(h.getCerrado())
                .build()
        ).orElse(null);
    }
}
