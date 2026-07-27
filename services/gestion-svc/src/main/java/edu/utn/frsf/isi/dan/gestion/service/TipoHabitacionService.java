package edu.utn.frsf.isi.dan.gestion.service;

import edu.utn.frsf.isi.dan.gestion.dao.TipoHabitacionRepository;
import edu.utn.frsf.isi.dan.gestion.model.TipoHabitacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TipoHabitacionService {
    @Autowired
    private TipoHabitacionRepository tipoHabitacionRepository;

    public TipoHabitacion save(TipoHabitacion tipoHabitacion) {
        // El id no es autogenerado (tipo_habitacion es un catalogo fijo, precargado por schema
        // con ids del 1 al 9): sin este chequeo, Hibernate rechaza el insert con una excepcion
        // de bajo nivel que el ControllerAdvisor termina devolviendo como 500.
        if (tipoHabitacion.getId() == null) {
            throw new IllegalArgumentException("El id del tipo de habitacion es requerido");
        }
        return tipoHabitacionRepository.save(tipoHabitacion);
    }

    public void deleteById(Integer id) {
        tipoHabitacionRepository.deleteById(id);
    }

    public Optional<TipoHabitacion> findById(Integer id) {
        return tipoHabitacionRepository.findById(id);
    }

    public List<TipoHabitacion> findAll() {
        return tipoHabitacionRepository.findAll();
    }
}
