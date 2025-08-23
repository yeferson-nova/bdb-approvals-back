package co.com.bancodebogota.bdbapprovals.application.port.in;

import java.util.UUID;

public interface CommentRequestUseCase {
    void comment(UUID id, String actorUpn, String comment);
}