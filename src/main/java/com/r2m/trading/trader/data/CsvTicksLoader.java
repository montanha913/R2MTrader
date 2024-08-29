package com.r2m.trading.trader.data;

import com.opencsv.CSVReader;
import eu.verdelhan.ta4j.BaseTick;
import eu.verdelhan.ta4j.BaseTimeSeries;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import lombok.extern.slf4j.Slf4j;

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
public class CsvTicksLoader {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * @return a time series from Apple Inc. ticks.
     */
    public static TimeSeries loadNQSeries() {

//        InputStream stream = CsvTicksLoader.class.getClassLoader().getResourceAsStream("nq-minute.csv");
        InputStream stream = CsvTicksLoader.class.getClassLoader().getResourceAsStream("nq-minute-120d.csv");

        List<Tick> ticks = new ArrayList<>();

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

        return new BaseTimeSeries("NQ_ticks", ticks);
    }
}
