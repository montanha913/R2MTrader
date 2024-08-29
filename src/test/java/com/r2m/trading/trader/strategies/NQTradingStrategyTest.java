package com.r2m.trading.trader.strategies;

import com.opencsv.CSVReader;
import com.r2m.trading.trader.data.CsvTicksLoader;
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
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class NQTradingStrategyTest {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static List<Tick> ticks;
    private static TimeSeries series;
    ////////////////// DO NOT CHANGE //////////////////
    private static final String NQ_MINUTE_2Y = "nq-minute.csv"; //
    private static final String NQ_MINUTE_7D = "nq-minute-7d.csv"; // 14-16 / 14-18 // 11-22
    private static final String NQ_MINUTE_15D = "nq-minute-15d.csv"; // 14-16 / 13-17
    private static final String NQ_MINUTE_30D = "nq-minute-30d.csv";
    private static final String NQ_MINUTE_120D = "nq-minute-120d.csv"; // 11-22 (59k)
    private static final String NQ_MINUTE_120D2018 = "nq-minute-120d2018.csv"; // 5-16 (21k)
    private static final String NQ_MINUTE_120D2019 = "nq-minute-120d2019.csv";
    ////////////////// DO NOT CHANGE //////////////////

    @BeforeAll
    public static void initialize() {
//        InputStream stream = CsvTicksLoader.class.getClassLoader().getResourceAsStream(NQ_MINUTE_7D);
//        InputStream stream = CsvTicksLoader.class.getClassLoader().getResourceAsStream(NQ_MINUTE_15D);
//        InputStream stream = CsvTicksLoader.class.getClassLoader().getResourceAsStream(NQ_MINUTE_30D);
        InputStream stream = CsvTicksLoader.class.getClassLoader().getResourceAsStream(NQ_MINUTE_120D);
//        InputStream stream = CsvTicksLoader.class.getClassLoader().getResourceAsStream(NQ_MINUTE_120D2018);
//        InputStream stream = CsvTicksLoader.class.getClassLoader().getResourceAsStream(NQ_MINUTE_120D2019);
//        InputStream stream = CsvTicksLoader.class.getClassLoader().getResourceAsStream(NQ_MINUTE_2Y);

        ticks = new ArrayList<>();

        CSVReader csvReader = new CSVReader(new InputStreamReader(stream, Charset.forName("UTF-8")), ',', '"', 1);

        try {
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                double open = Double.parseDouble(line[0]);
                double close = Double.parseDouble(line[1]);
                double high = Double.parseDouble(line[2]);
                double low = Double.parseDouble(line[3]);
                ZonedDateTime date = LocalDateTime.parse(line[4], DATE_FORMAT).atZone(ZoneId.systemDefault());

                ticks.add(new BaseTick(date, open, high, low, close, 0));
            }
        } catch (IOException ioe) {
            log.error("Unable to load ticks from CSV", ioe);
        } catch (NumberFormatException nfe) {
            log.error("Error while parsing value", nfe);
        }
    }

    @BeforeEach
    public void runBeforeEachTest() {
        series = new BaseTimeSeries();
    }

//    @Test
    public void givenEachTickFixedShouldProcessStrategy() {
        int si = 11;
        int li = 22;
        int macd = 18;

        String bestStrategy = "";
        Decimal bestGain = Decimal.ZERO;

        Strategy strategy = NQTradingStrategy.buildStrategy(series, si, li, macd, 13);

        // Running the strategy
        TimeSeriesManager seriesManager = new TimeSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        for (Tick tick : ticks) {
            series.addTick(tick);
            NQTradingStrategy.processStrategy(series, strategy, tradingRecord);
        }

        Decimal result = NQTradingStrategy.tradingSimpleReport(tradingRecord, si, li, macd, 14);

        if (result.isGreaterThan(bestGain)) {
            bestGain = result;
            bestStrategy = "\nBest Strategy " + si + "/" + li + "/" + macd + ": $";
        }

        System.out.println(bestStrategy + bestGain.toDouble());
    }

