package co.com.crediya.model.usuario;
import co.com.crediya.model.rol.Rol;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDate;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Usuario {
    private BigInteger id;
    private String documentoIdentidad;
    private String nombre;
    private String apellidos;
    private String email;
    private LocalDate fechaNacimiento;
    private String direccion;
    private BigInteger idRol;
    private Long salarioBase;
    private String password;

}
