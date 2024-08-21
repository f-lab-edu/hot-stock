package com.bjcareer.stockservice.timeDeal.domain.coupon;

import com.bjcareer.stockservice.timeDeal.domain.event.Event;
import com.github.ksuid.KsuidGenerator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(unique = true, nullable = false)
    private UUID couponNumber;

    @Enumerated(value = EnumType.STRING)
    private CouponStatus status;

    private LocalDate publishedDate;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    private String userPK;


    public Coupon(Event event, String userPK) {
        this.status = CouponStatus.UNUSED;
        this.publishedDate = LocalDate.now();
        this.event = event;
        this.couponNumber = UUID.randomUUID();
        this.userPK = userPK;
    }
}
