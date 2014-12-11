package impl.ast2

import impl.logic._

object Compiler {

  def compile(ast: Ast.Root): Ast.Root = {
    val ast2 = Desugaring.desugar(ast)
    val env = Symbols.visit(ast)
    val ast3 = Disambiguation.disambiguate(ast, env)
    println(env)

    ast
  }

  /**
   * A compiler-phases which constructs environments (i.e. the symbol table).
   */
  object Symbols {

    /**
     * A (fully qualified) name is a list of strings.
     */
    // TODO Move into Ast and change to Seq.
    type Name = List[String]

    /**
     * An environment is map from names to ast declaractions.
     *
     * An environment may contain multiple declaractions for the same names:
     *
     * (1) Names may be overloaded for values, types, etc.
     * (2) Names may be ambiguous.
     */
    // Todo: Move into some abstract compiler trait.
    type Environment = MultiMap[Name, Ast.Declaration]

    /**
     * The empty environment.
     */
    val Empty = MultiMap.empty[Name, Ast.Declaration]

    /**
     * Returns an environment with the given mapping.
     */
    def environmentOf(kv: (Name, Ast.Declaration)): Environment = MultiMap(kv)

    /**
     * Returns a map from fully qualified names to ast declarations.
     */
    def visit(ast: Ast.Root): Environment = ast match {
      case Ast.Root(decls) => (decls foldLeft Empty) {
        case (env, decl) => env ++ visit(Nil, decl)
      }
    }

    /**
     * Returns a map from fully qualified names to ast declaractions assuming the declarations reside under the given namespace.
     */
    def visit(namespace: Name, ast: Ast.Declaration): Environment = ast match {
      case Ast.Declaration.NameSpace(name, body) => (body foldLeft Empty) {
        case (env, decl) => env ++ visit(withSuffix(namespace, name), decl)
      }
      case decl: Ast.Declaration.Tpe => environmentOf(withSuffix(namespace, decl.name) -> decl)
      case decl: Ast.Declaration.Val => environmentOf(withSuffix(namespace, decl.name) -> decl)
      case decl: Ast.Declaration.Var => environmentOf(withSuffix(namespace, decl.name) -> decl)
      case decl: Ast.Declaration.Fun => environmentOf(withSuffix(namespace, decl.name) -> decl)
      case decl: Ast.Declaration.Enum => Empty // TODO
      case decl: Ast.Declaration.Lattice => Empty
      case decl: Ast.Declaration.Fact => Empty
      case decl: Ast.Declaration.Rule => Empty
    }

    /**
     * Returns `name` . `suffix`.
     */
    def withSuffix(name: Name, suffix: String): Name = name ::: List(suffix)

    /**
     * Returns `name` . `suffix`.
     */
    def withSuffix(name: Name, suffix: Seq[String]): Name = name ::: suffix.toList
  }


  // TODO: Check
  // -unresolved references
  // -ambigious decls
  // -patterns with the same variable
  // -recursive types, calls, etc.
  /**
   * A compiler-phase which replaces name references by their actuals.
   */
  object Disambiguation {
    // replaces all names by their actuals.

    import Symbols._

    /**
     *
     */
    def disambiguate(ast: Ast.Root, env: Environment): Ast.Root = Ast.Root(ast.decls map {
      case decl => disambiguate(decl, env)
    })

    /**
      */
    def disambiguate(ast: Ast.Declaration, env: Environment): Ast.Declaration = ast match {
      case Ast.Declaration.NameSpace(name, body) => Ast.Declaration.NameSpace(name, body map {
        case decl => disambiguate(decl, env)
      })
      case decl: Ast.Declaration.Lattice => decl.copy(record = disambiguate(Nil, decl.record, env, Set.empty))

      case _ => ast
    }

    /**
     * Replaces all ambiguous names in the given expression.
     */
    def disambiguate(namespace: Name, ast: Ast.Expression, env: Environment, bound: Set[String]): Ast.Expression = ast match {

      case Ast.Expression.AmbiguousName(name) => lookupVal(namespace, name.toList, env)

      case e: Ast.Expression.Var => ??? // introduced

      case e: Ast.Expression.Lit => e

      case Ast.Expression.Unary(op, e) => Ast.Expression.Unary(op, disambiguate(namespace, e, env, bound))
      case Ast.Expression.Binary(e1, op, e2) => Ast.Expression.Binary(disambiguate(namespace, e1, env, bound), op, disambiguate(namespace, e2, env, bound))


      case Ast.Expression.Record(elms) => Ast.Expression.Record(elms map {
        case (name, e) => (name, disambiguate(namespace, e, env, bound))
      })
    }


    // TODO: Messy. Rewrite.
    def lookupVal(namespace: Name, name: Name, env: Environment): Ast.Expression = {
      // Case 1: lookup in the current namespace, i.e. namespace . name
      val values = env.get(namespace ::: name).collect {
        case d: Ast.Declaration.Val => d
      }

      if (values.size > 1) throw new RuntimeException("Ambigious name")
      else {
        // try global namespace: name
        val values2 = env.get(name).collect {
          case d: Ast.Declaration.Val => d
        }
        if (values2.size == 1) return values.head.exp
        else if (values2.isEmpty)
          throw new RuntimeException("Name not found: " + name)
        else throw new RuntimeException("Ambigious name: " + values2)
      }
    }

  }





  case class CompilerException(msg: String) extends RuntimeException(msg)


  // TODO: Move somewhere appropiate.
  object MultiMap {
    def empty[K, V]: MultiMap[K, V] = new MultiMap[K, V](Map.empty[K, Set[V]])

    def apply[K, V](kv: (K, V)): MultiMap[K, V] = new MultiMap[K, V](Map[K, Set[V]](kv._1 -> Set(kv._2)))
  }

  class MultiMap[K, V](val m: Map[K, Set[V]]) {
    def get(k: K): Set[V] = m.getOrElse(k, Set.empty[V])

    def ++(that: MultiMap[K, V]): MultiMap[K, V] = new MultiMap(
      (that.m foldLeft this.m) {
        case (acc, (thatKey, thatValues)) =>
          val thisValues = acc.getOrElse(thatKey, Set.empty)
          acc + (thatKey -> (thisValues ++ thatValues))
      }
    )

    override def toString: String = m.toString()
  }

}
