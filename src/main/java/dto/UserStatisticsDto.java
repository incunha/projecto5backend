package dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class UserStatisticsDto {
    private int totalUsers;
    private int totalConfirmedusers;
    private int totalUnconfirmedUsers;
    private int totalBlockedUsers;
    private Map<LocalDate, Long> confirmedUsersByDate;

    public UserStatisticsDto() {
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalConfirmedusers() {
        return totalConfirmedusers;
    }

    public void setTotalConfirmedusers(int totalConfirmedusers) {
        this.totalConfirmedusers = totalConfirmedusers;
    }

    public int getTotalUnconfirmedUsers() {
        return totalUnconfirmedUsers;
    }

    public void setTotalUnconfirmedUsers(int totalUnconfirmedUsers) {
        this.totalUnconfirmedUsers = totalUnconfirmedUsers;
    }

    public Map<LocalDate

            , Long> getConfirmedUsersByDate() {
        return confirmedUsersByDate;
    }

    public void setConfirmedUsersByDate(Map<LocalDate, Long> confirmedUsersByDate) {
        this.confirmedUsersByDate = confirmedUsersByDate;
    }

    public int getTotalBlockedUsers() {
        return totalBlockedUsers;
    }

    public void setTotalBlockedUsers(int totalBlockedUsers) {
        this.totalBlockedUsers = totalBlockedUsers;
    }
}