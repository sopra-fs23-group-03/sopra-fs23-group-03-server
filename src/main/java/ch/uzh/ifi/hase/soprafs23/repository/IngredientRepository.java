package ch.uzh.ifi.hase.soprafs23.repository;

import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    Optional<Ingredient> findByName(String name);

    Optional<Ingredient> findById(Long id);

    List<Ingredient> findByGroupId(Long id);

    Optional<Ingredient> findByNameAndGroupId(String name, Long groupId);

}
