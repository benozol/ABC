/*
 * This file is part of the Automatic Brighton Classification Tool (ABC-Tool).
 *
 * The ABC-Tool is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * The ABC-Tool is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero Affero General Public
 * License along with The ABC-Tool.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2008, 2012 Benedikt Becker
 */

package org.brightoncollaboration.abc

import java.text.ParseException
import java.io.File
import scala.util.parsing.combinator.lexical.StdLexical
import scala.util.parsing.combinator.syntactical._
import scala.io.Source
import scala.collection.immutable.Map
import scala.collection.JavaConversions._
import org.brightoncollaboration.abc.Value._
import org.brightoncollaboration.abc.clause.{Clause, Relation}
import org.brightoncollaboration.abc.tools.StringI18n

// TODO drop restriction of criteria to start with an upper case letter

class CaseDefinitionParser(cc:Criterion.Creator) extends StandardTokenParsers {

  lexical.reserved ++= List("TRUE", "NOTAPPLICABLE", "AND", "OR", "NOT", "CASEDEFINITION", "LEVEL1", "LEVEL2", "LEVEL3", "EXACTLY", "ATMOST", "ATLEAST", "OF", "FROM")
  lexical.delimiters ++= List("(", ")", ",", "!", "?", ":")

  def this() =
    this(Criterion.Creator.fromScratch())

  def constant:Parser[Clause] =
    ( "TRUE"          ^^^ Clause.tru()
    | "NOTAPPLICABLE" ^^^ Clause.fals()
    | failure("Not a valid constant (TRUE, NOTAPPLICABLE)")
    )

  def criterion:Parser[String] = {
    ( elem("criterion", { case lexical.Identifier(name) => name.charAt(0).isUpper case _ => false }) ^^
        { case lexical.Identifier(name) => name }
    | failure("Criteria have to start with an upper case letter")
    )
  }

  def number:Parser[Int] =
    numericLit ^^ ( _.toInt )

  def name:Parser[StringI18n] = {
    def translation:Parser[(String, String)] =
      ident ~ ":" ~ stringLit ^^ { case lang ~ ":" ~ name => (lang, name) }
    ( stringLit         ^^ StringI18n.singleton
    | rep1(translation) ^^ { ts => new StringI18n(Map(ts:_*)) }
    | failure("The name of a case definition must be enclosed in double quotation marks")
    )
  }

  def symptom:Parser[Clause] = {
    def aux(value:Value)(shortName:String) = 
      Clause.criterion(cc.getByShortName(shortName), value)
    ( "!" ~> criterion ^^ aux(NEGATIVE)
    | "?" ~> criterion ^^ aux(UNDEFINED)
    |        criterion ^^ aux(POSITIVE)
    )
  }

  def term:Parser[Clause] =
    ( constant | symptom | "(" ~> clause <~ ")"
    | failure("Only constants, criteria and parenthesize clauses allowed here")
    )

  def relation:Parser[Relation] =
    ( "EXACTLY" ^^^ Relation.EXACTLY
    | "ATMOST"  ^^^ Relation.AT_MOST
    | "ATLEAST" ^^^ Relation.AT_LEAST
    | failure("Not a valid nlist relation (EXACTLY, ATMOST, ATLEAST)")
    )

  def prefixClause:Parser[Clause] =
    ( "NOT" ~> term              ^^ Clause.not
    | "AND" ~> repsep(term, ",") ^^ {
        case cs => Clause.and(cs.toArray:_*)
      }
    | "OR" ~> repsep(term, ",") ^^ {
        case cs => Clause.nlist(Relation.AT_LEAST, 1, cs.toArray:_*)
      }
    | relation ~ number ~ "FROM" ~ repsep(term, ",") ^^ {
        case rel ~ n ~ "FROM" ~ cs => Clause.nlist(rel, n, cs.toArray:_*)
      }
    | failure("Not a valid prefix clause (NOT ... or AND ... or ATLEAST/EXACTLY/ATMOST n FROM ...)")
    )

  def infixClause:Parser[Clause] =
    term >> { c1 =>
      List[(String, List[Clause] => Clause)](
          ("AND", Clause.and((_:List[Clause]):_*)),
          ("OR", Clause.nlist(Relation.AT_LEAST, 1, (_:List[Clause]):_*))
      ).map({
          case (sep, ctor) =>
            rep1(sep ~> term) ^^ {
              cs => ctor(c1 :: cs)
            }
      }).reduceLeft(_ | _) |
      failure("Not a valid infix operator (... AND ... AND ...)")
    }

  def clause:Parser[Clause] =
      constant ||| symptom ||| prefixClause ||| infixClause 

      
  def caseDefinition:Parser[CaseDefinition] =
      "CASEDEFINITION" ~ name ~
      "LEVEL1" ~ "(" ~ clause ~ ")" ~
      "LEVEL2" ~ "(" ~ clause ~ ")" ~
      "LEVEL3" ~ "(" ~ clause ~ ")" ^^ {
          case "CASEDEFINITION" ~ nm ~ "LEVEL1" ~ "(" ~ c1 ~ ")" ~ "LEVEL2" ~ "(" ~ c2 ~ ")" ~ "LEVEL3" ~ "(" ~ c3 ~ ")" =>
              new CaseDefinition(nm, c1, c2, c3)
      }

  def parseClause(s:String) = {
      val tokens = new lexical.Scanner(s)
      phrase(clause)(tokens)
  }

  def parseCaseDefinition(s:String) = {
      val tokens = new lexical.Scanner(s)
      phrase(caseDefinition)(tokens)
  }
}

object AbcParser {

  def main(args:Array[String]) {
      val parser = new CaseDefinitionParser(Criterion.Creator.fromScratch())
      if( args.length > 0 )
          parser.parseClause (args(0)) match {
              case parser.Success(clause, _) =>
                  println(clause.unparse())
              case e:parser.NoSuccess =>
                  println(e)
          }
      else
          parser.parseClause (Source.fromInputStream(System.in).mkString("", "", "")) match {
              case parser.Success(clause, _) =>
                  println(clause.unparse())
              case e:parser.NoSuccess =>
                  println(e)
          }
  }


  @throws(classOf[ParseException])
  def parseCaseDefinition(file:File):CaseDefinition =
      parseCaseDefinition(file, Criterion.Creator.fromScratch())

  @throws(classOf[ParseException])
  def parseCaseDefinition(file:File, cc:Criterion.Creator):CaseDefinition =
      parseCaseDefinition(scala.io.Source.fromFile(file).mkString, cc)

  @throws(classOf[ParseException])
  def parseCaseDefinition(str:String, cc:Criterion.Creator):CaseDefinition = {
      val parser = new CaseDefinitionParser(cc)
      parser.parseCaseDefinition(str) match {
        case parser.Success(cd, _) =>
          return cd
        case e:parser.NoSuccess =>
          throw new ParseException(e.msg, e.next.offset)
      }
  }


  @throws(classOf[ParseException])
  def parseClause(str:String, cc:Criterion.Creator):Clause = {
      val parser = new CaseDefinitionParser(cc)
      parser.parseClause (str) match {
          case parser.Success(c, _) =>
              return c
          case e:parser.NoSuccess =>
              throw new ParseException(e.msg, e.next.offset)
      }
  }

  @throws(classOf[ParseException])
  def parseClause(str:String):Clause =
      parseClause(str, Criterion.Creator.fromScratch())
}

// vim: set ts=4 sw=2 et:
