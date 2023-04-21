package ch.uzh.ifi.hase.soprafs23.rest.dto;

public class InvitationPostDTO {

    private Long groupId;

    private Long invitationId;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(Long invitationId) {
        this.invitationId = invitationId;
    }
}
