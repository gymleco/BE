package kr.co.gymleco.domain.support;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {
    List<Faq> findByVisibleTrueOrderByCategoryAscSortOrderAscIdAsc();
    List<Faq> findAllByOrderByCategoryAscSortOrderAscIdAsc();
}
