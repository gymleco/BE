package kr.co.gymleco.batch;
import kr.co.gymleco.domain.inquiry.Inquiry;
import kr.co.gymleco.domain.inquiry.InquiryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

//개인정보 자동 파기 , 개인정보처리 방침에 "처리 완료 후 1년"
@Component
public class InquiryPurgeScheduler {
    private static final Logger log = LoggerFactory.getLogger(InquiryPurgeScheduler.class);
    private static final int BATCH_SIZE =200;
    private final InquiryRepository inquiryRepository;
    public InquiryPurgeScheduler(InquiryRepository inquiryRepository){
        this.inquiryRepository = inquiryRepository;
    }
    /** 매일 새벽 4시. 트래픽이 가장 적은 시간대. */
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void purgeExpired(){
        Instant now = Instant.now();
        int total = 0;
        while (true){
            int deleted = purgeChunk(now);
            total += deleted;
            if(deleted < BATCH_SIZE){
                break;
            }
        }
        if(total > 0){
            log.info("개인정보 자동 파기 완료: {}건", total);
        }
    }
    @Transactional
    protected int purgeChunk(Instant now) {
        List<Inquiry> targets = inquiryRepository.findPurgeTargets(
            now, PageRequest.of(0, BATCH_SIZE));

        if (targets.isEmpty()) {
            return 0;
        }
        inquiryRepository.deleteAll(targets);
        return targets.size();
    }
}
