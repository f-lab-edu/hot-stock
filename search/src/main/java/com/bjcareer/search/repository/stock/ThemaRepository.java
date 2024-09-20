package com.bjcareer.search.repository.stock;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bjcareer.search.domain.entity.Thema;

public interface ThemaRepository extends JpaRepository<Thema, Long> {
	@Query("SELECT t FROM Thema t join fetch t.stock s join fetch  t.themaInfo ti WHERE t.stock.name LIKE %:name%")
	List<Thema> findByStockNameContaining(String name);

	@Query("SELECT t FROM Thema t WHERE t.stock.name = :stockName AND t.themaInfo.name = :themaName")
	Optional<Thema> findByStockNameAndThemaName(String stockName, String themaName);
}
