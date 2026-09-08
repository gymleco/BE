package kr.co.gymleco.service.machine;

import kr.co.gymleco.domain.machine.*;
import kr.co.gymleco.domain.product.Product;
import kr.co.gymleco.service.inquiry.RateLimitExceededException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class VerificationService {
    private static final int LOOKUPS_PER_HOUR = 60;
    private final MachineUnitRepository units;
    private final MachineUnitLookupRepository lookups;
    public VerificationService(MachineUnitRepository units, MachineUnitLookupRepository lookups){
        this.units = units;
        this.lookups = lookups;
    }
    private void guardEnumeration(String sourceIp){
        if(sourceIp == null || sourceIp.isBlank()){
            return;
        }
        long recent = lookups.countBySourceIpAndCreatedAtAfter(sourceIp, Instant.now().minus(Duration.ofHours(1)));
        if(recent >= LOOKUPS_PER_HOUR){
            throw new RateLimitExceededException();
        }
    }
    private void record(Long unitId, String serialTried, LookupResult result, String sourceIp, String userAgent){
        lookups.save(MachineUnitLookup.record(unitId, serialTried, result, sourceIp, userAgent));
    }
    private boolean modelMatches(Product product, String modelInput){
        String want = SerialNormalizer.normalize(product.getModelCode());
        String got = SerialNormalizer.normalize(modelInput);
        return want.isEmpty() || want.equals(got);
    }

    @Transactional
    public VerificationView verify(String serialInput, String modelInput, String sourceIp, String userAgent){
        guardEnumeration(sourceIp);
        String norm = SerialNormalizer.normalize(serialInput);
        if(norm.isEmpty()){
            return VerificationView.unknown();
        }
        Optional<MachineUnit>found = units.findBySerialNorm(norm);
        if(found.isEmpty()){
            record(null, serialInput, LookupResult.NOT_FOUND, sourceIp, userAgent);
            // 이 return 이 없으면 아래 found.get() 에서 터진다.
            // 「없는 번호 조회」 는 이 화면에서 가장 흔한 경로다.
            return VerificationView.unknown();
        }
        MachineUnit unit = found.get();
        Product product = unit.getProduct();
        if(!modelMatches(product, modelInput)){
            record(unit.getId(), serialInput, LookupResult.MISMATCH , sourceIp, userAgent);
            return new VerificationView(VerificationView.Outcome.MODEL_MISMATCH, null, null, null, null);
        }
        record(unit.getId(), serialInput, LookupResult.FOUND, sourceIp, userAgent);
        if(unit.getStatus() == MachineUnitStatus.FLAGGED){
            return VerificationView.needsCheck();
        }
        return new VerificationView(
            VerificationView.Outcome.GENUINE,
            unit.getSerial(),
            product.getModelCode(),
            product.getNameKo(),
            unit.getMadeYear()
        );
    }
}
