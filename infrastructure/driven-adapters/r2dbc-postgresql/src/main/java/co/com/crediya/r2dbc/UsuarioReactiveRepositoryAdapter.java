package co.com.crediya.r2dbc;

import co.com.crediya.model.usuario.Usuario;
import co.com.crediya.model.usuario.gateways.UsuarioRepository;
import co.com.crediya.model.usuario.security.PasswordService;
import co.com.crediya.r2dbc.entity.UsuarioEntity;
import co.com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class UsuarioReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        Usuario,
        UsuarioEntity,
        BigInteger,
        UsuarioReactiveRepository
        > implements UsuarioRepository {

    private static final Logger log = LoggerFactory.getLogger(UsuarioReactiveRepositoryAdapter.class);
    private final TransactionalOperator transactionalOperator;
    private final PasswordService passwordService;
    public UsuarioReactiveRepositoryAdapter(UsuarioReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator, PasswordService passwordService) {
        super(repository, mapper, d -> mapper.map(d, Usuario.class));
        this.transactionalOperator = transactionalOperator;
        this.passwordService = passwordService;
    }

    @Override
    public Mono<Usuario> registrarUsuario(Usuario usuario) {

        return getUsuarioByEmail(usuario.getEmail())
                .flatMap(existingUserByEmail -> {
                    log.warn("El email {} ya está en uso. Usuario existente con ID: {}",
                            usuario.getEmail(), existingUserByEmail.getId());
                    return Mono.<Usuario>error(new IllegalArgumentException("El email ya está registrado"));
                })
                .switchIfEmpty(
                        Mono.defer(() -> {
                            UsuarioEntity usuarioEntity = mapper.map(usuario, UsuarioEntity.class);
                            if (usuario.getIdRol() == null) {
                                usuarioEntity.setIdRol(usuario.getIdRol());
                            }
                            String hashedPassword = passwordService.encode(usuario.getPassword());
                            usuarioEntity.setPassword(hashedPassword);
                            return repository.save(usuarioEntity)
                                    .map(savedUserEntity -> mapper.map(savedUserEntity, Usuario.class));

                        })
                )
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<Usuario> getUsuarioById(BigInteger id) {
        return repository.findById(id)
                .map(entity -> mapper.map(entity, Usuario.class));
    }


    @Override
    public Mono<Usuario> getUsuarioByEmail(String email) {
        return repository.findByEmail(email)
                .map(entity -> mapper.map(entity, Usuario.class));
    }
}
