package com.studentlife.StudentLifeAPIs.DTO.Response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Full dashboard payload — all sections needed to render the student dashboard UI in one call")
public class DashboardResponse {

    @Schema(description = "Personalised greeting section shown at the top of the dashboard")
    private Greeting greeting;

    @Schema(description = "Today's class schedule: the class currently in progress and the next upcoming classes")
    private TodaySchedule todaySchedule;

    @Schema(description = "Assignment counts by status plus the next upcoming assignment")
    private AssignmentStatus assignmentStatus;

    @Schema(description = "Overall completion percentage and per-category counts for the progress ring")
    private ProgressOverview progressOverview;

    @Schema(description = "Up to 5 upcoming/overdue deadlines sorted by urgency then due date")
    private List<DeadlineItem> upcomingDeadlines;

    @Schema(description = "Collaborative group activity feed and active group count")
    private GroupActivity groupActivity;

    // ── Greeting ──────────────────────────────────────────────────────────────

    @Data
    @Builder
    @Schema(description = "Greeting section")
    public static class Greeting {

        @Schema(description = "User's first name", example = "Sal")
        private String firstName;

        @Schema(description = "Number of non-completed assignments due within the next 7 days", example = "2")
        private int deadlinesThisWeek;
    }

    // ── Today's Schedule ──────────────────────────────────────────────────────

    @Data
    @Builder
    @Schema(description = "Today's schedule section")
    public static class TodaySchedule {

        @Schema(description = "The class currently in progress based on server time. Null if no class is happening right now.")
        private CurrentClass currentClass;

        @Schema(description = "Next classes today after the current time, sorted by start time, max 3")
        private List<UpNextItem> upNext;
    }

    @Data
    @Builder
    @Schema(description = "Class currently in progress")
    public static class CurrentClass {

        @Schema(description = "Schedule ID", example = "12")
        private Long id;

        @Schema(description = "Class title", example = "English Literature")
        private String title;

        @Schema(description = "Room or location", example = "Room 204")
        private String location;

        @Schema(description = "Total class duration in minutes", example = "60")
        private int durationMinutes;

        @Schema(description = "Formatted end time (12-hour clock)", example = "11:00 AM")
        private String endsAt;

        @Schema(description = "How far through the class the student is, 0–100", example = "67")
        private int progressPercent;
    }

    @Data
    @Builder
    @Schema(description = "An upcoming class later today")
    public static class UpNextItem {

        @Schema(description = "Schedule ID", example = "15")
        private Long id;

        @Schema(description = "Class title", example = "Computer Science")
        private String title;

        @Schema(description = "Room or location", example = "Lab 3")
        private String location;

        @Schema(description = "Formatted start time (12-hour clock)", example = "1:30 PM")
        private String startTime;
    }

    // ── Assignment Status ─────────────────────────────────────────────────────

    @Data
    @Builder
    @Schema(description = "Assignment status counts for the status card")
    public static class AssignmentStatus {

        @Schema(description = "Assignments with status PENDING or IN_PROGRESS", example = "4")
        private int upcoming;

        @Schema(description = "Assignments with status OVERDUE", example = "1")
        private int overdue;

        @Schema(description = "Assignments with status COMPLETED", example = "3")
        private int done;

        @Schema(description = "The soonest upcoming assignment to highlight in the card. Null if none.")
        private FeaturedAssignment featured;
    }

    @Data
    @Builder
    @Schema(description = "Highlighted upcoming assignment shown inside the status card")
    public static class FeaturedAssignment {

        @Schema(description = "Assignment ID", example = "7")
        private Long id;

        @Schema(description = "Assignment title", example = "Contribution Accounting")
        private String title;

        @Schema(description = "Subject name", example = "Mathematics")
        private String subject;

        @Schema(description = "Human-readable due date label", example = "Due tomorrow")
        private String dueDateLabel;
    }

    // ── Progress Overview ─────────────────────────────────────────────────────

    @Data
    @Builder
    @Schema(description = "Progress ring data")
    public static class ProgressOverview {

        @Schema(description = "Average progress percentage across all accessible assignments (0–100)", example = "75")
        private int overallPercent;

        @Schema(description = "Count of PENDING + IN_PROGRESS assignments", example = "6")
        private int onTrack;

        @Schema(description = "Count of OVERDUE assignments", example = "1")
        private int behind;

        @Schema(description = "Count of COMPLETED assignments", example = "3")
        private int done;
    }

    // ── Upcoming Deadlines ────────────────────────────────────────────────────

    @Data
    @Builder
    @Schema(description = "A single deadline entry in the upcoming deadlines list")
    public static class DeadlineItem {

        @Schema(description = "Assignment ID", example = "3")
        private Long id;

        @Schema(description = "Assignment title", example = "Math Assignment")
        private String title;

        @Schema(description = "Subject name", example = "Mathematics")
        private String subject;

        @Schema(description = "Human-readable due date label", example = "Due tomorrow")
        private String dueDateLabel;

        @Schema(
            description = "Urgency badge to display. One of: OVERDUE, DUE_SOON (within 48 h), GROUP (collaborative), UPCOMING",
            example = "OVERDUE",
            allowableValues = {"OVERDUE", "DUE_SOON", "GROUP", "UPCOMING"}
        )
        private String urgency;

        @Schema(description = "True if this assignment has other accepted group members", example = "false")
        private boolean isGroup;
    }

    // ── Group Activity ────────────────────────────────────────────────────────

    @Data
    @Builder
    @Schema(description = "Collaboration / group activity section")
    public static class GroupActivity {

        @Schema(description = "Number of study groups the current user has actively joined", example = "2")
        private int activeGroups;

        @Schema(description = "Up to 5 most recent group chat messages from other members")
        private List<ActivityItem> recentActivity;
    }

    @Data
    @Builder
    @Schema(description = "A single activity item in the group feed")
    public static class ActivityItem {

        @Schema(description = "Full name of the person who sent the message", example = "Emma B.")
        private String actorName;

        @Schema(description = "2-letter initials for avatar display", example = "EB")
        private String actorInitials;

        @Schema(description = "Title of the assignment / group the message belongs to", example = "Physics Project")
        private String groupName;

        @Schema(description = "Assignment ID (use to deep-link into the group chat)", example = "5")
        private Long groupId;

        @Schema(description = "Human-readable time since the message was sent", example = "2h ago")
        private String timeAgo;
    }
}
