package co.com.crediya.usecase.user;

import co.com.crediya.model.rol.Rol;
import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import co.com.crediya.model.rol.gateways.RolRepository;
import co.com.crediya.model.usuario.security.PasswordService;
import co.com.crediya.model.usuario.security.TokenService;
import co.com.crediya.usecase.user.exceptions.InvalidCredentialsException;
import co.com.crediya.usecase.user.exceptions.RolNotFoundException;
import co.com.crediya.usecase.user.exceptions.UserNotFoundException;
import co.com.crediya.usecase.user.exceptions.InvalidUsuarioException;
import co.com.crediya.usecase.user.validators.UsuarioValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigInteger;
import java.time.LocalDate;

import static org.mockito.Mockito.*;

class UsuarioUseCaseTest {

    private UsuarioRepository usuarioRepository;
    private RolRepository rolRepository;
    private PasswordService passwordService;
    private TokenService tokenService;

    private UserUseCase usuarioUseCase;

    @BeforeEach
    void setUp() {
        usuarioRepository = Mockito.mock(UsuarioRepository.class);
        rolRepository = Mockito.mock(RolRepository.class);
        passwordService = Mockito.mock(PasswordService.class);
        tokenService = Mockito.mock(TokenService.class);

        usuarioUseCase = new UserUseCase(
                usuarioRepository,
                rolRepository,
                tokenService,
                passwordService
        );
    }

    @Test
    void createUser_ShouldReturnUsuario_WhenValid() {
        Usuario usuario = Usuario.builder()
                .id(BigInteger.ONE)
                .documentoIdentidad("123456789")
                .nombre("Carlos")
                .apellidos("Arteaga")
                .email("test@gmail.com")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .direccion("Calle Falsa 123")
                .idRol(BigInteger.ONE)
                .salarioBase(2000000L)
                .password("secret")
                .build();

        when(usuarioRepository.registrarUsuario(any(Usuario.class)))
                .thenReturn(Mono.just(usuario));

        Mono<Usuario> result = usuarioUseCase.createUser(usuario);

        StepVerifier.create(result)
                .expectNext(usuario)
                .verifyComplete();
    }

    @Test
    void createUser_ShouldReturnError_WhenInvalid() {

        Usuario usuarioInvalido = Usuario.builder()
                .documentoIdentidad("123456789")
                .nombre("Carlos")
                .apellidos("Arteaga")
                .email("")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .direccion("Calle Falsa 123")
                .idRol(BigInteger.ONE)
                .salarioBase(2000000L)
                .password("secret")
                .build();


        Mono<Usuario> result = usuarioUseCase.createUser(usuarioInvalido);
        
        StepVerifier.create(result)
                .expectError(InvalidUsuarioException.class)
                .verify();
    }

    @Test
    void login_debeRetornarTokenCuandoCredencialesSonValidas() {
        Usuario usuario = Usuario.builder()
                .email("test@test.com")
                .password("hashedPassword")
                .idRol(BigInteger.valueOf(1L))
                .build();

        String tokenEsperado = "jwtToken";

        when(usuarioRepository.getUsuarioByEmail("test@test.com"))
                .thenReturn(Mono.just(usuario));

        when(rolRepository.getRolById(BigInteger.valueOf(1L)))
                .thenReturn(Mono.just(
                        Rol.builder().id(BigInteger.valueOf(1L)).nombre("ADMIN").build()));

        when(passwordService.matches("1234", "hashedPassword"))
                .thenReturn(true);

        when(tokenService.generateToken("test@test.com", "ADMIN"))
                .thenReturn(tokenEsperado);

        Mono<String> resultado = usuarioUseCase.login("test@test.com", "1234");

        StepVerifier.create(resultado)
                .expectNext(tokenEsperado)
                .verifyComplete();

        verify(usuarioRepository, times(1)).getUsuarioByEmail("test@test.com");
        verify(rolRepository, times(1)).getRolById(BigInteger.valueOf(1L));
        verify(passwordService, times(1)).matches("1234", "hashedPassword");
        verify(tokenService, times(1)).generateToken("test@test.com", "ADMIN");
    }

