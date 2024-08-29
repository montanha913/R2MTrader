package com.r2m.trading.trader.analysis.criterion;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.Trade;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.analysis.criteria.AbstractAnalysisCriterion;

import java.util.Iterator;

public class PointsTotalProfitCriterion extends AbstractAnalysisCriterion {
    @Override
    public double calculate(TimeSeries timeSeries, Trade trade) {
        return this.calculateProfit(timeSeries, trade);
    }

    @Override
    public double calculate(TimeSeries timeSeries, TradingRecord tradingRecord) {
        double value = 1.0D;

        Trade trade;
        for(Iterator var5 = tradingRecord.getTrades().iterator(); var5.hasNext(); value += this.calculateProfit(timeSeries, trade)) {
            trade = (Trade)var5.next();
        }

        return value;
    }

    @Override
    public boolean betterThan(double criterionValue1, double criterionValue2) {
        return criterionValue1 > criterionValue2;
    }

    private double calculateProfit(TimeSeries series, Trade trade) {
        Decimal profit = Decimal.ONE;
        if (trade.isClosed()) {
            Decimal exitClosePrice = series.getTick(trade.getExit().getIndex()).getClosePrice();
            Decimal entryClosePrice = series.getTick(trade.getEntry().getIndex()).getClosePrice();
            if (trade.getEntry().isBuy()) {
                profit = exitClosePrice.minus(entryClosePrice);
            } else {
                profit = entryClosePrice.minus(exitClosePrice);
            }
        }

        return profit.toDouble();
    }
}
