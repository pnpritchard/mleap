package ml.combust.mleap.runtime

import org.apache.spark.mllib.linalg.Vectors
import org.scalatest.FunSpec

/** Base trait for testing [[ml.combust.mleap.runtime.Row]] implementations.
  *
  * @tparam R row class
  */
trait RowSpec[R <: Row] extends FunSpec {
  def create(values: Any *): R

  def row(): Unit = {
    val rowValues = Seq("test", 42, Array(56, 78, 23), 57.3, Vectors.dense(Array(2.3, 4.4)), 56L)
    val row = create(rowValues: _*)

    describe("#apply") {
      it("gets the value at a given index") {
        assert(row(0) == "test")
        assert(row(1) == 42)
        assert(row(2).asInstanceOf[Array[Int]] sameElements Array(56, 78, 23))
      }
    }

    describe("#get") {
      it("gets the value at a given index") {
        assert(row.get(0) == "test")
        assert(row.get(1) == 42)
        assert(row.get(2).asInstanceOf[Array[Int]] sameElements Array(56, 78, 23))
      }
    }

    describe("#getAs") {
      it("gets the value at a given index and casts it") {
        assert(row.getAs[String](0) == "test")
        assert(row.getAs[Int](1) == 42)
        assert(row.getAs[Array[Int]](2) sameElements Array(56, 78, 23))
      }
    }

    describe("#getDouble") {
      it("gets the value at a given index as a double") {
        assert(row.getDouble(3) == 57.3)
      }
    }

    describe("#getInt") {
      it("gets the value at a given index as an int") {
        assert(row.getInt(1) == 42)
      }
    }

    describe("#getLong") {
      it("gets the value at a given index as a long") {
        assert(row.getLong(5) == 56)
      }
    }

    describe("#getString") {
      it("gets the value at a given index as a string") {
        assert(row.getString(0) == "test")
      }
    }

    describe("#getVector") {
      it("gets the value at a given index as a vector") {
        val vec = row.getVector(4)

        assert(vec(0) == 2.3)
        assert(vec(1) == 4.4)
      }
    }

    describe("#getArray") {
      it("gets the value at a given index as an array") {
        val arr = row.getArray[Int](2)

        assert(arr(0) == 56)
        assert(arr(1) == 78)
        assert(arr(2) == 23)
      }
    }

    describe("#toArray") {
      it("gets all the values as an array") {
        assert(row.toArray sameElements rowValues)
      }
    }

    describe("#toSeq") {
      it("gets all the values as a seq") {
        assert(row.toSeq == rowValues)
      }
    }

    describe("#withValue") {
      describe("user defined function") {
        it("adds a value using a user defined function") {
          val r2 = row.withValue(r => r.get(1), r => r.get(2)) {
            (v1: Int, v2: Array[Int]) => v1 + v2(0)
          }

          assert(r2.getInt(6) == 98)
        }
      }

      describe("value arg") {
        it("adds the value to the row") {
          val r = row.withValue(789)

          assert(r.getInt(6) == 789)
        }
      }
    }

    describe("#selectIndices") {
      it("creates a new row from the selected indices") {
        val r = row.selectIndices(3, 0)

        assert(r.getDouble(0) == 57.3)
        assert(r.getString(1) == "test")
      }
    }

    describe("#dropIndex") {
      it("drops the value at an index") {
        val r = row.dropIndex(2).dropIndex(3)

        assert(r.toArray sameElements Array("test", 42, 57.3, 56L))
      }
    }
  }
}

class SeqRowSpec extends RowSpec[ArrayRow] {
  override def create(values: Any *): ArrayRow = ArrayRow(values)

  it should behave like row()
}
