package com.agentForgeBackend.models.hq.admin;

import com.agentForgeBackend.shared.defaultImplements.DefaultController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController extends DefaultController<AdminDTO, AdminMiniDTO, AdminListDTO, AdminForm, Long> {

    public AdminController(AdminServiceImpl service){
        super(service);
    }
}
