package com.hsbc.training.pipeline.function;

import com.hsbc.training.pipeline.entity.LegalDoc;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;

public class SumTradeResultFn extends DoFn<String, KV<String, LegalDoc>> {
    @ProcessElement
    public void processElement(@Element String in, OutputReceiver<KV<String, LegalDoc>> out) {


    }
}