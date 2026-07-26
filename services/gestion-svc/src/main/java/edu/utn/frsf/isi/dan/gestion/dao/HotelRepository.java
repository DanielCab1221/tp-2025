package edu.utn.frsf.isi.dan.gestion.dao;

import edu.utn.frsf.isi.dan.gestion.model.Amenity;
import edu.utn.frsf.isi.dan.gestion.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Integer> {
    boolean existsByCuit(String cuit);

    // El CAST a string es necesario: si el parametro viaja null, Postgres no puede inferir su
    // tipo dentro de LOWER()/CONCAT() y falla con "function lower(bytea) does not exist".
    @Query("SELECT DISTINCT h FROM Hotel h LEFT JOIN h.amenities a WHERE "
            + "(:nombre IS NULL OR LOWER(h.nombre) LIKE LOWER(CONCAT('%', CAST(:nombre AS string), '%'))) "
            + "AND (:categoriaMinima IS NULL OR h.categoria >= :categoriaMinima) "
            + "AND (:domicilio IS NULL OR LOWER(h.domicilio) LIKE LOWER(CONCAT('%', CAST(:domicilio AS string), '%'))) "
            + "AND (:cerrado IS NULL OR h.cerrado = :cerrado) "
            + "AND (:amenity IS NULL OR a.amenity = :amenity)")
    List<Hotel> buscar(@Param("nombre") String nombre,
                        @Param("categoriaMinima") Integer categoriaMinima,
                        @Param("domicilio") String domicilio,
                        @Param("cerrado") Boolean cerrado,
                        @Param("amenity") Amenity amenity);
}
