package com.hsbc.training.pipeline.transform;

import com.hsbc.training.pipeline.entity.CombinedTradeResult;
import com.hsbc.training.pipeline.entity.CompositeID;
import com.hsbc.training.pipeline.entity.LegalDoc;
import com.hsbc.training.pipeline.entity.Trade;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.*;

import java.util.Map;

public class SplitTradeByLegalDocCompositeTransform
    extends PTransform<PCollection<KV<String, CombinedTradeResult>>, PCollectionTuple> {

    private PCollectionView<Map<String, Trade>> tradeMap;

    private PCollectionView<Map<String, LegalDoc>> legalDocMap;

    private TupleTag<KV<CompositeID, Map<String, double[]>>>[] tuples;

    @SafeVarargs
    public SplitTradeByLegalDocCompositeTransform(PCollectionView<Map<String, Trade>> tradeMap,
                                                  PCollectionView<Map<String, LegalDoc>> legalDocMap,
                                                  TupleTag<KV<CompositeID, Map<String, double[]>>>... tuples) {
        super();
        this.tradeMap = tradeMap;
        this.legalDocMap = legalDocMap;
        this.tuples = tuples;
    }

    @Override
    public PCollectionTuple expand(PCollection<KV<String, CombinedTradeResult>> input) {
        return input
            .apply(ParDo.of(new DoFn<KV<String, CombinedTradeResult>, KV<CompositeID, Map<String, double[]>>>() {

                @ProcessElement
                public void processElement(@Element KV<String, CombinedTradeResult> input, MultiOutputReceiver out,
                                           ProcessContext c) {
                    String tradeId = input.getKey();

                    Map<String, Trade> tradeToLegalDocMap = c.sideInput(tradeMap);
                    String legalDocId = tradeToLegalDocMap.get(tradeId).getLegalDoc();

                    Map<String, LegalDoc> legalDoctoCptyMap = c.sideInput(legalDocMap);
                    String nettable = legalDoctoCptyMap.get(legalDocId).getNettable();
                    String collateralizable = legalDoctoCptyMap.get(legalDocId).getCollateralizable();
                    String cptyId = legalDoctoCptyMap.get(legalDocId).getCptyId();

                    if ("N".equalsIgnoreCase(nettable)) {
                        // Not nettable -> No Doc
                        out.get(tuples[0]).output(
                            KV.of(new CompositeID(tradeId, legalDocId, cptyId), input.getValue().getResults()));
                    } else if ("N".equalsIgnoreCase(collateralizable)) {
                        // Netable + Not collateralizable -> CloseOut
                        out.get(tuples[1])
                            .output(KV.of(new CompositeID(null, legalDocId, cptyId), input.getValue().getResults()));
                    } else {
                        // Netable + Collaterlizable -> CSA (Collateral)
                        out.get(tuples[2])
                            .output(KV.of(new CompositeID(null, legalDocId, cptyId), input.getValue().getResults()));
                    }
                    // TODO why NoDoc and CSA no need trade ID?
                }
            })//.withSideInputs(tradeMap, legalDocMap)
                // Pass side inputs to your ParDo transform by invoking ".withSideInputs".
                // Inside your DoFn, access the side input by using the method DoFn.ProcessContext.sideInput.
                .withOutputTags(tuples[0], TupleTagList.of(tuples[1]).and(tuples[2])));
    }
}
