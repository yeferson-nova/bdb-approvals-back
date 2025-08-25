package co.com.bancodebogota.bdbapprovals.infrastructure.security;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUserParam {}
