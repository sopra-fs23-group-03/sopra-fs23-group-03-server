package ch.uzh.ifi.hase.soprafs23.repository;
import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ch.uzh.ifi.hase.soprafs23.entity.Group;

import javax.persistence.NamedQuery;
import java.util.List;
import java.util.Optional;


@Repository("recipeRepository")
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByGroupId(Long groupId);

    @Query("SELECT r FROM Recipe r WHERE r.group = :group")
    List<Recipe> findAllByGroup(@Param("group") Group group);

    Recipe findByExternalRecipeIdAndGroupId(Long externalRecipeId, Long groupId);

    Optional<Recipe> findByUserId(Long userId); //optional prevents nullpointer errors
}
