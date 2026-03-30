package com.authServer.models.hq.client;

import com.authServer.exceptions.ItemNotFoundException;
import com.authServer.shared.defaultImplements.DefaultController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/client")
public class ClientController extends DefaultController<ClientDTO, ClientMiniDTO, ClientForm, Long> {

    private final Logger logger = LoggerFactory.getLogger(ClientController.class);

    public ClientController(ClientService service){
        super(service);
    }

    @GetMapping("/token/{username}")
    public ResponseEntity<String> getClientToken(@PathVariable String username) throws ItemNotFoundException {
        String token = ((ClientService)defaultService).generateTokenForClient(username);
        if (token == null)
            throw new ItemNotFoundException("Client not found");
        return ResponseEntity.ok(token);
    }
}
