package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.dao.HabitacionRepository;
import edu.utn.frsf.isi.dan.gestion.dao.HotelRepository;
import edu.utn.frsf.isi.dan.gestion.dao.TipoHabitacionRepository;
import edu.utn.frsf.isi.dan.gestion.model.Habitacion;
import edu.utn.frsf.isi.dan.gestion.model.Hotel;
import edu.utn.frsf.isi.dan.gestion.model.Tarifa;
import edu.utn.frsf.isi.dan.gestion.model.TipoHabitacion;
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
import java.util.stream.Collectors;

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
    private TipoHabitacionRepository tipoHabitacionRepository;

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

    /**
     * El precio no es una columna de Habitacion (se resuelve via la tarifa vigente del tipo),
     * asi que el filtro por precio se aplica en memoria despues de traer las que matchean el
     * resto de los criterios por query.
     */
    public List<Habitacion> buscar(Integer tipoHabitacionId, Integer capacidadMinima, Boolean disponible,
                                    Integer hotelId, Double precioMin, Double precioMax) {
        List<Habitacion> candidatas = habitacionRepository.buscar(tipoHabitacionId, capacidadMinima, disponible, hotelId);
        if (precioMin == null && precioMax == null) {
            return candidatas;
        }
        return candidatas.stream()
                .filter(h -> {
                    Double precio = tarifaService.obtenerPrecioVigente(h.getTipoHabitacion().getId());
                    return (precioMin == null || precio >= precioMin) && (precioMax == null || precio <= precioMax);
                })
                .collect(Collectors.toList());
    }

    public void enviarHabitacionJms(Habitacion habitacion, boolean isNew) {
        // habitacion.getTipoHabitacion() puede ser solo la referencia suelta que llego en el
        // request (por ej. {"id": 2}, sin nombre/descripcion/capacidad); se vuelve a buscar en
        // la base para completar el DTO con los datos reales.
        TipoHabitacion tipoHabitacion = tipoHabitacionRepository.findById(habitacion.getTipoHabitacion().getId())
                .orElse(habitacion.getTipoHabitacion());
        Double precioVigente = tarifaService.obtenerPrecioVigente(tipoHabitacion.getId());
        Hotel hotelCompleto = obtenerHotelCompleto(habitacion.getHotel());
        HotelDTO hotelDto = mapearHotel(hotelCompleto);

        HabitacionDTO dto = HabitacionDTO.builder()
                .habitacionId(habitacion.getId().longValue())
                .numero(habitacion.getNumero())
                .piso(habitacion.getPiso())
                .tipoHabitacionId(tipoHabitacion.getId())
                .tipoHabitacion(tipoHabitacion.getNombre())
                .tipoHabitacionDescripcion(tipoHabitacion.getDescripcion())
                .capacidad(tipoHabitacion.getCapacidad())
                .precioNoche(precioVigente)
                .disponible(habitacion.getDisponible())
                // Habitacion no tiene amenities propios: se propagan los del hotel al que
                // pertenece, para que reservas-svc pueda filtrar la busqueda de disponibilidad
                // por amenity (la habitacion "hereda" las comodidades de su hotel).
                .amenities(mapearAmenities(hotelCompleto))
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

    private Hotel obtenerHotelCompleto(Hotel hotelReferenciado) {
        if (hotelReferenciado == null || hotelReferenciado.getId() == null) {
            return null;
        }
        return hotelRepository.findById(hotelReferenciado.getId()).orElse(null);
    }

    private HotelDTO mapearHotel(Hotel hotel) {
        if (hotel == null) {
            return null;
        }
        return HotelDTO.builder()
                .id(hotel.getId())
                .nombre(hotel.getNombre())
                .cuit(hotel.getCuit())
                .domicilio(hotel.getDomicilio())
                .latitud(hotel.getLatitud())
                .longitud(hotel.getLongitud())
                .telefono(hotel.getTelefono())
                .correoContacto(hotel.getCorreoContacto())
                .categoria(hotel.getCategoria())
                .cerrado(hotel.getCerrado())
                .build();
    }

    private List<String> mapearAmenities(Hotel hotel) {
        if (hotel == null || hotel.getAmenities() == null) {
            return List.of();
        }
        return hotel.getAmenities().stream()
                .map(amenityHotel -> amenityHotel.getAmenity().name())
                .collect(Collectors.toList());
    }
}
