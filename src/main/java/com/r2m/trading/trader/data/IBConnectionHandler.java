package com.r2m.trading.trader.data;

import com.ib.client.EClientSocket;
import eu.verdelhan.ta4j.TimeSeries;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class IBConnectionHandler  {

    private EClientSocket clientSocket;

    private IBDataConnector ibDataConnector;

    public void openConnection() throws InterruptedException {
        log.info("Connecting to TWS...");

        ibDataConnector = new IBDataConnector();

        do {
            ibDataConnector.connect();
            ibDataConnector.connectAck();

            clientSocket = ibDataConnector.client();
            if (clientSocket.isConnected()) {
                log.info("Connected.");
            } else {
                log.info("Failed to connect, trying again...");
                Thread.sleep(5000);
            }
        } while (!clientSocket.isConnected());
    }

    public void requestHistoricalData() {
        //        client.reqAllOpenOrders();
//
//        try {
//            Thread.sleep(1000);
//            reader.processMsgs();
//        } catch (InterruptedException | IOException e) {
//            e.printStackTrace();
//        }
//
//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.MONTH, -6);
//        SimpleDateFormat form = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
//        String formatted = form.format(cal.getTime());
//
//        log.info("Requesting Historical Data...");
//        client.reqHistoricalData(18001, IBDataConnector.NQFutureContract(), formatted,
//                "4 D", "1 min", "TRADES", 0, 1,true, null);
    }

    public TimeSeries requestRealTimeData() {
        log.info("Requesting Market Data...");
        clientSocket.reqRealTimeBars(3001, IBDataConnector.NQFutureContract(), 1, "TRADES", false, null);

        return ibDataConnector.getRealTimeTicks();
    }

    public void checkConnection() throws InterruptedException {
        if (!clientSocket.isConnected()) {
            openConnection();
        }
    }

    public boolean isConnected() {
        return clientSocket.isConnected();
    }

}
