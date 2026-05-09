package com.studentlife.StudentLifeAPIs.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(
        name = "user_devices",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "device_id"})
        },
        indexes = {
                @Index(name = "idx_user_device_user", columnList = "user_id")
        }
)
public class UserDevices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(nullable = false, name = "device_id")
    private String deviceId;

    private String browser;
    private String os;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "device_name")
    private String deviceName;

    private String ipAddress;

    private Instant firstSeenAt;
    private Instant lastSeenAt;

    private boolean active = true;
}
