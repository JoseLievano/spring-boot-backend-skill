package com.authServer.models.hq.client;

import com.authServer.configuration.filter.JwtTokenService;
import com.authServer.exceptions.InvalidInsertDetails;
import com.authServer.exceptions.ItemAlreadyExist;
import com.authServer.exceptions.ItemNotFoundException;
import com.authServer.shared.defaultImplements.DefaultServiceImplements;
import com.authServer.shared.models.baseUser.UserRoles;
import com.authServer.shared.tools.FileSigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClientService extends DefaultServiceImplements<ClientDTO, ClientMiniDTO, ClientForm, ClientEntity, Long> {

    private final Logger logger = LoggerFactory.getLogger(ClientService.class);
    private final FileSigner fileSigner;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public ClientService(ClientRepository repository,
                         ClientMapper mapper,
                         FileSigner fileSigner,
                         PasswordEncoder passwordEncoder,
                         JwtTokenService jwtTokenService
    ) {
        super(repository, mapper);
        this.fileSigner = fileSigner;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public ClientDTO getOne(Long id) throws ItemNotFoundException {
        Optional<ClientEntity> optClient = repository.findById(id);
        if (optClient.isEmpty()){
            String msg = "Client " + id + " cannot be found";
            logger.error(msg);
            throw new ItemNotFoundException(msg);
        }
        return mapper.toDTO(optClient.get());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ClientMiniDTO insert(ClientForm clientForm) throws ItemNotFoundException, ItemAlreadyExist, InvalidInsertDetails {
        if (clientForm == null){
            logger.error("ClientForm is null");
            throw new InvalidInsertDetails("ClientForm is null");
        }
        String rawName = clientForm.getFirstName() + clientForm.getLastName();
        if (rawName.isEmpty() || clientForm.getFirstName().isEmpty() || clientForm.getLastName().isEmpty()){
            logger.error("Client name is null or empty");
            throw new InvalidInsertDetails("Client name is null or empty");
        }
        if (clientForm.getEmail() == null || clientForm.getEmail().isEmpty()){
            logger.error("Client email is null or empty");
            throw new InvalidInsertDetails("Client email is null or empty");
        }
        if (clientForm.getUsername() == null || clientForm.getUsername().isEmpty())
        {
            logger.error("Client username is null or empty");
            throw new InvalidInsertDetails("Client username is null or empty");
        }
        ClientRepository clientRepository = (ClientRepository) repository;
        Optional<ClientEntity> clientOPT = clientRepository.findByUsername(clientForm.getUsername());
        if (clientOPT.isPresent()){
            String msj = "Client with username " + clientForm.getUsername() + " already exist";
            logger.error(msj);
            throw new ItemAlreadyExist(msj);
        }
        clientOPT = clientRepository.findByEmail(clientForm.getEmail());
        if (clientOPT.isPresent()){
            String msj = "Client with email " + clientForm.getEmail() + " already exist";
            logger.error(msj);
            throw new ItemAlreadyExist(msj);
        }
        String hash = fileSigner.sign(clientForm.getEmail());
        ClientEntity toSave = mapper.toEntity(clientForm);
        if (toSave == null)
        {
            logger.error("ClientEntity is null");
            throw new InvalidInsertDetails("ClientEntity is null");
        }
        Set<UserRoles> roles = Set.of(UserRoles.CLIENT);
        toSave.setRoles(roles);
        toSave.setPassword(passwordEncoder.encode(hash));
        return mapper.toSmallDTO(repository.save(toSave));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(rollbackFor = { ItemNotFoundException.class, InvalidInsertDetails.class, ItemAlreadyExist.class })
    public ClientDTO update(Long id, ClientForm clientForm) throws ItemNotFoundException, InvalidInsertDetails {
        if (clientForm == null){
            logger.error("ClientForm is null");
            throw new InvalidInsertDetails("ClientForm is null");
        }

        ClientEntity toUpdate = repository.findById(id).orElseThrow(() -> {
            logger.error("Client not found id : " + id);
            return new ItemNotFoundException("Client not found");
        });

        ClientRepository clientRepository = (ClientRepository) repository;

        if (clientForm.getEmail() != null && !clientForm.getEmail().isEmpty()) {
            if (!clientForm.getEmail().equals(toUpdate.getEmail())) {
                Optional<ClientEntity> existingClient = clientRepository.findByEmail(clientForm.getEmail());
                if (existingClient.isPresent()) {
                    String msg = "Client with email " + clientForm.getEmail() + " already exists";
                    logger.error(msg);
                    throw new InvalidInsertDetails(msg);
                }
                toUpdate.setEmail(clientForm.getEmail());
            }
        }

        if (clientForm.getUsername() != null && !clientForm.getUsername().isEmpty()) {
            if (!clientForm.getUsername().equals(toUpdate.getUsername())) {
                Optional<ClientEntity> existingClient = clientRepository.findByUsername(clientForm.getUsername());
                if (existingClient.isPresent()) {
                    String msg = "Client with username " + clientForm.getUsername() + " already exists";
                    logger.error(msg);
                    throw new InvalidInsertDetails(msg);
                }
                toUpdate.setUsername(clientForm.getUsername());
            }
        }
        if (clientForm.getFirstName() != null && !clientForm.getFirstName().isEmpty()) {
            toUpdate.setFirstName(clientForm.getFirstName());
        }

        if (clientForm.getLastName() != null && !clientForm.getLastName().isEmpty()) {
            toUpdate.setLastName(clientForm.getLastName());
        }

        if ((clientForm.getEmail() != null && !clientForm.getEmail().equals(toUpdate.getEmail())) ) {
            String newHash = fileSigner.sign(toUpdate.getEmail());
            toUpdate.setPassword(passwordEncoder.encode(newHash));
        }

        return mapper.toDTO(repository.save(toUpdate));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String generateTokenForClient(String username) throws ItemNotFoundException{
        ClientEntity client = ((ClientRepository)repository).findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Client not found");
                    return new ItemNotFoundException("Client not found");
                });
        Collection<? extends GrantedAuthority> roles = client.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .toList();
        return jwtTokenService.generateToken(client, roles);
    }
}
