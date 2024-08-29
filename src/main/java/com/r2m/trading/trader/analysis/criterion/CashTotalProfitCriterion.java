package com.r2m.trading.trader.analysis.criterion;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.Trade;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.analysis.criteria.AbstractAnalysisCriterion;

import java.util.Iterator;

public class CashTotalProfitCriterion extends AbstractAnalysisCriterion {

    private Decimal initialAmount = Decimal.valueOf(10000);
    private Decimal currentAmount = Decimal.valueOf(10000);
    private Decimal valuePerContract = Decimal.valueOf(50000);
    private Decimal factor = Decimal.valueOf(20);
    private boolean compounding = false;


    public double calculate(TimeSeries timeSeries, TradingRecord tradingRecord, Decimal initialAmount,
                            Decimal valuePerContract, Decimal factor, boolean compounding) {
        this.initialAmount = initialAmount;
        this.currentAmount = currentAmount;
        this.valuePerContract = valuePerContract;
        this.factor = factor;
        this.compounding = compounding;

        return this.calculate(timeSeries, tradingRecord);
    }


    @Override
    public double calculate(TimeSeries timeSeries, Trade trade) {
        return this.calculateProfit(timeSeries, trade);
    }

    @Override
    public double calculate(TimeSeries timeSeries, TradingRecord tradingRecord) {
        Trade trade;
        for(Iterator var5 = tradingRecord.getTrades().iterator(); var5.hasNext(); currentAmount = currentAmount.plus(Decimal.valueOf(this.calculateProfit(timeSeries, trade)))) {
            trade = (Trade)var5.next();
        }

        return currentAmount.toDouble();
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

        double numContracts = Math.floor(currentAmount.dividedBy(valuePerContract).toDouble());

        if (numContracts < 1 || !compounding) {
            numContracts = 1;
        }

        profit = profit.multipliedBy(Decimal.valueOf(numContracts)).multipliedBy(factor);

        return profit.toDouble();
    }
}
