package com.r2m.trading.trader.model;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.ZonedDateTime;

@AllArgsConstructor
public class FutureTick implements Tick {
    Decimal open;
    Decimal high;
    Decimal low;
    Decimal close;
    Decimal volume;

    @Override
    public Decimal getOpenPrice() {
        return open;
    }

    @Override
    public Decimal getMinPrice() {
        return low;
    }

    @Override
    public Decimal getMaxPrice() {
        return high;
    }

    @Override
    public Decimal getClosePrice() {
        return close;
    }

    @Override
    public Decimal getVolume() {
        return volume;
    }

    @Override
    public int getTrades() {
        return 0;
    }

    @Override
    public Decimal getAmount() {
        return null;
    }

    @Override
    public Duration getTimePeriod() {
        return null;
    }

    @Override
    public ZonedDateTime getBeginTime() {
        return null;
    }

    @Override
    public ZonedDateTime getEndTime() {
        return null;
    }

    @Override
    public void addTrade(Decimal decimal, Decimal decimal1) {

    }
}
