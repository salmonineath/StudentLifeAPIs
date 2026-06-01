package com.studentlife.StudentLifeAPIs.Controller;

import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.DashboardResponse;
import com.studentlife.StudentLifeAPIs.Service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Single aggregated endpoint that powers the student dashboard UI")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(
        summary = "Get dashboard",
        description = """
            Returns all data needed to render the student dashboard in a single request:

            - **greeting** — student's first name + how many deadlines are due this week
            - **todaySchedule** — the class currently in progress (with % elapsed) + next classes today
            - **assignmentStatus** — counts for upcoming / overdue / done + the soonest featured assignment
            - **progressOverview** — average completion % across all assignments + on-track / behind / done counts
            - **upcomingDeadlines** — up to 5 non-completed assignments, overdue ones first, each tagged with an urgency badge (OVERDUE, DUE_SOON, GROUP, UPCOMING)
            - **groupActivity** — recent group chat messages from other members across all the student's groups

            Requires a valid JWT Bearer token. Accessible by any authenticated user.
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Dashboard data loaded successfully",
            content = @Content(schema = @Schema(implementation = DashboardResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Missing or invalid JWT token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied"
        )
    })
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }
}
