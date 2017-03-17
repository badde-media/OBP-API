package code.remotedata

import java.util.Date

import akka.actor.Actor
import akka.event.Logging
import code.metadata.counterparties.{MapperCounterparties, RemotedataCounterpartiesCaseClasses}
import code.model._


class RemotedataCounterpartiesActor extends Actor with ActorHelper {

  val logger = Logging(context.system, this)

  val mapper = MapperCounterparties
  val cc = RemotedataCounterpartiesCaseClasses

  def receive = {

    case cc.checkCounterpartyAvailable(name: String, thisBankId: String, thisAccountId: String, thisViewId: String)=>
      logger.info("checkCounterpartyAvailable(" + name +", "+ thisBankId +", "+ thisAccountId +", "+ thisViewId +")")
      sender ! extractResult(mapper.checkCounterpartyAvailable(name: String, thisBankId: String, thisAccountId: String, thisViewId: String))

    case cc.createCounterparty(createdByUserId, thisBankId, thisAccountId, thisViewId,
                               name, otherAccountRoutingScheme,
                               otherAccountRoutingAddress, otherBankRoutingScheme,
                               otherBankRoutingAddress, isBeneficiary) =>
      logger.info("createCounterparty(" + createdByUserId + ", " + thisBankId + ", " + thisAccountId + ", " + thisViewId + ", " + name + ", "
                    + otherAccountRoutingScheme +", "+ otherAccountRoutingAddress +", "+ otherBankRoutingScheme +", "+ otherBankRoutingAddress +", "+ isBeneficiary+ ")")
      sender ! extractResult(mapper.createCounterparty(createdByUserId, thisBankId, thisAccountId, thisViewId, name,
                                           otherAccountRoutingScheme, otherAccountRoutingAddress, otherBankRoutingScheme, otherBankRoutingAddress,
                                           isBeneficiary))

    case cc.getOrCreateMetadata(originalPartyBankId: BankId, originalPartyAccountId: AccountId, otherParty: Counterparty) =>
      logger.info("getOrCreateMetadata(" + originalPartyBankId +", " +originalPartyAccountId+otherParty+")")
      sender ! extractResult(mapper.getOrCreateMetadata(originalPartyBankId: BankId, originalPartyAccountId: AccountId, otherParty: Counterparty))

    case cc.getMetadatas(originalPartyBankId: BankId, originalPartyAccountId: AccountId) =>
      logger.info("getOrCreateMetadata(" + originalPartyBankId +", "+originalPartyAccountId+")")
      sender ! extractResult(mapper.getMetadatas(originalPartyBankId: BankId, originalPartyAccountId: AccountId))

    case cc.getMetadata(originalPartyBankId: BankId, originalPartyAccountId: AccountId, counterpartyMetadataId: String) =>
      logger.info("getMetadata(" + originalPartyBankId +", "+originalPartyAccountId+")")
      sender ! extractResult(mapper.getMetadata(originalPartyBankId: BankId, originalPartyAccountId: AccountId, counterpartyMetadataId: String))

    case cc.getCounterparty(counterPartyId: String) =>
      logger.info("getCounterparty(" + counterPartyId +")")
      sender ! extractResult(mapper.getCounterparty(counterPartyId: String))

    case cc.getCounterparties(thisBankId: BankId, thisAccountId: AccountId, viewId: ViewId) =>
      logger.info("getCounterparties(" + thisBankId +")")
      sender ! extractResult(mapper.getCounterparties(thisBankId, thisAccountId, viewId))

    case cc.getCounterpartyByIban(iban: String) =>
      logger.info("getOrCreateMetadata(" + iban +")")
      sender ! extractResult(mapper.getCounterpartyByIban(iban: String))

    case cc.getPublicAlias(counterPartyId: String) =>
      logger.info("getPublicAlias(" + counterPartyId + ")")
      sender ! extractResult(mapper.getPublicAlias(counterPartyId))

    case cc.getPrivateAlias(counterPartyId: String) =>
      logger.info("getPrivateAlias(" + counterPartyId + ")")
      sender ! extractResult(mapper.getPrivateAlias(counterPartyId))

    case cc.getCorporateLocation(counterPartyId: String) =>
      logger.info("getCorporateLocation(" + counterPartyId + ")")
      sender ! extractResult(mapper.getCorporateLocation(counterPartyId))

    case cc.getPhysicalLocation(counterPartyId: String) =>
      logger.info("getPhysicalLocation(" + counterPartyId + ")")
      sender ! extractResult(mapper.getPhysicalLocation(counterPartyId))

    case cc.getOpenCorporatesURL(counterPartyId: String) =>
      logger.info("getOpenCorporatesURL(" + counterPartyId + ")")
      sender ! extractResult(mapper.getOpenCorporatesURL(counterPartyId))

    case cc.getImageURL(counterPartyId: String) =>
      logger.info("getImageURL(" + counterPartyId + ")")
      sender ! extractResult(mapper.getImageURL(counterPartyId))

    case cc.getUrl(counterPartyId: String) =>
      logger.info("getUrl(" + counterPartyId + ")")
      sender ! extractResult(mapper.getUrl(counterPartyId))

    case cc.getMoreInfo(counterPartyId: String) =>
      logger.info("getMoreInfo(" + counterPartyId + ")")
      sender ! extractResult(mapper.getMoreInfo(counterPartyId))

    case cc.addPrivateAlias(counterPartyId: String, alias: String) =>
      logger.info("addPrivateAlias(" + counterPartyId + ", " + alias +")")
      sender ! extractResult(mapper.addPrivateAlias(counterPartyId, alias))

    case cc.addPublicAlias(counterPartyId: String, alias: String) =>
      logger.info("addPublicAlias(" + counterPartyId + ", " + alias +")")
      sender ! extractResult(mapper.addPublicAlias(counterPartyId, alias))

    case cc.addURL(counterPartyId: String, url: String) =>
      logger.info("addURL(" + counterPartyId + ", " + url +")")
      sender ! extractResult(mapper.addURL(counterPartyId, url))

    case cc.addImageURL(counterPartyId: String, url: String) =>
      logger.info("addImageURL(" + counterPartyId + ", " + url +")")
      sender ! extractResult(mapper.addImageURL(counterPartyId, url))

    case cc.addOpenCorporatesURL(counterPartyId: String, url: String) =>
      logger.info("addOpenCorporatesURL(" + counterPartyId + ", " + url +")")
      sender ! extractResult(mapper.addOpenCorporatesURL(counterPartyId, url))

    case cc.addMoreInfo(counterPartyId : String, moreInfo: String) =>
      logger.info("addMoreInfo(" + counterPartyId + ", " + moreInfo +")")
      sender ! extractResult(mapper.addMoreInfo(counterPartyId, moreInfo))

    case cc.addPhysicalLocation(counterPartyId : String, userId: UserId, datePosted : Date, longitude : Double, latitude : Double) =>
      logger.info("addPhysicalLocation(" + counterPartyId + ", " + userId + ", " + datePosted + ", " + longitude + ", " + latitude +")")
      sender ! extractResult(mapper.addPhysicalLocation(counterPartyId, userId, datePosted, longitude, latitude))

    case cc.addCorporateLocation(counterPartyId : String, userId: UserId, datePosted : Date, longitude : Double, latitude : Double) =>
      logger.info("addCorporateLocation(" + counterPartyId + ", " + userId + ", " + datePosted + ", " + longitude + ", " + latitude +")")
      sender ! extractResult(mapper.addCorporateLocation(counterPartyId, userId, datePosted, longitude, latitude))

    case cc.deleteCorporateLocation(counterPartyId: String) =>
      logger.info("deleteCorporateLocation(" + counterPartyId + ")")
      sender ! extractResult(mapper.deleteCorporateLocation(counterPartyId))

    case cc.deletePhysicalLocation(counterPartyId: String) =>
      logger.info("deletePhysicalLocation(" + counterPartyId + ")")
      sender ! extractResult(mapper.deletePhysicalLocation(counterPartyId))

    case message => logger.info("[AKKA ACTOR ERROR - REQUEST NOT RECOGNIZED] " + message)

  }

}
