package com.studentlife.StudentLifeAPIs.DTO.Response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponse {

    private Greeting greeting;
    private TodaySchedule todaySchedule;
    private AssignmentStatus assignmentStatus;
    private ProgressOverview progressOverview;
    private List<DeadlineItem> upcomingDeadlines;
    private GroupActivity groupActivity;

    @Data
    @Builder
    public static class Greeting {
        private String firstName;
        private int deadlinesThisWeek;
    }

    @Data
    @Builder
    public static class TodaySchedule {
        private CurrentClass currentClass;
        private List<UpNextItem> upNext;
    }

    @Data
    @Builder
    public static class CurrentClass {
        private Long id;
        private String title;
        private String location;
        private int durationMinutes;
        private String endsAt;
        private int progressPercent;
    }

    @Data
    @Builder
    public static class UpNextItem {
        private Long id;
        private String title;
        private String location;
        private String startTime;
    }

    @Data
    @Builder
    public static class AssignmentStatus {
        private int upcoming;
        private int overdue;
        private int done;
        private FeaturedAssignment featured;
    }

    @Data
    @Builder
    public static class FeaturedAssignment {
        private Long id;
        private String title;
        private String subject;
        private String dueDateLabel;
    }

    @Data
    @Builder
    public static class ProgressOverview {
        private int overallPercent;
        private int onTrack;
        private int behind;
        private int done;
    }

    @Data
    @Builder
    public static class DeadlineItem {
        private Long id;
        private String title;
        private String subject;
        private String dueDateLabel;
        private String urgency;
        private boolean isGroup;
    }

    @Data
    @Builder
    public static class GroupActivity {
        private int activeGroups;
        private List<ActivityItem> recentActivity;
    }

    @Data
    @Builder
    public static class ActivityItem {
        private String actorName;
        private String actorInitials;
        private String groupName;
        private Long groupId;
        private String timeAgo;
    }
}
