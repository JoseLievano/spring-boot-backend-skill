package com.agentForgeBackend.models.hq.admin;

import com.agentForgeBackend.exceptions.InvalidInsertDetails;
import com.agentForgeBackend.exceptions.ItemAlreadyExist;
import com.agentForgeBackend.exceptions.ItemNotFoundException;
import com.agentForgeBackend.shared.defaultImplements.DefaultServiceImplements;
import com.agentForgeBackend.shared.models.baseUser.UserRoles;
import com.agentForgeBackend.shared.query.PageableFactory;
import com.agentForgeBackend.shared.query.QueryPredicateBuilder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AdminServiceImpl extends DefaultServiceImplements <AdminDTO, AdminMiniDTO, AdminListDTO, AdminForm, AdminEntity, Long>  {

    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(
            AdminRepository repository,
            AdminMapper mapper,
            AdminQueryProfile queryProfile,
            PageableFactory<AdminEntity> pageableFactory,
            QueryPredicateBuilder<AdminEntity> queryPredicateBuilder,
            PasswordEncoder passwordEncoder)
    {
        super(repository, mapper, queryProfile, pageableFactory, queryPredicateBuilder);
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public AdminMiniDTO insert(AdminForm adminForm)
            throws ItemNotFoundException, ItemAlreadyExist, InvalidInsertDetails {
        AdminEntity toSave = mapper.toEntity(adminForm);
        if (toSave.getUsername() == null
                || toSave.getEmail() == null
                || toSave.getPassword() == null
        )
            throw new InvalidInsertDetails("Admin user needs a username, email and a password");

        AdminRepository repository = (AdminRepository) this.repository;
        //Check if user already exist in our DB, if user exist, throws ItemAlreadyExist exception
        if (repository.findByUsername(toSave.getUsername()) != null || repository.findByEmail(toSave.getEmail()) != null)
            throw new ItemAlreadyExist("Admin user already exist");

        //Encode password
        toSave.setPassword(passwordEncoder.encode(toSave.getPassword()));

        //Set roles
        Set<UserRoles> roles = Set.of(UserRoles.ADMIN);
        toSave.setRoles(roles);

        toSave = repository.save(toSave);

        return mapper.toSmallDTO(toSave);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDTO getOne(Long aLong) throws ItemNotFoundException {
        AdminRepository repository = (AdminRepository) this.repository;
        AdminEntity adminEntity = repository.findById(aLong).orElseThrow(() -> new ItemNotFoundException("Admin user not found"));
        return mapper.toDTO(adminEntity);
    }
}
