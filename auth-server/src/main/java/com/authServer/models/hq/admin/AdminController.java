package com.authServer.models.hq.admin;

import com.authServer.shared.defaultImplements.DefaultController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController extends DefaultController<AdminDTO, AdminMiniDTO, AdminForm, Long> {

    public AdminController(AdminServiceImpl service){
        super(service);
    }
}
