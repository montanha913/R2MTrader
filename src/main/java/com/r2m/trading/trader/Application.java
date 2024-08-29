package com.r2m.trading.trader;

import com.r2m.trading.trader.data.IBConnectionHandler;
import com.r2m.trading.trader.strategies.NQTradingStrategy;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TimeSeriesManager;
import eu.verdelhan.ta4j.TradingRecord;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {

    private static IBConnectionHandler handler;
    private static TimeSeriesManager seriesManager;
    private static TradingRecord tradingRecord;
    private static Strategy strategy;
    private static TimeSeries series;

    public static void main(String[] args) throws InterruptedException {
        log.info("Running NQ Trader...");

        handler = new IBConnectionHandler();
        handler.openConnection();
        initializeIBData();

        while (true) {
            runRealtimeStrategy();
            handler.checkConnection();
            handler.requestRealTimeData();
        }

//        if (handler.isConnected()) {
//            handler.getClientSocket().reqAccountSummary(9001, "", "");
//            Thread.sleep(5000);
//        }

//        log.info("Done.");
//
//        handler.getIbDataConnector().disconnect();
//        log.info("Bye");
//        Thread.sleep(2000);
//        /*** Canceling historical data requests ***/
//        client.cancelHistoricalData(3000);
    }

    private static void initializeIBData() {
        series = handler.requestRealTimeData();

        // Building the trading strategy
        strategy = NQTradingStrategy.buildStrategyRealTime(series, 11, 22, 19, 13);

        log.info("Preparing Strategy...");
        // Running the strategy
        seriesManager = new TimeSeriesManager(series);
        tradingRecord = seriesManager.run(strategy);
    }

    private static void runRealtimeStrategy() {
        log.info("Running Strategy...");
        while (handler.isConnected()) {
            NQTradingStrategy.processStrategy(series, strategy, tradingRecord);
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
