package ch.uzh.ifi.hase.soprafs23.entity;

import ch.uzh.ifi.hase.soprafs23.constant.JoinRequestStatus;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "JOIN_REQUEST")
public class JoinRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private Long guestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JoinRequestStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public Long getGuestId() {
        return guestId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public void setGuestId(Long guestId) {
        this.guestId = guestId;
    }

    public JoinRequestStatus getStatus() {
        return status;
    }

    public void setStatus(JoinRequestStatus status) {
        this.status = status;
    }
}
