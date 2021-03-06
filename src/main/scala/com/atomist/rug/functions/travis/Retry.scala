package com.atomist.rug.functions.travis

import scala.util.{Failure, Random, Success, Try}
import com.typesafe.scalalogging.Logger

/**
  * Generic retry with exponential backoff and jitter.
  */
object Retry {

  private val logger = Logger("Retry")

  private val rng = Random

  /**
    * Implement retry with exponential backoff and jiggle.  Each subsequent invocation
    * will wait double + jiggle of the previous.  Defaults for retries and wait result
    * in a mean retry period of about 50 seconds.
    *
    * @param opName name of operation, used for logging
    * @param n how many attempts to make
    * @param wait amount of time to wait
    * @param fn operation to retry
    * @tparam T return type pf fn
    * @return return value from successful call of fn
    */
  @annotation.tailrec
  private[travis] final def retry[T](opName: String, n: Int = 9, wait: Long = 0L)(fn: => T): T = {
    Thread.sleep(wait)
    Try { fn } match {
      case Success(x) => x
      case Failure(e : DoNotRetryException) => throw e
      case Failure(e) if n > 0 =>
        logger.warn(s"$opName attempt failed (${e.getMessage}), $n attempts left", e)
        retry(opName, n - 1, wait * 2L + rng.nextInt(100))(fn)
      case Failure(e) => throw e
    }
  }

}

class DoNotRetryException(msg: String, cause: Throwable) extends RuntimeException(msg, cause)
