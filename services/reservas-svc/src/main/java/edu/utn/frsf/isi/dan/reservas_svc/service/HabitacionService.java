package edu.utn.frsf.isi.dan.reservas_svc.service;

import edu.utn.frsf.isi.dan.reservas_svc.model.EstadoReserva;
import edu.utn.frsf.isi.dan.reservas_svc.model.Habitacion;
import edu.utn.frsf.isi.dan.reservas_svc.model.Hotel;
import edu.utn.frsf.isi.dan.reservas_svc.model.Reserva;
import edu.utn.frsf.isi.dan.reservas_svc.repository.HabitacionRepository;
import edu.utn.frsf.isi.dan.shared.HabitacionDTO;
import edu.utn.frsf.isi.dan.shared.HabitacionEvent;
import edu.utn.frsf.isi.dan.shared.HotelDTO;
import edu.utn.frsf.isi.dan.shared.TarifaDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HabitacionService {
    @Autowired
    private HabitacionRepository habitacionRepository;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private DisponibilidadService disponibilidadService;

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Habitacion> findAll() {
        return habitacionRepository.findAll();
    }

    public Optional<Habitacion> findById(String id) {
        return habitacionRepository.findById(id);
    }

    public Habitacion save(Habitacion habitacion) {
        return habitacionRepository.save(habitacion);
    }

    public void deleteById(String id) {
        habitacionRepository.deleteById(id);
    }

    public void handleEvent(HabitacionEvent event) {
        switch (event.getTipoEvento()) {
            case CREAR:
                save(mapFromHabitacion(event.getHabitacion()));
                break;
            case ACTUALIZAR_DATOS:
                actualizarDatos(event.getHabitacion());
                break;
            case ACTUALIZAR_PRECIO:
                actualizarPrecioPorTipo(event.getTarifa());
                break;
            case ELIMINAR:
                deleteByHabitacionId(event.getHabitacion().getHabitacionId());
                break;
            default:
                throw new IllegalArgumentException("Tipo de evento desconocido: " + event.getTipoEvento());
        }
    }

    /**
     * Ademas de actualizar los datos de la habitacion, detecta la transicion "hotel se cerro
     * ahora" comparando el estado guardado contra el que trae el evento. gestion-svc publica un
     * ACTUALIZAR_DATOS por cada habitacion del hotel al cerrarlo, asi que esto crea exactamente
     * una reserva CERRADA por habitacion, y no se duplica en reentregas del mensaje porque en
     * ese caso la habitacion ya va a tener hotel.cerrado = true guardado de antes.
     */
    private void actualizarDatos(HabitacionDTO dto) {
        boolean yaEstabaCerrado = findByHabitacionId(dto.getHabitacionId())
                .map(h -> h.getHotel() != null && Boolean.TRUE.equals(h.getHotel().getCerrado()))
                .orElse(false);
        boolean seCierraAhora = !yaEstabaCerrado && dto.getHotel() != null && Boolean.TRUE.equals(dto.getHotel().getCerrado());

        Habitacion actualizada = updateByHabitacionId(dto.getHabitacionId(), mapFromHabitacion(dto));

        if (seCierraAhora) {
            crearReservaCierreDeHotel(actualizada);
        }
    }

    private void crearReservaCierreDeHotel(Habitacion habitacion) {
        Reserva reserva = Reserva.builder()
                .idHabitacion(habitacion.getId())
                .hotelId(habitacion.getHotel() != null ? habitacion.getHotel().getId().longValue() : null)
                .createdAt(Instant.now())
                .checkIn(Instant.now())
                .checkOut(null)
                .estadoReserva(EstadoReserva.CERRADA)
                .build();
        reservaService.save(reserva);
    }

    private void actualizarPrecioPorTipo(TarifaDTO tarifa) {
        if (tarifa == null || tarifa.getTipoHabitacionId() == null) {
            return;
        }
        Query query = new Query(Criteria.where("idTipoHabitacion").is(tarifa.getTipoHabitacionId()));
        Update update = new Update().set("precioNoche", tarifa.getNuevoPrecio());
        mongoTemplate.updateMulti(query, update, Habitacion.class);
    }

    public Habitacion mapFromHabitacion(HabitacionDTO dto) {
        return Habitacion.builder()
                .habitacionId(dto.getHabitacionId())
                .precioNoche(dto.getPrecioNoche())
                .capacidad(dto.getCapacidad())
                .disponible(dto.getDisponible())
                .idTipoHabitacion(dto.getTipoHabitacionId())
                .tipoHabitacion(dto.getTipoHabitacion())
                .amenities(dto.getAmenities())
                .hotel(mapFromDto(dto.getHotel()))
                .build();
    }

    public Hotel mapFromDto(HotelDTO dto) {
        if (dto == null) {
            return null;
        }
        return Hotel.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .domicilio(dto.getDomicilio())
                .categoria(dto.getCategoria())
                .cerrado(dto.getCerrado())
                .ubicacion(buildUbicacion(dto.getLatitud(), dto.getLongitud()))
                .build();
    }

    /**
     * El hotel puede no tener lat/long cargadas (son opcionales en gestion-svc). Sin este
     * chequeo, el unboxing de un Double null en el constructor de GeoJsonPoint (que pide
     * double primitivo) tira NPE y el evento se pierde silenciosamente: la habitación nunca
     * llega a reservas-svc. Sin ubicacion, el hotel simplemente no participa de búsquedas por
     * cercanía ($near ya viene guardado con un chequeo propio de null en buscarDisponibles).
     */
    private GeoJsonPoint buildUbicacion(Double latitud, Double longitud) {
        if (latitud == null || longitud == null) {
            return null;
        }
        return new GeoJsonPoint(longitud, latitud);
    }

    public Optional<Habitacion> findByHabitacionId(Long habitacionId) {
        Query query = new Query(Criteria.where("habitacionId").is(habitacionId));
        Habitacion habitacion = mongoTemplate.findOne(query, Habitacion.class);
        return Optional.ofNullable(habitacion);
    }

    public Habitacion updateByHabitacionId(Long habitacionId, Habitacion nuevaHabitacion) {
        Query query = new Query(Criteria.where("habitacionId").is(habitacionId));
        Update update = new Update()
                .set("precioNoche", nuevaHabitacion.getPrecioNoche())
                .set("capacidad", nuevaHabitacion.getCapacidad())
                .set("disponible", nuevaHabitacion.getDisponible())
                .set("idTipoHabitacion", nuevaHabitacion.getIdTipoHabitacion())
                .set("tipoHabitacion", nuevaHabitacion.getTipoHabitacion())
                .set("amenities", nuevaHabitacion.getAmenities())
                .set("hotel", nuevaHabitacion.getHotel());
        Habitacion actualizada = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                Habitacion.class
        );
        if (actualizada == null) {
            throw new IllegalArgumentException("No se encontró la habitación con habitacionId: " + habitacionId);
        }
        return actualizada;
    }

    public void deleteByHabitacionId(Long habitacionId) {
        Query query = new Query(Criteria.where("habitacionId").is(habitacionId));
        mongoTemplate.remove(query, Habitacion.class);
    }

    /**
     * Filtra primero por los criterios que son propios de la habitacion/hotel (capacidad,
     * precio, categoria, amenities, cercania), y sobre ese subconjunto descarta las que tengan
     * alguna reserva activa que se solape con el rango pedido. Se hace en dos pasos porque la
     * disponibilidad por fechas depende de la coleccion Reserva, no de Habitacion.
     */
    public List<Habitacion> buscarDisponibles(Instant checkIn, Instant checkOut, Integer huespedesMinimos,
                                                Double precioMin, Double precioMax, Integer categoriaMinima,
                                                List<String> amenities, Double latitud, Double longitud,
                                                Double distanciaMaximaKm) {
        if (checkIn == null || checkOut == null || !checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException("El rango checkIn/checkOut es invalido: checkIn debe ser anterior a checkOut");
        }

        Query query = new Query();
        query.addCriteria(Criteria.where("disponible").is(true));
        if (huespedesMinimos != null) {
            query.addCriteria(Criteria.where("capacidad").gte(huespedesMinimos));
        }
        if (precioMin != null) {
            query.addCriteria(Criteria.where("precioNoche").gte(precioMin));
        }
        if (precioMax != null) {
            query.addCriteria(Criteria.where("precioNoche").lte(precioMax));
        }
        if (categoriaMinima != null) {
            query.addCriteria(Criteria.where("hotel.categoria").gte(categoriaMinima));
        }
        if (amenities != null && !amenities.isEmpty()) {
            query.addCriteria(Criteria.where("amenities").all(amenities));
        }
        if (latitud != null && longitud != null) {
            double maxDistanciaMetros = (distanciaMaximaKm != null ? distanciaMaximaKm : 10) * 1000;
            query.addCriteria(Criteria.where("hotel.ubicacion")
                    .near(new GeoJsonPoint(longitud, latitud))
                    .maxDistance(maxDistanciaMetros));
        }

        List<Habitacion> candidatas = mongoTemplate.find(query, Habitacion.class);
        if (candidatas.isEmpty()) {
            return candidatas;
        }

        List<String> idsCandidatas = candidatas.stream().map(Habitacion::getId).collect(Collectors.toList());
        Set<String> idsOcupadas = disponibilidadService.idsConReservaQueSolapa(idsCandidatas, checkIn, checkOut);

        return candidatas.stream()
                .filter(h -> !idsOcupadas.contains(h.getId()))
                .collect(Collectors.toList());
    }
}
