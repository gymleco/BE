package kr.co.gymleco.service.inquiry;

import kr.co.gymleco.config.GymlecoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class InquiryNotificationListener {

    private static final Logger log =
        LoggerFactory.getLogger(InquiryNotificationListener.class);

    private final JavaMailSender mailSender;
    private final GymlecoProperties.Inquiry config;

    public InquiryNotificationListener(JavaMailSender mailSender,
                                       GymlecoProperties properties) {
        this.mailSender = mailSender;
        this.config = properties.inquiry();
    }
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInquiryReceived(InquiryReceivedEvent event) {
        String to = config.notifyTo();
        if (to == null || to.isBlank()) {
            log.warn("문의 알림 수신처가 설정되지 않았습니다. 문의 #{} 알림을 건너뜁니다.",
                event.inquiryId());
            return;
        }

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject("[짐레코] 새 문의 — " + event.type().label());
        mail.setText("""
                새 문의가 접수되었습니다.

                유형   : %s
                이름   : %s
                업체   : %s
                지역   : %s

                연락처와 상세 내용은 관리자 화면에서 확인해 주세요.
                (보안상 메일에는 연락처를 포함하지 않습니다)
                """.formatted(
            event.type().label(),
            event.maskedName(),
            nullToDash(event.company()),
            nullToDash(event.region())));

        try {
            mailSender.send(mail);
            log.info("문의 알림 발송 완료: #{}", event.inquiryId());
        } catch (MailException e) {
            log.error("문의 알림 발송 실패 (접수는 정상). id={} 사유={}",
                event.inquiryId(), e.getMessage());
        }
    }
    private static String nullToDash(String value) {
        return (value == null || value.isBlank()) ? "—" : value;
    }
}
