package ru.artq.practice.socks.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.artq.practice.socks.model.Socks;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocksRepository extends JpaRepository<Socks, Long> {

    @Query("""
            SELECT s FROM Socks s
                WHERE (:color IS NULL OR s.color = :color)
                    AND (:comparison IS NULL OR
                        (:comparison = 'moreThen' AND s.cottonPart > :cottonPart) OR
                        (:comparison = 'lessThan' AND s.cottonPart < :cottonPart) OR
                        (:comparison = 'equal' AND s.cottonPart = :cottonPart))
                    AND (:cottonPart IS NULL OR s.cottonPart = :cottonPart)
                    AND (:minCotton IS NULL OR s.cottonPart >= :minCotton)
                    AND (:maxCotton IS NULL OR s.cottonPart <= :maxCotton)
                ORDER BY
                    CASE
                        WHEN :sortBy = 'color' THEN s.color
                        ELSE NULL
                    END ASC
            """)
    List<Socks> findByFilters(String color, String comparison, Long cottonPart, Long minCotton, Long maxCotton, String sortBy);

    Optional<Socks> findByColorAndCottonPart(String color, Long cottonPercentage);
}
