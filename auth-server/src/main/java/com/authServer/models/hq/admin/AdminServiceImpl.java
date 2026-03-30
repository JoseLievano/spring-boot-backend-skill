package com.authServer.models.hq.admin;

import com.authServer.exceptions.InvalidInsertDetails;
import com.authServer.exceptions.ItemAlreadyExist;
import com.authServer.exceptions.ItemNotFoundException;
import com.authServer.shared.defaultImplements.DefaultServiceImplements;
import com.authServer.shared.models.baseUser.UserRoles;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AdminServiceImpl extends DefaultServiceImplements <AdminDTO, AdminMiniDTO, AdminForm, AdminEntity, Long>  {

    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(
            AdminRepository repository,
            AdminMapper mapper,
            PasswordEncoder passwordEncoder)
    {
        super(repository, mapper);
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
