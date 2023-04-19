package ch.uzh.ifi.hase.soprafs23.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs23.entity.Invitation;

@Repository("invitationRepository")
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    List<Invitation> findByGroupId(Long groupId);

    List<Invitation> findByGuestId(Long guestId);
    
}
