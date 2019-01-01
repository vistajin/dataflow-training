package com.hsbc.training.pipeline.function;

import com.hsbc.training.pipeline.entity.CombinedTradeResult;
import com.hsbc.training.pipeline.entity.TradeResult;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.transforms.Combine.CombineFn;

import java.io.Serializable;
import java.util.*;

public class CombineTradeResultFn
        extends CombineFn<TradeResult, CombineTradeResultFn.Accum, CombinedTradeResult> {
    private static final long serialVersionUID = -6212468039609408782L;

    @Override
    public Accum addInput(Accum accumulator, TradeResult input) {
        accumulator.getTradeList().add(input);
        return accumulator;
    }

    @Override
    public Accum createAccumulator() {
        return new Accum();
    }

    @Override
    public CombinedTradeResult extractOutput(Accum accumulator) {
        Map<String, double[]> results = new HashMap<>();
        accumulator.getTradeList().forEach(trade -> {
            String timepoint = trade.getTimeStep();
            String[] values = trade.getResult().split(",");
            double[] doubleArray = Arrays.stream(values).mapToDouble(str -> Double.parseDouble(str)).toArray();
            results.put(timepoint, doubleArray);
        });
        return new CombinedTradeResult(accumulator.getTradeList().get(0).getTradeId(), results);
    }

    @Override
    public Accum mergeAccumulators(Iterable<Accum> accumulators) {
        Accum merged = createAccumulator();
        accumulators.forEach(accum -> merged.getTradeList().addAll(accum.getTradeList()));
        return merged;
    }

    @DefaultCoder(AvroCoder.class)
    class Accum implements Serializable {

        private static final long serialVersionUID = 1L;

        List<TradeResult> tradeList = new ArrayList<>();

        public List<TradeResult> getTradeList() {
            return tradeList;
        }

        public void setTradeList(List<TradeResult> tradeList) {
            this.tradeList = tradeList;
        }

    }

}
