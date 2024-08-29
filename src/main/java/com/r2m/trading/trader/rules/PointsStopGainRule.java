package com.r2m.trading.trader.rules;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Trade;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;
import eu.verdelhan.ta4j.trading.rules.AbstractRule;

public class PointsStopGainRule  extends AbstractRule {
    private ClosePriceIndicator closePrice;
    private Decimal gainPoints;

    public PointsStopGainRule(ClosePriceIndicator closePrice, Decimal gainPoints) {
        this.closePrice = closePrice;
        this.gainPoints = gainPoints;
    }

    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = false;
        if (tradingRecord != null) {
            Trade currentTrade = tradingRecord.getCurrentTrade();
            if (currentTrade.isOpened()) {
                Decimal entryPrice = currentTrade.getEntry().getPrice();
                Decimal currentPrice = this.closePrice.getValue(index);
                Decimal threshold = entryPrice.plus(this.gainPoints);
                if (currentTrade.getEntry().isBuy()) {
                    satisfied = currentPrice.isGreaterThanOrEqual(threshold);
                } else {
                    satisfied = currentPrice.isLessThanOrEqual(threshold);
                }
            }
        }

        this.traceIsSatisfied(index, satisfied);
        return satisfied;
    }
}