    @Test
    void login_debeLanzarUserNotFoundExceptionSiUsuarioNoExiste() {
        when(usuarioRepository.getUsuarioByEmail("noexiste@test.com"))
                .thenReturn(Mono.empty());

        Mono<String> resultado = usuarioUseCase.login("noexiste@test.com", "1234");

        StepVerifier.create(resultado)
                .expectError(UserNotFoundException.class)
                .verify();

        verify(usuarioRepository, times(1)).getUsuarioByEmail("noexiste@test.com");
        verifyNoInteractions(rolRepository, passwordService, tokenService);
    }

    @Test
    void login_debeLanzarRolNotFoundExceptionSiRolNoExiste() {
        Usuario usuario = Usuario.builder()
                .email("test@test.com")
                .password("hashedPassword")
                .idRol(BigInteger.valueOf(99L))
                .build();

        when(usuarioRepository.getUsuarioByEmail("test@test.com"))
                .thenReturn(Mono.just(usuario));

        when(rolRepository.getRolById(BigInteger.valueOf(99L))).thenReturn(Mono.empty());

        Mono<String> resultado = usuarioUseCase.login("test@test.com", "1234");

        StepVerifier.create(resultado)
                .expectError(RolNotFoundException.class)
                .verify();

        verify(usuarioRepository, times(1)).getUsuarioByEmail("test@test.com");
        verify(rolRepository, times(1)).getRolById(BigInteger.valueOf(99L));
        verifyNoInteractions(passwordService, tokenService);
    }

    @Test
    void login_debeLanzarInvalidCredentialsExceptionSiPasswordEsIncorrecta() {
        Usuario usuario = Usuario.builder()
                .email("test@test.com")
                .password("hashedPassword")
                .idRol(BigInteger.valueOf(1L))
                .build();

        when(usuarioRepository.getUsuarioByEmail("test@test.com"))
                .thenReturn(Mono.just(usuario));

        when(rolRepository.getRolById(BigInteger.valueOf(1L)))
                .thenReturn(Mono.just(
                        Rol.builder().id(BigInteger.valueOf(1L)).nombre("ADMIN").build()));

        when(passwordService.matches("wrongpass", "hashedPassword"))
                .thenReturn(false);

        Mono<String> resultado = usuarioUseCase.login("test@test.com", "wrongpass");

        StepVerifier.create(resultado)
                .expectError(InvalidCredentialsException.class)
                .verify();

        verify(usuarioRepository, times(1)).getUsuarioByEmail("test@test.com");
        verify(rolRepository, times(1)).getRolById(BigInteger.valueOf(1L));
        verify(passwordService, times(1)).matches("wrongpass", "hashedPassword");
        verifyNoInteractions(tokenService);
    }


    private Usuario buildValidUsuario() {
        return Usuario.builder()
                .nombre("Carlos")
                .apellidos("Arteaga")
                .fechaNacimiento(LocalDate.of(1990, 1, 1))
                .direccion("Calle 123")
                .salarioBase((long) 5_000_000.0)
                .email("carlos@test.com")
                .idRol(BigInteger.valueOf(1L))
                .build();
    }

    @Test
    void validator_debeRetornarUsuarioValido() {
        Usuario usuario = buildValidUsuario();

        StepVerifier.create(UsuarioValidator.validate(usuario))
                .expectNext(usuario)
                .verifyComplete();
    }

    @Test
    void validator_debeLanzarErrorSiFaltanCampos() {
        Usuario usuario = new Usuario(); // vacío

        StepVerifier.create(UsuarioValidator.validate(usuario))
                .expectError(InvalidUsuarioException.class)
                .verify();
    }

    @Test
    void validator_debeLanzarErrorSiEmailEsInvalido() {
        Usuario usuario = buildValidUsuario();
        usuario.setEmail("correoInvalido");

        StepVerifier.create(UsuarioValidator.validate(usuario))
                .expectError(InvalidUsuarioException.class)
                .verify();

    }

    @Test
    void validator_debeLanzarErrorSiSalarioFueraDeRango() {
        Usuario usuario = buildValidUsuario();
        usuario.setSalarioBase((long) 20_000_000.0);

        StepVerifier.create(UsuarioValidator.validate(usuario))
                .expectError(InvalidUsuarioException.class)
                .verify();

    }
}
