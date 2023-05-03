package ch.uzh.ifi.hase.soprafs23.rest.dto;

public class JoinRequestPutDTO {
    private Long hostId;
    private Long guestId;

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public Long getGuestId() {
        return guestId;
    }

    public void setGuestId(Long guestId) {
        this.guestId = guestId;
    }
}