//    @Test
    public void givenEachTick3ApartShouldProcessStrategy() {
        int si = 1;
        int li = 3;
        int macd = 18;

        String bestStrategy = "";
        Decimal bestGain = Decimal.ZERO;

        while (li <= 26) {
            Strategy strategy = NQTradingStrategy.buildStrategy(series, si, li, macd, 13);

            // Running the strategy
            TimeSeriesManager seriesManager = new TimeSeriesManager(series);
            TradingRecord tradingRecord = seriesManager.run(strategy);

            for (Tick tick : ticks) {
                series.addTick(tick);
                NQTradingStrategy.processStrategy(series, strategy, tradingRecord);
            }

            Decimal result = NQTradingStrategy.tradingSimpleReport(tradingRecord, si, li, macd, 14);

            if (result.isGreaterThan(bestGain)) {
                bestGain = result;
                bestStrategy = "Best Strategy " + si + "/" + li + "/" + macd + ": $";
            }

            si++;
            li++;
            series = new BaseTimeSeries();
        }

        System.out.println(bestStrategy + bestGain.toDouble());
    }

//    @Test
    public void givenEachTick5ApartShouldProcessStrategy() {
        int si = 11;
        int li = 22;
        int macd = 1;

        String bestStrategy = "";
        Decimal bestGain = Decimal.ZERO;

        while (macd <= 50) {
            Strategy strategy = NQTradingStrategy.buildStrategy(series, si, li, macd, 13);

            // Running the strategy
            TimeSeriesManager seriesManager = new TimeSeriesManager(series);
            TradingRecord tradingRecord = seriesManager.run(strategy);

            for (Tick tick : ticks) {
                series.addTick(tick);
                NQTradingStrategy.processStrategy(series, strategy, tradingRecord);
            }

            Decimal result = NQTradingStrategy.tradingSimpleReport(tradingRecord, si, li, macd, 14);

            if (result.isGreaterThan(bestGain)) {
                bestGain = result;
                bestStrategy = "Best Strategy " + si + "/" + li + "/" + macd + ": $";
            }

//            si++;
//            li++;
            macd++;
            series = new BaseTimeSeries();
        }

        System.out.println(bestStrategy + bestGain.toDouble());
    }

//    @Test
    public void givenEachTickShouldProcessStrategy() {
        int si = 1;
        int li = 2;
        int macd = 18;

        String bestStrategy = "";
        Decimal bestGain = Decimal.ZERO;

        while (si < 15) {
            while (li < 200) {
                Strategy strategy = NQTradingStrategy.buildStrategy(series, si, li, macd, 13);

                // Running the strategy
                TimeSeriesManager seriesManager = new TimeSeriesManager(series);
                TradingRecord tradingRecord = seriesManager.run(strategy);

                for (Tick tick : ticks) {
                    series.addTick(tick);
                    NQTradingStrategy.processStrategy(series, strategy, tradingRecord);
                }

                Decimal result = NQTradingStrategy.tradingSimpleReport(tradingRecord, si, li, macd, 14);

                if (result.isGreaterThan(bestGain)) {
                    bestGain = result;
                    bestStrategy = "Best Strategy " + si + "/" + li  + "/" + macd + ": $";
                }

                li += result.isEqual(Decimal.valueOf(10000)) ? 5 : 2;
                series = new BaseTimeSeries();
            }
            si++;
            li = si+1;
        }

        System.out.println(bestStrategy + bestGain.toDouble());
    }

    @Test
    public void givenEachTickShouldProcessSandboxStrategy() {
        int si = 11;
        int li = 22;
        int macd = 1;
        int k = 1;

        String bestStrategy = "";
        Decimal bestGain = Decimal.ZERO;

        while (macd < 25) {
            while (k < 25) {
                Strategy strategy = buildStrategy(series, si, li, macd, k);

                // Running the strategy
                TimeSeriesManager seriesManager = new TimeSeriesManager(series);
                TradingRecord tradingRecord = seriesManager.run(strategy);

                for (Tick tick : ticks) {
                    series.addTick(tick);
                    NQTradingStrategy.processStrategy(series, strategy, tradingRecord);
                }

                Decimal result = NQTradingStrategy.tradingSimpleReport(tradingRecord, si, li, macd, k);

                if (result.isGreaterThan(bestGain)) {
                    bestGain = result;
                    bestStrategy = "Best Strategy " + si + "/" + li  + "/" + macd + "/" + k + ": $";
                }

//                li += result.isEqual(Decimal.valueOf(10000)) ? 5 : 2;
                series = new BaseTimeSeries();
                k++;
            }
//            si++;
            macd++;
            k = 1;
//            li = si+1;
        }

        System.out.println(bestStrategy + bestGain.toDouble());
    }

    private Strategy buildStrategy(TimeSeries series, int shortIndicator, int longIndicator, int macdIndicator, int k) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

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

    @AfterEach
    public void runAfterEachTest() {
    }

    @AfterAll
    public static void tearDown() {
        ticks.clear();
        ticks = null;
    }

}
