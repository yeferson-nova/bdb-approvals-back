package co.com.bancodebogota.bdbapprovals.infrastructure.security;

import co.com.bancodebogota.bdbapprovals.infrastructure.rest.dto.CurrentUserDto;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.*;

import java.util.Optional;

public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserParam.class)
                && parameter.getParameterType().equals(CurrentUserDto.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }

        String upn = Optional.ofNullable(jwt.getClaimAsString("preferred_username"))
                .orElse(Optional.ofNullable(jwt.getClaimAsString("upn"))
                        .orElse(jwt.getClaimAsString("oid")));
        String oid = jwt.getClaimAsString("oid");
        String name = Optional.ofNullable(jwt.getClaimAsString("name")).orElse(upn);
        String email = Optional.ofNullable(jwt.getClaimAsString("email")).orElse(upn);

        return new CurrentUserDto(upn, oid, name, email);
    }
}