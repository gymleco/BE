package kr.co.gymleco.infra.revalidate;
import kr.co.gymleco.config.GymlecoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;
@Component
public class SiteRevalidationListener {
    private static final Logger log = LoggerFactory.getLogger(SiteRevalidationListener.class);
    private final GymlecoProperties.Revalidate config;
    private final RestClient restClient;
    public SiteRevalidationListener(GymlecoProperties properties){
        this.config = properties.revalidate();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onContentChanged(ContentChangedEvent event){
        if(!config.isConfigured()){
            log.debug("재검증 설정 없이 건너뜁니다: {}", event.paths());
            return;
        }
        try{
            restClient.post()
                .uri(config.url())
                .header("Authorization", "Bearer " + config.token())
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("paths", event.paths()))
                .retrieve()
                .toBodilessEntity();
            log.info("사이트 재검증 완료: {}", event.paths());
        }catch (Exception e){
            log.error("사이트 재검증 실패. path={} 사유 ={} ", event.paths(), e.getMessage());
        }
    }
}
