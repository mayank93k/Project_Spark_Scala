package spark.scala.org.UniqueLocation

import org.apache.log4j._
import org.apache.spark._
import org.apache.spark.rdd.RDD
import spark.scala.org.InputOutputFileUtility

import scala.collection.Map

class UniqueLocation(sc: SparkContext) {

  // read data from text files and computes the results. 
  def run(t: String, u: String): RDD[(String, String)] = {
    val transations = sc.textFile(InputOutputFileUtility.getInputPath(t))
    val transPair = transations.map { t =>
      val p = t.split("\t")
      (p(2).toInt, p(1).toInt)
    }
    val users = sc.textFile(InputOutputFileUtility.getInputPath(u))
    val userPair = users.map { t =>
      val p = t.split("\t")
      (p(0).toInt, p(3))
    }
    val result = processData(transPair, userPair)
    sc.parallelize(result.toSeq).map(t => (t._1.toString, t._2.toString))
  }

  private def processData(t: RDD[(Int, Int)], u: RDD[(Int, String)]): Map[Int, Long] = {
    val jne = t.join(u).values.distinct
    jne.countByKey
  }
}

object UniqueLocation {
  System.setProperty("hadoop.home.dir", "C:\\winutils")

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    val sc = new SparkContext("local[*]", "UniqueLocation")
    val job = new UniqueLocation(sc)
    val results = job.run("transactions_test.txt", "users_test.txt")
    results.saveAsTextFile(InputOutputFileUtility.getOutputPath("locationOut"))
    sc.stop()
  }
}
  
