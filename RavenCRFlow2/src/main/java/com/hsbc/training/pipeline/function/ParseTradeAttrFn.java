package com.hsbc.training.pipeline.function;

import com.hsbc.training.pipeline.entity.Trade;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;

public class ParseTradeAttrFn extends DoFn<String, KV<String, Trade>> {
    private static ParseTradeAttrFn instance = new ParseTradeAttrFn();

    private ParseTradeAttrFn() {
    }

    public static ParseTradeAttrFn getInstance() {
        return instance;
    }

    @ProcessElement
    public void processElement(@Element String in, OutputReceiver<KV<String, Trade>> out) {
        System.out.println(instance.hashCode() + ":::" + in);
        String[] rs = in.split(",");
        out.output(KV.of(rs[0], new Trade(rs[0], rs[1])));
    }
}