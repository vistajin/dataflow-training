/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hsbc.training.pipeline;

import com.hsbc.training.pipeline.entity.CombinedTradeResult;
import com.hsbc.training.pipeline.entity.CompositeID;
import com.hsbc.training.pipeline.entity.LegalDoc;
import com.hsbc.training.pipeline.entity.Trade;
import com.hsbc.training.pipeline.function.ParseLegalDocFn;
import com.hsbc.training.pipeline.function.ParseTradeAttrFn;
import com.hsbc.training.pipeline.function.ParseTradeResultFn;
import com.hsbc.training.pipeline.transform.CombineTimestepTradeResultCompositeTransform;
import com.hsbc.training.pipeline.transform.SplitTradeByLegalDocCompositeTransform;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.values.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class NormalFlowPipeline {

    private static final Logger LOG = LoggerFactory.getLogger(NormalFlowPipeline.class);

    public static void main(String[] args) {
        // TODO why need register?
        PipelineOptionsFactory.register(CustomOptions.class);
        CustomOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(CustomOptions.class);

        Pipeline pipeline = Pipeline.create(options);

        PCollection<KV<String, CombinedTradeResult>> combinedTradeResult =
            pipeline.apply(TextIO.read().from(options.getTradeResult()))
                .apply(ParDo.of(ParseTradeResultFn.getInstance()))
                .apply(new CombineTimestepTradeResultCompositeTransform());

        // need PCollectionView because side input need this type
        PCollectionView<Map<String, LegalDoc>> legalDoc = pipeline.apply(TextIO.read().
            from(options.getLegalDoc())).apply(ParDo.of(ParseLegalDocFn.getInstance()))
            .apply("Legal doc to map", View.asMap());

        PCollectionView<Map<String, Trade>> tradeAttr =
            pipeline.apply(TextIO.read().from(options.getTradeAttribute()))
                .apply(ParDo.of(ParseTradeAttrFn.getInstance())).apply("Trade attr to map", View.asMap());

        TupleTag<KV<CompositeID, Map<String, double[]>>> noDocTuple =
            new TupleTag<KV<CompositeID, Map<String, double[]>>>() {};
        TupleTag<KV<CompositeID, Map<String, double[]>>> closeOutTuple =
            new TupleTag<KV<CompositeID, Map<String, double[]>>>() {};
        TupleTag<KV<CompositeID, Map<String, double[]>>> csaTuple =
            new TupleTag<KV<CompositeID, Map<String, double[]>>>() {};

        PCollectionTuple resultTuple = combinedTradeResult.apply(
            new SplitTradeByLegalDocCompositeTransform(tradeAttr, legalDoc, noDocTuple, closeOutTuple, csaTuple));

        PCollection<KV<CompositeID, Map<String, double[]>>> noDocSet = resultTuple.get(noDocTuple);
        PCollection<KV<CompositeID, Map<String, double[]>>> closeOutSet = resultTuple.get(closeOutTuple);
        PCollection<KV<CompositeID, Map<String, double[]>>> csaSet = resultTuple.get(csaTuple);

        // Apply netting for CloseOut and CSA


        // Apply collateral balance for CSA


        // Join NoDoc, CloseOut, CSA

        // Apply floor = 0

        // Sum up to counterparty level

        // Apply average function over scenario


        pipeline.run();
    }
}
