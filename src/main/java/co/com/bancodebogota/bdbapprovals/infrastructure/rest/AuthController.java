package co.com.bancodebogota.bdbapprovals.infrastructure.rest;

import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.CurrentUserDto;
import co.com.bancodebogota.bdbapprovals.infrastructure.security.CurrentUserParam;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public CurrentUserDto me(@CurrentUserParam CurrentUserDto me) {
        return me;
    }
}
