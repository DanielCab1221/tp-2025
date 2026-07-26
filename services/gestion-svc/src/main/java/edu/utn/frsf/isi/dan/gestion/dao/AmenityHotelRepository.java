package edu.utn.frsf.isi.dan.gestion.dao;

import edu.utn.frsf.isi.dan.gestion.model.Amenity;
import edu.utn.frsf.isi.dan.gestion.model.AmenityHotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmenityHotelRepository extends JpaRepository<AmenityHotel, Long> {
    List<AmenityHotel> findByHotelId(Integer hotelId);

    Optional<AmenityHotel> findByHotelIdAndAmenity(Integer hotelId, Amenity amenity);
}
