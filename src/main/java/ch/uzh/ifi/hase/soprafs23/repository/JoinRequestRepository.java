package ch.uzh.ifi.hase.soprafs23.repository;

import ch.uzh.ifi.hase.soprafs23.entity.JoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("joinRequestRepository")
public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    Optional<JoinRequest> findByGuestIdAndGroupId(Long guestId, Long groupId);

    List<JoinRequest> findAllByGuestId(Long guestId);
    List<JoinRequest> findAllByGroupId(Long groupId);
    void deleteAllByGroupId(Long groupId);
}
