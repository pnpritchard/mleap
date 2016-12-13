package ml.combust.mleap.bundle.ops.feature

import ml.combust.bundle.BundleContext
import ml.combust.bundle.dsl._
import ml.combust.bundle.op.{OpModel, OpNode}
import ml.combust.mleap.core.feature.MaxAbsScalerModel
import ml.combust.mleap.runtime.MleapContext
import ml.combust.mleap.runtime.transformer.feature.MaxAbsScaler
import org.apache.spark.mllib.linalg.Vectors

/**
  * Created by mikhail on 9/19/16.
  */
class MaxAbsScalerOp extends OpNode[MleapContext, MaxAbsScaler, MaxAbsScalerModel]{
  override val Model: OpModel[MleapContext, MaxAbsScalerModel] = new OpModel[MleapContext, MaxAbsScalerModel] {
    override val klazz: Class[MaxAbsScalerModel] = classOf[MaxAbsScalerModel]

    override def opName: String = Bundle.BuiltinOps.feature.max_abs_scaler

    override def store(model: Model, obj: MaxAbsScalerModel)
                      (implicit context: BundleContext[MleapContext]): Model = {
      model.withAttr("maxAbs", Value.doubleVector(obj.maxAbs.toArray))
    }

    override def load(model: Model)
                     (implicit context: BundleContext[MleapContext]): MaxAbsScalerModel = {
      MaxAbsScalerModel(maxAbs = Vectors.dense(model.value("maxAbs").getDoubleVector.toArray))
    }
  }

  override val klazz: Class[MaxAbsScaler] = classOf[MaxAbsScaler]

  override def name(node: MaxAbsScaler): String = node.uid

  override def model(node: MaxAbsScaler): MaxAbsScalerModel = node.model

  override def load(node: Node, model: MaxAbsScalerModel)
                   (implicit context: BundleContext[MleapContext]): MaxAbsScaler = {
    MaxAbsScaler(uid = node.name,
      inputCol = node.shape.standardInput.name,
      outputCol = node.shape.standardOutput.name,
      model = model)
  }

  override def shape(node: MaxAbsScaler): Shape = Shape().withStandardIO(node.inputCol, node.outputCol)
}
