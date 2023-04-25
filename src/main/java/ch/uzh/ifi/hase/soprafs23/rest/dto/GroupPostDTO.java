package ch.uzh.ifi.hase.soprafs23.rest.dto;

public class GroupPostDTO {

    private String groupName;

    private Long hostId;

    private String votingType;

    public String getGroupName() {
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

    public String getVotingType() {
        return votingType;
    }

    public void setVotingType(String votingType) {
        this.votingType = votingType;
    }

}
