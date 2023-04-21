package ch.uzh.ifi.hase.soprafs23.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "GROUP")
public class Group implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String groupName;

    @Column(nullable = false, unique = true)
    private Long hostId;

    @ElementCollection
    private Set<Long> guestIds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupName(){
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public Set<Long> getGuestIds() {
        return guestIds;
    }

    public void addGuestId(Long guestId) {
        this.guestIds.add(guestId);
    }

    public void removeGuestId(Long guestId) {
        this.guestIds.remove(guestId);
    }

}
