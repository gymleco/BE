package kr.co.gymleco.service.machine;

import java.time.LocalDate;
import java.util.List;

public record MachineUnitDetail(
    Long id,
    String serial,
    String modelCode,
    String productNameKo,
    Short madeYear,
    String status,
    Long ownerId,
    String ownerName,
    String ownerRegion,
    String phoneMasked,
    LocalDate soldAt,
    LocalDate installedAt,
    LocalDate warrantyUntil,
    String note,
    List<ServiceLine> history,
    int lookupCount
) {
    public record ServiceLine(Long id, String kind, LocalDate happenedOn, String summary, Integer costKrw){}
}
