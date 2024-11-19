package ma.ensa.soaptp.webservice

import ma.ensa.soaptp.beans.Compte
import ma.ensa.soaptp.beans.TypeCompte
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import java.text.SimpleDateFormat
import java.util.*

class SoapService {
    private val serviceNamespace = "http://ws.tpsoap.projet.ensa.com/"
    private val serviceUrl = "http://10.0.2.2:8082/services/ws"
    private val methodRetrieveAccounts = "getComptes"
    private val methodCreateAccount = "createCompte"
    private val methodDeleteAccount = "deleteCompte"
    private val soapActionHeader = ""

    @Throws(Exception::class)
    fun retrieveAccounts(): List<Compte> {
        val request = SoapObject(serviceNamespace, methodRetrieveAccounts)
        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
        envelope.dotNet = true
        envelope.setOutputSoapObject(request)
        val transport = HttpTransportSE(serviceUrl)
        transport.debug = true
        transport.call(null, envelope)
        val response = envelope.bodyIn as SoapObject
        val accounts: MutableList<Compte> = mutableListOf()

        for (i in 0 until response.propertyCount) {
            val soapAccount = response.getProperty(i) as SoapObject
            val account = Compte(
                id = soapAccount.getPropertySafely("id").toString().toLongOrNull(),
                solde = soapAccount.getPropertySafely("solde").toString().toDoubleOrNull() ?: 0.0,
                dateCreation = Date(),
                type = TypeCompte.valueOf(soapAccount.getPropertySafely("type").toString())
            )
            accounts.add(account)
        }
        return accounts
    }

    fun createAccount(balance: Double, accountType: TypeCompte): Boolean {
        val request = SoapObject(serviceNamespace, methodCreateAccount)
        request.addProperty("solde", balance.toString())
        request.addProperty("type", accountType.name)

        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
        envelope.dotNet = false
        envelope.setOutputSoapObject(request)

        return try {
            val transport = HttpTransportSE(serviceUrl)
            transport.debug = true
            transport.call(soapActionHeader, envelope)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteAccount(accountId: Long): Boolean {
        val request = SoapObject(serviceNamespace, methodDeleteAccount)
        request.addProperty("id", accountId.toString())

        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
        envelope.dotNet = false
        envelope.setOutputSoapObject(request)

        return try {
            val transport = HttpTransportSE(serviceUrl)
            transport.debug = true
            transport.call(soapActionHeader, envelope)
            envelope.response as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun mapToAccount(soapObject: SoapObject): Compte {
        return Compte(
            id = soapObject.getPropertyAsString("id").toLong(),
            solde = soapObject.getPropertyAsString("solde").toDouble(),
            dateCreation = SimpleDateFormat("yyyy-MM-dd").parse(soapObject.getPropertyAsString("dateCreation")),
            type = TypeCompte.valueOf(soapObject.getPropertyAsString("type"))
        )
    }
}