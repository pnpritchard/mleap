package org.apache.spark.ml.parity.feature

import org.apache.spark.ml.parity.SparkParityBase
import org.apache.spark.ml.feature.{ElementwiseProduct, VectorAssembler}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.ml.{Pipeline, Transformer}
import org.apache.spark.sql.DataFrame

/**
  * Created by hollinwilkins on 10/30/16.
  */
class ElementWiseProductParitySpec extends SparkParityBase {
  override val dataset: DataFrame = baseDataset.select("dti", "loan_amount")
  override val sparkTransformer: Transformer = new Pipeline().setStages(Array(new VectorAssembler().
    setInputCols(Array("dti", "loan_amount")).setOutputCol("features"),
    new ElementwiseProduct().setInputCol("features").setOutputCol("scaled_features").setScalingVec(Vectors.dense(1.3, 3.4)))).
    fit(dataset)
}
