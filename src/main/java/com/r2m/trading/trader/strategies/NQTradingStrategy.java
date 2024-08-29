package com.r2m.trading.trader.strategies;

import com.r2m.trading.trader.rules.PointsStopLossRule;
import eu.verdelhan.ta4j.*;
import eu.verdelhan.ta4j.indicators.EMAIndicator;
import eu.verdelhan.ta4j.indicators.MACDIndicator;
import eu.verdelhan.ta4j.indicators.StochasticOscillatorKIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;
import eu.verdelhan.ta4j.trading.rules.CrossedDownIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.CrossedUpIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.OverIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.UnderIndicatorRule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NQTradingStrategy {

    public static Strategy buildStrategy(TimeSeries series) {
        return buildStrategy(series, 9, 26, 18, 14);
    }

    /**
     * TWS real time ticks return a new tick every 5 seconds, so we multiply the given indicators by 12
     * to consider as minute
     *
     * @param series
     * @param shortIndicator
     * @param longIndicator
     * @return
     */
    public static Strategy buildStrategyRealTime(TimeSeries series, int shortIndicator, int longIndicator, int macdIndicator, int k) {
        int factor = 2;
        return buildStrategy(series, shortIndicator*factor, longIndicator*factor, k*factor, macdIndicator*factor);
    }

    /**
     * @param series a time series
     * @return a moving momentum strategy
     */
    public static Strategy buildStrategy(TimeSeries series, int shortIndicator, int longIndicator, int macdIndicator, int k) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        if (shortIndicator < 0) shortIndicator = 9;
        if (longIndicator < 0) longIndicator = 26;
        if (macdIndicator < 0) macdIndicator = 18;

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        // The bias is bullish when the shorter-moving average moves above the longer moving average.
        // The bias is bearish when the shorter-moving average moves below the longer moving average.
        EMAIndicator shortEma = new EMAIndicator(closePrice, shortIndicator);
        EMAIndicator longEma = new EMAIndicator(closePrice, longIndicator);

        StochasticOscillatorKIndicator stochasticOscillK = new StochasticOscillatorKIndicator(series, k);

        MACDIndicator macd = new MACDIndicator(closePrice, shortIndicator, longIndicator);
        EMAIndicator emaMacd = new EMAIndicator(macd, macdIndicator);

        // Entry rule
        Rule entryRule = new OverIndicatorRule(shortEma, longEma) // Trend
                .and(new CrossedDownIndicatorRule(stochasticOscillK, Decimal.valueOf(20))) // Signal 1
                .and(new OverIndicatorRule(macd, emaMacd)); // Signal 2

        // Exit rule
        Rule exitRule = new UnderIndicatorRule(shortEma, longEma) // Trend
                .and(new CrossedUpIndicatorRule(stochasticOscillK, Decimal.valueOf(80))) // Signal 1
                .and(new UnderIndicatorRule(macd, emaMacd)) // Signal 2
                .xor(new PointsStopLossRule(closePrice, Decimal.valueOf(80))) // Signal 3
//                .xor(new PointsStopGainRule(closePrice, Decimal.valueOf(10))) // Signal 4
                ;

        return new BaseStrategy(entryRule, exitRule);
    }

    public static void processStrategy(TimeSeries series, Strategy strategy, TradingRecord tradingRecord) {

        int endIndex = series.getEndIndex();
        if (endIndex < 0) return;

        Tick newTick = series.getTick(endIndex);
//        log.info(newTick.getSimpleDateName() + " Tick @" + newTick.getClosePrice());

        if (endIndex % 100 == 0) {
            if (!tradingRecord.getCurrentTrade().isOpened()) {
                log.info(newTick.getSimpleDateName() + " Tick @" + newTick.getClosePrice());
            }
//            if (tradingRecord.getCurrentTrade().isOpened()) {
//                log.info("System currently LONG - ENTRY "
//                        + series.getTick(tradingRecord.getCurrentTrade().getEntry().getIndex()).getSimpleDateName()
//                        + " @ " + series.getTick(tradingRecord.getCurrentTrade().getEntry().getIndex()).getClosePrice());
//            } else if (tradingRecord.getLastTrade() != null) {
//                log.info("System currently FLAT since " + series.getTick(tradingRecord.getLastTrade().getExit().getIndex()).getSimpleDateName());
//            }
        }

        if (strategy.shouldEnter(endIndex)) {
            // Our strategy should enter
            boolean entered = tradingRecord.enter(endIndex, newTick.getClosePrice(), Decimal.ONE);
            if (entered) {
                Order entry = tradingRecord.getLastEntry();
                System.out.print(newTick.getSimpleDateName() + " " + entry.getPrice().toDouble());
            }
        } else if (tradingRecord.getCurrentTrade().isOpened() && strategy.shouldExit(endIndex)) {
            // Our strategy should exit
            boolean exited = tradingRecord.exit(endIndex, newTick.getClosePrice(), Decimal.ONE);
            if (exited) {
                Order exit = tradingRecord.getLastExit();
                Double result = tradingRecord.getLastTrade().getExit().getPrice()
                        .minus(tradingRecord.getLastTrade().getEntry().getPrice()).toDouble();
                Double cash = result * 20;
                System.out.println(" " + newTick.getSimpleDateName() + " " + exit.getPrice().toDouble() + " " + result + " " + cash);
            }
        }
    }

    public static void tradingReport(TimeSeries series, TradingRecord record) {
        Decimal cash = Decimal.valueOf(10000);
        Decimal factor = Decimal.valueOf(20);
        Decimal maxLoss = Decimal.ZERO;
        Decimal maxWin = Decimal.ZERO;
        int tradeNum = 1;

//        log.info("#     Points      $ Trade     $ Total");
        for (Trade trade : record.getTrades()) {
            Decimal tradePoints = trade.getExit().getPrice().minus(trade.getEntry().getPrice());
            Decimal tradeCash = tradePoints.multipliedBy(factor);

            if (tradePoints.isLessThan(maxLoss)) maxLoss = tradePoints;
            if (tradePoints.isGreaterThan(maxWin)) maxWin = tradePoints;

            log.info(tradeNum++ + "       " + tradePoints +  "       " + tradeCash + "       "
                    + cash.plus(tradeCash).toString() +  "       " + trade.isOpened());
            cash = cash.plus((trade.getExit().getPrice().minus(trade.getEntry().getPrice()).multipliedBy(factor)));
        }
//
//        log.info("Final Cash: " + cash.toString() + " Max Loss: " + maxLoss + " Max Win: " + maxWin);
//
//        log.info("==========================================");
//        log.info("Analysis:");
//
//        log.info("Number of trades for the strategy: " + tradingRecord.getTradeCount());
//
//        // Analysis
//        Double totalPoints = new PointsTotalProfitCriterion().calculate(series, tradingRecord);
//
//        log.info("Total points profit for the strategy: " + totalPoints);
//        log.info("Total points profit ratio for the strategy: " + new PointsTotalProfitRatioCriterion().calculate(series, tradingRecord));
//        log.info("Total cash profit for the strategy: " + new CashTotalProfitCriterion().calculate(series, tradingRecord));
//        log.info("Total cash compounding profit for the strategy: " + new CashTotalProfitCriterion().calculate(series, tradingRecord,
//                Decimal.valueOf(30000), Decimal.valueOf(50000), Decimal.valueOf(20), true));
//
//        log.info("Finished in " + Duration.between(start, LocalDateTime.now()));
    }

    public static Decimal tradingSimpleReport(TradingRecord record, int si, int li, int macd, int k) {
        Decimal cash = Decimal.valueOf(10000);
        Decimal factor = Decimal.valueOf(20);
        Decimal maxLoss = Decimal.ZERO;
        Decimal maxWin = Decimal.ZERO;

        for (Trade trade : record.getTrades()) {
            Decimal tradePoints = trade.getExit().getPrice().minus(trade.getEntry().getPrice());

            if (tradePoints.isLessThan(maxLoss)) maxLoss = tradePoints;
            if (tradePoints.isGreaterThan(maxWin)) maxWin = tradePoints;

            cash = cash.plus((trade.getExit().getPrice().minus(trade.getEntry().getPrice()).multipliedBy(factor)));
        }

        log.info("Strategy " + si + "/" + li  + "/" + macd + "/" + k
                + " Final Cash: " + cash.toString() + " Max Loss: " + maxLoss + " Max Win: " + maxWin);

        return cash;
    }

}
