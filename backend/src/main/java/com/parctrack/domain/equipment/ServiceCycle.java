package com.parctrack.domain.equipment;

import java.time.Period;

public enum ServiceCycle {
    MONTHLY(Period.ofMonths(1)),
    QUARTERLY(Period.ofMonths(3)),
    SEMESTERLY(Period.ofMonths(6)),
    ANNUALLY(Period.ofYears(1));

    private final Period period;

    ServiceCycle(Period period) {
        this.period = period;
    }

    public Period getPeriod() {
        return period;
    }
}
