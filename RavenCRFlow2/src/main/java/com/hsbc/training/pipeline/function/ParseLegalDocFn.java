package com.hsbc.training.pipeline.function;

import com.hsbc.training.pipeline.entity.LegalDoc;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;

public class ParseLegalDocFn extends DoFn<String, KV<String, LegalDoc>> {
  @ProcessElement
  public void processElement(@Element String in, OutputReceiver<KV<String, LegalDoc>> out) {
    String[] rs = in.split(",");
    out.output(KV.of(rs[0], new LegalDoc(rs[0], rs[1], rs[2], rs[3], rs[4])));
  }
}