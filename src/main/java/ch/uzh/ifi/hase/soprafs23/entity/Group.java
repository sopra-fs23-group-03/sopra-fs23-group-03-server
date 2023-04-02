package ch.uzh.ifi.hase.soprafs23.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

public class Group {

    @Id
    @GeneratedValue
    private Long groupId;

    @Column(nullable = false, unique = true)
    private String groupName;

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
}
