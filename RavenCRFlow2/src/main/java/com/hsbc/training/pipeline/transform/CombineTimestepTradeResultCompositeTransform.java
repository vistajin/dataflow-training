package com.hsbc.training.pipeline.transform;

import com.hsbc.training.pipeline.entity.CombinedTradeResult;
import com.hsbc.training.pipeline.entity.TradeResult;
import com.hsbc.training.pipeline.function.CombineTradeResultFn;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

public class CombineTimestepTradeResultCompositeTransform
    extends PTransform<PCollection<TradeResult>, PCollection<KV<String, CombinedTradeResult>>> {

    private static final long serialVersionUID = 1L;

    @Override
    public PCollection<KV<String, CombinedTradeResult>> expand(PCollection<TradeResult> input) {
        return input.apply("Key by trade ID", ParDo.of(new DoFn<TradeResult, KV<String, TradeResult>>() {
            private static final long serialVersionUID = 1L;

            @ProcessElement
            public void processElement(@Element TradeResult input, OutputReceiver<KV<String, TradeResult>> out) {
                // System.out.print(input.getTradeId() + "::" + input);
                out.output(KV.of(input.getTradeId(), input));
            }
        })).apply("Collect Trade Results", Combine.perKey(new CombineTradeResultFn()));
    }
}
