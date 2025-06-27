
/*package com.cnunez.docufast.common.firebase

import com.cnunez.docufast.common.dataclass.Organization
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class OrganizationDao(private val database: FirebaseDatabase) {

    private val orgsRef = database.reference.child("organizations")

    /**
     * Comprueba el valor de hasAdmin para una organización dada.
     * - Si no existe esa ruta, onResult(null).
     * - Si existe, onResult(true) o onResult(false).
     */
    fun getHasAdmin(orgId: String, onResult: (Boolean?) -> Unit) {
        orgsRef.child(orgId).child("hasAdmin")
            .get()
            .addOnSuccessListener { snap ->
                val flag = snap.getValue(Boolean::class.java)
                onResult(flag)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    /**
     * Crea la organización /organizations/{orgId} con name y hasAdmin = false.
     * Llama onSuccess() si se escribió correctamente, onFailure(e) en caso de error.
     */
    fun createOrganization(orgId: String, name: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val org = Organization(id = orgId, name = name, hasAdmin = false)
        orgsRef.child(orgId).setValue(org.toMap())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    /**
     * Marca /organizations/{orgId}/hasAdmin = true, para que ya no se permita
     * otro primer admin en esa organización.
     */
    fun markOrganizationHasAdmin(orgId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        orgsRef.child(orgId).child("hasAdmin").setValue(true)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}
*/