package ch.uzh.ifi.hase.soprafs23.entity;

import javax.persistence.*;

import ch.uzh.ifi.hase.soprafs23.constant.GroupState;
import ch.uzh.ifi.hase.soprafs23.constant.VotingType;

import java.io.Serializable;

import java.util.HashSet;
import java.util.List;
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

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private Set<Ingredient> ingredientsSet;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GroupState groupState;

    // constructor
    public Group() {
        this.guestIds = new HashSet<>();
        this.ingredientsSet = new HashSet<>();
        this.groupState = GroupState.GROUPFORMING;
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

    public void addIngredient(List<Ingredient> ingredients) {
        for (Ingredient ingredient : ingredients) {
            if (!ingredientsSet.contains(ingredient)) {
                ingredientsSet.add(ingredient);
            }
        }
    }

    public Set<Ingredient> getIngredients() {
        return ingredientsSet;
    }

    public GroupState getGroupState() {
        return groupState;
    }

    public void setGroupState(GroupState groupState) {
        this.groupState = groupState;
    }

}
