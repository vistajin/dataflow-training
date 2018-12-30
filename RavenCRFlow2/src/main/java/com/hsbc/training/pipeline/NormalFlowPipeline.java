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

import com.hsbc.training.pipeline.entity.LegalDoc;
import com.hsbc.training.pipeline.entity.Trade;
import com.hsbc.training.pipeline.entity.TradeResult;
import com.hsbc.training.pipeline.function.ParseLegalDocFn;
import com.hsbc.training.pipeline.function.ParseTradeAttrFn;
import com.hsbc.training.pipeline.function.ParseTradeResultFn;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class NormalFlowPipeline {

  private static final Logger LOG = LoggerFactory.getLogger(NormalFlowPipeline.class);

  public static void main(String[] args) {
    // PipelineOptionsFactory.register(CustomOptions.class);
    CustomOptions options = PipelineOptionsFactory.fromArgs(args)
            .withValidation().as(CustomOptions.class);

    Pipeline pipeline = Pipeline.create(options);

    PCollection<String> tradeResultPs = pipeline.apply(TextIO.read()
            .from(options.getTradeResult()));
    System.out.println(tradeResultPs);
    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

    PCollection<TradeResult> tradeResult = tradeResultPs.apply(ParDo.of(new ParseTradeResultFn()));


    PCollection<String> legalDocPs = pipeline.apply(TextIO.read().
            from(options.getLegalDoc()));
    // System.out.println(legalDocPs);
    PCollectionView<Map<String, LegalDoc>> legalDoc = legalDocPs.apply(
            ParDo.of(new ParseLegalDocFn())).apply("Legal doc to map", View.asMap());
    System.out.println(legalDoc);


    PCollection<String> tradeAttributePs = pipeline.apply(TextIO.read()
            .from(options.getTradeAttribute()));
    // System.out.println(tradeAttribute);
    PCollectionView<Map<String, Trade>> tradeAttribute = tradeAttributePs.apply(
            ParDo.of(new ParseTradeAttrFn())).apply("Trade attr to map", View.asMap());
    System.out.println(tradeAttribute);


    pipeline.run();
  }
}
