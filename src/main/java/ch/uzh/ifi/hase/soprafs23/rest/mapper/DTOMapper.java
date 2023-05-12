package ch.uzh.ifi.hase.soprafs23.rest.mapper;

import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.IngredientInfo;
import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.RecipeInfo;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.entity.Invitation;
import ch.uzh.ifi.hase.soprafs23.rest.dto.IngredientInfoDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.RecipeInfoDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import java.util.Set;

@Mapper
public interface DTOMapper {

    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "password", target = "password")
    User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "allergiesSet", target = "allergies", qualifiedByName = "map")
    @Mapping(source = "favoriteCuisineSet", target = "favoriteCuisine", qualifiedByName = "map")
    @Mapping(source = "specialDiet", target = "specialDiet")
    @Mapping(source = "groupId", target = "groupId")
    UserGetDTO convertEntityToUserGetDTO(User user);

    @Mapping(source = "groupName", target = "groupName")
    @Mapping(source = "hostId", target = "hostId")
    @Mapping(source = "votingType", target = "votingType")
    Group convertGroupPostDTOtoEntity(GroupPostDTO groupPostDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "groupName", target = "groupName")
    @Mapping(source = "hostId", target = "hostId")
    @Mapping(source = "hostName", target = "hostName")
    @Mapping(source = "votingType", target = "votingType")
    GroupGetDTO convertEntityToGroupGetDTO(Group group);


    @Mapping(source = "invitationId", target = "id")
    Invitation convertInvitationPostDTOtoEntity(InvitationPostDTO invitationPostDTO);

    Invitation convertInvitationPostDTOtoEntity(Long groupId, Long guestId);

    default String map(Set<String> value) {
        return String.join(",", value);
    }

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    IngredientGetDTO convertEntityToIngredientGetDTO(Ingredient ingredient);


    RecipeInfoDTO convertEntityToRecipeInfoDTO(RecipeInfo recipeInfo);
    IngredientInfoDTO convertEntityToIngredientInfoDTO(IngredientInfo ingredientInfo);


}
