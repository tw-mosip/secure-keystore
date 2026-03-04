package com.reactnativesecurekeystore

interface SecureKeystoreV2 : SecureKeystore {

    fun storeKeyPair(publicKey: String, privateKey: String, alias: String)

    fun retrieveKeyPair(alias: String, context: Any): List<String>

    @Deprecated(
        "Use storeKeyPair instead",
        replaceWith = ReplaceWith("storeKeyPair(publicKey, privateKey, account)")
    )
    override fun storeGenericKey(publicKey: String, privateKey: String, account: String) {
        storeKeyPair(publicKey, privateKey, account)
    }

    @Deprecated(
        "Use retrieveKeyPair instead",
        replaceWith = ReplaceWith("retrieveKeyPair(account, context)")
    )
    override fun retrieveGenericKey(account: String, context: Any): List<String> {
        return retrieveKeyPair(account, context)
    }
}