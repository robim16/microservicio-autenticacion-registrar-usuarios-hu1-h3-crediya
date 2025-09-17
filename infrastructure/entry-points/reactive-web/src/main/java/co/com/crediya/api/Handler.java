package co.com.crediya.api;

import co.com.crediya.api.dto.CreateUserDTO;
import co.com.crediya.api.dto.LoginRequestDTO;
import co.com.crediya.api.dto.LoginResponseDTO;
import co.com.crediya.api.dto.UsuarioResponseDTO;
import co.com.crediya.api.mapper.user.UserDTOMapper;
import co.com.crediya.usecase.user.IUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.math.BigInteger;


@Component
@RequiredArgsConstructor
public class Handler {
    private final IUserUseCase userUseCase;
    private final TransactionalOperator transactionalOperator;
    private final UserDTOMapper userDTOMapper;
    private static final Logger log = LoggerFactory.getLogger(Handler.class);


    @Operation(
            summary = "Crear un nuevo usuario",
            description = "Crea un usuario a partir de un DTO con información personal, rol y salario",
            security = @SecurityRequirement(name = "BearerAuth"),
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateUserDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Usuario creado correctamente",
                            content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Datos inválidos"
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "El email ya existe"
                    )
            }
    )
    public Mono<ServerResponse> listenSaveUser(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateUserDTO.class)
                .flatMap(dto -> userUseCase.createUser(userDTOMapper.mapToEntity(dto)))
                .flatMap(savedUser -> {
                    UsuarioResponseDTO usuarioResponseDTO = userDTOMapper.mapToResponseDTO(savedUser);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(usuarioResponseDTO);

                });
    }

    @Operation(
            summary = "Buscar usuario por ID",
            description = "Obtiene un usuario existente a partir de su identificador único",
            security = @SecurityRequirement(name = "BearerAuth"),
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Identificador del usuario",
                            required = true,
                            example = "123",
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Usuario encontrado",
                            content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Usuario no encontrado"
                    )
            }
    )

    public Mono<ServerResponse> listenGetUserById(ServerRequest serverRequest) {
        BigInteger id = BigInteger.valueOf(Integer.parseInt(serverRequest.pathVariable("id")));
        return userUseCase.getUsuarioById(id)
                .flatMap(user -> {
                    UsuarioResponseDTO usuarioResponseDTO = userDTOMapper.mapToResponseDTO(user);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(usuarioResponseDTO);
                })
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    @Operation(
            summary = "Buscar usuario por email",
            description = "Obtiene un usuario existente a partir de su email único",
            security = @SecurityRequirement(name = "BearerAuth"),
            parameters = {
                    @Parameter(
                            name = "email",
                            description = "Email del usuario",
                            required = true,
                            example = "user123@gmail.com",
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Usuario encontrado",
                            content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Usuario no encontrado"
                    )
            }
    )

    public Mono<ServerResponse> listenGetUserByEmail(ServerRequest serverRequest) {
        String email = serverRequest.pathVariable("email");
        return userUseCase.getUsuarioByEmail(email)
                .flatMap(user -> {
                    UsuarioResponseDTO usuarioResponseDTO = userDTOMapper.mapToResponseDTO(user);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(usuarioResponseDTO);
                })
                .switchIfEmpty(ServerResponse.notFound().build());
    }


    @Operation(
            summary = "Loguear usuario",
            description = "Loguear un usuario a partir de email y password",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequestDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Login exitoso",
                            content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Datos inválidos"
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "El email ya existe"
                    )
            }
    )
    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequestDTO.class)
                .flatMap(loginRequest ->
                        userUseCase.login(loginRequest.email(), loginRequest.password())
                                .map(LoginResponseDTO::new)
                                .flatMap(response -> ServerResponse.ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(response))
                );
    }

}
