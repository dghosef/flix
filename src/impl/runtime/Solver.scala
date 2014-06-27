package impl.runtime

import impl._
import impl.util.collection.mutable

/**
 * A semi-naive solver.
 */
class Solver(p: Program) {

  /**
   * Relations.
   */
  val relation1 = mutable.MultiMap1.empty[Symbol, Value]
  val relation2 = mutable.MultiMap2.empty[Symbol, Value, Value]
  val relation3 = mutable.MultiMap3.empty[Symbol, Value, Value, Value]
  val relation4 = mutable.MultiMap4.empty[Symbol, Value, Value, Value, Value]
  val relation5 = mutable.MultiMap5.empty[Symbol, Value, Value, Value, Value, Value]

  /**
   * Lattice Maps.
   */
  val map1 = mutable.Map1.empty[Symbol, Value]
  val map2 = mutable.Map2.empty[Symbol, Value, Value]
  val map3 = mutable.Map3.empty[Symbol, Value, Value, Value]
  val map4 = mutable.Map4.empty[Symbol, Value, Value, Value, Value]
  val map5 = mutable.Map5.empty[Symbol, Value, Value, Value, Value, Value]

  /**
   * Dependencies.
   */
  val dependencies = mutable.MultiMap1.empty[Symbol, HornClause]

  /**
   * Worklist Queue
   */
  val queue = scala.collection.mutable.Queue.empty[(HornClause, Map[Symbol, Value])]

  /**
   * Depedency computation.
   */
  def deps(): Unit = {
    for (h <- p.clauses; p <- h.body) {
      dependencies.put(p.name, h)
    }
  }

  /**
   * Fixpoint computation.
   */
  def solve(): Unit = {

    for (f <- p.facts) {
      val name = f.head.name
      val terms = f.head.terms
    }


    while (queue.nonEmpty) {

    }

    ???
  }

  // TODO: Binding variables to values... for solving


  /**
   * Evaluates the given horn clause `h` under the given interpretations `inv` and environment `env`.
   *
   * Returns `true` iff the body is satisfied.
   */
  def evaluate(h: HornClause, inv: Map[Symbol, Interpretation], env: Map[Symbol, Value]): Boolean = inv.get(h.head.name) match {
    case None => throw new Error.UnknownInterpretation(h.head.name)
    case Some(i) => evaluate(h, i, env)
  }


  /**
   * Evaluates the given horn clause `h` under the given interpretation `i` and environment `env`.
   *
   * Returns `true` iff the body is satisfied.
   */
  def evaluate(h: HornClause, i: Interpretation, env: Map[Symbol, Value]): Boolean =
    ???


  def evaluate(p: Predicate, i: Interpretation, env: Map[Symbol, Value]): Boolean = i match {
    case Interpretation.Proposition(Value.Bool(true)) => true;
    case Interpretation.Proposition(Value.Bool(false)) => false;
    case Interpretation.Relation.In1(t1) => ???
    case _ => ???
  }

  /**
   * Satisfies the given predicate `p` under the given interpretation `i` and environment `env`.
   */
  def satisfy(p: Predicate, i: Interpretation, env: Map[Symbol, Value]): Unit = i match {
    case Interpretation.Relation.In1(t1) =>
      val v = lookupValue(p, 0, env)
      val newFact = relation1.put(p.name, v)
      if (newFact)
        propagate(p, Map(0 -> v))

    case Interpretation.Relation.In2(t1, t2) =>
      val k1 = lookupValue(p, 0, env)
      val v = lookupValue(p, 1, env)
      val newFact = relation2.put(p.name, k1, v)
      if (newFact)
        propagate(p, Map(0 -> k1, 1 -> v))

    case Interpretation.Relation.In3(t1, t2, t3) =>
      val k1 = lookupValue(p, 0, env)
      val k2 = lookupValue(p, 1, env)
      val v = lookupValue(p, 2, env)
      val newFact = relation3.put(p.name, k1, k2, v)
      if (newFact)
        propagate(p, Map(0 -> k1, 1 -> k2, 2 -> v))

    case Interpretation.Relation.In4(t1, t2, t3, t4) =>
      val k1 = lookupValue(p, 0, env)
      val k2 = lookupValue(p, 1, env)
      val k3 = lookupValue(p, 2, env)
      val v = lookupValue(p, 3, env)
      val newFact = relation4.put(p.name, k1, k2, k3, v)
      if (newFact)
        propagate(p, Map(0 -> k1, 1 -> k2, 2 -> k3, 3 -> v))

    case Interpretation.Relation.In5(t1, t2, t3, t4, t5) =>
      val k1 = lookupValue(p, 0, env)
      val k2 = lookupValue(p, 1, env)
      val k3 = lookupValue(p, 2, env)
      val k4 = lookupValue(p, 3, env)
      val v = lookupValue(p, 4, env)
      val newFact = relation5.put(p.name, k1, k2, k3, k4, v)
      if (newFact)
        propagate(p, Map(0 -> k1, 1 -> k2, 2 -> k3, 3 -> k4, 4 -> v))

    case Interpretation.Map.Leq1(t1) => ???
    case Interpretation.Map.Leq2(t1, t2) => ???
    case Interpretation.Map.Leq3(t1, t2, t3) => ???
    case Interpretation.Map.Leq4(t1, t2, t3, t4) => ???
    case Interpretation.Map.Leq5(t1, t2, t3, t4, t5) => ???

    case _ => throw new Error.UnableToSatisfyPredicate(p)
  }

  /**
   * Returns the value of the variable with the given `index` in the given predicate `p`.
   */
  def lookupValue(p: Predicate, index: Int, env: Map[Symbol, Value]): Value = p.terms.lift(index) match {
    case None => throw new Error.ArityMismatch(p, index)
    case Some(Term.Constant(v)) => v
    case Some(Term.Variable(s)) => env.get(s) match {
      case None => throw new Error.UnboundVariable(s)
      case Some(v) => v
    }
    case _ => ??? // TODO: What about destructors?
  }


  /**
   * Enqueues all depedencies of the given predicate with the given environment.
   */
  def propagate(p: Predicate, env: Map[Int, Value]): Unit = {
    // TODO: Need binding ...
    for (h <- dependencies.get(p.name)) {
      queue.enqueue((h, ???))
    }
  }

}
