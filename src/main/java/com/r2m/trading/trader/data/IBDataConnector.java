package com.r2m.trading.trader.data;

import com.ib.client.*;
import eu.verdelhan.ta4j.BaseTick;
import eu.verdelhan.ta4j.BaseTimeSeries;
import eu.verdelhan.ta4j.TimeSeries;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class IBDataConnector implements EWrapper {

    private static final int MAX_MESSAGES = 1000000;
    private final static SimpleDateFormat m_df = new SimpleDateFormat("HH:mm:ss");

    // main client
    private EJavaSignal m_signal = new EJavaSignal();
    private EClientSocket m_client = new EClientSocket(this, m_signal);

    // utils
    private long ts;
    private PrintStream m_output;
    private int m_outputCounter = 0;
    private int m_messageCounter;
    private TimeSeries realTimeTicks = new BaseTimeSeries();

    public EClientSocket client() {
        return m_client;
    }

    public EJavaSignal signal() {
        return m_signal;
    }

    public IBDataConnector() {
        initNextOutput();
        attachDisconnectHook(this);
    }

    public void connect() {
        connect(1);
    }

    public void connect(int clientId) {
        String host = System.getProperty("jts.host");
        host = host != null ? host : "localhost";
//        m_client.eConnect(host, 7497, clientId);
//      Gateway Port
        m_client.eConnect(host, 4001, clientId);

        final EReader reader = new EReader(m_client, m_signal);

        reader.start();

        new Thread(() -> {
            while (m_client.isConnected()) {
                m_signal.waitForSignal();
                try {
                    SwingUtilities.invokeAndWait(() -> {
                        try {
                            reader.processMsgs();
                        } catch (IOException e) {
                            error(e);
                        }
                    });
                } catch (Exception e) {
                    error(e);
                }
            }
        }).start();
    }

    public void disconnect() {
        m_client.eDisconnect();
    }

    /* ***************************************************************
     * AnyWrapper
     *****************************************************************/

    public void error(Exception e) {
        e.printStackTrace(m_output);
    }

    public void error(String str) {
        m_output.println(str);
    }

    @Override
    public void error(int i, int i1, String s, String s1) {

    }

    public void error(int id, int errorCode, String errorMsg) {
        logIn("Error id=" + id + " code=" + errorCode + " msg=" + errorMsg);
    }

    public void connectionClosed() {
        m_output.println("--------------------- CLOSED ---------------------");
    }

    public static Contract NQFutureContract() {
        Contract contract = new Contract();
        contract.symbol("NQ");
        contract.secType("FUT");
        contract.currency("USD");
        contract.exchange("GLOBEX");
        contract.lastTradeDateOrContractMonth("202309");
        return contract;
    }
    /* ***************************************************************
     * EWrapper
     *****************************************************************/

    public void tickPrice(int tickerId, int field, double price, TickAttrib attribs) {
        logIn("tickPrice");
    }

    @Override
    public void tickSize(int i, int i1, Decimal decimal) {

    }

    @Override
    public void tickOptionComputation(int i, int i1, int i2, double v, double v1, double v2, double v3, double v4, double v5, double v6, double v7) {

    }

    public void tickSize(int tickerId, int field, int size) {
        logIn("tickSize");
    }

    public void tickGeneric(int tickerId, int tickType, double value) {
        logIn("tickGeneric");
    }

    public void tickString(int tickerId, int tickType, String value) {
        logIn("tickString");
    }

    public void tickSnapshotEnd(int tickerId) {
        logIn("tickSnapshotEnd");
    }

    public void tickOptionComputation(int tickerId, int field, double impliedVol,
                                      double delta, double optPrice, double pvDividend,
                                      double gamma, double vega, double theta, double undPrice) {
        logIn("tickOptionComputation");
    }

    public void tickEFP(int tickerId, int tickType, double basisPoints,
                        String formattedBasisPoints, double impliedFuture, int holdDays,
                        String futureLastTradeDate, double dividendImpact, double dividendsToLastTradeDate) {
        logIn("tickEFP");
    }

    @Override
    public void orderStatus(int i, String s, Decimal decimal, Decimal decimal1, double v, int i1, int i2, double v1, int i3, String s1, double v2) {

    }

    public void orderStatus(int orderId, String status, double filled, double remaining,
                            double avgFillPrice, int permId, int parentId, double lastFillPrice,
                            int clientId, String whyHeld, double mktCapPrice) {
        logIn("orderStatus");
    }

    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
        logIn("openOrder");
    }

    public void openOrderEnd() {
        logIn("openOrderEnd");
    }

    public void updateAccountValue(String key, String value, String currency, String accountName) {
        logIn("updateAccountValue");
    }

    @Override
    public void updatePortfolio(Contract contract, Decimal decimal, double v, double v1, double v2, double v3, double v4, String s) {

    }

    public void updatePortfolio(Contract contract, double position, double marketPrice, double marketValue,
                                double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
        logIn("updatePortfolio");
    }

    public void updateAccountTime(String timeStamp) {
        logIn("updateAccountTime");
    }

    public void accountDownloadEnd(String accountName) {
        logIn("accountDownloadEnd");
    }

    public void nextValidId(int orderId) {
        logIn("nextValidId");
    }

    public void contractDetails(int reqId, ContractDetails contractDetails) {
        logIn("contractDetails");
    }

    public void contractDetailsEnd(int reqId) {
        logIn("contractDetailsEnd");
    }

    public void bondContractDetails(int reqId, ContractDetails contractDetails) {
        logIn("bondContractDetails");
    }

    public void execDetails(int reqId, Contract contract, Execution execution) {
        logIn("execDetails");
    }

    public void execDetailsEnd(int reqId) {
        logIn("execDetailsEnd");
    }

    @Override
    public void updateMktDepth(int i, int i1, int i2, int i3, double v, Decimal decimal) {

    }

    @Override
    public void updateMktDepthL2(int i, int i1, String s, int i2, int i3, double v, Decimal decimal, boolean b) {

    }

    public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
        logIn("updateMktDepth");
    }

    public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation,
                                 int side, double price, int size, boolean isSmartDepth) {
        logIn("updateMktDepthL2");
    }

    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
        logIn("updateNewsBulletin");
    }

    public void managedAccounts(String accountsList) {
        logIn("managedAccounts");
    }

    public void receiveFA(int faDataType, String xml) {
        logIn("receiveFA");
    }

    public void historicalData(int reqId, Bar bar) {
        logIn("HistoricalData. " + reqId + " - Date: " + bar.time() + ", Open: " + bar.open() + ", High: "
                + bar.high() + ", " + "Low: " + bar.low() + ", Close: " + bar.close() + ", Volume: " + bar.volume()
                + ", Count: " + bar.count() + ", WAP: " + bar.wap());
    }

    public void scannerParameters(String xml) {
        logIn("scannerParameters");
    }

    public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance,
                            String benchmark, String projection, String legsStr) {
        logIn("scannerData");
    }

    public void scannerDataEnd(int reqId) {
        logIn("scannerDataEnd");
    }

    @Override
    public void realtimeBar(int i, long l, double v, double v1, double v2, double v3, Decimal decimal, Decimal decimal1, int i1) {

    }

    public void realtimeBar(int reqId, long time, double open, double high, double low, double close,
                            long volume, double wap, int count) {
        ZonedDateTime dateTime = ZonedDateTime.of(LocalDateTime.ofEpochSecond(time, 0,
                ZoneOffset.systemDefault().getRules().getOffset(Instant.now())), ZoneOffset.systemDefault());
        logIn("RTB(" + reqId + ") - " +  dateTime.toString() + "," + open + "," + high + "," + low + ","
                + close + "," + volume);
        realTimeTicks.addTick(new BaseTick(dateTime, open, high, low, close, Double.longBitsToDouble(volume)));
    }

    public void currentTime(long millis) {
        logIn("currentTime");
    }

    public void fundamentalData(int reqId, String data) {
        logIn("fundamentalData");
    }

    public void deltaNeutralValidation(int reqId, DeltaNeutralContract deltaNeutralContract) {
        logIn("deltaNeutralValidation");
    }

    public void marketDataType(int reqId, int marketDataType) {
        logIn("marketDataType");
    }

    public void commissionReport(CommissionReport commissionReport) {
        logIn("commissionReport");
    }

    @Override
    public void position(String s, Contract contract, Decimal decimal, double v) {

    }

    public void position(String account, Contract contract, double pos, double avgCost) {
        log.info(account + contract + pos + avgCost);
    }

    public void positionEnd() {
        logIn("positionEnd");
    }

    public void accountSummary(int reqId, String account, String tag, String value, String currency) {
        logIn("accountSummary");
    }

    public void accountSummaryEnd(int reqId) {
        logIn("accountSummaryEnd");
    }

    public void verifyMessageAPI(String apiData) {
        logIn("verifyMessageAPI");
    }

    public void verifyCompleted(boolean isSuccessful, String errorText) {
        logIn("verifyCompleted");
    }

    public void verifyAndAuthMessageAPI(String apiData, String xyzChallenge) {
        logIn("verifyAndAuthMessageAPI");
    }

    public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {
        logIn("verifyAndAuthCompleted");
    }

    public void displayGroupList(int reqId, String groups) {
        logIn("displayGroupList");
    }

    public void displayGroupUpdated(int reqId, String contractInfo) {
        logIn("displayGroupUpdated");
    }

    public void positionMulti(int reqId, String account, String modelCode, Contract contract, double pos, double avgCost) {
        logIn("positionMulti");
    }

    public void positionMultiEnd(int reqId) {
        logIn("positionMultiEnd");
    }

    public void accountUpdateMulti(int reqId, String account, String modelCode, String key, String value, String currency) {
        logIn("accountUpdateMulti");
    }

    public void accountUpdateMultiEnd(int reqId) {
        logIn("accountUpdateMultiEnd");
    }

    /* ***************************************************************
     * Helpers
     *****************************************************************/
    protected void logIn(String method) {
        m_messageCounter++;
        if (m_messageCounter == MAX_MESSAGES) {
            m_output.close();
            initNextOutput();
            m_messageCounter = 0;
        }
        m_output.println("[W] > " + method);
    }

    protected static void consoleMsg(String str) {
        System.out.println(Thread.currentThread().getName() + " (" + tsStr() + "): " + str);
    }

    protected static String tsStr() {
        synchronized (m_df) {
            return m_df.format(new Date());
        }
    }

    protected static void sleepSec(int sec) {
        sleep(sec * 1000);
    }

    private static void sleep(int msec) {
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected void swStart() {
        ts = System.currentTimeMillis();
    }

    protected void swStop() {
        long dt = System.currentTimeMillis() - ts;
        m_output.println("[API]" + " Time=" + dt);
    }

    public TimeSeries getRealTimeTicks() {
        return this.realTimeTicks;
    }

    private void initNextOutput() {
        try {
//            m_output = System.out;
            m_output = new PrintStream(new File("sysout_" + (++m_outputCounter) + ".log"), "UTF-8");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void attachDisconnectHook(final IBDataConnector ut) {
        Runtime.getRuntime().addShutdownHook(new Thread(ut::disconnect));
    }

    public void connectAck() {
        m_client.startAPI();
    }

    @Override
    public void positionMulti(int i, String s, String s1, Contract contract, Decimal decimal, double v) {

    }

    @Override
    public void securityDefinitionOptionalParameter(int reqId, String exchange, int underlyingConId, String tradingClass,
                                                    String multiplier, Set<String> expirations, Set<Double> strikes) {
        // TODO Auto-generated method stub

    }

    @Override
    public void securityDefinitionOptionalParameterEnd(int reqId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {
        // TODO Auto-generated method stub

    }

    @Override
    public void familyCodes(FamilyCode[] familyCodes) {
        // TODO Auto-generated method stub

    }

    @Override
    public void symbolSamples(int reqId, ContractDescription[] contractDescriptions) {
        // TODO Auto-generated method stub

    }

    @Override
    public void historicalDataEnd(int reqId, String startDateStr, String endDateStr) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {
        // TODO Auto-generated method stub

    }

    @Override
    public void tickNews(int tickerId, long timeStamp, String providerCode, String articleId, String headline,
                         String extraData) {
        // TODO Auto-generated method stub

    }

    @Override
    public void smartComponents(int reqId, Map<Integer, Map.Entry<String, Character>> theMap) {
        // TODO Auto-generated method stub

    }

    @Override
    public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {
        // TODO Auto-generated method stub

    }

    @Override
    public void newsProviders(NewsProvider[] newsProviders) {
        // TODO Auto-generated method stub

    }

    @Override
    public void newsArticle(int requestId, int articleType, String articleText) {
        // TODO Auto-generated method stub

    }

    @Override
    public void historicalNews(int requestId, String time, String providerCode, String articleId, String headline) {
        // TODO Auto-generated method stub

    }

    @Override
    public void historicalNewsEnd(int requestId, boolean hasMore) {
        // TODO Auto-generated method stub

    }

    @Override
    public void headTimestamp(int reqId, String headTimestamp) {
        // TODO Auto-generated method stub

    }

    @Override
    public void histogramData(int reqId, List<HistogramEntry> items) {
        // TODO Auto-generated method stub

    }

    @Override
    public void historicalDataUpdate(int reqId, Bar bar) {
        logIn("HistoricalData. " + reqId + " - Date: " + bar.time() + ", Open: " + bar.open() + ", High: " + bar.high() + ", " +
                "Low: " + bar.low() + ", Close: " + bar.close() + ", Volume: " + bar.volume() + ", Count: " + bar.count() + ", WAP: " + bar.wap());
    }

    @Override
    public void pnl(int reqId, double dailyPnL, double unrealizedPnL, double realizedPnL) {
        // TODO Auto-generated method stub

    }

    @Override
    public void pnlSingle(int i, Decimal decimal, double v, double v1, double v2, double v3) {

    }

    @Override
    public void rerouteMktDataReq(int reqId, int conId, String exchange) {
        // TODO Auto-generated method stub

    }

    @Override
    public void rerouteMktDepthReq(int reqId, int conId, String exchange) {
        // TODO Auto-generated method stub

    }

    @Override
    public void marketRule(int marketRuleId, PriceIncrement[] priceIncrements) {
        // TODO Auto-generated method stub

    }

    public void pnlSingle(int reqId, int pos, double dailyPnL, double unrealizedPnL, double realizedPnL, double value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void historicalTicks(int reqId, List<HistoricalTick> ticks, boolean last) {
        logIn("HistoricalTicks. " + reqId);
        for (HistoricalTick tick : ticks) {
            logIn(tick.toString());
        }
    }

    @Override
    public void historicalTicksBidAsk(int reqId, List<HistoricalTickBidAsk> ticks, boolean done) {
        logIn("historicalTicksBidAsk. " + reqId);
        for (HistoricalTickBidAsk tick : ticks) {
            logIn(tick.toString());
        }
    }


    @Override
    public void historicalTicksLast(int reqId, List<HistoricalTickLast> ticks, boolean done) {
        logIn("historicalTicksLast. " + reqId);
        for (HistoricalTickLast tick : ticks) {
            logIn(tick.toString());
        }
    }

    @Override
    public void tickByTickAllLast(int i, int i1, long l, double v, Decimal decimal, TickAttribLast tickAttribLast, String s, String s1) {

    }

    @Override
    public void tickByTickBidAsk(int i, long l, double v, double v1, Decimal decimal, Decimal decimal1, TickAttribBidAsk tickAttribBidAsk) {

    }

    public void tickByTickAllLast(int reqId, int tickType, long time, double price, int size, TickAttribLast tickAttribLast,
                                  String exchange, String specialConditions) {
        logIn("tickByTickAllLast. " + reqId);

    }

    public void tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, int bidSize, int askSize,
                                 TickAttribBidAsk tickAttribBidAsk) {
        // TODO Auto-generated method stub
    }

    @Override
    public void tickByTickMidPoint(int reqId, long time, double midPoint) {
        // TODO Auto-generated method stub
    }

    @Override
    public void orderBound(long orderId, int apiClientId, int apiOrderId) {
        // TODO Auto-generated method stub
    }

    @Override
    public void completedOrder(Contract contract, Order order, OrderState orderState) {
        // TODO Auto-generated method stub
    }

    @Override
    public void completedOrdersEnd() {
        // TODO Auto-generated method stub
    }

    @Override
    public void replaceFAEnd(int i, String s) {

    }

    @Override
    public void wshMetaData(int i, String s) {

    }

    @Override
    public void wshEventData(int i, String s) {

    }

    @Override
    public void historicalSchedule(int i, String s, String s1, String s2, List<HistoricalSession> list) {

    }

    @Override
    public void userInfo(int i, String s) {

    }
}
