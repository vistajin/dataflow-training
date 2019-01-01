package com.hsbc.training.pipeline.function;

import com.hsbc.training.pipeline.entity.TradeResult;
import org.apache.beam.sdk.transforms.DoFn;

public class ParseTradeResultFn extends DoFn<String, TradeResult> {

    private static ParseTradeResultFn instance = new ParseTradeResultFn();

    private ParseTradeResultFn() {
    }

    public static ParseTradeResultFn getInstance() {
        return instance;
    }

    @ProcessElement
    public void processElement(@Element String in, OutputReceiver<TradeResult> out) {
        // System.out.println(instance.hashCode() + ":::" + in);

        String[] values = in.split(",");
        if (!"TradeId".equalsIgnoreCase(values[0])) {
            StringBuilder resultStr = new StringBuilder();
            for (int i = 2; i < values.length - 1; i++) {
                resultStr.append(values[i]);
                resultStr.append(",");
            }
            resultStr.append(values[values.length - 1]);
            TradeResult traderesult = new TradeResult(values[0], values[1], resultStr.toString());
            // System.out.println(traderesult);
            out.output(traderesult);
        }
    }
}