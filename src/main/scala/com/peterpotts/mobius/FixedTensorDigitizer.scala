package com.peterpotts.mobius

import scala.annotation.tailrec

class FixedTensorDigitizer(
  tensor: Tensor,
  leftContinuation: => Digitizer,
  rightContinuation: => Digitizer) extends Digitizer {
  lazy private val leftDigitizer = leftContinuation
  lazy private val rightDigitizer = rightContinuation

  lazy val (head, tail) = digitize

  @tailrec private def digitize: (Matrix, Digitizer) =
    split match {
      case Some((digit, remainder)) =>
        digit -> new FixedTensorDigitizer(remainder, leftDigitizer, rightDigitizer)
      case None =>
        if (tensor.range > tensor.transpose.range)
          new FixedTensorDigitizer(tensor * leftDigitizer.head, leftDigitizer.tail, rightDigitizer).digitize
        else
          new FixedTensorDigitizer(tensor ** rightDigitizer.head, leftDigitizer, rightDigitizer.tail).digitize
    }

  private lazy val alpha = Digit.alpha.inverse * tensor
  private lazy val beta = Digit.beta.inverse * tensor
  private lazy val gamma = Digit.gamma.inverse * tensor

  private lazy val split =
    if (alpha.valid)
      Some(Digit.alpha -> alpha)
    else if (gamma.valid)
      Some(Digit.gamma -> gamma)
    else if (beta.valid)
      Some(Digit.beta -> beta)
    else
      None
}