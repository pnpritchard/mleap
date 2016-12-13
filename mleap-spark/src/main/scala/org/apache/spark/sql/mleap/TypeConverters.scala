package org.apache.spark.sql.mleap

import ml.combust.mleap.runtime.types
import org.apache.spark.sql.types._
import org.apache.spark.mllib.linalg.VectorUDT

import scala.language.implicitConversions

/**
  * Created by hollinwilkins on 10/22/16.
  */
trait TypeConverters {
  implicit def sparkType(dataType: types.DataType): Option[DataType] = dataType match {
    case types.BooleanType => Some(BooleanType)
    case types.StringType => Some(StringType)
    case types.IntegerType => Some(IntegerType)
    case types.LongType => Some(LongType)
    case types.DoubleType => Some(DoubleType)
    case lt: types.ArrayType => sparkType(lt.base).map(t => ArrayType(t, containsNull = false))
    case tt: types.TensorType if tt.dimensions.length == 1 => Some(new VectorUDT())
    case ct: types.CustomType => Some(ct.klazz.getAnnotation(classOf[SQLUserDefinedType]).udt().newInstance())
    case types.AnyType => None
  }
}
object TypeConverters extends TypeConverters
