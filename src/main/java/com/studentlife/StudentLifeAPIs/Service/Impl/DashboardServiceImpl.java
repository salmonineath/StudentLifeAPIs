package com.studentlife.StudentLifeAPIs.Service.Impl;

import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.DashboardResponse;
import com.studentlife.StudentLifeAPIs.Entity.AssignmentMember;
import com.studentlife.StudentLifeAPIs.Entity.Assignments;
import com.studentlife.StudentLifeAPIs.Entity.GroupMessage;
import com.studentlife.StudentLifeAPIs.Entity.Schedules;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentMemberStatus;
import com.studentlife.StudentLifeAPIs.Enum.AssignmentStatus;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentMemberRepository;
import com.studentlife.StudentLifeAPIs.Repository.AssignmentRepository;
import com.studentlife.StudentLifeAPIs.Repository.GroupMessageRepository;
import com.studentlife.StudentLifeAPIs.Repository.ScheduleRepository;
import com.studentlife.StudentLifeAPIs.Service.DashboardService;
import com.studentlife.StudentLifeAPIs.Utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final AuthUtil authUtil;
    private final ScheduleRepository scheduleRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentMemberRepository assignmentMemberRepository;
    private final GroupMessageRepository groupMessageRepository;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    @Override
    public ApiResponse<DashboardResponse> getDashboard() {
        Users currentUser = authUtil.getAuthenticatedUser();
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        LocalDateTime now = LocalDateTime.now();

        List<Assignments> allAssignments = assignmentRepository.findAllAccessibleByUserId(currentUser.getId());
        List<Long> assignmentIds = allAssignments.stream().map(Assignments::getId).toList();

        // Pre-load group membership flags to avoid N+1 in deadline section
        Set<Long> groupAssignmentIds = assignmentIds.isEmpty() ? Set.of() :
                assignmentMemberRepository
                        .findByAssignmentIdInAndStatus(assignmentIds, AssignmentMemberStatus.ACCEPTED)
                        .stream()
                        .map(m -> m.getAssignment().getId())
                        .collect(Collectors.toSet());

        DashboardResponse dashboard = DashboardResponse.builder()
                .greeting(buildGreeting(currentUser, now, allAssignments))
                .todaySchedule(buildTodaySchedule(currentUser, today, nowTime))
                .assignmentStatus(buildAssignmentStatus(allAssignments, now))
                .progressOverview(buildProgressOverview(allAssignments))
                .upcomingDeadlines(buildUpcomingDeadlines(allAssignments, groupAssignmentIds, now))
                .groupActivity(buildGroupActivity(currentUser, allAssignments, assignmentIds))
                .build();

        return new ApiResponse<>(
                200,
                true,
                "Dashboard loaded successfully.",
                dashboard
        );
    }

    // ── Greeting ──────────────────────────────────────────────────────────────

    private DashboardResponse.Greeting buildGreeting(Users user, LocalDateTime now, List<Assignments> allAssignments) {
        // Use the first name token for the greeting; fall back to username.
        // (Previous code indexed [1] and crashed for any single-word name.)
        String displayName = user.getUsername();
        if (user.getFullname() != null && !user.getFullname().isBlank()) {
            displayName = user.getFullname().trim().split("\\s+")[0];
        }

        LocalDateTime weekEnd = now.toLocalDate().plusDays(7).atStartOfDay();
        long deadlinesThisWeek = allAssignments.stream()
                .filter(a -> a.getStatus() != AssignmentStatus.COMPLETED)
                .filter(a -> a.getDueDate() != null
                        && !a.getDueDate().isBefore(now)
                        && a.getDueDate().isBefore(weekEnd))
                .count();

        return DashboardResponse.Greeting.builder()
                .firstName(displayName)
                .deadlinesThisWeek((int) deadlinesThisWeek)
                .build();
    }

    // ── Today's Schedule ──────────────────────────────────────────────────────

    private DashboardResponse.TodaySchedule buildTodaySchedule(Users user, LocalDate today, LocalTime nowTime) {
        // 0=Sun, 1=Mon, ..., 6=Sat  (Java DayOfWeek 7=Sun → 7%7=0)
        int dayOfWeek = today.getDayOfWeek().getValue() % 7;

        List<Schedules> oneTimeToday = scheduleRepository.findOneTimeByUserAndDate(user.getId(), today);
        List<Schedules> recurringToday = scheduleRepository.findRecurringByUserAndDayOfWeek(user.getId(), dayOfWeek);

        DashboardResponse.CurrentClass currentClass = findCurrentClass(oneTimeToday, recurringToday, nowTime);
        List<DashboardResponse.UpNextItem> upNext = buildUpNext(oneTimeToday, recurringToday, nowTime);

        return DashboardResponse.TodaySchedule.builder()
                .currentClass(currentClass)
                .upNext(upNext)
                .build();
    }

    private DashboardResponse.CurrentClass findCurrentClass(
            List<Schedules> oneTimeToday, List<Schedules> recurringToday, LocalTime nowTime) {

        // Check ONE_TIME first
        for (Schedules s : oneTimeToday) {
            if (s.getStartTime() == null || s.getEndTime() == null) continue;
            LocalTime start = s.getStartTime().toLocalTime();
            LocalTime end = s.getEndTime().toLocalTime();
            if (!nowTime.isBefore(start) && nowTime.isBefore(end)) {
                long totalMin = ChronoUnit.MINUTES.between(start, end);
                long elapsedMin = ChronoUnit.MINUTES.between(start, nowTime);
                int pct = totalMin > 0 ? (int) Math.min(100, elapsedMin * 100 / totalMin) : 0;
                return DashboardResponse.CurrentClass.builder()
                        .id(s.getId())
                        .title(s.getTitle())
                        .location(s.getLocation())
                        .durationMinutes((int) totalMin)
                        .endsAt(end.format(TIME_FMT))
                        .progressPercent(pct)
                        .build();
            }
        }

        // Then check RECURRING
        for (Schedules s : recurringToday) {
            if (s.getRecurringStartTime() == null || s.getRecurringEndTime() == null) continue;
            LocalTime start = s.getRecurringStartTime();
            LocalTime end = s.getRecurringEndTime();
            if (!nowTime.isBefore(start) && nowTime.isBefore(end)) {
                long totalMin = ChronoUnit.MINUTES.between(start, end);
                long elapsedMin = ChronoUnit.MINUTES.between(start, nowTime);
                int pct = totalMin > 0 ? (int) Math.min(100, elapsedMin * 100 / totalMin) : 0;
                return DashboardResponse.CurrentClass.builder()
                        .id(s.getId())
                        .title(s.getTitle())
                        .location(s.getLocation())
                        .durationMinutes((int) totalMin)
                        .endsAt(end.format(TIME_FMT))
                        .progressPercent(pct)
                        .build();
            }
        }

        return null;
    }

    private List<DashboardResponse.UpNextItem> buildUpNext(
            List<Schedules> oneTimeToday, List<Schedules> recurringToday, LocalTime nowTime) {

        record Slot(Long id, String title, String location, LocalTime startTime) {}

        List<Slot> slots = new ArrayList<>();

        for (Schedules s : oneTimeToday) {
            if (s.getStartTime() == null) continue;
            LocalTime start = s.getStartTime().toLocalTime();
            if (start.isAfter(nowTime)) {
                slots.add(new Slot(s.getId(), s.getTitle(), s.getLocation(), start));
            }
        }

        for (Schedules s : recurringToday) {
            if (s.getRecurringStartTime() == null) continue;
            LocalTime start = s.getRecurringStartTime();
            if (start.isAfter(nowTime)) {
                slots.add(new Slot(s.getId(), s.getTitle(), s.getLocation(), start));
            }
        }

        slots.sort(Comparator.comparing(Slot::startTime));

        return slots.stream()
                .limit(3)
                .map(slot -> DashboardResponse.UpNextItem.builder()
                        .id(slot.id())
                        .title(slot.title())
                        .location(slot.location())
                        .startTime(slot.startTime().format(TIME_FMT))
                        .build())
                .toList();
    }

    // ── Assignment Status ─────────────────────────────────────────────────────

    private DashboardResponse.AssignmentStatus buildAssignmentStatus(List<Assignments> all, LocalDateTime now) {
        int upcoming = (int) all.stream()
                .filter(a -> a.getStatus() == AssignmentStatus.PENDING
                          || a.getStatus() == AssignmentStatus.IN_PROGRESS)
                .count();
        int overdue = (int) all.stream()
                .filter(a -> a.getStatus() == AssignmentStatus.OVERDUE)
                .count();
        int done = (int) all.stream()
                .filter(a -> a.getStatus() == AssignmentStatus.COMPLETED)
                .count();

        DashboardResponse.FeaturedAssignment featured = all.stream()
                .filter(a -> a.getStatus() == AssignmentStatus.PENDING
                          || a.getStatus() == AssignmentStatus.IN_PROGRESS)
                .min(Comparator.comparing(Assignments::getDueDate))
                .map(a -> DashboardResponse.FeaturedAssignment.builder()
                        .id(a.getId())
                        .title(a.getTitle())
                        .subject(a.getSubject())
                        .dueDateLabel(buildDueDateLabel(a.getDueDate(), now))
                        .build())
                .orElse(null);

        return DashboardResponse.AssignmentStatus.builder()
                .upcoming(upcoming)
                .overdue(overdue)
                .done(done)
                .featured(featured)
                .build();
    }

    // ── Progress Overview ─────────────────────────────────────────────────────

    private DashboardResponse.ProgressOverview buildProgressOverview(List<Assignments> all) {
        if (all.isEmpty()) {
            return DashboardResponse.ProgressOverview.builder()
                    .overallPercent(0).onTrack(0).behind(0).done(0)
                    .build();
        }

        int done = (int) all.stream().filter(a -> a.getStatus() == AssignmentStatus.COMPLETED).count();
        int behind = (int) all.stream().filter(a -> a.getStatus() == AssignmentStatus.OVERDUE).count();
        int onTrack = (int) all.stream()
                .filter(a -> a.getStatus() == AssignmentStatus.PENDING
                          || a.getStatus() == AssignmentStatus.IN_PROGRESS)
                .count();

        int overallPercent = (int) all.stream()
                .mapToInt(Assignments::getProgress)
                .average()
                .orElse(0);

        return DashboardResponse.ProgressOverview.builder()
                .overallPercent(overallPercent)
                .onTrack(onTrack)
                .behind(behind)
                .done(done)
                .build();
    }

    // ── Upcoming Deadlines ────────────────────────────────────────────────────

    private List<DashboardResponse.DeadlineItem> buildUpcomingDeadlines(
            List<Assignments> all, Set<Long> groupAssignmentIds, LocalDateTime now) {

        return all.stream()
                .filter(a -> a.getStatus() != AssignmentStatus.COMPLETED)
                .sorted(Comparator
                        .comparingInt((Assignments a) -> a.getStatus() == AssignmentStatus.OVERDUE ? 0 : 1)
                        .thenComparing(Assignments::getDueDate))
                .limit(5)
                .map(a -> {
                    boolean isGroup = groupAssignmentIds.contains(a.getId());
                    return DashboardResponse.DeadlineItem.builder()
                            .id(a.getId())
                            .title(a.getTitle())
                            .subject(a.getSubject())
                            .dueDateLabel(buildDueDateLabel(a.getDueDate(), now))
                            .urgency(determineUrgency(a, isGroup, now))
                            .isGroup(isGroup)
                            .build();
                })
                .toList();
    }

    private String determineUrgency(Assignments a, boolean isGroup, LocalDateTime now) {
        if (a.getStatus() == AssignmentStatus.OVERDUE) return "OVERDUE";
        if (a.getDueDate() != null && ChronoUnit.HOURS.between(now, a.getDueDate()) <= 48) return "DUE_SOON";
        if (isGroup) return "GROUP";
        return "UPCOMING";
    }

    private String buildDueDateLabel(LocalDateTime dueDate, LocalDateTime now) {
        if (dueDate == null) return "No due date";
        LocalDate dueDay = dueDate.toLocalDate();
        LocalDate today = now.toLocalDate();
        long daysAway = ChronoUnit.DAYS.between(today, dueDay);

        if (daysAway < 0) return "Overdue";
        if (daysAway == 0) return "Due today";
        if (daysAway == 1) return "Due tomorrow";
        if (daysAway < 7) {
            String dow = dueDay.getDayOfWeek().toString();
            return "Due " + dow.charAt(0) + dow.substring(1).toLowerCase();
        }
        return "Due " + dueDate.format(DateTimeFormatter.ofPattern("MMM d"));
    }

    // ── Group Activity ────────────────────────────────────────────────────────

    private DashboardResponse.GroupActivity buildGroupActivity(
            Users currentUser, List<Assignments> allAssignments, List<Long> assignmentIds) {

        int activeGroups = assignmentMemberRepository
                .findByUserIdAndStatus(currentUser.getId(), AssignmentMemberStatus.ACCEPTED)
                .size();

        if (assignmentIds.isEmpty()) {
            return DashboardResponse.GroupActivity.builder()
                    .activeGroups(activeGroups)
                    .recentActivity(List.of())
                    .build();
        }

        Map<Long, String> titleById = allAssignments.stream()
                .collect(Collectors.toMap(Assignments::getId, Assignments::getTitle));

        Instant now = Instant.now();
        List<DashboardResponse.ActivityItem> recentActivity = groupMessageRepository
                .findByAssignmentIdInOrderByCreatedAtDesc(assignmentIds, PageRequest.of(0, 10))
                .stream()
                .filter(m -> !m.getSender().getId().equals(currentUser.getId()))
                .limit(5)
                .map(m -> {
                    String name = m.getSender().getFullname() != null
                            ? m.getSender().getFullname()
                            : m.getSender().getUsername();
                    return DashboardResponse.ActivityItem.builder()
                            .actorName(name)
                            .actorInitials(initials(name))
                            .groupName(titleById.getOrDefault(m.getAssignmentId(), "Group"))
                            .groupId(m.getAssignmentId())
                            .timeAgo(timeAgo(m.getCreatedAt(), now))
                            .build();
                })
                .toList();

        return DashboardResponse.GroupActivity.builder()
                .activeGroups(activeGroups)
                .recentActivity(recentActivity)
                .build();
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private String timeAgo(Instant time, Instant now) {
        long minutes = ChronoUnit.MINUTES.between(time, now);
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        long days = hours / 24;
        if (days == 1) return "Yesterday";
        return days + "d ago";
    }
}
