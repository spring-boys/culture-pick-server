package com.ssafy.culturepick.culture.repository;

import com.ssafy.culturepick.culture.domain.Culture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CultureRepository extends JpaRepository<Culture, Long> {

    Optional<Culture> findBySeq(Long seq);
}
