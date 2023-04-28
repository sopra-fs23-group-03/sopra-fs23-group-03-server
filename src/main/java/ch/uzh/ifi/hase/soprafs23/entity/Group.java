package ch.uzh.ifi.hase.soprafs23.entity;

import javax.persistence.*;

import ch.uzh.ifi.hase.soprafs23.constant.VotingType;

import java.io.Serializable;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "XGROUP")
public class Group implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String groupName;

    @Column(nullable = false, unique = true)
    private Long hostId;

    @Column(nullable = false, unique = true)
    private String hostName;

    @Column(nullable = false)
    private VotingType votingType;

    @Column(nullable = true)
    @ElementCollection
    private Set<Long> guestIds;

    // constructor
    public Group() {
        this.guestIds = new HashSet<>();
    }

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

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public VotingType getVotingType() {
        return votingType;
    }

    public void setVotingType(VotingType votingType) {
        this.votingType = votingType;
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
