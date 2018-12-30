package com.hsbc.training.pipeline.function;

import com.hsbc.training.pipeline.entity.Trade;
import com.hsbc.training.pipeline.entity.TradeResult;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.util.StringUtils;
import org.apache.beam.sdk.values.KV;

public class ParseTradeResultFn extends DoFn<String, TradeResult> {
  @ProcessElement
  public void processElement(@Element String in, OutputReceiver<TradeResult> out) {
    System.out.println(System.currentTimeMillis() + ":::" + in);
    String[] values = in.split(",");
    if (!"TradeId".equalsIgnoreCase(values[0])) {
      String resultStr = "";
      for (int i = 2; i < values.length; i++) {
        if (i != values.length - 1) {
          resultStr += values[i] + ",";
        } else {
          resultStr += values[i];
        }
      }
      TradeResult traderesult = new TradeResult(values[0], values[1], resultStr);
      out.output(traderesult);
    }
  }
}