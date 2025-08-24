package co.com.bancodebogota.bdbapprovals.infrastructure.notification;

import co.com.bancodebogota.bdbapprovals.application.port.out.EmailPort;
import co.com.bancodebogota.bdbapprovals.domain.model.ApprovalRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

@Component
@Lazy
@Profile("!dev")
public class SesEmailAdapter implements EmailPort {

    private final SesV2Client ses;
    private final String from;

    public SesEmailAdapter(@Value("${aws.region:us-east-1}") String region,
                           @Value("${app.email.from:no-reply@example.com}") String from) {
        this.ses = SesV2Client.builder().region(Region.of(region)).build();
        this.from = from;
    }

    @Override
    public void sendRequestCreated(ApprovalRequest req) {
        String subject = "[BDB Approvals] Nueva solicitud pendiente";
        String body = """
        <h3>Nueva solicitud</h3>
        <p><b>Título:</b> %s</p>
        <p><b>Solicitante:</b> %s</p>
        <p>Por favor revisar y aprobar/rechazar.</p>
        """.formatted(req.getTitle(), req.getRequesterUpn());
        sendHtml(req.getApproverUpn(), subject, body);
    }

    @Override
    public void sendRequestApproved(ApprovalRequest req, String comment) {
        String subject = "[BDB Approvals] Solicitud APROBADA";
        String body = """
        <p>Tu solicitud <b>%s</b> fue <b>APROBADA</b>.</p>
        <p><b>Comentario:</b> %s</p>
        """.formatted(req.getTitle(), comment == null ? "(sin comentario)" : comment);
        sendHtml(req.getRequesterUpn(), subject, body);
    }

    @Override
    public void sendRequestRejected(ApprovalRequest req, String comment) {
        String subject = "[BDB Approvals] Solicitud RECHAZADA";
        String body = """
        <p>Tu solicitud <b>%s</b> fue <b>RECHAZADA</b>.</p>
        <p><b>Comentario:</b> %s</p>
        """.formatted(req.getTitle(), comment);
        sendHtml(req.getRequesterUpn(), subject, body);
    }

    private void sendHtml(String to, String subject, String html) {
        var content = Content.builder().data(html).build();
        var sub = Content.builder().data(subject).build();
        var body = Body.builder().html(content).build();
        var msg = Message.builder().subject(sub).body(body).build();
        var email = EmailContent.builder().simple(msg).build();

        var req = SendEmailRequest.builder()
                .destination(Destination.builder().toAddresses(to).build())
                .content(email).fromEmailAddress(from).build();

        ses.sendEmail(req);
    }
}
