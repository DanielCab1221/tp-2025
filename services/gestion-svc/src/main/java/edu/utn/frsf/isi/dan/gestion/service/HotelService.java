package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.dao.HabitacionRepository;
import edu.utn.frsf.isi.dan.gestion.dao.HotelRepository;
import edu.utn.frsf.isi.dan.gestion.model.Habitacion;
import edu.utn.frsf.isi.dan.gestion.model.Hotel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class HotelService {
    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private HabitacionRepository habitacionRepository;

    @Autowired
    private HabitacionService habitacionService;

    public Hotel save(Hotel hotel) {
        if (hotel.getId() == null && hotelRepository.existsByCuit(hotel.getCuit())) {
            throw new IllegalArgumentException("Ya existe un hotel registrado con el CUIT: " + hotel.getCuit());
        }
        return hotelRepository.save(hotel);
    }

    public Optional<Hotel> findById(Integer id) {
        return hotelRepository.findById(id);
    }

    public List<Hotel> findAll() {
        return hotelRepository.findAll();
    }

    public Optional<Hotel> actualizarDatosPermitidos(Integer id, Hotel datosActualizados) {
        return hotelRepository.findById(id).map(hotel -> {
            hotel.setCategoria(datosActualizados.getCategoria());
            hotel.setTelefono(datosActualizados.getTelefono());
            hotel.setCorreoContacto(datosActualizados.getCorreoContacto());
            return hotelRepository.save(hotel);
        });
    }

    @Transactional
    public Optional<Hotel> cerrar(Integer id) {
        return hotelRepository.findById(id).map(hotel -> {
            hotel.setCerrado(true);
            Hotel hotelCerrado = hotelRepository.save(hotel);

            List<Habitacion> habitaciones = habitacionRepository.findByHotelId(id);
            habitaciones.forEach(habitacion -> {
                habitacion.setDisponible(false);
                habitacionService.save(habitacion);
            });

            return hotelCerrado;
        });
    }
}
