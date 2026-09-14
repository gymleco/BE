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
        // 제목에 번호를 넣는다. 카톡으로 «[접수번호 GL-...]» 가 오면
        // 메일함에서 그 번호로 바로 찾을 수 있어야 한다.
        String subject = event.isRepeat()
            ? "[짐레코] 재문의 " + event.referenceNo()
                + " (앞 접수 " + event.originalReferenceNo() + ") — " + event.type().label()
            : "[짐레코] 새 문의 " + event.referenceNo() + " — " + event.type().label();
        mail.setSubject(subject);

        String repeatNote = event.isRepeat()
            ? "※ 같은 연락처로 " + event.originalReferenceNo()
                + " 가 먼저 접수돼 있습니다. 같은 분이니 한 번만 연락해 주세요.\n\n"
            : "";

        mail.setText(repeatNote + """
                새 문의가 접수되었습니다.

                접수번호 : %s
                유형     : %s
                이름     : %s
                업체     : %s
                지역     : %s

                연락처와 상세 내용은 관리자 화면에서 확인해 주세요.
                (보안상 메일에는 연락처를 포함하지 않습니다)
                """.formatted(
            event.referenceNo(),
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
