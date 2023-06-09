package ch.uzh.ifi.hase.soprafs23.repository;

import ch.uzh.ifi.hase.soprafs23.entity.FullIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface FullIngredientRepository extends JpaRepository<FullIngredient, Long> {
    Optional<FullIngredient> findByName(String name);
    List<FullIngredient> findByNameContainingIgnoreCase(String initialString);
    boolean existsByQuery(String query);
    List<FullIngredient> findByNameIgnoreCase(String name);
}




