package com.r2m.trading.trader.data;

import com.ib.controller.ApiConnection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Logger implements ApiConnection.ILogger {

    @Override
    public void log(String valueOf) {
        log.info(valueOf);
    }
}
