package com.openbankproject.commons.util

import java.util.Objects

import com.openbankproject.commons.util.ApiVersion.allVersion
import net.liftweb.json.JsonAST.{JArray, JField, JObject, JString}

import scala.annotation.StaticAnnotation
import scala.reflect.runtime.universe._
import Functions.RichCollection
import net.liftweb.json.JsonDSL._


/**
 * Mark given type's field or constructor variable is required for some apiVersion
 *
 * Only the follow three style be legal
 * example:
 *  required for all versions: [allVersion]
 *  > @OBPRequired
 *
 *  required for some versions except some versions: [v3_0_0, v4_1_0]
 *  > @OBPRequired(Array(ApiVersion.v3_0_0, ApiVersion.v4_1_0))
 *
 *  required for all versions except some versions: [-v3_0_0, -v4_1_0]
 *  > @OBPRequired(include=Array(ApiVersion.allVersion), exclude=Array(ApiVersion.v3_0_0, ApiVersion.v4_1_0))
 *
 * Note: The include and exclude parameter should not change order, because this is not a real class, it is annotation, scala's
 * annotation not allowed switch parameter's order as these:
 *    > @OBPRequired(exclude = Array(ApiVersion.allVersion), include = Array(ApiVersion.v3_0_0)
 *    > @OBPRequired(exclude = Array(ApiVersion.allVersion))
 *
 * @param value do validate for these apiVersions
 * @param exclude not do validate for these apiVersions
 */
@scala.annotation.meta.field
@scala.annotation.meta.param
class OBPRequired(value: Array[ApiVersion] = Array(ApiVersion.allVersion),
                  exclude: Array[ApiVersion] = Array.empty
                 ) extends StaticAnnotation


sealed class RequiredFields
/**
 * cooperate with RequiredInfo to deal with generate swagger doc and json serialization problem (show wrong structure of json)
 * field `data.bankId`: it is special identifier name that just for ResourceDoc definitions
 */
object FieldNameApiVersions extends RequiredFields with JsonAble {
  val `data.bankId`: List[String] = List(ApiVersion.v2_2_0.toString, ApiVersion.v3_1_0.toString)

  override def toJValue: JObject = "data.bankId" -> JArray(this.`data.bankId`.map(JString(_)))
}

/**
 * cooperate with RequiredInfo to deal with generate swagger doc and json serialization problem (show wrong structure of json)
 * @param requiredArgs
 */
case class RequiredInfo(requiredArgs: Seq[RequiredArgs]) extends RequiredFields with JsonAble {

  override def toJValue: JObject = {
    val jFields = requiredArgs
        .toList
        .map(info => JField(
                      info.fieldPath,
                      JArray(info.apiVersions.map(JString(_)))
                    )
        )
    JObject(jFields)
  }
}

case class RequiredArgs(fieldPath:String, include: Array[ApiVersion],
                        exclude: Array[ApiVersion] = Array.empty) extends JsonAble {
  {
    val includeAll = include.contains(allVersion)
    val excludeAll = exclude.contains(allVersion)
    val excludeSome = exclude.filterNot(allVersion ==).nonEmpty

    def assertNot(assertion: Boolean, message: => Any) = assert(!assertion, message)

    assertNot(includeAll && include.size > 1, s"OBPRequired's include have $allVersion, it should not have other ApiVersion values.")
    assertNot(excludeAll, s"OBPRequired's should not exclude $allVersion.")

    assertNot(!includeAll && excludeSome, s"OBPRequired's when exclude have values, include must be $allVersion.")
    assertNot(include.isEmpty && exclude.isEmpty, s"OBPRequired's should not both include and exclude are empty.")
  }

  def isNeedValidate(version: ApiVersion): Boolean = {
    if(include.contains(allVersion)) {
      !exclude.contains(version)
    } else {
      include.contains(version)
    }
  }

  // only the follow pattern are valid: [allVersion], [v3_0_0, v4_1_0], [-v3_0_0, -v4_1_0]
  override def toString: String = (include, exclude) match {
    case (_, Array()) => include.mkString("[", ", ", "]")
    case (_, ex) if ex.nonEmpty => exclude.mkString("[-", ", -", "]")
  }

  override def equals(obj: Any): Boolean = obj match {
    case RequiredArgs(path, inc, exc) => Objects.equals(fieldPath, path) && include.sameElements(inc) && exclude.sameElements(exc)
    case _ => false
  }
  override val toJValue: JArray = (include, exclude) match {
    case (_, Array()) =>
      val includeList = include.toList.map(_.toString).map(JString(_))
      JArray(includeList)
    case _ =>
      val excludeList = include.toList.map("-" + _.toString).map(JString(_))
      JArray(excludeList)
  }
  val apiVersions: List[String] = (include, exclude) match {
    case (_, Array()) => include.toList.map(_.toString)
    case _ => include.toList.map("-" + _.toString)
  }
}

object RequiredFieldValidation {
  val OBP_REQUIRED_NAME = classOf[OBPRequired].getSimpleName()

  //OBPRequired's first default param and second default param name, they are end with 1 and 2 accordingly
  val defaultParam = """\$lessinit\$greater\$default\$([1,2])""".r

