package co.com.crediya.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigInteger;
import java.time.LocalDate;

@Table("usuarios")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UsuarioEntity {
    @Id
    private BigInteger id;

    @Column("documento_identidad")
    private String documentoIdentidad;

    private String nombre;
    private String apellidos;
    private String email;

    @Column("fecha_nacimiento")
    private LocalDate fechaNacimiento;

    private String direccion;

    @Column("id_rol")
    private BigInteger idRol;

    @Column("salario_base")
    private Long salarioBase;

    private String password;
}