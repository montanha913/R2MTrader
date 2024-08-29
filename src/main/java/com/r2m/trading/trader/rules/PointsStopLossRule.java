package com.r2m.trading.trader.rules;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Trade;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;
import eu.verdelhan.ta4j.trading.rules.AbstractRule;

public class PointsStopLossRule extends AbstractRule {
    private ClosePriceIndicator closePrice;
    private Decimal lossPoints;

    public PointsStopLossRule(ClosePriceIndicator closePrice, Decimal lossPoints) {
        this.closePrice = closePrice;
        this.lossPoints = lossPoints;
    }

    public boolean isSatisfied(int index, TradingRecord tradingRecord) {
        boolean satisfied = false;
        if (tradingRecord != null) {
            Trade currentTrade = tradingRecord.getCurrentTrade();
            if (currentTrade.isOpened()) {
                Decimal entryPrice = currentTrade.getEntry().getPrice();
                Decimal currentPrice = this.closePrice.getValue(index);
                Decimal threshold = entryPrice.minus(this.lossPoints);
                if (currentTrade.getEntry().isBuy()) {
                    satisfied = currentPrice.isLessThanOrEqual(threshold);
                } else {
                    satisfied = currentPrice.isGreaterThanOrEqual(threshold);
                }
            }
        }

        this.traceIsSatisfied(index, satisfied);
        return satisfied;
    }
}
