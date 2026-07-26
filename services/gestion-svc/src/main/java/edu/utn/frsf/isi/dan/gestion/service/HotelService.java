package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.dao.HotelRepository;
import edu.utn.frsf.isi.dan.gestion.model.Hotel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HotelService {
    @Autowired
    private HotelRepository hotelRepository;

    public Hotel save(Hotel hotel) {
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

    public Optional<Hotel> cerrar(Integer id) {
        return hotelRepository.findById(id).map(hotel -> {
            hotel.setCerrado(true);
            return hotelRepository.save(hotel);
        });
    }
}
