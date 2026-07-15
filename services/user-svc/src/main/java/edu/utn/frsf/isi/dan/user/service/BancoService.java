package edu.utn.frsf.isi.dan.user.service;

import edu.utn.frsf.isi.dan.user.dao.BancoRepository;
import edu.utn.frsf.isi.dan.user.dto.BancoDTO;
import edu.utn.frsf.isi.dan.user.model.Banco;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BancoService {

    @Autowired
    private BancoRepository bancoRepository;

    public Banco crearBanco(BancoDTO dto) {
        Banco banco = Banco.builder()
                .nombre(dto.nombre())
                .build();
        return bancoRepository.save(banco);
    }

    public List<Banco> listarBancos() {
        return bancoRepository.findAll();
    }

    public Optional<Banco> obtenerBancoPorId(Integer id) {
        return bancoRepository.findById(id);
    }

    public Banco actualizarBanco(Integer id, BancoDTO dto) {
        Optional<Banco> bancoOptional = bancoRepository.findById(id);
        if (bancoOptional.isEmpty()) {
            throw new IllegalArgumentException("Banco no encontrado con ID: " + id);
        }

        Banco banco = bancoOptional.get();
        banco.setNombre(dto.nombre());
        return bancoRepository.save(banco);
    }

    public void eliminarBanco(Integer id) {
        if (!bancoRepository.existsById(id)) {
            throw new IllegalArgumentException("Banco no encontrado con ID: " + id);
        }
        bancoRepository.deleteById(id);
    }
}
