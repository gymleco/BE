package kr.co.gymleco.service.machine;

import kr.co.gymleco.domain.machine.*;
import kr.co.gymleco.domain.product.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MachineUnitAdminService {
    private final MachineUnitRepository units;
    private final MachineOwnerRepository owners;
    private final MachineServiceRepository services;
    private final MachineUnitLookupRepository lookups;

    public MachineUnitAdminService(MachineUnitRepository units, MachineOwnerRepository owners, MachineServiceRepository services, MachineUnitLookupRepository lookups) {
        this.units = units;
        this.owners = owners;
        this.services = services;
        this.lookups = lookups;
    }

    public Optional<MachineUnitDetail> findBySerial(String serialInput) {
        String norm = SerialNormalizer.normalize(serialInput);
        if (norm.isEmpty()) {
            return Optional.empty();
        }
        return units.findBySerialNorm(norm).map(this::toDetail);
    }

    private MachineUnitDetail toDetail(MachineUnit u) {
        Product p = u.getProduct();
        MachineOwner owner = u.getOwnerId() == null
            ? null
            : owners.findById(u.getOwnerId()).orElse(null);
        List<MachineUnitDetail.ServiceLine> history = services.findByMachineUnitIdOrderByHappenedOnDescIdDesc(u.getId())
            .stream()
            .map(s -> new MachineUnitDetail.ServiceLine(s.getId(), s.getKind().name(), s.getHappenedOn(), s.getSummary(), s.getCostKrw())).toList();
        int lookupCount = lookups.findTop50ByMachineUnitIdOrderByCreatedAtDesc(u.getId()).size();
        return new MachineUnitDetail(u.getId(), u.getSerial(), p.getModelCode(), p.getNameKo(), u.getMadeYear(), u.getStatus().name(),
            owner == null ? null : owner.getId(),
            owner == null ? null : owner.getName(),
            owner == null ? null : owner.getRegion(),
            owner == null ? null : maskedPhoneOf(owner),
            u.getSoldAt(), u.getInstalledAt(), u.getWarrantyUntil(), u.getNote(), history, lookupCount);
    }

    private String maskedPhoneOf(MachineOwner owner) {
        return owner.getPhoneEncrypted().isBlank() ? null : "***-****-****";
    }
}
