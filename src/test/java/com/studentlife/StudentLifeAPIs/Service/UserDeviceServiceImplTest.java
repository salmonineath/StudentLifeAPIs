package com.studentlife.StudentLifeAPIs.Service;

import com.studentlife.StudentLifeAPIs.DTO.Request.RegisterDeviceRequest;
import com.studentlife.StudentLifeAPIs.DTO.Response.ApiResponse;
import com.studentlife.StudentLifeAPIs.DTO.Response.UserDeviceResponse;
import com.studentlife.StudentLifeAPIs.Entity.UserDevices;
import com.studentlife.StudentLifeAPIs.Entity.Users;
import com.studentlife.StudentLifeAPIs.Mapper.DeviceMapper;
import com.studentlife.StudentLifeAPIs.Repository.UserDeviceRepository;
import com.studentlife.StudentLifeAPIs.Service.Impl.UserDeviceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.studentlife.StudentLifeAPIs.Exception.ApiException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDeviceServiceImplTest {

    @Mock
    private UserDeviceRepository userDeviceRepository;

    @Mock
    private DeviceMapper deviceMapper;

    @InjectMocks
    private UserDeviceServiceImpl userDeviceService;

    private Users user;
    private RegisterDeviceRequest request;
    private UserDevices deviceEntity;
    private UserDeviceResponse deviceResponse;

    @BeforeEach
    void setUp() {
        user = Users.builder()
                .id(1L)
                .fullname("Test User")
                .username("testuser")
                .email("test@example.com")
                .password("encoded-password")
                .build();

        request = RegisterDeviceRequest.builder()
                .deviceId("device-abc-123")
                .deviceType("DESKTOP")
                .deviceName("Chrome")
                .os("Windows")
                .browser("Chrome")
                .build();

        Instant now = Instant.now();
        deviceEntity = UserDevices.builder()
                .id(10L)
                .user(user)
                .deviceId("device-abc-123")
                .deviceType("DESKTOP")
                .deviceName("Chrome")
                .os("Windows")
                .browser("Chrome")
                .active(true)
                .firstSeenAt(now)
                .lastSeenAt(now)
                .build();

        deviceResponse = UserDeviceResponse.builder()
                .id(10L)
                .deviceId("device-abc-123")
                .deviceType("DESKTOP")
                .deviceName("Chrome")
                .os("Windows")
                .browser("Chrome")
                .active(true)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // registerDevice
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("registerDevice()")
    class RegisterDevice {

        @Test
        @DisplayName("creates a new device record when device is seen for the first time")
        void newDevice_savesAndReturnsResponse() {
            when(userDeviceRepository.findByUserIdAndDeviceId(1L, "device-abc-123"))
                    .thenReturn(Optional.empty());
            when(deviceMapper.toEntity(request)).thenReturn(deviceEntity);
            when(userDeviceRepository.save(deviceEntity)).thenReturn(deviceEntity);
            when(deviceMapper.toResponse(deviceEntity)).thenReturn(deviceResponse);

            ApiResponse<UserDeviceResponse> result =
                    userDeviceService.registerDevice(user, request, "127.0.0.1");

            assertThat(result.getSuccess()).isTrue();
            assertThat(result.getStatus()).isEqualTo(200);
            assertThat(result.getData().getDeviceId()).isEqualTo("device-abc-123");
            verify(userDeviceRepository).save(deviceEntity);
        }

        @Test
        @DisplayName("updates lastSeenAt and ipAddress for an existing device")
        void existingDevice_updatesWithoutChangingFirstSeenAt() {
            Instant originalFirstSeen = deviceEntity.getFirstSeenAt();
            when(userDeviceRepository.findByUserIdAndDeviceId(1L, "device-abc-123"))
                    .thenReturn(Optional.of(deviceEntity));
            when(userDeviceRepository.save(deviceEntity)).thenReturn(deviceEntity);
            when(deviceMapper.toResponse(deviceEntity)).thenReturn(deviceResponse);

            userDeviceService.registerDevice(user, request, "10.0.0.1");

            assertThat(deviceEntity.getIpAddress()).isEqualTo("10.0.0.1");
            assertThat(deviceEntity.isActive()).isTrue();
            assertThat(deviceEntity.getFirstSeenAt()).isEqualTo(originalFirstSeen);
            verify(deviceMapper).updateEntity(request, deviceEntity);
        }

        @Test
        @DisplayName("throws 401 when user is null")
        void nullUser_throwsUnauthorized() {
            assertThatThrownBy(() -> userDeviceService.registerDevice(null, request, "127.0.0.1"))
                    .isInstanceOf(ApiException.class);
        }

        @Test
        @DisplayName("throws 400 when deviceId is blank")
        void blankDeviceId_throwsBadRequest() {
            request.setDeviceId("  ");
            assertThatThrownBy(() -> userDeviceService.registerDevice(user, request, "127.0.0.1"))
                    .isInstanceOf(ApiException.class);
        }

        @Test
        @DisplayName("throws 400 when request is null")
        void nullRequest_throwsBadRequest() {
            assertThatThrownBy(() -> userDeviceService.registerDevice(user, null, "127.0.0.1"))
                    .isInstanceOf(ApiException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // removeDevice
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("removeDevice()")
    class RemoveDevice {

        @Test
        @DisplayName("sets active=false for a device owned by the user")
        void ownerRemovesDevice_setsInactive() {
            when(userDeviceRepository.findById(10L)).thenReturn(Optional.of(deviceEntity));

            ApiResponse<?> result = userDeviceService.removeDevice(user, 10L);

            assertThat(result.getSuccess()).isTrue();
            assertThat(deviceEntity.isActive()).isFalse();
            verify(userDeviceRepository).save(deviceEntity);
        }

        @Test
        @DisplayName("throws 404 when device does not exist")
        void unknownDevice_throwsNotFound() {
            when(userDeviceRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userDeviceService.removeDevice(user, 99L))
                    .isInstanceOf(ApiException.class);
        }

        @Test
        @DisplayName("throws 403 when another user tries to remove the device")
        void differentUser_throwsForbidden() {
            Users otherUser = Users.builder().id(2L).username("other").email("other@example.com")
                    .password("pw").fullname("Other").build();

            when(userDeviceRepository.findById(10L)).thenReturn(Optional.of(deviceEntity));

            assertThatThrownBy(() -> userDeviceService.removeDevice(otherUser, 10L))
                    .isInstanceOf(ApiException.class);
        }

        @Test
        @DisplayName("throws 401 when user is null")
        void nullUser_throwsUnauthorized() {
            assertThatThrownBy(() -> userDeviceService.removeDevice(null, 10L))
                    .isInstanceOf(ApiException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // getMyDevices
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getMyDevices()")
    class GetMyDevices {

        @Test
        @DisplayName("returns mapped list of user's devices")
        void returnsDeviceList() {
            when(userDeviceRepository.findAllByUserId(1L)).thenReturn(List.of(deviceEntity));
            when(deviceMapper.toResponseList(List.of(deviceEntity))).thenReturn(List.of(deviceResponse));

            List<UserDeviceResponse> result = userDeviceService.getMyDevices(user);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDeviceId()).isEqualTo("device-abc-123");
        }

        @Test
        @DisplayName("returns empty list when user has no devices")
        void noDevices_returnsEmptyList() {
            when(userDeviceRepository.findAllByUserId(1L)).thenReturn(List.of());
            when(deviceMapper.toResponseList(List.of())).thenReturn(List.of());

            List<UserDeviceResponse> result = userDeviceService.getMyDevices(user);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("throws 401 when user is null")
        void nullUser_throwsUnauthorized() {
            assertThatThrownBy(() -> userDeviceService.getMyDevices(null))
                    .isInstanceOf(ApiException.class);
        }
    }
}
