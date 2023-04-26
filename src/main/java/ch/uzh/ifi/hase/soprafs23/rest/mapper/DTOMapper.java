package ch.uzh.ifi.hase.soprafs23.rest.mapper;

import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.entity.Invitation;
import ch.uzh.ifi.hase.soprafs23.rest.dto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import java.util.Set;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
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
  @Mapping(source = "favoriteCuisine", target = "favoriteCuisine")
  @Mapping(source = "specialDiet", target = "specialDiet")
  UserGetDTO convertEntityToUserGetDTO(User user);

    @Mapping(source = "groupName", target = "groupName")
    @Mapping(source = "hostId", target = "hostId")
    @Mapping(source = "votingType", target = "votingType")
    Group convertGroupPostDTOtoEntity(GroupPostDTO groupPostDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "groupName", target = "groupName")
    @Mapping(source = "hostId", target = "hostId")
    @Mapping(source = "votingType", target = "votingType")
    GroupGetDTO convertEntityToGroupGetDTO(Group group);


    @Mapping(source = "invitationId", target = "id")
    Invitation convertInvitationPostDTOtoEntity(InvitationPostDTO invitationPostDTO);

    Invitation convertInvitationPostDTOtoEntity(Long groupId, Long guestId);

    default String map(Set<String> value) {
        return String.join(",", value);
    }

}
