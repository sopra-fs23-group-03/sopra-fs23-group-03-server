package ch.uzh.ifi.hase.soprafs23.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "GROUP")
public class Group implements Serializable {

    @Id
    @GeneratedValue
    private Long groupId;

    @Column(nullable = false, unique = true)
    private String groupName;

    @Column(nullable = false, unique = true)
    private Long hostId;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
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
}
