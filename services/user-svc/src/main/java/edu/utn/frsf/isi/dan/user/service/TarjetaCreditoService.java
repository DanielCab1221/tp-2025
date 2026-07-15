package edu.utn.frsf.isi.dan.user.service;

import edu.utn.frsf.isi.dan.user.dao.BancoRepository;
import edu.utn.frsf.isi.dan.user.dao.TarjetaCreditoRepository;
import edu.utn.frsf.isi.dan.user.dao.UsuarioRepository;
import edu.utn.frsf.isi.dan.user.dto.TarjetaCreditoDTO;
import edu.utn.frsf.isi.dan.user.model.Banco;
import edu.utn.frsf.isi.dan.user.model.Huesped;
import edu.utn.frsf.isi.dan.user.model.TarjetaCredito;
import edu.utn.frsf.isi.dan.user.model.Usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TarjetaCreditoService {

    @Autowired
    private TarjetaCreditoRepository tarjetaCreditoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BancoRepository bancoRepository;

    @Transactional
    public TarjetaCredito agregarTarjeta(Integer huespedId, TarjetaCreditoDTO dto) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(huespedId);
        if (usuarioOptional.isEmpty() || !(usuarioOptional.get() instanceof Huesped)) {
            throw new IllegalArgumentException("Huésped no encontrado con ID: " + huespedId);
        }

        Huesped huesped = (Huesped) usuarioOptional.get();

        Optional<Banco> bancoOptional = bancoRepository.findById(dto.idBanco());
        if (bancoOptional.isEmpty()) {
            throw new IllegalArgumentException("Banco no encontrado con ID: " + dto.idBanco());
        }

        if (dto.esPrincipal() != null && dto.esPrincipal()) {
            Optional<TarjetaCredito> principalActual = tarjetaCreditoRepository
                    .findByHuespedIdAndEsPrincipalTrue(huespedId);
            principalActual.ifPresent(tarjeta -> {
                tarjeta.setEsPrincipal(false);
                tarjetaCreditoRepository.save(tarjeta);
            });
        }

        TarjetaCredito tarjeta = TarjetaCredito.builder()
                .numero(dto.numero())
                .nombreTitular(dto.nombreTitular())
                .fechaVencimiento(dto.fechaVencimiento())
                .cvc(dto.cvc())
                .esPrincipal(dto.esPrincipal() != null ? dto.esPrincipal() : false)
                .banco(bancoOptional.get())
                .huesped(huesped)
                .build();

        return tarjetaCreditoRepository.save(tarjeta);
    }

    public void eliminarTarjeta(Integer tarjetaId) {
        Optional<TarjetaCredito> tarjetaOptional = tarjetaCreditoRepository.findById(Long.valueOf(tarjetaId));
        if (tarjetaOptional.isEmpty()) {
            throw new IllegalArgumentException("Tarjeta no encontrada con ID: " + tarjetaId);
        }

        TarjetaCredito tarjeta = tarjetaOptional.get();
        if (tarjeta.getEsPrincipal()) {
            throw new IllegalStateException("No se puede eliminar la tarjeta principal");
        }

        tarjetaCreditoRepository.deleteById(Long.valueOf(tarjetaId));
    }

    @Transactional
    public TarjetaCredito cambiarTarjetaPrincipal(Integer huespedId, Integer tarjetaId) {
        Optional<TarjetaCredito> tarjetaOptional = tarjetaCreditoRepository.findById(Long.valueOf(tarjetaId));
        if (tarjetaOptional.isEmpty()) {
            throw new IllegalArgumentException("Tarjeta no encontrada con ID: " + tarjetaId);
        }

        TarjetaCredito tarjeta = tarjetaOptional.get();
        if (!tarjeta.getHuesped().getId().equals(huespedId)) {
            throw new IllegalArgumentException("La tarjeta no pertenece al huésped especificado");
        }

        Optional<TarjetaCredito> principalActual = tarjetaCreditoRepository
                .findByHuespedIdAndEsPrincipalTrue(huespedId);
        principalActual.ifPresent(t -> {
            t.setEsPrincipal(false);
            tarjetaCreditoRepository.save(t);
        });

        tarjeta.setEsPrincipal(true);
        return tarjetaCreditoRepository.save(tarjeta);
    }

    public List<TarjetaCredito> listarTarjetasDeHuesped(Integer huespedId) {
        return tarjetaCreditoRepository.findByHuespedId(huespedId);
    }
}
