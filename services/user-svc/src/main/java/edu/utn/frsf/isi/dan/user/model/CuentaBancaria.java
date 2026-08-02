package edu.utn.frsf.isi.dan.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cuentas_bancarias")
@Data
@NoArgsConstructor
public class CuentaBancaria {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "numero_cuenta")
  private String numeroCuenta;

  private String cbu;
  private String alias;

  @ManyToOne
  @JoinColumn(name = "banco_id")
  private Banco banco;

  @OneToOne(mappedBy = "cuentaBancaria")
  @JsonIgnore
  private Propietario propietario;
}