  private def getVersions(tree: Tree): Array[ApiVersion] = tree match {
    // match default parameter value
    case Select(Select(_: Tree, TermName(OBP_REQUIRED_NAME)), TermName(defaultParam(num))) => {
      if(num == "1") Array(allVersion) else Array.empty
    }
    // match Array.empty
    case Apply(TypeApply(Select(Select(Ident(TermName("scala")),TermName("Array")), TermName("empty")) , _: List[Tree]), _) => {
      Array.empty
    }
    // match Array() and Array(...)
    case Apply(Apply(TypeApply(Select(Select(Ident(TermName("scala")),TermName("Array")), TermName("apply")), TypeTree()::Nil), versionList:List[Tree]), _) => {
      versionList.toArray.map({
        case Select(Select(packageName: Select, TermName(typeName)), TermName(versionName)) =>
          ReflectUtils.getField(s"$packageName.$typeName", versionName).asInstanceOf[ApiVersion]

        case Select(outer: Ident, TermName(versionName)) =>
          val outerName = outer.tpe.toString.replace(".type", "")
          ReflectUtils.getField(outerName, versionName).asInstanceOf[ApiVersion]

        case ident: Ident =>
          ReflectUtils.getObject(ident.tpe.toString).asInstanceOf[ApiVersion]

        case _ => throw new IllegalArgumentException(s"$OBP_REQUIRED_NAME's parameter not correct: $versionList")
      })
    }
    // match include and exclude order exchanged
    case Ident(TermName("x$1")) | Ident(TermName("x$2")) => throw new IllegalArgumentException(s"$OBP_REQUIRED_NAME's parameter order should not be changed, the first parameter must be [include]'")
    case _ => throw new IllegalArgumentException(s"$OBP_REQUIRED_NAME's parameter not correct.")
  }

  /**
   * get all field name to OBPRequired annotation info
   * @param tp to process type
   * @return map of field name to RequiredArgs
   */
  def getAnnotations(tp: Type): Iterable[RequiredArgs] = {
    val members = tp.members

    val constructors = members.filter(_.isConstructor).map(_.asMethod)

    def getFieldNameAndAnnotation(symbol: Symbol): Option[RequiredArgs] =  {
      val fieldName = symbol.name.decodedName.toString.trim
      getAnnotation(fieldName, symbol) match{
        case some: Some[RequiredArgs] => some
        case _ => None
      }
    }

    // constructor param name to RequiredArgs
    val constructorParamToRequiredArgs: Iterable[RequiredArgs] = constructors
      .flatMap(_.paramLists.head) // all the constructor's parameters
      .map(getFieldNameAndAnnotation)
      .collect {
        case Some(requiredArgs) => requiredArgs
      }
    val constructorParamNames = constructorParamToRequiredArgs.map(_.fieldPath).toSet
    // those annotated field name to RequiredArgs
    val annotatedFieldNameToRequiredArgs: Iterable[RequiredArgs] =
      members
        .filter(it => {
          !it.isConstructor && !constructorParamNames.contains(it.name.decodedName.toString.trim)
        })
        .map(getFieldNameAndAnnotation)
        .collect {
          case Some(requiredArgs) => requiredArgs
        }
        .distinctBy(_.fieldPath)

    constructorParamToRequiredArgs ++ annotatedFieldNameToRequiredArgs
  }

  def getAnnotation(fieldName: String, symbol: Symbol): Option[RequiredArgs] = {
    val annotation: Option[Annotation] =
      (symbol :: symbol.overrides)
        .flatMap(_.annotations)
        .find(_.tree.tpe <:< typeOf[OBPRequired])

    annotation.map { it: Annotation =>
      it.tree.children.tail match {
        case (include: Tree)::(exclude: Tree)::Nil => RequiredArgs(fieldName, getVersions(include), getVersions(exclude))
      }
    }
  }

  def getAllNestedRequiredInfo(tp: Type,
                               predicate: Type => Boolean = Functions.isOBPType,
                               fieldName: String = null
  ): Seq[RequiredArgs] = {

    if(!predicate(tp)) {
      return Nil
    }

    val fieldToRequiredInfo: Iterable[RequiredArgs] = getAnnotations(tp)

    // current type's fields full path to RequiredInfo
    val currentPathToRequiredInfo = fieldName match {
      case null => fieldToRequiredInfo
      case _ => fieldToRequiredInfo.map(it=> it.copy(fieldPath = s"$fieldName.${it.fieldPath}"))
    }

    // find all sub fields RequiredInfo
    val subPathToRequiredInfo: Iterable[RequiredArgs] = tp.members.collect {
      case m: MethodSymbolApi if m.isGetter => {
        (m.name.decodedName.toString.trim, ReflectUtils.getNestFirstTypeArg(m.returnType))
      }
      case m: TermSymbolApi if m.isCaseAccessor || m.isVal => {
        (m.name.decodedName.toString.trim, ReflectUtils.getNestFirstTypeArg(m.info))
      }
    } .filter(tuple => predicate(tuple._2))
      .distinctBy(_._1)
      .flatMap(pair => {
        val (memberName, membersType) = pair
        val subFieldName = if(fieldName == null) memberName else s"$fieldName.$memberName"
        getAllNestedRequiredInfo(membersType, predicate, subFieldName)
      })

    (currentPathToRequiredInfo ++ subPathToRequiredInfo).toSeq.sortBy(_.fieldPath)
  }
}

