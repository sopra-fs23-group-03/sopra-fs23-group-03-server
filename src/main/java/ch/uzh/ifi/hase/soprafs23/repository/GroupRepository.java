package ch.uzh.ifi.hase.soprafs23.repository;

import ch.uzh.ifi.hase.soprafs23.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("groupRepository")
public interface GroupRepository extends JpaRepository<Group, Long> {
    Group findByGroupName(String groupName);

    Optional<Group> findById(Long id);

}

