package edu.utn.frsf.isi.dan.gestion.controller;

import edu.utn.frsf.isi.dan.gestion.model.Amenity;
import edu.utn.frsf.isi.dan.gestion.model.Hotel;
import edu.utn.frsf.isi.dan.gestion.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hoteles")
public class HotelController {
    @Autowired
    private HotelService hotelService;

    @PostMapping
    public ResponseEntity<Hotel> create(@RequestBody Hotel hotel) {
        return ResponseEntity.ok(hotelService.save(hotel));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hotel> getById(@PathVariable Integer id) {
        return hotelService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Hotel> getAll(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Integer categoriaMinima,
            @RequestParam(required = false) String domicilio,
            @RequestParam(required = false) Boolean cerrado,
            @RequestParam(required = false) Amenity amenity) {
        if (nombre == null && categoriaMinima == null && domicilio == null && cerrado == null && amenity == null) {
            return hotelService.findAll();
        }
        return hotelService.buscar(nombre, categoriaMinima, domicilio, cerrado, amenity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Hotel> update(@PathVariable Integer id, @RequestBody Hotel hotel) {
        return hotelService.actualizarDatosPermitidos(id, hotel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<Hotel> cerrar(@PathVariable Integer id) {
        return hotelService.cerrar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/amenities")
    public ResponseEntity<Hotel> agregarAmenities(@PathVariable Integer id, @RequestBody List<Amenity> amenities) {
        return ResponseEntity.ok(hotelService.agregarAmenities(id, amenities));
    }

    @DeleteMapping("/{id}/amenities/{amenity}")
    public ResponseEntity<Void> quitarAmenity(@PathVariable Integer id, @PathVariable Amenity amenity) {
        hotelService.quitarAmenity(id, amenity);
        return ResponseEntity.noContent().build();
    }
}
